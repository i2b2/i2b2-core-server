package edu.harvard.i2b2.crc.quartz;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCQueueDAO;
import edu.harvard.i2b2.crc.dao.DataSourceLookupDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.AnalysisJob;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;

public class CronJob implements Job {

	/** log **/
	protected final static Log log = LogFactory.getLog(CronJob.class);

	Scheduler sch = null;
	String queueName = "MEDIUM_QUEUE";
	int maxReturnSize = 4;
	long timeout = 30L * 60L * 1000L;

	public CronJob(Scheduler sch, String queueName, int maxReturnSize,
			long timeout) {
		this.sch = sch;
		this.queueName = queueName;
		this.maxReturnSize = maxReturnSize;
		this.timeout = timeout;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// read first X rows with timeout or queue type medium/long
		// call queryinstance dao to get first X rows based on the timestamp
		CRCQueueDAO queueDao = null;
		try {
			queueDao = DataSourceLookupDAOFactory.getCRCQueueDAO();
		} catch (I2B2DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<AnalysisJob> analysisJobList = queueDao.getJobListByQueuedStatus(
				queueName, maxReturnSize);

		// dispatch job for each row
		String domainId, projectId, userId, instanceId;
		for (AnalysisJob singleAnalysisJob : analysisJobList) {
			domainId = singleAnalysisJob.getDomainId();
			projectId = singleAnalysisJob.getProjectId();
			userId = singleAnalysisJob.getUserId();
			instanceId = singleAnalysisJob.getJobId();
			StartJobHandler startJobHandler = new StartJobHandler(sch);
			try {
				log.debug("starting job with instance id [" + instanceId
						+ "] domainid = " + domainId);
				queueDao.updateStatus(instanceId, projectId,
						StatusEnum.PROCESSING.toString());
				startJobHandler.startJob(domainId, projectId, userId,
						instanceId, timeout);
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
