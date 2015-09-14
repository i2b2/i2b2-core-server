package edu.harvard.i2b2.crc.ejb.analysis;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IAnalysisPluginDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;

public class QueryMaster {

	private static Log log = LogFactory.getLog(QueryMaster.class);

	private SetFinderDAOFactory sfDAOFactory = null;

	public QueryMaster(SetFinderDAOFactory sfDAOFactory) {
		this.sfDAOFactory = sfDAOFactory;
	}

	public QtAnalysisPlugin lookupAnalysisPlugin(String analysisName,
			String version, String projectId) throws I2B2Exception {
		log.debug("looking for: " + analysisName +":" + version + ":" + projectId);
		IAnalysisPluginDao analysisPluginDao = sfDAOFactory
				.getAnalysisPluginDao();
		log.debug("getting QtAnalysisPlugin");
		QtAnalysisPlugin analysisPlugin = analysisPluginDao
				.lookupAnalysisPluginByNameVersionProject(analysisName,
						version, projectId);
		return analysisPlugin;
	}

	public String saveQuery(String requestXml, String generatedSql,
			QtAnalysisPlugin analysisPlugin) throws I2B2Exception {
		IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();

		QtQueryMaster queryMaster = new QtQueryMaster();
		I2B2RequestMessageHelper msgHelper = new I2B2RequestMessageHelper(
				requestXml);
		UserType userType = msgHelper.getUserType();
		String userId = userType.getLogin();
		String groupId = msgHelper.getProjectId();

		queryMaster.setUserId(userId);

		AnalysisDefinitionType anaDefType;
		String queryMasterId = null;
		try {
			anaDefType = msgHelper.getAnalysisDefinition();

			String analysisDefinitionXml = msgHelper
					.getAnalysisDefinitionXml(anaDefType);
			queryMaster.setRequestXml(analysisDefinitionXml);
			queryMaster.setGroupId(groupId);
			queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
			queryMaster.setDeleteFlag(QtQueryMaster.DELETE_OFF_FLAG);
			queryMaster.setGeneratedSql(null);
			queryMaster.setName(anaDefType.getAnalysisPluginName());
			queryMaster.setPluginId(analysisPlugin.getPluginId());
			queryMaster.setMasterTypeCd(analysisPlugin.getPluginName());

			queryMasterId = queryMasterDao.createQueryMaster(queryMaster,
					requestXml, null);
		} catch (JAXBUtilException e) {

			throw new I2B2Exception("Failed to save query definition: ["
					+ e.getMessage() + "]", e);
		}

		return queryMasterId;
	}

	public AnalysisDefinitionType getAnalysisDefinitionByMasterId(
			String masterId) throws JAXBUtilException {
		AnalysisDefinitionType analysisDefType = null;
		IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
		QtQueryMaster queryMaster = queryMasterDao.getQueryDefinition(masterId);
		String definitionXml = queryMaster.getRequestXml();
		if (definitionXml == null) {
			return analysisDefType;
		}
		analysisDefType = I2B2RequestMessageHelper
				.getAnalysisDefinitionFromXml(definitionXml);
		return analysisDefType;
	}

	public QtQueryMaster getQueryMaster(String requestXml) throws Exception {
		QtQueryMaster queryMaster = new QtQueryMaster();
		I2B2RequestMessageHelper msgHelper = new I2B2RequestMessageHelper(
				requestXml);
		UserType userType = msgHelper.getUserType();
		String userId = userType.getLogin();
		String groupId = userType.getGroup();

		queryMaster.setUserId(userId);

		AnalysisDefinitionType anaDefType;
		try {
			anaDefType = msgHelper.getAnalysisDefinition();

			String analysisDefinitionXml = msgHelper
					.getAnalysisDefinitionXml(anaDefType);
			queryMaster.setRequestXml(analysisDefinitionXml);
			queryMaster.setGroupId(groupId);
			queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
			queryMaster.setDeleteFlag(QtQueryMaster.DELETE_OFF_FLAG);
			queryMaster.setGeneratedSql("generatedSql");
			queryMaster.setName(anaDefType.getAnalysisPluginName());
			queryMaster.setI2b2RequestXml(requestXml);

		} catch (JAXBUtilException e) {

			throw new I2B2Exception("Failed to save query definition: ["
					+ e.getMessage() + "]", e);
		}
		return queryMaster;
	}

}
