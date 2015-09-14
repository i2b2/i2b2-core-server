package edu.harvard.i2b2.crc.loader.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.EidType;
import edu.harvard.i2b2.crc.datavo.pdo.EidType.EventMapId;

/**
 * Patient Dimension data access object.
 * 
 * @author rk903
 */
public class EidDAO extends CRCLoaderDAO implements IEidDAO {

	private int DB_BATCH_INSERT_SIZE = 2000;

	private static Log log = LogFactory.getLog(EidDAO.class);
	private DataSourceLookup dataSourceLookup = null;

	public EidDAO(DataSourceLookup dataSourceLookup, DataSource ds) {
		setDataSource(ds);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	public int getRecordCountByUploadId(int uploadId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		int insertCount = jdbcTemplate.queryForInt("select count(1) from "
				+ getDbSchemaName() + " encounter_mapping where upload_id =?",
				new Object[] { uploadId });
		return insertCount;
	}

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempEncounterMappingTableName)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ getDbSchemaName() + "CREATE_TEMP_EID_TABLE(?,?)}");
			callStmt.setString(1, tempEncounterMappingTableName);
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
	public TempEidInsertHandler createTempEidInsert(String tempTableName) {
		TempEidInsert tempEidInsert = new TempEidInsert(getDataSource(),
				tempTableName, this.getDbSchemaName());
		tempEidInsert.setBatchSize(DB_BATCH_INSERT_SIZE);
		return new TempEidInsertHandler(tempEidInsert);
	}

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createEidFromTempTable(String tempMapTableName, int uploadId)
			throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "INSERT_EID_MAP_FROMTEMP(?,?,?)}");
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

	/**
	 * Patient_Mapping insert code.
	 */
	protected class TempEidInsert extends BatchSqlUpdate {

		/**
		 * Create a new instance of Patient_MappingInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected TempEidInsert(DataSource ds, String tempTableName,
				String schemaName) {

			super(
					ds,
					"INSERT INTO "
							+ schemaName
							+ tempTableName
							+ "  ("
							+ "encounter_map_id, encounter_map_id_source, patient_map_id,"
							+ "patient_map_id_source, encounter_id, "
							+ "encounter_id_source, "
							+ "ENCOUNTER_MAP_ID_STATUS ," + " UPDATE_DATE, "
							+ "DOWNLOAD_DATE," + " SOURCESYSTEM_CD )"
							+ " VALUES(?,?,?,?,?,?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected void insert(EidType eid) {
			List<EventMapId> mapIdList = eid.getEventMapId();
			String encounterId = eid.getEventId().getValue();
			String encounterIdSource = eid.getEventId().getSource();
			String encounterPatientId = eid.getEventId().getPatientId();
			String encounterPatientIdSource = eid.getEventId()
					.getPatientIdSource();

			for (EventMapId mapId : mapIdList) {

				Object[] objs = new Object[] {
						mapId.getValue(),
						mapId.getSource(),
						mapId.getPatientId(),
						mapId.getPatientIdSource(),
						encounterId,
						encounterIdSource,
						mapId.getStatus(),
						(mapId.getUpdateDate() != null) ? mapId.getUpdateDate()
								.toGregorianCalendar().getTime() : null,
						(mapId.getDownloadDate() != null) ? mapId
								.getDownloadDate().toGregorianCalendar()
								.getTime() : null, mapId.getSourcesystemCd() };
				super.update(objs);
			}
			// do self insert
			Object[] objs = new Object[] {
					encounterId,
					encounterIdSource,
					encounterPatientId,
					encounterPatientIdSource,
					encounterId,
					encounterIdSource,
					eid.getEventId().getStatus(),
					(eid.getEventId().getUpdateDate() != null) ? eid
							.getEventId().getUpdateDate().toGregorianCalendar()
							.getTime() : null,
					(eid.getEventId().getDownloadDate() != null) ? eid
							.getEventId().getDownloadDate()
							.toGregorianCalendar().getTime() : null,
					eid.getEventId().getSourcesystemCd() };
			super.update(objs);
		}

	}

}
