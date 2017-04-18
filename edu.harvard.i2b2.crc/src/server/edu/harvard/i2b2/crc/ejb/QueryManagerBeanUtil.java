/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.CheckSkipTempTable;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryExecutorDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.i2b2message.InfoType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PollingUrlType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryModeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryManagerBeanUtil {

	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	public final static String RESPONSE_QUEUE_NAME = "queue/jms.querytool.QueryResponse";
	// public final static String UPLOADPROCESSOR_QUEUE_NAME =
	// "queue/jms.querytool.QueryExecutor";

	public final static String SMALL_QUEUE_NAME = "queue/jms.querytool.QueryExecutorSmall";
	public final static String MEDIUM_QUEUE_NAME = "queue/jms.querytool.QueryExecutorMedium";
	public final static String LARGE_QUEUE_NAME = "queue/jms.querytool.QueryExecutorLarge";

	public final static String QUEUE_CONN_FACTORY_NAME = "ConnectionFactory";

	public final static String QUERY_MASTER_GENERATED_SQL_PARAM = "QUERY_MASTER_GENERATED_SQL_PARAM";
	public final static String QUERY_INSTANCE_ID_PARAM = "QUERY_INSTANCE_ID_PARAM";
	public final static String QUERY_PATIENT_SET_ID_PARAM = "QUERY_PATIENT_SET_ID_PARAM";
	public final static String XML_REQUEST_PARAM = "XML_REQUEST_PARAM";

	public final static String DS_LOOKUP_DOMAIN_ID = "DS_LOOKUP_DOMAIN_ID";
	public final static String DS_LOOKUP_PROJECT_ID = "DS_LOOKUP_PROJECT_ID";
	public final static String DS_LOOKUP_OWNER_ID = "DS_LOOKUP_OWNER_ID";

	public static final String QUEUED = "QUEUED";
	public static final String ERROR = "ERROR";
	public static final String FINISHED = "FINISHED";
	public static final String PROCESSING = "PROCESSING";
	public static final String SMALL_QUEUE = "SMALL_QUEUE";
	public static final String MEDIUM_QUEUE = "MEDIUM_QUEUE";
	public static final String LARGE_QUEUE = "LARGE_QUEUE";
	public static final String MEDIUM_QUEUE_RUNNING = "MEDIUM_QUEUE_RUNNING";
	public static final String LARGE_QUEUE_RUNNING = "LARGE_QUEUE_RUNNING";
	public static final String RUNNING = "RUNNING";
	public final static String QUERY_STATUS_PARAM = "QUERY_STATUS_PARAM";
	public final static String QT_QUERY_RESULT_INSTANCE_ID_PARAM = "QT_QUERY_RESULT_INSTANCE_ID_PARAM";


	public QueryManagerBeanUtil() {

	}

	public Map testSend(String domainId, String projectId, String ownerId,
			String generatedSql, String sessionId, String queryInstanceId,
			String patientSetId, String xmlRequest, long timeout)
					throws Exception {
		String status = null;
		int queryResultInstanceId = 0;

		log.debug("in testSend");
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		ServiceLocator serviceLocator = ServiceLocator.getInstance();
	

		long waitTime = getTimeout(xmlRequest);

		ExecRunnable exec = new ExecRunnable();

		exec.execute(generatedSql, queryInstanceId, patientSetId, xmlRequest,
				domainId, projectId, ownerId, waitTime);


		Thread t = new Thread(exec);


		synchronized (t) {
			t.start();

			try {
				//if (waitTime > 0) {
				//	t.wait(waitTime);
				//} else {
				//	t.wait();
				//}
				
				// the exec is running for time  = request timeout (waitTime)
				// wait a little longer (+10 seconds) to give exec chance to finish before deciding about queue
			//	long waitTimePlus = waitTime + 10 ;
				long startTime = System.currentTimeMillis(); 
				long deltaTime = -1; 
				while((exec.isJobCompleteFlag() == false)&& (deltaTime < waitTime)){ 
					if (waitTime > 0) { 
						t.wait(waitTime - deltaTime); 
						deltaTime = System.currentTimeMillis() - startTime; 
					} else { 
						t.wait(); 
					} 
				} 
				
				if (exec.isJobCompleteFlag() == false) {
					String timeOuterror = "Result waittime millisecond <result_waittime_ms> :" +
					waitTime +
					" elapsed, setting to next queue";
					log.debug(timeOuterror);
					
					DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
    						domainId, projectId, ownerId);

					IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();

    				SetFinderDAOFactory sfDAOFactory = daoFactory
    						.getSetFinderDAOFactory();
    				//DataSourceLookup dsLookup = sfDAOFactory.getDataSourceLookup();
    	
					IQueryInstanceDao queryInstanceDao = sfDAOFactory
							.getQueryInstanceDAO();
    				QtQueryInstance queryInstance = queryInstanceDao
							.getQueryInstanceByInstanceId(queryInstanceId);
		
    				queryInstance.setBatchMode(MEDIUM_QUEUE);
					//queryInstance.setEndDate(new Date(System
					//		.currentTimeMillis()));
					queryInstanceDao.update(queryInstance, false);
					
					log.debug("QMgrBean: Set interactive query to MEDIUM Queue; status = running");
							Map returnMap = new HashMap();
							returnMap.put(QUERY_STATUS_PARAM, "RUNNING");
							int id =  Integer.parseInt(queryInstanceId);
							returnMap.put(QT_QUERY_RESULT_INSTANCE_ID_PARAM, id);
							return returnMap;
    				//throw new Exception("Timed Out, setting to MEDIUM Queue");
				} 
			}
			catch (InterruptedException e) {
				log.error("Error in thread: " + e.getMessage());

				e.printStackTrace();
				throw new I2B2Exception("Thread error while running CRC job " 
						, e);
			} finally {
				
			//	t.interrupt();
				//exec = null;
				t = null;
			}
		}

		//		closeAll(sender, null, conn, session);
		//		closeAll(null, rcvr, conn1, recvSession);
		// closeAllTopic(rcvr,conn1,recvSession);
		//MM 
		//		Map returnMap = new HashMap();
		//		returnMap.put(QUERY_STATUS_PARAM, status);
		//		returnMap.put(QT_QUERY_RESULT_INSTANCE_ID_PARAM, queryResultInstanceId);
		//		return returnMap;
		return exec.getResult();
	}



	/*
	 * public String buildQueryRequestResponse(String requestXml, String
	 * status,String sessionId,String masterId, String
	 * queryInstanceId,ResultResponseType resultResponseType) throws Exception {
	 * return buildObject(requestXml, status, sessionId,
	 * masterId,queryInstanceId,resultResponseType ); }
	 */
	public void writerRepsonseFile(String sessionId, long recordCount) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(sessionId));
			bw.write(String.valueOf(recordCount));
			bw.close();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	public long getTimeout(String xmlRequest) throws Exception {
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();

		RequestHeaderType requestHeader = requestMessageType.getRequestHeader();
		long timeOut = 1;
		if (requestHeader != null && requestHeader.getResultWaittimeMs() > -1) {
			timeOut = requestHeader.getResultWaittimeMs();
		}
		return timeOut;
	}

	public DataSourceLookup getDataSourceLookupInput(String xmlRequest)
			throws Exception {
		DataSourceLookup dsLookupInput = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		String projectId = requestMessageType.getMessageHeader().getProjectId();
		String domainId = requestMessageType.getMessageHeader().getSecurity()
				.getDomain();
		String ownerId = requestMessageType.getMessageHeader().getSecurity()
				.getUsername();
		dsLookupInput = new DataSourceLookup();
		dsLookupInput.setProjectPath(projectId);
		dsLookupInput.setDomainId(domainId);
		dsLookupInput.setOwnerId(ownerId);
		return dsLookupInput;

	}

	public String getStatus(String sessionId) {
		String status = "UNKNOWN";
		// check directory
		File file = new File(sessionId);
		if (file.exists()) {
			status = "DONE";
		} else {
			//TODO removed JMS
			//			QueueConnection conn1 = null;
			//			Queue responseQueue = null;
			//			QueueSession recvSession = null;
			//			QueueReceiver rcvr = null;
			try {
				// check jms
				QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
				ServiceLocator serviceLocator = ServiceLocator.getInstance();
		
			} catch (Exception e) {
				status = "ERROR";
				e.printStackTrace();
			} finally {
				//	closeAll(null, rcvr, conn1, recvSession);
			}

		}
		return status;

	}

	public String buildGetQueryResultResponse(String sessionId, String status) {
		StringWriter strWriter = new StringWriter();
		// build infotype
		InfoType infoType = new InfoType();
		infoType.setUrl("");
		infoType.setValue("");

		ResultStatusType rsType = new ResultStatusType();
		StatusType st = new StatusType();
		st.setType(sessionId);
		st.setValue(status);
		rsType.setStatus(st);

		PollingUrlType pollUrlType = new PollingUrlType();
		pollUrlType
		.setValue("http://localhost:9093/queryProcessor/checkStatus");
		rsType.setPollingUrl(pollUrlType);

		// MessageType messageType = dtoFactory.buildMessageType(infoType,
		// rsType);
		ResponseHeaderType responseHeader = new ResponseHeaderType();
		responseHeader.setInfo(infoType);
		responseHeader.setResultStatus(rsType);
		ResponseMessageType responseMessageType = new ResponseMessageType();
		responseMessageType.setResponseHeader(responseHeader);

		try {
			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
			jaxbUtil.marshaller(of.createResponse(responseMessageType),
					strWriter);
			log.debug("i2b2 Response XML " + strWriter.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strWriter.toString();
	}

}
