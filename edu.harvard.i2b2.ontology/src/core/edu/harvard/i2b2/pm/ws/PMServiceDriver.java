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
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.pm.ws;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.pm.ConfigureType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.ontology.datavo.pm.ObjectFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ParamType;
import edu.harvard.i2b2.ontology.datavo.pm.ParamsType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import jakarta.xml.bind.JAXBElement;

public class PMServiceDriver {
	private static Log log = LogFactory.getLog(PMServiceDriver.class.getName());

	protected static Log logesapi = LogFactory.getLog(PMServiceDriver.class);


	/**
	 * Function to send getRoles request to PM web service
	 * 
	 * @param GetUserConfigurationType  userConfig we wish to get data for
	 * @return A String containing the PM web service response 
	 */

	public static String setProjectParam(String status,
			String paramName, String value, SecurityType securityType, String projectId, String ontologyUrl ) throws I2B2Exception {

		
			return setProjectParam(-1, status, paramName, value, securityType, projectId, ontologyUrl);
		}
	
	public static String setProjectParam(int id, String status,
			String paramName, String value, SecurityType securityType, String projectId, String ontologyUrl ) throws I2B2Exception {

		ParamType paramType = new ParamType();
		if (id != -1)
			paramType.setId(id);
		paramType.setName(paramName);
		paramType.setValue(value);
		paramType.setDatatype("T");
		paramType.setStatus(status);

		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();

		ProjectType projectType1 = of.createProjectType();
		projectType1.getParam().add(paramType);
		projectType1.setId(projectId);
		bodyType.getAny().add(of.createSetProjectParam(projectType1));
		RequestMessageType requestMessageType = getI2B2RequestMessage(bodyType, securityType, projectId);
		OMElement requestElement = null;
		String response = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			//OMElement response = getServiceClient().sendReceive(requestElement);
			response = ServiceClient.sendREST(ontologyUrl, requestElement);
			// :TODO check the status in the response

			// projectType = getUserProjectFromResponse(response.toString());
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception  e) {
			e.printStackTrace();
			throw new I2B2Exception(
					"AxisFault error when setting lockedout param for user "
							+ StackTraceUtil.getStackTrace(e));
		} 
		return response;
	}


	public static ParamType getProjectParam( 
			String paramName,   SecurityType securityType, String projectId, String ontologyUrl ) throws I2B2Exception {

		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();

		bodyType.getAny().add(of.createGetAllProjectParam(projectId));
		RequestMessageType requestMessageType = getI2B2RequestMessage(bodyType, securityType, projectId);
		OMElement requestElement = null;
		ParamType param = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			//OMElement response = getServiceClient().sendReceive(requestElement);
			String response = ServiceClient.sendREST(ontologyUrl, requestElement);
			// :TODO check the status in the response

			// projectType = getUserProjectFromResponse(response.toString());

			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(response);
			ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
					.getValue();
			
			
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			ParamsType params = (ParamsType) helper.getObjectByClass(
					pmRespMessageType.getMessageBody().getAny(),
					ParamsType.class);
			
			for (ParamType p: params.getParam())
			{
				if (p.getName().equals(paramName))
					return p;
				
			}
			
			log.debug("CRC PM call's request xml " + requestElement);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception  e) {
			e.printStackTrace();
			throw new I2B2Exception(
					"AxisFault error when setting lockedout param for user "
							+ StackTraceUtil.getStackTrace(e));
		} 
		return null;
	}

	public static  String getRoles(GetUserConfigurationType userConfig, MessageHeaderType header) throws I2B2Exception, AxisFault, Exception{
		String response = null;	
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
			String getRolesRequestString = reqMsg.doBuildXML(userConfig, header);
			//			OMElement getPm = getPmPayLoad(getRolesRequestString);


			// First step is to get PM endpoint reference from properties file.
			String pmEPR = "";
			//	String pmMethod = "";
			try {
				pmEPR = OntologyUtil.getInstance().getPmEndpointReference();
				//		pmMethod = OntologyUtil.getInstance().getPmWebServiceMethod();
			} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				throw e1;
			}


			response = ServiceClient.sendREST(pmEPR, getRolesRequestString);


			logesapi.debug("PM response = " + response);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return response;
	}



	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory();
		OntologyJAXBUtil.getJAXBUtil().marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		OMElement request = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();

		return request;
	}

	private static RequestMessageType getI2B2RequestMessage(BodyType bodyType, SecurityType securityType,  String projectId ) {
		OntologyUtil queryUtil = OntologyUtil.getInstance();
		MessageHeaderType messageHeaderType =  queryUtil.getMessageHeader();
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}


}
