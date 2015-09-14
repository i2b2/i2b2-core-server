package edu.harvard.i2b2.crc.ejb.analysis;

import java.util.List;


import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IAnalysisPluginDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisParameterXmlType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataTypeType;

/**
 * Bean class to return analysis plugin metadata info
 */
//@Stateless
public class AnalysisPluginInfo implements AnalysisPluginInfoLocal {

	/**
	 * Return analysis plugin metadata by plugin name and project
	 */
	public AnalysisPluginMetadataResponseType getAnalysisPluginMetadata(
			DataSourceLookup dataSourceLookup,
			AnalysisPluginMetadataRequestType apMetadataRequestType,
			String projectId) throws I2B2Exception {
		SetFinderDAOFactory sfDaoFactory = getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IAnalysisPluginDao analysisPluginDao = sfDaoFactory
				.getAnalysisPluginDao();
		String analysisName = apMetadataRequestType.getPluginName();

		List<QtAnalysisPlugin> analysisPluginList = analysisPluginDao
				.getAnalysisPluginMetadata(analysisName, projectId);

		return buildAnalysisPluginMetadataResponse(analysisPluginList);
	}

	// Function to build analysis plugin metadata response type
	private AnalysisPluginMetadataResponseType buildAnalysisPluginMetadataResponse(
			List<QtAnalysisPlugin> analysisPluginList) {
		DTOFactory dtoFactory = new DTOFactory();
		AnalysisPluginMetadataResponseType apMetadataResponseType = new AnalysisPluginMetadataResponseType();
		for (QtAnalysisPlugin analysisPlugin : analysisPluginList) {
			AnalysisPluginMetadataTypeType apmType = new AnalysisPluginMetadataTypeType();
			apmType.setPluginId(analysisPlugin.getPluginId());
			apmType.setPluginName(analysisPlugin.getPluginName());
			apmType.setDescription(analysisPlugin.getDescription());
			apmType.setStatusCd(analysisPlugin.getStatusId());
			AnalysisParameterXmlType parameterXmlType = new AnalysisParameterXmlType();
			parameterXmlType.getContent()
					.add(analysisPlugin.getParameterInfo());
			apmType.setParameterInfo(parameterXmlType);
			AnalysisParameterXmlType parameterXsdType = new AnalysisParameterXmlType();
			parameterXmlType.getContent().add(
					analysisPlugin.getParameterInfoXsd());
			apmType.setParameterInfoXsd(parameterXsdType);
			if (analysisPlugin.getUpdateDate() != null) {
				apmType.setUpdateDate(dtoFactory
						.getXMLGregorianCalendar(analysisPlugin.getUpdateDate()
								.getTime()));
			}

			apmType.setVersionCd(analysisPlugin.getVersion());
			if (analysisPlugin.getCreateDate() != null) {
				apmType.setCreateDate(dtoFactory
						.getXMLGregorianCalendar(analysisPlugin.getCreateDate()
								.getTime()));
			}
			apmType.setUserId(analysisPlugin.getUserId());
			apmType.setCommandoptionCd(analysisPlugin.getCommandOptionCd());
			apmType.setCommandLine(analysisPlugin.getCommandLine());
			apmType.setWorkingFolder(analysisPlugin.getWorkingFolder());
			apmType.setGroupId(analysisPlugin.getGroupId());
			apMetadataResponseType.getAnalysisPluginMetadataType().add(apmType);
		}
		return apMetadataResponseType;
	}

	private SetFinderDAOFactory getSetFinderDaoFactory(String domainId,
			String projectPath, String ownerId) throws I2B2DAOException {
		DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectPath,
				ownerId);
		SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory()
				.getSetFinderDAOFactory();
		return sfDaoFactory;
	}
}
