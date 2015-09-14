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
 * Class to support visit/event section of plain pdo query $Id:
 * PdoQueryVisitDao.java,v 1.12 2008/04/08 19:41:30 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryVisitDao extends CRCDAO implements IPdoQueryVisitDao {

	private DataSourceLookup dataSourceLookup = null;
	private List<ParamType> metaDataParamList = null;

	public PdoQueryVisitDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDbSchemaName(dataSourceLookup.getFullSchema());
		setDataSource(dataSource);
		this.dataSourceLookup = dataSourceLookup;
	}
	
	public void setMetaDataParamList(List<ParamType> metaDataParamList) { 
		this.metaDataParamList = metaDataParamList; 
	}

	/**
	 * Function to return list of eventset for given encounter number list
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @exception I2B2DAOException
	 */
	public EventSet getVisitsByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		EventSet visitDimensionSet = new EventSet();
		log.debug("visit list size " + encounterNumList.size());
		Connection conn = null;
		PreparedStatement query = null;
		String tempTableName = "";
		try {
			conn = getDataSource().getConnection();
			VisitFactRelated visitRelated = new VisitFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			visitRelated.setMetaDataParamList(this.metaDataParamList);
			String selectClause = visitRelated.getSelectClause();
			String serverType = dataSourceLookup.getServerType();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = null;//(oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
					//	.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "visit_dimension visit WHERE visit.encounter_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY)))";
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
				tempTableName = this.getDbSchemaName()
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				java.sql.Statement tempStmt = conn.createStatement();

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
						+ "visit_dimension visit WHERE visit.encounter_num IN (select distinct char_param1 FROM "
						+ tempTableName + ") order by encounter_num";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);

			}
			ResultSet resultSet = query.executeQuery();
			I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
					detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
			while (resultSet.next()) {
				EventType visitDimensionType = eventBuilder
						.buildEventSet(resultSet,this.metaDataParamList);
				visitDimensionSet.getEvent().add(visitDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
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
		return visitDimensionSet;
	}

	/**
	 * Get visit dimension data base on visit list
	 * (InputOptionList.getVisitListType())
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return I2B2DAOException
	 * @throws Exception
	 */
	public EventSet getVisitDimensionSetFromVisitList(
			EventListType visitListType, boolean detailFlag, boolean blobFlag,
			boolean statusFlag) throws I2B2DAOException {
		VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(
				dataSourceLookup, visitListType);
		String inSqlClause = visitListTypeHandler.generateWhereClauseSql();
		VisitFactRelated visitRelated = new VisitFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		visitRelated.setMetaDataParamList(this.metaDataParamList);
		String selectClause = visitRelated.getSelectClause();

		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName()
				+ "visit_dimension visit WHERE visit.encounter_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " ) order by visit.encounter_num, visit.patient_num \n";

		EventSet visitDimensionSet = new EventSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			log.debug("Executing Sql[" + mainSqlString + "]");

			if (visitListTypeHandler.isCollectionId()) {
				String patientEncCollectionId = visitListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setInt(1,Integer.parseInt(patientEncCollectionId));

			} else if (visitListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				visitListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

			ResultSet resultSet = preparedStmt.executeQuery();
			I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
					detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
			while (resultSet.next()) {
				// VisitDimensionType visitDimensionType =
				// getVisitDimensionType(resultSet);
				EventType visitDimensionType = eventBuilder
						.buildEventSet(resultSet, metaDataParamList);
				visitDimensionSet.getEvent().add(visitDimensionType);
			}
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("io exception", ioEx);
		} finally {
			if (visitListTypeHandler.isEnumerationSet()) {
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

		return visitDimensionSet;
	}

	/**
	 * Get visit dimension from patientlist (InputOptionList.getPatientList())
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.VisitDimensionSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromPatientList(
			PatientListType patientListType, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		VisitFactRelated visitRelated = new VisitFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		visitRelated.setMetaDataParamList(this.metaDataParamList);
		String selectClause = visitRelated.getSelectClause();
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName()
				+ "visit_dimension visit WHERE visit.patient_num IN ( ";
		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();
		mainSqlString += inSqlClause;
		mainSqlString += " ) order by visit.encounter_num, visit.patient_num \n";

		log.debug("Executing sql[" + mainSqlString + "]");
		EventSet visitDimensionSet = new EventSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();

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
			I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
					detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
			while (resultSet.next()) {
				// VisitDimensionType visitDimensionType =
				// getVisitDimensionType(resultSet);
				EventType visitDimensionType = eventBuilder
						.buildEventSet(resultSet,this.metaDataParamList);
				visitDimensionSet.getEvent().add(visitDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("io exception", ioEx);
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
		return visitDimensionSet;
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
		I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
				detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
		VisitFactRelated eventFactRelated = new VisitFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		eventFactRelated.setMetaDataParamList(this.metaDataParamList);
		
		String selectClause = eventFactRelated.getSelectClause();
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
					+ "visit_dimension visit where encounter_num in (select distinct char_param1 from "
					+ factTempTable + ") order by encounter_num";
			log.debug("Executing SQL [" + finalSql + "]");
			System.out.println("Final Sql " + finalSql);

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			while (resultSet.next()) {
				EventType event = eventBuilder.buildEventSet(resultSet,this.metaDataParamList);
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

		System.out.println(totalSql + " [ " + sqlParamCount + " ]");
		if (inputOptionListHandler.isCollectionId()) {
			for (int i = 1; i <= sqlParamCount; i++) {
				stmt.setInt(i, Integer.parseInt(inputOptionListHandler
						.getCollectionId()));
			}
		}

		stmt.executeUpdate();

	}

}
