package edu.harvard.i2b2.common.util.axis2;

/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Mike Mendis
 */

import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
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
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

public class ServiceClient {
	private static Log log = LogFactory.getLog(ServiceClient.class.getName());

	private static org.apache.axis2.client.ServiceClient serviceClient = null;

	

	public static String sendREST(String restEPR, String requestString) throws Exception{	

		OMElement request = getPayLoad(requestString);
		return sendREST(restEPR, request);
	}
	


	public static String sendREST(String restEPR, OMElement request) throws Exception{	

		String response = null;
		try {
			
			if (serviceClient == null)
				serviceClient = new org.apache.axis2.client.ServiceClient();


			ServiceContext context = serviceClient.getServiceContext();
			MultiThreadedHttpConnectionManager connManager = (MultiThreadedHttpConnectionManager)context.getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);

			if(connManager == null) {
				connManager = new MultiThreadedHttpConnectionManager();
				context.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
				connManager.getParams().setMaxTotalConnections(100);
				connManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 100);
			}
			HttpClient httpClient = new HttpClient(connManager);

			Options options = new Options();
			options.setTo(new EndpointReference(restEPR));
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
			options.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);	
			options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
			serviceClient.setOptions(options);
			
			OMElement result = serviceClient.sendReceive(request);
			if (result != null) {
				response = result.toString();
				log.debug(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
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

		
		return response;
	}
		
	

	public static MessageContext getSOAPFile(String frUrl, OMElement requestElement, String frOperationName, String timeout) throws AxisFault
	{

		if (serviceClient == null)
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

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			lineItem = builder.getDocumentElement();
		} catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return lineItem;
	}
	
	public static String sendSOAP(String soapEPR, String requestString, String action, String operation) throws Exception{	

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

	public static  org.apache.axis2.client.ServiceClient getServiceClient() throws AxisFault{
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