package edu.harvard.i2b2.crc.loader.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;

/**
 * Patient Dimension data access object.
 * 
 * @author rk903
 */
public class ProviderDAO extends CRCLoaderDAO implements IProviderDAO {

	private int DB_BATCH_INSERT_SIZE = 2000;

	private static Log log = LogFactory.getLog(ProviderDAO.class);
	private DataSourceLookup dataSourceLookup = null;

	public ProviderDAO(DataSourceLookup dataSourceLookup, DataSource ds) {
		setDataSource(ds);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	public int getRecordCountByUploadId(int uploadId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		int insertCount = jdbcTemplate.queryForInt("select count(1) from "
				+ this.getDbSchemaName()
				+ "provider_dimension where upload_id =?",
				new Object[] { uploadId });
		return insertCount;
	}

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempProviderTableName)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = this.getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "CREATE_TEMP_PROVIDER_TABLE(?,?)}");
			callStmt.setString(1, tempProviderTableName);
			callStmt.registerOutParameter(2, java.sql.Types.VARCHAR);
			callStmt.execute();
			this.getSQLServerProcedureError(dataSourceLookup.getServerType(),
					callStmt, 2);
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
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempProviderInsertHandler createTempProviderInsert(
			String tempTableName) {
		TempProviderInsert tempProviderInsert = new TempProviderInsert(
				getDataSource(), tempTableName, this.getDbSchemaName());
		tempProviderInsert.setBatchSize(DB_BATCH_INSERT_SIZE);
		return new TempProviderInsertHandler(tempProviderInsert);
	}

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createProviderFromTempTable(String tempProviderTableName,
			int uploadId) throws I2B2Exception {
		Connection conn = null;
		try {
			conn = this.getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "INSERT_PROVIDER_FROMTEMP(?,?,?)}");
			callStmt.setString(1, tempProviderTableName);
			callStmt.setInt(2, uploadId);
			callStmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			callStmt.execute();
			this.getSQLServerProcedureError(dataSourceLookup.getServerType(),
					callStmt, 3);
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
	 * Function to backup and clear provider dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void backupAndSyncProviderDimensionTable(
			String tempConceptTableName,
			String backupProviderDimensionTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "SYNC_CLEAR_PROVIDER_TABLE(?,?,?,?)}");
			callStmt.setString(1, tempConceptTableName);
			callStmt.setString(2, backupProviderDimensionTableName);
			callStmt.setInt(3, uploadId);
			callStmt.registerOutParameter(4, java.sql.Types.VARCHAR);
			callStmt.execute();
			this.getSQLServerProcedureError(dataSourceLookup.getServerType(),
					callStmt, 4);
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
	 * Patient_Mapping insert code.
	 */
	protected class TempProviderInsert extends BatchSqlUpdate {

		/**
		 * Create a new instance of Patient_MappingInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected TempProviderInsert(DataSource ds, String tempTableName,
				String schemaName) {

			super(ds, "INSERT INTO " + schemaName + tempTableName + "  ("
					+ "PROVIDER_ID," + "PROVIDER_PATH," + "NAME_CHAR,"
					+ "PROVIDER_BLOB," + "UPDATE_DATE," + "DOWNLOAD_DATE,"
					+ "IMPORT_DATE," + "SOURCESYSTEM_CD," + "UPLOAD_ID" + ")"
					+ " VALUES(?,?,?,?,?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		protected void insert(ObserverType provider) {

			// do self insert
			Object[] objs = new Object[] {
					provider.getObserverCd(),
					provider.getObserverPath(),
					provider.getNameChar(),
					(provider.getObserverBlob() != null) ? provider
							.getObserverBlob().getContent().get(0) : null,
					(provider.getUpdateDate() != null) ? provider
							.getUpdateDate().toGregorianCalendar().getTime()
							: null,
					(provider.getDownloadDate() != null) ? provider
							.getDownloadDate().toGregorianCalendar().getTime()
							: null,
					(provider.getImportDate() != null) ? provider
							.getImportDate().toGregorianCalendar().getTime()
							: null, provider.getSourcesystemCd(),
					provider.getUploadId() };
			super.update(objs);
		}

	}

}
