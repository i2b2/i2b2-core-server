/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.delegate;

import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.loader.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.loader.delegate.pm.PMServiceDriver;

/**
 * PDO query request delegate class $Id: LoaderQueryRequestDelegate.java,v 1.3
 * 2008/02/26 23:09:59 rk903 Exp $
 * 
 * @author rkuttan
 */
public class LoaderQueryRequestDelegate extends RequestHandlerDelegate {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate#handleRequest(java.lang.String)
	 */
	public String handleRequest(String requestXml, RequestHandler requestHandler)
			throws I2B2Exception {
		String response = null;
		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();

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
			ProjectType projectType = null;
			String projectId = null;
			try {

				SecurityType securityType = null;
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

				PMServiceDriver pmServiceDriver = new PMServiceDriver();
				projectType = pmServiceDriver.checkValidUser(securityType,
						projectId);
				if (projectType == null) {
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
							.setValue("Invalid user/password for the given domain");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}

				log.debug("project name from PM " + projectType.getName());
				log.debug("project id from PM " + projectType.getId());
				if (projectType.getRole().get(0) != null) {
					log.debug("Project role from PM "
							+ projectType.getRole().get(0));
				} else {
					log.warn("project role not set for user ["
							+ securityType.getUsername() + "]");
				}

			} catch (AxisFault e) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus.setValue("Could not connect to server");
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				return response;
			} catch (I2B2Exception e) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus
						.setValue("Message error connecting Project Management cell");
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				return response;
			} catch (JAXBUtilException e) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus
						.setValue("Message error from Project Management cell");
				response = I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				return response;
			}

			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();

			BodyType responseBodyType = null;
			if (requestHandler instanceof PublishDataRequestHandler) {
				String irodsStorageResource = null;
				for (ParamType paramType : projectType.getParam()) {

					if (paramType.getName().equalsIgnoreCase(
							"SRBDefaultStorageResource")) {
						irodsStorageResource = paramType.getValue();
						log.debug("param value for SRBDefaultStorageResource"
								+ paramType.getValue());
					}
				}
				((PublishDataRequestHandler) requestHandler)
						.setIrodsDefaultStorageResource(irodsStorageResource);
			}

			responseBodyType = requestHandler.execute();

			procStatus = new StatusType();
			procStatus.setType("DONE");
			procStatus.setValue("DONE");

			response = I2B2MessageResponseFactory.buildResponseMessage(
					requestXml, procStatus, responseBodyType, true);

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