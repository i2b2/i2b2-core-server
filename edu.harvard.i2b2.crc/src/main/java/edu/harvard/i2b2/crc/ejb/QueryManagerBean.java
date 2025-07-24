/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryInstanceSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryResultInstanceSpringDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainDateTimeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType.Condition;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.delegate.pm.CallPMUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * Ejb manager class for query operation
 * 
 * @author rkuttan
 * 
 * @ejb.bean description="QueryTool Query Manager"
 *           display-name="QueryTool Query Manager"
 *           jndi-name="ejb.querytool.QueryManager"
 *           local-jndi-name="ejb.querytool.QueryManagerLocal"
 *           name="querytool.QueryManager" type="Stateless" view-type="both"
 *           transaction-type="Bean"
 * 
 * @ejb.transaction type="Required"
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryManagerRemote"
 * 
 * 
 */
public class QueryManagerBean{ // implements SessionBean {
	private static Log log = LogFactory.getLog(QueryManagerBean.class);
	public static String RESPONSE_QUEUE_NAME = "queue/jms.querytool.QueryResponse";

	protected static Log logesapi = LogFactory.getLog(QueryManagerBean.class);


	// public static String UPLOADPROCESSOR_QUEUE_NAME =
	// "queue/jms.querytool.QueryExecutor";

