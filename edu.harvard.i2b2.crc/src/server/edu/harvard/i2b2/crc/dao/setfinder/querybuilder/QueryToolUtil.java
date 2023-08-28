/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;


import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.xml.XMLOperatorLookup;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.DateConstrainHandler;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainValueType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

/**
 * Main class to generate setfinder sql from query definition xml. $Id:
 * QueryToolUtil.java,v 1.18 2008/04/18 15:20:51 rk903 Exp $
 * 
 * @author chris,rkuttan
 */
public class QueryToolUtil extends CRCDAO {

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd-MMM-yyyy HH:mm:ss");

	DatabaseType dbType = DatabaseType.Oracle;
	Connection conn = null;

	// DATABASE VARIABLES
	private String FACT_TABLE = "dw_f_conc_noval";
	private String PATIENT_TABLE = "dw_dim_patient";
	private String ENCOUNTER_TABLE = "dw_dim_enct";
	private String CONCEPT_TABLE = "dw_dim_concept";
	private String PROVIDER_TABLE = "dw_dim_provider";
	private String PATIENTLISTS_TABLE = "dw_patientlists";
	private String ENCOUNTER_SHORTCUT_TABLE = "dw_dim_patient_enct";
	private String ENCOUNTER_DIM_ID = "encounter_id_e";
	private String ENCOUNTER_INOUT_COL = "inout_cd";
	private String ENCOUNTER_COMPANY_COL = "company_cd";
	private String ENCOUNTER_START_DATE = "start_date";
	private String ENCOUNTER_PATIENT_ID = "patient_id_e";
	private String CONCEPT_DIM_ID = "c_basecode";
	private String CONCEPT_DIM_PATH = "c_fullname";
	private String PROVIDER_DIM_ID = "c_basecode";
	private String PROVIDER_DIM_PATH = "c_fullname";
	private String PATIENTLISTS_DIM_ID = "patient_id_e";
	private String PATIENTLISTS_DIM_PATH = "filename";
	private String PATIENT_DIM_ID = "patient_id_e";
	private String FACT_VAL_TYPE = "valtype";
	private String FACT_TEXT_VAL = "tval";
	private String FACT_NUM_VAL = "nval";
	private String FACT_FLAG_VAL = "valueflag";
	private String FACT_CONCEPT_RANK = "principal_concept";
	private String FACT_START_DATE = "start_date";
	private String FACT_END_DATE = "end_date";
	private String FACT_ENCOUNTER_ID = "encounter_id_e";
	private String FACT_PATIENT_ID = "patient_id_e";
	private String FACT_CONCEPT_ID = "concept_id";
	private String FACT_PROVIDER_ID = "practitioner_id";
	private String TEMP_TABLE = "#t";
	private String TEMP_TABLE_PATIENT_ID = "patient_num";
	private String TEMP_TABLE_PATIENT_DATATYPE = "varchar(100)";
	private String TEMP_TABLE_ENCOUNTER_ID = "encounter_num";
	private String TEMP_TABLE_ENCOUNTER_DATATYPE = "varchar(100)";
	private String TEMP_PANELCOUNT_DATATYPE = "tinyint";
	private String TEMP_RETURN_TABLE = "#DX";
	private String METADATA_DATABASE = "MetaData_8086";
	private String METADATA_COLUMNNAME = "c_column_name";
	private String METADATA_TABLENAME = "c_table_name";
	private String METADATA_DIMCODE = "c_dim_code";
	private String METADATA_OPERATOR = "c_operator";
	private String METADATA_FULLNAME = "c_fullname";

	private CallOntologyUtil ontologyUtil = null;
	private DataSourceLookup dataSourceLookup = null;
	private StringBuffer ignoredItemMessageBuffer = new StringBuffer();

	public QueryToolUtil() {
		SetQueryDatabaseConstants(dbType);
	}

