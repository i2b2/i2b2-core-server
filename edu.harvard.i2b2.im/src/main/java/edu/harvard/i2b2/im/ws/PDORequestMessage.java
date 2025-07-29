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
 * 		Mike Mendis
 */
package edu.harvard.i2b2.im.ws;

import java.io.StringWriter;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.im.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoRequestTypeType;
import edu.harvard.i2b2.im.datavo.pdo.query.RequestType;
import edu.harvard.i2b2.im.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.im.util.IMJAXBUtil;
import edu.harvard.i2b2.impm.ws.ProjectManagementRequestData;


/**
 * The ExportChildDataMessage class is a helper class to build IM messages in the
 * i2b2 format
 */
public class PDORequestMessage extends RequestDataMessage{

	public PDORequestMessage() {
	}

	/**
	 * Function to get pdo object from i2b2 request message type
	 * @return
	 * @throws I2B2Exception
	 */



	public GetPDOFromInputListRequestType getgetPDOFromInputListRequestType() throws I2B2Exception {
		GetPDOFromInputListRequestType getPDOFromInputListRequestType; 
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();


			getPDOFromInputListRequestType = (GetPDOFromInputListRequestType) helper.getObjectByClass(bodyType.getAny(),
					GetPDOFromInputListRequestType.class);



		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}catch (Exception e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}        

		return getPDOFromInputListRequestType;
	}



	/**
	 * Function to build PM Request message type and return it as an XML string
	 * 
	 * @param GetUserConfigurationType (user config data)
	 * @return A String data type containing the PM RequestMessage in XML format
	 */
	public String doBuildXML(GetPDOFromInputListRequestType userConfig, MessageHeaderType header){ 
		String requestString = null;
		try {
			MessageHeaderType messageHeader = getMessageHeader(); 

			messageHeader.setSecurity(header.getSecurity());
			messageHeader.setProjectId(header.getProjectId());

			RequestHeaderType reqHeader  = getRequestHeader();
			BodyType bodyType = getBodyType(userConfig) ;
			RequestMessageType reqMessageType = getRequestMessageType(messageHeader,
					reqHeader, bodyType);
			
			JAXBUtil jaxbUtil = IMJAXBUtil.getJAXBUtil();
			StringWriter strWriter = new StringWriter();
			try {
				edu.harvard.i2b2.im.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.im.datavo.i2b2message.ObjectFactory();
				jaxbUtil.marshaller(of.createRequest(reqMessageType), strWriter);
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

	public BodyType getBodyType(GetPDOFromInputListRequestType userConfigurationType) {
		edu.harvard.i2b2.im.datavo.pdo.query.ObjectFactory of = new edu.harvard.i2b2.im.datavo.pdo.query.ObjectFactory();

		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createRequest(userConfigurationType));
		return bodyType;
	}



	public PdoQryHeaderType getPdoQryHeaderType() throws I2B2Exception {
		PdoQryHeaderType PdoQryHeaderTypes; 
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();


			PdoQryHeaderTypes = (PdoQryHeaderType) helper.getObjectByClass(bodyType.getAny(),
					PdoQryHeaderType.class);



		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}catch (Exception e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}        

		return PdoQryHeaderTypes;
	}


}
