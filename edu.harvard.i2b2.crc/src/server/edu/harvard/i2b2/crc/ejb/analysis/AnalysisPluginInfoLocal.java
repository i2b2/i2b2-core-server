package edu.harvard.i2b2.crc.ejb.analysis;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataResponseType;

//@Local
public interface AnalysisPluginInfoLocal {

	/**
	 * Return analysis plugin metadata by plugin name and project
	 */
	public AnalysisPluginMetadataResponseType getAnalysisPluginMetadata(
			DataSourceLookup dataSourceLookup,
			AnalysisPluginMetadataRequestType apMetadataRequestType,
			String projectId) throws I2B2Exception;

}