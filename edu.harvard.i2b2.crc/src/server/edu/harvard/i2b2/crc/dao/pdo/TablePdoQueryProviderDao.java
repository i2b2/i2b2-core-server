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
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;

/**
 * Class to support observer section of table pdo query $Id:
 * TablePdoQueryProviderDao.java,v 1.10 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class TablePdoQueryProviderDao extends CRCDAO implements
		ITablePdoQueryProviderDao {
	private DataSourceLookup dataSourceLookup = null;
	private String schemaName = null;

	public TablePdoQueryProviderDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		setDbSchemaName(dataSourceLookup.getFullSchema());
		setDataSource(dataSource);
	}

	public ObserverSet getProviderByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		ObserverSet observerSet = new ObserverSet();
		RPDRPdoFactory.ProviderBuilder observerBuilder = new RPDRPdoFactory.ProviderBuilder(
				detailFlag, blobFlag, statusFlag);
		ProviderFactRelated providerFactRelated = new ProviderFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = providerFactRelated.getSelectClause();
		String serverType = dataSourceLookup.getServerType();
		String factTempTable = "";
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = dataSource.getConnection();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				factTempTable = this.getDbSchemaName()
						+ FactRelatedQueryHandler.TEMP_FACT_PARAM_TABLE;
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				factTempTable = this.getDbSchemaName()
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
						+ "(char_param1) select distinct obs_provider_id from ( "
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
					+ "provider_dimension provider where provider_id in (select distinct char_param1 from "
					+ factTempTable + ") order by provider_path";
			log.debug("Executing SQL [" + finalSql + "]");
			

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			while (resultSet.next()) {
				ObserverType observer = observerBuilder
						.buildObserverSet(resultSet);
				observerSet.getObserver().add(observer);
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
		return observerSet;
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

	/**
	 * Returns observerset for the given list of provider id
	 * 
	 * @param providerIdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return ObserverSet
	 * @throws I2B2DAOException
	 */
	public ObserverSet getProviderById(List<String> providerIdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		log.debug("input encounter list size " + providerIdList.size());

		ObserverSet observerSet = new ObserverSet();
		RPDRPdoFactory.ProviderBuilder providerBuilder = new RPDRPdoFactory.ProviderBuilder(
				detailFlag, blobFlag, statusFlag);
		Connection conn = null;
		PreparedStatement query = null;
		String tempTable = "";
		try {
			conn = getDataSource().getConnection();

			String selectClause = getSelectClause(detailFlag, blobFlag,
					statusFlag);
			String serverType = dataSourceLookup.getServerType();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = null;//(oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
			//			.getUnderlyingConnection();
				query = conn1
						.prepareStatement("SELECT "
								+ selectClause
								+ " FROM "
								+ getDbSchemaName()
								+ "provider_dimension provider WHERE provider.provider_id IN (SELECT * FROM TABLE (?))");

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);

				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						providerIdList.toArray(new String[] {}));
				query.setArray(1, paramArray);
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				tempTable = this.getDbSchemaName()
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + tempTable);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, tempTable, providerIdList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "provider_dimension provider WHERE provider.provider_id IN (select distinct char_param1 FROM "
						+ tempTable + ") order by provider_path";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);

			}

			ResultSet resultSet = query.executeQuery();

			// JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				ObserverType observer = providerBuilder
						.buildObserverSet(resultSet);
				observerSet.getObserver().add(observer);
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
				PdoTempTableUtil tempTableUtil = new PdoTempTableUtil(); 
				tempTableUtil.deleteTempTableSqlServer(conn, tempTable);
			}
			try {
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return observerSet;
	}

	/**
	 * Function returns select clause based on given flag
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 */
	private String getSelectClause(boolean detailFlag, boolean blobFlag,
			boolean statusFlag) {
		String selectClause = "";

		selectClause = " provider.provider_id provider_provider_id, provider.provider_path provider_provider_path ";

		if (detailFlag) {
			selectClause += ", provider.name_char provider_name_char ";
		}

		if (blobFlag) {
			selectClause += ", provider.provider_blob provider_provider_blob ";
		}

		if (statusFlag) {
			selectClause += " , provider.update_date provider_update_date, provider.download_date provider_download_date, provider.import_date provider_import_date, provider.sourcesystem_cd provider_sourcesystem_cd, provider.upload_id provider_upload_id ";
		}

		return selectClause;
	}

	private void uploadTempTable(Statement tempStmt, String tempTable,
			List<String> patientNumList) throws SQLException {
		String createTempInputListTable = "create table " + tempTable
				+ " ( char_param1 varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table" + tempTable);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			tempStmt.addBatch("insert into " + tempTable + " values ('"
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

	
}
