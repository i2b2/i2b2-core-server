/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util.axis2;

/*

 * 
 * Contributors:
 * 		Mike Mendis
 */

import java.io.StringReader;
import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
//import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.xml.utils.QName;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

public class ServiceClient {
	private static Log log = LogFactory.getLog(ServiceClient.class.getName());

	//private static org.apache.axis2.client.ServiceClient serviceClient = null;


	private static Object LOCK = new Object();
	 
	public static String sendREST(String restEPR, String requestString) throws Exception{	

		OMElement request = getPayLoad(requestString);
		return sendREST(restEPR, request);
	}



	public static String sendREST(String restEPR, OMElement request) throws Exception{	

		String response = null;
		org.apache.axis2.client.ServiceClient serviceClient = null;
		//org.apache.axis2.client.ServiceClient serviceClient_retry = null;

		int retry = 0;
		boolean done = false;
		String msg = null;
		do {
			try {

				serviceClient = new org.apache.axis2.client.ServiceClient();


				ServiceContext context = serviceClient.getServiceContext();
				/*
				MultiThreadedHttpConnectionManager connManager = (MultiThreadedHttpConnectionManager)context.getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);


				if(connManager == null) {
					connManager = new MultiThreadedHttpConnectionManager();
					context.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
					connManager.getParams().setMaxTotalConnections(100);
					connManager.getParams().setDefaultMaxConnectionsPerHost(100);
					connManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 100);
				}
				HttpClient httpClient = new HttpClient(connManager);
				*/
				Options options = new Options();
				options.setTo(new EndpointReference(restEPR));
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
//				options.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);	
				//MM axis2 2.0 options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
				serviceClient.setOptions(options);

				OMElement result = serviceClient.sendReceive(request);
				if (result != null) {
					response = result.toString();
					//logesapi.debug(response);
				}
				done = true;
			} catch (Exception e) {
				Thread.sleep(1000);
				msg = e.getMessage();
			} finally {
				if (serviceClient != null) {
					try{
						serviceClient.cleanupTransport();
						serviceClient.cleanup();
					} catch (AxisFault e) {
						log.debug("Error .", e);
					}
				}
			}		
			if (retry == 5)
				done = true;
			retry ++;
		} while (done == false);
		
		if (retry ==- 5)
			throw new I2B2Exception(msg);

		return response;
	}



	public static MessageContext getSOAPFile(String frUrl, OMElement requestElement, String frOperationName, String timeout) throws AxisFault
	{

		org.apache.axis2.client.ServiceClient serviceClient =  null; 
		serviceClient = new org.apache.axis2.client.ServiceClient();

		// OMElement getRequestElmt = getRequestPayLoad();
		Options options = new Options();
		options.setTo(new EndpointReference(frUrl));
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		//		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);

		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(Integer.parseInt(timeout));

		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);

		//ServiceClient sender = new ServiceClient();

		serviceClient.setOptions(options);
		OperationClient mepClient = serviceClient.createClient(org.apache.axis2.client.ServiceClient.ANON_OUT_IN_OP);

		//		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPFactory sfac = OMAbstractFactory.getSOAP12Factory();
		SOAPEnvelope env = sfac.getDefaultEnvelope();
		env.getBody().addChild(requestElement);
		MessageContext mc = new MessageContext();
		mc.setEnvelope(env);
		mc.setDoingMTOM(false);
		mc.setDoingSwA( true );

		mepClient.addMessageContext(mc);

		mepClient.execute(true);
		return mepClient
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);


	}

	public static OMElement getPayLoad(String requestPm) throws Exception {
		OMElement lineItem = null;
		try {

			StringReader strReader = new StringReader(requestPm);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			lineItem = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
			lineItem.build();
			
			/*
			   // Create an XMLStreamReader without building the object model
			StringReader strReader = new StringReader(requestPm);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		    while (reader.hasNext()) {
		        if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
		                reader.getName().equals(new QName("tag"))) {
		            // A matching START_ELEMENT event was found. Build a corresponding OMElement.
		        	lineItem = 
		                OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
		            // Make sure that all events belonging to the element are consumed so
		            // that the XMLStreamReader points to a well defined location (namely the
		            // event immediately following the END_ELEMENT event).
		        	lineItem.build();
		            // Now process the element.
		        } else {
		            reader.next();
		        }
		    }
		    */
		} catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return lineItem;
	}
	
	/**
	 * Function constructs OMElement for the given String
	 * 
	 * @param xmlString
	 * @return OMElement
	 * @throws XMLStreamException
	 */
	public static OMElement buildOMElementFromString(String xmlString)
			throws XMLStreamException {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		StringReader strReader = new StringReader(xmlString);
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		OMElement element = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();

		return element;
	}

	public static String sendSOAP(String soapEPR, String requestString, String action, String operation) throws Exception{	

		org.apache.axis2.client.ServiceClient serviceClient = null;
		serviceClient = new
				org.apache.axis2.client.ServiceClient(); 
		OperationClient operationClient = serviceClient
				.createClient(org.apache.axis2.client.ServiceClient.ANON_OUT_IN_OP);

		// creating message context
		MessageContext outMsgCtx = new MessageContext();
		// assigning message context's option object into instance variable
		Options opts = outMsgCtx.getOptions();
		// setting properties into option
		//		log.debug(soapEPR);
		opts.setTo(new EndpointReference(soapEPR));
		opts.setAction(action);
		opts.setTimeOutInMilliSeconds(180000);

		log.debug(requestString);

		SOAPEnvelope envelope = null;

		try {
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			envelope = fac.getDefaultEnvelope();
			OMNamespace omNs = fac.createOMNamespace(
					"http://rpdr.partners.org/",                                   
					"rpdr");


			// creating the SOAP payload
			OMElement method = fac.createOMElement(operation, omNs);
			OMElement value = fac.createOMElement("RequestXmlString", omNs);
			value.setText(requestString);
			method.addChild(value);
			envelope.getBody().addChild(method);
		}
		catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}

		outMsgCtx.setEnvelope(envelope);


		operationClient.addMessageContext(outMsgCtx);
		operationClient.execute(true);


		MessageContext inMsgtCtx = operationClient.getMessageContext("In");
		SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();

		OMElement soapResponse = responseEnv.getBody().getFirstElement();

		OMElement soapResult = soapResponse.getFirstElement();

		String i2b2Response = soapResult.getText();
		log.debug(i2b2Response);

		return i2b2Response;		
	}
	public static String getContextRoot() throws InstanceNotFoundException, AttributeNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, AxisFault {

		org.apache.axis2.client.ServiceClient cl = ServiceClient.getServiceClient();
		Integer port = (Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http"), "port"); 

		MessageContext mCtx =
				MessageContext.getCurrentMessageContext();
		String url = null;
		if (mCtx == null)
		{
			url =  "http://localhost:" + port + "/i2b2";
		} else  {
			ConfigurationContext cCtx =
					mCtx.getConfigurationContext();
			url=    "http://localhost:" + port + cCtx.getContextRoot();
		}
		return url;

	}

	public static  org.apache.axis2.client.ServiceClient getServiceClient() throws AxisFault{
		org.apache.axis2.client.ServiceClient serviceClient =
				null; 
		if (serviceClient == null) {
			try {
				serviceClient = new org.apache.axis2.client.ServiceClient();
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw e;
			}
		}
		return serviceClient;
	}

}
