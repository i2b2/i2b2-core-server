package edu.harvard.i2b2.crc.dao.quartz;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.DateBuilder.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ScheduleType;



public class QuartzExec {

	protected final Log logger = LogFactory.getLog(getClass());
	protected final Log logesapi = LogFactory.getLog(getClass());
	 SchedulerFactory sf = null;
	 Scheduler sched = null;
	 
	 public void run(JobDetail job, Trigger trigger) throws Exception {


		    // First we must get a reference to a scheduler
		     sf = new StdSchedulerFactory();
		     sched = sf.getScheduler();

		  // Trigger the job to run on the next round minute

		     // Tell quartz to schedule the job using our trigger
		     sched.scheduleJob(job, trigger);

		     // Start up the scheduler (nothing can actually run until the
		     // scheduler has been started)
		     sched.start();

		     logger.info("------- Started Scheduler -----------------");

		     // wait long enough so that the scheduler as an opportunity to
		     // run the job!
		     logger.info("------- Waiting 65 seconds... -------------");
		     try {
		       // wait 65 seconds to show job
		       Thread.sleep(65L * 1000L);
		       // executing...
		     } catch (Exception e) {
		       //
		     }

		     // shut down the scheduler
		     logger.info("------- Shutting Down ---------------------");
		     sched.shutdown(true);
		     logger.info("------- Shutdown Complete -----------------");
	 }

	public void addJob(DataSourceLookup dataSourceLookup, JobType job) throws I2B2Exception {
		JobDetail nweJob = newJob(i2b2QuartzSP.class) 
				.withIdentity("storedProcecure", job.getName()) 
			//	.usingJobData("alpha", 1.23f) 							
			//	.usingJobData("beta", 3.141f) 
				.build();
		
		ScheduleType schedule = job.getSchedule();
	    Date runTime = null;
	    
	    switch(schedule.getIntervalIn()) {
	    case "MINUTE" : runTime = evenMinuteDate(new Date());
	    case "HOUR" : runTime = evenHourDate(new Date());
	    }

	     Trigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(runTime).build();

		
	}


}
