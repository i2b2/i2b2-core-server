/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
