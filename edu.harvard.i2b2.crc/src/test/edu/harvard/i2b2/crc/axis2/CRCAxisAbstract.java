/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.axis2;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;

/**
 * Class to hold helper functions to pack and unwrap xml payload
 * 
 * @author rkuttan
 */
public abstract class CRCAxisAbstract {
	public static MessageHeaderType generateMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		messageHeader.setI2B2VersionCompatible(new BigDecimal("1.0"));
		messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));
		edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType appType = new edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType();
		appType.setApplicationName("i2b2 Project Management");
		appType.setApplicationVersion("1.0");
		messageHeader.setSendingApplication(appType);
		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory
				.getXMLGregorianCalendar(currentDate.getTime()));
		messageHeader.setAcceptAcknowledgementType("AL");
		messageHeader.setApplicationAcknowledgementType("AL");
		messageHeader.setCountryCode("US");
		SecurityType securityType = new SecurityType();
		securityType.setDomain("demo");
		securityType.setUsername("demo");
		PasswordType ptype = new PasswordType();
		ptype.setValue("demouser");
		securityType.setPassword(ptype);
		messageHeader.setSecurity(securityType);
		messageHeader.setProjectId("Demo");
		return messageHeader;
	}

	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType();
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}

	public static String getQueryString(String filename) throws Exception {
		StringBuffer queryStr = new StringBuffer();
		DataInputStream dataStream = new DataInputStream(new FileInputStream(
				filename));
		while (dataStream.available() > 0) {
			queryStr.append(dataStream.readLine() + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();
	}

	public static ServiceClient getServiceClient(String serviceUrl)
			throws Exception {
		Options options = new Options();
		EndpointReference endpointReference = new EndpointReference(serviceUrl);
		options.setTo(endpointReference);
		options.setTimeOutInMilliSeconds(2700000);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
	//	options.setProperty(Constants.Configuration.CONTENT_TYPE,Constants.MIME_CT_TEXT_XML);
		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.HTTP_METHOD, Constants.Configuration.HTTP_METHOD_PUT);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		return sender;
	}

	public static OMElement convertStringToOMElement(String requestXmlString)
			throws Exception {
		StringReader strReader = new StringReader(requestXmlString);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);

		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement lineItem = builder.getDocumentElement();
		return lineItem;
	}

	public static OMElement convertStringToOMElement(InputStream requestXmlString)
			throws Exception {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(requestXmlString);

		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement lineItem = builder.getDocumentElement();
		return lineItem;
	}
	
}
