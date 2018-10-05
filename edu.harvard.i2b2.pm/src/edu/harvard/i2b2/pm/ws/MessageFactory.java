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
 *     Mike Mendis - initial API and implementation
 */


package edu.harvard.i2b2.pm.ws;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.pm.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.pm.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.pm.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.pm.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.pm.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.pm.datavo.pm.ApprovalType;
import edu.harvard.i2b2.pm.datavo.pm.ApprovalsType;
import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.CellDatasType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.datavo.pm.ConfiguresType;
import edu.harvard.i2b2.pm.datavo.pm.DatasourcesType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDataType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDatasType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ParamsType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectRequestType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectRequestsType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectsType;
import edu.harvard.i2b2.pm.datavo.pm.RoleType;
import edu.harvard.i2b2.pm.datavo.pm.RolesType;
import edu.harvard.i2b2.pm.datavo.pm.UserType;
import edu.harvard.i2b2.pm.datavo.pm.UsersType;
import edu.harvard.i2b2.pm.util.JAXBConstant;
//import edu.harvard.i2b2.pm.datavo.pm.ResponseType;


/**
 * Factory class to create request/response message objects.
 *
 */
public class MessageFactory {
	private static Log log = LogFactory.getLog(MessageFactory.class);
	private static JAXBUtil jaxbUtil = new JAXBUtil(JAXBConstant.DEFAULT_PACKAGE_NAME);

	public static JAXBUtil getJAXBUtil()
	{
		return jaxbUtil;
	}
	/**
	 * Function creates PFT response OMElement from xml string
	 * @param xmlString
	 * @return OMElement
	 * @throws XMLStreamException
	 */
	public static OMElement createResponseOMElementFromString(String xmlString)
			throws XMLStreamException {
		OMElement returnElement = null;

		try {
			StringReader strReader = new StringReader(xmlString);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			returnElement = builder.getDocumentElement();        	

		} catch (XMLStreamException xmlStreamEx) {
			log.error("Error while converting PM response ConfigureType to OMElement");
			throw xmlStreamEx;
		}

		return returnElement;
	}

