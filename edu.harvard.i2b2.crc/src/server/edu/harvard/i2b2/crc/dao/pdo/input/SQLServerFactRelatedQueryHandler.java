/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.input;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.common.util.xml.XMLOperatorLookup;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.I2B2PdoFactory;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.RPDRPdoFactory;
import edu.harvard.i2b2.crc.dao.pdo.filter.DimensionFilter;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.EidFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ModifierFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PidFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.input.ModifierConstrainsHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.UnitConverstionUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainDateTimeType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainDateType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InclusiveType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType.ConstrainByDate;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType.TotalItemOccurrences;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Observation fact handler class for pdo request. This class uses given pdo
 * request to generate pdo sql and build observation fact, unique list of fact's
 * patient,concept code, visit and provider list
 * <p>
 * This class handles fact related queries for both plain and table pdo $Id:
 * SQLServerFactRelatedQueryHandler.java,v 1.3 2008/06/10 14:59:05 rk903 Exp $
 * 
 * @author rkuttan
 * @see VisitFactRelated
 * @see ProviderFactRelated
 * @see PatientFactRelated
 * @see ObservationFactRelated
 */
public class SQLServerFactRelatedQueryHandler extends CRCDAO implements
		IFactRelatedQueryHandler {
	/** Input option list from pdo request* */
	private InputOptionListType inputList = null;

	/** filter list from pdo request * */
	private FilterListType filterList = null;
	
	private OutputOptionListType outputOptionList = null; 

	/** helper class for visit/event in pdo * */
	private VisitFactRelated visitFactRelated = null;

	/** helper class for observer/provider in pdo * */
	private ProviderFactRelated providerFactRelated = null;

	/** helper class for patient in pdo * */
	private PatientFactRelated patientFactRelated = null;

	/** helper class for concepts in pdo * */
	private ConceptFactRelated conceptFactRelated = null;
	
	private ModifierFactRelated modifierFactRelated = null;
	
	private PidFactRelated pidFactRelated = null;

	private EidFactRelated eidFactRelated = null;

	/** helper class for observation fact in pdo * */
	private ObservationFactFactRelated obsFactFactRelated = null;
	/** to store unique patient number list present in fact* */
	List<String> patientFactList = new Vector<String>();
	/** to store unique concept code list present in fact* */
	List<String> conceptFactList = new Vector<String>();
	/** to store unique modifier code list present in fact* */
	List<String> modifierFactList = new Vector<String>();
	/** to store unique encounter number present in fact * */
	List<String> visitFactList = new Vector<String>();
	/** to store unique provider/observer id present in fact * */
	List<String> providerFactList = new Vector<String>();

	/** Handler interface for input list, i.e Patient list or visit list * */
	private IInputOptionListHandler inputOptionListHandler = null;

	/**
	 * flag to see if concept filter is set, used in observation fact set
	 * element *
	 */
	private boolean checkFilter = false;

	/**
	 * field to keep track number of prepared statment parameters in the
	 * genereated pdo query*
	 */
	private int queryParameterCount = 0;

	private DataSourceLookup dataSourceLookup = null;

	// public static final String TEMP_PDO_OBSFACT_TABLE = "#TEMP_PDO_OBSFACT";
	public static  String TEMP_PDO_INPUTLIST_TABLE = "#TEMP_PDO_INPUTLIST";
	public static  String TEMP_FACT_PARAM_TABLE = "#TEMP_FACT_PARAM_TABLE";
	// public static final String TEMP_PDO_INPUTLIST_TABLE1 =
	// "#TEMP_PDO_INPUTLIST1";

	private List<String> panelSqlList = new ArrayList<String>();
	private Map projectParamMap = null; 
	private Map<String,XmlValueType> modifierMetadataXmlMap = null;
	private String requestVersion = "";
	

	private String factTable = "observation_FACT";
	private boolean derivedFactTable = QueryProcessorUtil.getInstance().getDerivedFactTable();
	
	
	/**
	 * Constructor with parameter
	 * 
	 * @param inputList
	 * @param filterList
	 * @param outputOptionList
	 */
	public SQLServerFactRelatedQueryHandler(DataSourceLookup dataSourceLookup,
			InputOptionListType inputList, FilterListType filterList,
			OutputOptionListType outputOptionList) {
		this.dataSourceLookup = dataSourceLookup;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.inputList = inputList;
		this.filterList = filterList;
		this.outputOptionList = outputOptionList;

		visitFactRelated = new VisitFactRelated(outputOptionList.getEventSet());
		providerFactRelated = new ProviderFactRelated(outputOptionList
				.getObserverSetUsingFilterList());
		patientFactRelated = new PatientFactRelated(outputOptionList
				.getPatientSet());
		conceptFactRelated = new ConceptFactRelated(outputOptionList
				.getConceptSetUsingFilterList());
		modifierFactRelated = new ModifierFactRelated(outputOptionList
				.getConceptSetUsingFilterList());
		obsFactFactRelated = new ObservationFactFactRelated(outputOptionList
				.getObservationSet());
		pidFactRelated = new PidFactRelated(outputOptionList.getPidSet());
		eidFactRelated = new EidFactRelated(outputOptionList.getEidSet());

		// If postgresql change temp table name
		if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
			 TEMP_PDO_INPUTLIST_TABLE = "TEMP_PDO_INPUTLIST";
			 TEMP_FACT_PARAM_TABLE = "TEMP_FACT_PARAM_TABLE";

		}
		// check if concept filter present
		if ((filterList != null) && (filterList.getPanel() != null)
				&& (filterList.getPanel().size() > 0)) {
			checkFilter = true;
		}
	}
	
	public void setProjectParamMap(Map projectParamMap) { 
		this.projectParamMap =  projectParamMap;
	}
	
	public void setModifierMetadataXmlMap(Map<String,XmlValueType> modifierMetadataXmlMap) {
		this.modifierMetadataXmlMap = modifierMetadataXmlMap;
	}
	
	public void setRequestVersion(String requestVersion) { 
		this.requestVersion = requestVersion;
	}
	
	
	public String getFactTable(){
		return this.factTable;
	}

	public void setFactTable(String table){
		this.factTable = table;
	}


	/**
	 * Function to build and execute pdo sql and build plain pdo's observation
	 * fact
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ObservationSet> getPdoObservationFact() throws I2B2DAOException {
		ResultSet resultSet = null;
		Connection conn = null;
		List<ObservationSet> observationFactSetList = new ArrayList<ObservationSet>();
		PreparedStatement stmt = null;
		try {
			conn = this.getApplicationDataSource(
					dataSourceLookup.getDataSource()).getConnection();

			int sqlParamCount = 1;

			boolean createTempTable = true;

			if(checkFilter == false) {
//WAS			if (filterList.getPanel().size() == 0) {
				
				if(derivedFactTable == true){
					String sql=   "select * from information_schema.tables where lower(table_name) = 'observation_fact'";
					if(dataSourceLookup.getFullSchema() != null){
						int lastIndex = dataSourceLookup.getFullSchema().lastIndexOf(".");
						sql += " and table_catalog = '" +	dataSourceLookup.getFullSchema().substring(0, lastIndex) + "'";
						sql += " and table_schema = '" + 	dataSourceLookup.getFullSchema().substring(lastIndex+1, (dataSourceLookup.getFullSchema().length())) + "'";
					}
					PreparedStatement stmt1 = conn.prepareStatement(sql);
					ResultSet resultSet1 = stmt1.executeQuery();
		//			log.info("no filter Pdo sql: " + sql);
					
					if(!(resultSet1.next())){
						resultSet1.close();
						createTempTable = false;
						try {
							JDBCUtil.closeJdbcResource(null, stmt1, conn);
						} catch (SQLException e) {
							log.error("Error trying to close connection:"+ e.getMessage());
						}
		
						return observationFactSetList;
					}
				}
				
				// generate sql
				String querySql = buildQuery(null,
						PdoQueryHandler.PLAIN_PDO_TYPE);
				panelSqlList.add(buildQueryCommon(null,
						PdoQueryHandler.PLAIN_PDO_TYPE));
				log.debug("Executing sql[" + querySql + "]");
				if (inputOptionListHandler.isEnumerationSet()) {
					inputOptionListHandler
							.uploadEnumerationValueToTempTable(conn);
				}
				// execute fullsql
				resultSet = executeQuery(conn, stmt, querySql, sqlParamCount);
				// build facts
				observationFactSetList.add(buildPDOFact(resultSet, ""));
			} else {
				for (PanelType panel : filterList.getPanel()) {
					// generate sql
					String querySql = buildQuery(panel,
							PdoQueryHandler.PLAIN_PDO_TYPE);
					if (querySql.length() ==0) { 
						continue;
					}
					
					String defaultTableName = this.getDbSchemaName() + "observation_FACT" ;
					// OMOP addition
					if(panel != null){
						if(derivedFactTable == true){
							if(panel.getItem().get(0).getFacttablecolumn().contains(".")){

								String baseItemFactColumn = panel.getItem().get(0).getFacttablecolumn();
								int lastIndex = baseItemFactColumn.lastIndexOf(".");
								String factTable = this.getDbSchemaName() + (baseItemFactColumn.substring(0, lastIndex));

								DerivedFactColumnsType columns = getFactColumnsFromOntologyCell(panel.getItem().get(0).getItemKey());
								if(columns != null){
									if(columns.getDerivedFactTableColumn().size() > 1) {
										// parse through solumns and build up replace string.

										Iterator<String> it = columns.getDerivedFactTableColumn().iterator();
										String column = it.next();
										
										lastIndex = column.lastIndexOf(".");
										String table = this.getDbSchemaName() + (column.substring(0, lastIndex));

										
										factTable = "( select * from " + table;
										while(it.hasNext()){
											column = it.next();
											
											lastIndex = column.lastIndexOf(".");
											table = this.getDbSchemaName() + (column.substring(0, lastIndex));

											factTable += "\n UNION ALL \n"
													+ " select * from " +  table;
										}
										factTable += " )";

									}
								}
								log.debug("Parse columns " + factTable);
								querySql=querySql.replaceAll(defaultTableName, factTable)	;
							}
						}
					}
					panelSqlList.add(buildQueryCommon(panel,
							PdoQueryHandler.PLAIN_PDO_TYPE));
					log.debug("PLAIN PDO Executing sql[" + querySql + "]");
					// execute fullsql
					sqlParamCount = panel.getItem().size();
					if (panel.getInvert() == 1) {
						sqlParamCount++;
					}
					long startTimeSql = System.currentTimeMillis();
					if (createTempTable) {
						if (inputOptionListHandler.isEnumerationSet()) {
							inputOptionListHandler
									.uploadEnumerationValueToTempTable(conn);
						}
					}
					
					resultSet = executeQuery(conn, stmt, querySql,
							sqlParamCount);
					// set createTempTable flag to false after the first
					// iteration
					createTempTable = false;
					//
					long endTimeSql = System.currentTimeMillis();
					long totalTimeSql = endTimeSql - startTimeSql;
					System.out
							.println("******* Total time to execute observation sql"
									+ totalTimeSql);
					long startTime = System.currentTimeMillis();
					// build facts
					observationFactSetList.add(buildPDOFact(resultSet, panel
							.getName()));
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					System.out
							.println("******* Total time to build observation "
									+ totalTime);
					resultSet.close();
				}
			}
		} catch (SQLException sqlEx) {
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			throw new I2B2DAOException("", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				// deleteTempTable(conn);
				try {
					if(inputOptionListHandler != null && conn != null)
						inputOptionListHandler.deleteTempTable(conn);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// close connection
			try {
				JDBCUtil.closeJdbcResource(null, stmt, conn);
			} catch (SQLException e) {
				log.error("Error trying to close connection", e);
			}
		}

		return observationFactSetList;
	}

	/**
	 * Function to build and execute pdo sql and build table pdo's observation
	 * fact
	 * 
	 * @return ObservationSet list
	 * @throws I2B2DAOException
	 */
	public List<ObservationSet> getTablePdoObservationFact()
			throws I2B2DAOException {
		Connection conn = null;
		List<ObservationSet> observationSetList = new ArrayList<ObservationSet>();
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getApplicationDataSource(
					dataSourceLookup.getDataSource()).getConnection();

			int sqlParamCount = 1;

			//
			boolean createTempTable = true;

			if(checkFilter == false) {
//WAS			if (filterList.getPanel().size() == 0) {
				
				if(derivedFactTable == true){
					String sql=   "select * from information_schema.tables where lower(table_name) = 'observation_fact'";
					if(dataSourceLookup.getFullSchema() != null){
		//				log.info("schema: " + dataSourceLookup.getFullSchema());
						
						int lastIndex = dataSourceLookup.getFullSchema().lastIndexOf(".");
						sql += " and table_catalog = '" +	dataSourceLookup.getFullSchema().substring(0, lastIndex) + "'";
						sql += " and table_schema = '" + 	dataSourceLookup.getFullSchema().substring(lastIndex+1, (dataSourceLookup.getFullSchema().length())) + "'";
					}
		//			log.info("no filter tablePDO sql: " + sql);
					PreparedStatement stmt1 = conn.prepareStatement(sql);
					ResultSet resultSet1 = stmt1.executeQuery();
					
					if(!(resultSet1.next())){
						resultSet1.close();
						createTempTable = false;
						try {
							JDBCUtil.closeJdbcResource(null, stmt1, conn);
						} catch (SQLException e) {
							log.error("Error trying to close connection:"+ e.getMessage());
						}
		
						return observationSetList;
					}

				}
				// generate sql
				String querySql = buildQuery(null,
						PdoQueryHandler.PLAIN_PDO_TYPE);
				
				log.debug("Executing sql PLAIN PDO[" + querySql + "]");
				panelSqlList.add(buildQueryCommon(null,
						PdoQueryHandler.PLAIN_PDO_TYPE));
				if (inputOptionListHandler.isEnumerationSet()) {
					inputOptionListHandler
							.uploadEnumerationValueToTempTable(conn);
				}

				// execute fullsql
				resultSet = executeQuery(conn, stmt, querySql, sqlParamCount);
				// build facts
				observationSetList.add(buildPDOFact(resultSet, ""));
			} else {
				for (PanelType panel : filterList.getPanel()) {
					// generate sql
					String querySql = buildQuery(panel,
							PdoQueryHandler.TABLE_PDO_TYPE);
					if (querySql.length() ==0 ) { 
						continue;
					}

				//	panelSqlList.add(buildQueryCommon(panel,
				//			PdoQueryHandler.TABLE_PDO_TYPE));
					sqlParamCount = panel.getItem().size();
					if (panel.getInvert() == 1) {
						sqlParamCount++;
					}
					long startTimeSql = System.currentTimeMillis();
					if (createTempTable) {
						if (inputOptionListHandler.isEnumerationSet()) {
							inputOptionListHandler
									.uploadEnumerationValueToTempTable(conn);
						}
						
					}
					String defaultTableName = this.getDbSchemaName() + "observation_FACT" ;
					// OMOP addition
					if(panel != null){
						if(derivedFactTable == true){
							if(panel.getItem().get(0).getFacttablecolumn().contains(".")){

								String baseItemFactColumn = panel.getItem().get(0).getFacttablecolumn();
								int lastIndex = baseItemFactColumn.lastIndexOf(".");
								String factTable = this.getDbSchemaName() + (baseItemFactColumn.substring(0, lastIndex));

								DerivedFactColumnsType columns = getFactColumnsFromOntologyCell(panel.getItem().get(0).getItemKey());
								if(columns != null){
									if(columns.getDerivedFactTableColumn().size() > 1) {
										// parse through solumns and build up replace string.

										Iterator<String> it = columns.getDerivedFactTableColumn().iterator();
										String column = it.next();
										
										lastIndex = column.lastIndexOf(".");
										String table = this.getDbSchemaName() + (column.substring(0, lastIndex));

										
										factTable = "( select * from " + table;
										while(it.hasNext()){
											column = it.next();
											
											lastIndex = column.lastIndexOf(".");
											table = this.getDbSchemaName() + (column.substring(0, lastIndex));

											factTable += "\n UNION ALL \n"
													+ " select * from " +  table;
										}
										factTable += " )";

									}
								}
								log.debug("Parse columns " + factTable);
								querySql=querySql.replaceAll(defaultTableName, factTable)	;
							}
						}
					}
					panelSqlList.add(querySql);
					log.debug("TABLE PDO Executing sql[" + querySql + "]");

					// execute fullsql
					resultSet = executeQuery(conn, stmt, querySql,
							sqlParamCount);
					long endTimeSql = System.currentTimeMillis();
					long totalTimeSql = endTimeSql - startTimeSql;
					System.out
							.println("******* Total time to execute observation sql"
									+ totalTimeSql);
					long startTime = System.currentTimeMillis();
					// build facts
					observationSetList.add(buildTablePDOFact(resultSet, panel
							.getName()));
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					System.out
							.println("******* Total time to build observation "
									+ totalTime);
					resultSet.close();
					createTempTable = false;
				}
			}
		} catch (SQLException sqlEx) {
			log.error(sqlEx.getMessage());
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			log.error(ioEx.getMessage());
			throw new I2B2DAOException("", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				/*
				 * IInputOptionListHandler inputOptionListHandler = PDOFactory
				 * .buildInputListHandler(inputList, dataSourceLookup);
				 */
				try {
					if(conn != null && inputOptionListHandler != null)
						inputOptionListHandler.deleteTempTable(conn);
				} catch (SQLException e) {
					log.error(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// close connection
			try {
				JDBCUtil.closeJdbcResource(null, stmt, conn);
			} catch (SQLException e) {
				log.error("Error trying to close connection", e);
			}
		}

		return observationSetList;
	}

	/**
	 * Returns provider id, belong to the facts
	 * 
	 * @return list of provider/observer id
	 */
	public List<String> getProviderFactList() {
		return providerFactList;
	}

	/**
	 * Returns concept code belong to the facts
	 * 
	 * @return
	 */
	public List<String> getConceptFactList() {
		return conceptFactList;
	}
	
	/**
	 * Returns modifier code belong to the facts
	 * 
	 * @return
	 */
	public List<String> getModifierFactList() {
		return modifierFactList;
	}

	/**
	 * Returns patient number belong to the facts
	 * 
	 * @return
	 */
	public List<String> getPatientFactList() {
		return patientFactList;
	}

	/**
	 * Returns encounter number belong to the facts
	 * 
	 * @return list of encounter number
	 */
	public List<String> getVisitFactList() {
		return visitFactList;
	}

	public String buildTotalQuery(PanelType panel, String pdoType)
			throws I2B2DAOException {
		// TODO Auto-generated method stub
		return buildQueryCommon(panel, pdoType);
	}

	private String buildQueryCommon(PanelType panel, String pdoType)
			throws I2B2DAOException {
		String obsFactSelectClause = null;
	
		if (obsFactFactRelated != null) {
			obsFactSelectClause = obsFactFactRelated
					.getSelectClauseWithoutBlob();

			if (obsFactSelectClause.length() <= 0) {
				obsFactSelectClause = obsFactFactRelated
						.getDefaultSelectClause();
			}
		}

		String tableLookupJoinClause = " ";

		if (pdoType.equals(PdoQueryHandler.TABLE_PDO_TYPE)) {
			tableLookupJoinClause = getLookupJoinClause(obsFactFactRelated
					.isSelectDetail(), obsFactFactRelated.isSelectBlob(),
					obsFactFactRelated.isSelectStatus());
			obsFactSelectClause += " , concept_lookup.name_char concept_name, provider_lookup.name_char provider_name, modifier_lookup.name_char modifier_name";
			if (obsFactFactRelated.isSelectDetail()) {
				obsFactSelectClause += " , location_lookup.name_char location_name ";
			}
		}

		String fullWhereClause = "";

		if (inputOptionListHandler == null) {
			inputOptionListHandler = PDOFactory.buildInputListHandler(
					inputList, dataSourceLookup);
		}
		// fullWhereClause = patientSetWhereBuilder.getWhereClause();
		if (inputList.getPatientList() != null) {
			fullWhereClause = " obs.patient_num IN  ";
		} else if (inputList.getEventList() != null) {
			fullWhereClause = " obs.encounter_num IN  \n";
		} else if (inputList.getPidList() != null) {
			fullWhereClause = " obs.patient_num IN  ";
		} else if (inputList.getEidList() != null) {
			fullWhereClause = " obs.encounter_num IN  ";
		} else {
			throw new I2B2DAOException(
					"Input option list does not contain visit or patient list");
		}

		fullWhereClause += (" ( "
				+ inputOptionListHandler.generateWhereClauseSql() + " ) \n");

		String factByConceptSql = "";
		String factWithoutFilterSql = "";

		String mainSelectBlobClause = " ";
		if (obsFactFactRelated.isSelectBlob()) {
			mainSelectBlobClause = " , observation_blob obs_observation_blob";
		}

		// String mainQuerySql = "SELECT * FROM ( \n";
		String mainQuerySql = "SELECT b.* " + mainSelectBlobClause + " FROM "
				+ this.getDbSchemaName() + getFactTable() + " obs ,( \n";
		try {
			if (panel != null) {
				factByConceptSql = factQueryWithDimensionFilter(
						obsFactSelectClause, tableLookupJoinClause,
						fullWhereClause, panel);
				if (factByConceptSql.trim().length()==0) { 
					return "";
				}
				if (panel.getTotalItemOccurrences() != null
						&& panel.getTotalItemOccurrences().getValue() > 0) {
					mainQuerySql += " select *, rank() over (partition by obs_encounter_num,obs_patient_num,obs_start_date,obs_concept_cd order by rnum) as seqNumber from ( "
							+ factByConceptSql + " ) as t";
				} else {
					mainQuerySql += factByConceptSql;
				}

			} else {
				factWithoutFilterSql = factQueryWithoutFilter(
						obsFactSelectClause, tableLookupJoinClause,
						fullWhereClause);
				mainQuerySql += factWithoutFilterSql +  ") b \n";
			}

		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2DAOException(i2b2Ex.getMessage(), i2b2Ex);
		}

		mainQuerySql += "   ) as b where rnum >= 0 ";
		mainQuerySql += " and obs.encounter_num = b.obs_encounter_num and obs.patient_num = b.obs_patient_num  ";
		mainQuerySql += "  and obs.concept_cd = b.obs_concept_cd and obs.provider_id = b.obs_provider_id and obs.start_date  = b.obs_start_date and obs.modifier_cd = b.obs_modifier_cd and obs.instance_num = b.obs_instance_num ";

		if (panel != null) {
			TotalItemOccurrences totOccurance = panel.getTotalItemOccurrences();
			if (totOccurance != null && totOccurance.getValue() > 1) {
				int totOcurranceValue = totOccurance.getValue();
				String totOccuranceOperator = ">=";
				if (totOccurance.getOperator() != null) {
					String totOccuranceOperatorValue = totOccurance.getOperator()
							.value();
					totOccuranceOperator = XMLOperatorLookup
							.getComparisonOperatorFromAcronum(totOccuranceOperatorValue);
					if (totOccuranceOperator == null) {
						totOccuranceOperator = ">=";
					}
				}
	
				mainQuerySql += " AND seqNumber " + totOccuranceOperator
						+ totOcurranceValue;
			}
			
		}
		

		if (this.outputOptionList.getObservationSet().getSelectionfilter()!= null)
		{
			//mainQuerySql = "with main_query as (" + mainQuerySql + ") select * from (select f1.*, " +
			String newMainQuerySql = "select * from (select f1.*, " +
					
					" rank() over(partition by obs_patient_num, obs_concept_cd order by ";
			if (this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("min_value"))
				newMainQuerySql += " obs_nval_num ";
			else if (this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("max_value"))
				newMainQuerySql += " obs_nval_num desc ";
			else if (this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("first_value"))
				newMainQuerySql += " obs_start_date ";
			else if (this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("last_value"))
				newMainQuerySql += " obs_start_date desc ";
			else if (this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("single_observation"))
				newMainQuerySql += " case when obs_modifier_cd = '@' then 1 else 0 end, obs_modifier_cd ";
			else
				newMainQuerySql += " obs_start_date desc ";

			mainQuerySql = newMainQuerySql + " ) rank " +
					" from  (" + mainQuerySql + ") f1 " ; 

			if ((this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("min_value")) ||
					(this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("max_value")) )
				mainQuerySql +=	" where obs_nval_num is not null ";

			
			if (this.outputOptionList.getObservationSet().getSelectionfilter().value().equalsIgnoreCase("last_n_values"))
			{
				mainQuerySql +=	") f where f.rank <  " + this.outputOptionList.getObservationSet().getValue();
			} else {
				mainQuerySql +=	") f where f.rank = 1 ";
			}
				
		}

		return mainQuerySql;

	}

	/**
	 * This is the main function to build query for plain and table pdo request
	 * 
	 * @param pdoType
	 * @return String
	 * @throws I2B2DAOException
	 */
	public String buildQuery(PanelType panel, String pdoType)
			throws I2B2DAOException {
		String mainQuerySql = this.buildQueryCommon(panel, pdoType);
		if (mainQuerySql.length() == 0) { 
			return "";
		}
		//check for version 1.5, if so return the fact without the duplicates in modifier_cd and instance num
		//TODO Removed because not working in 1.6.05
		/*
		 if (this.requestVersion.startsWith("1.5")) { 	
			 mainQuerySql = " select * from (select *, rank() over(partition by obs_encounter_num, obs_patient_num,obs_concept_cd,obs_start_date,obs_provider_id order by obs_modifier_cd,obs_instance_num ) ordernum " +
				" from ( " + mainQuerySql + ") ordersql   ) ordersql1 where ordernum = 1 ";
			 
		 }
		 */ 
		 mainQuerySql += " ORDER BY obs_patient_num,obs_start_date,obs_concept_cd,obs_instance_num,obs_modifier_cd,rnum";
		
		return mainQuerySql;
	}

	// -----------------------------------------
	// private helper functions start from here
	// -----------------------------------------

	/**
	 * Function to uses given select, join and where clause to build core pdo
	 * query, related to provider filter
	 * 
	 * @param obsFactSelectClause
	 * @param tableLookupJoinClause
	 * @param fullWhereClause
	 * @return
	 */
	private String factQueryWithDimensionFilter(String obsFactSelectClause,
			String tableLookupJoinClause, String fullWhereClause,
			PanelType panel) throws I2B2Exception {
		String factByProviderSql = "";
		int i = 0;
		String panelName = null;
		DateConstrainHandler dateConstrainHandler = new DateConstrainHandler(
				dataSourceLookup);
		
	
		if (panel.getName() != null) {
			panelName = JDBCUtil.escapeSingleQuote(panel.getName());
		}

		obsFactSelectClause += (", '" + panelName + "' panel_name ");

		int totalItemOccurance = 0;
		if (panel.getTotalItemOccurrences() != null) {
			totalItemOccurance = panel.getTotalItemOccurrences().getValue();
		}
		int panelAccuracyScale = panel.getPanelAccuracyScale();
		//ignore the panel accuracy scale value, bcos the function is reverted
		panelAccuracyScale = 0;
		
		String panelDateConstrain = null;
		// generate panel date constrain
		if (panel.getPanelDateFrom() != null || panel.getPanelDateTo() != null) {
			panelDateConstrain = generatePanelDateConstrain(
					dateConstrainHandler, panel.getPanelDateFrom(), panel
							.getPanelDateTo());
		}

		for (ItemType item : panel.getItem()) {
			// read the first item
			// ItemType item = panel.getItem().get(0);
			if (item.getFacttablecolumn() != null
					|| item.getDimColumnname() != null || item.getItemKey().toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_MASTERID)) {

				if (item.getDimTablename() != null) {
					if (item.getDimTablename().equalsIgnoreCase("patient_dimension") ||
							item.getDimTablename().equalsIgnoreCase("visit_dimension")) { 
						continue;
					}
				}
				
				if (i == 0) {
					i = 1;
				} else {
					if (totalItemOccurance > 1) {
						factByProviderSql += "UNION ALL \n";
					} else {
						factByProviderSql += "UNION \n";
					}
				}

				factByProviderSql += (" SELECT  "
						+ obsFactSelectClause + " FROM \n");

				// check if the item key has "patient_set_coll_id:XXXX" as
				// prefix
				String itemKey = item.getItemKey();

				if (itemKey.toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_SET)) {
					String[] itemKeyParam = itemKey.split(":");
					factByProviderSql += (" select patient_num from "
							+ this.getDbSchemaName()
							+ "qt_patient_set_collection where patient_set_coll_id = "
							+ itemKeyParam[1] + "  \n");
					item.setFacttablecolumn("patient_num");

				} else if (itemKey.toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_ENCOUNTER_SET)) { // check
					// if
					// the
					// item
					// key
					// has
					// "patient_enc_coll_id:XXXX"
					// as
					// prefix
					String[] itemKeyParam = itemKey.split(":");
					factByProviderSql += (" select encounter_num from "
							+ this.getDbSchemaName()
							+ "qt_patient_enc_collection where patient_enc_coll_id = "
							+ itemKeyParam[1] + "  \n");
					//WAS item.setFacttablecolumn("encount_num");
					item.setFacttablecolumn("encounter_num");
				} else {
					DimensionFilter providerFilter = new DimensionFilter(item,
							this.getDbSchemaName(), dataSourceLookup);
					factByProviderSql += (" "
							+ providerFilter.getFromSqlString() + "  \n");

				}
				factByProviderSql += ", " + this.getDbSchemaName()
				+ getFactTable() + " obs \n";

				String tempSqlClause = "",containsJoinSql = "";
				
				//OMOP addition
				String facttablecolumn = item.getFacttablecolumn();
				if(derivedFactTable == true){

					if(item.getFacttablecolumn().contains(".")){

						String factColumnName = item.getFacttablecolumn();

						int lastIndex = factColumnName.lastIndexOf(".");

						facttablecolumn = (factColumnName.substring(lastIndex+1));
					}
				}

				// OMOP WAS item.getFacttablecolumn();
				//	String fullWhereClause1 = fullWhereClause
				//			+ (" AND obs." + item.getFacttablecolumn()
				//					+ " = dimension." + item.getFacttablecolumn());


				String fullWhereClause1 = fullWhereClause
						+ (" AND obs." + facttablecolumn
						+ " = dimension." + facttablecolumn);

				//factByProviderSql += tableLookupJoinClause;
				tempSqlClause+= tableLookupJoinClause;

				//factByProviderSql += (" WHERE \n" + fullWhereClause1 + "\n");
				tempSqlClause+= (" WHERE \n" + fullWhereClause1 + "\n");

				//if output option has modifier option false, then select modifier_cd = '@'
				if (outputOptionList.getObservationSet() != null && outputOptionList.getObservationSet().isWithmodifiers() == false) {
					//factByProviderSql += (" AND  obs.modifier_cd = '@' \n");
					tempSqlClause+= (" AND  obs.modifier_cd = '@' \n");
				}
				
				boolean itemConstrainValueFlag = false, modifierConstrainValueFlag = false;
				//if modifier constrain is present in the request, then generate modifier constrain sql
				ItemType.ConstrainByModifier modifierConstrain = item.getConstrainByModifier();
				if (modifierConstrain != null) { 
					// generate sql for item date constrain
					ModifierConstrainsHandler modifierConstrainUtil = new ModifierConstrainsHandler(this.getDbSchemaName());
					String itemModifierConstrainSql = modifierConstrainUtil.constructModifierConstainClause(modifierConstrain);
					if ((itemModifierConstrainSql != null)
							&& (itemModifierConstrainSql.length() > 0)) {
						//factByProviderSql += (" AND (" + itemModifierConstrainSql + ")\n");
						tempSqlClause+=(" AND (" + itemModifierConstrainSql + ")\n");
					}
					if (modifierConstrain.getConstrainByValue() != null && 
							modifierConstrain.getConstrainByValue().size()>0) { 
						modifierConstrainValueFlag = true;
					}
					
				}
				String unitCdSwitchClause = "", unitCdInClause = "";
				String modifierUnitCdSwitchClause = "", modifierUnitCdInClause = "";
				
				if (item.getConstrainByValue() != null
				&& item.getConstrainByValue().size() > 0) { 
					itemConstrainValueFlag = true;
				}
				// if value constrain is given, generate value constrain sql
				if (  itemConstrainValueFlag ||  modifierConstrainValueFlag ) {
					//generate sql for unit_cd conversion
					if ( projectParamMap != null && projectParamMap.get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION) != null) {
						String unitCdConversionFlag = (String)projectParamMap.get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
						if (unitCdConversionFlag != null && unitCdConversionFlag.equalsIgnoreCase("ON")) { 
							if (item.getMetadataxml() != null && item.getMetadataxml().getContent().get(0) != null) {
								Element valueMetadataElement = (Element)item.getMetadataxml().getContent().get(0);
								UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
								unitCdSwitchClause = unitConverstionUtil.buildUnitCdSwitchClause(valueMetadataElement,false,"obs.");
								unitCdInClause = unitConverstionUtil.buildUnitCdInClause(valueMetadataElement,"");
							}
							
							if (modifierConstrainValueFlag) { 
								XmlValueType xmlValueType = this.modifierMetadataXmlMap.get(item.getConstrainByModifier().getModifierKey()+item.getConstrainByModifier().getAppliedPath());
								if (xmlValueType != null && xmlValueType.getAny().get(0) != null) {
									Element valueMetadataElement = (Element)xmlValueType.getAny().get(0);
									UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
									modifierUnitCdSwitchClause = unitConverstionUtil.buildUnitCdSwitchClause(valueMetadataElement,false,"obs.");
									modifierUnitCdInClause = unitConverstionUtil.buildUnitCdInClause(valueMetadataElement,"");
								}
							}
						}
					}
				}
				
				ValueConstrainsHandler vh = new ValueConstrainsHandler();
				if (unitCdSwitchClause.length()>0) {
						vh.setUnitCdConversionFlag(true, unitCdInClause, unitCdSwitchClause);
				}
				String[] valueConstrainSql = new String[] {"",""} ;
				if (itemConstrainValueFlag) { 
					valueConstrainSql = vh
							.constructValueConstainClause(item
									.getConstrainByValue(), this.dataSourceLookup.getServerType(),this.getDbSchemaName(),panelAccuracyScale);
					if (panelAccuracyScale>0) { 
						containsJoinSql = valueConstrainSql[1];
					}
				} 
				
				String[] modifierConstrainValueSql = new String[]{"",""};
				if (modifierConstrainValueFlag) { 
					if (modifierUnitCdInClause.length()>0) { 
						vh.setUnitCdConversionFlag(true, modifierUnitCdInClause, modifierUnitCdSwitchClause);
					}
					modifierConstrainValueSql = vh.constructValueConstainClause(buildItemValueConstrain(modifierConstrain.getConstrainByValue()),this.dataSourceLookup.getServerType(),this.getDbSchemaName(),panelAccuracyScale);
					if (panelAccuracyScale>0) { 
						containsJoinSql += modifierConstrainValueSql[1];
					}
				}
				
				if ((valueConstrainSql[0] != null
							&& valueConstrainSql[0].length() > 0)|| (modifierConstrainValueSql[0] != null
									&& modifierConstrainValueSql[0].length() > 0)) {
						//factByProviderSql += (" AND (" + valueConstrainSql[0] + modifierConstrainValueSql[0] +  ")\n");
					tempSqlClause+=  (" AND (" + valueConstrainSql[0] + modifierConstrainValueSql[0] +  ")\n");
						
				}
				
				factByProviderSql += containsJoinSql;
				
				factByProviderSql += tempSqlClause;

				// add start and end date constrains

				List<ConstrainByDate> constrainByDateList = item
						.getConstrainByDate();
				for (ConstrainByDate constrainByDate : constrainByDateList) {

					ConstrainDateType dateFrom = constrainByDate.getDateFrom();
					ConstrainDateType dateTo = constrainByDate.getDateTo();

					String dateFromColumn = null, dateToColumn = null;
					InclusiveType dateFromInclusive = null, dateToInclusive = null;
					XMLGregorianCalendar dateFromValue = null, dateToValue = null;
					String dateConstrainSql = null;

					// obs.start_date, obs.end_date
					if (dateFrom != null || dateTo != null) {

						if (dateFrom != null) {
							dateFromInclusive = dateFrom.getInclusive();
							dateFromValue = dateFrom.getValue();
							if (dateFrom.getTime() != null
									&& dateFrom.getTime().name() != null
									&& dateFrom
											.getTime()
											.name()
											.equalsIgnoreCase(
													ConstrainDateTimeType.END_DATE
															.name())) {
								dateFromColumn = "obs.end_date";
							} else {
								dateFromColumn = "obs.start_date";
							}

						}

						if (dateTo != null) {
							dateToInclusive = dateTo.getInclusive();
							dateToValue = dateTo.getValue();
							if (dateTo.getTime() != null
									&& dateTo.getTime().name() != null
									&& dateTo.getTime().name().equals(
											ConstrainDateTimeType.END_DATE
													.name())) {
								dateToColumn = "obs.end_date";
							} else {
								dateToColumn = "obs.start_date";
							}
						}

						dateConstrainSql = dateConstrainHandler
								.constructDateConstrainClause(dateFromColumn,
										dateToColumn, dateFromInclusive,
										dateToInclusive, dateFromValue,
										dateToValue);
						if (dateConstrainSql != null) {
							factByProviderSql += (" AND " + dateConstrainSql + "\n");
						}

						// item.getConstrainByModifier().get(0).getModifierName()
					}
				}
				// generate panel date constrain
				if (panelDateConstrain != null) {
					factByProviderSql += (" AND (" + panelDateConstrain + ")\n");
				}
			}
		}
		
		if (factByProviderSql.trim().length()<10) { 
			return "";
		}

		int invert = panel.getInvert();

		if (invert == 1) {
			String invertSql = ("( SELECT " + obsFactSelectClause + " FROM \n");
			invertSql += " " + this.getDbSchemaName()
			+ getFactTable() + " obs \n";
			invertSql += tableLookupJoinClause;
			invertSql += (" WHERE \n" + fullWhereClause + ")\n");
			factByProviderSql = invertSql + " EXCEPT \n " + "("
					+ factByProviderSql + ")";
		}

		if (totalItemOccurance > 1) {
			factByProviderSql = "SELECT  a.*, row_number() over(order by obs_patient_num) as rnum FROM (\n"
					+ factByProviderSql;
		} else {
			factByProviderSql = "SELECT  a.*, row_number() over(order by obs_patient_num) as rnum FROM (\n"
					+ factByProviderSql;
		}

		factByProviderSql += "  ) a \n";
		// factByProviderSql += " ORDER BY 2,5,3) a \n";

		return factByProviderSql;
	}

	/**
	 * Function to uses given select, join and where clause to build core pdo
	 * query, without any filter (concept and provider)
	 * 
	 * @param obsFactSelectClause
	 * @param tableLookupJoinClause
	 * @param fullWhereClause
	 * @return
	 */
	private String factQueryWithoutFilter(String obsFactSelectClause,
			String tableLookupJoinClause, String fullWhereClause) {
		String factSql = "SELECT  b.*, ROW_NUMBER() OVER (order by obs_patient_num) rnum FROM (\n";
		factSql += (" SELECT  "
				+ obsFactSelectClause + " FROM " + this.getDbSchemaName() +  getFactTable() + " obs\n");

		factSql += tableLookupJoinClause;

		factSql += (" WHERE \n" + fullWhereClause + "\n");
		if (this.outputOptionList.getObservationSet().isWithmodifiers() == false) {
			factSql += " AND obs.modifier_cd = '@' ";
		}
		// lcp5 following was causing invalid sql  12/20/16
//		factSql += " ORDER BY obs.patient_num,obs.start_date,obs.concept_cd,obs.instance_num,obs.modifier_cd,obs.rowid) b \n";

		return factSql;
	}

	/**
	 * Generate fact's join clause for table pdo
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 */
	private String getLookupJoinClause(boolean detailFlag, boolean blobFlag,
			boolean statusFlag) {
		String joinClause = " ";

		joinClause = " left JOIN "
				+ this.getDbSchemaName()
				+ "code_lookup modifier_lookup \n"
				+ " ON (obs.modifier_cd = modifier_lookup.code_Cd AND modifier_lookup.column_cd = 'MODIFIER_CD') \n"
				+ " left JOIN " 
				+ "(select name_char, concept_cd from "
				+ this.getDbSchemaName()
				+ "concept_dimension group by name_char, concept_cd) concept_lookup "
				+ "ON (obs.concept_cd = concept_lookup.concept_Cd)  \n"
				+ " left JOIN " + this.getDbSchemaName()
				+ "provider_dimension provider_lookup \n"
				+ " ON (obs.provider_id = provider_lookup.provider_id) \n";

		if (detailFlag) {
			joinClause += " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup location_lookup \n"
					+ " ON (obs.location_Cd = location_lookup.code_Cd AND location_lookup.column_cd = 'LOCATION_CD') \n";
		}

		return joinClause;
	}

	private void deleteTempTable(Connection conn) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			//conn
				//	.createStatement()
			deleteStmt.executeUpdate(
							"drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
			
		} catch (SQLException sqle) {
			;
		} finally {
			try {
				if(deleteStmt != null)
					deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private List<ItemType.ConstrainByValue> buildItemValueConstrain(List<ItemType.ConstrainByModifier.ConstrainByValue> modifierConstrainList) {
		List<ItemType.ConstrainByValue> itemValueConstrainList = new ArrayList<ItemType.ConstrainByValue>();
		for (ItemType.ConstrainByModifier.ConstrainByValue modifierValueConstrain : modifierConstrainList) { 
			ItemType.ConstrainByValue constrainByValue = new ItemType.ConstrainByValue();
			constrainByValue.setValueConstraint(modifierValueConstrain.getValueConstraint());
			constrainByValue.setValueOperator(modifierValueConstrain.getValueOperator()) ; 
			constrainByValue.setValueType(modifierValueConstrain.getValueType()); 
			constrainByValue.setValueUnitOfMeasure(modifierValueConstrain.getValueUnitOfMeasure()) ;
			itemValueConstrainList.add(constrainByValue);
		}
		return itemValueConstrainList;
	}

	/**
	 * Helper function to build unique visit, patient, concept list from
	 * observation fact
	 * 
	 * @param encounterNum
	 * @param patientNum
	 * @param providerId
	 * @param conceptCd
	 */
	private void addToDistinctList(String encounterNum, String patientNum,
			String providerId, String conceptCd,String modifierCd) {
		if (visitFactRelated.isSelected() || eidFactRelated.isSelected()) {
			if (!visitFactList.contains(encounterNum)) {
				visitFactList.add(encounterNum);
			}
		}

		if (patientFactRelated.isSelected() || pidFactRelated.isSelected()) {
			if (!patientFactList.contains(patientNum)) {
				patientFactList.add(patientNum);
			}
		}

		if (providerFactRelated.isSelected()) {
			if (!providerFactList.contains(providerId)) {
				providerFactList.add(providerId);
			}
		}

		if (conceptFactRelated.isSelected()) {
			if (!conceptFactList.contains(conceptCd)) {
				conceptFactList.add(conceptCd);
			}
		}
		
		if (modifierFactRelated.isSelected()) {
			if (!modifierFactList.contains(modifierCd)) {
				modifierFactList.add(modifierCd);
			}
		}

	}

	/**
	 * Executive the given query
	 * 
	 * @param conn
	 * @param querySql
	 * @return
	 * @throws SQLException
	 */
	private ResultSet executeQuery(Connection conn, PreparedStatement stmt,
			String querySql, int sqlParamCount) throws SQLException {

		stmt = conn.prepareStatement(querySql);

		if (inputOptionListHandler.isCollectionId()) {
			for (int i = 1; i <= sqlParamCount; i++) {
				stmt.setInt(i, Integer.parseInt(inputOptionListHandler
						.getCollectionId()));
			}
		}

		ResultSet resultSet = stmt.executeQuery();

		return resultSet;

		// JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
		// return rowSet;
	}

	/**
	 * Build plain pdo's observation fact
	 * 
	 * @param rowSet
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private ObservationSet buildPDOFact(ResultSet rowSet, String panelName)
			throws SQLException, IOException {
		ObservationSet currentObsFactSetType = new ObservationSet();
		currentObsFactSetType.setPanelName(panelName);

		I2B2PdoFactory.ObservationFactBuilder observationFactBuilder = new I2B2PdoFactory().new ObservationFactBuilder(
				obsFactFactRelated.isSelectDetail(), obsFactFactRelated
						.isSelectBlob(), obsFactFactRelated.isSelectStatus(), dataSourceLookup.getServerType());

		while (rowSet.next()) {
			ObservationType obsFactType = null;

			obsFactType = observationFactBuilder.buildObservationSet(rowSet);

			if (obsFactFactRelated.isSelected()) {
				currentObsFactSetType.getObservation().add(obsFactType);
			}

			addToDistinctList(obsFactType.getEventId().getValue(), obsFactType
					.getPatientId().getValue(), obsFactType.getObserverCd()
					.getValue(), obsFactType.getConceptCd().getValue(), obsFactType.getModifierCd().getValue());
		}

		return currentObsFactSetType;
	}

	/**
	 * Build table pdo observaton fact
	 * 
	 * @param rowSet
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private ObservationSet buildTablePDOFact(ResultSet rowSet, String panelName)
			throws SQLException, IOException {
		// obsFactSetTypeList = new
		// Vector<PatientDataType.ObservationFactSet>();
		boolean detailFlag = obsFactFactRelated.isSelectDetail();
		boolean booleanFlag = obsFactFactRelated.isSelectBlob();
		boolean statusFlag = obsFactFactRelated.isSelectStatus();
		ObservationSet currentObservationSet = new ObservationSet();
		currentObservationSet.setPanelName(panelName);

		RPDRPdoFactory.ObservationFactBuilder observationFactBuilder = new RPDRPdoFactory.ObservationFactBuilder(
				detailFlag, booleanFlag, statusFlag);

		while (rowSet.next()) {
			ObservationType observation = null;
			observation = observationFactBuilder.buildObservationSet(rowSet,
					"HIVE");

			if (obsFactFactRelated.isSelected()) {
				currentObservationSet.getObservation().add(observation);
			}

			
			addToDistinctList(observation.getEventId().getValue(), observation.getPatientId().getValue(),
					observation.getObserverCd().getValue(), observation.getConceptCd().getValue(), observation.getModifierCd().getValue());
		}

		return currentObservationSet;
	}

	private String generatePanelDateConstrain(
			DateConstrainHandler dateConstrainHandler,
			ConstrainDateType dateFrom, ConstrainDateType dateTo)
			throws I2B2Exception {

		String dateFromColumn = null, dateToColumn = null;
		InclusiveType dateFromInclusive = null, dateToInclusive = null;
		XMLGregorianCalendar dateFromValue = null, dateToValue = null;
		String dateConstrainSql = null;

		if (dateFrom != null || dateTo != null) {

			if (dateFrom != null) {
				dateFromInclusive = dateFrom.getInclusive();
				dateFromValue = dateFrom.getValue();

				if (dateFrom.getTime() != null
						&& dateFrom.getTime().name() != null
						&& dateFrom.getTime().name().equalsIgnoreCase(
								dateFrom.getTime().END_DATE.name())) {
					dateFromColumn = "obs.end_date";
				} else {

					dateFromColumn = "obs.start_date";
				}

			}

			if (dateTo != null) {
				dateToInclusive = dateTo.getInclusive();
				dateToValue = dateTo.getValue();

				if (dateTo.getTime() != null
						&& dateTo.getTime().name() != null
						&& dateTo.getTime().name().equalsIgnoreCase(
								dateTo.getTime().END_DATE.name())) {
					dateToColumn = "obs.end_date";
				} else {

					dateToColumn = "obs.start_date";
				}

			}

			dateConstrainSql = dateConstrainHandler
					.constructDateConstrainClause(dateFromColumn, dateToColumn,
							dateFromInclusive, dateToInclusive, dateFromValue,
							dateToValue);
		}
		return dateConstrainSql;
	}

	public List<String> getPanelSqlList() {
		return this.panelSqlList;
	}
	
	protected DerivedFactColumnsType getFactColumnsFromOntologyCell(String itemKey)
			throws ConceptNotFoundException, OntologyException {
		DerivedFactColumnsType factColumns = new DerivedFactColumnsType();
		try {
			
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);

			SecurityType securityType = PMServiceAccountUtil
					.getServiceSecurityType(dataSourceLookup.getDomainId());
			
			factColumns = CallOntologyUtil.callGetFactColumns(itemKey,
					securityType, dataSourceLookup.getProjectPath(),
					ontologyUrl +"/getDerivedFactColumns");
		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + itemKey
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

//		if (factColumns.isEmpty()) {
//			throw new ConceptNotFoundException("[" + itemKey + "] ");

//		} 

		return factColumns;
	}


}
