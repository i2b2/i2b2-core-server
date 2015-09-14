package edu.harvard.i2b2.crc.loader.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;

/**
 * Upload Status data access object.
 * 
 * @author rk903
 */
public class UploadStatusDAO extends CRCLoaderDAO implements UploadStatusDAOI {

	private static Log log = LogFactory.getLog(UploadStatusDAO.class);

	public static final int EVENT_SET = 1;
	public static final int PATIENT_SET = 2;
	public static final int CONCEPT_SET = 3;
	public static final int OBSERVER_SET = 4;
	public static final int OBSERVATION_SET = 5;
	public static final int PID_SET = 6;
	public static final int EID_SET = 7;
	public static final int MODIFIER_SET = 8;

	private DataSourceLookup dataSourceLookup = null;
	private JdbcTemplate jdbcTemplate = null;

	public UploadStatusDAO(DataSourceLookup dataSourceLookup, DataSource ds) {
		setDataSource(ds);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		jdbcTemplate = new JdbcTemplate(
				getDataSource());
		// jdbcTemplate = new JdbcTemplate(dsTm.getDataSource());
	}

	/**
	 * Fetch Upload Status info.
	 * 
	 * @param uploadStatusId
	 * @return
	 * @throws UniqueKeyException
	 */
	public UploadStatus findById(int uploadStatusId) throws UniqueKeyException {
		UploadStatusQuery uploadStatusQuery = new UploadStatusQuery(
				getDataSource(), this.getDbSchemaName(), dataSourceLookup);

		java.util.List uploadList = uploadStatusQuery
				.execute(new Object[] { uploadStatusId });
		UploadStatus uploadStatus = null;
		if (uploadList != null) {
			if (uploadList.size() > 1) {
				throw new UniqueKeyException(
						"More than one upload for upload id " + uploadStatusId);
			} else {
				if (uploadList.size() > 0) {
					uploadStatus = (UploadStatus) uploadList.get(0);
				}
			}
		}
		return uploadStatus;
	}

	public void dropTempTable(String tempTable) {
		final String sql = "{call " + getDbSchemaName()
				+ "REMOVE_TEMP_TABLE(?)}";
		jdbcTemplate.update(sql, new Object[] { tempTable });
	}

	/**
	 * Insert UploadStatus
	 * 
	 * @param uploadStatus
	 * @return
	 */
	public int insertUploadStatus(UploadStatus uploadStatus) {
		UploadStatusInsert uploadStatusInsert = new UploadStatusInsert(
				getDataSource(), this.getDbSchemaName(), dataSourceLookup);
		// if (dataSourceLookup.getServerType().equalsIgnoreCase(
		// LoaderDAOFactoryHelper.ORACLE)) {
		//uploadStatus.setUploadId(uploadStatusInsert.generateUploadStatusId());
		// }
		uploadStatusInsert.insert(uploadStatus);
		// uploadStatusInsert.flush();
		return uploadStatus.getUploadId();
	}

