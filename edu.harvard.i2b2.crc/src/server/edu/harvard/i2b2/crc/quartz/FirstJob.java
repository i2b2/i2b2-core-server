package edu.harvard.i2b2.crc.quartz;

import javax.annotation.Resource;

import javax.naming.InitialContext;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;
import edu.harvard.i2b2.crc.ejb.analysis.QueryInstance;

public class FirstJob implements Job {

//	Connection connection = null;
//	@Resource(mappedName = "ConnectionFactory")
//	private ConnectionFactory connectionFactory;
//	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadresponse")
//	private static Queue responseQueue;

	public FirstJob() {
	}

	public void executeNonQuartzJob(JobDataMap jobDataMap)
			throws JobExecutionException {
		executeHelper(jobDataMap);
	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		executeHelper(jobDataMap);
	}

	private void executeHelper(JobDataMap jobDataMap)
			throws JobExecutionException {
		String domainId = jobDataMap.getString(JobParamName.DOMAIN_ID);
		String projectId = jobDataMap.getString(JobParamName.PROJECT_ID);
		String userId = jobDataMap.getString(JobParamName.USER_ID);
		Long timeout = jobDataMap.getLong(JobParamName.TIMEOUT);
		String instanceId = jobDataMap.getString(JobParamName.INSTANCE_ID);

		// pass this jobDataMap to handler
		AnalysisJobStarter jobStarter = null;
		boolean errorFlag = false;
		Throwable e = null;
		try {
			jobStarter = new AnalysisJobStarter(domainId, projectId, userId);
			jobStarter.start(instanceId, timeout);

		} catch (I2B2DAOException daoEx) {
			errorFlag = true;
			e = daoEx;
		} catch (I2B2Exception i2b2Ex) {
			errorFlag = true;
			e = i2b2Ex;
			// no need to write to query instance
		} finally {
			if (errorFlag) {
				DAOFactoryHelper factoryHelper;
				try {
					factoryHelper = new DAOFactoryHelper(domainId, projectId,
							userId);

					SetFinderDAOFactory setfinderDaoFactory = factoryHelper
							.getDAOFactory().getSetFinderDAOFactory();
					// update instance with error status
					QueryInstance qi = new QueryInstance(setfinderDaoFactory);
					String stacktrace = StackTraceUtil.getStackTrace(e);
					if (stacktrace != null) {
						if (stacktrace.length() > 2000) {
							stacktrace = stacktrace.substring(0, 1998);
						} else {
							stacktrace = stacktrace.substring(0,
							stacktrace.length());
						}
					}
					// update error status to the instance
					qi.updateInstanceStatus(instanceId, StatusEnum.ERROR
							.toString(), stacktrace);
					// update result instance
					qi.updateResultInstanceStatusByInstanceId(instanceId,
							StatusEnum.ERROR.toString(), 0, stacktrace);
				} catch (I2B2DAOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// throw job execution exception to the quartz layer
				JobExecutionException jobExecEx = new JobExecutionException();
				jobExecEx.setStackTrace(e.getStackTrace());
				throw jobExecEx;
			}
			try {
				// sent ack to jms about completion of task
				sendResponse(instanceId, "");
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}

	}

	/*
	 * @throws Throwable
	 */
	private void sendResponse(String sessionId, String publishMessage)
			throws Throwable {
		/*
		Session session = null;
		MessageProducer publisher = null;
		TextMessage message = null;
		try {
			InitialContext ic = new InitialContext();
			connectionFactory = (ConnectionFactory) ic
					.lookup("ConnectionFactory");
			responseQueue = (Queue) ic
					.lookup("jms/edu.harvard.i2b2.crc.loader.loadresponse");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			publisher = session.createProducer(responseQueue);
			message = session.createTextMessage();
			message.setJMSCorrelationID(sessionId);
			message.setText(publishMessage);
			// message.setIntProperty(UPLOAD_ID, uploadId);
			publisher.send(message);

		} catch (Throwable t) {
			throw t;
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
				}

			}
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {

				}
			}
		}
*/
	}

}