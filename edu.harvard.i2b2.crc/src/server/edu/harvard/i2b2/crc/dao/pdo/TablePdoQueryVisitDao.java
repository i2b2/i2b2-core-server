/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import oracle.sql.ArrayDescriptor;

//import org.jboss.resource.adapter.jdbc.WrappedConnection;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.PatientListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.VisitListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

/**
 * Class to support event section of table pdo query $Id:
 * TablePdoQueryVisitDao.java,v 1.11 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class TablePdoQueryVisitDao extends CRCDAO implements
		ITablePdoQueryVisitDao {

	private DataSourceLookup dataSourceLookup = null;
	private String schemaName = null;
	private List<ParamType> metaDataParamList = null;
	

	public TablePdoQueryVisitDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		setDataSource(dataSource);
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
	}

	public void setMetaDataParamList(List<ParamType> metaDataParamList) { 
		this.metaDataParamList = metaDataParamList; 
	}
	
	/**
	 * Function to return EventSet from visit information
	 * 
	 * @param encounterNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return EventSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitsByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		EventSet eventSet = new EventSet();

		RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(
				detailFlag, blobFlag, statusFlag);
		System.out.println("input encounter list size "
				+ encounterNumList.size());

		Connection conn = null;
		PreparedStatement query = null;
		String tempTableName = "";
		try {
			conn = getDataSource().getConnection();

			// create type StudentIdArrayType as varray(1000) of integer;
			String selectClause = getSelectClause(detailFlag, blobFlag,
					statusFlag);
			String joinClause = getLookupJoinClause(detailFlag, blobFlag,
					statusFlag);
			String serverType = dataSourceLookup.getServerType();

			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = null;//(oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
		//				.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "visit_dimension visit \n"
						+ joinClause
						+ " WHERE visit.encounter_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY)))";
				log.debug("Executing sql[" + finalSql + "]");
				query = conn1.prepareStatement(finalSql);

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);
				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						encounterNumList.toArray(new String[] {}));
				query.setArray(1, paramArray);
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				tempTableName = getDbSchemaName()
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + tempTableName);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, tempTableName, encounterNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "visit_dimension visit \n"
						+ joinClause
						+ " WHERE visit.encounter_num IN (select distinct CAST(coalesce(char_param1, '0') as integer) FROM "
						+ tempTableName + " ) order by encounter_num,patient_num ";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);

			}
			long startTimeSql = System.currentTimeMillis();
			ResultSet resultSet = query.executeQuery();
			long endTimeSql = System.currentTimeMillis();
			long totalTimeSql = endTimeSql - startTimeSql;
			log.debug("********* Total time for visit sql ****"
					+ totalTimeSql);
			long startTime = System.currentTimeMillis();
			while (resultSet.next()) {
				EventType event = eventBuilder.buildEventSet(resultSet, "i2b2",this.metaDataParamList);
				eventSet.getEvent().add(event);
			}
			long endTime = System.currentTimeMillis();
			long totalTime = endTimeSql - startTimeSql;
			log.debug("********* Total time for visit objects ****"
					+ totalTime);
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				PdoTempTableUtil tempUtil = new PdoTempTableUtil();
				tempUtil.deleteTempTableSqlServer(conn, tempTableName);
			}
			try {
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return eventSet;
	}

	/**
	 * Function to return EventSet from visit information
	 * 
	 * @param visitListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return EventSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromVisitList(
			EventListType visitListType, boolean detailFlag, boolean blobFlag,
			boolean statusFlag) throws I2B2DAOException {
		VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(
				dataSourceLookup, visitListType);
		String inSqlClause = visitListTypeHandler.generateWhereClauseSql();
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause = getLookupJoinClause(detailFlag, blobFlag,
				statusFlag);
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName() + "visit_dimension visit " + joinClause
				+ " WHERE visit.encounter_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " ) order by visit.encounter_num,visit.patient_num \n";

		EventSet eventSet = new EventSet();
		RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(
				detailFlag, blobFlag, statusFlag);
		Connection conn = null;
		PreparedStatement preparedStmt = null;

		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			log.debug("Executing sql[" + mainSqlString + "]");

			if (visitListTypeHandler.isCollectionId()) {
				String patientEncCollectionId = visitListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt
						.setInt(1, Integer.parseInt(patientEncCollectionId));
			} else if (visitListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				visitListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

			ResultSet resultSet = preparedStmt.executeQuery();

			while (resultSet.next()) {
				// VisitDimensionType visitDimensionType =
				// getVisitDimensionType(resultSet);
				EventType event = eventBuilder.buildEventSet(resultSet, "i2b2",this.metaDataParamList);
				eventSet.getEvent().add(event);
			}
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				try {
					visitListTypeHandler.deleteTempTable(conn);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return eventSet;
	}

	/**
	 * Function to return EventSet for given patient set
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromPatientList(
			PatientListType patientListType, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause = getLookupJoinClause(detailFlag, blobFlag,
				statusFlag);

		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName() + "visit_dimension visit " + joinClause
				+ " WHERE visit.patient_num IN ( ";
		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();

		mainSqlString += inSqlClause;
		mainSqlString += " ) order by visit.encounter_num,visit.patient_num \n";

		log.debug("Visit dimension sql " + mainSqlString);

		EventSet eventSet = new EventSet();
		RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(
				detailFlag, blobFlag, statusFlag);

		Connection conn = null;
		PreparedStatement preparedStmt = null;

		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			log.debug("Executing sql[" + mainSqlString + "]");

			if (patientListTypeHandler.isCollectionId()) {
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setInt(1, Integer.parseInt(patientListTypeHandler
						.getCollectionId()));
			} else if (patientListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				patientListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else if (patientListTypeHandler.isEntireSet()) {
				// log.debug("No need to pass parameter to sql");
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

			ResultSet resultSet = preparedStmt.executeQuery();

			while (resultSet.next()) {
				EventType event = eventBuilder.buildEventSet(resultSet, "i2b2",this.metaDataParamList);
				eventSet.getEvent().add(event);
			}
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			if (patientListTypeHandler.isEnumerationSet()) {
				try {
					patientListTypeHandler.deleteTempTable(conn);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return eventSet;
	}
	
	  private String buildCustomSelectClause(String prefix) {
	    	String detailSelectClause = " ";
	    	for (Iterator<ParamType> iterator = this.metaDataParamList.iterator();iterator.hasNext();) { 
	    		ParamType paramType = iterator.next();
	    		
	    			detailSelectClause += prefix + "." + paramType.getColumn() + "  " + prefix + "_" + paramType.getColumn();
	    			if (iterator.hasNext()) { 
	    				detailSelectClause += " , ";
	    			}
	    		
	    	}
	    	return detailSelectClause;
	    }
	
	  private String buildCustomLookupSelectClause() {
	    	String detailSelectClause = " ";
	    	for (Iterator<ParamType> iterator = this.metaDataParamList.iterator();iterator.hasNext();) { 
	    		ParamType paramType = iterator.next();
	    		if (paramType.getType().equalsIgnoreCase("string")) { 
	    			detailSelectClause += " , " +   paramType.getColumn() + "_lookup" + ".name_char" +   "  "  + paramType.getColumn() + "_name";
	    		}
	    	}
	    	return detailSelectClause;
	    }

	private String getSelectClause(boolean detailFlag, boolean blobFlag,
			boolean statusFlag) {
		String selectClause = "";
		selectClause = " visit.encounter_num visit_encounter_num, visit.patient_num visit_patient_num ";

		if (detailFlag) {
			selectClause += " ," + buildCustomSelectClause("visit");
			selectClause +=   buildCustomLookupSelectClause() ; 
			selectClause += ",  visit.start_date visit_start_date,visit.end_date visit_end_date ";
			//selectClause += ", inout_lookup.name_char inout_name,location_lookup.name_char location_name, active_status_lookup.name_char active_status_name";
		}

		if (blobFlag) {
			selectClause += ", visit.visit_blob visit_visit_blob ";
		}

		if (statusFlag) {
			selectClause += " , visit.update_date visit_update_date, visit.download_date visit_download_date, visit.import_date visit_import_date, visit.sourcesystem_cd visit_sourcesystem_cd, visit.upload_id visit_upload_id ";
		}

		return selectClause;
	}

	private String getLookupJoinClause(boolean detailFlag, boolean blobFlag,
			boolean statusFlag) {
		String joinClause = " ";
	
		if (detailFlag) {
			for (Iterator<ParamType> iterator = this.metaDataParamList.iterator();iterator.hasNext();) { 
				ParamType paramType = iterator.next();
				if (paramType.getType().equalsIgnoreCase("string")) { 
				String columnName = paramType.getColumn();
				joinClause += " left JOIN " 
					+ this.getDbSchemaName()
					+ "code_lookup " + columnName + "_lookup \n"
					+ " ON (visit." + columnName + " = " + columnName + "_lookup.code_Cd AND  upper(" + columnName +"_lookup.column_cd) = '" + columnName.toUpperCase() + "') \n";
				}
			}
			
		}

		return joinClause;
	}

	private void uploadTempTable(Statement tempStmt, String tempTableName,
			List<String> patientNumList) throws SQLException {
		String createTempInputListTable = "create table " + tempTableName
				+ " ( char_param1 varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table" + tempTableName);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			tempStmt.addBatch("insert into " + tempTableName + " values ('"
					+ singleValue + "' )");
			log.debug("adding batch" + singleValue);
			i++;
			if (i % 100 == 0) {
				log.debug("batch insert");
				tempStmt.executeBatch();

			}
		}
		log.debug("batch insert1");
		tempStmt.executeBatch();
	}

	

	public EventSet getVisitByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		EventSet eventSet = new EventSet();
		RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(
				detailFlag, blobFlag, statusFlag);
		VisitFactRelated eventFactRelated = new VisitFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause = getLookupJoinClause(detailFlag, blobFlag,
				statusFlag);

		String serverType = dataSourceLookup.getServerType();
		String factTempTable = "";
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = dataSource.getConnection();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				factTempTable = getDbSchemaName()
						+ FactRelatedQueryHandler.TEMP_FACT_PARAM_TABLE;
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				factTempTable = getDbSchemaName()
						+ SQLServerFactRelatedQueryHandler.TEMP_FACT_PARAM_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + factTempTable);
				} catch (SQLException sqlex) {
					;
				}
				String createTempInputListTable = "create table "
						+ factTempTable
						+ " ( set_index int, char_param1 varchar(500) )";
				tempStmt.executeUpdate(createTempInputListTable);
				log.debug("created temp table" + factTempTable);
			}
			// if the inputlist is enumeration, then upload the enumerated input
			// to temp table.
			// the uploaded enumerated input will be used in the fact join.
			if (inputOptionListHandler.isEnumerationSet()) {
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
			}
			String insertSql = "";
			int i = 0;
			int sqlParamCount = 0;
			ResultSet resultSet = null;
			for (String panelSql : panelSqlList) {
				insertSql = " insert into "
						+ factTempTable
						+ "(char_param1) select distinct obs_encounter_num from ( "
						+ panelSql + ") b";

				log.debug("Executing SQL [ " + insertSql + "]");
				sqlParamCount = sqlParamCountList.get(i++);
				// conn.createStatement().executeUpdate(insertSql);
				executeUpdateSql(insertSql, conn, sqlParamCount,
						inputOptionListHandler);

			}

			String finalSql = "SELECT "
					+ selectClause
					+ " FROM "
					+ getDbSchemaName()
					+ "visit_dimension visit "
					+ joinClause
					+ " where encounter_num in (select distinct CAST(coalesce(char_param1, '0') as integer) from "
					+ factTempTable + ") order by encounter_num";
			log.debug("Executing SQL [" + finalSql + "]");
			

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			while (resultSet.next()) {
				EventType event = eventBuilder.buildEventSet(resultSet, "i2b2",this.metaDataParamList);
				eventSet.getEvent().add(event);
			}
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			
			PdoTempTableUtil tempUtil = new PdoTempTableUtil();
			tempUtil.clearTempTable(dataSourceLookup.getServerType(), conn, factTempTable);
			if (inputOptionListHandler != null
					&& inputOptionListHandler.isEnumerationSet()) {
				try {
					inputOptionListHandler.deleteTempTable(conn);
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
			try {

				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return eventSet;

	}

	private void executeUpdateSql(String totalSql, Connection conn,
			int sqlParamCount, IInputOptionListHandler inputOptionListHandler)
			throws SQLException {

		PreparedStatement stmt = conn.prepareStatement(totalSql);

		log.debug(totalSql + " [ " + sqlParamCount + " ]");
		if (inputOptionListHandler.isCollectionId()) {
			for (int i = 1; i <= sqlParamCount; i++) {
				stmt.setInt(i, Integer.parseInt(inputOptionListHandler
						.getCollectionId()));
			}
		}

		stmt.executeUpdate();

	}
}
