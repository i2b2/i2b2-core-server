package edu.harvard.i2b2.crc.quartz;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class StartJobHandlerTest {

	static Scheduler sch = null;

	@BeforeClass
	public static void init() throws SchedulerException, InterruptedException {
		// QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		QuartzFactory qFac = QuartzFactory.getInstance();
		sch = qFac.getScheduler();
		sch.start();
		// Thread.sleep(20000);
	}

	@Test
	public void blahTest() throws JobExecutionException {
		// call CronJob with
		String queueName = "MEDIUM_QUEUE";
		int maxReturnSize = 4;
		long timeout = 30L * 60L * 1000L;
		CronJob cronJob = new CronJob(sch, queueName, maxReturnSize, timeout);
		cronJob.execute(null);
	}

	@AfterClass
	public static void tearDown() throws SchedulerException,
			InterruptedException {
		Thread.sleep(20000);
		sch.shutdown();
	}
}
