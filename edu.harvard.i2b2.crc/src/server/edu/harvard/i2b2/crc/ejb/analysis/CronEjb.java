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

public class CronEjb implements CronEjbLocal {
	/** log **/
	protected final static Log log = LogFactory.getLog(CronEjb.class);

//	@Resource
//	TimerService timerService;

	public void start() {
		/*
		for (Iterator iterator = timerService.getTimers().iterator(); iterator
				.hasNext();) {
			Timer timer = (Timer) iterator.next();
			if (timer.getInfo().toString().equals("mcj")) {
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

		timerService.createTimer(jobCheckTimeMills, jobCheckTimeMills, "mcj");
		*/
	}

	public void someMethodToInvoke() { //Timer timer) {

		Scheduler sch;
		try {
			sch = QueryProcessorUtil.getInstance().getQuartzScheduler();
			String queueName = "MEDIUM_QUEUE";
			int maxReturnSize = 4;
			long timeout = 30L * 60L * 1000L;
			try {
				String maxReturnSizeStr = QueryProcessorUtil.getInstance()
						.getCRCPropertyValue(
								AnalysisPropertyUtil.MEDIUM_QUEUE_JOBCOUNT);
				maxReturnSize = Integer.parseInt(maxReturnSizeStr);

				String timeoutStr = QueryProcessorUtil.getInstance()
						.getCRCPropertyValue(
								AnalysisPropertyUtil.MEDIUM_QUEUE_TIMEOUT);
				timeout = Long.parseLong(timeoutStr);
			} catch (I2B2Exception e) {
				e.printStackTrace();
			}
			CronJob cronJob = new CronJob(sch, queueName, maxReturnSize,
					timeout);

			cronJob.execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
