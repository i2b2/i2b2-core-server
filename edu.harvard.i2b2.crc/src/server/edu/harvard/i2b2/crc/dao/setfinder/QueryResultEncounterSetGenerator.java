package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryDefinitionUnWrapUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryResultEncounterSetGenerator extends CRCDAO implements
		IResultGenerator {

	public void generateResult(Map param) throws I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");
		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		String resultTypeName = (String) param.get("ResultOptionName");
		DataSourceLookup originalDataSource = (DataSourceLookup) param
				.get("OriginalDataSourceLookup");
		List<String> roles = (List<String>) param.get("Roles");
		String processTimingFlag = (String)param.get("ProcessTimingReportFlag");
		int obfucatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		int recordCount = (Integer) param.get("RecordCount");
		boolean obfuscatedRoleFlag = (Boolean)param.get("ObfuscatedRoleFlag");
		this.setDbSchemaName(sfDAOFactory.getDataSourceLookup().getFullSchema()); 
		

		String queryGeneratorVersion = "1.6";
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			queryGeneratorVersion = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinder.querygenerator.version");
		} catch (I2B2Exception e) {
			// ignore this default will be 1.6
		}

		boolean errorFlag = false;
		Exception exception = null;
		int loadCount = 0;
		String obfuscationDescription = "", obfusMethod = "";

		try {

			int i = 0;
			IEncounterSetCollectionDao encounterSetCollectionDao = sfDAOFactory
					.getEncounterSetCollectionDAO();
			encounterSetCollectionDao
					.createPatientEncCollection(resultInstanceId);

			// build the encounter set sql using dx table
			// if the querytiming is not SAMEVISIT, then join the
			// visit_dimension table to get encountner num for the patients.
			String encounterSql = buildEncounterSetSql(sfDAOFactory,
					queryInstanceId, TEMP_DX_TABLE, queryGeneratorVersion);
			log.debug("Executing setfinder query result type encounter set sql [" + encounterSql + "]");
			/////////
			//JNix: refactored to no longer pull down records just to insert back.
			String sql = null;
			String dbSchemaName = this.getDbSchemaName();
			if (sfDAOFactory.getDataSourceLookup().getServerType().equals(DAOFactoryHelper.ORACLE)) {
				sql = "INSERT INTO " + dbSchemaName + "qt_patient_enc_collection"
						+ " (patient_enc_coll_id, result_instance_id, set_index, patient_num, encounter_num) "
						+ "SELECT " + dbSchemaName + "QT_SQ_QPER_PECID.nextval AS patient_set_coll_id, ? AS result_instance_id, rownum AS set_index, t.patient_num, t.encounter_num "
						+ "FROM (" + encounterSql + ") t";
			} else if (sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				sql = "INSERT INTO " + dbSchemaName + "qt_patient_enc_collection"
						+ " (result_instance_id, set_index, patient_num, encounter_num) "
						+ "SELECT ? AS result_instance_id, ROW_NUMBER() OVER(ORDER BY patient_num) AS set_index, t.patient_num, t.encounter_num "
						+ "FROM (" + encounterSql + ") t order by t.patient_num,t.encounter_num";
			}
			log.debug("Executing sql:\n" + sql);
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			logTimingUtil.setStartTime();
			PreparedStatement ps = sfConn.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(resultInstanceId));
			loadCount = ps.executeUpdate();
			ps.close();
			logTimingUtil.setEndTime();
			log.debug("Total patients loaded for query instance ="
					+ queryInstanceId + " is [" + loadCount + "]");
			/////////
			if (processTimingFlag != null) {
				ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
				ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(logTimingUtil, "BUILD - ENCOUNTERSET", ""));
			}
		} catch (SQLException sqlEx) {
			exception = sqlEx;
			log.error("QueryResultPatientSetGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);

		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {
			IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				
				//set size for the encounter set = 0
				if (obfuscatedRoleFlag) { 
					loadCount = obfucatedRecordCount;
				} else {
					loadCount = recordCount;
				}
				
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, "",
						loadCount, recordCount, obfusMethod);
				//String description = "Encounter Set - "
				//		+ obfuscationDescription + loadCount + " encounters";
				String queryName = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(
						sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId()).getName();
				String description = "Encounter Set for \"" + queryName +"\"";
				resultInstanceDao.updateResultInstanceDescription(
						resultInstanceId, description);

			}
		}
	}

	private String buildEncounterSetSql(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, String TEMP_DX_TABLE,
			String queryGeneratorVersion) throws I2B2DAOException {
		// get request xml from query instance id
		// call timing helper to find if timing is samevisit

		String encounterSetSql = " select encounter_num,patient_num from "
				+ TEMP_DX_TABLE;
		if (queryGeneratorVersion.equals("1.6")||
				queryGeneratorVersion.equals("1.7")) {
			IQueryInstanceDao queryInstanceDao = sfDAOFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			String queryMasterId = queryInstance.getQtQueryMaster()
					.getQueryMasterId();
			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			QtQueryMaster queryMasterType = queryMasterDao
					.getQueryDefinition(queryMasterId);
			QueryDefinitionUnWrapUtil qpUnwrapUtil = new QueryDefinitionUnWrapUtil();
			QueryDefinitionType queryDefType = null;
			try {
				queryDefType = qpUnwrapUtil
						.getQueryDefinitionType(queryMasterType
								.getI2b2RequestXml());
			} catch (I2B2DAOException e) {
				throw e;
			}
			QueryTimingHandler queryTimingHandler = new QueryTimingHandler();
			boolean sameVisitFlag = queryTimingHandler
					.isSameVisit(queryDefType);

			if (sameVisitFlag == false) {
				encounterSetSql = " select encounter_num, patient_num from "
						+ sfDAOFactory.getDataSourceLookup().getFullSchema()
						+ ".visit_dimension  "
						+ " where patient_num in (select distinct patient_num from "
						+ TEMP_DX_TABLE
						+ ")  ";
			}
		}
		return encounterSetSql;
	}
}
