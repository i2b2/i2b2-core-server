package edu.harvard.i2b2.crc.loader.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;

/**
 * Encounter mapping and Visit data access object.
 * 
 * @author rk903
 */
public class VisitDAO extends CRCLoaderDAO implements IVisitDAO {

	private int DB_BATCH_INSERT_SIZE = 2000;

	private static Log log = LogFactory.getLog(VisitDAO.class);
	private DataSourceLookup dataSourceLookup = null;

	public VisitDAO(DataSourceLookup dataSourceLookup, DataSource ds) {
		setDataSource(ds);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	public int getRecordCountByUploadId(int uploadId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.getDataSource());
		int insertCount = jdbcTemplate.queryForInt(
				"select count(1) from " + this.getDbSchemaName()
						+ "visit_dimension where upload_id =?",
				new Object[] { uploadId });
		return insertCount;
	}

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempVisitDimensionInsertHandler createTempVisitDimensionInsert(
			String tempTableName) {
		TempEncounterVisitInsert tempEncounterVisitInsert = new TempEncounterVisitInsert(
				getDataSource(), tempTableName, this.getDbSchemaName());
		tempEncounterVisitInsert.setBatchSize(DB_BATCH_INSERT_SIZE);
		return new TempVisitDimensionInsertHandler(tempEncounterVisitInsert);
	}

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempTableName) throws I2B2Exception {
		Connection conn = null;
		try {
			conn = this.getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName() + "CREATE_TEMP_VISIT_TABLE(?,?)}");
			callStmt.setString(1, tempTableName);
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
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createVisitFromTempTable(String tempTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = this.getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "INSERT_ENCOUNTERVISIT_FROMTEMP(?,?,?)}");
			callStmt.setString(1, tempTableName);
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

	// ************* Operation Objects section ***************

	/**
	 * <code>TempEncounterVisitInsert</code> Insert Object.
	 */
	protected class TempEncounterVisitInsert extends BatchSqlUpdate {
		/**
		 * Create a new instance of TempEncounterVisitInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected TempEncounterVisitInsert(DataSource ds, String tempTableName,
				String schemaName) {
			super(ds, "INSERT INTO " + schemaName + tempTableName + " ("
					+ "encounter_id," + "encounter_id_source," + "patient_id,"
					+ "patient_id_source," + "inout_cd," + "location_cd,"
					+ "location_path," + "start_date," + "end_date,"
					+ "visit_blob," + "update_date," + "download_date,"
					+ "import_date," + "sourcesystem_cd)"
					+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.LONGVARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected void insert(EventType event) {
			Map<String, ParamType> paramMap = null;
			if (event.getParam() != null) {
				paramMap = buildNVParam(event.getParam());
			} else {
				paramMap = new HashMap<String, ParamType>();
			}
			Object[] objs = new Object[] {
			// encounterVisitMap.get("encounter_ide"),
					event.getEventId().getValue(),
					// encounterVisitMap.get("encounter_ide_source"),
					event.getEventId().getSource(),
					// encounterVisitMap.get("patient_ide"),
					(event.getPatientId() != null) ? event.getPatientId()
							.getValue() : null,
					// encounterVisitMap.get("patient_ide_source"),
					(event.getPatientId() != null) ? event.getPatientId()
							.getSource() : null,
					// encounterVisitMap.get("inout_cd"),
					(paramMap.get("inout_cd") != null) ? paramMap.get(
							"inout_cd").getValue() : null,
					// encounterVisitMap.get("location_cd"),
					(paramMap.get("location_cd") != null) ? paramMap.get(
							"location_cd").getValue() : null,
					// encounterVisitMap.get("location_path"),
					(paramMap.get("location_path") != null) ? paramMap.get(
							"location_path").getValue() : null,
					// encounterVisitMap.get("start_date"),
					(event.getStartDate() != null) ? event.getStartDate()
							.toGregorianCalendar().getTime() : null,
					// encounterVisitMap.get("end_date"),
					(event.getEndDate() != null) ? event.getEndDate()
							.toGregorianCalendar().getTime() : null,
					// encounterVisitMap.get("visit_blob"),
					(event.getEventBlob() != null) ? event.getEventBlob()
							.getContent().get(0).toString() : null,
					// encounterVisitMap.get("update_date"),
					(event.getUpdateDate() != null) ? event.getUpdateDate()
							.toGregorianCalendar().getTime() : null,
					// encounterVisitMap.get("download_date"),
					(event.getDownloadDate() != null) ? event.getDownloadDate()
							.toGregorianCalendar().getTime() : null,
					// encounterVisitMap.get("import_date"),
					(event.getImportDate() != null) ? event.getImportDate()
							.toGregorianCalendar().getTime() : null,
					// encounterVisitMap.get("source_system_cd")
					event.getSourcesystemCd() };
			super.update(objs);

		}
	}

}
