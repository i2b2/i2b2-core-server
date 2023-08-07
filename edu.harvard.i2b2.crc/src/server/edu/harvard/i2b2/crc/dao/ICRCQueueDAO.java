/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao;

import java.util.List;

import edu.harvard.i2b2.crc.datavo.db.AnalysisJob;

public interface ICRCQueueDAO {

	public void addJob(AnalysisJob analysisJob);

	public List<AnalysisJob> getJob(String jobId, String projectId);

	public void moveJob(String jobId, String projectId, String queueName,
			String statusTypeId);

	public void updateStatus(String jobId, String projectId, String statusType);

	public List<AnalysisJob> getJobListByQueuedStatus(String queueName,
			int maxReturnSize);

}
