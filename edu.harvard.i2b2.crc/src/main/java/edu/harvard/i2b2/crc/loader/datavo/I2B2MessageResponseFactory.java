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
package edu.harvard.i2b2.crc.loader.datavo;

import java.io.StringWriter;
import java.math.BigDecimal;

import jakarta.xml.bind.JAXBElement;

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
		JAXBUtil util = CRCLoaderJAXBUtil.getJAXBUtil();
		
		MessageHeaderType messageHeader = new MessageHeaderType();
		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("edu.harvard.i2b2.crc.loader");
		appType.setApplicationVersion("1.0");
		
		
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
		util.marshaller(of.createResponse(responseMessageType), strWriter,
				splCharFilterFlag);
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
		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
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
			JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
			jaxbUtil.marshaller(of.createResponse(responseMessageType),
					strWriter);
		} catch (JAXBUtilException e) {
			log.error("Error while generating response message"
					+ e.getMessage());
		}

		return strWriter.toString();
	}

}
