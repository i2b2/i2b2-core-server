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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.jboss.resource.adapter.jdbc.WrappedConnection;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;

/**
 * This class handles Concept dimension query's related to PDO request $Id:
 * PdoQueryConceptDao.java,v 1.11 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryConceptDao extends CRCDAO implements IPdoQueryConceptDao {

	private DataSourceLookup dataSourceLookup = null;

	public PdoQueryConceptDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
	}

	/** log * */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Get concepts detail from concept code list
	 * 
	 * @param conceptCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return {@link PatientDataType.ConceptDimensionSet}
	 * @throws I2B2DAOException
	 */
	public ConceptSet getConceptByConceptCd(List<String> conceptCdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		ConceptSet conceptDimensionSet = new ConceptSet();
		log.debug("Size of input concept cd list " + conceptCdList.size());
		Connection conn = null;
		PreparedStatement query = null;
		String tempTableName = "";
		try {
			conn = getDataSource().getConnection();
			ConceptFactRelated conceptFactRelated = new ConceptFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));

			String selectClause = conceptFactRelated.getSelectClause();
			String serverType = dataSourceLookup.getServerType();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				// get oracle connection from jboss wrapped connection
				// Otherwise Jboss wrapped connection fails when using oracle
				// Arrays
				oracle.jdbc.driver.OracleConnection conn1 =null;
				//(oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
				//		.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ "  FROM "
						+ getDbSchemaName()
						+ "concept_dimension concept WHERE concept.concept_cd IN (SELECT * FROM TABLE (?))";
				log.debug("Pdo Concept sql [" + finalSql + "]");
				query = conn1.prepareStatement(finalSql);

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);

				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						conceptCdList.toArray(new String[] {}));
				query.setArray(1, paramArray);
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) || 
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				tempTableName = SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + tempTableName);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, tempTableName, conceptCdList, serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL));
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "concept_dimension concept WHERE concept.concept_cd IN (select distinct char_param1 FROM "
						+ tempTableName + ") order by concept_path";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);

			}
			ResultSet resultSet = query.executeQuery();

			I2B2PdoFactory.ConceptBuilder conceptBuilder = new I2B2PdoFactory().new ConceptBuilder(
					detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
			while (resultSet.next()) {
				ConceptType conceptDimensionType = conceptBuilder
						.buildConceptSet(resultSet);
				conceptDimensionSet.getConcept().add(conceptDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("", ioEx);
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
		return conceptDimensionSet;
	}

	/**
	 * Get concept children by item key
	 * 
	 * @param itemKey
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public ConceptSet getChildrentByItemKey(String itemKey, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		ConceptSet conceptDimensionSet = new ConceptSet();
		if (itemKey != null) {
			if (itemKey.lastIndexOf('\\') == itemKey.length() - 1) {
				itemKey = itemKey + "%";
			} else {
				log.debug("Adding \\ at the end of the Concept path ");
				itemKey = itemKey + "\\%";
			}
		}
		log.debug("getChildrenByItemKey [" + itemKey + "]");
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = getDataSource().getConnection();
			ConceptFactRelated conceptFactRelated = new ConceptFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));

			String selectClause = conceptFactRelated.getSelectClause();
			String serverType = dataSourceLookup.getServerType();
			String finalSql = "";
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				finalSql = "Select * from (SELECT "
						+ " RowNum RowNum, "
						+ selectClause
						+ "  FROM "
						+ getDbSchemaName()
						+ "concept_dimension concept WHERE concept_path LIKE ? order by concept_path)  ";

			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				finalSql = "Select * from ( SELECT "
						+ selectClause
						+ " ROW_NUMBER() OVER (ORDER BY concept_path) AS RowNum"
						+ "  FROM "
						+ getDbSchemaName()
						+ "concept_dimension concept WHERE concept_path LIKE ? order by concept_path) ";

			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				finalSql = "Select * from ( SELECT "
						+ selectClause
						+ " ROW_NUMBER() OVER (ORDER BY concept_path) AS RowNum"
						+ "  FROM "
						+ getDbSchemaName()
						+ "concept_dimension concept WHERE concept_path LIKE ? order by concept_path) ";
				itemKey = itemKey.replaceAll("\\\\", "\\\\\\\\");
			}
			log.debug("Pdo Concept sql [" + finalSql + "]");
			query = conn.prepareStatement(finalSql);
			query.setString(1, itemKey);
			ResultSet resultSet = query.executeQuery();

			I2B2PdoFactory.ConceptBuilder conceptBuilder = new I2B2PdoFactory().new ConceptBuilder(
					detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
			while (resultSet.next()) {
				ConceptType conceptDimensionType = conceptBuilder
						.buildConceptSet(resultSet);
				conceptDimensionSet.getConcept().add(conceptDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("", ioEx);
		} finally {

			try {
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return conceptDimensionSet;
	}

	private void uploadTempTable(Statement tempStmt, String tempTable,
			List<String> patientNumList, boolean isPostgresql) throws SQLException {
		String createTempInputListTable = "create "
				 + (isPostgresql ? " temp ": "" )
				+ " table " + tempTable
				+ " ( char_param1 varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table" + tempTable);
		PreparedStatement preparedStmt = tempStmt.getConnection()
				.prepareStatement("insert into " + tempTable + " values (?)");
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			preparedStmt.setString(1, singleValue);
			preparedStmt.addBatch();
			log.debug("adding batch [" + i + "] " + singleValue);
			i++;
			if (i % 100 == 0) {
				log.debug("batch insert [" + i + "]");
				preparedStmt.executeBatch();

			}
		}
		log.debug("batch insert [" + i + "]");
		preparedStmt.executeBatch();
	}

	public ConceptSet getConceptByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		ConceptSet conceptSet = new ConceptSet();
		I2B2PdoFactory.ConceptBuilder conceptBuilder = new I2B2PdoFactory().new ConceptBuilder(
				detailFlag, blobFlag, statusFlag, dataSourceLookup.getServerType());
		ConceptFactRelated conceptFactRelated = new ConceptFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = conceptFactRelated.getSelectClause();
		String serverType = dataSourceLookup.getServerType();
		String tempTable = "";
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = dataSource.getConnection();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				tempTable = FactRelatedQueryHandler.TEMP_FACT_PARAM_TABLE;
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();
				tempTable = SQLServerFactRelatedQueryHandler.TEMP_FACT_PARAM_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + tempTable);
				} catch (SQLException sqlex) {
					;
				}
				String createTempInputListTable = "create " 
						 + (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? " temp ": "" )
						+ " table "
						+ tempTable
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
						+ "(char_param1) select distinct obs_concept_cd from ( "
						+ panelSql + ") b";

				log.debug("Executing SQL [ " + insertSql + "]");
				sqlParamCount = sqlParamCountList.get(i++);
				// conn.createStatement().executeUpdate(insertSql);
				executeTotalSql(insertSql, conn, sqlParamCount,
						inputOptionListHandler);

			}

			String finalSql = "SELECT "
					+ selectClause
					+ " FROM "
					+ getDbSchemaName()
					+ "concept_dimension concept where concept_cd in (select distinct char_param1 from "
					+ tempTable + ") order by concept_path";
			log.debug("Executing SQL [" + finalSql + "]");
			

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			while (resultSet.next()) {
				ConceptType concept = conceptBuilder.buildConceptSet(resultSet);
				conceptSet.getConcept().add(concept);
			}
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
		return conceptSet;
	}

	private void executeTotalSql(String totalSql, Connection conn,
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
