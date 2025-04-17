/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.delegate.pm;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.AxisFault;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class CallPMUtil {

	private static JAXBUtil jaxbUtil =   CRCJAXBUtil.getJAXBUtil();
	private static Log log = LogFactory.getLog(CallPMUtil.class);
	protected static Log logesapi = LogFactory.getLog(CallPMUtil.class);

	public static String callUserResponse(SecurityType securityType,  String projectId ) throws AxisFault, I2B2Exception {
		RequestMessageType requestMessageType = getI2B2RequestMessage(securityType, projectId);
		OMElement requestElement = null;
		String response =  null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			response = ServiceClient.sendREST(QueryProcessorUtil.getInstance()
					.getProjectManagementCellUrl(), requestElement);
			log.debug("Got Response");
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		log.debug("Returning ProjectType");
		return response;
	}

	public static ProjectType callUserProjectRedcap(SecurityType securityType,  String projectId ) throws AxisFault, I2B2Exception {

		RequestMessageType requestMessageType = getI2B2Hive(securityType, null);
		OMElement requestElement = null;
		ProjectType projectType = null;
		String hive = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			String response = ServiceClient.sendREST(QueryProcessorUtil.getInstance()
					.getProjectManagementCellUrl(), requestElement);
			log.debug("Got Response");

			JAXBElement responseJaxb = jaxbUtil.unMashallFromString(response);

			//CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseXml);
			ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
					.getValue();
			logesapi.debug("CRC's PM call response xml" + response);

			ResponseHeaderType responseHeader = pmRespMessageType
					.getResponseHeader();
			StatusType status = responseHeader.getResultStatus().getStatus();
			String procStatus = status.getType();
			String procMessage = status.getValue();

			if (procStatus.equals("ERROR")) {
				logesapi.info("PM Error reported by CRC web Service " + procMessage);




				int startstr = procMessage.indexOf("this domain") + 12;
				int endstr = procMessage.indexOf(" ", startstr+1) -1;
				hive = procMessage.substring(startstr, endstr);
			}
			
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		securityType.setDomain(hive);
		requestMessageType = getI2B2RequestMessage(securityType, null);
		requestElement = null;
		projectType = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			String response = ServiceClient.sendREST(QueryProcessorUtil.getInstance()
					.getProjectManagementCellUrl(), requestElement);
			log.debug("Got Response");
			projectType = getUserProjectFromResponseRedcap(response, securityType, projectId);
			log.debug("Parsed Projcet Type: " + projectType.getName());
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		log.debug("Returning ProjectType");
		return projectType;
	}


	public static ProjectType callUserProject(SecurityType securityType,  String projectId ) throws AxisFault, I2B2Exception {
		RequestMessageType requestMessageType = getI2B2RequestMessage(securityType, projectId);
		OMElement requestElement = null;
		ProjectType projectType = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			String response = ServiceClient.sendREST(QueryProcessorUtil.getInstance()
					.getProjectManagementCellUrl(), requestElement);
			log.debug("Got Response");
			projectType = getUserProjectFromResponse(response, securityType, projectId);
			log.debug("Parsed Projcet Type: " + projectType.getName());
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		log.debug("Returning ProjectType");
		return projectType;
	}


	public static ProjectType getUserProjectFromResponseRedcap(String responseXml, SecurityType securityType,  String projectId)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = jaxbUtil.unMashallFromString(responseXml);

		//CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		logesapi.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			logesapi.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			logesapi.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConfigureType configureType = (ConfigureType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(),
				ConfigureType.class);
		UserType userType = configureType.getUser();
		List<ProjectType> projectTypeList = userType.getProject();

		ProjectType projectType = null;
		if (projectTypeList != null && projectTypeList.size() > 0) {
			for (ProjectType pType : projectTypeList) {
				for (ParamType paramType : pType.getParam())
				if ((paramType.getName().equalsIgnoreCase("REDCAP_SURVEY_PROJECT")
						&& (paramType.getValue().equalsIgnoreCase(projectId)))) {
					projectType = pType;

					// Add user params to porject params
					projectType.getParam().addAll(userType.getParam());
					break;
				}
			}
			if (projectType == null) {
				throw new I2B2Exception("Redcap not registered to the project["
						+ projectId + "]");
			}
		}

		return projectType;
	}

	public static ProjectType getUserProjectFromResponse(String responseXml, SecurityType securityType,  String projectId)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = jaxbUtil.unMashallFromString(responseXml);

		//CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		logesapi.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			logesapi.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			logesapi.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConfigureType configureType = (ConfigureType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(),
				ConfigureType.class);
		UserType userType = configureType.getUser();
		List<ProjectType> projectTypeList = userType.getProject();

		ProjectType projectType = null;
		if (projectTypeList != null && projectTypeList.size() > 0) {
			for (ProjectType pType : projectTypeList) {
				if (pType.getId().equalsIgnoreCase(projectId)) {
					projectType = pType;

					break;
				}
			}
			if (projectType == null) {
				throw new I2B2Exception("User not registered to the project["
						+ projectId + "]");
			}
		}
		if (configureType.getUser().isIsAdmin())
			projectType.getRole().add("ADMIN");

		return projectType;
	}

	public static boolean getUserIsAdminFromResponse(String responseXml, SecurityType securityType,  String projectId)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = jaxbUtil.unMashallFromString(responseXml);

		//CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		logesapi.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			logesapi.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			logesapi.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConfigureType configureType = (ConfigureType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(),
				ConfigureType.class);
		UserType userType = configureType.getUser();
		return userType.isIsAdmin();
	}
	
	public static UserType getUserFromResponse(String responseXml)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = jaxbUtil.unMashallFromString(responseXml);

		//CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		logesapi.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			logesapi.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			logesapi.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConfigureType configureType = (ConfigureType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(),
				ConfigureType.class);
		return configureType.getUser();
		//return userType.isIsAdmin();
	}
	
	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		//CRCJAXBUtil.getJAXBUtil()
		jaxbUtil.marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		OMElement request = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();

		return request;
	}


	private static RequestMessageType getI2B2Hive(SecurityType securityType,  String projectId) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType =  queryUtil.getMessageHeader();
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		// GetUserInfoType getUserInfoType = null;


		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetAllHive(null));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}
	private static RequestMessageType getI2B2RequestMessage(SecurityType securityType,  String projectId) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType =  queryUtil.getMessageHeader();
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		// GetUserInfoType getUserInfoType = null;
		GetUserConfigurationType userConfig = new GetUserConfigurationType();
		if (projectId != null) {
			userConfig.getProject().add(projectId);
		}

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetUserConfiguration(userConfig));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	public static boolean callIsAdmin(SecurityType securityType, String projectId) throws AxisFault, I2B2Exception {
		RequestMessageType requestMessageType = getI2B2RequestMessage(securityType, projectId);
		OMElement requestElement = null;
		boolean projectType = false;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			String response = ServiceClient.sendREST(QueryProcessorUtil.getInstance()
					.getProjectManagementCellUrl(), requestElement);
			log.debug("Got Response");
			projectType = getUserIsAdminFromResponse(response, securityType, projectId);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		log.debug("Returning ProjectType");
		return projectType;
	}


}
