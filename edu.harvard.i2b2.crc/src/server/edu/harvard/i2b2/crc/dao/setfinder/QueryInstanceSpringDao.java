/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.dao.DataSourceLookupDAOFactory;

/**
 * Class to handle persistance operation of Query instance i.e. each run of
 * query is called query instance $Id: QueryInstanceSpringDao.java,v 1.4
 * 2008/04/08 19:38:24 rk903 Exp $
 * 
 * @author rkuttan
 * @see QtQueryInstance
 */
public class QueryInstanceSpringDao extends CRCDAO implements IQueryInstanceDao {

	JdbcTemplate jdbcTemplate = null;
	SaveQueryInstance saveQueryInstance = null;
	QtQueryInstanceRowMapper queryInstanceMapper = null;
	private DataSourceLookup dataSourceLookup = null;
	
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	
	

	public QueryInstanceSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;
		queryInstanceMapper = new QtQueryInstanceRowMapper();
	}

	/**
	 * Function to create query instance
	 * 
	 * @param queryMasterId
	 * @param userId
	 * @param groupId
	 * @param batchMode
	 * @param statusId
	 * @return query instance id
	 */
	public String createQueryInstance(String queryMasterId, String userId,
			String groupId, String batchMode, int statusId) {
		QtQueryInstance queryInstance = new QtQueryInstance();
		queryInstance.setUserId(userId);
		queryInstance.setGroupId(groupId);
		queryInstance.setBatchMode(batchMode);
		queryInstance.setDeleteFlag("N");

		QtQueryMaster queryMaster = new QtQueryMaster();
		queryMaster.setQueryMasterId(queryMasterId);
		queryInstance.setQtQueryMaster(queryMaster);

		QtQueryStatusType statusType = new QtQueryStatusType();
		statusType.setStatusTypeId(statusId);
		queryInstance.setQtQueryStatusType(statusType);

		Date startDate = new Date(System.currentTimeMillis());
		queryInstance.setStartDate(startDate);
		saveQueryInstance = new SaveQueryInstance(getDataSource(),
				getDbSchemaName(), dataSourceLookup);
		saveQueryInstance.save(queryInstance);

		return queryInstance.getQueryInstanceId();
	}

	/**
	 * Returns list of query instance for the given master id
	 * 
	 * @param queryMasterId
	 * @return List<QtQueryInstance>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryInstance> getQueryInstanceByMasterId(String queryMasterId) {
		String sql = "select *  from " + getDbSchemaName()
				+ "qt_query_instance where query_master_id = ?";
		List<QtQueryInstance> queryInstanceList = jdbcTemplate.query(sql,
				new Object[] { Integer.parseInt(queryMasterId) }, queryInstanceMapper);
		return queryInstanceList;
	}

	/**
	 * Find query instance by id
	 * 
	 * @param queryInstanceId
	 * @return QtQueryInstance
	 */
	public QtQueryInstance getQueryInstanceByInstanceId(String queryInstanceId) {
		String sql = "select *  from " + getDbSchemaName()
				+ "qt_query_instance  where query_instance_id =?";

		QtQueryInstance queryInstance = (QtQueryInstance) jdbcTemplate
				.queryForObject(sql, new Object[] { Integer.parseInt(queryInstanceId ) },
						queryInstanceMapper);

		return queryInstance;
	}

	/**
	 * Update query instance
	 * 
	 * @param queryInstance
	 * @return QtQueryInstance
	 * @throws I2B2DAOException 
	 */
	public QtQueryInstance update(QtQueryInstance queryInstance,
			boolean appendMessageFlag) throws I2B2DAOException {

		Integer statusTypeId = (queryInstance.getQtQueryStatusType() != null) ? queryInstance
				.getQtQueryStatusType().getStatusTypeId()
				: null;
		String messageUpdate = "";
		if (appendMessageFlag) {
			String concatOperator = "";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				concatOperator = "||";
				messageUpdate = " MESSAGE = nvl(MESSAGE,'') " + concatOperator
						+ " ? ";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				//concatOperator = "+";
				// messageUpdate = " MESSAGE = isnull(Cast(MESSAGE as nvarchar(4000)),'') "
				//		+ concatOperator + " ? ";
				// Cast(notes as nvarchar(4000))
				
				//update message field
				updateMessage(queryInstance.getQueryInstanceId(),queryInstance.getMessage(),true);
			
				if (queryInstance.getEndDate() != null) { 
				//update rest of the fields
				String sql = "UPDATE "
					+ getDbSchemaName()
					+ "QT_QUERY_INSTANCE set USER_ID = ?, GROUP_ID = ?,BATCH_MODE = ?,END_DATE = ? ,STATUS_TYPE_ID = ? "
					+ " where query_instance_id = ? ";
			
				jdbcTemplate.update(sql, new Object[] {
					queryInstance.getUserId(),
					queryInstance.getGroupId(),
					queryInstance.getBatchMode(),
					queryInstance.getEndDate(),
					statusTypeId,
					Integer.parseInt( queryInstance.getQueryInstanceId()) });
				} else { 
					//update rest of the fields
					String sql = "UPDATE "
						+ getDbSchemaName()
						+ "QT_QUERY_INSTANCE set USER_ID = ?, GROUP_ID = ?,BATCH_MODE = ?,STATUS_TYPE_ID = ? "
						+ " where query_instance_id = ? ";
				
					jdbcTemplate.update(sql, new Object[] {
						queryInstance.getUserId(),
						queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						statusTypeId,
						Integer.parseInt(queryInstance.getQueryInstanceId()) });
				}
				return queryInstance;
			}

		} else {
			messageUpdate = " MESSAGE = ?";
		}
		
		if (queryInstance.getEndDate() != null) { 
		String sql = "UPDATE "
				+ getDbSchemaName()
				+ "QT_QUERY_INSTANCE set USER_ID = ?, GROUP_ID = ?,BATCH_MODE = ?,END_DATE = ? ,STATUS_TYPE_ID = ?, "
				+ messageUpdate + " where query_instance_id = ? ";
		
		jdbcTemplate.update(sql, new Object[] {
				queryInstance.getUserId(),
				queryInstance.getGroupId(),
				queryInstance.getBatchMode(),
				queryInstance.getEndDate(),
				statusTypeId,
				(queryInstance.getMessage() == null) ? "" : queryInstance
						.getMessage(), Integer.parseInt(queryInstance.getQueryInstanceId()) });
		} else { 
			String sql = "UPDATE "
				+ getDbSchemaName()
				+ "QT_QUERY_INSTANCE set USER_ID = ?, GROUP_ID = ?,BATCH_MODE = ?,STATUS_TYPE_ID = ?, "
				+ messageUpdate + " where query_instance_id = ? ";
		
		jdbcTemplate.update(sql, new Object[] {
				queryInstance.getUserId(),
				queryInstance.getGroupId(),
				queryInstance.getBatchMode(),
				statusTypeId,
				(queryInstance.getMessage() == null) ? "" : queryInstance
						.getMessage(), Integer.parseInt(queryInstance.getQueryInstanceId()) });
		}
		return queryInstance;
	}
	

	/**
	 * Update query instance message
	 * 
	 * @param queryInstanceId
	 * @param message
	 * @param appendMessageFlag
	 * @return 
	 */
	public void updateMessage(String  queryInstanceId, String message,
			boolean appendMessageFlag)  throws  I2B2DAOException {

		String messageUpdate = "";
		if (appendMessageFlag) {
			String concatOperator = "";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)  ) {
				concatOperator = "||";
				messageUpdate = " MESSAGE = nvl(MESSAGE,'') " + concatOperator
						+ " ? ";
			} else 	if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)  ) {
				concatOperator = "||";
				messageUpdate = " MESSAGE = ? ";
	
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				// Cast(notes as nvarchar(4000))
				//messageUpdate = " message.write (?, NULL, 0) ";
				
				Connection conn = null;
				try {
					conn = getDataSource().getConnection();
					CallableStatement callStmt = conn.prepareCall("{call "
							+ getDbSchemaName() + "UPDATE_QUERYINSTANCE_MESSAGE(?,?,?)}");
					callStmt.setString(1, message);
					callStmt.setString(2, queryInstanceId);
					callStmt.registerOutParameter(3, java.sql.Types.VARCHAR);
					// callStmt.setString(2, tempPatientMappingTableName);
					callStmt.execute();
					this.getSQLServerProcedureError(dataSourceLookup.getServerType(),
							callStmt, 3);
					callStmt.close();
				} catch (SQLException sqlEx) {
					sqlEx.printStackTrace();
					throw new I2B2DAOException(
							"SQLException occured" + sqlEx.getMessage(), sqlEx);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new I2B2DAOException("Exception occured" + ex.getMessage(), ex);
				} finally {
					if (conn != null) {
						try {
							conn.close();
						} catch (SQLException sqlEx) {
							sqlEx.printStackTrace();
							log.error("Error while closing connection", sqlEx);
						}
					}
				}
				return ;
				//////

			}

		} else {
			messageUpdate = " MESSAGE = ?";
		}
		String sql = "UPDATE "
				+ getDbSchemaName()
				+ "QT_QUERY_INSTANCE set "
				+ messageUpdate + " where query_instance_id = ? ";
		jdbcTemplate.update(sql, new Object[] {
				
				(message == null) ? "" : 
						message, Integer.parseInt(queryInstanceId) });
		
	}
	
	private static class SaveQueryInstance extends SqlUpdate {

		private String INSERT_ORACLE = "";
		private String INSERT_SQLSERVER = "";
		private String SEQUENCE_ORACLE = "";
		private String SEQUENCE_POSTGRESQL = "";
		private String INSERT_POSTGRESQL = "";

		
		private DataSourceLookup dataSourceLookup = null;

		public SaveQueryInstance(DataSource dataSource, String dbSchemaName,
				DataSourceLookup dataSourceLookup) {
			super();
			this.dataSourceLookup = dataSourceLookup;
			// sqlServerSequenceDao = new
			// SQLServerSequenceDAO(dataSource,dataSourceLookup) ;
			setDataSource(dataSource);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				INSERT_ORACLE = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_INSTANCE "
						+ "(QUERY_INSTANCE_ID, QUERY_MASTER_ID, USER_ID, GROUP_ID,BATCH_MODE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);
				SEQUENCE_ORACLE = "select " + dbSchemaName
						+ "QT_SQ_QI_QIID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				INSERT_SQLSERVER = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_INSTANCE "
						+ "( QUERY_MASTER_ID, USER_ID, GROUP_ID,BATCH_MODE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?)";
				setSql(INSERT_SQLSERVER);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				INSERT_POSTGRESQL = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_INSTANCE "
						+ "(QUERY_INSTANCE_ID, QUERY_MASTER_ID, USER_ID, GROUP_ID,BATCH_MODE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_POSTGRESQL);
				SEQUENCE_POSTGRESQL = "select " // + dbSchemaName
						+ "nextval('qt_query_instance_query_instance_id_seq') ";
				declareParameter(new SqlParameter(Types.INTEGER));

			} 

			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		public void save(QtQueryInstance queryInstance) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int queryInstanceId = 0;
			Object[] object = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) ) {
				object = new Object[] {

				queryInstance.getQtQueryMaster().getQueryMasterId(),
						queryInstance.getUserId(), queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						queryInstance.getStartDate(),
						queryInstance.getEndDate(),
						queryInstance.getQtQueryStatusType().getStatusTypeId(),
						queryInstance.getDeleteFlag() };

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				queryInstanceId = jdbc.queryForInt(SEQUENCE_ORACLE);
				queryInstance.setQueryInstanceId(String
						.valueOf(queryInstanceId));
				object = new Object[] { queryInstance.getQueryInstanceId(),
						queryInstance.getQtQueryMaster().getQueryMasterId(),
						queryInstance.getUserId(), queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						queryInstance.getStartDate(),
						queryInstance.getEndDate(),
						queryInstance.getQtQueryStatusType().getStatusTypeId(),
						queryInstance.getDeleteFlag() };
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				queryInstanceId = jdbc.queryForInt(SEQUENCE_POSTGRESQL);
				queryInstance.setQueryInstanceId(String
						.valueOf(queryInstanceId));
				object = new Object[] { queryInstance.getQueryInstanceId(),
						queryInstance.getQtQueryMaster().getQueryMasterId(),
						queryInstance.getUserId(), queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						queryInstance.getStartDate(),
						queryInstance.getEndDate(),
						queryInstance.getQtQueryStatusType().getStatusTypeId(),
						queryInstance.getDeleteFlag() };
			}

			update(object);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				int queryInstanceIdentityId = jdbc
						.queryForInt("SELECT @@IDENTITY");

				queryInstance.setQueryInstanceId(String
						.valueOf(queryInstanceIdentityId));
				
			}
		}
	}

	private class QtQueryInstanceRowMapper implements RowMapper {
		QueryStatusTypeSpringDao statusTypeDao = new QueryStatusTypeSpringDao(
				dataSource, dataSourceLookup);

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryInstance queryInstance = new QtQueryInstance();
			queryInstance.setQueryInstanceId(rs.getString("QUERY_INSTANCE_ID"));
			QtQueryMaster queryMaster = new QtQueryMaster();
			queryMaster.setQueryMasterId(rs.getString("QUERY_MASTER_ID"));
			queryInstance.setQtQueryMaster(queryMaster);
			queryInstance.setUserId(rs.getString("USER_ID"));
			queryInstance.setGroupId(rs.getString("GROUP_ID"));
			queryInstance.setBatchMode(rs.getString("BATCH_MODE"));
			queryInstance.setStartDate(rs.getTimestamp("START_DATE"));
			queryInstance.setEndDate(rs.getTimestamp("END_DATE"));
			queryInstance.setMessage(rs.getString("MESSAGE"));
			int statusTypeId = rs.getInt("STATUS_TYPE_ID");
			queryInstance.setQtQueryStatusType(statusTypeDao
					.getQueryStatusTypeById(statusTypeId));
			queryInstance.setDeleteFlag(rs.getString("DELETE_FLAG"));
			return queryInstance;
		}
	}
	
	private void getSQLServerProcedureError(String serverType,
			CallableStatement callStmt, int outParamIndex) throws SQLException,
			I2B2Exception {

		if (serverType.equalsIgnoreCase(DataSourceLookupDAOFactory.SQLSERVER)) {
			String errorMsg = callStmt.getString(outParamIndex);
			if (errorMsg != null) {
				log.debug("error codde" + errorMsg);
				throw new I2B2Exception("Error from stored procedure ["
						+ errorMsg + "]");
			}
		}
	}
	
}
