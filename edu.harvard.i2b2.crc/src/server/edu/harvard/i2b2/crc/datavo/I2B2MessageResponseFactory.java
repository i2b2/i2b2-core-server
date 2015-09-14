/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.datavo;

import java.io.StringWriter;
import java.math.BigDecimal;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.InfoType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PollingUrlType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class I2B2MessageResponseFactory {

	/** log * */
	protected final static Log log = LogFactory
			.getLog(I2B2MessageResponseFactory.class);

	/**
	 * Function to build response message type using given bodytype and request
	 * xml
	 * 
	 * @param requestXml
	 * @param bodyType
	 * @return i2b2 response message xml
	 * @throws JAXBUtilException
	 */
	public static String buildResponseMessage(String requestXml,
			StatusType statusType, BodyType bodyType, boolean splCharFilterFlag)
			throws JAXBUtilException {
		JAXBUtil util = CRCJAXBUtil.getJAXBUtil();
		
		MessageHeaderType messageHeader = new MessageHeaderType();
		ApplicationType appType = (ApplicationType) QueryProcessorUtil
		.getInstance().getSpringBeanFactory()
		.getBean("appType");
		
		if (requestXml != null) {
			RequestMessageType requestMsgType = getI2B2RequestMessageType(requestXml);
			MessageHeaderType clientMessageHeader = requestMsgType.getMessageHeader();
			// reverse sending and receiving app
			if (clientMessageHeader != null) {
				ApplicationType sendingApp = clientMessageHeader
						.getSendingApplication();
				messageHeader.setSendingApplication(appType);
				messageHeader.setReceivingApplication(sendingApp);
				messageHeader.setProjectId(clientMessageHeader.getProjectId());
			}
		} else {   
			messageHeader = new MessageHeaderType();
			messageHeader.setSendingApplication(appType);
		}
		
         messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
         messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));
        
         MessageControlIdType messageControlIdType = new MessageControlIdType();
         messageControlIdType.setInstanceNum(1);
         messageHeader.setMessageControlId(messageControlIdType);
         
         FacilityType facility = new FacilityType();
         facility.setFacilityName("i2b2 Hive");
         messageHeader.setSendingFacility(facility);
         messageHeader.setReceivingFacility(facility);

		// :TODO statusType.setValue(sessionId);
		PollingUrlType pollingType = new PollingUrlType();
		pollingType.setIntervalMs(100);

		// :TODO value come from property file
		// pollingType.setValue("http://localhost:8080/QueryProcessor/getResult");
		ResultStatusType resultStatusType = new ResultStatusType();
		resultStatusType.setStatus(statusType);
		resultStatusType.setPollingUrl(pollingType);

		InfoType infoType = new InfoType();
		// :TODO value come from property file
		// infoType.setUrl("http://localhost:8080/QueryProcessor/getStatus");
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
		util.marshallerWithCDATA(of.createResponse(responseMessageType), strWriter,
				new String[]{"observation_blob","patient_blob","observer_blob","concept_blob","event_blob"});
		return strWriter.toString();
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
	public static String buildResponseMessage(String requestXml,
			StatusType statusType, BodyType bodyType) throws JAXBUtilException {
		return buildResponseMessage(requestXml, statusType, bodyType, false);
	}

	/**
	 * Function to unmarshall i2b2 request message type
	 * 
	 * @param requestXml
	 * @return RequestMessageType
	 * @throws JAXBUtilException
	 */
	private static RequestMessageType getI2B2RequestMessageType(
			String requestXml) throws JAXBUtilException {
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
	public static String getResponseString(
			ResponseMessageType responseMessageType) {
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

}
