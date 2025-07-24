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
package edu.harvard.i2b2.fr.ws;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.fr.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.fr.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.fr.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.fr.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.fr.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.fr.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.fr.datavo.i2b2message.SecurityType;

/**
 * Class to hold helper functions to pack and unwrap xml payload
 * 
 * @author rkuttan
 */
public abstract class FRAxisAbstract {

	public static RequestMessageType buildRequestMessage(BodyType bodyType, String user, String passwd, String project) {
		MessageHeaderType messageHeaderType = generateMessageHeader();
		SecurityType securityType = new SecurityType();
		securityType.setDomain("i2b2demo");
		securityType.setUsername(user);
		PasswordType ptype = new PasswordType();
		ptype.setValue(passwd);
		securityType.setPassword(ptype);
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(project);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body

		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}



	public static String getQueryString(String filename) throws Exception {


	StringBuffer queryStr = new StringBuffer();
		DataInputStream dataStream = new DataInputStream(new FileInputStream(
				filename));
		while (dataStream.available() > 0) {
			queryStr.append(dataStream.readLine() + "\n");
		}
		return queryStr.toString();
}	


public static MessageHeaderType generateMessageHeader() {
	MessageHeaderType messageHeader = new MessageHeaderType();
	messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
	messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));
	edu.harvard.i2b2.fr.datavo.i2b2message.ApplicationType appType = new edu.harvard.i2b2.fr.datavo.i2b2message.ApplicationType();
	appType.setApplicationName("i2b2 Project Management");
	appType.setApplicationVersion("1.602");
	messageHeader.setSendingApplication(appType);
	Date currentDate = new Date();
	DTOFactory factory = new DTOFactory();
	messageHeader.setDatetimeOfMessage(factory
			.getXMLGregorianCalendar(currentDate.getTime()));
	messageHeader.setAcceptAcknowledgementType("AL");
	messageHeader.setApplicationAcknowledgementType("AL");
	messageHeader.setCountryCode("US");

	return messageHeader;
}

public static RequestHeaderType generateRequestHeader() {
	RequestHeaderType reqHeaderType = new RequestHeaderType();
	reqHeaderType.setResultWaittimeMs(90000);
	return reqHeaderType;
}


public static ServiceClient getServiceClient(String serviceUrl)
		throws Exception {
	Options options = new Options();
	EndpointReference endpointReference = new EndpointReference(serviceUrl);
	options.setTo(endpointReference);
	options.setTimeOutInMilliSeconds(2700000);
	options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
	options.setProperty(Constants.Configuration.ENABLE_REST,
			Constants.VALUE_TRUE);
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	return sender;
}


}