	/**
	 * Function to build patientData body type
	 *
	 * @param obsSet
	 *            Observation fact set to be returned to requester
	 * @return BodyType object
	 */
	public static BodyType createBodyType(Object uType) {

		edu.harvard.i2b2.pm.datavo.pm.ObjectFactory of = new edu.harvard.i2b2.pm.datavo.pm.ObjectFactory();
		BodyType bodyType = new BodyType();
		if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ConfigureType"))
			bodyType.getAny().add(of.createConfigure((ConfigureType) uType));
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.UserType"))
			bodyType.getAny().add(of.createUser((UserType) uType));
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.UsersType"))
			bodyType.getAny().add(of.createUsers((UsersType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ProjectType"))
			bodyType.getAny().add(of.createProject((ProjectType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ProjectsType"))
			bodyType.getAny().add(of.createProjects((ProjectsType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.CellDataType"))
			bodyType.getAny().add(of.createCell((CellDataType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.CellDatasType"))
			bodyType.getAny().add(of.createCells((CellDatasType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.GlobalDataType"))
			bodyType.getAny().add(of.createGlobal((GlobalDataType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.GlobalDatasType"))
			bodyType.getAny().add(of.createGlobals((GlobalDatasType) uType));            
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ParamType"))
			bodyType.getAny().add(of.createParam((ParamType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ParamsType"))
			bodyType.getAny().add(of.createParams((ParamsType) uType));              
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ProjectRequestType"))
			bodyType.getAny().add(of.createProjectRequest((ProjectRequestType) uType));  
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ProjectRequestsType"))
			bodyType.getAny().add(of.createProjectRequests((ProjectRequestsType) uType));              
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.RoleType"))
			bodyType.getAny().add(of.createRole((RoleType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.RolesType"))
			bodyType.getAny().add(of.createRoles((RolesType) uType));      
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ConfigureType"))
			bodyType.getAny().add(of.createHive((ConfigureType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ConfiguresType"))
			bodyType.getAny().add(of.createHives((ConfiguresType) uType));      
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ApprovalType"))
			bodyType.getAny().add(of.createApproval((ApprovalType) uType));    	
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.ApprovalsType"))
			bodyType.getAny().add(of.createApprovals((ApprovalsType) uType));      
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.i2b2message.ResultStatusType"))
			bodyType.getAny().add(of.createResponse(((ResultStatusType) uType).getStatus().getValue()));     
		else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.DatasourcesType"))
			bodyType.getAny().add(of.createDatasources((DatasourcesType) uType));    	
		//else  if (uType.getClass().getName().equals("edu.harvard.i2b2.pm.datavo.pm.DatasourcesType"))
	//		bodyType.getAny().add(of.createApprovals((ApprovalsType) uType));      


		return bodyType;
	}


	/**
	 * Function to create response  message header based
	 * on request message header
	 *
	 * @return MessageHeader object
	 */
	public static MessageHeaderType createResponseMessageHeader(
			MessageHeaderType messageHeaderType) {
		MessageHeaderType messageHeader = new MessageHeaderType();

		messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
		messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));

		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("PM Cell");
		appType.setApplicationVersion("1.700");
		messageHeader.setSendingApplication(appType);

		FacilityType facility = new FacilityType();
		facility.setFacilityName("i2b2 Hive");
		messageHeader.setSendingFacility(facility);

		if (messageHeaderType != null) {
			if (messageHeaderType.getSendingApplication() != null) {
				messageHeader.setReceivingApplication(messageHeaderType.getSendingApplication());
			}

			messageHeader.setReceivingFacility(messageHeaderType.getSendingFacility());
		}

		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(
				currentDate.getTime()));

		MessageControlIdType mcIdType = new MessageControlIdType();
		mcIdType.setInstanceNum(1);

		if ((messageHeaderType != null) &&  (messageHeaderType.getMessageControlId() != null)) {
			mcIdType.setMessageNum(messageHeaderType.getMessageControlId()
					.getMessageNum());
			mcIdType.setSessionId(messageHeaderType.getMessageControlId()
					.getSessionId());
		}


		messageHeader.setMessageControlId(mcIdType);

		ProcessingIdType proc = new ProcessingIdType();
		proc.setProcessingId("P");
		proc.setProcessingMode("I");
		messageHeader.setProcessingId(proc);

		messageHeader.setAcceptAcknowledgementType("AL");
		messageHeader.setApplicationAcknowledgementType("AL");
		messageHeader.setCountryCode("US");
		messageHeader.setProjectId(messageHeaderType.getProjectId());

		return messageHeader;
	}

	/**
	 * Function to create response message type
	 * @param messageHeader
	 * @param respHeader
	 * @param bodyType
	 * @return ResponseMessageType
	 */
	public static ResponseMessageType createResponseMessageType(
			MessageHeaderType messageHeader, ResponseHeaderType respHeader,
			BodyType bodyType) {
		ResponseMessageType respMsgType = new ResponseMessageType();
		respMsgType.setMessageHeader(messageHeader);
		respMsgType.setMessageBody(bodyType);
		respMsgType.setResponseHeader(respHeader);

		return respMsgType;
	}


	/**
	 * Function to convert ResponseMessageType to string
	 * @param respMessageType
	 * @return String
	 * @throws Exception
	 */
	public static String convertToXMLString(ResponseMessageType respMessageType)
			throws I2B2Exception {
		StringWriter strWriter = null;

		try {
			// JAXBUtil jaxbUtil = new JAXBUtil(JAXBConstant.DEFAULT_PACKAGE_NAME);
			strWriter = new StringWriter();

			edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory objectFactory = new edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory();
			jaxbUtil.marshaller(objectFactory.createResponse(respMessageType),
					strWriter);
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception(
					"Error converting response message type to string " +
							e.getMessage(), e);
		}

		return strWriter.toString();
	}


	/**
	 * Function to convert ResponseMessageType to string
	 * @param respMessageType
	 * @return String
	 * @throws Exception
	 */
	public static String convertToXMLString(edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType respMessageType)
			throws I2B2Exception {
		StringWriter strWriter = null;

		try {
			JAXBUtil jaxbUtil = new JAXBUtil(JAXBConstant.DEFAULT_PACKAGE_NAME);
			strWriter = new StringWriter();

			edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ObjectFactory objectFactory = new edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ObjectFactory();
			jaxbUtil.marshaller(objectFactory.createResponse(respMessageType),
					strWriter);
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception(
					"Error converting response message type to string " +
							e.getMessage(), e);
		}

		return strWriter.toString();
	}    
	/**
	 * Function to get i2b2 Request message header
	 *
	 * @return RequestHeader object
	 */
	public static RequestHeaderType getRequestHeader() {
		RequestHeaderType reqHeader = new RequestHeaderType();
		reqHeader.setResultWaittimeMs(120000);

		return reqHeader;
	}

	/**
	 * Function to create Response with given error message
	 * @param messageHeaderType
	 * @param errorMessage
	 * @return
	 * @throws Exception
	 */
	public static ResponseMessageType doBuildErrorResponse(
			MessageHeaderType messageHeaderType, String errorMessage) {
		ResponseMessageType respMessageType = null;

		MessageHeaderType messageHeader = createResponseMessageHeader(messageHeaderType);
		ResponseHeaderType respHeader = createResponseHeader("ERROR",
				errorMessage);
		respMessageType = createResponseMessageType(messageHeader, respHeader,
				null);

		return respMessageType;
	}

	/**
	 * Creates ResponseHeader for the given type and value
	 * @param type
	 * @param value
	 * @return
	 */
	private static ResponseHeaderType createResponseHeader(String type,
			String value) {
		ResponseHeaderType respHeader = new ResponseHeaderType();
		StatusType status = new StatusType();
		status.setType(type);
		status.setValue(value);

		ResultStatusType resStat = new ResultStatusType();
		resStat.setStatus(status);
		respHeader.setResultStatus(resStat);

		return respHeader;
	}

	/**
	 * Function to build Response message type and return it as an XML string
	 *
	 * @param obsSet
	 *            observation fact set to be included in response PDO
	 *
	 * @return A String data type containing the ResponseMessage in XML format
	 * @throws Exception
	 */
	public static ResponseMessageType createBuildResponse(
			MessageHeaderType messageHeaderType,
			Object obsSet) {
		ResponseMessageType respMessageType = null;

		MessageHeaderType messageHeader = createResponseMessageHeader(messageHeaderType);
		log.debug("Created message header");

		ResponseHeaderType respHeader = createResponseHeader("DONE",
				"PM processing completed");
		log.debug("Created response  header");

		BodyType bodyType = createBodyType(obsSet);
		log.debug("Created body part");
		respMessageType = createResponseMessageType(messageHeader, respHeader,
				bodyType);
		log.debug("Response message type ");

		return respMessageType;
	}


}
