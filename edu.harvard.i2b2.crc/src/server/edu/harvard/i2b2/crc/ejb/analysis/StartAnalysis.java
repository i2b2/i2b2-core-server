package edu.harvard.i2b2.crc.ejb.analysis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType.Condition;
import edu.harvard.i2b2.crc.exec.ExecException;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.quartz.StartJobHandler;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.I2B2ResponseMessageHelper;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class StartAnalysis implements StartAnalysisLocal {
/*
	Connection connection = null;
	@Resource(mappedName = "ConnectionFactory")
	private static ConnectionFactory connectionFactory;
	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadrunner")
	private static Queue queue;
	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadresponse")
	private static Queue responseQueue;
	@Resource
	private UserTransaction utx;
	// public static ApplicationContext ac;
*/
	// log
	private static Log log = LogFactory.getLog(StartAnalysis.class);

	public MasterInstanceResultResponseType start(IDAOFactory daoFactory,
			String requestXml) throws I2B2Exception {
		StatusType statusType = new StatusType();
		String statusName = null, statusMessage = null;

		log.debug("in StartAnalysis: getting getSetFinderDAOFactory");
		SetFinderDAOFactory sfDAOFactory = daoFactory.getSetFinderDAOFactory();

		MasterInstanceResultResponseType masterInstanceResultResponseType = null;

		String queryMasterId = null, queryInstanceId = null;
		UserType userType = null;

		log.debug("in StartAnalysis: getting queryMaster");

		QueryMaster queryMaster = new QueryMaster(sfDAOFactory);
		QueryInstance queryInstance = new QueryInstance(sfDAOFactory);

		try {
			// store the request in master table
			I2B2RequestMessageHelper msgHelper = new I2B2RequestMessageHelper(
					requestXml);
			AnalysisDefinitionType analysisDefType = msgHelper
					.getAnalysisDefinition();
			userType = msgHelper.getUserType();

			log.debug("in StartAnalysis: getting analysisPlugin in queryMaster:" + queryMaster);

			log.debug("analysisDefType is " + analysisDefType);
			log.debug("plugin Name: " + analysisDefType.getAnalysisPluginName());
			log.debug("plugin version: " +  analysisDefType
					.getVersion());
			log.debug("plugin projectid" + msgHelper.getProjectId());
			QtAnalysisPlugin analysisPlugin = queryMaster.lookupAnalysisPlugin(
					analysisDefType.getAnalysisPluginName(), analysisDefType
							.getVersion(), msgHelper.getProjectId());
			log.debug("in StartAnalysis: my plugin id is " + analysisPlugin.getPluginId());

			String pluginId = analysisPlugin.getPluginId();
			String domainId = userType.getGroup();
			String projectId = msgHelper.getProjectId();
			String userId = userType.getLogin();
			// call privilege bean to check for permission
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);

			authHelper.checkRoleForPluginId(pluginId);
			log.debug("query master saved");
//			utx.begin();
			String generatedSql = null;
			// save the analysis request
			queryMasterId = queryMaster.saveQuery(requestXml, generatedSql,
					analysisPlugin);

//			utx.commit();
			log.debug("query master saved [" + queryMasterId + "]");

			// get run instance
//			utx.begin();
			// save the analysis instance

			analysisDefType.getAnalysisPluginName();
			ResultOutputOptionListType resultOutputList = I2B2RequestMessageHelper
					.buildResultOptionListFromAnalysisResultList(analysisDefType
							.getCrcAnalysisResultList());

			// userType.setGroup(msgHelper.getProjectId());

			queryInstanceId = queryInstance.saveInstanceAndResultInstance(
					queryMasterId, userType, "WITHOUT_QUEUE", resultOutputList);
//			utx.commit();

			// determine which queue it goes and put the jobs in that queue
			/*
			 * QueryExecutor queryExecutor = new QueryExecutor(sfDAOFactory,
			 * queryInstanceId); long timeout = msgHelper.getTimeout();
			 * queryExecutor.execute(analysisDefType, timeout);
			 */

			long timeout = msgHelper.getTimeout();
			StartJobHandler startJobHandler = new StartJobHandler(
					QueryProcessorUtil.getInstance().getQuartzScheduler());

			startJobHandler.startNonQuartzJob(domainId, projectId, userId,
					queryInstanceId, timeout);

			// Thread.sleep(timeout);
			waitForProcess(timeout, queryInstanceId);
		} catch (ExecException execEx) {
			if (execEx.getExitStatus().equals(ExecException.TIMEOUT_STATUS)) {
				statusName = StatusEnum.QUEUED.toString();
			} else {
				statusName = StatusEnum.ERROR.toString();
			}
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(execEx);
		} catch (SecurityException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (IllegalStateException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (I2B2Exception e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (JAXBUtilException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// just update query instance status, result instance status will be
			// updated by queryexecutor
			// queryInstance.updateInstanceStatus(queryInstanceId, statusName,
			// statusMessage);
			// if error then rollback
			// utx.setRollbackOnly();

		}

		Condition condition = new Condition();
		condition.setType(statusName);
		condition.setValue(statusMessage);
		statusType.getCondition().add(condition);
		// build masterInstanceResultResponse
		I2B2ResponseMessageHelper responseMessageHelper = new I2B2ResponseMessageHelper(
				sfDAOFactory);
		try {
			masterInstanceResultResponseType = responseMessageHelper
					.buildResponse(queryMasterId, queryInstanceId, userType
							.getLogin(), statusType);
		} catch (I2B2DAOException e) {
			throw new I2B2Exception("Error " + e.getMessage() + "] ", e);
		}
		return masterInstanceResultResponseType;
	}

	private void waitForProcess(long timeout, String instanceId) {
		/*
		MessageConsumer receiver = null;
		TextMessage message = null;
		//TODO removed loaders
		//LoadDataResponseType response = null;
		Session session = null;
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			String selector = "JMSCorrelationID='" + instanceId + "'";
			receiver = session.createConsumer(responseQueue, selector);

			connection.start();

			TextMessage inMessage = (TextMessage) receiver.receive(timeout);
			if (inMessage != null) {
				log.info("Received text message from response queue"
						+ inMessage.getText());

			}
		} catch (JMSException jmsEx) {
			jmsEx.printStackTrace();
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
				}
			}

		}
		*/
	}

	public void queueProcess() {
		// write the status to table
		// if processs is small, run them by submitting to quartz right away
	}

	public void cronJobProcess() {
		// a)Cronjob for midium and long queue :
		// a.1) Look for running job if the start time>30min, kill the job and
		// change status to stop
		// a.2) Start new job
	}

	/**
	 * Creates the connection.
	 */
	@PostConstruct
	public void makeConnection() {
		try {
		//	connection = connectionFactory.createConnection();
		} catch (Throwable t) {
			// JMSException could be thrown
			log.error("DataMartLoaderAsync.makeConnection:" + "Exception: "
					+ t.toString());
		}
	}

	/**
	 * Closes the connection.
	 */
	@PreDestroy
	public void endConnection() throws RuntimeException {
	//	if (connection != null) {
	//		try {
	//			connection.close();
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//	}
	}

}
