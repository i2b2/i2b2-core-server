/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.delegate.pm;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;

public class GetUserConfigurationRequestMessage extends ProjectManagementRequestData {
		
		public static final String THIS_CLASS_NAME = GetUserConfigurationRequestMessage.class.getName();
	    private Log log = LogFactory.getLog(THIS_CLASS_NAME);	


		public GetUserConfigurationRequestMessage() {
		}
		
		
		/**
		 * Function to build getUserConfiguration body type
		 * 
		 * @param 
		 * @return BodyType object
		 */
		
		public BodyType getBodyType(GetUserConfigurationType userConfigurationType) {
			edu.harvard.i2b2.crc.datavo.pm.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.pm.ObjectFactory();
			
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createGetUserConfiguration(userConfigurationType));
			return bodyType;
		}

		/**
		 * Function to build PM Request message type and return it as an XML string
		 * 
		 * @param GetUserConfigurationType (user config data)
		 * @return A String data type containing the PM RequestMessage in XML format
		 */
		public String doBuildXML(GetUserConfigurationType userConfig, SecurityType userSecurity){ 
			String requestString = null;
				try {
					MessageHeaderType messageHeader = getMessageHeader(); 
					messageHeader.setSecurity(userSecurity);
					RequestHeaderType reqHeader  = getRequestHeader();
					BodyType bodyType = getBodyType(userConfig) ;
					RequestMessageType reqMessageType = getRequestMessageType(messageHeader,
							reqHeader, bodyType);
					requestString = getXMLString(reqMessageType);

				} catch (JAXBUtilException e) {
					log.error(e.getMessage());
				} 
			return requestString;
		}
		

		/**
		 * Function to build PM Request message type and return it as an XML string
		 * 
		 * @param GetUserConfigurationType (user config data)
		 * 		  MessageHeaderType
		 * 		  boolean (not used, only to render this method different from the previous one)
		 * @return A String data type containing the PM RequestMessage in XML format
		 */
		public String buildXML(GetUserConfigurationType userConfig, MessageHeaderType header) { 
			String requestString = null;
			try {
				MessageHeaderType messageHeader = getMessageHeader(); 				
				messageHeader.setSecurity(header.getSecurity());
				messageHeader.setProjectId(header.getProjectId());				
				RequestHeaderType reqHeader = getRequestHeader();
				BodyType bodyType = getBodyType(userConfig);
				RequestMessageType reqMessageType = getRequestMessageType(messageHeader, reqHeader, bodyType);
				requestString = getXMLString(reqMessageType);
			} catch (JAXBUtilException e) {
				log.error(e.getMessage());
			} 
			return requestString;
		}

		
		/**
		 * Function to convert Ont Request message type to an XML string
		 * 
		 * @param reqMessageType   String containing Ont request message to be converted to string
		 * @return A String data type containing the Ont RequestMessage in XML format
		 */
		public String getXMLString(RequestMessageType reqMessageType) throws JAXBUtilException{ 
			StringWriter strWriter = null;
			try {
				strWriter = new StringWriter();
				edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
				CRCLoaderJAXBUtil.getJAXBUtil().marshaller(of.createRequest(reqMessageType), strWriter);
			} catch (JAXBUtilException e) {
				log.error("Error marshalling Ont request message");
				throw e;
			} 
			return strWriter.toString();
		}
}
