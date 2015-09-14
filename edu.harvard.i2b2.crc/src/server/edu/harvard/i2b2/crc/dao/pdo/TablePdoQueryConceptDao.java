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
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;

/**
 * Class to support concept section of table pdo query $Id:
 * TablePdoQueryConceptDao.java,v 1.13 2008/07/21 19:53:40 rk903 Exp $
 * 
 * @author rkuttan
 */
public class TablePdoQueryConceptDao extends CRCDAO implements
		ITablePdoQueryConceptDao {

	private DataSourceLookup dataSourceLookup = null;
	private String schemaName = null;

	public TablePdoQueryConceptDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		setDataSource(dataSource);
	}

	public ConceptSet getConceptByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		ConceptSet conceptSet = new ConceptSet();
		RPDRPdoFactory.ConceptBuilder conceptBuilder = new RPDRPdoFactory.ConceptBuilder(
				detailFlag, blobFlag, statusFlag);
		ConceptFactRelated conceptFactRelated = new ConceptFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = conceptFactRelated.getSelectClause();
		String serverType = dataSourceLookup.getServerType();
		String factTempTable = "";
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = dataSource.getConnection();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				factTempTable = this.getDbSchemaName()
						+ FactRelatedQueryHandler.TEMP_FACT_PARAM_TABLE;
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) || serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL )) {
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
						+ factTempTable;
				
				if (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))
					insertSql = " CAST(coalesce(char_param1, '0') as integer) ";
				//else 
				//	insertSql += " (char_param1) ";
				insertSql += "(char_param1) select distinct obs_concept_cd from ( "
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
					+ factTempTable + ") order by concept_path";
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
			PdoTempTableUtil tempTableUtil = new PdoTempTableUtil(); 
			tempTableUtil.clearTempTable(serverType, conn, factTempTable);
			
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

	/**
	 * Function returns concepts based on list of concept codes
	 * 
	 * @param conceptCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return ConceptSet
	 * @throws I2B2DAOException
	 */
	public ConceptSet getConceptByConceptCd(List<String> conceptCdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		ConceptSet conceptSet = new ConceptSet();
		RPDRPdoFactory.ConceptBuilder conceptBuilder = new RPDRPdoFactory.ConceptBuilder(
				detailFlag, blobFlag, statusFlag);
		log.debug("input concept cd list size " + conceptCdList.size());
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
				oracle.jdbc.driver.OracleConnection conn1 = null;// (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
			//			.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ "  FROM "
						+ getDbSchemaName()
						+ "concept_dimension concept WHERE concept.concept_cd IN (SELECT * FROM TABLE (?))";
				log.debug("Executing sql[" + finalSql + "]");
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
				tempTableName = this.getDbSchemaName()
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				try {
					tempStmt.executeUpdate("drop table " + tempTableName);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, tempTableName, conceptCdList);
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
			PdoTempTableUtil tempTableUtil = new PdoTempTableUtil();
			tempTableUtil.deleteTempTableSqlServer(conn, tempTableName);
			
			try {
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return conceptSet;
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

	
}
