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
import edu.harvard.i2b2.crc.dao.pdo.input.EidListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.PatientListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.EidFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.EidSet;
import edu.harvard.i2b2.crc.datavo.pdo.EidType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType.Pid;

/**
 * Class to build patient section of plain pdo $Id: PdoQueryPatientDao.java,v
 * 1.11 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryEidDao extends CRCDAO implements IPdoQueryEidDao {

	private DataSourceLookup dataSourceLookup = null;

	public PdoQueryEidDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	/**
	 * Function to return patient dimension data for given list of patient num
	 * 
	 * @param patientNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws Exception
	 */
	public EidSet getEidByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		Connection conn = null;
		PreparedStatement query = null;
		EidSet eidSet = new EidSet();

		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			EidFactRelated eidRelated = new EidFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = eidRelated.getSelectClause();
			ResultSet resultSet = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {

				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "encounter_mapping em WHERE em.encounter_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY))) order by em_encounter_num";
				log.debug("Executing [" + finalSql + "]");

				oracle.jdbc.driver.OracleConnection conn1 = null;// (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
					//	.getUnderlyingConnection();
				query = conn.prepareStatement(finalSql);
				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);

				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						encounterNumList.toArray(new String[] {}));
				query.setArray(1, paramArray);
				resultSet = query.executeQuery();
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL) ) {
				// create temp table
				// load to temp table
				// execute sql
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();

				uploadTempTable(tempStmt, encounterNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "encounter_mapping em WHERE em.encounter_num IN (select distinct char_param1 FROM "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ ") order by em_encounter_num";
				log.debug("Size of the encounter list "
						+ encounterNumList.size());
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);
				resultSet = query.executeQuery();
			}

			RPDRPdoFactory.EidBuilder eidBuilder = new RPDRPdoFactory.EidBuilder(
					detailFlag, blobFlag, statusFlag);
			eidSet = buildEidSetFromResultSet(resultSet, eidBuilder);

		} catch (SQLException ex) {
			log.error("", ex);
			throw new I2B2DAOException("sql exception", ex);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(
						conn,
						SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
			}
			try {
				JDBCUtil.closeJdbcResource(null, query, conn);

			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}

		}
		return eidSet;
	}

	/**
	 * Get Patient dimension data based on patientlist present in input option
	 * list
	 * 
	 * @param patientListType
	 *            {@link PatientListType}
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws I2B2DAOException
	 */
	public EidSet getEidFromPatientSet(PatientListType patientListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();

		EidFactRelated eidRelated = new EidFactRelated(buildOutputOptionType(
				detailFlag, blobFlag, statusFlag));
		String selectClause = eidRelated.getSelectClause();
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName() + "encounter_mapping em,"
				+ getDbSchemaName()
				+ "visit_dimension vd WHERE vd.patient_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " ) and vd.encounter_num = em.encounter_num order by em_encounter_num \n";

		EidSet eidSet = new EidSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			log.debug("Executing sql[" + mainSqlString + "]");

			if (patientListTypeHandler.isCollectionId()) {
				String patientSetCollectionId = patientListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setString(1, patientSetCollectionId);

			} else if (patientListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				patientListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}
			ResultSet resultSet = preparedStmt.executeQuery();
			RPDRPdoFactory.EidBuilder pidBuilder = new RPDRPdoFactory.EidBuilder(
					detailFlag, blobFlag, statusFlag);
			eidSet = buildEidSetFromResultSet(resultSet, pidBuilder);

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("SQLException", sqlEx);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} finally {
			if (patientListTypeHandler.isEnumerationSet()) {
				try {
					patientListTypeHandler.deleteTempTable(conn);
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return eidSet;
	}

	/**
	 * Function to return patient dimension data for given list of pid list
	 * 
	 * @param pidList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws Exception
	 */
	public EidSet getEidByEidList(EidListType eidList, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		Connection conn = null;
		PreparedStatement query = null;
		EidSet eidSet = new EidSet();
		EidListTypeHandler eidListHandler = new EidListTypeHandler(
				dataSourceLookup, eidList);
		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			EidFactRelated eidRelated = new EidFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = eidRelated.getSelectClause();

			ResultSet resultSet = null;

			// create temp table
			// load to temp table
			// execute sql
			log.debug("creating temp table");
			java.sql.Statement tempStmt = conn.createStatement();

			eidListHandler.uploadEnumerationValueToTempTable(conn);
			String tempTableName = eidListHandler.getTempTableName();
			String finalSql = "SELECT "
					+ selectClause
					+ " FROM "
					+ getDbSchemaName()
					+ "encounter_mapping em where encounter_num in ( "
					+ " select encounter_num FROM "
					+ getDbSchemaName()
					+ "encounter_mapping em "
					+ " WHERE exists "
					+ "(select char_param2 FROM "
					+ tempTableName
					+ " where em.encounter_ide = char_param1 and em.encounter_ide_source = char_param2 ) "
					+ ") order by em_encounter_num";
			log.debug("Executing [" + finalSql + "]");

			query = conn.prepareStatement(finalSql);
			resultSet = query.executeQuery();

			RPDRPdoFactory.EidBuilder eidBuilder = new RPDRPdoFactory.EidBuilder(
					detailFlag, blobFlag, statusFlag);
			eidSet = buildEidSetFromResultSet(resultSet, eidBuilder);

		} catch (SQLException ex) {
			log.error("", ex);
			throw new I2B2DAOException("sql exception", ex);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {

			try {
				eidListHandler.deleteTempTable(conn);
			} catch (SQLException e) {

				e.printStackTrace();
			}

			try {
				JDBCUtil.closeJdbcResource(null, query, conn);

			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}

		}
		return eidSet;
	}

	private EidSet buildEidSetFromResultSet(ResultSet resultSet,
			RPDRPdoFactory.EidBuilder eidBuilder) throws SQLException,
			IOException {
		String prevPatientNum = "";
		EidType eidType = new EidType();
		EidSet eidSet = new EidSet();
		boolean firstFlag = true;
		EidType.EventId singleEventId = new EidType.EventId();
		EidType singleEidType = null;
		EidType.EventMapId eidMapId = null;
		String tempSinglePidType = null;
		while (resultSet.next()) {
			singleEidType = eidBuilder.buildEidSet(resultSet);
			eidMapId = singleEidType.getEventMapId().get(0);
			System.out.println("Building  pidMapId " + eidMapId.getValue()
					+ "   " + singleEidType.getEventId().getValue() + " "
					+ eidMapId.getSource());
			tempSinglePidType = singleEidType.getEventId().getValue();

			if (prevPatientNum.equals(singleEidType.getEventId().getValue())) {
				if (!eidMapId.getSource().equalsIgnoreCase("hive")) {
					eidType.getEventMapId().add(eidMapId);
				}
			} else {
				if (firstFlag) {
					if (!eidMapId.getSource().equalsIgnoreCase("hive")) {
						eidType.getEventMapId().add(eidMapId);
					}

					// pidType.getPatientMapId().add(pidMapId);
					firstFlag = false;
				} else {

					eidSet.getEid().add(eidType);
					singleEventId = new EidType.EventId();
					eidType = new EidType();
					if (!eidMapId.getSource().equalsIgnoreCase("hive")) {
						eidType.getEventMapId().add(eidMapId);
					}
				}
			}
			if (eidMapId.getSource().equalsIgnoreCase("hive")) {
				singleEventId.setSource(eidMapId.getSource());
				singleEventId.setValue(eidMapId.getValue());
				singleEventId.setPatientId(eidMapId.getPatientId());
				singleEventId.setPatientIdSource(eidMapId.getPatientIdSource());
				singleEventId.setSourcesystemCd(eidMapId.getSourcesystemCd());
				singleEventId.setStatus(eidMapId.getStatus());
				singleEventId.setUploadId(eidMapId.getUploadId());
				singleEventId.setUpdateDate(eidMapId.getUpdateDate());
				singleEventId.setImportDate(eidMapId.getImportDate());
				singleEventId.setDownloadDate(eidMapId.getDownloadDate());
				eidType.setEventId(singleEventId);
			}
			prevPatientNum = tempSinglePidType;

		}
		// eidType.setEventId(singleEventId);
		if ((eidType.getEventId() != null && eidType.getEventId().getValue() != null)
				|| (eidType.getEventMapId() != null && eidType.getEventMapId()
						.size() > 0)) {
			eidSet.getEid().add(eidType);
		}
		return eidSet;
	}

	private void uploadTempTable(Statement tempStmt, List<String> patientNumList)
			throws SQLException {
		String createTempInputListTable = "create table "
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
				+ " ( char_param1 varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table"
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			tempStmt.addBatch("insert into "
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
					+ " values ('" + singleValue + "' )");
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

	private void deleteTempTable(Connection conn, String tempTableName) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			//conn.createStatement().executeUpdate("drop table " + tempTableName);
			deleteStmt.executeUpdate("drop table " + tempTableName);

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

	private void upLoadEidListToTempTable(Connection conn,
			String tempTableName, PidListType pidListType) throws SQLException {

		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			String createTempInputListTable = "create table "
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
					+ " (set_index int, char_param1 varchar(200), char_param2 varchar(200) )";
			tempStmt.executeUpdate(createTempInputListTable);
		}
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0, j = 1;
		for (Pid pid : pidListType.getPid()) {
			tempStmt.addBatch("insert into " + tempTableName
					+ "(set_index,char_param1,char_param2)  values ("
					+ pid.getIndex() + ",'" + pid.getSource() + "','"
					+ pid.getValue() + "')");
			i++;
			if (i % 100 == 0) {
				tempStmt.executeBatch();

			}
		}
		tempStmt.executeBatch();
	}

	public EidSet getEidByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		EidSet eidSet = new EidSet();
		RPDRPdoFactory.EidBuilder eidBuilder = new RPDRPdoFactory.EidBuilder(
				detailFlag, blobFlag, statusFlag);
		EidFactRelated eidFactRelated = new EidFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = eidFactRelated.getSelectClause();

		String serverType = dataSourceLookup.getServerType();
		String tempTable = "";
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = dataSource.getConnection();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				tempTable = FactRelatedQueryHandler.TEMP_PARAM_TABLE;
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				tempTable = SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + tempTable);
				} catch (SQLException sqlex) {
					;
				}
				String createTempInputListTable = "create table " + tempTable
						+ " ( set_index int, char_param1 varchar(500) )";
				tempStmt.executeUpdate(createTempInputListTable);
				log.debug("created temp table" + tempTable);
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
						+ tempTable
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
					+ "encounter_mapping em "
					+ " where encounter_num in (select distinct char_param1 from "
					+ tempTable + ") order by encounter_num";
			log.debug("Executing SQL [" + finalSql + "]");
			System.out.println("Final Sql " + finalSql);

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			// while (resultSet.next()) {
			// EidType eid = eidBuilder.buildEidSet(resultSet);
			// eidSet.getEid().add(eid);
			// }
			eidSet = buildEidSetFromResultSet(resultSet, eidBuilder);
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			
				PdoTempTableUtil tempUtil = new PdoTempTableUtil(); 
				tempUtil.clearTempTable(dataSourceLookup.getServerType(), conn, tempTable);
			
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
		return eidSet;

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

		int updatedRow = stmt.executeUpdate();
		System.out.println("Total encounter num inserted [" + updatedRow + "]");
	}

}
