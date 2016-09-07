package edu.harvard.i2b2.crc.ejb;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import javax.xml.bind.JAXBElement;

import org.apache.catalina.tribes.tipis.AbstractReplicatedMap.MapMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.CRCTimeOutException;
import edu.harvard.i2b2.crc.dao.setfinder.CheckSkipTempTable;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.LockedoutException;
import edu.harvard.i2b2.crc.dao.setfinder.QueryExecutorDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryModeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class ExecRunnable implements Runnable{
	private static Log log = LogFactory.getLog(ExecRunnable.class);


	String sqlString = "";
	String queryInstanceId = "";
	String patientSetId = "";
	String xmlRequest = "";
	String dsLookupDomainId= "";
	String dsLookupProjectId ="";
	String dsLookupOwnerId = "";
	String pmXml = null;

	Map returnMap = new HashMap();

	public ExecRunnable() {
	}

	private Exception ex = null;

	private String callingMDBName = QueryManagerBeanUtil.SMALL_QUEUE, sessionId = "";
	// default timeout three minutes
	int transactionTimeout = 0;

	MapMessage message = null;
	private boolean jobCompleteFlag = false;


	public Exception getJobException() {
		return ex;
	}

	public boolean isJobCompleteFlag() {
		return jobCompleteFlag;
	}

	public void setJobCompleteFlag(boolean jobCompleteFlag) {
		this.jobCompleteFlag = jobCompleteFlag;
	}

	public void setJobException(Exception ex) {
		this.ex = ex;
	}
	public Map getResult() {

		return returnMap;
	}

	public void run() {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		boolean allowLargeTextValueConstrainFlag = true;
		int queryResultInstanceId = 0;

		try {

			//    			if (message != null) {
				/*
    				replyToQueue = (Queue) message.getJMSReplyTo();
    				String sqlString = message
    						.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM);
    				String queryInstanceId = message
    						.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM);
    				String patientSetId = message
    						.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM);
    				String xmlRequest = message
    						.getString(QueryManagerBeanUtil.XML_REQUEST_PARAM);

    				String dsLookupDomainId = message
    						.getString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID);
    				String dsLookupProjectId = message
    						.getString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID);
    				String dsLookupOwnerId = message
    						.getString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID);
				 */
			log.debug("domain id" + dsLookupDomainId + " "
					+ dsLookupProjectId + " " + dsLookupOwnerId
					+ " *********************");

			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
					dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId);

			/*
			 * DataSourceLookupHelper dataSourceHelper = new
			 * DataSourceLookupHelper(); DataSourceLookup dsLookup =
			 * dataSourceHelper.matchDataSource( dsLookupDomainId,
			 * dsLookupProjectId, dsLookupOwnerId);
			 */

			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();

			SetFinderDAOFactory sfDAOFactory = daoFactory
					.getSetFinderDAOFactory();
			DataSourceLookup dsLookup = sfDAOFactory.getDataSourceLookup();
			log.debug("ORIG domain id"
					+ sfDAOFactory.getOriginalDataSourceLookup()
					.getDomainId()
					+ " ORIG "
					+ sfDAOFactory.getOriginalDataSourceLookup()
					.getProjectPath()
					+ " ORIG "
					+ sfDAOFactory.getOriginalDataSourceLookup()
					.getOwnerId());

			try { 
				AuthrizationHelper authHelper = new AuthrizationHelper(dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId, daoFactory);
				authHelper.checkRoleForProtectionLabel("SETFINDER_QRY_WITH_LGTEXT");
			} catch(I2B2Exception i2b2Ex) {
				allowLargeTextValueConstrainFlag = false;
			}

			//try {
			// check if the status is cancelled
			IQueryInstanceDao queryInstanceDao = sfDAOFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			int queryStatusId = queryInstance.getQtQueryStatusType()
					.getStatusTypeId();
			if (queryStatusId == 9) {
				log
				.info("Ignoring this query, query status was 'Cancelled'");
				//check end date, if not set set now
				if (queryInstance.getEndDate() == null)
				{


					queryInstance.setEndDate(new Date(System
							.currentTimeMillis()));
					queryInstanceDao.update(queryInstance, false);										

				}
			} else {
				// set the query instance batch mode to queue name
				queryInstance.setBatchMode(this.callingMDBName);
				//queryInstance.setEndDate(new Date(System
				//		.currentTimeMillis()));
				queryInstanceDao.update(queryInstance, false);

				log.debug("ExecRunnable my pmXml is" + pmXml);
				// process the query request
				patientSetId = processQueryRequest(
						transactionTimeout, dsLookup, sfDAOFactory,
						xmlRequest, sqlString, sessionId,
						queryInstanceId, patientSetId,allowLargeTextValueConstrainFlag, pmXml);
				log
				.debug("QueryExecutorMDB completed processing query instance ["
						+ queryInstanceId + "]");
				// finally send reply to queue
				//		sendReply(sessionId, patientSetId, "", replyToQueue);
			}

			//			} catch (CRCTimeOutException daoEx) {
			// catch this error and ignore. send general reply message.
			//				log.error(daoEx.getMessage(), daoEx);
			//				if (callingMDBName.equalsIgnoreCase(LARGE_QUEUE)) {
			//				transaction.begin();
			// set status to error
			//					setQueryInstanceStatus(sfDAOFactory, queryInstanceId,
			//							10,
			//							"Could not complete the query in the large queue with transaction timeout "
			//									+ transactionTimeout);
			//					transaction.commit();

			//				} else {

			// send message to next queue and if the there is no
			// next queue then update query instance to error
			//				tryNextQueue(sfDAOFactory, sessionId, message,
			//						queryInstanceId);

			//    					}
			/*	
    				} catch (I2B2DAOException daoEx) {
    					log.debug("got an error in ExecRunnable throwing: 1" + daoEx.getMessage() );
    					if (daoEx instanceof LockedoutException) {
    						log.debug("Lockedout happend"
    								+ daoEx.getMessage());
    						// message.
    						log.error(daoEx.getMessage(), daoEx);
    						// finally send reply to queue
    		//				sendReply(sessionId, patientSetId, daoEx.getMessage(),
    		//						replyToQueue);
    						throw(daoEx);
    					} else {
    						// catch this error and ignore. send general reply
    						// message.

    						log.debug("got an error in ExecRunnable throwing: 2");
    						log.error(daoEx.getMessage(), daoEx);

    						// finally send reply to queue
    	//					sendReply(sessionId, patientSetId, "", replyToQueue);
    						//MM Dont throw?  throw(daoEx);
    					}
    				}
			 */
			//}
			// setFinishedFlag(true);


			//  outputString = reqHandler.execute();
			setJobCompleteFlag(true);
			log.debug("Finished Running Query, my queryResultId is : " + queryResultInstanceId);

			returnMap.put(QueryManagerBeanUtil.QUERY_STATUS_PARAM, "DONE");
			returnMap.put(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM, queryResultInstanceId);
		}
		catch (CRCTimeOutException daoEx) {
			// catch this error and ignore. send general reply message.
			log.error(daoEx.getMessage(), daoEx);

			returnMap.put(QueryManagerBeanUtil.QUERY_STATUS_PARAM, "ERROR");
			returnMap.put(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM, queryResultInstanceId);
			setJobCompleteFlag(false);

			//				if (callingMDBName.equalsIgnoreCase(LARGE_QUEUE)) {
			//				transaction.begin();
			// set status to error
			//					setQueryInstanceStatus(sfDAOFactory, queryInstanceId,
			//							10,
			//							"Could not complete the query in the large queue with transaction timeout "
			//									+ transactionTimeout);
			//					transaction.commit();

			//				} else {

			// send message to next queue and if the there is no
			// next queue then update query instance to error
			//				tryNextQueue(sfDAOFactory, sessionId, message,
			//						queryInstanceId);

			//			}



		} catch (Exception e) {
			returnMap.put(QueryManagerBeanUtil.QUERY_STATUS_PARAM, "ERROR");
			returnMap.put(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM, queryResultInstanceId);
			setJobCompleteFlag(true);
			//setJobException(e);
			log.error("Got an excpetion in ExecRunnable (RUN): " + e.getMessage());
			e.printStackTrace();
			//throw(e);
		}

		//notify();
	}

	/**/
	public ExecRunnable(int transactionTimeout,
			String callingMDBName, MapMessage message, String sessionId) {
		this.transactionTimeout = transactionTimeout;
		this.callingMDBName = callingMDBName;
		this.message = message;
		this.sessionId = sessionId;
	}

	public ExecRunnable(String sqlString, String queryInstanceId, String patientSetId ,
			String xmlRequest, String dsLookupDomainId, String dsLookupProjectId ,
			String dsLookupOwnerId, String pmXml ) throws Exception {

		this.sqlString = sqlString;
		this.queryInstanceId =  queryInstanceId;
		this.patientSetId =  patientSetId;
		this.xmlRequest =  xmlRequest;
		this.dsLookupDomainId =  dsLookupDomainId;
		this.dsLookupProjectId = dsLookupProjectId;
		this.dsLookupOwnerId =  dsLookupOwnerId;
		this.pmXml = pmXml;


	}

	public void execute(String sqlString, String queryInstanceId, String patientSetId ,
			String xmlRequest, String dsLookupDomainId, String dsLookupProjectId ,
			String dsLookupOwnerId ) throws Exception {

		this.sqlString = sqlString;
		this.queryInstanceId =  queryInstanceId;
		this.patientSetId =  patientSetId;
		this.xmlRequest =  xmlRequest;
		this.dsLookupDomainId =  dsLookupDomainId;
		this.dsLookupProjectId = dsLookupProjectId;
		this.dsLookupOwnerId =  dsLookupOwnerId;

	}

	/*
	private void sendReply(String sessionId, String patientSetId,
			String message, Queue replyToQueue) throws JMSException,
			ServiceLocatorException {
		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender sender = null;
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			ServiceLocator serviceLocator = ServiceLocator.getInstance();
			conn = serviceLocator.getQueueConnectionFactory(
					QueryManagerBeanUtil.QUEUE_CONN_FACTORY_NAME)
					.createQueueConnection();
			session = conn.createQueueSession(false,
					javax.jms.Session.AUTO_ACKNOWLEDGE);
			MapMessage mapMessage = session.createMapMessage();
			// mapMessage.setString("para1", responseXML);
			log.debug("message session id " + sessionId);
			mapMessage.setJMSCorrelationID(sessionId);
			mapMessage.setString(
					QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM,
					patientSetId);
			mapMessage.setString(QueryManagerBeanUtil.QUERY_STATUS_PARAM,
					message);
			sender = session.createSender(replyToQueue);
			sender.send(mapMessage);
		} catch (JMSException jmse) {
			throw jmse;
		} finally {
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
	//		qmBeanUtil.closeAll(sender, null, conn, session);
		}

	}


	private void tryNextQueue(SetFinderDAOFactory sfDAOFactory,
			String sessionId, MapMessage msg, String queryInstanceId)
			throws JMSException, ServiceLocatorException {
		String jmsQueueName = null;

		// check which queue is this
		if (callingMDBName.equalsIgnoreCase(SMALL_QUEUE)) {
			// set status to running
			jmsQueueName = QueryManagerBeanUtil.MEDIUM_QUEUE_NAME;
			// this.setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 7,
			// "Queued in MEDIUM queue");
		} else if (callingMDBName.equalsIgnoreCase(MEDIUM_QUEUE)) {
			// set status to running
			jmsQueueName = QueryManagerBeanUtil.LARGE_QUEUE_NAME;
			// this.setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 8,
			// "Queued in LARGE queue");
		}

		if (jmsQueueName != null) {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			ServiceLocator serviceLocator = ServiceLocator.getInstance();
			QueueConnection conn = serviceLocator.getQueueConnectionFactory(
					QueryManagerBeanUtil.QUEUE_CONN_FACTORY_NAME)
					.createQueueConnection();
			Queue responseQueue = serviceLocator
					.getQueue(QueryManagerBeanUtil.RESPONSE_QUEUE_NAME);
			Queue sendQueue = serviceLocator.getQueue(jmsQueueName);

			QueueSession session = conn.createQueueSession(false,
					javax.jms.Session.AUTO_ACKNOWLEDGE);
			String id = sessionId;
			String selector = "JMSCorrelationID='" + id + "'";
			QueueSender sender = session.createSender(sendQueue);
			MapMessage mapMsg = session.createMapMessage();
			mapMsg.setJMSCorrelationID(id);
			mapMsg.setJMSReplyTo(responseQueue);

			mapMsg.setString(QueryManagerBeanUtil.XML_REQUEST_PARAM, msg
					.getString(QueryManagerBeanUtil.XML_REQUEST_PARAM));
			mapMsg
					.setString(
							QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM,
							msg
									.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM, msg
					.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM));
			mapMsg
					.setString(
							QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM,
							msg
									.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID, msg
					.getString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID, msg
					.getString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID, msg
					.getString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID));

			sender.send(mapMsg);
		}
	}
	 */

	private String processQueryRequest(
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String xmlRequest,
			String sqlString, String sessionId, String queryInstanceId,
			String patientSetId, boolean allowLargeTextValueConstrainFlag, String pmXml) throws I2B2DAOException, I2B2Exception, JAXBUtilException {

		// QueryRequestDao queryRequestDao = new QueryRequestDao();
		// returnedPatientSetId =
		// queryRequestDao.getPatientCount(queryRequestXml,
		// queryInstanceId,patientSetId);
		QueryDefinitionRequestType qdRequestType = getQueryDefinitionRequestType(xmlRequest);
		ResultOutputOptionListType resultOutputList = qdRequestType
				.getResultOutputList();
		DataSource dataSource = ServiceLocator.getInstance()
				.getAppServerDataSource(dsLookup.getDataSource());

		QueryExecutorDao queryExDao = new QueryExecutorDao(dataSource,
				dsLookup, sfDAOFactory.getOriginalDataSourceLookup());

		//see if the query will be direct query(no temp table)
		boolean queryWithoutTempTableFlag = false;

		PsmQryHeaderType psmQryHeaderType = getSetfinderRequestHeaderType(xmlRequest);
		if (psmQryHeaderType != null && psmQryHeaderType.getQueryMode() != null) {
			String queryMode = psmQryHeaderType.getQueryMode().value();

			if (queryMode.equals(QueryModeType.OPTIMIZE_WITHOUT_TEMP_TABLE.value())) {
				log.debug("Setfinder query header has [<query_mode>optimize_without_temp_table</query_mode>]");
				CheckSkipTempTable checkSkipTempTable = new CheckSkipTempTable(); 
				queryWithoutTempTableFlag = checkSkipTempTable.getSkipTempTable(qdRequestType, resultOutputList);
				log.debug("Sefinder query without temp table flag [" + queryWithoutTempTableFlag +"]");
			} else { 
				log.debug("Setfinder query header doesnt have [<query_mode>optimize_without_temp_table</query_mode>]");
			}
		} else { 
			log.debug("Setfinder query request header <psmheader> is null");
		}
		queryExDao.setQueryWithoutTempTableFlag(queryWithoutTempTableFlag);

		queryExDao.executeSQL( transactionTimeout, dsLookup,
				sfDAOFactory, xmlRequest, sqlString, queryInstanceId,
				patientSetId, resultOutputList,allowLargeTextValueConstrainFlag, pmXml);

		return patientSetId;
	}

	private QueryDefinitionRequestType getQueryDefinitionRequestType(
			String xmlRequest) throws I2B2Exception {
		String queryName = null;
		QueryDefinitionType queryDef = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = null;
		QueryDefinitionRequestType queryDefReqType = null;
		try {
			jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

			if (jaxbElement == null) {
				throw new I2B2Exception(
						"null value in after unmarshalling request string ");
			}

			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();
			requestMessageType.getMessageHeader().getSecurity();
			requestMessageType.getMessageHeader().getProjectId();


			BodyType bodyType = requestMessageType.getMessageBody();
			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			queryDefReqType = (QueryDefinitionRequestType) unWrapHelper
					.getObjectByClass(bodyType.getAny(),
							QueryDefinitionRequestType.class);
		} catch (JAXBUtilException e) {
			log.error(e.getMessage(), e);
			throw new I2B2Exception(e.getMessage(), e);
		}
		return queryDefReqType;

	}

	private PsmQryHeaderType getSetfinderRequestHeaderType(
			String xmlRequest) throws I2B2Exception {
		String queryName = null;
		QueryDefinitionType queryDef = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = null;
		PsmQryHeaderType psmHeaderType = null;
		try {
			jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

			if (jaxbElement == null) {
				throw new I2B2Exception(
						"null value in after unmarshalling request string ");
			}

			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();
			BodyType bodyType = requestMessageType.getMessageBody();
			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			psmHeaderType = (PsmQryHeaderType) unWrapHelper
					.getObjectByClass(bodyType.getAny(),
							PsmQryHeaderType.class);

		} catch (JAXBUtilException e) {
			log.error(e.getMessage(), e);
			throw new I2B2Exception(e.getMessage(), e);
		}
		return psmHeaderType;

	}


	private void setQueryInstanceStatus(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, int statusTypeId, String message) throws I2B2DAOException {

		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(queryInstanceId);

		QtQueryStatusType queryStatusType = new QtQueryStatusType();
		queryStatusType.setStatusTypeId(statusTypeId);
		queryInstance.setQtQueryStatusType(queryStatusType);
		queryInstance.setEndDate(new Date(System.currentTimeMillis()));
		queryInstance.setMessage(message);
		queryInstance.setBatchMode(callingMDBName);
		queryInstanceDao.update(queryInstance, true);
	}

	/*
	private int readTimeoutPropertyValue(String queueType) {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String timeoutStr = "";
		int timeoutVal = 0;
		try {
			if (queueType.equals(SMALL_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.small.timeoutsec");
			} else if (queueType.equals(MEDIUM_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.medium.timeoutsec");
			} else if (queueType.equals(LARGE_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.large.timeoutsec");
			}
			timeoutVal = Integer.parseInt(timeoutStr);

		} catch (I2B2Exception ex) {
			ex.printStackTrace();
		}
		return timeoutVal;

	}
	 */
}
