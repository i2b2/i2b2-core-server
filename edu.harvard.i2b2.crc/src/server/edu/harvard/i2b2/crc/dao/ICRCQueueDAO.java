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