/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Mike Mendis
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.pm.ws;

import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.workplace.util.WorkplaceUtil;

public class PMServiceDriver {
	private static Log log = LogFactory.getLog(PMServiceDriver.class.getName());
	//private static ServiceClient serviceClient = null;
	/**
	 * Function to convert pm requestVdo to OMElement
	 * 
	 * @param requestPm   String request to send to pm web service
	 * @return An OMElement containing the pm web service requestVdo
	 */
	public static OMElement getPmPayLoad(String requestPm) throws Exception {
		OMElement lineItem = null;
		try {
			StringReader strReader = new StringReader(requestPm);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			lineItem = builder.getDocumentElement();
		} catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return lineItem;
	}

	/**
	 * Function to send getRoles request to PM web service
	 * 
	 * @param GetUserConfigurationType  userConfig we wish to get data for
	 * @return A String containing the PM web service response 
	 */

	public static  String getRoles(GetUserConfigurationType userConfig, MessageHeaderType header) throws I2B2Exception, AxisFault, Exception{
		String response = null;	
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
			String getRolesRequestString = reqMsg.doBuildXML(userConfig, header);
//			OMElement getPm = getPmPayLoad(getRolesRequestString);


			// First step is to get PM endpoint reference from properties file.
			String pmEPR = "";
			String pmMethod = "";
			try {
				pmEPR = WorkplaceUtil.getInstance().getPmEndpointReference();
				pmMethod = WorkplaceUtil.getInstance().getPmWebServiceMethod();
			} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				throw e1;
			}
			 if(pmMethod.equals("SOAP")) {
				 response = ServiceClient.sendSOAP(pmEPR, getRolesRequestString, "http://rpdr.partners.org/GetUserConfiguration", "GetUserConfiguration" );
			 }
			 else {
				 response = ServiceClient.sendREST(pmEPR, getRolesRequestString);
			 }

		} catch (AxisFault e) {
			log.error(e.getMessage());
			throw e; 
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} 
		return response;
	}
	
	
}