	/**
	 * Insert SetUploadStatus
	 * 
	 * @param uploadStatus
	 * @return
	 */
	public void insertUploadSetStatus(final UploadSetStatus uploadSetStatus) {
		String insertSql = "insert into " + this.getDbSchemaName()
				+ "SET_UPLOAD_STATUS(UPLOAD_ID, " + "SET_TYPE_ID,"
				+ "SOURCE_CD, " + "NO_OF_RECORD," + "LOADED_RECORD,"
				+ "DELETED_RECORD," + "LOAD_DATE," + "END_DATE,"
				+ "LOAD_STATUS," + "MESSAGE," + "INPUT_FILE_NAME,"
				+ "LOG_FILE_NAME,"
				+ "TRANSFORM_NAME) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

		/*
		 * jdbcTemplate.update(insertSql, new
		 * Object[]{uploadSetStatus.getUploadId(),
		 * uploadSetStatus.getSetTypeId(), uploadSetStatus.getSourceCd(),
		 * uploadSetStatus.getNoOfRecord(), uploadSetStatus.getLoadedRecord(),
		 * uploadSetStatus.getDeletedRecord(), uploadSetStatus.getLoadDate(),
		 * uploadSetStatus.getEndDate(), uploadSetStatus.getLoadStatus(),
		 * uploadSetStatus.getMessage(), uploadSetStatus.getInputFileName(),
		 * uploadSetStatus.getLogFileName(),
		 * uploadSetStatus.getTransformName()});
		 */

		jdbcTemplate.update(insertSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, uploadSetStatus.getUploadId());
				ps.setInt(2, uploadSetStatus.getSetTypeId());
				ps.setString(3, uploadSetStatus.getSourceCd());
				ps.setInt(4, uploadSetStatus.getNoOfRecord());
				ps.setInt(5, uploadSetStatus.getLoadedRecord());
				ps.setInt(6, uploadSetStatus.getDeletedRecord());
				ps.setTimestamp(7, new java.sql.Timestamp(uploadSetStatus
						.getLoadDate().getTime()));
				ps
						.setTimestamp(
								8,
								(uploadSetStatus.getEndDate() != null) ? new java.sql.Timestamp(
										uploadSetStatus.getEndDate().getTime())
										: null);
				ps.setString(9, uploadSetStatus.getLoadStatus());
				ps.setString(10, uploadSetStatus.getMessage());
				ps.setString(11, uploadSetStatus.getInputFileName());
				ps.setString(12, uploadSetStatus.getLogFileName());
				ps.setString(13, uploadSetStatus.getTransformName());
			}
		});

	}

	/**
	 * update SetUploadStatus
	 * 
	 * @param uploadSetStatus
	 * @return
	 */
	public void updateUploadSetStatus(final UploadSetStatus uploadSetStatus) {
		String updateSql = "update " + this.getDbSchemaName()
				+ "SET_UPLOAD_STATUS  " + "set NO_OF_RECORD = ?,"
				+ " LOADED_RECORD =?," + " DELETED_RECORD=?," + " LOAD_DATE=?,"
				+ " END_DATE=?," + " LOAD_STATUS=?," + " MESSAGE=?,"
				+ " INPUT_FILE_NAME=?," + " LOG_FILE_NAME=?,"
				+ " TRANSFORM_NAME=? where UPLOAD_ID =? and SET_TYPE_ID=?";

		/*
		 * jdbcTemplate.update(updateSql, new Object[]{
		 * uploadSetStatus.getNoOfRecord(), uploadSetStatus.getLoadedRecord(),
		 * uploadSetStatus.getDeletedRecord(), uploadSetStatus.getLoadDate(),
		 * uploadSetStatus.getEndDate(), uploadSetStatus.getLoadStatus(),
		 * uploadSetStatus.getMessage(), uploadSetStatus.getInputFileName(),
		 * uploadSetStatus.getLogFileName(), uploadSetStatus.getTransformName(),
		 * uploadSetStatus.getUploadId(), uploadSetStatus.getSetTypeId()});
		 */

		jdbcTemplate.update(updateSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, uploadSetStatus.getNoOfRecord());
				ps.setInt(2, uploadSetStatus.getLoadedRecord());
				ps.setInt(3, uploadSetStatus.getDeletedRecord());
				ps
						.setTimestamp(
								4,
								(uploadSetStatus.getLoadDate() != null) ? new java.sql.Timestamp(
										uploadSetStatus.getLoadDate().getTime())
										: null);
				ps
						.setTimestamp(
								5,
								(uploadSetStatus.getEndDate() != null) ? new java.sql.Timestamp(
										uploadSetStatus.getEndDate().getTime())
										: null);
				ps.setString(6, uploadSetStatus.getLoadStatus());
				ps.setString(7, uploadSetStatus.getMessage());
				ps.setString(8, uploadSetStatus.getInputFileName());
				ps.setString(9, uploadSetStatus.getLogFileName());
				ps.setString(10, uploadSetStatus.getTransformName());
				ps.setInt(11, uploadSetStatus.getUploadId());
				ps.setInt(12, uploadSetStatus.getSetTypeId());
			}
		});

	}

	/**
	 * 
	 */
	public UploadSetStatus getUploadSetStatus(int uploadId, int setId) {
		UploadSetStatus uploadSetStatus = (UploadSetStatus) jdbcTemplate
				.queryForObject(
						"select * from "
								+ this.getDbSchemaName()
								+ "SET_UPLOAD_STATUS where UPLOAD_ID=? and SET_TYPE_ID=?",
						new Object[] { uploadId, setId }, new RowMapper() {

							public Object mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								UploadSetStatus uploadSetStatus = new UploadSetStatus();
								uploadSetStatus.setUploadId(rs
										.getInt("UPLOAD_ID"));
								uploadSetStatus.setSetTypeId(rs
										.getInt("SET_TYPE_ID"));
								uploadSetStatus.setLoadDate(rs
										.getTimestamp("LOAD_DATE"));
								uploadSetStatus.setEndDate(rs
										.getTimestamp("END_DATE"));
								uploadSetStatus.setDeletedRecord(rs
										.getInt("DELETED_RECORD"));
								uploadSetStatus.setLoadedRecord(rs
										.getInt("LOADED_RECORD"));
								uploadSetStatus.setLoadStatus(rs
										.getString("LOAD_STATUS"));
								uploadSetStatus.setLogFileName(rs
										.getString("LOG_FILE_NAME"));
								uploadSetStatus.setMessage(rs
										.getString("MESSAGE"));
								uploadSetStatus.setNoOfRecord(rs
										.getInt("NO_OF_RECORD"));
								uploadSetStatus.setSourceCd(rs
										.getString("SOURCE_CD"));
								uploadSetStatus.setTransformName(rs
										.getString("TRANSFORM_NAME"));
								return uploadSetStatus;
							}
						});

		return uploadSetStatus;
	}

	/**
	 * Return load status of individual sets in patient data objects
	 * 
	 * @param uploadId
	 * @return
	 */
	public List<UploadSetStatus> getUploadSetStatusByLoadId(int uploadId) {
		List<UploadSetStatus> setUploadStatusList = new ArrayList<UploadSetStatus>();
		int rowCount = jdbcTemplate.queryForInt("select count(1) from "
				+ this.getDbSchemaName()
				+ "SET_UPLOAD_STATUS where UPLOAD_ID=?",
				new Object[] { uploadId });
		if (rowCount < 1) {
			return setUploadStatusList;
		}
		System.out.println("ROW COUNT " + rowCount);
		Connection conn = null;
		try {
			String sql = "select upload_id as upload_id, "
					+ " set_type_id as set_type_id,"
					+ " load_date,end_date ,deleted_record as deleted_record, "
					+ "loaded_record as loaded_record, "
					+ "load_status, log_file_name, message,no_of_record as no_of_record,"
					+ "source_cd, transform_name from "
					+ this.getDbSchemaName()
					+ "SET_UPLOAD_STATUS where UPLOAD_ID=?";

			conn = jdbcTemplate.getDataSource().getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, uploadId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				UploadSetStatus uploadSetStatus = new UploadSetStatus();
				uploadSetStatus.setUploadId(rs.getInt("UPLOAD_ID"));
				uploadSetStatus.setSetTypeId(rs.getInt("SET_TYPE_ID"));
				uploadSetStatus.setLoadDate(rs.getTimestamp("LOAD_DATE"));
				uploadSetStatus.setEndDate(rs.getTimestamp("END_DATE"));
				uploadSetStatus.setDeletedRecord(rs.getInt("DELETED_RECORD"));
				uploadSetStatus.setLoadedRecord(rs.getInt("LOADED_RECORD"));
				uploadSetStatus.setLoadStatus(rs.getString("LOAD_STATUS"));
				uploadSetStatus.setLogFileName(rs.getString("LOG_FILE_NAME"));
				uploadSetStatus.setMessage(rs.getString("MESSAGE"));
				uploadSetStatus.setNoOfRecord(rs.getInt("NO_OF_RECORD"));
				uploadSetStatus.setSourceCd(rs.getString("SOURCE_CD"));
				uploadSetStatus
						.setTransformName(rs.getString("TRANSFORM_NAME"));
				setUploadStatusList.add(uploadSetStatus);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/*
		 * SqlRowSet rs =
		 * jdbcTemplate.queryForRowSet("select upload_id as upload_id, " +
		 * " set_type_id as set_type_id," +
		 * " load_date,end_date ,deleted_record as deleted_record, " +
		 * "loaded_record as loaded_record, " +
		 * "load_status, log_file_name, message,no_of_record as no_of_record," +
		 * "source_cd, transform_name from "+ this.getDbSchemaName()
		 * +"SET_UPLOAD_STATUS where UPLOAD_ID=?", new Object[]{uploadId});
		 */
		return setUploadStatusList;

	}

	/**
	 * Update UploadStatus information based on upload_id.
	 */
	public void updateUploadStatus(UploadStatus uploadStatus) {
		UploadStatusUpdate uploadStatusUpdate = new UploadStatusUpdate(
				getDataSource(), this.getDbSchemaName());
		uploadStatusUpdate.update(uploadStatus);
		uploadStatusUpdate.flush();
	}

	public List getAllUploadStatus() {
		List uploadStatusList = null;
		UploadStatusQuery uploadStatusQuery = new UploadStatusQuery(
				getDataSource(), "select * from " + this.getDbSchemaName()
						+ "upload_status order by upload_id desc");
		uploadStatusList = uploadStatusQuery.execute();
		return uploadStatusList;
	}

	@SuppressWarnings("unchecked")
	public List<UploadStatus> getUpoadStatusByUser(String userId) {
		List<UploadStatus> uploadStatusList = null;
		UploadStatusQuery uploadStatusQuery = new UploadStatusQuery(
				getDataSource(),
				"select * from "
						+ this.getDbSchemaName()
						+ "upload_status where user_id = ? order by upload_id desc");
		uploadStatusQuery.declareParameter(new SqlParameter("user_id",
				Types.CHAR));
		uploadStatusList = (List<UploadStatus>) uploadStatusQuery
				.execute(userId);
		return uploadStatusList;
	}

	/**
	 * Calculate upload status information like records loaded,etc for given
	 * upload id using stored proc.
	 * 
	 * @param upload
	 *            id
	 * @throws Exception
	 */
	public void calculateUploadStatus(int uploadId) throws I2B2Exception {
		Connection conn = null;
		try {
			conn = this.getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName() + "CALCULATE_UPLOAD_STATUS(?)}");
			callStmt.setInt(1, uploadId);
			callStmt.execute();
		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			throw new I2B2Exception(
					"SQLException occured" + sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2Exception("Exception occured" + ex.getMessage(), ex);
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
	}

	/**
	 * Function to delete upload data(observation_fact) based on upload id using
	 * stored proc.
	 * 
	 * @param upload
	 *            id
	 * @throws Exception
	 */
	public void deleteUploadData(int uploadId) throws I2B2Exception {
		Connection conn = null;
		try {
			conn = this.getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName() + "DELETE_UPLOAD_DATA(?)}");
			callStmt.setInt(1, uploadId);
			callStmt.execute();
		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			throw new I2B2Exception(
					"SQLException occured" + sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2Exception("Exception occured" + ex.getMessage(), ex);
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
	}

	/**
	 * Base class for all <code>UploadStatus</code> Query Objects.
	 */
	protected class UploadStatusQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of UploadStatusQuery.
		 * 
		 * @param ds
		 *            the DataSource to use for the query
		 * @param sql
		 *            SQL string to use for the query
		 */
		protected UploadStatusQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		/**
		 * Create a new instance of UploadStatusQuery that returns all
		 * UploadStatus.
		 * 
		 * @param ds
		 *            the DataSource to use for the query
		 */
		protected UploadStatusQuery(DataSource ds, String schemaName,
				DataSourceLookup dataSourceLookup) {

			super(ds, "SELECT upload_id, " + " upload_label, " + " user_id, "
					+ " source_cd, " + " no_of_record, " + " deleted_record, "
					+ " loaded_record, " + " load_date, " + " end_date, "
					+ " load_status, " + " input_file_name, "
					+ " log_file_name, " + " transform_name ," + "message "
					+ " FROM  " + schemaName
					+ "upload_status WHERE upload_id = ? ");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			UploadStatus uploadStatus = new UploadStatus();
			uploadStatus.setUploadId(rs.getInt("upload_id"));
			uploadStatus.setUploadLabel(rs.getString("upload_label"));
			uploadStatus.setUserId(rs.getString("user_id"));
			uploadStatus.setSourceCd(rs.getString("source_cd"));
			uploadStatus.setNoOfRecord(rs.getInt("no_of_record"));
			uploadStatus.setDeletedRecord(rs.getInt("deleted_record"));
			uploadStatus.setLoadedRecord(rs.getInt("loaded_record"));
			uploadStatus.setLoadDate(rs.getTimestamp("load_date"));
			uploadStatus.setEndDate(rs.getTimestamp("end_date"));
			uploadStatus.setLoadStatus(rs.getString("load_status"));
			uploadStatus.setInputFileName(rs.getString("input_file_name"));
			uploadStatus.setLogFileName(rs.getString("log_file_name"));
			uploadStatus.setTransformName(rs.getString("transform_name"));
			uploadStatus.setMessage(rs.getString("message"));
			return uploadStatus;
		}
	}

	/**
	 * <code>Visit</code> Insert Object.
	 */
	protected class UploadStatusInsert extends SqlUpdate {
		/**
		 * Create a new instance of OwnerInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected UploadStatusInsert(DataSource ds, String schemaName,
				DataSourceLookup dataSourceLookup) {

			String sql = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					LoaderDAOFactoryHelper.SQLSERVER)) {
				sql = "INSERT INTO " + schemaName + "upload_status (" +

				" upload_label, " + " user_id, " + " source_cd, "
						+ " no_of_record, " + " deleted_record, "
						+ " loaded_record, " + " load_date, " + " end_date, "
						+ " load_status, " + " input_file_name, "
						+ " log_file_name, " + " message, "
						+ " transform_name) "
						+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					LoaderDAOFactoryHelper.ORACLE)) {
				sql = "INSERT INTO " + schemaName + "upload_status ("
						+ " upload_id," + " upload_label, " + " user_id, "
						+ " source_cd, " + " no_of_record, "
						+ " deleted_record, " + " loaded_record, "
						+ " load_date, " + " end_date, " + " load_status, "
						+ " input_file_name, " + " log_file_name, "
						+ " message, " + " transform_name) "
						+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			}
			this.setSql(sql);
			this.setDataSource(dataSource);

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					LoaderDAOFactoryHelper.ORACLE)) {
				declareParameter(new SqlParameter(Types.INTEGER));
			}
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected void insert(UploadStatus uploadStatus) {
			int uploadId = 0;
			Object[] objs = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					LoaderDAOFactoryHelper.ORACLE)) {
				uploadId = getJdbcTemplate().queryForInt(
						"select sq_uploadstatus_uploadid.nextval from dual");
				uploadStatus.setUploadId(uploadId);
				objs = new Object[] { uploadStatus.getUploadId(),
						uploadStatus.getUploadLabel(),
						uploadStatus.getUserId(), uploadStatus.getSourceCd(),
						uploadStatus.getNoOfRecord(),
						uploadStatus.getDeletedRecord(),
						uploadStatus.getLoadedRecord(),
						uploadStatus.getLoadDate(), uploadStatus.getEndDate(),
						uploadStatus.getLoadStatus(),
						uploadStatus.getInputFileName(),
						uploadStatus.getLogFileName(),
						uploadStatus.getMessage(),
						uploadStatus.getTransformName() };
				update(objs);

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					LoaderDAOFactoryHelper.SQLSERVER)) {
				objs = new Object[] { uploadStatus.getUploadLabel(),
						uploadStatus.getUserId(), uploadStatus.getSourceCd(),
						uploadStatus.getNoOfRecord(),
						uploadStatus.getDeletedRecord(),
						uploadStatus.getLoadedRecord(),
						uploadStatus.getLoadDate(), uploadStatus.getEndDate(),
						uploadStatus.getLoadStatus(),
						uploadStatus.getInputFileName(),
						uploadStatus.getLogFileName(),
						uploadStatus.getMessage(),
						uploadStatus.getTransformName() };
				update(objs);
				uploadId = getJdbcTemplate().queryForInt("SELECT @@IDENTITY");
			}

			uploadStatus.setUploadId(uploadId);
		}

		protected int generateUploadStatusId() {
			return getJdbcTemplate().queryForInt(
					"select sq_uploadstatus_uploadid.nextval from dual");
		}

	}

	/**
	 * <code>UploadStatus</code> Update Object.
	 */
	protected class UploadStatusUpdate extends BatchSqlUpdate {

		/**
		 * Create a new instance of UploadStatusUpdate.
		 * 
		 * @param ds
		 *            the DataSource to use for the update
		 */
		protected UploadStatusUpdate(DataSource ds, String schemaName) {
			super(ds, "UPDATE " + schemaName + "upload_status SET "
					+ "upload_label=?, " + "user_id=?, " + "source_cd=?, "
					+ "no_of_record=?, " + "deleted_record=?, "
					+ "loaded_record=?, " + "load_date=?, " + "end_date=?,"
					+ "load_status=?, " + "input_file_name=?, "
					+ "log_file_name=?, " + "transform_name=?, " + "message=? "
					+ "WHERE upload_id=?");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.LONGVARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		/**
		 * Method to update <code>UploadStatus</code>'s data.
		 * 
		 * @param UploadStatus
		 *            to update
		 * @return the number of rows affected by the update
		 */
		protected int update(UploadStatus uploadStatus) {
			return this.update(new Object[] { uploadStatus.getUploadLabel(),
					uploadStatus.getUserId(), uploadStatus.getSourceCd(),
					uploadStatus.getNoOfRecord(),
					uploadStatus.getDeletedRecord(),
					uploadStatus.getLoadedRecord(), uploadStatus.getLoadDate(),
					uploadStatus.getEndDate(), uploadStatus.getLoadStatus(),
					uploadStatus.getInputFileName(),
					uploadStatus.getLogFileName(),
					uploadStatus.getTransformName(), uploadStatus.getMessage(),
					uploadStatus.getUploadId()

			});

		}
	}

}