	//	SessionContext context;

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            publish request XML fileName
	 * 
	 * @return String publish response XML
	 * @throws Exception 
	 */
	public MasterInstanceResultResponseType processQuery(
			DataSourceLookup dataSourceLookup, String xmlRequest)
					throws Exception {
		String responseXML = null;

		MasterInstanceResultResponseType masterInstanceResultType = null;

		try {
			String sessionId = String.valueOf(System.currentTimeMillis());
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			long timeout = qmBeanUtil.getTimeout(xmlRequest);

			DataSourceLookup dsLookupInput = qmBeanUtil
					.getDataSourceLookupInput(xmlRequest);
			SetFinderDAOFactory sfDAOFactory = null;
			// tm.begin();
			//			transaction.begin();
			if (dsLookupInput.getProjectPath() == null) {
				throw new I2B2Exception("project id is missing in the request");
			}
			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
					dsLookupInput.getDomainId(),
					dsLookupInput.getProjectPath(), dsLookupInput.getOwnerId());
			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			sfDAOFactory = daoFactory.getSetFinderDAOFactory();

			String generatedSql = null;
			RequestMessageType requestMsgType = this.getI2B2RequestType(xmlRequest);
			String queryMasterId = saveQuery(sfDAOFactory, requestMsgType,
					generatedSql);

			// create query instance
			IQueryInstanceDao queryInstanceDao = sfDAOFactory
					.getQueryInstanceDAO();

			UserType userType = getUserTypeFromSetfinderHeader(requestMsgType);
			String userId = userType.getLogin();
			String groupId = userType.getGroup();
			String queryInstanceId = queryInstanceDao.createQueryInstance(
					queryMasterId, userId, groupId,
					"QUEUED", 5);
			//					QueryExecutorMDB.SMALL_QUEUE, 5);
			log.debug("New Query instance id " + queryInstanceId);

			IQueryResultInstanceDao patientSetResultDao = sfDAOFactory
					.getPatientSetResultDAO();
			String patientSetId = null;
			QueryDefinitionRequestType queryDefRequestType = getQueryDefinitionRequestType(requestMsgType);
			ResultOutputOptionListType resultOptionList = queryDefRequestType
					.getResultOutputList();

			if (resultOptionList != null
					&& resultOptionList.getResultOutput() != null
					&& resultOptionList.getResultOutput().size() > 0) {
				for (ResultOutputOptionType resultOption : resultOptionList
						.getResultOutput()) {

					patientSetId = patientSetResultDao.createPatientSet(
							queryInstanceId, resultOption.getName());
					logesapi.debug("Patient Set ID [" + patientSetId
							+ "] for query instance= " + queryInstanceId);
				}
			} else {
				QueryProcessorUtil qp = QueryProcessorUtil.getInstance();
				//BeanFactory bf = qp.getSpringBeanFactory();
				String defaultResultType = "PATIENTSET";
				patientSetId = patientSetResultDao.createPatientSet(
						queryInstanceId, defaultResultType);
				logesapi.debug("Patient Set ID [" + patientSetId
						+ "] for query instance= " + queryInstanceId);
			}

			// tm.commit();
			//			transaction.commit();
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(queryInstanceId);


			queryInstance.setBatchMode(QueryManagerBeanUtil.PROCESSING);
			log.debug("getting responsetype");
			ResultResponseType responseType = executeSqlInQueue(
					dsLookupInput.getDomainId(),
					dsLookupInput.getProjectPath(), dsLookupInput.getOwnerId(),
					userId, generatedSql, sessionId, queryInstanceId,
					patientSetId, xmlRequest, timeout);

			//			transaction.begin();
			// responseXML = qmBeanUtil.buildQueryRequestResponse(xmlRequest,
			// status,
			// sessionId,queryMasterId,queryInstanceId,responseType);
			log.debug("after queue exectution");

			/*
			 * query instance status is updated in the query executor class
			 * QtQueryInstance queryInstance = updateQueryInstanceStatus(
			 * sfDAOFactory, responseType, userId, queryInstanceId);
			 */

			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			QtQueryMaster queryMaster = queryMasterDao
					.getQueryDefinition(queryMasterId);
			masterInstanceResultType = new MasterInstanceResultResponseType();

			QueryMasterType queryMasterType = PSMFactory
					.buildQueryMasterType(queryMaster);
			// set query master
			masterInstanceResultType.setQueryMaster(queryMasterType);

			// fetch query instance by queryinstance id and build response
			//QtQueryInstance queryInstance = queryInstanceDao
			//		.getQueryInstanceByInstanceId(queryInstanceId);
			QueryInstanceType queryInstanceType = PSMFactory
					.buildQueryInstanceType(queryInstance);
			// set query instance
			masterInstanceResultType.setQueryInstance(queryInstanceType);
			// set status
			masterInstanceResultType.setStatus(responseType.getStatus());

			QueryResultBean queryResultBean = new QueryResultBean();
			ResultResponseType responseType1 = queryResultBean
					.getResultInstanceFromQueryInstanceId(dataSourceLookup,
							userId, queryInstanceId, false);

			log.debug("Size of result when called thru ejb "
					+ responseType1.getQueryResultInstance().size());

			//If query result instanace -> query_status_type is processing that set QUEUE
			//Determine if any resultinstance is type ERROR or PROCSSING
			boolean processing = false;
			boolean error = false;
			if (responseType1.getQueryResultInstance() != null) 
				for (QueryResultInstanceType resultType :responseType1.getQueryResultInstance())
				{
					if (resultType.getQueryStatusType().getStatusTypeId().equals("1"))
						processing = true;
					if (resultType.getQueryStatusType().getStatusTypeId().equals("2"))
						processing = true;
					if (resultType.getQueryStatusType().getStatusTypeId().equals("4"))
						error = true;
					if (resultType.getQueryStatusType().getStatusTypeId().equals("5"))
						error = true;

				}
			/*
			if (responseType1.getQueryResultInstance() != null && processing== true) //responseType1.getQueryResultInstance().get(0).getQueryStatusType().getStatusTypeId().equals("2"))
			{
				responseType1.getQueryResultInstance().get(0).getQueryStatusType().setStatusTypeId("1");			
				responseType1.getQueryResultInstance().get(0).getQueryStatusType().setName("QUEUED");
				responseType1.getQueryResultInstance().get(0).getQueryStatusType().setDescription("WAITING IN QUEUE TO START PROCESS");
				StatusType stype = new StatusType();
				Condition e = new Condition();
				e.setType("RUNNING");
				e.setValue("RUNNING");
				stype.getCondition().add(e);
				responseType1.setStatus(stype);
			} else 
			*/
			if 	(responseType1.getQueryResultInstance() != null && error == false && processing == false) //responseType1.getQueryResultInstance().get(0).getQueryStatusType().getStatusTypeId().equals("3"))
			{
				//	QueryStatusTypeType status = queryInstanceType.getQueryStatusType();
				//	status.setStatusTypeId("3");
				//	status.setDescription("FINISHED");
				//	status.setName("FINISHED");
				//	queryInstanceType.setQueryStatusType(status);

				QtQueryStatusType status1 = queryInstance.getQtQueryStatusType();
				status1.setStatusTypeId(3);
				status1.setName("DONE");
				status1.setDescription("DONE");
				queryInstance.setQtQueryStatusType(status1);
				//masterInstanceResultType.setQueryInstance(queryInstanceType);

				
				if  (queryMaster.getMasterTypeCd() != null && queryMaster.getMasterTypeCd().equals("EXPORT"))
					queryInstance.setBatchMode(QueryManagerBeanUtil.SUBMITTED);
				else
					queryInstance.setBatchMode(QueryManagerBeanUtil.FINISHED);
				//queryInstance.setQueryInstanceId("3");

				queryInstance.setEndDate(new Date(System
						.currentTimeMillis()));
				queryInstanceDao.update(queryInstance, true);
			} else if 	(responseType1.getQueryResultInstance() != null && error == true) //responseType1.getQueryResultInstance().get(0).getQueryStatusType().getStatusTypeId().equals("4"))
			{

				//			QueryStatusTypeType status = queryInstanceType.getQueryStatusType();
				//			status.setStatusTypeId("4");
				//			status.setDescription("ERROR");
				//			status.setName("ERROR");
				//			queryInstanceType.setQueryStatusType(status);
				//			QtQueryStatusType status1 = queryInstance.getQtQueryStatusType();
				//			status1.setStatusTypeId(4);
				//			status1.setName("ERROR");
				//			status1.setDescription("ERROR");
				//			queryInstance.setQtQueryStatusType(status1);

				queryInstance.setBatchMode(QueryManagerBeanUtil.ERROR);
				queryInstance.setEndDate(new Date(System
						.currentTimeMillis()));
				queryInstanceDao.update(queryInstance, false);
				//SKIP this one and goto MEDIUM Queue
				/* */

			} else if (processing == true) //queryInstance.getBatchMode().equals("PROCESSING"))
			{
				QueryStatusTypeType status = queryInstanceType.getQueryStatusType();

				status.setStatusTypeId("10");
				status.setDescription("TIMEDOUT");
				status.setName("TIMEDOUT");
				queryInstanceType.setQueryStatusType(status);

				QtQueryStatusType status1 = queryInstance.getQtQueryStatusType();
				status1.setStatusTypeId(10);
				status1.setName("TIMEDOUT");
				status1.setDescription("TIMEDOUT");

				queryInstance.setQtQueryStatusType(status1);

				queryInstance.setBatchMode("MEDIUM_QUEUE");
				queryInstance.setEndDate(null);
				queryInstanceDao.update(queryInstance, false);

				responseType1.getQueryResultInstance().get(0).setQueryStatusType(status);

			} else if 	(responseType1.getQueryResultInstance() != null && error == true) //responseType1.getQueryResultInstance().get(0).getQueryStatusType().getStatusTypeId().equals("5"))
			{

				// why does 5 (incomplete) become error?	
				queryInstance.setBatchMode(QueryManagerBeanUtil.ERROR);

				//	QueryStatusTypeType status = queryInstanceType.getQueryStatusType();
				//	status.setStatusTypeId("4");
				//	status.setDescription("ERROR");
				//	status.setName("ERROR");
				//	queryInstanceType.setQueryStatusType(status);

				QtQueryStatusType status1 = queryInstance.getQtQueryStatusType();
				status1.setStatusTypeId(4);
				status1.setName("ERROR");
				status1.setDescription("ERROR");
				queryInstance.setQtQueryStatusType(status1);
				queryInstance.setEndDate(new Date(System
						.currentTimeMillis()));
				queryInstanceDao.update(queryInstance, false);
			}
			// set result instance
			masterInstanceResultType.getQueryResultInstance().addAll(
					responseType1.getQueryResultInstance());

			//refresh the queryInstance to reflect the updates above
			QueryInstanceType queryInstanceType2 = PSMFactory
					.buildQueryInstanceType(queryInstance);
			// set query instance
			masterInstanceResultType.setQueryInstance(queryInstanceType2);


		} catch (I2B2DAOException ex) {
			log.debug("Got an error in QueryManagerBean, thropwing: " + ex.getMessage());
			ex.printStackTrace();

			throw new I2B2Exception(ex.getMessage());
		}

