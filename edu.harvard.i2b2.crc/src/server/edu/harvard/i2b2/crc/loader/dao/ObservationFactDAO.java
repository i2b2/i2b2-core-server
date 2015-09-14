package edu.harvard.i2b2.crc.loader.dao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;

/**
 * Observation Fact data access object.
 * 
 * @author rk903
 * 
 */
public class ObservationFactDAO extends CRCLoaderDAO implements
		IObservationFactDAO {

	private int DB_BATCH_INSERT_SIZE = 1;

	private static Log log = LogFactory.getLog(ObservationFactDAO.class);
	private DataSourceLookup dataSourceLookup = null;

	public ObservationFactDAO(DataSourceLookup dataSourceLookup, DataSource ds) {
		setDataSource(ds);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;

	}

	public int getRecordCountByUploadId(int uploadId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		int insertCount = jdbcTemplate.queryForInt("select count(1) from "
				+ getDbSchemaName() + "observation_fact where upload_id =?",
				new Object[] { uploadId });
		return insertCount;
	}

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public ObservationFactInsertHandle createObservationFactInserter(
			String tempTableName) {
		ObservationFactInsert observationFactInsert = new ObservationFactInsert(
				getDataSource(), tempTableName, getDbSchemaName());
		observationFactInsert.setBatchSize(DB_BATCH_INSERT_SIZE);
		return new ObservationFactInsertHandle(observationFactInsert);
	}

	/**
	 * Function to check if given table exists
	 * 
	 * @param tableName
	 * @return boolean
	 * @throws Exception
	 */
	public boolean checkTableExists(String tableName) throws I2B2Exception {
		Connection conn = null;
		boolean returnFlag = false;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{? = call "
					+ this.getDbSchemaName() + "isTableExists(?)}");
			callStmt.registerOutParameter(1, Types.VARCHAR);
			callStmt.setString(2, tableName.toUpperCase());
			callStmt.execute();
			String stringFlag = callStmt.getString(1);
			if (stringFlag.equalsIgnoreCase("TRUE")) {
				returnFlag = true;
			} else {
				returnFlag = false;
			}
		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			log.error("SQLException occured" + sqlEx.getMessage(), sqlEx);
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
		return returnFlag;
	}

	/**
	 * Function to call MERGE_TEMP_OBSERVATION_FACT(?) stored procedure.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void doTempTableMerge(String tempTableName, int uploadId,
			boolean appendFlag) throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName()
					+ "UPDATE_OBSERVATION_FACT(?,?,?,?)}");
			callStmt.setString(1, tempTableName);
			callStmt.setInt(2, uploadId);

			if (appendFlag == true) {
				callStmt.setInt(3, 1);
			} else {
				callStmt.setInt(3, 0);
			}
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
	 * Function to call remove temp table stored procedure.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void removeTempTable(String tempTableName) throws I2B2Exception {
		Connection conn = null;
		try {

			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName() + "REMOVE_TEMP_TABLE(?)}");
			callStmt.setString(1, tempTableName);
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
	 * Function to create create temp table stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempTableName) throws I2B2Exception {
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			CallableStatement callStmt = conn.prepareCall("{call "
					+ this.getDbSchemaName() + "CREATE_TEMP_TABLE(?,?)}");
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

	public void writeMissedDataLog(BufferedWriter bufWriter,
			String tempTableName) throws I2B2Exception {
		String queryString = "select  a.* "
				+ " from  (select utemp.encounter_ide, utemp.patient_ide, utemp.concept_cd, "
				+ " utemp.provider_id,utemp.start_date,utemp.modifier_cd "
				+ " from temp_upload_try1 utemp "
				+ " where utemp.encounter_ide not in (select emap.encounter_ide from encounter_mapping emap) "
				+ " union "
				+ " select utemp1.encounter_ide, utemp1.patient_ide, utemp1.concept_cd, "
				+ " utemp1.provider_id,utemp1.start_date,utemp1.modifier_cd "
				+ " from temp_upload_try1 utemp1 "
				+ " where utemp1.patient_ide not in (select patmap.patient_ide  from patient_mapping patmap ) "
				+ " union "
				+ " select utemp.encounter_ide, utemp.patient_ide, utemp.concept_cd, "
				+ " utemp.provider_id,utemp.start_date,utemp.modifier_cd "
				+ " from temp_upload_try1 utemp "
				+ " where  utemp.concept_cd is null " + " ) a ";

		Statement stmt = null;
		try {
			stmt = getDataSource().getConnection().createStatement();
			stmt.setFetchSize(5000);
			ResultSet resultSet = stmt.executeQuery(queryString);
			String encounterIde = "", patientIde = "", conceptCd = "", providerId = "";
			Date startDate = null;
			while (resultSet.next()) {
				encounterIde = resultSet.getString("encounter_ide");
				patientIde = resultSet.getString("patient_ide");
				conceptCd = resultSet.getString("concept_cd");
				providerId = resultSet.getString("provider_id");
				startDate = resultSet.getDate("start_date");
				// write to logwriter
				bufWriter.write(encounterIde + "|" + patientIde + "|"
						+ conceptCd + "|" + providerId + "\n");
			}
		} catch (IOException ioEx) {
			log.error("IOException ", ioEx);
			throw new I2B2Exception("IOException " + ioEx.getMessage(), ioEx);
		} catch (SQLException sqlEx) {
			log.error("SQLException ", sqlEx);
			throw new I2B2Exception("SQLException " + sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			log.error("Exception ", ex);
			throw new I2B2Exception("Exception " + ex.getMessage(), ex);
		} finally {
			try {
				stmt.close();
			} catch (SQLException sqlEx) {
				log.info("Unable to close statment", sqlEx);
			}
		}

	}

	// ************* Operation Objects section ***************

	/**
	 * <code>ObservationFact</code> Insert Object.
	 */
	protected class ObservationFactInsert extends BatchSqlUpdate {
		/**
		 * Create a new instance of ObservationInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected ObservationFactInsert(DataSource ds,
				String observationFactTable, String schemaName) {
			super(
					ds,
					"INSERT INTO "
							+ schemaName
							+ observationFactTable
							+ "  ("
							+ "encounter_id, "
							+ "encounter_id_source, "
							+ "concept_cd, "
							+ "patient_id, "
							+ "patient_id_source, "
							+ "provider_id, "
							+ "start_date, "
							+ "modifier_cd, "
							+ "instance_num, "
							+ "valtype_cd, "
							+ "tval_char, "
							+ "nval_num, "
							+ "valueflag_cd, "
							+ "quantity_num, "
							+ "confidence_num, "
							+ "observation_blob, "
							+ "units_cd, "
							+ "end_date, "
							+ "location_cd, "
							+ "update_date, "
							+ "download_date, "
							+ "import_Date, "
							+ "sourcesystem_cd,"
							+ "upload_id) "
							+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.FLOAT));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.FLOAT));
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.LONGVARCHAR)); // Types.CLOB
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		protected void insert(ObservationType observationType) {

			// new SqlLobValue(observationType
			// .getObservationBlob().getContent().get(0))

			Object[] objs = new Object[] {
					// observationFactMap.get("encounter_ide"),
					observationType.getEventId().getValue(),
					observationType.getEventId().getSource(),
					// observationFactMap.get("concept_cd"),
					observationType.getConceptCd().getValue(),
					// observationFactMap.get("patient_ide"),
					(observationType.getPatientId() != null) ? observationType
							.getPatientId().getValue() : null,
					(observationType.getPatientId() != null) ? observationType
							.getPatientId().getSource() : null,
					// observationFactMap.get("provider_id"),
					(observationType.getObserverCd() != null) ? observationType
							.getObserverCd().getValue() : null,
					// observationFactMap.get("start_date"),
					(observationType.getStartDate() != null) ? observationType
							.getStartDate().toGregorianCalendar().getTime()
							: null,
					// observationFactMap.get("modifier_cd"),
					(observationType.getModifierCd() != null) ? observationType
							.getModifierCd().getValue() : null,
					(observationType.getInstanceNum() != null) ? observationType
							.getInstanceNum().getValue()
							: null,
					// observationFactMap.get("valtype_cd"),
					observationType.getValuetypeCd(),
					// observationFactMap.get("tval_char"),
					observationType.getTvalChar(),
					// (Float)observationFactMap.get("nval_num"),
					(observationType.getNvalNum() != null) ? observationType
							.getNvalNum().getValue() : null,
					// observationFactMap.get("valueflag_cd"),
					(observationType.getValueflagCd() != null) ? observationType
							.getValueflagCd().getValue()
							: null,
					// (Float)observationFactMap.get("quantity_num"),
					(observationType.getQuantityNum() != null) ? observationType
							.getQuantityNum()
							: null,
					// observationFactMap.get("confidence_num"),

					// TODO add confidence number to observation
					null,
					// observationFactMap.get("observation_blob"),
					(observationType.getObservationBlob() != null) ? observationType
							.getObservationBlob().getContent().get(0)
							.toString()
							: null,
					// observationFactMap.get("units_cd"),
					observationType.getUnitsCd(),
					// observationFactMap.get("end_date"),
					(observationType.getEndDate() != null) ? observationType
							.getEndDate().toGregorianCalendar().getTime()
							: null,
					// observationFactMap.get("location_cd"),
					(observationType.getLocationCd() != null) ? observationType
							.getLocationCd().getValue() : null,
					// (Date) observationFactMap.get("update_date"),
					(observationType.getUpdateDate() != null) ? observationType
							.getUpdateDate().toGregorianCalendar().getTime()
							: null,
					// (Date) observationFactMap.get("download_date"),
					(observationType.getDownloadDate() != null) ? observationType
							.getDownloadDate().toGregorianCalendar().getTime()
							: null,
					// (Date) observationFactMap.get("import_date"),
					(observationType.getImportDate() != null) ? observationType
							.getImportDate().toGregorianCalendar().getTime()
							: null,
					// observationFactMap.get("sourcesystem_cd"),
					observationType.getSourcesystemCd(),
					// observationFactMap.get("upload_id")
					observationType.getUploadId() };
			update(objs);
		}

	}

}
