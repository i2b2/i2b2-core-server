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
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.im.ws;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.util.IMJAXBUtil;
import edu.harvard.i2b2.im.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.im.datavo.i2b2message.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import jakarta.xml.bind.JAXBElement;


/**
 * The ResponseDataMessage class is a helper class to build Workplace messages in the
 * i2b2 format
 */
public abstract class ResponseDataMessage{
	protected final Log log = LogFactory.getLog(getClass());

	public ResponseMessageType reqMessageType = null;
	
	public void setResponseMessageType(String responseWdo) throws I2B2Exception {
		try {
			JAXBElement jaxbElement = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseWdo);
			this.reqMessageType = (ResponseMessageType) jaxbElement.getValue();
			
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Umarshaller error: " + e.getMessage() +
					responseWdo, e);
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
		appType.setApplicationName("IM Cell");
		appType.setApplicationVersion("1.700"); 
		messageHeader.setSendingApplication(appType);
		
		FacilityType facility = new FacilityType();
		facility.setFacilityName("i2b2 Hive");
		messageHeader.setSendingFacility(facility);
		
		ApplicationType appType2 = new ApplicationType();
		appType2.setApplicationVersion("1.700");
		appType2.setApplicationName("Project Management Cell");		
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
	 * Function to build i2b2 Response message header
	 * 
	 * @return ResponseHeader object
	 */
	public ResponseHeaderType getResponseHeader() { 
		ResponseHeaderType reqHeader = new ResponseHeaderType();
		
        StatusType status = new StatusType();
        status.setType("DONE");
        status.setValue("DONE");

        ResultStatusType resStat = new ResultStatusType();
        resStat.setStatus(status);
        reqHeader.setResultStatus(resStat);
		return reqHeader;
	}
	
	
	/**
	 * Function to build Response message type
	 * 
	 * @param messageHeader MessageHeader object  
	 * @param reqHeader     ResponseHeader object
	 * @param bodyType      BodyType object 
	 * @return ResponseMessageType object
	 */
	public ResponseMessageType getResponseMessageType(MessageHeaderType messageHeader,
			ResponseHeaderType reqHeader, BodyType bodyType) { 
		ResponseMessageType reqMsgType = new ResponseMessageType();
		reqMsgType.setMessageHeader(messageHeader);
		reqMsgType.setMessageBody(bodyType);
		reqMsgType.setResponseHeader(reqHeader);
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
			SecureRandom random = new SecureRandom();

			number = 48+(int) Math.round(random.nextDouble() * 74);
			if((number > 47 && number < 58) || (number > 64 && number < 91) 
				|| (number > 96 && number < 123)) {
					break;
				}
		}
		return number;
	}
	public ResponseMessageType getResponseMessageType() { 
		return reqMessageType;
	}

	public MessageHeaderType getMessageHeaderType() {
		return reqMessageType.getMessageHeader();
	}

}
