/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.crc.datavo.db.AnalysisJob;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;

public class CRCQueueDAO extends CRCDAO implements ICRCQueueDAO {
	JdbcTemplate jdbcTemplate = null;
	AnalysisJobRowMapper analysisJobMapper = new AnalysisJobRowMapper();

	public CRCQueueDAO(DataSource dataSource, String schemaName) {
		setDataSource(dataSource);
		setDbSchemaName(schemaName);
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void addJob(AnalysisJob analysisJob) {
		String sql = "insert into "
				+ getDbSchemaName()
				+ "CRC_ANALYSIS_JOB(job_id, queue_name, status_type_id,domain_id, project_id, user_id, request_xml, create_date) "
				+ " values(?,?,?,?,?,?,?,?)";
		Object[] params = new Object[] { analysisJob.getJobId(),
				analysisJob.getQueueName(), analysisJob.getStatusTypeId(),
				analysisJob.getDomainId(), analysisJob.getProjectId(),
				analysisJob.getUserId(), analysisJob.getRequestXml(),
				analysisJob.getCreateDate() };
		jdbcTemplate.update(sql, params);
	}

	@Override
	public List<AnalysisJob> getJob(String jobId, String projectId) {
		String sql = "select * from  " + getDbSchemaName()
				+ "CRC_ANALYSIS_JOB where job_id = ? and project_id = ? ";
		List<AnalysisJob> analysisJobList = jdbcTemplate.query(sql,
				new Object[] { jobId, projectId }, analysisJobMapper);
		return analysisJobList;
	}

	@Override
	public void moveJob(String jobId, String projectId, String queueName,
			String statusType) {
		String sql = "update "
				+ getDbSchemaName()
				+ "CRC_ANALYSIS_JOB set status_type_id = ?, queue_name = ?  where job_id = ? and project_id = ?";
		StatusEnum statusEnum = StatusEnum.valueOf(statusType);

		jdbcTemplate.update(sql, new Object[] { statusEnum.ordinal(),
				queueName, jobId });
	}

	@Override
	public void updateStatus(String jobId, String projectId, String statusType) {
		String sql = "update "
				+ getDbSchemaName()
				+ "CRC_ANALYSIS_JOB set status_type_id = ? where job_id = ? and project_id =? ";
		StatusEnum statusEnum = StatusEnum.valueOf(statusType);
		jdbcTemplate.update(sql, new Object[] { statusEnum.ordinal(), jobId,
				projectId });
	}

	@Override
	public List<AnalysisJob> getJobListByQueuedStatus(String queueName,
			int maxReturnSize) {
		int QUEUED_STATUS_TYPE_ID = StatusEnum.QUEUED.ordinal();
		JdbcTemplate localJdbcTemplate = new JdbcTemplate(getDataSource());
		String sql = " select * from "
				+ getDbSchemaName()
				+ "CRC_ANALYSIS_JOB where queue_name = ? and status_type_id = ? order by create_date";
		localJdbcTemplate.setMaxRows(maxReturnSize);
		List<AnalysisJob> analysisJobList = localJdbcTemplate.query(sql,
				new Object[] { queueName, QUEUED_STATUS_TYPE_ID },
				analysisJobMapper);
		return analysisJobList;

	}

	private class AnalysisJobRowMapper implements RowMapper {

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			AnalysisJob analysisJob = new AnalysisJob();
			analysisJob.setJobId(rs.getString("JOB_ID"));
			analysisJob.setQueueName(rs.getString("QUEUE_NAME"));
			analysisJob.setStatusTypeId(rs.getInt("STATUS_TYPE_ID"));
			analysisJob.setDomainId(rs.getString("DOMAIN_ID"));
			analysisJob.setProjectId(rs.getString("PROJECT_ID"));
			analysisJob.setUserId(rs.getString("USER_ID"));
			analysisJob.setRequestXml(rs.getString("REQUEST_XML"));
			analysisJob.setCreateDate(rs.getTimestamp("CREATE_DATE"));
			analysisJob.setUpdateDate(rs.getTime("UPDATE_DATE"));
			return analysisJob;
		}
	}

}
