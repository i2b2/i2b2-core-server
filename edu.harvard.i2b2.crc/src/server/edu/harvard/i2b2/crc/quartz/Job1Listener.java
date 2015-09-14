/* 
 * Copyright 2005 OpenSymphony 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package edu.harvard.i2b2.crc.quartz;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

/**
 * @author wkratzer
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Job1Listener implements JobListener {

	private static Log _log = LogFactory.getLog(Job1Listener.class);

	public String getName() {
		return "job1_to_job2";
	}

	public void jobToBeExecuted(JobExecutionContext inContext) {
		_log.info("Job1Listener says: Job Is about to be executed.");
	}

	public void jobExecutionVetoed(JobExecutionContext inContext) {
		_log.info("Job1Listener says: Job Execution was vetoed.");
	}

	public void jobWasExecuted(JobExecutionContext inContext,
			JobExecutionException inException) {
		_log.info("Job1Listener says: Job was executed.");

		// Simple job #2
		JobDetail job2 = new JobDetail("job2", Scheduler.DEFAULT_GROUP,
				edu.harvard.i2b2.crc.quartz.FirstJob.class);

		// Simple trigger to fire immediately
		SimpleTrigger trigger = new SimpleTrigger("job2Trigger",
				Scheduler.DEFAULT_GROUP, new Date(), null, 0, 0L);

		try {
			// schedule the job to run!
			inContext.getScheduler().scheduleJob(job2, trigger);
		} catch (SchedulerException e) {
			_log.warn("Unable to schedule job2!");
			e.printStackTrace();
		}

	}

}
