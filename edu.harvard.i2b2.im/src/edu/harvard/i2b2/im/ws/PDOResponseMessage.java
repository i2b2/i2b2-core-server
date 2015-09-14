/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     Michael Mendis
 *     
 */

package edu.harvard.i2b2.im.ws;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.im.util.IMJAXBUtil;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.im.datavo.pdo.EventSet;
import edu.harvard.i2b2.im.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.im.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.im.datavo.pdo.PatientSet;
import edu.harvard.i2b2.im.datavo.pdo.PidSet;
import edu.harvard.i2b2.im.datavo.pdo.PidType;
import edu.harvard.i2b2.im.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.im.datavo.pdo.query.PatientDataResponseType;

public class PDOResponseMessage  extends ResponseDataMessage{
	
	public PDOResponseMessage() {
	}


	/**
	 * Function to build PM Request message type and return it as an XML string
	 * 
	 * @param GetUserConfigurationType (user config data)
	 * @return A String data type containing the PM RequestMessage in XML format
	 */
	public String doBuildXML(PatientDataResponseType userConfig, MessageHeaderType header){ 
		String requestString = null;
		try {
			MessageHeaderType messageHeader = getMessageHeader(); 

			messageHeader.setSecurity(header.getSecurity());
			messageHeader.setProjectId(header.getProjectId());

			ResponseHeaderType reqHeader  = getResponseHeader();
			BodyType bodyType = getBodyType(userConfig) ;
			ResponseMessageType reqMessageType = getResponseMessageType(messageHeader,
					reqHeader, bodyType);
			
			JAXBUtil jaxbUtil = IMJAXBUtil.getJAXBUtil();
			StringWriter strWriter = new StringWriter();
			try {
				edu.harvard.i2b2.im.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.im.datavo.i2b2message.ObjectFactory();
				jaxbUtil.marshaller(of.createResponse(reqMessageType), strWriter);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
			requestString = strWriter.toString();
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
		return requestString;
	}

	public BodyType getBodyType(PatientDataResponseType userConfigurationType) {
		edu.harvard.i2b2.im.datavo.pdo.query.ObjectFactory of = new edu.harvard.i2b2.im.datavo.pdo.query.ObjectFactory();

		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createResponse(userConfigurationType));
		return bodyType;
	}

	public PatientDataResponseType getPatientDataFromResponseXML(String responseXML)
			throws Exception {

		JAXBUtil jaxbUtil = IMJAXBUtil.getJAXBUtil();

		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
		ResponseMessageType messageType = (ResponseMessageType) jaxbElement
				.getValue();
		BodyType bodyType = messageType.getMessageBody();
		PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
				.getObjectByClass(bodyType.getAny(),
						PatientDataResponseType.class);
		return responseType;
	}

}