		return masterInstanceResultType;
	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            userId
	 * @param int master id
	 * @param long timeout
	 * 
	 * @return InstanceResultResponseType
	 */
	public InstanceResultResponseType runQueryMaster(
			DataSourceLookup dataSourceLookup, String userId, String masterId,
			long timeout) throws I2B2Exception {
		return null;

	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            userId
	 * @param int master id
	 * @param long timeout
	 * 
	 * @return InstanceResultResponseType
	 */
	public InstanceResultResponseType runExportQueryInstance(
			DataSourceLookup dataSourceLookup, String userId, String quertyInstanceId,
			String xmlRequest) throws Exception {
		
		InstanceResultResponseType instanceResultResponse = new InstanceResultResponseType();

		try {
			String sessionId = String.valueOf(System.currentTimeMillis());
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			long timeout = qmBeanUtil.getTimeout(xmlRequest);

			DataSourceLookup dsLookupInput = qmBeanUtil
					.getDataSourceLookupInput(xmlRequest);
			SetFinderDAOFactory sfDAOFactory = null;
			// tm.begin();
			//			transaction.begin();
			if (dsLookupInput.getProjectPath() == null) {
				throw new I2B2Exception("project id is missing in the request");
			}
			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
					dsLookupInput.getDomainId(),
					dsLookupInput.getProjectPath(), dsLookupInput.getOwnerId());
			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			sfDAOFactory = daoFactory.getSetFinderDAOFactory();

			
			QueryInstanceSpringDao instanceSpringDao = new QueryInstanceSpringDao(sfDAOFactory.getDataSource(), sfDAOFactory.getDataSourceLookup());
			
			
			QueryResultInstanceSpringDao resultInstanceDao = new QueryResultInstanceSpringDao(sfDAOFactory.getDataSource(), sfDAOFactory.getDataSourceLookup());
			Set<QtQueryResultInstance>  setResult = new HashSet<>(); 
			
			//QueryInstanceType queryInstanceResponse = PSMFactory
			//		.buildQueryInstanceType(instanceSpringDao.getQueryInstanceByInstanceId(quertyInstanceId));
			//instanceResultResponse.setQueryInstance(queryInstanceResponse);

			for (QtQueryResultInstance r : resultInstanceDao.getResultInstanceList(quertyInstanceId))
			{
				if (r.getQtQueryResultType().getClassname().equals("edu.harvard.i2b2.crc.dao.setfinder.QueryResultPatientDownload"))
					r.getClass();
			}
				
	

		} catch (I2B2DAOException ex) {
			log.debug("Got an error in QueryManagerBean, thropwing: " + ex.getMessage());
			ex.printStackTrace();

			throw new I2B2Exception(ex.getMessage());
		}


		return instanceResultResponse;
	}
	