	public QueryToolUtil(DataSourceLookup dataSourceLookup) {
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		SetQueryDatabaseConstants(dbType);
		this.dataSourceLookup = dataSourceLookup;
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL) || dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SNOWFLAKE)) {
			TEMP_TABLE = "QUERY_GLOBAL_TEMP";
			TEMP_RETURN_TABLE = "DX";
			dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			TEMP_TABLE = "#global_temp_table";
			TEMP_RETURN_TABLE = "#dx";
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}
	}

	public String getIgnoredItemMessage() {
		if (this.ignoredItemMessageBuffer != null
				&& this.ignoredItemMessageBuffer.length() > 0) {
			return "Missing Concept in Ontology Cell : \n"
					+ this.ignoredItemMessageBuffer.toString();
		} else {
			return "";
		}

	}

	private void SetQueryDatabaseConstants(DatabaseType dbType) {
		if (dbType == DatabaseType.SqlServer) {
			FACT_TABLE = "dw_f_conc_noval";
			PATIENT_TABLE = "dw_dim_patient";
			ENCOUNTER_TABLE = "dw_dim_enct";
			CONCEPT_TABLE = "dw_dim_concept";
			PROVIDER_TABLE = "dw_dim_provider";
			PATIENTLISTS_TABLE = "dw_patientlists";
			ENCOUNTER_SHORTCUT_TABLE = "dw_dim_patient_enct";

			ENCOUNTER_DIM_ID = "encounter_id_e";
			ENCOUNTER_INOUT_COL = "inout_cd";
			ENCOUNTER_COMPANY_COL = "company_cd";
			ENCOUNTER_START_DATE = "start_date";
			ENCOUNTER_PATIENT_ID = "patient_id_e";

			CONCEPT_DIM_ID = "c_basecode";
			CONCEPT_DIM_PATH = "c_fullname";

			PROVIDER_DIM_ID = "c_basecode";
			PROVIDER_DIM_PATH = "c_fullname";

			PATIENT_DIM_ID = "patient_id_e";

			PATIENTLISTS_DIM_ID = "patient_id_e";
			PATIENTLISTS_DIM_PATH = "filename";

			FACT_VAL_TYPE = "valtype";
			FACT_TEXT_VAL = "tval";
			FACT_NUM_VAL = "nval";
			FACT_FLAG_VAL = "valueflag";
			FACT_CONCEPT_RANK = "principal_concept";
			FACT_START_DATE = "start_date";
			FACT_END_DATE = "end_date";
			FACT_ENCOUNTER_ID = "encounter_id_e";
			FACT_PATIENT_ID = "patient_id_e";
			FACT_CONCEPT_ID = "concept_id";
			FACT_PROVIDER_ID = "practitioner_id";

			TEMP_TABLE = "#t";
			TEMP_TABLE_PATIENT_ID = "patient_id_e";
			TEMP_TABLE_PATIENT_DATATYPE = "varchar(100)";
			TEMP_TABLE_ENCOUNTER_ID = "encounter_id_e";
			TEMP_TABLE_ENCOUNTER_DATATYPE = "varchar(100)";
			TEMP_PANELCOUNT_DATATYPE = "tinyint";
			TEMP_RETURN_TABLE = "#DX";

			METADATA_DATABASE = "";
			METADATA_COLUMNNAME = "c_column_name";
			METADATA_TABLENAME = "c_table_name";
			METADATA_DIMCODE = "c_dim_code";
			METADATA_OPERATOR = "c_operator";
			METADATA_FULLNAME = "c_fullname";
		} else if (dbType == DatabaseType.Oracle) {
			FACT_TABLE = "observation_fact";
			PATIENT_TABLE = "patient_dimension";
			ENCOUNTER_TABLE = "visit_dimension";
			CONCEPT_TABLE = "concept_dimension";
			PROVIDER_TABLE = "provider_dimension";
			PATIENTLISTS_TABLE = "dw_patientlists"; // not sure on this yet
			ENCOUNTER_SHORTCUT_TABLE = "visit_dimension";

			ENCOUNTER_DIM_ID = "encounter_num";
			ENCOUNTER_INOUT_COL = "inout_cd";
			ENCOUNTER_COMPANY_COL = "location_cd";
			ENCOUNTER_START_DATE = "start_date";
			ENCOUNTER_PATIENT_ID = "patient_num";

			CONCEPT_DIM_ID = "concept_cd";
			CONCEPT_DIM_PATH = "concept_path";

			PROVIDER_DIM_ID = "provider_id";
			PROVIDER_DIM_PATH = "provider_path";

			PATIENT_DIM_ID = "patient_num";

			PATIENTLISTS_DIM_ID = "patient_id_e"; // ??
			PATIENTLISTS_DIM_PATH = "filename"; // ??

			FACT_VAL_TYPE = "valtype_cd";
			FACT_TEXT_VAL = "tval_char";
			FACT_NUM_VAL = "nval_num";
			FACT_FLAG_VAL = "valueflag_cd";
			FACT_CONCEPT_RANK = "modifier_cd";
			FACT_START_DATE = "start_date";
			FACT_END_DATE = "end_date";
			FACT_ENCOUNTER_ID = "encounter_num";
			FACT_PATIENT_ID = "patient_num";
			FACT_CONCEPT_ID = "concept_cd";
			FACT_PROVIDER_ID = "provider_id";

			TEMP_TABLE = "QUERY_GLOBAL_TEMP";
			TEMP_TABLE_PATIENT_ID = "patient_num";
			TEMP_TABLE_PATIENT_DATATYPE = "number(22,0)";
			TEMP_TABLE_ENCOUNTER_ID = "encounter_num";
			TEMP_TABLE_ENCOUNTER_DATATYPE = "number(22,0)";
			TEMP_PANELCOUNT_DATATYPE = "number(5)";
			TEMP_RETURN_TABLE = "DX";

			// METADATA_DATABASE = "MetaData";
			// METADATA_DATABASE = "MetaData_8086";
			METADATA_COLUMNNAME = "c_columnname";
			METADATA_TABLENAME = "c_tablename";
			METADATA_DIMCODE = "c_dimcode";
			METADATA_OPERATOR = "c_operator";
			METADATA_FULLNAME = "c_fullname";
		}
	}

	protected long GetEstimatedSize(Connection conn, String theTableName,
			String theColumnName, String theOperator, String theData,
			long DBNumPatients) {
		long EstSize = 0;
		String sql = "";

		try {
			if (theTableName.equals(CONCEPT_TABLE)) {
				sql = "select sum(n.patient_count) n "
						+ "from rpdrconceptlookup n " + "where table_name = '"
						+ FACT_TABLE + "' " + "and column_name = '"
						+ FACT_CONCEPT_ID + "'" + "and concept_t_value in "
						+ "(select " + CONCEPT_DIM_ID + " from " + ""
						+ CONCEPT_TABLE + " c " + "where " + CONCEPT_DIM_PATH
						+ " " + theOperator + " " + theData + ")";

				java.sql.Statement st1 = conn.createStatement();
				ResultSet rs = st1.executeQuery(sql);

				if (rs.next()) {
					EstSize = rs.getLong("n");
				}

				rs.close();
				st1.close();
			} else if (theTableName.equals(PROVIDER_TABLE)) {
				sql = "select sum(n.patient_count) n "
						+ "from rpdrconceptlookup n " + "where table_name = '"
						+ FACT_TABLE + "' " + "and column_name = '"
						+ FACT_PROVIDER_ID + "'" + "and concept_t_value in "
						+ "(select " + PROVIDER_DIM_ID + " " + "from "
						+ PROVIDER_TABLE + " c " + "where " + PROVIDER_DIM_PATH
						+ " " + theOperator + " " + theData + ")";

				java.sql.Statement st1 = conn.createStatement();
				ResultSet rs = st1.executeQuery(sql);

				if (rs.next()) {
					EstSize = rs.getLong("n");
				}

				rs.close();
				st1.close();
			} else if (theTableName.equals(ENCOUNTER_TABLE)) {
				sql = "select sum(n.patient_count) n "
						+ "from rpdrconceptlookup n " + "where table_name = '"
						+ ENCOUNTER_TABLE + "' " + "and column_name = '"
						+ theColumnName + "' " + "and concept_t_value "
						+ theOperator + " " + theData;

				java.sql.Statement st1 = conn.createStatement();
				ResultSet rs = st1.executeQuery(sql);

				if (rs.next()) {
					EstSize = rs.getLong("n");
				}

				rs.close();
				st1.close();
			} else if (theTableName.equals(PATIENT_TABLE)) {
				if (theColumnName.equals("age_in_years_num")) {
					sql = "select sum(n.patient_count) n "
							+ "from rpdrconceptlookup n "
							+ "where table_name = '" + PATIENT_TABLE + "' "
							+ "and column_name = '" + theColumnName + "' "
							+ "and concept_n_value " + theOperator + " "
							+ theData;
				} else {
					sql = "select sum(n.patient_count) n "
							+ "from rpdrconceptlookup n "
							+ "where table_name = '" + PATIENT_TABLE + "' "
							+ "and column_name = '" + theColumnName + "' "
							+ "and concept_t_value " + theOperator + " "
							+ theData;
				}

				java.sql.Statement st1 = conn.createStatement();
				ResultSet rs = st1.executeQuery(sql);

				if (rs.next()) {
					EstSize = rs.getLong("n");
				}

				rs.close();
				st1.close();
			} else {
				EstSize = 1;
			}
		} catch (Exception e) {
			log.error("Unable to get Estimated Size: " + e.getMessage());
		}

		return EstSize;
	}

	private String buildDateConstrain(String dateColumn, String fromDateValue,
			String toDateValue) {

		String dateConstrain = " ";
		String serverType = dataSourceLookup.getServerType();
		String fromFormatDateValue = "";
		String toFormatDateValue = "";
		if (fromDateValue != null && fromDateValue.trim().length() > 0) {
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				fromFormatDateValue = " to_date('"
						+ fromDateValue.substring(0, fromDateValue.length())
						+ "','DD-MON-YYYY HH24:MI:SS')";
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				fromFormatDateValue = "  '" + fromDateValue + "'";
			}
		}
		if (toDateValue != null && toDateValue.trim().length() > 0) {
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				toFormatDateValue = " to_date('"
						+ toDateValue.substring(0, toDateValue.length())
						+ "','DD-MON-YYYY HH24:MI:SS')";
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				toFormatDateValue = "  '" + toDateValue + "'";
			}
		}
		if (fromDateValue != null
				&& toDateValue != null
				&& (fromDateValue.trim().length() > 0 && toDateValue.trim()
						.length() > 0)) {
			dateConstrain = " AND " + dateColumn + " between "
					+ fromFormatDateValue + " AND " + toFormatDateValue;
		}

		if (fromDateValue != null && fromDateValue.trim().length() > 0) {
			dateConstrain = " AND " + dateColumn + " >= " + fromFormatDateValue;
		}
		if (toDateValue != null && toDateValue.trim().length() > 0) {
			dateConstrain = " AND " + dateColumn + " <= " + toFormatDateValue;
		}

		return dateConstrain;

	}

	private String buildDateConstrainNew(String fromDateColumn,
			String toDateColumn, String fromInclusive, String toInclusive,
			String fromDateValue, String toDateValue) {

		String dateConstrain = " ";
		String serverType = dataSourceLookup.getServerType();
		String fromFormatDateValue = "";
		String toFormatDateValue = "";

		if (fromInclusive == null) {
			fromInclusive = "yes";
		}
		if (toInclusive == null) {
			toInclusive = "yes";
		}
		if (fromDateValue != null && fromDateValue.trim().length() > 0) {
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				fromFormatDateValue = " to_date('"
						+ fromDateValue.substring(0, fromDateValue.length())
						+ "','DD-MON-YYYY HH24:MI:SS')";
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				fromFormatDateValue = "  '" + fromDateValue + "'";
			}
		}
		if (toDateValue != null && toDateValue.trim().length() > 0) {
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				toFormatDateValue = " to_date('"
						+ toDateValue.substring(0, toDateValue.length())
						+ "','DD-MON-YYYY HH24:MI:SS')";
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				toFormatDateValue = "  '" + toDateValue + "'";
			}
		}
		if (fromDateValue != null
				&& toDateValue != null
				&& (fromDateValue.trim().length() > 0 && toDateValue.trim()
						.length() > 0)) {
			if (fromDateColumn.equalsIgnoreCase(toDateColumn)
					&& fromInclusive.equalsIgnoreCase("yes")
					&& toInclusive.equalsIgnoreCase("yes")) {
				dateConstrain = " AND " + fromDateColumn + " between "
						+ fromFormatDateValue + " AND " + toFormatDateValue;
				return dateConstrain;
			}
		}

		if (fromDateValue != null && fromDateValue.trim().length() > 0) {
			dateConstrain = " AND " + fromDateColumn;
			if (fromInclusive.equalsIgnoreCase("yes")) {
				dateConstrain += ">=" + fromFormatDateValue;
			} else {
				dateConstrain += ">" + fromFormatDateValue;
			}

		}
		if (toDateValue != null && toDateValue.trim().length() > 0) {
			dateConstrain += " AND " + toDateColumn;
			if (toInclusive.equalsIgnoreCase("yes")) {
				dateConstrain += " <= " + toFormatDateValue;
			} else {
				dateConstrain += " < " + toFormatDateValue;
			}
		}

		return dateConstrain;

	}

	private ResultEntry[] OrderPanels(ArrayList panelEntries,
			ArrayList itemEntries, boolean sameVisit, int specificity) {
		try {
			Integer firstPanel = -1;

			// first set all inverted panels to one panel
			ArrayList invertPanels = new ArrayList();
			Integer minInvert = -1;

			for (int i = 0; i < panelEntries.size(); i++) {
				PanelEntry p = (PanelEntry) panelEntries.get(i);

				if (p.Invert == 1) {
					invertPanels.add(p.Panel);

					if (minInvert < 0) {
						minInvert = p.Panel;
					} else if (minInvert > p.Panel) {
						minInvert = p.Panel;
					}
				}
			}

			/*
			 * if (minInvert>=0) { for (int i=0; i<invertPanels.size(); i++)
			 * ((PanelEntry) panelEntries.get((Integer)
			 * invertPanels.get(i))).Panel = minInvert; }
			 */

			// now, get the distinct items and put them into v hashtable
			Hashtable v = new Hashtable();

			for (int i = 0; i < itemEntries.size(); i++) {
				ItemEntry t = (ItemEntry) itemEntries.get(i);

				if ((minInvert >= 0) && (invertPanels.contains(t.Panel))
						&& (t.Panel != minInvert)) {
					t.Panel = minInvert;
				}

				if (!v.containsKey(t.Panel)) {
					ArrayList vItems = new ArrayList();
					vItems.add(t);
					v.put(t.Panel, vItems);
				} else {
					ArrayList vItems = (ArrayList) v.get(t.Panel);

					if (!vItems.contains(t)) {
						vItems.add(t);
					}
				}
			}

			// update panelEntries with the count of items from v
			for (int p = 0; p < panelEntries.size(); p++) {
				PanelEntry panel = (PanelEntry) panelEntries.get(p);
				ArrayList items = (ArrayList) v.get(panel.Panel);

				if (items != null) {
					panel.Items = items.size();
				} else {
					panel.Items = 0;
				}
			}

			// now, find first Panel
			Comparator comp = new PanelEntryComparator();
			Collections.sort(panelEntries, comp);
			((PanelEntry) panelEntries.get(0)).FirstPanel = 1;
			firstPanel = ((PanelEntry) panelEntries.get(0)).Panel;

			int e = 0;

			if (sameVisit) {
				// find count of encounter and concept panels
				for (Enumeration i = v.keys(); i.hasMoreElements();) {
					ArrayList vItems = (ArrayList) v.get(i.nextElement());

					for (int vi = 0; vi < vItems.size(); vi++) {
						ItemEntry t = (ItemEntry) vItems.get(vi);

						if ((t.TableName.equals(CONCEPT_TABLE))
								|| (t.TableName.equals(ENCOUNTER_TABLE))) {
							e++;
							vi = vItems.size();
						}
					}
				}
			}

			// since we're no longer ordering ids by these columns this update
			// isn't valid
			/*
			 * itemSql.append("update @v set sqlx = 1 " + "where panel <> @f " +
			 * "and lower(thetable) = '" + PATIENT_TABLE + "' " + "and
			 * lower(thefield) in " +
			 * "('age_in_years_num','vital_status_cd','sex_cd','race_cd','vip_cd')
			 * \r\n");
			 */
			if (e > 1) {
				for (Enumeration i = v.keys(); i.hasMoreElements();) {
					Integer panel = (Integer) i.nextElement();

					if (!panel.equals(firstPanel)) {
						ArrayList vItems = (ArrayList) v.get(panel);

						for (int vi = 0; vi < vItems.size(); vi++) {
							ItemEntry t = (ItemEntry) vItems.get(vi);

							if (t.TableName.equals(ENCOUNTER_TABLE)) {
								t.SqlX = 1;
							}
						}
					}
				}
			}

			// set all short values
			// all shorts stands for shortcut values...we can't use shortcuts
			// with
			// the current database structure
			/*
			 * for (int i=0; i<panelEntries.size(); i++) { PanelEntry p =
			 * (PanelEntry) panelEntries.get(i); if (!p.equals(firstPanel)) {
			 * ArrayList vItems = (ArrayList) v.get(p.Panel); int xitems = 0;
			 * for (int vi=0; vi<vItems.size(); vi++) if (((ItemEntry)
			 * vItems.get(vi)).SqlX > 0) xitems++;
			 * 
			 * if (p.Items==xitems) p.AllShort = 1; }
			 * 
			 * if ((p.AllShort==1)||(p.FirstPanel==1)) p.ForInsert = 1; }
			 */
			comp = new QPanelEntryComparator();
			Collections.sort(panelEntries, comp);

			ArrayList q = new ArrayList();

			for (int i = 0; i < panelEntries.size(); i++) {
				PanelEntry p = (PanelEntry) panelEntries.get(i);
				// RAJ comment for panel with not item
				// if (p.Items > 0) {
				// IdentityPanelEntry ip = new IdentityPanelEntry(p);
				// q.add(ip);
				// }
				IdentityPanelEntry ip = new IdentityPanelEntry(p);
				q.add(ip);
			}

			ArrayList resultArray = new ArrayList();

			for (int i = 0; i < q.size(); i++) {
				IdentityPanelEntry p = (IdentityPanelEntry) q.get(i);
				ArrayList vItems = (ArrayList) v.get(p.OldPanel);

				if (vItems == null) {
					resultArray.add(new ResultEntry(e, p, new ItemEntry()));
				} else {
					for (int vi = 0; vi < vItems.size(); vi++) {
						resultArray.add(new ResultEntry(e, p,
								((ItemEntry) vItems.get(vi))));
					}
				}
			}

			if (specificity > 1) {
				comp = new ResultEntryComparator(PATIENT_TABLE,
						ENCOUNTER_TABLE, PROVIDER_TABLE);
			} else {
				comp = new ResultEntryComparator(PATIENT_TABLE,
						ENCOUNTER_TABLE, CONCEPT_TABLE);
			}

			Collections.sort(resultArray, comp);

			ResultEntry[] rs = new ResultEntry[resultArray.size()];

			return (ResultEntry[]) resultArray.toArray(rs);
		} catch (Exception e) {
			log.error(e.getMessage());

			return null;
		}
	}

	public class ItemMetaData {
		public String QueryTable;
		public String QueryColumn;
		public String QueryOp;
		public String QueryCode;
		public String QueryFactTableColumn;
		public String QueryColumnDataType;
	}

	public enum DatabaseType {
		SqlServer, Oracle;
	}

	public enum XmlFormat {
		RPDR, I2B2;
	}

	private PanelEntry callPanelIfNoItem(boolean doInvert,
			int totalItemOccurance, String totalItemOccurrenceOperator,
			long EstQuerySize, long EstPanelSize, int panelNumber) {
		PanelEntry panel = new PanelEntry();
		if (doInvert) {
			EstQuerySize = EstQuerySize + (1 - EstPanelSize);

			// EstQuerySize = EstQuerySize * (1 - EstPanelSize);

			panel.Panel = panelNumber;
			panel.Invert = 1;
			panel.EstPanelSize = (1 - EstPanelSize);
			panel.Items = 0;
			panel.AllShort = 0;
			panel.ForInsert = 0;
			panel.FirstPanel = 0;
			panel.totalItemOccurrences = totalItemOccurance;
			panel.totalItemOccurrencesOperator = totalItemOccurrenceOperator;

		} else {
			EstQuerySize = EstQuerySize + EstPanelSize;

			panel.Panel = panelNumber;
			panel.Invert = 0;
			panel.EstPanelSize = EstPanelSize;
			panel.Items = 0;
			panel.AllShort = 0;
			panel.ForInsert = 0;
			panel.FirstPanel = 0;
			panel.totalItemOccurrences = totalItemOccurance;
			panel.totalItemOccurrencesOperator = totalItemOccurrenceOperator;

		}
		return panel;

	}

}
