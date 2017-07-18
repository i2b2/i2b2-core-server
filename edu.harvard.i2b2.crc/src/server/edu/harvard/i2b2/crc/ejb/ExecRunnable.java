package edu.harvard.i2b2.crc.ejb;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
//

	String sqlString = "";
	String queryInstanceId = "";
	String patientSetId = "";
	String xmlRequest = "";
	String dsLookupDomainId= "";
	String dsLookupProjectId ="";
	String dsLookupOwnerId = "";
	String pmXml = null;
	int transactionTimeout;
	
	Map returnMap = new HashMap();

	public ExecRunnable() {
	}

	private Exception ex = null;

	private String callingMDBName = QueryManagerBeanUtil.RUNNING, sessionId = "";
	// default timeout three minutes
//	int transactionTimeout = 0;

//	private volatile boolean running = true;

//	public void terminate() {
//		running = false;
//	}

	MapMessage message = null;
	private boolean jobCompleteFlag = false;
	private boolean jobErrorFlag = false;


	public boolean isJobErrorFlag() {
		return jobErrorFlag;
	}

	public void setJobErrorFlag(boolean jobErrorFlag) {
		this.jobErrorFlag = jobErrorFlag;
	}

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

	@Override
	public void run() {

			try {
				QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
				boolean allowLargeTextValueConstrainFlag = true;
				int queryResultInstanceId = 0;

				QtQueryInstance queryInstance = null;
				IQueryInstanceDao queryInstanceDao = null;
				try {

				
					log.debug("domain id" + dsLookupDomainId + " "
							+ dsLookupProjectId + " " + dsLookupOwnerId
							+ " *********************");

					DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
							dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId);

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
					queryInstanceDao = sfDAOFactory
							.getQueryInstanceDAO();
					queryInstance = queryInstanceDao
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
						if (queryInstance.getBatchMode().equals(QueryManagerBeanUtil.MEDIUM_QUEUE))
							queryInstance.setBatchMode(QueryManagerBeanUtil.MEDIUM_QUEUE_RUNNING);
						else if (queryInstance.getBatchMode().equals(QueryManagerBeanUtil.LARGE_QUEUE))
							queryInstance.setBatchMode(QueryManagerBeanUtil.LARGE_QUEUE_RUNNING);
						else
							queryInstance.setBatchMode(this.callingMDBName);

						queryInstanceDao.update(queryInstance, false);

						patientSetId = processQueryRequest(
								transactionTimeout, dsLookup, sfDAOFactory,
								xmlRequest, sqlString, sessionId,
								queryInstanceId, patientSetId,allowLargeTextValueConstrainFlag, pmXml);
		
					}

					//  outputString = reqHandler.execute();
					setJobCompleteFlag(true);
					log.info("Exec Finished Running Query");

					queryInstance.setBatchMode(QueryManagerBeanUtil.FINISHED);
					
					// status and end time were missing -- added during transactionTimeout fix
					QtQueryStatusType queryStatusType = queryInstance.getQtQueryStatusType();
					queryStatusType.setStatusTypeId(3);
					queryStatusType.setName("DONE");
					queryStatusType.setDescription("DONE");
					queryInstance.setQtQueryStatusType(queryStatusType);
					
					queryInstance.setEndDate(new Date(System
							.currentTimeMillis()));
					
					queryInstanceDao.update(queryInstance, false);
					
					returnMap.put(QueryManagerBeanUtil.QUERY_STATUS_PARAM, "DONE");
					returnMap.put(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM, queryResultInstanceId);
				}
				catch (CRCTimeOutException daoEx) {
					log.info("Caught query time out");
					// catch this error and ignore. send general reply message.
					setJobCompleteFlag(false);
			// following commented out during transaction timeout fix		
			//		setJobException(daoEx);
			//		setJobErrorFlag(true);
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						return;
					}

				} catch (Exception e) {
					if(e.getMessage() != null){
						log.error(e.getMessage());
						if(e.getMessage().contains("Interrupted")){
							log.info("Received interrupt");
							return;
						}
					}
					setJobException(e);
					setJobErrorFlag(true);
					
					if (queryInstance != null)
					{
						queryInstance.setBatchMode(QueryManagerBeanUtil.ERROR);
						QtQueryStatusType queryStatusType = queryInstance.getQtQueryStatusType();
						queryStatusType.setStatusTypeId(4);
						queryInstance.setQtQueryStatusType(queryStatusType);
						queryInstance.setEndDate(new Date(System
								.currentTimeMillis()));
						try {
							queryInstanceDao.update(queryInstance, false);
						} catch (Exception e2) {
							log.error("Problem updating query instance to ERROR " + e.getMessage());
						}
					}
					returnMap.put(QueryManagerBeanUtil.QUERY_STATUS_PARAM, "ERROR");
					returnMap.put(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM, queryResultInstanceId);
					setJobCompleteFlag(true);

					log.error("Job Exception: " + getJobException().getMessage());
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						;
					}
	
				}
			} catch (Exception e) {
				log.error("Exception", e);
				
			}

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
			String dsLookupOwnerId, String pmXml, int transactionTimeout) throws Exception {

		this.sqlString = sqlString;
		this.queryInstanceId =  queryInstanceId;
		this.patientSetId =  patientSetId;
		this.xmlRequest =  xmlRequest;
		this.dsLookupDomainId =  dsLookupDomainId;
		this.dsLookupProjectId = dsLookupProjectId;
		this.dsLookupOwnerId =  dsLookupOwnerId;
		this.pmXml = pmXml;

		this.transactionTimeout = transactionTimeout;


	}

	public void execute(String sqlString, String queryInstanceId, String patientSetId ,
			String xmlRequest, String dsLookupDomainId, String dsLookupProjectId ,
			String dsLookupOwnerId, long waitTimeMs ) throws Exception {

		this.sqlString = sqlString;
		this.queryInstanceId =  queryInstanceId;
		this.patientSetId =  patientSetId;
		this.xmlRequest =  xmlRequest;
		this.dsLookupDomainId =  dsLookupDomainId;
		this.dsLookupProjectId = dsLookupProjectId;
		this.dsLookupOwnerId =  dsLookupOwnerId;

		this.transactionTimeout = (int) (waitTimeMs/1000);
	}

	private String processQueryRequest(
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String xmlRequest,
			String sqlString, String sessionId, String queryInstanceId,
			String patientSetId, boolean allowLargeTextValueConstrainFlag, String pmXml) throws I2B2DAOException, I2B2Exception, JAXBUtilException {

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
