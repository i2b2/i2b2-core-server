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
 *     Wayne Chan
 */
package edu.harvard.i2b2.crc.delegate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DblookupDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.InfoType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PollingUrlType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.loader.delegate.pm.PMResponseMessage;
import edu.harvard.i2b2.crc.loader.delegate.pm.PMServiceDriver;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

/**
 * Top level class to process the request. There will be separate request handler
 * class for each request type. The main processing of for the request will be
 * done inside execute function 
 * 
 */
public abstract class JobReqHandler {
	protected final Log log = LogFactory.getLog(getClass());
	protected static Logger logesapi = ESAPI.getLogger(JobReqHandler.class);

	protected DataSourceLookup dataSourceLookup = null;

	/**
	 * Function to perform operation on the given request
	 * 
	 * @return response xml message
	 */
	public abstract String execute() throws I2B2Exception;
    
    public boolean isAdmin(MessageHeaderType header) {
		try {
			GetUserConfigurationType userConfigType = new GetUserConfigurationType();
			PMServiceDriver pmSrvDrvr = new PMServiceDriver();
			String response = pmSrvDrvr.getRoles(userConfigType, header);		
			logesapi.debug(null,response);
			PMResponseMessage msg = new PMResponseMessage();
			StatusType procStatus = msg.processResult(response);
			if(procStatus.getType().equals("ERROR")) return false;
			ConfigureType pmConfigure = msg.readUserInfo();
			if (pmConfigure.getUser().isIsAdmin()) return true;
		} catch (AxisFault e) {
				log.error("Can't connect to PM service");
		} catch (I2B2Exception e) {
				log.error("Problem processing PM service address");
		} catch (Exception e) {
				log.error("General PM processing problem: "+ e.getMessage());
		}
		return false;
    }        
    
    /**
	 * Class to fetch specific request message from i2b2 message xml
	 * 
	 * @param requestXml
	 * @param classname
	 * @return object which is of type classname
	 * @throws JAXBUtilException
	 */
	protected Object getRequestType(String requestXml, Class classname)
			throws JAXBUtilException {
		Object returnObject = null;

		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		BodyType bodyType = requestMessageType.getMessageBody();
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		// get request header type
		returnObject = unWrapHelper.getObjectByClass(bodyType.getAny(),
				classname);

		return returnObject;
	}

	/**
	 * Function to build response message type using given bodytype and request
	 * xml
	 * 
	 * @param requestXml
	 * @param bodyType
	 * @return i2b2 response message xml
	 * @throws JAXBUtilException
	 */
	protected String buildResponseMessage(String requestXml, BodyType bodyType)
			throws JAXBUtilException {
		JAXBUtil util = CRCJAXBUtil.getJAXBUtil();
		RequestMessageType requestMsgType = getI2B2RequestMessageType(requestXml);
		MessageHeaderType messageHeader = requestMsgType.getMessageHeader();

		// reverse sending and receiving app
		ApplicationType sendingApp = messageHeader.getSendingApplication();
		ApplicationType receiveApp = messageHeader.getReceivingApplication();
		messageHeader.setSendingApplication(receiveApp);
		messageHeader.setReceivingApplication(sendingApp);

		// set instance num
		MessageControlIdType messageControlIdType = messageHeader
				.getMessageControlId();

		if (messageControlIdType != null) {
			messageControlIdType.setInstanceNum(1);
		}

		StatusType statusType = new StatusType();
		statusType.setType("DONE");

		// :TODO statusType.setValue(sessionId);
		PollingUrlType pollingType = new PollingUrlType();
		pollingType.setIntervalMs(100);

		// :TODO value come from property file
		// pollingType.setValue(
		// "http://host:port/QueryProcessor/getResult");
		ResultStatusType resultStatusType = new ResultStatusType();
		resultStatusType.setStatus(statusType);
		resultStatusType.setPollingUrl(pollingType);

		InfoType infoType = new InfoType();
		// :TODO value come from property file
		//infoType.setUrl("http://host:port/QueryProcessor/getStatus");
		infoType.setValue("Log information");

		ResponseHeaderType responseHeader = new ResponseHeaderType();
		responseHeader.setResultStatus(resultStatusType);
		responseHeader.setInfo(infoType);

		ResponseMessageType responseMessageType = new ResponseMessageType();
		responseMessageType.setMessageHeader(messageHeader);
		responseMessageType.setResponseHeader(responseHeader);
		responseMessageType.setMessageBody(bodyType);

		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		StringWriter strWriter = new StringWriter();
		util.marshaller(of.createResponse(responseMessageType), strWriter);

		return strWriter.toString();
	}

	/**
	 * Function to unmarshall i2b2 request message type
	 * 
	 * @param requestXml
	 * @return RequestMessageType
	 * @throws JAXBUtilException
	 */
	protected RequestMessageType getI2B2RequestMessageType(String requestXml)
			throws JAXBUtilException {
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();

		return requestMessageType;
	}

	/**
	 * Function marshall i2b2 response message type
	 * 
	 * @param responseMessageType
	 * @return
	 */
	protected String getResponseString(ResponseMessageType responseMessageType) {
		StringWriter strWriter = new StringWriter();

		try {
			edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			jaxbUtil.marshaller(of.createResponse(responseMessageType),
					strWriter);
		} catch (JAXBUtilException e) {
			log.error("Error while generating response message"
					+ e.getMessage());
		}

		return strWriter.toString();
	}

	protected edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType buildCRCStausType(
			String statusType, String message) {
		edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType st = new edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType();
		edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType.Condition condition = new edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType.Condition();
		condition.setType(statusType);
		condition.setValue(message);
		st.getCondition().add(condition);
		return st;
	}

	protected void setDataSourceLookup(String requestXml)
			throws JAXBUtilException {
		RequestMessageType reqMessage = getI2B2RequestMessageType(requestXml);
		String projectId = reqMessage.getMessageHeader().getProjectId();
		String domainId = reqMessage.getMessageHeader().getSecurity()
				.getDomain();
		String userId = reqMessage.getMessageHeader().getSecurity()
				.getUsername();
		dataSourceLookup = new DataSourceLookup();
		dataSourceLookup.setProjectPath(projectId);
		dataSourceLookup.setDomainId(domainId);
		dataSourceLookup.setOwnerId(userId);

	}

	protected DataSourceLookup getDataSourceLookup() {
		return dataSourceLookup;
	}
    
}
