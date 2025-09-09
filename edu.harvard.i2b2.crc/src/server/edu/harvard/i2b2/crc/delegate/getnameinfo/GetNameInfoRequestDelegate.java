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
 *     Mike Mendis
 */
package edu.harvard.i2b2.crc.delegate.getnameinfo;
 
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
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
import edu.harvard.i2b2.crc.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.delegate.pm.PMServiceDriver;
import edu.harvard.i2b2.crc.ejb.ExecRunnable;
import edu.harvard.i2b2.crc.util.CacheUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;


public class GetNameInfoRequestDelegate extends RequestHandlerDelegate {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	protected static Log logesapi = LogFactory.getLog(GetNameInfoRequestDelegate.class);

	/**
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate#handleRequest(java.lang.String)
	 */
	@Override
	public String handleRequest(String requestXml) throws I2B2Exception {
//		PdoQryHeaderType headerType = null;  PsmQryHeaderType
		FindByChildType childType = null;
		String response = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

		try {
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();
			BodyType bodyType = requestMessageType.getMessageBody();

			if (bodyType == null) {
				log.error("null value in body type");
				throw new I2B2Exception("null value in body type");
			}

			// Call PM cell to validate user

			StatusType procStatus = null;
			try {
				SecurityType securityType = null;
				String projectId = null;
				if (requestMessageType.getMessageHeader() != null) {
					if (requestMessageType.getMessageHeader().getSecurity() != null) {
						securityType = requestMessageType.getMessageHeader()
								.getSecurity();
					}
					projectId = requestMessageType.getMessageHeader()
							.getProjectId();
				}

				if (securityType == null) {
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
							.setValue("Request message missing user/password");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}
				if (projectId == null) {
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus.setValue("Missing <project_id>");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}

				PMServiceDriver pmServiceDriver = new PMServiceDriver();
				ProjectType projectType = pmServiceDriver.checkValidUser(
						securityType, projectId);
				// projectType.getRole()
				if (projectType == null) {
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
							.setValue("Invalid user/password for the given project ["
									+ projectId + "]");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}

				log.debug("project name from PM " + projectType.getName());
				//logesapi.debug("project id from PM " + projectType.getId());
				if (projectType.getRole() != null) {
					log.debug("project role from PM "
							+ projectType.getRole().get(0));
					this.putRoles(projectId, securityType.getUsername(),
							securityType.getDomain(), projectType.getRole());

					//TODO removed cache
					//Node rootNode = CacheUtil.getCache().getRoot();
					//List<String> roles = (List<String>) rootNode
					//		.get(securityType.getDomain() + "/" + projectId
					//				+ "/" + securityType.getUsername());
					List<String> roles = (List<String>) CacheUtil
							.get(securityType.getDomain() + "/" + projectId
									+ "/" + securityType.getUsername());
					if (roles != null) {
						log.debug("User Roles count " + roles.size());
					}
					
					ParamUtil paramUtil = new ParamUtil();
					paramUtil.clearParam(projectId, securityType.getUsername(), securityType.getDomain(), ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
					if (projectType.getParam() != null) {
						for (ParamType param : projectType.getParam()) { 
							if (param.getName() != null && param.getName().trim().equalsIgnoreCase(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION))  {
								paramUtil.putParam(projectId, securityType.getUsername(), securityType.getDomain(),ParamUtil.CRC_ENABLE_UNITCD_CONVERSION,param);
								String unitCdCache = paramUtil.getParam(projectId, securityType.getUsername(), securityType.getDomain(),ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
								//logesapi.debug("CRC param stored in the cache Project Id [" + projectId + "] user [" + securityType.getUsername() + "] domain [" + securityType.getDomain() + "] " + ParamUtil.CRC_ENABLE_UNITCD_CONVERSION  + "[" + unitCdCache + "]" );
								break;
							}
						}
					}

					
				} else {

					log.error("Project role not set for the user ");

				}
			} catch (AxisFault e) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus.setValue("Could not connect to server["
						+ e.getDetail() + "]");
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				return response;
			} catch (I2B2Exception e) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus
						.setValue("Project Management cell interface error: ["
								+ e.getMessage() + "]");
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				return response;
			} catch (JAXBUtilException e) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus
						.setValue("Message error from Project Management cell["
								+ e.getMessage() + "]");
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				return response;
			}

			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			childType = (FindByChildType) unWrapHelper
					.getObjectByClass(
							bodyType.getAny(),
							edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType.class);

			BodyType responseBodyType = null;
			
			GeNameInfoHandler handler = new GeNameInfoHandler(
					requestXml);
			responseBodyType = handler.execute();
			/*
			if (childType.getRequestType().equals(
					PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST)) {
				GetPDOFromInputListHandler handler = new GetPDOFromInputListHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType.getRequestType().equals(
					PdoRequestTypeType.GET_OBSERVATIONFACT_BY_PRIMARY_KEY)) {
				GetObservationFactFromPrimaryKeyHandler handler = new GetObservationFactFromPrimaryKeyHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType.getRequestType().equals(
					PdoRequestTypeType.GET_PDO_TEMPLATE)) {
				GetPDOTemplateHandler handler = new GetPDOTemplateHandler(
						requestXml);
				responseBodyType = handler.execute();
			}
			*/
			procStatus = new StatusType();
			procStatus.setType("DONE");
			procStatus.setValue("DONE");

			long startTime = System.currentTimeMillis();
			response = I2B2MessageResponseFactory.buildResponseMessage(
					requestXml, procStatus, responseBodyType, true);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			log.debug("Total time to pdo  jaxb  " + totalTime);

		} catch (JAXBUtilException e) {
			log.error("JAXBUtil exception", e);
			StatusType procStatus = new StatusType();
			procStatus.setType("ERROR");
			procStatus.setValue(requestXml + "\n\n"
					+ StackTraceUtil.getStackTrace(e));
			try {
				response = I2B2MessageResponseFactory.buildResponseMessage(
						null, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
		} catch (I2B2Exception e) {
			log.error("I2B2Exception", e);
			StatusType procStatus = new StatusType();
			procStatus.setType("ERROR");
			procStatus.setValue(StackTraceUtil.getStackTrace(e));
			try {
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
		} catch (Throwable e) {
			log.error("Throwable", e);
			StatusType procStatus = new StatusType();
			procStatus.setType("ERROR");
			procStatus.setValue(StackTraceUtil.getStackTrace(e));
			try {
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
		}
		return response;
	}
}
