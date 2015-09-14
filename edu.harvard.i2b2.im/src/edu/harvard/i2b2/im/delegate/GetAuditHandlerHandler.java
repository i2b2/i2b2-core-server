/*
 * Copyright (c) 2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Creator:
 * 		Neha Patel
 */
package edu.harvard.i2b2.im.delegate;

import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.dao.IMDbDao;
import edu.harvard.i2b2.im.dao.IMKey;
import edu.harvard.i2b2.im.dao.PdoDao;
import edu.harvard.i2b2.im.ws.GetAuditRequestMessage;
import edu.harvard.i2b2.im.ws.MessageFactory;
import edu.harvard.i2b2.im.ws.IsKeySetRequestMessage;
import edu.harvard.i2b2.im.ws.PDORequestMessage;
import edu.harvard.i2b2.im.ws.PDOResponseMessage;

import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.im.datavo.pdo.PidSet;
import edu.harvard.i2b2.im.datavo.pdo.query.PageByPatientType;
import edu.harvard.i2b2.im.datavo.pdo.query.PageRangeType;
import edu.harvard.i2b2.im.datavo.pdo.query.PageType;
import edu.harvard.i2b2.im.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;
import edu.harvard.i2b2.im.datavo.wdo.AuditsType;
import edu.harvard.i2b2.im.datavo.wdo.GetAuditType;
import edu.harvard.i2b2.im.datavo.wdo.IsKeySetType;

public class GetAuditHandlerHandler extends RequestHandler {
	private String userId = null;
	private ProjectType projectInfo = null;

	private PdoDao imDao = null;

	private GetAuditRequestMessage  auditRequestMsg;
	private GetAuditType requestType;
	public GetAuditHandlerHandler(GetAuditRequestMessage requestMsg) throws I2B2Exception{

		try {
			imDao = new PdoDao();
			auditRequestMsg = requestMsg;
			requestType = requestMsg.getAuditType();
			userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());
			//If projectInfo is null than get project from allprojects.
			
			if (projectInfo == null)
			{
				projectInfo = getAllProjectsInfo(requestMsg.getMessageHeaderType(), requestType.getProjectId());	
				if (projectInfo != null)
				{
					projectInfo.getRole().add("ADMIN");
					requestMsg.getMessageHeaderType().setProjectId(requestType.getProjectId());
				}
			}

//			projectInfo = getAllProjectsInfo(requestMsg.getMessageHeaderType());	
			setDbInfo(requestMsg.getMessageHeaderType());

		} catch (Exception e) {
			log.error("error setting up GetAuditHandlerHandler");
			throw new I2B2Exception("GetAuditHandlerHandler not configured");
		} 
	}

	public String execute() throws I2B2Exception{

		// call ejb and pass input object
		//IMDao foldersDao = new IMDao();
		ResponseMessageType responseMessageType = null;
		String errResponse = "";
		Boolean errorFlag = false;

		// check to see if we have userId(if not indicates problem)
		if(userId == null) {
			log.error("user Id is null");
			responseMessageType = MessageFactory.doBuildErrorResponse(auditRequestMsg.getMessageHeaderType(), "PM service is not responding");
		}



		// Error flag has been set to true, return a error response with appropriate message
		if(errorFlag){
			responseMessageType = MessageFactory.doBuildErrorResponse(auditRequestMsg.getMessageHeaderType(), errResponse);
			errResponse = MessageFactory.convertToXMLString(responseMessageType);
			return errResponse;	
		}
		String response = null;

//		PDOResponseMessage protectedDataMsg = new PDOResponseMessage();

		try {
			//MM

			AuditsType patientDataType = imDao.getAudit(auditRequestMsg.getAuditType(), userId, projectInfo,  this.getDbInfo());
			//response = IMKey.isKeySet(projectInfo);

			


			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(auditRequestMsg.getMessageHeaderType());

			responseMessageType = MessageFactory.createBuildResponseAudits(messageHeader, patientDataType);
			//responseMessageType.setMessageBody(protectedDataMsg.getBodyType(patientDataType));
			//response = MessageFactory.convertToXMLString(responseMessageType);
	
			
			
		//	MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
		//	responseMessageType = MessageFactory.createBuildResponseKeySet(messageHeader, keySet);
			response = MessageFactory.convertToXMLString(responseMessageType);


			//response = protectedDataMsg.doBuildXML(patientDataResponsse,pdoRequestMsg.getMessageHeaderType());
		}
		catch (Exception e)
		{
			responseMessageType = MessageFactory.doBuildErrorResponse(auditRequestMsg.getMessageHeaderType(), e.getMessage());
			response = MessageFactory.convertToXMLString(responseMessageType);

		}

		return response;		
			
	}    	
}
