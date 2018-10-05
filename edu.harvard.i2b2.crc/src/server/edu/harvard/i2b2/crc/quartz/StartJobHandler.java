/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.quartz;

import java.util.Date;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

public class StartJobHandler {

	Scheduler sch = null;

	public StartJobHandler(Scheduler sch) {
		this.sch = sch;
	}

	public void startNonQuartzJob(String domainId, String projectId,
			String userId, String instanceId, long timeout)
			throws SchedulerException {

		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(JobParamName.DOMAIN_ID, domainId);
		jobDataMap.put(JobParamName.PROJECT_ID, projectId);
		jobDataMap.put(JobParamName.USER_ID, userId);
		jobDataMap.put(JobParamName.INSTANCE_ID, instanceId);
		jobDataMap.put(JobParamName.TIMEOUT, timeout);
		FirstJob generalJob = new FirstJob();
		generalJob.executeNonQuartzJob(jobDataMap);
	}

	public void startJob(String domainId, String projectId, String userId,
			String instanceId, long timeout) throws SchedulerException {
		long startTime = System.currentTimeMillis() + 1000L;

		SimpleTrigger trigger = new SimpleTrigger("AnalysisJobTrigger", null,
				new Date(startTime), null, 0, 0L);
		long endTime = startTime + 3000;
		trigger.setEndTime(new Date(startTime));

		/*
		 * InitialContext ctx = new InitialContext(); Scheduler scheduler =
		 * (Scheduler) ctx.lookup("Quartz"); Trigger trigger =
		 * TriggerUtils.makeDailyTrigger("myTrigger", 0, 0); //a trigger which
		 * gets fired on each midnight trigger.setStartTime(new Date());
		 * 
		 * JobDetail job = new JobDetail("jobName", "jobGroup", Executor.class);
		 */
		JobDetail job = new JobDetail(projectId + instanceId, "group1",
				FirstJob.class);
		job.getJobDataMap().put(JobParamName.DOMAIN_ID, domainId);
		job.getJobDataMap().put(JobParamName.PROJECT_ID, projectId);
		job.getJobDataMap().put(JobParamName.USER_ID, userId);
		job.getJobDataMap().put(JobParamName.INSTANCE_ID, instanceId);
		job.getJobDataMap().put(JobParamName.TIMEOUT, timeout);

		sch.scheduleJob(job, trigger);

	}

}
