/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
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

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
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
						DAOFactoryHelper.POSTGRESQL)) {
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

	public String generateSQL(Connection conn, String queryXML,
			boolean encounterSetFlag) throws I2B2DAOException {
		String sql = null;
		try {
			this.conn = conn;

		//	ontologyUtil = new CallOntologyUtil(queryXML);

			org.jdom.Document controlDoc = getDocument(queryXML);
			String dataRequested = " ";
			if (encounterSetFlag) {
				dataRequested = "PE";
			}

			Integer iteration = new Integer(0);
			sql = ProcessControlFileI2B2("", controlDoc, dataRequested,
					iteration);
	//	} catch (JAXBUtilException jEx) {
	//		throw new I2B2DAOException(jEx.getMessage() + jEx);
		} catch (I2B2Exception iEx) {
			iEx.printStackTrace();
			throw new I2B2DAOException(iEx.getMessage() + iEx);
		}
		return sql;
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

	public org.jdom.Document getDocument(String queryXML)
			throws I2B2DAOException {
		SAXBuilder parser = new SAXBuilder();
		parser
				.setFeature(
						"http://apache.org/xml/features/standard-uri-conformant",
						false);

		StringReader strReader = new StringReader(queryXML);
		StreamSource s = new StreamSource(strReader);
		org.jdom.Document controlDoc = null;

		try {
			controlDoc = parser.build(strReader);
		} catch (JDOMException e) {
			log.error("", e);
			throw new I2B2DAOException("JDOMException", e);
		} catch (IOException e) {
			log.error("IOException ", e);
			throw new I2B2DAOException("IOException", e);
		}

		// controlDoc = parser.build(s.getInputStream());
		parser = null;

		return controlDoc;
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

	protected String ProcessControlFileI2B2(String controlFilePath, // only
			// needed
			// for query
			// in query,
			// path to
			// folder
			// that
			// contains
			// controlfiles
			org.jdom.Document controlDoc, // main control document
			String dataRequested, // type of data requested: either "P" for
			// patient or "PE" for patient and encounter
			Integer iteration // number of calls into ProcessControlFile, used
	// by query in query to uniquely identify temp
	// tables
	) throws I2B2DAOException {
		try {
			String querySQL = "";
			String tableSuffix = "";

			if (iteration.intValue() > 0) {
				tableSuffix = iteration.toString();
			}

			boolean sameVisit = false;

			org.jdom.Element i2b2Xml = controlDoc.getRootElement(); // i2b2:i2b2
			org.jdom.Element bodyXml = i2b2Xml.getChild("message_body");
			List child = bodyXml.getChildren();

			org.jdom.Element querySetXml = (org.jdom.Element) child.get(1);

			org.jdom.Element controlXml = querySetXml.getChild(
					"query_definition", null);
			String qTiming = controlXml.getChildText("query_timing");

			if ((qTiming != null) && (qTiming.equals("SAME"))) {
				sameVisit = true;
			} else {
				sameVisit = false;
			}

			String theQueryDateFrom = controlXml
					.getChildText("query_date_from");

			if (theQueryDateFrom == null) {
				theQueryDateFrom = "";
			}

			String theQueryDateTo = controlXml.getChildText("query_date_to");

			if (theQueryDateTo == null) {
				theQueryDateTo = "";
			}

			String sSpec = controlXml.getChildText("specificity_scale");
			int specificity = 0;

			if ((sSpec != null) && (sSpec.trim().length() > 0)) {
				specificity = Integer.parseInt(sSpec);
				specificity++;
			}

			List<Element> panelList = controlXml.getChildren("panel");

			long EstSize = 0;
			long EstPanelSize = 0;
			long EstQuerySize = 1;

			// long DBNumPatients = 0;
			long DBNumPatients = 3638280;
			int i = 0;
			int j = 0;
			int origIteration = iteration.intValue();
			boolean doInvert = false;
			ArrayList t = new ArrayList();
			ArrayList p = new ArrayList();

			DateConstrainHandler dateConstrainHandler = new DateConstrainHandler(
					dataSourceLookup);
			// sort panel based on item total num
			SortPanel sortPanel = new SortPanel();
			List<Element> sortPanelList = null; //sortPanel.getSortedPanelList(
	//				panelList, ontologyUtil);

			for (Iterator itr = sortPanelList.iterator(); itr.hasNext();) {
				i++;

				Element panelXml = (org.jdom.Element) itr.next();

				String invertString = panelXml.getChildText("invert");

				if ((invertString != null) && (invertString.equals("1"))) {
					doInvert = true;
				} else {
					doInvert = false;
				}

				String thePanelDateFrom = null, thePanelDateTo = null;
				String thePanelDateFromInclusive = null, thePanelDateToInclusive = null;
				String thePanelDateFromTime = null, thePanelDateToTime = null;
				String panelDateConstrain = "";
				Element panelDateFromElement = panelXml
						.getChild("panel_date_from");
				if (panelDateFromElement != null) {
					thePanelDateFromInclusive = panelDateFromElement
							.getAttributeValue("inclusive");
					thePanelDateFromTime = panelDateFromElement
							.getAttributeValue("time");
					if (thePanelDateFromTime != null
							&& thePanelDateFromTime
									.equalsIgnoreCase("end_date")) {
						thePanelDateFromTime = this.FACT_END_DATE;
					} else {
						thePanelDateFromTime = this.FACT_START_DATE;
					}
					thePanelDateFrom = panelDateFromElement.getText();
					if (thePanelDateFrom == null) {
						thePanelDateFrom = "";
					} else {
						DatatypeFactory dataTypeFactory = DatatypeFactory
								.newInstance();
						XMLGregorianCalendar cal = dataTypeFactory
								.newXMLGregorianCalendar(thePanelDateFrom);
						thePanelDateFrom = dateFormat.format(cal
								.toGregorianCalendar().getTime());
					}
				}

				Element panelDateToElement = panelXml.getChild("panel_date_to");
				if (panelDateToElement != null) {
					thePanelDateToInclusive = panelDateToElement
							.getAttributeValue("inclusive");
					thePanelDateToTime = panelDateToElement
							.getAttributeValue("time");
					if (thePanelDateToTime != null
							&& thePanelDateToTime.equalsIgnoreCase("end_date")) {
						thePanelDateToTime = this.FACT_END_DATE;
					} else {
						thePanelDateToTime = this.FACT_START_DATE;
					}
					thePanelDateTo = panelDateToElement.getText();
					if (thePanelDateTo == null) {
						thePanelDateTo = "";
					} else {
						DatatypeFactory dataTypeFactory = DatatypeFactory
								.newInstance();
						XMLGregorianCalendar cal = dataTypeFactory
								.newXMLGregorianCalendar(thePanelDateTo);
						thePanelDateTo = dateFormat.format(cal
								.toGregorianCalendar().getTime());
					}
				}

				panelDateConstrain = buildDateConstrainNew(
						thePanelDateFromTime, thePanelDateToTime,
						thePanelDateFromInclusive, thePanelDateToInclusive,
						thePanelDateFrom, thePanelDateTo);

				String totalItemOccuranceStr = null;
				Element totalItemOccurrencesElement = panelXml
						.getChild("total_item_occurrences");
				int totalItemOccurance = 0;
				String totalItemOccurrenceOperator = ">=";
				if (totalItemOccurrencesElement != null) {
					String totalItemOccurrenceStr = totalItemOccurrencesElement
							.getText();
					if (totalItemOccurrenceStr != null) {
						totalItemOccurance = Integer
								.parseInt(totalItemOccurrenceStr);
						totalItemOccurrenceOperator = totalItemOccurrencesElement
								.getAttributeValue("operator");

						if (totalItemOccurrenceOperator == null) {
							totalItemOccurrenceOperator = ">=";
						} else {
							totalItemOccurrenceOperator = XMLOperatorLookup
									.getComparisonOperatorFromAcronum(totalItemOccurrenceOperator);
						}
					}

				}

				// totalItemOccuranceStr = panelXml
				// .getChildText("total_item_occurrences");

				// if (totalItemOccuranceStr != null) {
				// totalItemOccurance = Integer
				// .parseInt(totalItemOccuranceStr);
				// }

				List itemList = panelXml.getChildren("item");
				String sql0 = "";
				String sql1 = "";
				EstPanelSize = 0;
				boolean singleValidItem = false;
				ItemMetaData itemMeta = null;
				for (Iterator itItem = itemList.iterator(); itItem.hasNext();) {
					j++;

					Element itemXml = (org.jdom.Element) itItem.next();
					String itemKey = itemXml.getChildText("item_key");

					String noLockSqlServer = " ";
					if (this.dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.SQLSERVER)) {
						noLockSqlServer = " WITH(NOLOCK) ";
					}
					String theTable = null;
					String theColumn = null;
					// if item key start with ""
					if (itemKey.startsWith("patient_set_coll_id:")) {
						singleValidItem = true;
						String[] patientSetId = itemKey.split(":");
						if (patientSetId[1] != null) {
							sql0 = " patient_num "
									+ " IN (select patient_num  from "
									+ getDbSchemaName()
									+ "qt_patient_set_collection " + " c "
									+ noLockSqlServer
									+ " where patient_set_coll_id = "
									+ patientSetId[1] + ")";
						}
					} else if (itemKey.startsWith("patient_enc_coll_id:")) {
						singleValidItem = true;
						String[] encounterSetId = itemKey.split(":");
						if (encounterSetId[1] != null) {
							sql0 = " encounter_num "
									+ " IN (select encounter_num  from "
									+ getDbSchemaName()
									+ "qt_patient_enc_collection " + " c "
									+ noLockSqlServer
									+ " where patient_enc_coll_id = "
									+ encounterSetId[1] + ")";
						}
					} else {
						String itemClass = itemXml.getChildText("class");
						ConceptType conceptType = null;
						//ontologyUtil
						//		.callOntology(itemKey);

						itemMeta = new ItemMetaData();

						if (conceptType == null) {
							// add it the message
							ignoredItemMessageBuffer.append("\n [" + itemKey
									+ "] in panel #" + i + "\n");
							itemMeta.QueryTable = "";
						} else {
							singleValidItem = true;
							itemMeta.QueryTable = conceptType.getTablename();
							itemMeta.QueryColumn = conceptType.getColumnname();
							itemMeta.QueryOp = conceptType.getOperator();
							itemMeta.QueryCode = conceptType.getDimcode();
							itemMeta.QueryFactTableColumn = conceptType
									.getFacttablecolumn();
							itemMeta.QueryColumnDataType = conceptType
									.getColumndatatype();

							if ((itemMeta.QueryOp != null)
									&& (itemMeta.QueryOp.toUpperCase()
											.equals("LIKE"))) {
								if (itemMeta.QueryCode.lastIndexOf('\\') == itemMeta.QueryCode
										.length() - 1) {
									itemMeta.QueryCode = itemMeta.QueryCode
											+ "%";
								} else {
									log
											.debug("Adding \\ at the end of the Concept path ");
									itemMeta.QueryCode = itemMeta.QueryCode
											+ "\\%";
								}
							}
						}
						theTable = itemMeta.QueryTable;
						if (theTable != null) {
							theTable = theTable.toLowerCase();
						} else {
							theTable = "";
						}

						theColumn = itemMeta.QueryColumn;

						if (theColumn != null) {
							theColumn.toLowerCase();
						} else {
							theColumn = "";
						}

						String theOperator = itemMeta.QueryOp;

						if (theOperator == null) {
							theOperator = "";
						}

						String theData = itemMeta.QueryCode;

						if (theData == null) {
							theData = "";
						}

						EstSize = 0;
						sql0 = "";
						sql1 = "";

						if (theOperator.toUpperCase().equals("IN")) {
							if (itemMeta.QueryColumnDataType
									.equalsIgnoreCase("T")) {
								theData = SqlClauseUtil.buildINClause(theData,
										true);
							} else if (itemMeta.QueryColumnDataType
									.equalsIgnoreCase("N")) {
								theData = SqlClauseUtil.buildINClause(theData,
										false);
							}
							theData = "(" + theData + ")";
						} else {
							theData = theData.replaceAll("'", "''");
							if (itemMeta.QueryColumnDataType
									.equalsIgnoreCase("T")) {
								theData = "'" + theData + "'";
							}
						}

						//
						// <C_OPERATOR>IN</C_OPERATOR>
						// <C_DIMCODE>'NA','NAT. AM.','NI'</C_DIMCODE>
						// <C_FACTTABLECOLUMN>PATIENT_NUM</C_FACTTABLECOLUMN>
						// <C_TABLENAME>PATIENT_DIMENSION</C_TABLENAME>
						// <C_COLUMNNAME>RACE_CD</C_COLUMNNAME>

						itemMeta.QueryTable = conceptType.getTablename();
						itemMeta.QueryColumn = conceptType.getColumnname();
						itemMeta.QueryOp = conceptType.getOperator();
						itemMeta.QueryCode = conceptType.getDimcode();
						itemMeta.QueryFactTableColumn = conceptType
								.getFacttablecolumn();

						sql0 = itemMeta.QueryFactTableColumn + " IN (select "
								+ itemMeta.QueryFactTableColumn + " from "
								+ getDbSchemaName() + itemMeta.QueryTable
								+ " c " + noLockSqlServer + " where "
								+ itemMeta.QueryColumn + " " + itemMeta.QueryOp
								+ " " + theData + ")";
					}
					// date constraint start
					String itemDateConstrain = "";
					List children = itemXml.getChildren("constrain_by_date");

					for (Iterator iterator = children.iterator(); iterator
							.hasNext();) {
						Element consDate1 = (Element) iterator.next();
						String dateFromTime = null, dateToTime = null;
						String dateFromInclusive = null, dateToInclusive = null;
						String dateFromValue = null, dateToValue = null;
						Element dateFromElement = null, dateToElement = null;
						// listElements(n);
						if (consDate1 != null) {
							dateFromElement = consDate1.getChild("date_from");
							if (dateFromElement != null) {
								dateFromValue = dateFromElement.getText();
								dateFromTime = dateFromElement
										.getAttributeValue("time");
								if (dateFromTime != null
										&& dateFromTime
												.equalsIgnoreCase("end_date")) {
									dateFromTime = this.FACT_END_DATE;
								} else {
									dateFromTime = this.FACT_START_DATE;
								}

								dateFromInclusive = dateFromElement
										.getAttributeValue("inclusive");
								if (dateFromValue == null) {
									dateFromValue = "";
								} else {
									DatatypeFactory dataTypeFactory = DatatypeFactory
											.newInstance();
									XMLGregorianCalendar cal = dataTypeFactory
											.newXMLGregorianCalendar(dateFromValue);
									dateFromValue = dateFormat.format(cal
											.toGregorianCalendar().getTime());
								}
							}

							dateToElement = consDate1.getChild("date_to");
							if (dateToElement != null) {
								dateToValue = dateToElement.getText();
								dateToTime = dateToElement
										.getAttributeValue("time");
								dateToInclusive = dateToElement
										.getAttributeValue("inclusive");

								if (dateToTime != null
										&& dateToTime
												.equalsIgnoreCase("end_date")) {
									dateToTime = this.FACT_END_DATE;
								} else {
									dateToTime = this.FACT_START_DATE;
								}
								if (dateToValue == null) {
									dateToValue = "";
								} else {
									DatatypeFactory dataTypeFactory = DatatypeFactory
											.newInstance();
									XMLGregorianCalendar cal = dataTypeFactory
											.newXMLGregorianCalendar(dateToValue);
									dateToValue = dateFormat.format(cal
											.toGregorianCalendar().getTime());
								}
							}
						}
						itemDateConstrain += buildDateConstrainNew(
								dateFromTime, dateToTime, dateFromInclusive,
								dateToInclusive, dateFromValue, dateToValue);

					}

					// date constrain end

					//
					/*
					 * if (theTable.toLowerCase().equals(CONCEPT_TABLE)) { sql0
					 * = FACT_CONCEPT_ID + " IN (select " + CONCEPT_DIM_ID +
					 * " from " + getDbSchemaName() + CONCEPT_TABLE + " c " +
					 * noLockSqlServer + " where " + CONCEPT_DIM_PATH + " " +
					 * theOperator + " " + theData + ")"; } else if
					 * (theTable.equals(PROVIDER_TABLE)) { sql0 =
					 * FACT_PROVIDER_ID + " IN (SELECT " + PROVIDER_DIM_ID +
					 * " FROM " + getDbSchemaName() + PROVIDER_TABLE + " c " +
					 * noLockSqlServer + " where " + PROVIDER_DIM_PATH + " " +
					 * theOperator + " " + theData + ")"; }
					 */
					//

					StringBuilder theFilter = new StringBuilder();

					if (itemDateConstrain != null) {
						theFilter.append(itemDateConstrain);
					}

					if (panelDateConstrain != null) {
						theFilter.append(panelDateConstrain);
					}
					String queryDateConstrain = buildDateConstrain(
							FACT_START_DATE, theQueryDateFrom, theQueryDateTo);
					if (queryDateConstrain != null) {
						theFilter.append(queryDateConstrain);
					}

					List constraintList = itemXml
							.getChildren("constrain_by_value");

					if (constraintList != null) {
						for (Iterator itConstraint = constraintList.iterator(); itConstraint
								.hasNext();) {
							Element constraintXml = (org.jdom.Element) itConstraint
									.next();

							String theValueType = constraintXml
									.getChildText("value_type");

							if (theValueType == null) {
								theValueType = "";
							}

							String theValueOp = constraintXml
									.getChildText("value_operator");

							if (theValueOp == null) {
								theValueOp = "";
							}

							String theValueCons = constraintXml
									.getChildText("value_constraint");

							if (theValueCons == null) {
								theValueCons = "";
							}

							if (theValueType.equalsIgnoreCase("T")
									|| theValueType
											.equalsIgnoreCase(ConstrainValueType.TEXT
													.value())) {
								if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.EQ
												.value())) {
									theFilter.append(" AND "
											+ FACT_VAL_TYPE
											+ " = 'T' and "
											+ FACT_TEXT_VAL
											+ " = '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.NE
												.value())) {
									theFilter.append(" AND "
											+ FACT_VAL_TYPE
											+ " = 'T' and "
											+ FACT_TEXT_VAL
											+ " <> '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.LIKE
												.value())) {
									theFilter.append(" AND "
											+ FACT_VAL_TYPE
											+ " = 'T' and "
											+ FACT_TEXT_VAL
											+ " LIKE  '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "%'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.IN
												.value())) {
									theValueCons = SqlClauseUtil.buildINClause(
											theValueCons, true);

									theFilter.append(" AND " + FACT_VAL_TYPE
											+ " = 'T' and " + FACT_TEXT_VAL
											+ " IN  (" + theValueCons + ")");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.BETWEEN
												.value())) {
									theValueCons = SqlClauseUtil
											.buildBetweenClause(theValueCons);
									theFilter.append(" AND " + FACT_VAL_TYPE
											+ " = 'T' and " + FACT_TEXT_VAL
											+ " BETWEEN  " + theValueCons);
								}
							} else if (theValueType
									.equalsIgnoreCase(ConstrainValueType.FLAG
											.value())) {
								// theFilter.append(" AND " + FACT_VAL_TYPE
								// + " = 'F'");
								if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.EQ
												.value())) {
									theFilter.append(" AND "
											+ FACT_FLAG_VAL
											+ " = '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.NE
												.value())) {
									theFilter.append(" AND "
											+ FACT_FLAG_VAL
											+ " <> '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.IN
												.value())) {
									theValueCons = SqlClauseUtil.buildINClause(
											theValueCons, true);
									theFilter.append(" AND " + FACT_FLAG_VAL
											+ " IN (" + theValueCons + ")");
								}

							} else if (theValueType
									.equalsIgnoreCase(ConstrainValueType.NUMBER
											.value())) {
								String prefixNumberConstrain = ("  "
										+ FACT_VAL_TYPE + " = 'N'");
								// to make the sql injection proof
								theValueCons.replaceAll("'", "''");
								if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.GT
												.value())) {
									// [VALTYPE_CD = 'N' AND NVAL_NUM > NNN
									// AND TVAL_CHAR IN ( 'E','GE') OR (
									// VALTYPE_CD = 'N' AND NVAL_NUM >= NNN
									// AND TVAL_CHAR ='G'))]
									theFilter.append(" AND (("
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " > "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " IN ('GE','E'))"
											+ " OR " + " ("
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " >= "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " = 'G'))");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.GE
												.value())) {
									// [VALTYPE_CD = 'N' AND NVAL_NUM >= NNN
									// AND TVAL_CHAR IN ( 'E','GE','G')]
									theFilter.append("  AND "
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " >= "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL
											+ " IN ('G','E','GE')");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.EQ
												.value())) {
									// [VALTYPE_CD ='N' AND NVAL_NUM = NNN
									// AND TVAL_CHAR='E']
									theFilter.append("  AND "
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " = "
											+ theValueCons + " AND  "
											+ FACT_TEXT_VAL + " = 'E'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.NE
												.value())) {
									// [(VALTYPE_CD ='N' AND NVAL_NUM <> NNN
									// AND TVAL_CHAR <> 'NE') OR (VALTYPE_CD
									// ='N' AND NVAL_NUM = NNN AND TVAL_CHAR
									// = 'NE') ]
									theFilter.append("AND (("
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " <> "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " <> 'NE')"
											+ " OR " + " ("
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " = "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " = 'NE'))");

								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.LT
												.value())) {
									// [VALTYPE_CD = 'N' AND NVAL_NUM < NNN
									// AND TVAL_CHAR IN ( 'E','LE') OR (
									// VALTYPE_CD = 'N' AND NVAL_NUM <= NNN
									// AND TVAL_CHAR ='L'))]
									theFilter.append("AND (("
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " < "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " IN ('LE','E'))"
											+ " OR " + " ("
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " <= "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " = 'L'))");

								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.LE
												.value())) {
									// [VALTYPE_CD = 'N' AND NVAL_NUM <= NNN
									// AND TVAL_CHAR IN ( 'E','LE','L')]
									theFilter.append(" AND "
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " <= "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL
											+ " IN ('L','E','LE')");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.BETWEEN
												.value())) {
									// [VALTYPE_CD='N' AND NVAL_NUM BETWEEN
									// NNN1 TO NNN2 AND TVAL_CHAR ='E']
									theValueCons = SqlClauseUtil
											.buildBetweenClause(theValueCons);
									theFilter.append("AND "
											+ prefixNumberConstrain + " AND "
											+ FACT_NUM_VAL + " BETWEEN "
											+ theValueCons + " AND "
											+ FACT_TEXT_VAL + " = 'E'");
								}

							} else if (theValueType
									.equalsIgnoreCase(ConstrainValueType.MODIFIER
											.value())) {
								String modifierPrefix = (" AND "
										+ FACT_VAL_TYPE + " = 'M' ");
								// VALTYPE_CD = 'M' AND TVAL_CHAR =
								// 'somevalue'
								if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.EQ
												.value())) {
									theFilter.append(modifierPrefix
											+ " and "
											+ FACT_TEXT_VAL
											+ " = '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.NE
												.value())) {
									theFilter.append(modifierPrefix
											+ " and "
											+ FACT_TEXT_VAL
											+ " <> '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "'");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.IN
												.value())) {

									theValueCons = SqlClauseUtil.buildINClause(
											theValueCons, true);
									theFilter.append(modifierPrefix + " and "
											+ FACT_TEXT_VAL + " IN ("
											+ theValueCons + ")");
								} else if (theValueOp
										.equalsIgnoreCase(ConstrainOperatorType.LIKE
												.value())) {
									theFilter.append(modifierPrefix
											+ " and "
											+ FACT_TEXT_VAL
											+ " LIKE '"
											+ theValueCons
													.replaceAll("'", "''")
											+ "%'");
								}
							}
						}
					}

					if (theFilter.length() > 0) {
						if (sql0.trim().length() > 0) {
							sql0 = "((" + sql0 + ")" + theFilter.toString()
									+ ")";
						} else {
							sql0 = "(" + theFilter.toString().substring(4)
									+ ")";
						}
					}

					EstPanelSize = EstPanelSize + EstSize;

					if (!itItem.hasNext()) {
						// if (singleValidItem) {
						if (doInvert) {
							EstQuerySize = EstQuerySize + (1 - EstPanelSize);

							// EstQuerySize = EstQuerySize * (1 -
							// EstPanelSize);
							PanelEntry panel = new PanelEntry();
							panel.Panel = i;
							panel.Invert = 1;
							panel.EstPanelSize = (1 - EstPanelSize);
							panel.Items = 0;
							panel.AllShort = 0;
							panel.ForInsert = 0;
							panel.FirstPanel = 0;
							panel.totalItemOccurrences = totalItemOccurance;
							panel.totalItemOccurrencesOperator = totalItemOccurrenceOperator;
							p.add(panel);
						} else {
							EstQuerySize = EstQuerySize + EstPanelSize;

							PanelEntry panel = new PanelEntry();
							panel.Panel = i;
							panel.Invert = 0;
							panel.EstPanelSize = EstPanelSize;
							panel.Items = 0;
							panel.AllShort = 0;
							panel.ForInsert = 0;
							panel.FirstPanel = 0;
							panel.totalItemOccurrences = totalItemOccurance;
							panel.totalItemOccurrencesOperator = totalItemOccurrenceOperator;
							p.add(panel);
						}
						// } else {
						// if a panel doesnt have valid item, then the
						// entire
						// query return zero
						// return querySQL;
						// }
					}

					if (sql0.trim().length() > 0) {
						ItemEntry item = new ItemEntry();
						item.Panel = i;
						item.TableName = theTable;
						item.FieldName = theColumn;
						item.EstItemSize = EstSize;
						item.SqlX = 0;
						item.Sql0 = sql0;
						item.Sql1 = sql1;
						t.add(item);
					}
				}

				// if no item atleast build panel if the invert is on
				if (itemList.size() < 1) {
					// System.out
					// .println("*******  invert without iterm *****");
					PanelEntry panel = callPanelIfNoItem(doInvert,
							totalItemOccurance, totalItemOccurrenceOperator,
							EstQuerySize, EstPanelSize, i);
					p.add(panel);
				}
			}

			String[] panelSQL = new String[100];
			String[] panelTables = new String[100];
			String[] shortcutSQL = new String[100];
			boolean[] panelInvert = new boolean[100];
			int[] totalItemOccurance = new int[100];
			String[] totalItemOccurrenceOperator = new String[100];

			for (int ii = 0; ii < 100; ii++) {
				panelSQL[ii] = "";
				panelTables[ii] = "";
				shortcutSQL[ii] = "";
				panelInvert[ii] = false;
				totalItemOccurance[ii] = 0;
				totalItemOccurrenceOperator[ii] = "";
			}

			int numPanels = -1;

			// java.sql.Statement st1 = conn.createStatement();
			// ResultSet rs = st1.executeQuery(itemSql.toString());
			ResultEntry[] results = OrderPanels(p, t, sameVisit, specificity);
			String old_panel = "";
			String prevTable = "";
			EstQuerySize = 1;

			boolean newPanel = false;

			if ((results != null) && (results.length > 0)) {
				for (int r = 0; r < results.length; r++) {

					ResultEntry re = results[r];

					String new_panel = re.Panel.toString();

					if (!new_panel.equals(old_panel)) {
						newPanel = true;
						numPanels++;
						old_panel = new_panel;

						if (re.Invert == 1) {
							panelInvert[numPanels] = true;

						} else {
							;
						}
					} else {
						newPanel = false;
					}

					totalItemOccurance[numPanels] = re.totalItemOccurrences;
					totalItemOccurrenceOperator[numPanels] = re.totalItemoccurrencesOperator;

					String theTable = re.TableName;
					String theColumn = re.FieldName;
					String theTable2 = theTable;

					if (theTable != null && theColumn != null) {
						theTable = re.TableName.toLowerCase();
						theColumn = re.FieldName.toLowerCase();
						theTable2 = theTable;
					} else {
						theTable = "";
						theColumn = "";
						theTable2 = theTable;
					}

					if ((newPanel) || (!theTable.equals(prevTable))) {
						if (!panelSQL[numPanels].equals("")) {
							panelSQL[numPanels] = panelSQL[numPanels] + ")<|>";
						}

						String numFactsCheck = "";

						theTable2 = FACT_TABLE;

						String is_fact = "";

						if (sameVisit || dataRequested.equals("PE")) {
							if (theTable.equals(PATIENT_TABLE)) {
								panelSQL[numPanels] = panelSQL[numPanels]
										+ "SELECT " + ENCOUNTER_DIM_ID + ", "
										+ ENCOUNTER_PATIENT_ID + " " + is_fact
										+ " " + "FROM " + getDbSchemaName()
										+ ENCOUNTER_TABLE

										+ " WHERE  (";

								// p.num_facts > 0 AND (";
							} else if ((theTable.equals(PATIENTLISTS_TABLE))) {
								panelSQL[numPanels] = panelSQL[numPanels]
										+ "SELECT e." + ENCOUNTER_DIM_ID
										+ ", e." + ENCOUNTER_PATIENT_ID + " "
										+ is_fact + " " + "FROM "
										+ getDbSchemaName() + ENCOUNTER_TABLE
										+ " e, " + getDbSchemaName()
										+ PATIENTLISTS_TABLE + " p "
										+ "WHERE e." + ENCOUNTER_PATIENT_ID
										+ " = p." + PATIENT_DIM_ID + " AND (";

								// p.num_facts > 0 AND (";
							} else if ((theTable.startsWith(TEMP_TABLE))) {
								if (re.Sql0 != null) {
									panelSQL[numPanels] = panelSQL[numPanels]
											+ "(" + re.Sql0;
								} else {
									panelSQL[numPanels] = panelSQL[numPanels]
											+ " (SELECT " + FACT_ENCOUNTER_ID
											+ ", " + FACT_PATIENT_ID + " "
											+ is_fact + " " + "FROM "
											+ getDbSchemaName() + theTable2
											+ " ";
								}
							} else {
								panelSQL[numPanels] = panelSQL[numPanels]
										+ "SELECT " + FACT_ENCOUNTER_ID + ", "
										+ FACT_PATIENT_ID + " " + is_fact + " "
										+ "FROM " + getDbSchemaName()
										+ theTable2 + " WHERE " + numFactsCheck
										+ "(";
							}
						} else {
							if (theTable.equals(PATIENT_TABLE)) {
								panelSQL[numPanels] = panelSQL[numPanels]
										+ "SELECT " + PATIENT_DIM_ID + " "
										+ is_fact + " " + "FROM "
										+ getDbSchemaName() + theTable2 + " "
										+ "WHERE " + numFactsCheck + "(";
							} else if ((theTable
									.equals(ENCOUNTER_SHORTCUT_TABLE))
									&& ((theColumn.equals(ENCOUNTER_INOUT_COL)) || (theColumn
											.equals(ENCOUNTER_COMPANY_COL)))) {
								panelSQL[numPanels] = panelSQL[numPanels]
										+ "SELECT " + ENCOUNTER_PATIENT_ID
										+ " " + is_fact + " " + "FROM "
										+ getDbSchemaName()
										+ ENCOUNTER_SHORTCUT_TABLE + " "
										+ "WHERE " + numFactsCheck + "(";
							} else if (theTable.startsWith(TEMP_TABLE)) {
								if (re.Sql0 != null) {
									panelSQL[numPanels] = panelSQL[numPanels]
											+ " (" + re.Sql0;
								} else {
									panelSQL[numPanels] = panelSQL[numPanels]
											+ " (" + "SELECT "
											+ FACT_PATIENT_ID + " " + is_fact
											+ " " + "FROM " + getDbSchemaName()
											+ theTable2 + " ";
								}
							} else {
								String queryHint = " ";
								if (theTable.equals(PROVIDER_TABLE)) {
									log
											.debug("Join table is provider_dimension");
									queryHint = "/*+ index(observation_fact observation_fact_pk) */";
								} else {
									log
											.debug("Join table is concept_dimension");
									queryHint = "/*+ index(observation_fact fact_cnpt_pat_enct_idx) */";
								}
								String unLockSql = " ";
								if (dataSourceLookup.getServerType()
										.equalsIgnoreCase(
												DAOFactoryHelper.SQLSERVER)) {
									unLockSql = " WITH(NOLOCK) ";
								}
								String encounterSelect = " ";
								if (dataRequested.equals("PE")) {
									encounterSelect = FACT_ENCOUNTER_ID + " , ";
								}

								panelSQL[numPanels] = panelSQL[numPanels]
										+
										// RAJESH CHANGE (ADDED INDEX NAME)
										// fact_cnpt_pat_enct_idx
										// observation_fact_pk

										"SELECT " + queryHint + encounterSelect
										+ FACT_PATIENT_ID + is_fact + " "
										+ "FROM " + getDbSchemaName()
										+ theTable2 + " " + unLockSql
										+ "WHERE " + numFactsCheck + "(";
							}
						}

						if (theTable.equals(CONCEPT_TABLE)) {
							panelTables[numPanels] = panelTables[numPanels]
									+ "1|";
						} else if (theTable.startsWith(TEMP_TABLE)) {
							panelTables[numPanels] = panelTables[numPanels]
									+ "2|";
						} else {
							panelTables[numPanels] = panelTables[numPanels]
									+ "0|";
						}

						prevTable = theTable;

						if (theTable.startsWith(TEMP_TABLE)) {
							shortcutSQL[numPanels] = shortcutSQL[numPanels]
									+ re.Sql1;
						} else {
							panelSQL[numPanels] = panelSQL[numPanels] + " OR "
									+ re.Sql0;
						}

						// System.out.println("panel sql " +
						// panelSQL[numPanels]);
						if (theTable.length() < 1) {
							panelSQL[numPanels] = " ";
						}
					} else if ((theTable.equals(prevTable)) && (!newPanel)) {
						panelSQL[numPanels] = panelSQL[numPanels] + " OR "
								+ re.Sql0;
					} else {

						panelSQL[numPanels] = panelSQL[numPanels] + " "
								+ re.Sql1;
					}
				}
			}

			if (numPanels >= 0) {
				for (i = 0; i <= numPanels; i++) {
					if (!panelSQL[i].equals("")) {
						String pSql = panelSQL[i] + ")";
						pSql = pSql.replaceAll("\\( OR ", "(");
						panelSQL[i] = pSql + "\r\n";
					}
				}

				int panelCount = 1;

				boolean firstFilter = true;

				int oldPanelCount = 0;
				int newPanelCount = 0;

				boolean continueQuery = false;
				String panelQuerySQL = "";
				for (i = 0; i <= numPanels; i++) {
					continueQuery = false;

					String[] panelItemsTable = panelTables[i].split("\\|");

					if (i == 0) {
						// querySQL = panelSQL[0].replaceAll("<\\|>", "\r\n" +
						// "UNION ALL" + "\r\n");

						String[] singleItemSql = panelSQL[0].split("<\\|>");

						// RAJESH CHANGE BEGIN
						if (totalItemOccurance[i] == 0) {
							querySQL = panelSQL[0].replaceAll("<\\|>", "\r\n"
									+ "UNION ALL" + "<\\|>\r\n");
						} else {
							if (sameVisit || dataRequested.equals("PE")) {
								querySQL = panelSQL[0]
										.replaceAll(
												"<\\|>",
												" group by encounter_num,patient_num having count(*) "
														+ totalItemOccurrenceOperator[i]
														+ totalItemOccurance[i]
														+ "\r\n" + "UNION ALL"
														+ "<\\|>\r\n");
							} else {
								querySQL = panelSQL[0]
										.replaceAll(
												"<\\|>",
												" group by patient_num having count(*) "
														+ totalItemOccurrenceOperator[i]
														+ totalItemOccurance[i]
														+ "\r\n" + "UNION ALL"
														+ "<\\|>\r\n");

							}

						}

						// RAJESH CHANGE END
						if (panelInvert[i]) {
							continueQuery = true;

							if (sameVisit || dataRequested.equals("PE")) {
								querySQL = "INSERT INTO " + getDbSchemaName()
										+ TEMP_TABLE + tableSuffix + " " + "("
										+ TEMP_TABLE_PATIENT_ID + ", "
										+ TEMP_TABLE_ENCOUNTER_ID
										+ ", panel_count) \r\n" + "SELECT "
										+ ENCOUNTER_PATIENT_ID + ", "
										+ ENCOUNTER_DIM_ID + ", " + panelCount
										+ " FROM ( " + "\r\nSELECT "
										+ ENCOUNTER_PATIENT_ID + ", "
										+ ENCOUNTER_DIM_ID + " FROM "
										+ getDbSchemaName() + ENCOUNTER_TABLE
										+ ") t ";

								/*
								 * querySQL = "SELECT " + ENCOUNTER_PATIENT_ID +
								 * ", " + ENCOUNTER_DIM_ID + ", " + panelCount +
								 * " panel_count into " + getDbSchemaName() +
								 * TEMP_TABLE + tableSuffix + "  FROM ( " +
								 * "\r\nSELECT " + ENCOUNTER_PATIENT_ID + ", " +
								 * ENCOUNTER_DIM_ID + " FROM " +
								 * getDbSchemaName() + ENCOUNTER_TABLE + ") t ";
								 */
							} else {
								querySQL = "INSERT INTO " + getDbSchemaName()
										+ TEMP_TABLE + tableSuffix + " "
										+ "\r\n" + "(" + TEMP_TABLE_PATIENT_ID
										+ ", panel_count) \r\n" + "SELECT "
										+ PATIENT_DIM_ID + ", " + panelCount
										+ " FROM ( " + "\r\nSELECT "
										+ PATIENT_DIM_ID + " FROM "
										+ getDbSchemaName() + PATIENT_TABLE
										+ ") t ";

								/*
								 * querySQL = " SELECT " + PATIENT_DIM_ID + ", "
								 * + panelCount + " panel_count into " +
								 * getDbSchemaName() + TEMP_TABLE + tableSuffix
								 * + "  FROM ( " + "\r\nSELECT " +
								 * PATIENT_DIM_ID + " FROM " + getDbSchemaName()
								 * + PATIENT_TABLE + ") t";
								 */
							}
						} else if (sameVisit) {
							String occuranceSql = " group by  encounter_num,patient_num having count(*) "
									+ totalItemOccurrenceOperator[i]
									+ totalItemOccurance[i];

							querySQL = "INSERT INTO " + getDbSchemaName()
									+ TEMP_TABLE + tableSuffix + " " + "("
									+ TEMP_TABLE_PATIENT_ID + ", "
									+ TEMP_TABLE_ENCOUNTER_ID
									+ ", panel_count) \r\n" + "SELECT "
									+ TEMP_TABLE_ENCOUNTER_ID + ", "
									+ TEMP_TABLE_PATIENT_ID + ", " + panelCount
									+ " FROM ( " + "\r\n" + querySQL + ") t ";

							/*
							 * querySQL = "SELECT " + TEMP_TABLE_ENCOUNTER_ID +
							 * ", " + TEMP_TABLE_PATIENT_ID + ", " + panelCount
							 * + " panel_count into " + getDbSchemaName() +
							 * TEMP_TABLE + tableSuffix + "  FROM ( " + "\r\n" +
							 * querySQL + ") t ";
							 */

							// // ///
							querySQL = "";
							// add insert statment for each select
							for (int k = 0; k < singleItemSql.length; k++) {

								// commented to see the select into will speed
								// up the query
								querySQL = querySQL + "\r\nINSERT INTO "
										+ getDbSchemaName() + TEMP_TABLE
										+ tableSuffix + " " + "("
										+ TEMP_TABLE_ENCOUNTER_ID + ","
										+ TEMP_TABLE_PATIENT_ID
										+ ", panel_count) \r\n" + "SELECT "
										+ TEMP_TABLE_ENCOUNTER_ID + ","
										+ TEMP_TABLE_PATIENT_ID + ", "
										+ panelCount + " FROM ( " + "\r\n"
										+ singleItemSql[k] + // RAJESH CHANGE
										// BEGIN
										occuranceSql + // RAJESH CHANGE END
										") t ";

								if (k + 1 < singleItemSql.length) {
									querySQL += "\r\n<*>\r\n";
								}

							}
							// ///
						} else {
							String occuranceSql = " ";

							if (totalItemOccurance[i] > 0) {
								if (sameVisit || dataRequested.equals("PE")) {
									occuranceSql = " group by encounter_num,patient_num having count(*) "
											+ totalItemOccurrenceOperator[i]
											+ totalItemOccurance[i];
								} else {
									occuranceSql = " group by patient_num having count(*) "
											+ totalItemOccurrenceOperator[i]
											+ totalItemOccurance[i];
								}

							}
							querySQL = "";
							String encounterSelect = " ";
							if (dataRequested.equals("PE")) {
								encounterSelect = TEMP_TABLE_ENCOUNTER_ID
										+ " , ";
							}

							// add insert statment for each select
							for (int k = 0; k < singleItemSql.length; k++) {

								// commented to see the select into will speed
								// up the query
								querySQL = querySQL + "\r\nINSERT INTO "
										+ getDbSchemaName() + TEMP_TABLE
										+ tableSuffix + " " + "("
										+ encounterSelect
										+ TEMP_TABLE_PATIENT_ID
										+ ", panel_count) \r\n" + "SELECT "
										+ encounterSelect
										+ TEMP_TABLE_PATIENT_ID + ", "
										+ panelCount + " FROM ( " + "\r\n"
										+ singleItemSql[k] + // RAJESH
										// CHANGE
										// BEGIN
										occuranceSql + // RAJESH CHANGE END
										") t ";

								/*
								 * querySQL = querySQL + "SELECT " +
								 * TEMP_TABLE_PATIENT_ID + ", " + panelCount +
								 * " panel_count into " + getDbSchemaName() +
								 * TEMP_TABLE + tableSuffix + " FROM ( " +
								 * "\r\n" + singleItemSql[k] + occuranceSql +
								 * ") t";
								 */
								if (k + 1 < singleItemSql.length) {
									querySQL += "\r\n<*>\r\n";
								}

							}

						}

						String specCount = "";

						if (panelItemsTable[0].equals("2")) {
							querySQL = shortcutSQL[0] + querySQL;
						}

						if (dbType == DatabaseType.Oracle) {
							// querySQL = "begin \r\n" + querySQL + ";";
							querySQL = querySQL + "\r\n<*>\r\n";
						}
						panelQuerySQL = querySQL;

					}

					if ((i > 0) || (continueQuery)) {
						if (panelInvert[i]) {
							oldPanelCount = panelCount;
							newPanelCount = 0;
						} else {
							oldPanelCount = panelCount;
							newPanelCount = panelCount + 1;
							panelCount = panelCount + 1;
						}

						if ((!shortcutSQL[i].equals(""))
								&& (!panelTables[i].contains("2|"))) {
							querySQL = querySQL + "UPDATE " + getDbSchemaName()
									+ "t SET t.panel_count = " + newPanelCount
									+ " FROM " + getDbSchemaName() + TEMP_TABLE
									+ tableSuffix + " t " + "\r\n" + "WHERE "
									+ shortcutSQL[i];

							if (firstFilter) {
								firstFilter = false;
							} else {
								querySQL = querySQL + "WHERE t.panel_count = "
										+ oldPanelCount + "\r\n";
							}

							if (dbType == DatabaseType.Oracle) {
								querySQL = querySQL + "<*>";
							}

							// querySQL = querySQL + ";";
							querySQL = querySQL + "\r\n\r\n";
						}

						if (!panelSQL[i].equals("")) {
							String[] panelItemsSQL = panelSQL[i].split("<\\|>");

							for (j = 0; j < panelItemsSQL.length; j++) {
								if ((!shortcutSQL[i].equals(""))
										&& (panelItemsTable[j].equals("2"))) {
									querySQL = querySQL + shortcutSQL[i];
								}

								if (dbType == DatabaseType.Oracle) {
									String previousQuerySQL = querySQL;
									if ((specificity > 1)
											&& (panelItemsTable[j].equals("1"))) {

									} else {

										String occuranceSql = " ";

										if (totalItemOccurance[i] > 0) {
											if (sameVisit
													|| dataRequested
															.equals("PE")) {
												occuranceSql = " group by encounter_num,patient_num having count(*) "
														+ totalItemOccurrenceOperator[i]
														+ totalItemOccurance[i];
											} else {
												occuranceSql = " group by patient_num having count(*) "
														+ totalItemOccurrenceOperator[i]
														+ totalItemOccurance[i];
											}

										}

										querySQL = querySQL
												+ "UPDATE "
												+ getDbSchemaName()
												+ TEMP_TABLE
												+ " SET panel_count = "
												+ newPanelCount
												+ " WHERE EXISTS ( SELECT 1 FROM ( "
												+ "\r\n"
												+ panelItemsSQL[j]
												+
												// RAJ OCCURANCE CHANGE BEGIN
												occuranceSql
												+ // RAJ OCCURANCE CHANGE END
												") v " + " WHERE "
												+ getDbSchemaName()
												+ TEMP_TABLE + "."
												+ TEMP_TABLE_PATIENT_ID
												+ " = v."
												+ TEMP_TABLE_PATIENT_ID + " ";

										if (sameVisit
												|| dataRequested.equals("PE")) {
											querySQL = querySQL + "AND "
													+ getDbSchemaName()
													+ TEMP_TABLE + "."
													+ TEMP_TABLE_ENCOUNTER_ID
													+ " = v."
													+ TEMP_TABLE_ENCOUNTER_ID
													+ " ";
										}

										querySQL = querySQL + ")";
									}

									if (firstFilter) {
										firstFilter = false;
									} else {
										querySQL = querySQL
												+ "\r\nAND panel_count = "
												+ oldPanelCount + "\r\n";
									}

									if (panelItemsSQL[j].trim().equals(")")) {
										querySQL = previousQuerySQL;
									} else {

										querySQL = querySQL + "\r\n<*>";
									}
								}

								// querySQL = querySQL + ";";
								querySQL = querySQL + "\r\n";
							}
							if (querySQL.trim().length() < 1) {
								querySQL += panelQuerySQL;
							}
						}
					}
				}

				String querySQLTemp = "SELECT 0 " + TEMP_TABLE_PATIENT_ID
						+ " WHERE 1=0";

				if (specificity > 1) {
					;
				} else {
					querySQLTemp = "SELECT DISTINCT t." + TEMP_TABLE_PATIENT_ID
							+ " ";

					if (dataRequested.equals("PE")) {
						querySQLTemp = querySQLTemp + ", t."
								+ TEMP_TABLE_ENCOUNTER_ID + " ";
					}
					// if (dataRequested.equals("PE")) {
					// if (sameVisit) {
					// querySQLTemp = querySQLTemp + ", t."
					// + TEMP_TABLE_ENCOUNTER_ID + " ";
					// } else {
					// querySQLTemp = querySQLTemp + ", e."
					// + TEMP_TABLE_ENCOUNTER_ID + " ";
					// }
					// }

					querySQLTemp = querySQLTemp + "FROM " + getDbSchemaName()
							+ TEMP_TABLE + tableSuffix + " t ";

					// if ((dataRequested.equals("PE")) && (!sameVisit)) {
					// querySQLTemp = querySQLTemp + ", " + getDbSchemaName()
					// + ENCOUNTER_TABLE + " e" + " ";
					// }

					querySQLTemp = querySQLTemp + "WHERE panel_count = "
							+ panelCount + "\r\n";

					// if ((dataRequested.equals("PE")) && (!sameVisit)) {
					// querySQLTemp = querySQLTemp + " AND e."
					// + ENCOUNTER_PATIENT_ID + " = t."
					// + TEMP_TABLE_PATIENT_ID + "\r\n";
					// }

				}

				if (origIteration == 0) {

					// select into to see if it speed up the query
					if (dbType == DatabaseType.SqlServer) {
						querySQL = querySQL + "SELECT * INTO "
								+ TEMP_RETURN_TABLE + tableSuffix + " FROM ("
								+ "\r\n" + querySQLTemp + ") q" + "\r\n";
					} else {
						querySQL = querySQL + "INSERT INTO "
								+ getDbSchemaName() + TEMP_RETURN_TABLE
								+ tableSuffix + " ";

						if (dataRequested.equals("PE")) {
							querySQL = querySQL + "(" + TEMP_TABLE_PATIENT_ID
									+ ", " + TEMP_TABLE_ENCOUNTER_ID + ")";
						} else {
							querySQL = querySQL + "(" + TEMP_TABLE_PATIENT_ID
									+ ")";
						}

						querySQL = querySQL + " SELECT * FROM (" + "\r\n"
								+ querySQLTemp + ") q";

						// querySQL = querySQL + ";\r\n\r\n"; //
						querySQL = querySQL + "\r\n<*>\r\n";
						querySQL = querySQL + "\r\n";
					}

					/*
					 * querySQL = querySQL + "SELECT * INTO " +
					 * getDbSchemaName() + TEMP_RETURN_TABLE + tableSuffix +
					 * " FROM (" + "\r\n" + querySQLTemp + ") q" + "\r\n";
					 */

				}
			}

			return querySQL;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new I2B2DAOException(e.getMessage(), e);
		}
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
