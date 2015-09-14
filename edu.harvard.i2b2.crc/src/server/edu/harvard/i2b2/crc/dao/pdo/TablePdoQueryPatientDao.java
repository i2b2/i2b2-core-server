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
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

/**
 * Class to support Patient section of table pdo query $Id:
 * TablePdoQueryPatientDao.java,v 1.11 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class TablePdoQueryPatientDao extends CRCDAO implements
		ITablePdoQueryPatientDao {

	private DataSourceLookup dataSourceLookup = null;
	private String schemaName = null;
	private List<ParamType> metaDataParamList = null;
	

	public TablePdoQueryPatientDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;

	}
	
	public void setMetaDataParamList(List<ParamType> metaDataParamList) { 
		this.metaDataParamList = metaDataParamList; 
	}

	/**
	 * Function returns Patient information for given list of patient number in
	 * TablePDO format
	 * 
	 * @param patientNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientByPatientNum(List<String> patientNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		Connection conn = null;

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(
				detailFlag, blobFlag, statusFlag);
		PreparedStatement query = null;
		String tempTableName = "";
		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			String serverType = dataSourceLookup.getServerType();

			String selectClause = getSelectClause(detailFlag, blobFlag,
					statusFlag);
			String joinClause = getLookupJoinClause(detailFlag, blobFlag,
					statusFlag);
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = null;//(oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
			//			.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_dimension patient "
						+ joinClause
						+ " WHERE patient.patient_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY)))";
				log.debug("Executing sql[" + finalSql + "]");
				query = conn1.prepareStatement(finalSql);

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);

				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						patientNumList.toArray(new String[] {}));
				query.setArray(1, paramArray);

			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				// create temp table
				// load to temp table
				// execute sql
				log.debug("creating temp table");
				tempTableName = this.getDbSchemaName()
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
				java.sql.Statement tempStmt = conn.createStatement();

				try {
					tempStmt.executeUpdate("drop table " + tempTableName);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, tempTableName, patientNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_dimension patient "
						+ joinClause
						+ " WHERE patient.patient_num IN (select distinct char_param1 FROM "
						+ tempTableName + ") order by patient_num";
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
			// JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,
						"i2b2",metaDataParamList);
				patientSet.getPatient().add(patient);

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
		return patientSet;
	}

	/**
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromPatientSet(PatientListType patientListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause = getLookupJoinClause(detailFlag, blobFlag,
				statusFlag);
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName() + "patient_dimension patient " + joinClause
				+ " WHERE patient.patient_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " ) order by patient.patient_num \n";

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(
				detailFlag, blobFlag, statusFlag);
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
				preparedStmt
						.setInt(1, Integer.parseInt(patientSetCollectionId));

			} else if (patientListTypeHandler.isEnumerationSet()) {

				patientListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}
			long startTimeSql = System.currentTimeMillis();
			ResultSet resultSet = preparedStmt.executeQuery();
			long endTimeSql = System.currentTimeMillis();
			long totalTimeSql = endTimeSql - startTimeSql;
			log.debug("********* Total time for patient sql ****"
					+ totalTimeSql);
			long startTime = System.currentTimeMillis();
			// JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,
						"i2b2",metaDataParamList);
				patientSet.getPatient().add(patient);
			}
			long endTime = System.currentTimeMillis();
			long totalTime = endTimeSql - startTimeSql;
			log.debug("********* Total time for patient objects ****"
					+ totalTime);

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
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
		return patientSet;
	}

	/**
	 * Function returns patient information for given list of encounters
	 * 
	 * @param visitListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientSet
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromVisitSet(EventListType visitListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(
				dataSourceLookup, visitListType);

		String inSqlClause = null;
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause = getLookupJoinClause(detailFlag, blobFlag,
				statusFlag);
		String mainSqlString = " select " + selectClause + "  from "
				+ getDbSchemaName() + "patient_dimension patient " + joinClause
				+ " where patient.patient_num in ";

		// if visit set id, then take patient num directly from
		// qt_patient_enc_collection table, else go thru visit dimension to get
		// patient num
		if (visitListTypeHandler.isCollectionId()) {
			inSqlClause = visitListTypeHandler.generatePatentSql();
			mainSqlString += " ( " + inSqlClause + " ) ";
		} else {
			inSqlClause = visitListTypeHandler.generateWhereClauseSql();
			mainSqlString += " (select distinct patient_num from "
					+ getDbSchemaName() + "visit_dimension where "
					+ " encounter_num in ( " + inSqlClause + " )) order by patient.patient_num ";
		}

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(
				detailFlag, blobFlag, statusFlag);
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			log.debug("Executing sql[" + mainSqlString + "]");

			if (visitListTypeHandler.isCollectionId()) {
				String encounterSetCollectionId = visitListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setInt(1, Integer
						.parseInt(encounterSetCollectionId));

			} else if (visitListTypeHandler.isEnumerationSet()) {

				visitListTypeHandler.uploadEnumerationValueToTempTable(conn);
				preparedStmt = conn.prepareStatement(mainSqlString);

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}
			ResultSet resultSet = preparedStmt.executeQuery();
			// JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,
						"i2b2",metaDataParamList);
				patientSet.getPatient().add(patient);
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			if (visitListTypeHandler.isEnumerationSet()) {
				try {
					visitListTypeHandler.deleteTempTable(conn);
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
		return patientSet;

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
	        		detailSelectClause +=  " , " +   paramType.getColumn() + "_lookup" + ".name_char" +   "  "  + paramType.getColumn() + "_name";
	    		}
	    	}
	    	detailSelectClause += " , vital_status_cd_lookup.name_char vital_status_cd_name";
	    	return detailSelectClause;
	    }
	  
	  
	  
	/**
	 * Function to generate select clause based on input flags
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 */
	private String getSelectClause(boolean detailFlag, boolean blobFlag,
			boolean statusFlag) {
		String selectClause = "";
		selectClause = "  patient.patient_num patient_patient_num";

		if (detailFlag) {
			selectClause += " ,patient.vital_status_cd patient_vital_status_cd, vital_Status_cd_lookup.name_char vital_status_cd_name, patient.birth_date patient_birth_date " ; 
			selectClause += " ," + buildCustomSelectClause("patient");
			selectClause +=  buildCustomLookupSelectClause() ; 
			//status_lookup.name_char vital_status_name, sex_lookup.name_char sex_name, language_lookup.name_char language_name, race_lookup.name_char race_name, religion_lookup.name_char religion_name, marital_status_lookup.name_char marital_status_name ";
		}
		if (blobFlag) {
			selectClause += ", patient.patient_blob patient_patient_blob ";
		}
		if (statusFlag) {
			selectClause += " , patient.update_date patient_update_date, patient.download_date patient_download_date, patient.import_date patient_import_date, patient.sourcesystem_cd patient_sourcesystem_cd, patient.upload_id patient_upload_id ";
		}

		return selectClause;
	}

	/**
	 * Function returns sql join clause, which joins lookup tables
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return String joinclause required for table pdo lookup
	 */
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
						+ " ON (patient." + columnName + " = " + columnName + "_lookup.code_Cd AND  upper(" + columnName +"_lookup.column_cd) = '" + columnName.toUpperCase() + "') \n";
				}
			}
			
				
			
			/*
			joinClause = " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup vital_status_lookup \n"
					+ " ON (patient.vital_status_Cd = vital_status_lookup.code_Cd AND vital_status_lookup.column_cd = 'VITAL_STATUS_CD') \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup sex_lookup \n"
					+ " ON (patient.sex_Cd = sex_lookup.code_Cd AND sex_lookup.column_cd = 'SEX_CD') \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup language_lookup \n"
					+ " ON (patient.language_Cd = language_lookup.code_Cd AND language_lookup.column_cd = 'LANGUAGE_CD') \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup race_lookup \n"
					+ " ON (patient.race_Cd = race_lookup.code_Cd AND race_lookup.column_cd = 'RACE_CD') \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup marital_status_lookup \n"
					+ " ON (patient.marital_status_cd = marital_status_lookup.code_Cd AND marital_status_lookup.column_cd = 'MARITAL_STATUS_CD') \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup religion_lookup \n"
					+ " ON (patient.religion_Cd = religion_lookup.code_Cd AND religion_lookup.column_cd = 'RELIGION_CD') \n";
			*/

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

	

	public PatientSet getPatientByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(
				detailFlag, blobFlag, statusFlag);
		PatientFactRelated patientFactRelated = new PatientFactRelated(
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
					+ "patient_dimension patient "
					+ joinClause
					+ " where patient_num in (select distinct char_param1 from "
					+ factTempTable + ") order by patient_num";
			log.debug("Executing SQL [" + finalSql + "]");
			

			query = conn.prepareStatement(finalSql);

			resultSet = query.executeQuery();

			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,
						"i2b2",metaDataParamList);
				patientSet.getPatient().add(patient);
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
		return patientSet;

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
