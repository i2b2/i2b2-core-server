package edu.harvard.i2b2.crc.ejb.analysis;

import java.util.Iterator;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.quartz.CronJob;
import edu.harvard.i2b2.crc.util.AnalysisPropertyUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class LargeCronEjb implements LargeCronEjbLocal {

	/** log **/
	protected final static Log log = LogFactory.getLog(LargeCronEjb.class);

//	TimerService timerService;

	public void start() {
		/*
		for (Iterator iterator = timerService.getTimers().iterator(); iterator
				.hasNext();) {
			Timer timer = (Timer) iterator.next();
			if (timer.getInfo().toString().equals("lcj")) {
				timer.cancel();
			}
		}

		String jobCheckTime = null;
		Long jobCheckTimeMills = 2L * 60L * 1000L;
		try {
			jobCheckTime = QueryProcessorUtil.getInstance()
					.getCRCPropertyValue(
							AnalysisPropertyUtil.LARGE_QUEUE_JOBCHECK_TIME);
			jobCheckTimeMills = Long.parseLong(jobCheckTime);
		} catch (I2B2Exception e) {
			log.warn("Error reading property "
					+ AnalysisPropertyUtil.LARGE_QUEUE_JOBCHECK_TIME
					+ " defaulted to 120000 mills ", e);
			jobCheckTimeMills = 2L * 60L * 1000L;
		}
		timerService.createTimer(jobCheckTimeMills, jobCheckTimeMills, "lcj");
		*/
	}

	public void someMethodToInvoke() { //Timer timer) {

		Scheduler sch;
		try {
			sch = QueryProcessorUtil.getInstance().getQuartzScheduler();
			String queueName = "LARGE_QUEUE";
			int maxReturnSize = 4;
			long timeout = 24L * 60L * 60L * 1000L;
			try {
				String maxReturnSizeStr = QueryProcessorUtil.getInstance()
						.getCRCPropertyValue(
								AnalysisPropertyUtil.LARGE_QUEUE_JOBCOUNT);
				maxReturnSize = Integer.parseInt(maxReturnSizeStr);

				String timeoutStr = QueryProcessorUtil.getInstance()
						.getCRCPropertyValue(
								AnalysisPropertyUtil.LARGE_QUEUE_TIMEOUT);
				timeout = Long.parseLong(timeoutStr);
			} catch (I2B2Exception e) {
				e.printStackTrace();
			}
			CronJob cronJob = new CronJob(sch, queueName, maxReturnSize,
					timeout);
			cronJob.execute(null);
		} catch (JobExecutionException e) {
			e.printStackTrace();

		} catch (I2B2Exception e) {
			e.printStackTrace();
		}

	}

}
