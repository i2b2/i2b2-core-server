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
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierType;

/**
 * Patient Dimension data access object.
 * 
 * @author rk903
 */
public class ModifierDAO extends CRCLoaderDAO implements IModifierDAO {

	private int DB_BATCH_INSERT_SIZE = 2000;
	private DataSourceLookup dataSourceLookup = null;

	private static Log log = LogFactory.getLog(ModifierDAO.class);

	public ModifierDAO(DataSourceLookup dataSourceLookup, DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Function to create temp modifier dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempPatientMappingTableName)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "CREATE_TEMP_MODIFIER_TABLE(?,?)}");
			callStmt.setString(1, tempPatientMappingTableName);
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
	public TempModifierInsertHandler createTempModifierInsert(String tempTableName) {
		TempModifierInsert tempModifierInsert = new TempModifierInsert(
				getDataSource(), tempTableName, getDbSchemaName());
		tempModifierInsert.setBatchSize(DB_BATCH_INSERT_SIZE);
		return new TempModifierInsertHandler(tempModifierInsert);
	}

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createModifierFromTempTable(String tempMapTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "INSERT_MODIFIER_FROMTEMP(?,?,?)}");
			callStmt.setString(1, tempMapTableName);
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

	public int getRecordCountByUploadId(int uploadId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		int insertCount = jdbcTemplate.queryForInt("select count(1) from "
				+ this.getDbSchemaName()
				+ "modifier_dimension where upload_id =?",
				new Object[] { uploadId });
		return insertCount;
	}

	/**
	 * Function to backup and clear concept dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void backupAndSyncModifierDimensionTable(String tempModifierTableName,
			String backupModifierDimensionTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "SYNC_CLEAR_MODIFIER_TABLE(?,?,?,?)}");
			callStmt.setString(1, tempModifierTableName);
			callStmt.setString(2, backupModifierDimensionTableName);
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
	protected class TempModifierInsert extends BatchSqlUpdate {

		/**
		 * Create a new instance of Patient_MappingInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected TempModifierInsert(DataSource ds, String tempTableName,
				String dbSchemaName) {

			super(ds, "INSERT INTO " + dbSchemaName + tempTableName + "  ("
					+ "MODIFIER_CD," + "MODIFIER_PATH," + "NAME_CHAR,"
					+ "MODIFIER_BLOB," + "UPDATE_DATE," + "DOWNLOAD_DATE,"
					+ "IMPORT_DATE," + "SOURCESYSTEM_CD" + ")"
					+ " VALUES(?,?,?,?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected void insert(ModifierType modifier) {

			// do self insert
			Object[] objs = new Object[] {
					modifier.getModifierCd(),
					modifier.getModifierPath(),
					modifier.getNameChar(),
					(modifier.getModifierBlob() != null) ? modifier
							.getModifierBlob().getContent().get(0) : null,
					(modifier.getUpdateDate() != null) ? modifier.getUpdateDate()
							.toGregorianCalendar().getTime() : null,
					(modifier.getDownloadDate() != null) ? modifier
							.getDownloadDate().toGregorianCalendar().getTime()
							: null,
					(modifier.getImportDate() != null) ? modifier.getImportDate()
							.toGregorianCalendar().getTime() : null,
					modifier.getSourcesystemCd() };
			super.update(objs);
		}

	}

}
