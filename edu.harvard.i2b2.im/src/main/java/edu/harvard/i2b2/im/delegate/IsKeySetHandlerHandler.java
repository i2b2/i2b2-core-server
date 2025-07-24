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
 * Creator:
 * 		Neha Patel
 */
package edu.harvard.i2b2.im.delegate;

import java.util.Iterator;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.dao.IMKey;
import edu.harvard.i2b2.im.ws.MessageFactory;
import edu.harvard.i2b2.im.ws.IsKeySetRequestMessage;

import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;
import edu.harvard.i2b2.im.datavo.wdo.IsKeySetType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;

public class IsKeySetHandlerHandler extends RequestHandler {
	private String userId = null;
	private ProjectType projectInfo = null;

	private IsKeySetRequestMessage  getFoldersMsg;
	private SetKeyType requestType;
	public IsKeySetHandlerHandler(IsKeySetRequestMessage requestMsg) throws I2B2Exception{

		try {

			getFoldersMsg = requestMsg;
			requestType = requestMsg.isKeySet();
			userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
			//projectInfo = getAllProjectsInfo(requestMsg.getMessageHeaderType());	
			

			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());
			//If projectInfo is null than get project from allprojects.
			
			if (projectInfo == null)
			{
				projectInfo = getAllProjectsInfo(requestMsg.getMessageHeaderType(), requestType.getProjectId());	
				if (projectInfo != null)
					projectInfo.getRole().add("ADMIN");
			}
			

		} catch (Exception e) {
			log.error("error setting up getNameInfoHandler");
			throw new I2B2Exception("GetSetKeyHandler not configured");
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
			response = IMKey.isKeySet(projectInfo, requestType.getProjectId());
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
			else if (response == -1){
				errResponse = "Project not found";
				log.error(errResponse);
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			}
			else if (response == 1) {
				// Key  set
				IsKeySetType keySet = new IsKeySetType();
				keySet.setActive(true);
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponseKeySet(messageHeader, keySet);
			}
			else {
				IsKeySetType keySet = new IsKeySetType();
				keySet.setActive(false);
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponseKeySet(messageHeader, keySet);
			}
		}
		String responseWdo = null;
		responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;		
	}    	
}
