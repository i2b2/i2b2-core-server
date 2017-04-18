/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;
 
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmRequestTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType.Condition;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.delegate.ejbpm.EJBPMUtil;
import edu.harvard.i2b2.crc.delegate.pm.CallPMUtil;
import edu.harvard.i2b2.crc.delegate.pm.PMServiceDriver;
import edu.harvard.i2b2.crc.util.CacheUtil;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;

/**
 * Setfinder query request delegate class $Id: QueryRequestDelegate.java,v 1.17
 * 2008/05/08 15:13:45 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryRequestDelegate extends RequestHandlerDelegate {

	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate#handleRequest(java.lang.String)
	 */
	public String handleRequest(String requestXml) throws I2B2Exception {
		PsmQryHeaderType headerType = null;
		String response = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

		try {
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);

			if (jaxbElement == null) {
				throw new I2B2Exception("Request is null after unmashall"
						+ requestXml);
			}

			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();
			BodyType bodyType = requestMessageType.getMessageBody();

			if (bodyType == null) {
				log.error("null value in body type");
				throw new I2B2Exception("null value in body type");
			}
			// Call PM cell to validate user
			ProjectType projectType = null;
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

				// String pmResponse =
				// PMServiceDriver.checkValidUser(securityType);
				PMServiceDriver pmServiceDriver = new PMServiceDriver();
				projectType = pmServiceDriver.checkValidUser(securityType,
						projectId);

				if (projectType == null) {
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
					.setValue("Invalid user/password for the given project["
							+ projectId + "]");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}

				log.debug("project name from PM " + projectType.getName());
				log.debug("project id from PM " + projectType.getId());


				if (projectType.getRole() != null) {
					log.debug("project role from PM "
							+ projectType.getRole().get(0));

					this.putRoles(projectId, securityType.getUsername(),
							securityType.getDomain(), projectType.getRole());

					//TODO removed cache
					//	Node rootNode = CacheUtil.getCache().getRoot();
					//List<String> roles = (List<String>) rootNode
					//		.get(securityType.getDomain() + "/" + projectId
					//				+ "/" + securityType.getUsername());
					List<String> roles = (List<String>) CacheUtil
							.get(securityType.getDomain() + "/" + projectId
									+ "/" + securityType.getUsername());
					if (roles != null) {
						log.debug("User Roles count " + roles.size());
					}
				} else {
					log.error("Project role not set for the user ");

				}

				//check if process_timing_flag is set
				log.debug("check if process_timing_flag is set");
				LogTimingUtil.clearPocessTiming(projectId, securityType.getUsername(), securityType.getDomain());
				ParamUtil paramUtil = new ParamUtil();
				paramUtil.clearParam(projectId, securityType.getUsername(), securityType.getDomain(), ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
				if (projectType.getParam() != null) {
					for (ParamType param : projectType.getParam()) { 
						if (param.getName() != null && param.getName().trim().equalsIgnoreCase(LogTimingUtil.PM_ENABLE_PROCESS_TIMING)) {
							//this.putPocessTiming(projectId, securityType.getUsername(), securityType.getDomain(), param);
							LogTimingUtil.putPocessTiming(projectId, securityType.getUsername(), securityType.getDomain(), param);
							String cacheValue = LogTimingUtil.getPocessTiming(projectId, securityType.getUsername(), securityType.getDomain());
							log.debug("CRC param stored in the cache Project Id [" + projectId + "] user [" + securityType.getUsername() + "] domain [" + securityType.getDomain() + "] " + ParamUtil.PM_ENABLE_PROCESS_TIMING  + "[" + cacheValue + "]" );

						} else if (param.getName() != null && param.getName().trim().equalsIgnoreCase(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION))  {
							paramUtil.putParam(projectId, securityType.getUsername(), securityType.getDomain(),ParamUtil.CRC_ENABLE_UNITCD_CONVERSION,param);
							String unitCdCache = paramUtil.getParam(projectId, securityType.getUsername(), securityType.getDomain(),ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
							log.debug("CRC param stored in the cache Project Id [" + projectId + "] user [" + securityType.getUsername() + "] domain [" + securityType.getDomain() + "] " + ParamUtil.CRC_ENABLE_UNITCD_CONVERSION  + "[" + unitCdCache + "]" );
						}
					}
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

			// check if the role is DATA_AGG to proceed
			log.debug("check if the role is DATA_AGG to proceed");
			boolean errorFlag = false;
			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			headerType = (PsmQryHeaderType) unWrapHelper
					.getObjectByClass(
							bodyType.getAny(),
							edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
			BodyType responseBodyType = null;
			if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID)) {
				GetQueryMasterListFromUserIdHandler handler = new GetQueryMasterListFromUserIdHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_GROUP_ID)) {
				// check if user have right permission to access this request
				if (projectType != null && projectType.getRole().size() > 0) {
					if ((!projectType.getRole().contains("MANAGER"))) {
						// Not authorized
						procStatus = new StatusType();
						procStatus.setType("ERROR");
						procStatus
						.setValue("Authorization failure, should have MANAGER  role");
						response = I2B2MessageResponseFactory
								.buildResponseMessage(requestXml, procStatus,
										bodyType);
						return response;
					}
				} else {
					// Not authorized
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
					.setValue("Authorization failure, should have MANAGER role");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}

				GetQueryMasterListFromGroupIdHandler handler = new GetQueryMasterListFromGroupIdHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION)) {
				log.debug("Running in " + PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION);
				ParamType lockedParamType = null;
				List<ParamType> paramList = projectType.getParam();
				for (ParamType paramType : paramList) {
					if (paramType.getName().equals(EJBPMUtil.LOCKEDOUT)) {
						lockedParamType = paramType;
						break;
					}
				}
				log.debug("Check if user is locked out");
				if (lockedParamType != null) {
					// Not authorized
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
					.setValue("LOCKEDOUT error: The user account is lockedout at ["
							+ lockedParamType.getValue() + "]");
					bodyType = new BodyType();
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				} else {
					RunQueryInstanceFromQueryDefinitionHandler handler = new RunQueryInstanceFromQueryDefinitionHandler(
							requestXml);
					responseBodyType = handler.execute();

					// check if the response body type has lockedout error
					if (handler.getLockedoutFlag()) {
						procStatus = new StatusType();
						procStatus.setType("ERROR");
						procStatus
						.setValue("LOCKEDOUT error: The user account is lockedout at ["
								+ new Date(System.currentTimeMillis())
								+ "]");
						response = I2B2MessageResponseFactory
								.buildResponseMessage(requestXml, procStatus,
										responseBodyType);
						return response;
					}

					//if (handler.getErrorFlag()) { 
					//	errorFlag = true;
					//}

				}
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_MASTER_ID)) {
				RunQueryInstanceFromQueryMasterHandler handler = new RunQueryInstanceFromQueryMasterHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID)) {
				GetQueryResultInstanceListFromQueryInstanceIdHandler handler = new GetQueryResultInstanceListFromQueryInstanceIdHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_GET_QUERY_INSTANCE_LIST_FROM_QUERY_MASTER_ID)) {
				GetQueryInstanceListFromMasterIdHandler handler = new GetQueryInstanceListFromMasterIdHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_GET_REQUEST_XML_FROM_QUERY_MASTER_ID)) {
				GetRequestXmlFromQueryMasterIdHandler handler = new GetRequestXmlFromQueryMasterIdHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType.getRequestType().equals(
					PsmRequestTypeType.CRC_QRY_DELETE_QUERY_MASTER)) {
				DeleteQueryMasterHandler handler = new DeleteQueryMasterHandler(
						requestXml);
				log.info("DELETE QUERY MASTER: " + requestXml);
				responseBodyType = handler.execute();
			} else if (headerType.getRequestType().equals(
					PsmRequestTypeType.CRC_QRY_RENAME_QUERY_MASTER)) {
				RenameQueryMasterHandler handler = new RenameQueryMasterHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_UPDATE_RESULT_INSTANCE_DESCRIPTION)) {
				UpdateQueryResultInstanceDescriptionHandler handler = new UpdateQueryResultInstanceDescriptionHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_GET_RESULT_DOCUMENT_FROM_RESULT_INSTANCE_ID)) {
				GetXmlResultFromQueryResultIdHandler handler = new GetXmlResultFromQueryResultIdHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType.getRequestType().equals(
					PsmRequestTypeType.CRC_QRY_GET_RESULT_TYPE)) {
				GetAllQueryResultTypeHandler handler = new GetAllQueryResultTypeHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType
					.getRequestType()
					.equals(
							PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_ANALYSIS_DEFINITION)) {

				RunQueryInstanceFromAnalysisDefinitionHandler handler = new RunQueryInstanceFromAnalysisDefinitionHandler(
						requestXml);
				responseBodyType = handler.execute();

			} else if (headerType.getRequestType().equals(
					PsmRequestTypeType.CRC_QRY_CANCEL_QUERY)) {
				CancelQueryInstanceHandler handler = new CancelQueryInstanceHandler(
						requestXml);
				responseBodyType = handler.execute();
			} else if (headerType.getRequestType().equals(
					PsmRequestTypeType.CRC_QRY_GET_ANALYSIS_PLUGIN_METADATA)) {
				GetAnalysisPluginMetadataTypeHandler handler = new GetAnalysisPluginMetadataTypeHandler(
						requestXml);
				responseBodyType = handler.execute();
			}

			procStatus = new StatusType();
			if (errorFlag == false) { 
				procStatus.setType("DONE");
				procStatus.setValue("DONE");
			} else { 
				procStatus.setType("ERROR");
				procStatus.setValue("ERROR");
			}

			response = I2B2MessageResponseFactory.buildResponseMessage(
					requestXml, procStatus, responseBodyType);

		} catch (JAXBUtilException e) {
			log.error("JAXBUtilException", e);
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
			// throw new I2B2Exception("JAXBUtil exception",e);
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