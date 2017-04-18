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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;

/**
 * This is class handles persistance of result instance and its update operation
 * $Id: QueryResultInstanceSpringDao.java,v 1.14 2010/07/22 18:54:51 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryResultInstanceSpringDao extends CRCDAO implements
IQueryResultInstanceDao {

	JdbcTemplate jdbcTemplate = null;
	SavePatientSetResult savePatientSetResult = null;
	PatientSetResultRowMapper patientSetMapper = null;
	DataSourceLookup dataSourceLookup = null;

	public QueryResultInstanceSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

		patientSetMapper = new PatientSetResultRowMapper();
	}

	/**
	 * Function to create result instance for given query instance id. The
	 * result instance status is set to running. Use updatePatientSet function
	 * to change the status to completed or error
	 * 
	 * @param queryInstanceId
	 * @return
	 */
	public String createPatientSet(String queryInstanceId, String resultName)
			throws I2B2DAOException {
		QtQueryResultInstance resultInstance = new QtQueryResultInstance();
		resultInstance.setDeleteFlag("N");

		QueryResultTypeSpringDao resultTypeDao = new QueryResultTypeSpringDao(
				dataSource, dataSourceLookup);
		List<QtQueryResultType> resultType = resultTypeDao
				.getQueryResultTypeByName(resultName);
		if (resultType.size() < 1) {
			throw new I2B2DAOException(" Result type  [" + resultName
					+ "] not found");
		}
		resultInstance.setQtQueryResultType(resultType.get(0));
		resultInstance.setDescription(resultType.get(0).getDescription());
		QtQueryInstance queryInstance = new QtQueryInstance();
		queryInstance.setQueryInstanceId(queryInstanceId);
		resultInstance.setQtQueryInstance(queryInstance);

		QtQueryStatusType queryStatusType = new QtQueryStatusType();
		queryStatusType.setStatusTypeId(StatusEnum.QUEUED.ordinal());  // 1
		resultInstance.setQtQueryStatusType(queryStatusType);

		Date startDate = new Date(System.currentTimeMillis());
		resultInstance.setStartDate(startDate);
		savePatientSetResult = new SavePatientSetResult(getDataSource(),
				getDbSchemaName(), dataSourceLookup);
		savePatientSetResult.save(resultInstance);

		return resultInstance.getResultInstanceId();
	}

	/**
	 * Function used to update result instance Particularly its status and size
	 * 
	 * @param resultInstanceId
	 * @param statusTypeId
	 * @param setSize
	 */
	public void updatePatientSet(String resultInstanceId, int statusTypeId,
			int setSize) {
		updatePatientSet(resultInstanceId, statusTypeId, "", setSize, 0, "");
	}

	/**
	 * Function used to update result instance Particularly its status and size
	 * 
	 * @param resultInstanceId
	 * @param statusTypeId
	 * @param setSize
	 */
	public void updatePatientSet(String resultInstanceId, int statusTypeId,
			String message, int setSize, int realSetSize, String obsMethod) {

		Date endDate = new Date(System.currentTimeMillis());
		String sql = "update "
				+ getDbSchemaName()
				+ "qt_query_result_instance set set_size = ?, real_set_size = ? , obfusc_method = ?, status_type_id =?, end_date = ?, message = ?  where result_instance_id = ?";
		jdbcTemplate.update(sql, new Object[] { setSize, realSetSize,
				obsMethod, statusTypeId, endDate, message, resultInstanceId },
				new int[] { Types.INTEGER, Types.INTEGER, Types.VARCHAR,
				Types.INTEGER, Types.TIMESTAMP, Types.VARCHAR,
				Types.INTEGER });
	}

	/**
	 * Function used to update result instance description
	 * 
	 * @param resultInstanceId
	 * @param description
	 */
	public void updateResultInstanceDescription(String resultInstanceId,
			String description) {
		String sql = "update "
				+ getDbSchemaName()
				+ "qt_query_result_instance set description = ?  where result_instance_id = ?";
		jdbcTemplate.update(sql,
				new Object[] { description, Integer.parseInt(resultInstanceId) }, new int[] {
				Types.VARCHAR, Types.INTEGER });
	}

	/**
	 * Return list of query result instance by query instance id
	 * 
	 * @param queryInstanceId
	 * @return List<QtQueryResultInstance>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryResultInstance> getResultInstanceList(
			String queryInstanceId) {
		String sql = "select *  from " + getDbSchemaName()
				+ "qt_query_result_instance where query_instance_id = ? ";
		List<QtQueryResultInstance> queryResultInstanceList = jdbcTemplate
				.query(sql, new Object[] { Integer.parseInt(queryInstanceId) }, patientSetMapper);
		return queryResultInstanceList;
	}

	/**
	 * Return list of query result instance by query result id
	 * 
	 * @param queryResultId
	 * @return QtQueryResultInstance
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultInstance getResultInstanceById(String queryResultId)
			throws I2B2DAOException {
		String sql = "select *  from " + getDbSchemaName()
				+ "qt_query_result_instance where result_instance_id = ? ";
		List<QtQueryResultInstance> queryResultInstanceList = jdbcTemplate
				.query(sql, new Object[] { Integer.parseInt(queryResultId) }, patientSetMapper);
		if (queryResultInstanceList.size() > 0) {
			return queryResultInstanceList.get(0);
		} else {
			throw new I2B2DAOException("Query result id " + queryResultId
					+ " not found");
		}

	}

	/**
	 * Return list of query result instance by query instance id and result name
	 * 
	 * @param queryInstanceId
	 * @param resultName
	 * @return QtQueryResultInstance
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultInstance getResultInstanceByQueryInstanceIdAndName(
			String queryInstanceId, String resultName) {
		String sql = "select *  from "
				+ getDbSchemaName()
				+ "qt_query_result_instance ri, "
				+ getDbSchemaName()
				+ "qt_query_result_type rt where ri.query_instance_id = ? and ri.result_type_id = rt.result_type_id and rt.name=?";
		QtQueryResultInstance queryResultInstanceList = (QtQueryResultInstance) jdbcTemplate
				.queryForObject(sql,
						new Object[] { Integer.parseInt(queryInstanceId), resultName },
						patientSetMapper);
		return queryResultInstanceList;
	}

	/**
	 * Return a list of query result instance with waiting status
	 * 
	 * @param queueName
	 * @param maxListSize
	 * @return
	 */
	public List<QtQueryResultInstance> getUnfinishedInstanceByQueue(
			String queueName, int maxListSize) {
		List<QtQueryResultInstance> resultInstanceList = null;
		int waitStatus = 1;
		String sql = "select * from "
				+ getDbSchemaName()
				+ "qt_query_result_instance ri, "
				+ getDbSchemaName()
				+ "qt_query_result_type rt where status_type_id = ? and queue_name = ? and ri.result_type_id = rt.result_type_id order by start_date";
		resultInstanceList = jdbcTemplate.query(sql, new Object[] { waitStatus,
				queueName }, patientSetMapper);
		return resultInstanceList;
	}

	/**
	 * Get result instance count by set size
	 * 
	 * @param userId
	 * @param compareDays
	 * @param setSize
	 * @param totalCount
	 * @return
	 * @throws I2B2DAOException
	 */
	public int getResultInstanceCountBySetSize(String userId, int compareDays,
			int resultTypeId, int setSize, int totalCount)
					throws I2B2DAOException {
		// int betweenDayValue = compareDays / 2;
		int startBetweenDayValue = compareDays * -1;
		int returnSetSize = 0;
		String queryCountSql = "";

		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) ) {
			queryCountSql = " select count(r1.result_instance_id) result_count,r1.real_set_size "
					+ " from " + this.getDbSchemaName() + "qt_query_result_instance r1 inner join " + this.getDbSchemaName()+ "qt_query_result_instance r2 on "
					+ " r1.real_set_size = r2.real_set_size, "
					+ this.getDbSchemaName() +"qt_query_instance qi "
					+ " where "
					+ "  r1.start_date between sysdate- "
					+ compareDays
					+ " and sysdate   "
					+ " and r2.start_date between sysdate- "
					+ compareDays
					+ "  and sysdate "
					+ " and r1.result_type_id = ?"
					+ " and r2.result_type_id = ? "
					+ " and  qi.user_id = ? "
					+ " and qi.query_instance_id = r1.query_instance_id "
					+ " and qi.query_instance_id = r2.query_instance_id "
					+ " and r1.real_set_size = ? "
					+ " group by r1.real_set_size "
					+ " having count(r1.result_instance_id) > ? ";
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL) ) {
			queryCountSql = " select count(r1.result_instance_id) result_count,r1.real_set_size "
					+ " from " + this.getDbSchemaName() + "qt_query_result_instance r1 inner join " + this.getDbSchemaName()+ "qt_query_result_instance r2 on "
					+ " r1.real_set_size = r2.real_set_size, "
					+ this.getDbSchemaName() +"qt_query_instance qi "
					+ " where "
					+ "  r1.start_date between LOCALTIMESTAMP - INTERVAL '"
					+ compareDays
					+ " days' and LOCALTIMESTAMP "
					+ " and r2.start_date between LOCALTIMESTAMP - INTERVAL '"
					+ compareDays
					+ " days' and LOCALTIMESTAMP "
					+ " and r1.result_type_id = ?"
					+ " and r2.result_type_id = ? "
					+ " and  qi.user_id = ? "
					+ " and qi.query_instance_id = r1.query_instance_id "
					+ " and qi.query_instance_id = r2.query_instance_id "
					+ " and r1.real_set_size = ? "
					+ " group by r1.real_set_size "
					+ " having count(r1.result_instance_id) > ? ";
		} 	else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			queryCountSql = " select count(r1.result_instance_id) result_count,r1.real_set_size "
					+ " from " + this.getDbSchemaName() + "qt_query_result_instance r1 inner join " + this.getDbSchemaName() + "qt_query_result_instance r2 on "
					+ " r1.real_set_size = r2.real_set_size, "
					+ this.getDbSchemaName() +"qt_query_instance qi "
					+ " where "
					+ " r1.start_date between DATEADD ( day , "
					+ startBetweenDayValue
					+ ", getDate())  and DATEADD ( day , "
					+ "1"
					+ ", getDate()) "
					+ " and r2.start_date between DATEADD ( day , "
					+ startBetweenDayValue
					+ ", getDate()) and DATEADD ( day , "
					+ "1"
					+ ", getDate()) "
					+ " and r1.result_type_id = ? "
					+ " and r2.result_type_id = ? "
					+ " and  qi.user_id = ? "
					+ " and qi.query_instance_id = r1.query_instance_id "
					+ " and qi.query_instance_id = r2.query_instance_id "
					+ " and r1.real_set_size = ? "
					+ " group by r1.real_set_size "
					+ " having count(r1.result_instance_id) > ? ";
		}

		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			conn = dataSource.getConnection();

			log.debug("Executing sql [" + queryCountSql + "]");
			preparedStmt = conn.prepareStatement(queryCountSql);
			preparedStmt.setInt(1, resultTypeId);
			preparedStmt.setInt(2, resultTypeId);
			preparedStmt.setString(3, userId);
			preparedStmt.setInt(4, setSize);
			preparedStmt.setInt(5, totalCount);

			ResultSet resultSet = preparedStmt.executeQuery();
			if (resultSet.next()) {
				returnSetSize = resultSet.getInt("result_count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new I2B2DAOException(
					"Error while calculating query count by set size"
							+ StackTraceUtil.getStackTrace(e));
		} finally {
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException e) {

				e.printStackTrace();
			}

		}
		return returnSetSize;

	}

	private static class SavePatientSetResult extends SqlUpdate {


		private String INSERT_ORACLE = "";
		private String INSERT_SQLSERVER = "";
		private String SEQUENCE_ORACLE = "";
		private String SEQUENCE_POSTGRESQL = "";
		private String INSERT_POSTGRESQL = "";
		DataSourceLookup dataSourceLookup = null;

		public SavePatientSetResult(DataSource dataSource, String dbSchemaName,
				DataSourceLookup dataSourceLookup) {
			super();
			setDataSource(dataSource);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				INSERT_ORACLE = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_RESULT_INSTANCE "
						+ "(RESULT_INSTANCE_ID, QUERY_INSTANCE_ID, RESULT_TYPE_ID, SET_SIZE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);
				SEQUENCE_ORACLE = "select " + dbSchemaName
						+ "QT_SQ_QRI_QRIID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				INSERT_SQLSERVER = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_RESULT_INSTANCE "
						+ "( QUERY_INSTANCE_ID, RESULT_TYPE_ID, SET_SIZE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?)";
				setSql(INSERT_SQLSERVER);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				INSERT_POSTGRESQL = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_RESULT_INSTANCE "
						+ "(RESULT_INSTANCE_ID, QUERY_INSTANCE_ID, RESULT_TYPE_ID, SET_SIZE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?)";
				setSql(INSERT_POSTGRESQL);
				SEQUENCE_POSTGRESQL = "select " //+ dbSchemaName
						+ "nextval('qt_query_result_instance_result_instance_id_seq') ";
				declareParameter(new SqlParameter(Types.INTEGER));


			}

			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			this.dataSourceLookup = dataSourceLookup;

			compile();
		}

		public void save(QtQueryResultInstance resultInstance) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int resultInstanceId = 0;
			Object[] object = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {

				object = new Object[] {
						resultInstance.getQtQueryInstance()
						.getQueryInstanceId(),

						resultInstance.getQtQueryResultType().getResultTypeId(),
						resultInstance.getSetSize(),
						resultInstance.getStartDate(),
						resultInstance.getEndDate(),
						resultInstance.getQtQueryStatusType().getStatusTypeId(),
						resultInstance.getDeleteFlag()

				};
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				resultInstanceId = jdbc.queryForInt(SEQUENCE_ORACLE);
				resultInstance.setResultInstanceId(String
						.valueOf(resultInstanceId));
				object = new Object[] {
						resultInstance.getResultInstanceId(),
						resultInstance.getQtQueryInstance()
						.getQueryInstanceId(),
						resultInstance.getQtQueryResultType().getResultTypeId(),
						resultInstance.getSetSize(),
						resultInstance.getStartDate(),
						resultInstance.getEndDate(),
						resultInstance.getQtQueryStatusType().getStatusTypeId(),
						resultInstance.getDeleteFlag()

				};
			} else  if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				resultInstanceId = jdbc.queryForInt(SEQUENCE_POSTGRESQL);
				resultInstance.setResultInstanceId(String
						.valueOf(resultInstanceId));
				object = new Object[] {
						resultInstance.getResultInstanceId(),
						resultInstance.getQtQueryInstance()
						.getQueryInstanceId(),
						resultInstance.getQtQueryResultType().getResultTypeId(),
						resultInstance.getSetSize(),
						resultInstance.getStartDate(),
						resultInstance.getEndDate(),
						resultInstance.getQtQueryStatusType().getStatusTypeId(),
						resultInstance.getDeleteFlag()

				};
			}

			update(object);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				int resultInstanceIdentityId = jdbc
						.queryForInt("SELECT @@IDENTITY");

				resultInstance.setResultInstanceId(String
						.valueOf(resultInstanceIdentityId));

			}

		}
	}

	private class PatientSetResultRowMapper implements RowMapper {
		QueryStatusTypeSpringDao statusTypeDao = new QueryStatusTypeSpringDao(
				dataSource, dataSourceLookup);
		QueryResultTypeSpringDao resultTypeDao = new QueryResultTypeSpringDao(
				dataSource, dataSourceLookup);

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryResultInstance resultInstance = new QtQueryResultInstance();
			resultInstance.setResultInstanceId(rs
					.getString("RESULT_INSTANCE_ID"));

			QtQueryInstance queryInstance = new QtQueryInstance();
			queryInstance.setQueryInstanceId(rs.getString("QUERY_INSTANCE_ID"));
			resultInstance.setQtQueryInstance(queryInstance);
			resultInstance.setDescription(rs.getString("DESCRIPTION"));

			int resultTypeId = rs.getInt("RESULT_TYPE_ID");
			resultInstance.setQtQueryResultType(resultTypeDao
					.getQueryResultTypeById(resultTypeId));
			resultInstance.setSetSize(rs.getInt("SET_SIZE"));
			resultInstance.setRealSetSize(rs.getInt("REAL_SET_SIZE"));
			resultInstance.setObfuscateMethod(rs.getString("OBFUSC_METHOD"));
			resultInstance.setStartDate(rs.getTimestamp("START_DATE"));
			resultInstance.setEndDate(rs.getTimestamp("END_DATE"));
			resultInstance.setMessage(rs.getString("MESSAGE"));
			// QtQueryStatusType queryStatusType = new QtQueryStatusType();
			int statusTypeId = rs.getInt("STATUS_TYPE_ID");
			resultInstance.setQtQueryStatusType(statusTypeDao
					.getQueryStatusTypeById(statusTypeId));
			// resultInstance.setQtQueryStatusType(queryStatusType);
			resultInstance.setDeleteFlag(rs.getString("DELETE_FLAG"));
			return resultInstance;
		}
	}
}
