package edu.harvard.i2b2.crc.datavo.db;

import java.util.Date;

public class AnalysisJob {
	String jobId;
	String queueName;
	int statusTypeId;
	String domainId;
	String projectId;
	String userId;
	String requestXml;
	Date createDate;

	Date updateDate;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getDomainId() {
		return domainId;
	}

	public int getStatusTypeId() {
		return statusTypeId;
	}

	public void setStatusTypeId(int statusTypeId) {
		this.statusTypeId = statusTypeId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRequestXml() {
		return requestXml;
	}

	public void setRequestXml(String requestXml) {
		this.requestXml = requestXml;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
