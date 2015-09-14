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
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;

public class QueryResultPatientSetGenerator extends CRCDAO implements
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
		String processTimingFlag = (String) param.get("ProcessTimingFlag");
		int obfucatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		DataSourceLookup originalDataSource = (DataSourceLookup) param
				.get("OriginalDataSourceLookup");
		List<String> roles = (List<String>) param.get("Roles");
		this.setDbSchemaName(sfDAOFactory.getDataSourceLookup().getFullSchema()); 

		boolean errorFlag = false;
		Exception exception = null;
		int loadCount = 0, realCount = 0;
		String obfuscationDescription = "", obfusMethod = "";

		try {

			int i = 0;
			IPatientSetCollectionDao patientSetCollectionDao = sfDAOFactory
					.getPatientSetCollectionDAO();
			patientSetCollectionDao
					.createPatientSetCollection(resultInstanceId);

			String patientIdSql = " select distinct patient_num from " + TEMP_DX_TABLE
					+ " order by patient_num ";
			
			////
			//JNix: refactored to no longer pull down records just to insert back.
			String sql = null;
			String dbSchemaName = this.getDbSchemaName();
			if (sfDAOFactory.getDataSourceLookup().getServerType().equals(DAOFactoryHelper.ORACLE)) {
				sql = "INSERT INTO " + dbSchemaName + "qt_patient_set_collection"
						+ " (patient_set_coll_id, result_instance_id, set_index, patient_num) "
						+ "SELECT " + dbSchemaName + "QT_SQ_QPR_PCID.nextval AS patient_set_coll_id, ? AS result_instance_id, rownum AS set_index, t.patient_num "
						+ "FROM (SELECT DISTINCT patient_num FROM " + TEMP_DX_TABLE + ") t";
			} else if (sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
					sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				sql = "INSERT INTO " + dbSchemaName + "qt_patient_set_collection"
						+ " (result_instance_id, set_index, patient_num) "
						+ "SELECT ? AS result_instance_id, ROW_NUMBER() OVER(ORDER BY patient_num) AS set_index, t.patient_num "
						+ "FROM (SELECT DISTINCT patient_num FROM " + TEMP_DX_TABLE + ") t";
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
			////
			if (processTimingFlag != null) {
				if (!processTimingFlag.trim().equalsIgnoreCase(ProcessTimingReportUtil.NONE) ) {
					ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
					ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(logTimingUtil, "BUILD - PATIENTSET", ""));
				}
			}
			realCount = loadCount;
			

			// check for the user role to see if it needs data obfscation
			DataSourceLookup dataSourceLookup = sfDAOFactory
					.getOriginalDataSourceLookup();
			String domainId = originalDataSource.getDomainId();
			String projectId = originalDataSource.getProjectPath();
			String userId = originalDataSource.getOwnerId();
			boolean noDataAggFlag = false, noDataObfscFlag = false;

			DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
					.getDomainId(), dataSourceLookup.getProjectPath(),
					dataSourceLookup.getOwnerId());

			IDAOFactory daoFactory = helper.getDAOFactory();
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);

			try {
				authHelper.checkRoleForProtectionLabel(
						"SETFINDER_QRY_WITHOUT_DATAOBFSC", roles);
			} catch (MissingRoleException noRoleEx) {
				noDataAggFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}
			try {
				authHelper.checkRoleForProtectionLabel(
						"SETFINDER_QRY_WITH_DATAOBFSC", roles);
			} catch (MissingRoleException noRoleEx) {
				noDataObfscFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}

			if (noDataAggFlag && !noDataObfscFlag) {
				obfuscationDescription = "~";
				obfusMethod = IQueryResultInstanceDao.OBTOTAL;
				GaussianBoxMuller gaussianBoxMuller = new GaussianBoxMuller();
				//read count sigma from property file
				double countSigma = GaussianBoxMuller.getCountSigma();
				double obfuscatedMinimumValue = GaussianBoxMuller.getObfuscatedMinimumVal();
				loadCount = (int) gaussianBoxMuller
						.getNormalizedValueForCount(loadCount,countSigma,obfuscatedMinimumValue);
			} else { 
				obfucatedRecordCount = loadCount;
			}

			// readQueryStmt.close();
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
			
			String queryName = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(
					sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId()).getName();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, "",
						obfucatedRecordCount, 
						//loadCount, 
						realCount, obfusMethod);
				//String description = "Patient Set - " + obfuscationDescription
				//		+ loadCount + " Patients";
				String description = "Patient Set for \"" + queryName +"\"";
				resultInstanceDao.updateResultInstanceDescription(
						resultInstanceId, description);

			}
		}
	}
}
