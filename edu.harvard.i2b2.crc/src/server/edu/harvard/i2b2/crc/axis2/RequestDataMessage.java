/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.crc.axis2;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
//import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.bind.JAXBElement;


/**
 * The RequestDataMessage class is a helper class to build Workplace messages in the
 * i2b2 format
 */
public abstract class RequestDataMessage{
	protected final Log log = LogFactory.getLog(getClass());

	public RequestMessageType reqMessageType = null;
	
	public void setRequestMessageType(String requestWdo) throws I2B2Exception {
		try {
			JAXBElement jaxbElement = CRCJAXBUtil.getJAXBUtil().unMashallFromString(requestWdo);
			this.reqMessageType = (RequestMessageType) jaxbElement.getValue();
			
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Umarshaller error: " + e.getMessage() +
					requestWdo, e);
		}
	}


	/**
	 * Function to build i2b2 message header
	 * 
	 * @return MessageHeader object
	 */
	public MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		
		messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
		messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));
		
		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("CRC Cell");
		appType.setApplicationVersion("1.700"); 
		messageHeader.setSendingApplication(appType);
		
		FacilityType facility = new FacilityType();
		facility.setFacilityName("i2b2 Hive");
		messageHeader.setSendingFacility(facility);
		
		ApplicationType appType2 = new ApplicationType();
		appType2.setApplicationVersion("1.700");
		appType2.setApplicationName("i2b2_QueryTool");		
		messageHeader.setReceivingApplication(appType2);
	
		FacilityType facility2 = new FacilityType();
		facility2.setFacilityName("i2b2 Hive");
		messageHeader.setReceivingFacility(facility2);
		
		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));
		
		MessageControlIdType mcIdType = new MessageControlIdType();
		mcIdType.setInstanceNum(0);
		mcIdType.setMessageNum(generateMessageId());
		messageHeader.setMessageControlId(mcIdType);

		ProcessingIdType proc = new ProcessingIdType();
		proc.setProcessingId("P");
		proc.setProcessingMode("I");
		messageHeader.setProcessingId(proc);
		
		messageHeader.setAcceptAcknowledgementType("AL");
		messageHeader.setApplicationAcknowledgementType("AL");
		messageHeader.setCountryCode("US");
		
		return messageHeader;
	}
	
	/**
	 * Function to generate i2b2 message header message number
	 * 
	 * @return String
	 */
	protected String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<20; i++) {
			int num = getValidAcsiiValue();
			strWriter.append((char)num);
		}
		return strWriter.toString();
	}
	/**
	 * Function to build i2b2 Request message header
	 * 
	 * @return RequestHeader object
	 */
	public RequestHeaderType getRequestHeader() { 
		RequestHeaderType reqHeader = new RequestHeaderType();
		reqHeader.setResultWaittimeMs(120000);
		return reqHeader;
	}
	
	
	/**
	 * Function to build Request message type
	 * 
	 * @param messageHeader MessageHeader object  
	 * @param reqHeader     RequestHeader object
	 * @param bodyType      BodyType object 
	 * @return RequestMessageType object
	 */
	public RequestMessageType getRequestMessageType(MessageHeaderType messageHeader,
			RequestHeaderType reqHeader, BodyType bodyType) { 
		RequestMessageType reqMsgType = new RequestMessageType();
		reqMsgType.setMessageHeader(messageHeader);
		reqMsgType.setMessageBody(bodyType);
		reqMsgType.setRequestHeader(reqHeader);
		return reqMsgType;
	}
	
	/**
	 * Function to generate random number used in message number
	 * 
	 * @return int 
	 */
	private int getValidAcsiiValue() {
		int number = 48;
		while(true) {
			number = 48+(int) Math.round(Math.random() * 74);
			if((number > 47 && number < 58) || (number > 64 && number < 91) 
				|| (number > 96 && number < 123)) {
					break;
				}
		}
		return number;
	}
	public RequestMessageType getRequestMessageType() { 
		return reqMessageType;
	}

	public MessageHeaderType getMessageHeaderType() {
		return reqMessageType.getMessageHeader();
	}

}