	private ResultResponseType executeSqlInQueue(String domainId,
			String projectId, String ownerId, String userId,
			String generatedSql, String sessionId, String queryInstanceId,
			String patientSetId, String xmlRequest, long timeout)
					throws Exception {

		QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();

		// process interactive query
		//	log.debug("process query in queue");
		Map returnValues = qmBeanUtil.testSend(domainId, projectId, ownerId,
				generatedSql, sessionId, queryInstanceId, patientSetId,
				xmlRequest, timeout);

		//	log.debug("My returnValue map is:" + returnValues);
		//	log.debug("My mapssize is: " + returnValues.size());
		// build response message, if query completed before given timeout
		String status = (String) returnValues
				.get(QueryManagerBeanUtil.QUERY_STATUS_PARAM);
		int queryResultInstanceId = (Integer) returnValues
				.get(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM);
		log.debug("Query Result Instance id " + queryResultInstanceId);

		log.debug("Status coming from queue : " + status);
		StatusType statusType = new StatusType();
		StatusType.Condition condition = new StatusType.Condition();
		condition.setValue(status);
		if (status != null && status.indexOf("LOCKEDOUT") > -1) {
			status = "ERROR";
		}
		condition.setType(status);
		statusType.getCondition().add(condition);
		ResultResponseType responseType = new ResultResponseType();
		responseType.setStatus(statusType);
		return responseType;
	}

