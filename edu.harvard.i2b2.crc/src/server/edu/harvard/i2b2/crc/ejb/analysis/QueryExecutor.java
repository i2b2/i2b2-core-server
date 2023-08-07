/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.ejb.analysis;

import java.util.Map;

import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.exec.ExecException;
import edu.harvard.i2b2.crc.exec.ExecUtil;

public class QueryExecutor {

	private String queryInstanceId = null;
	private Map paramMap = null;
	private SetFinderDAOFactory sfDAOFactory = null;

	public QueryExecutor(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId) {
		this.sfDAOFactory = sfDAOFactory;
		this.queryInstanceId = queryInstanceId;
	}

	public void execute(AnalysisDefinitionType analysisDefinition,
			String projectId, long timeout) throws ExecException {
		// from analysis definition type get the command line, type of program
		// etc
		// pass the command line , input parameter to exec util
		AnalysisPluginHelper analysisPlugin = new AnalysisPluginHelper(
				sfDAOFactory);
		String analysisName = analysisDefinition.getAnalysisPluginName();
		String version = analysisDefinition.getVersion();

		Map execInfoMap = analysisPlugin.getExecInfo(analysisName, version,
				projectId);

		ExecUtil execUtil = new ExecUtil();

		String line = (String) execInfoMap
				.get(AnalysisPluginHelper.COMMAND_LINE);
		String workingFolder = (String) execInfoMap
				.get(AnalysisPluginHelper.WORKING_FOLDER);

		execUtil.execute(workingFolder, line, timeout);

		// get the exec result based on the output list
		// update the result instance and query instance
	}

	// public void getQueryInstanceStatus();

	// public void getQueryResultInstanceStatus();

}
