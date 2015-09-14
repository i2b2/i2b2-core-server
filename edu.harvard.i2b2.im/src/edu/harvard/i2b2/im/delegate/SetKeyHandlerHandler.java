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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.dao.IMKey;
import edu.harvard.i2b2.im.ws.MessageFactory;
import edu.harvard.i2b2.im.ws.SetKeyRequestMessage;

import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;

public class SetKeyHandlerHandler extends RequestHandler {
	private String userId = null;
	private ProjectType projectInfo = null;
	private SetKeyRequestMessage  getFoldersMsg;
	private SetKeyType requestType;
	public SetKeyHandlerHandler(SetKeyRequestMessage requestMsg) throws I2B2Exception{

		try {

			getFoldersMsg = requestMsg;
			requestType = requestMsg.setKeyType();
			userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
			
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());
			//If projectInfo is null than get project from allprojects.
			
			if (projectInfo == null)
			{
				projectInfo = getAllProjectsInfo(requestMsg.getMessageHeaderType(), requestType.getProjectId());	
				if (projectInfo != null)
					projectInfo.getRole().add("ADMIN");
			}

		} catch (Exception e) {
			log.error("error setting up SetKeyHandlerHandler");
			throw new I2B2Exception("GetSetKeyHandler not configured");
		} 
	}

	public String execute() throws I2B2Exception{

		// call ejb and pass input object
		ResponseMessageType responseMessageType = null;
		String errResponse = "";
		Boolean errorFlag = false;

		// check to see if we have userId(if not indicates problem)
		if(userId == null) {
			log.error("user Id is null");
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "PM service is not responding");
		}



		// Error flag has been set to true, return a error response with appropriate message
		if(errorFlag){
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			errResponse = MessageFactory.convertToXMLString(responseMessageType);
			return errResponse;	
		}

		int response = -1;
		try {
			//MM
			response = IMKey.setKey(requestType, projectInfo, userId);
			log.debug("My Response is: " + response);
			log.debug("My Project is: " + projectInfo.getId());
		} catch (Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "Database error");
		}

		if(responseMessageType == null) {
			// no db error but response is empty
			
			if (response == -11111){
				errResponse = "User does not have correct privileges set key";
				log.error(errResponse);
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			}
			else if (response < 0) {
				errResponse = "Key does not match project key";
				log.error(errResponse);
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			} else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponsePdo(messageHeader, null);
			}
		}
		String responseWdo = null;
		responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;		
	}    	
}
