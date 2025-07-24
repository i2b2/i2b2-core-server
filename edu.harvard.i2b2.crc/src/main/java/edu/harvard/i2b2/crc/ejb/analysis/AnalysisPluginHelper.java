/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.ejb.analysis;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IAnalysisPluginDao;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;

public class AnalysisPluginHelper {

	private SetFinderDAOFactory sfDAOFactory;
	public static final String COMMAND_LINE = "COMMAND_LINE";
	public static final String WORKING_FOLDER = "WORKING_FOLDER";

	public AnalysisPluginHelper(SetFinderDAOFactory sfDAOFactory) {
		this.sfDAOFactory = sfDAOFactory;
	}

	public Map getExecInfo(String analysisName, String version, String projectId) {
		Map execInfoMap = new HashMap();
		// lookup by name and return
		IAnalysisPluginDao analysisPluginDao = sfDAOFactory
				.getAnalysisPluginDao();
		try {
			QtAnalysisPlugin analysisPlugin = analysisPluginDao
					.lookupAnalysisPluginByNameVersionProject(analysisName,
							version, projectId);
			execInfoMap.put(COMMAND_LINE, analysisPlugin.getCommandLine());
			execInfoMap.put(WORKING_FOLDER, analysisPlugin.getWorkingFolder());
		} catch (I2B2DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return command line and working directory
		return execInfoMap;
	}
}
