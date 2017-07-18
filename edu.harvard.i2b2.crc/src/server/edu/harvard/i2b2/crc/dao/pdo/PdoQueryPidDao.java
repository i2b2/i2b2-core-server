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
import edu.harvard.i2b2.crc.dao.pdo.output.PidFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.PidSet;
import edu.harvard.i2b2.crc.datavo.pdo.PidType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType.PatientId;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType.Pid;

/**
 * Class to build patient section of plain pdo $Id: PdoQueryPatientDao.java,v
 * 1.11 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryPidDao extends CRCDAO implements IPdoQueryPidDao {

	private DataSourceLookup dataSourceLookup = null;

	public PdoQueryPidDao(DataSourceLookup dataSourceLookup,
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
	public PidSet getPidByPatientNum(List<String> patientNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
					throws I2B2DAOException {

		Connection conn = null;
		PreparedStatement query = null;
		PidSet pidSet = new PidSet();

		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			PidFactRelated pidRelated = new PidFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = pidRelated.getSelectClause();
			ResultSet resultSet = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {

				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_mapping pm WHERE pm.patient_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY))) order by pm_patient_num";
				log.debug("Executing [" + finalSql + "]");

				oracle.jdbc.driver.OracleConnection conn1 = null;// (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
				//	.getUnderlyingConnection();
				query = conn.prepareStatement(finalSql);
				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);

				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						patientNumList.toArray(new String[] {}));
				query.setArray(1, paramArray);
				resultSet = query.executeQuery();
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				// create temp table
				// load to temp table
				// execute sql
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();

				uploadTempTable(tempStmt, patientNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_mapping pm WHERE pm.patient_num IN (select distinct char_param1 FROM "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ ") order by pm_patient_num";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);
				resultSet = query.executeQuery();
			} else  if 
			(dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL))
			{
				// create temp table
				// load to temp table
				// execute sql
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();

				uploadTempTable(tempStmt, patientNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_mapping pm WHERE pm.patient_num IN (select distinct char_param1 FROM "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE.substring(1)
						+ ") order by pm_patient_num";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);
				resultSet = query.executeQuery();		
			}

			RPDRPdoFactory.PidBuilder pidBuilder = new RPDRPdoFactory.PidBuilder(
					detailFlag, blobFlag, statusFlag);
			pidSet = buildPidSetFromResultSet(resultSet, pidBuilder);

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
		return pidSet;
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
	public PidSet getPidFromPatientSet(PatientListType patientListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
					throws I2B2DAOException {

		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();

		PidFactRelated pidRelated = new PidFactRelated(buildOutputOptionType(
				detailFlag, blobFlag, statusFlag));
		String selectClause = pidRelated.getSelectClause();
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName()
				+ "patient_mapping pm WHERE pm.patient_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " ) order by pm_patient_num \n";

		PidSet pidSet = new PidSet();
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
				preparedStmt.setInt(1, Integer.parseInt(patientSetCollectionId));

			} else if (patientListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				patientListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}
			ResultSet resultSet = preparedStmt.executeQuery();
			RPDRPdoFactory.PidBuilder pidBuilder = new RPDRPdoFactory.PidBuilder(
					detailFlag, blobFlag, statusFlag);
			pidSet = buildPidSetFromResultSet(resultSet, pidBuilder);

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
		return pidSet;
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
	public PidSet getPidByPidList(PidListType pidList, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		Connection conn = null;
		PreparedStatement query = null;
		PidSet pidSet = new PidSet();
		String tempTableName = this.getDbSchemaName() + FactRelatedQueryHandler.TEMP_PARAM_TABLE;
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			tempTableName = this.getDbSchemaName() + SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL)) {
			tempTableName =  SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE.substring(1);
		}
		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			PidFactRelated pidRelated = new PidFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = pidRelated.getSelectClause();
			ResultSet resultSet = null;



			// create temp table
			// load to temp table
			// execute sql
			log.debug("creating temp table");
			java.sql.Statement tempStmt = conn.createStatement();

			upLoadPidListToTempTable(conn, tempTableName, pidList);
			String finalSql = "SELECT "
					+ selectClause
					+ " FROM "
					+ getDbSchemaName()
					+ "patient_mapping pm WHERE "
					+ "patient_num in (select patient_num from "
					+ getDbSchemaName()
					+ "patient_mapping where "
					+ " exists (select char_param2 FROM "
					+ tempTableName
					+ " where patient_ide = char_param2 and patient_ide_source = char_param1 )) order by pm_patient_num";
			log.debug("Executing [" + finalSql + "]");

			query = conn.prepareStatement(finalSql);
			resultSet = query.executeQuery();

			RPDRPdoFactory.PidBuilder pidBuilder = new RPDRPdoFactory.PidBuilder(
					detailFlag, blobFlag, statusFlag);
			pidSet = buildPidSetFromResultSet(resultSet, pidBuilder);
			if (pidSet.getPid()!=null) { 
				log.debug("pid set size " + pidSet.getPid().size());
				if (pidSet.getPid().size()>0) {
					log.debug("pid set size " + pidSet.getPid().get(0).getPatientId().getValue());
				}
			}

		} catch (SQLException ex) {
			log.error("", ex);
			throw new I2B2DAOException("sql exception", ex);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			PdoTempTableUtil tempUtil = new PdoTempTableUtil();
			tempUtil.clearTempTable(dataSourceLookup.getServerType(), conn, tempTableName);

			try {
				JDBCUtil.closeJdbcResource(null, query, conn);

			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}

		}
		return pidSet;
	}

	private PidSet buildPidSetFromResultSet(ResultSet resultSet,
			RPDRPdoFactory.PidBuilder pidBuilder) throws SQLException,
			IOException {
		String prevPatientNum = "";
		PidType pidType = new PidType();
		PidSet pidSet = new PidSet();
		boolean firstFlag = true;
		PatientId singlePatientId = new PatientId();
		PidType singlePidType = null;
		PidType.PatientMapId pidMapId = null;
		String tempSinglePidType = null;
		while (resultSet.next()) {
			singlePidType = pidBuilder.buildPidSet(resultSet);
			pidMapId = singlePidType.getPatientMapId().get(0);

			tempSinglePidType = singlePidType.getPatientId().getValue();

			if (pidMapId.getSource().equalsIgnoreCase("hive")) {
				singlePatientId = new PatientId();
				singlePatientId.setSource(pidMapId.getSource());
				singlePatientId.setValue(pidMapId.getValue());
				singlePatientId.setSourcesystemCd(pidMapId.getSourcesystemCd());
				singlePatientId.setStatus(pidMapId.getStatus());
				singlePatientId.setUploadId(pidMapId.getUploadId());
				singlePatientId.setUpdateDate(pidMapId.getUpdateDate());
				singlePatientId.setImportDate(pidMapId.getImportDate());
				singlePatientId.setDownloadDate(pidMapId.getDownloadDate());
			}

			if (prevPatientNum.equals(tempSinglePidType)) {
				if (!pidMapId.getSource().equalsIgnoreCase("hive")) {
					pidType.getPatientMapId().add(pidMapId);
				} else {
					pidType.setPatientId(singlePatientId);
				}

			} else {
				if (!firstFlag) {
					pidSet.getPid().add(pidType);
				} else {
					firstFlag = false;
				}

				pidType = new PidType();
				if (pidMapId.getSource().equalsIgnoreCase("hive")) {
					pidType.setPatientId(singlePatientId);
				} else {
					pidType.getPatientMapId().add(pidMapId);
				}
			}
			prevPatientNum = tempSinglePidType;

		}
		if ((pidType.getPatientId() != null && pidType.getPatientId()
				.getValue() != null)
				|| (pidType.getPatientMapId() != null && pidType
				.getPatientMapId().size() > 0)) {
			// pidType.setPatientId(singlePatientId);
			pidSet.getPid().add(pidType);
		}
		return pidSet;
	}

	private void uploadTempTable(Statement tempStmt, List<String> patientNumList)
			throws SQLException {
		String temp_pdo = SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL))
			temp_pdo = temp_pdo.substring(1);

		String createTempInputListTable = "create table "
				+ temp_pdo
				+ " ( char_param1 varchar(100) )";
	
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL))
		 createTempInputListTable = "create temp table "
				+ temp_pdo
				+ " ( char_param1 varchar(100) )";

		
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table"
				+ temp_pdo);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			tempStmt.addBatch("insert into "
					+ temp_pdo
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
				if (deleteStmt != null)
				deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void upLoadPidListToTempTable(Connection conn,
			String tempTableName, PidListType pidListType) throws SQLException {

		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			String createTempInputListTable = "create table "
					+ tempTableName
					+ " (set_index int, char_param1 varchar(200), char_param2 varchar(200) )";
			tempStmt.executeUpdate(createTempInputListTable);
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL)) {
			String createTempInputListTable = "create temp table "
					+ tempTableName
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

	public PidSet getPidByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		PidSet pidSet = new PidSet();
		RPDRPdoFactory.PidBuilder pidBuilder = new RPDRPdoFactory.PidBuilder(
				detailFlag, blobFlag, statusFlag);
		PidFactRelated pidFactRelated = new PidFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = pidFactRelated.getSelectClause();

		String serverType = dataSourceLookup.getServerType();
		String tempTable = "";
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = dataSource.getConnection();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				tempTable = FactRelatedQueryHandler.TEMP_PARAM_TABLE;
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
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
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				tempTable = SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE.substring(1);
				try {
					tempStmt.executeUpdate("drop table " + tempTable);
				} catch (SQLException sqlex) {
					;
				}
				String createTempInputListTable = "create temp table  " + tempTable
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
						+ "(char_param1) select distinct obs_patient_num from ( "
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
					+ "patient_mapping pm "
					+ " where patient_num in (select distinct ";

			if (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))
				finalSql += " CAST(coalesce(char_param1, '0') as integer) ";
			else
				finalSql += " char_param1 ";


			finalSql += "from " 
					+ tempTable + ") order by patient_num";
			log.debug("Executing SQL [" + finalSql + "]");
			System.out.println("Final Sql " + finalSql);

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			// while (resultSet.next()) {
			// PidType pid = pidBuilder.buildPidSet(resultSet);
			// pidSet.getPid().add(pid);
			// }
			pidSet = buildPidSetFromResultSet(resultSet, pidBuilder);
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
		return pidSet;

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
