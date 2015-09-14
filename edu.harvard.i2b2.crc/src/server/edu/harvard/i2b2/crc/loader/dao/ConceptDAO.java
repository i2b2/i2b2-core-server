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

/**
 * Patient Dimension data access object.
 * 
 * @author rk903
 */
public class ConceptDAO extends CRCLoaderDAO implements IConceptDAO {

	private int DB_BATCH_INSERT_SIZE = 2000;
	private DataSourceLookup dataSourceLookup = null;

	private static Log log = LogFactory.getLog(ConceptDAO.class);

	public ConceptDAO(DataSourceLookup dataSourceLookup, DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Function to create temp visit dimension table using stored proc.
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
					+ "CREATE_TEMP_CONCEPT_TABLE(?,?)}");
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
	public TempConceptInsertHandler createTempConceptInsert(String tempTableName) {
		TempConceptInsert tempConceptInsert = new TempConceptInsert(
				getDataSource(), tempTableName, getDbSchemaName());
		tempConceptInsert.setBatchSize(DB_BATCH_INSERT_SIZE);
		return new TempConceptInsertHandler(tempConceptInsert);
	}

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createConceptFromTempTable(String tempMapTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "INSERT_CONCEPT_FROMTEMP(?,?,?)}");
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
				+ "concept_dimension where upload_id =?",
				new Object[] { uploadId });
		return insertCount;
	}

	/**
	 * Function to backup and clear concept dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void backupAndSyncConceptDimensionTable(String tempConceptTableName,
			String backupConceptDimensionTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "SYNC_CLEAR_CONCEPT_TABLE(?,?,?,?)}");
			callStmt.setString(1, tempConceptTableName);
			callStmt.setString(2, backupConceptDimensionTableName);
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
	protected class TempConceptInsert extends BatchSqlUpdate {

		/**
		 * Create a new instance of Patient_MappingInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected TempConceptInsert(DataSource ds, String tempTableName,
				String dbSchemaName) {

			super(ds, "INSERT INTO " + dbSchemaName + tempTableName + "  ("
					+ "CONCEPT_CD," + "CONCEPT_PATH," + "NAME_CHAR,"
					+ "CONCEPT_BLOB," + "UPDATE_DATE," + "DOWNLOAD_DATE,"
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

		protected void insert(ConceptType concept) {

			// do self insert
			Object[] objs = new Object[] {
					concept.getConceptCd(),
					concept.getConceptPath(),
					concept.getNameChar(),
					(concept.getConceptBlob() != null) ? concept
							.getConceptBlob().getContent().get(0) : null,
					(concept.getUpdateDate() != null) ? concept.getUpdateDate()
							.toGregorianCalendar().getTime() : null,
					(concept.getDownloadDate() != null) ? concept
							.getDownloadDate().toGregorianCalendar().getTime()
							: null,
					(concept.getImportDate() != null) ? concept.getImportDate()
							.toGregorianCalendar().getTime() : null,
					concept.getSourcesystemCd() };
			super.update(objs);
		}

	}

}