	private String saveQuery(SetFinderDAOFactory sfDAOFactory,
			RequestMessageType i2b2RequestMsgType, String generatedSql) throws Exception {
		IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
		QtQueryMaster queryMaster = new QtQueryMaster();
		UserType userType = getUserTypeFromSetfinderHeader(i2b2RequestMsgType);
		String userId = userType.getLogin();
		String groupId = userType.getGroup();
		QueryDefinitionType queryDefType = getQueryDefinition(i2b2RequestMsgType);
		queryDefType.setQueryName(queryDefType.getQueryName().replaceAll("\\<[^>]*>",""));
		edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		if (userId.equalsIgnoreCase(qpUtil
				.getCRCPropertyValue("edu.harvard.i2b2.crc.pm.serviceaccount.user")))
			throw new Exception("Cannot run queries as servcice account: " + userId);
		queryMaster.setUserId(userId);

		//If time not set than set to 12:00

		for (PanelType panel: queryDefType.getPanel())
		{
			if (panel.getPanelDateFrom() != null && panel.getPanelDateFrom().getValue() != null) {
				ConstrainDateTimeType time = panel.getPanelDateFrom().getTime();
				if(time == null)
				{
					XMLGregorianCalendar fixTime = panel.getPanelDateFrom().getValue();
					fixTime.setSecond(0);
					fixTime.setHour(12);
					fixTime.setMinute(0);
					fixTime.setMillisecond(0);
				}
			}
			if (panel.getPanelDateTo() != null && panel.getPanelDateTo().getValue() != null) {
				ConstrainDateTimeType time = panel.getPanelDateTo().getTime();
				if(time == null)
				{
					XMLGregorianCalendar fixTime2 = panel.getPanelDateTo().getValue();
					fixTime2.setSecond(0);
					fixTime2.setHour(12);
					fixTime2.setMinute(0);
					fixTime2.setMillisecond(0);

				}
			}
		}


		StringWriter queryDefWriter = new StringWriter();
		CRCJAXBUtil.getQueryDefJAXBUtil().marshaller(
				of.createQueryDefinition(queryDefType), queryDefWriter);

		queryMaster.setRequestXml(queryDefWriter.toString());
		queryMaster.setGroupId(groupId);
		queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
		queryMaster.setDeleteFlag(QtQueryMaster.DELETE_OFF_FLAG);
		queryMaster.setGeneratedSql(generatedSql);
		queryMaster.setName(queryDefType.getQueryName());

		String pmXml = CallPMUtil.callUserResponse(i2b2RequestMsgType.getMessageHeader().getSecurity(), "");

		//remove user password form the request
		PasswordType passType = i2b2RequestMsgType.getMessageHeader().getSecurity().getPassword();
		passType.setValue("password not stored"); 
		passType.setIsToken(false);

		JAXBUtil util = CRCJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory i2b2ObjFactory = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		util.marshaller(i2b2ObjFactory.createRequest(i2b2RequestMsgType), strWriter);
		String queryMasterId = queryMasterDao.createQueryMaster(queryMaster,
				strWriter.toString(), pmXml);

		return queryMasterId;
	}

	private UserType getUserTypeFromSetfinderHeader(RequestMessageType requestMessageType)
			throws Exception {
		UserType userType = new UserType();
		userType.setLogin(requestMessageType.getMessageHeader().getSecurity()
				.getUsername());
		userType.setGroup(requestMessageType.getMessageHeader().getProjectId());

		return userType;
	}

	private RequestMessageType getI2B2RequestType(String xmlRequest)
			throws Exception {

		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

		if (jaxbElement == null) {
			throw new Exception(
					"null value in after unmarshalling request string ");
		}

		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();

		return requestMessageType;
	}

	private QueryDefinitionRequestType getQueryDefinitionRequestType(
			RequestMessageType requestMessageType) throws Exception {
		String queryName = null;
		QueryDefinitionType queryDef = null;

		BodyType bodyType = requestMessageType.getMessageBody();
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		QueryDefinitionRequestType queryDefReqType = (QueryDefinitionRequestType) unWrapHelper
				.getObjectByClass(bodyType.getAny(),
						QueryDefinitionRequestType.class);
		return queryDefReqType;

	}

	public QueryDefinitionType getQueryDefinition(RequestMessageType requestMessageType)
			throws Exception {
		QueryDefinitionRequestType queryDefReqType = getQueryDefinitionRequestType(requestMessageType);
		QueryDefinitionType queryDef = null;
		if (queryDefReqType != null) {
			queryDef = queryDefReqType.getQueryDefinition();
		}
		return queryDef;
	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param int session id publish request XML fileName
	 * 
	 * @return String publish response XML
	 */
	public String getResponseXML(String sessionId) {
		QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
		String status = qmBeanUtil.getStatus(sessionId);
		String response = qmBeanUtil.buildGetQueryResultResponse(sessionId,
				status);

		return response;
	}

	/*
	public void setSessionContext(SessionContext context) throws EJBException,
			RemoteException {
		this.context = context;
	}

	public void ejbCreate() throws CreateException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}
	 */
}
