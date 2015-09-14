/*
 * Copyright (c) 2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Creator:
 * 		Neha Patel
 */
package edu.harvard.i2b2.workplace.delegate;

import java.util.Iterator;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.ProtectedType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.ProtectedDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;

public class SetProtectedAcessHandler extends RequestHandler {
	private ProtectedDataMessage  getFoldersMsg = null;
	private ProtectedType requestType = null;
	private String userId = null;
	private ProjectType projectInfo = null;

	public SetProtectedAcessHandler(ProtectedDataMessage requestMsg) throws I2B2Exception{
		
		try {
			
			getFoldersMsg = requestMsg;
			requestType = requestMsg.getProtectedRequestType();
			userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
			setDbInfo(requestMsg.getMessageHeaderType());
			
		} catch (JAXBUtilException e) {
			log.error("error setting up getNameInfoHandler");
			throw new I2B2Exception("GetNameInfoHandler not configured");
		} 
	}
	
	public String execute() throws I2B2Exception{
		
		// call ejb and pass input object
		FolderDao foldersDao = new FolderDao();
		ResponseMessageType responseMessageType = null;
		String errResponse = "";
		Boolean errorFlag = false;
		
		// check to see if we have userId(if not indicates problem)
		if(userId == null) {
			log.error("user Id is null");
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "PM service is not responding");
		}
		
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			errorFlag = true;
			errResponse = "User was not validated";
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
		}
		
		// check if user has protected access
		// only person with a role of protected access should be
		// able to set the protected_access on a workplace item
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		if(!protectedAccess){
			errorFlag = true;
			errResponse = "User does not have role of protected_access";
			log.debug("User does not have role of protected_access");			
		}
		
		// validating all the request parameters - index
		else if(requestType.getIndex() == null || requestType.getIndex().trim().length()<=0){
			errorFlag = true;
			errResponse = "folder index is missing. Please verify your request";
			log.debug("folder index is missing. Please verify your request");
		}
		
		// validating all the request parameters - setProtectedAccess
		else if(requestType.getProtectedAccess() == null || requestType.getProtectedAccess().trim().length()<=0){
			errorFlag = true;
			errResponse = "Please specify protected access value. Values can be 'true' or 'false'";
			log.debug("Please specify protected access value. Values can be 'true' or 'false'");
		}
		
		// validating all the request parameters - setProtectedAccess
		else if(requestType.getProtectedAccess() != null && !requestType.getProtectedAccess().trim().equalsIgnoreCase("true") 
				&& !requestType.getProtectedAccess().trim().equalsIgnoreCase("false")){
			errorFlag = true;
			errResponse = "Please specify protected access value. Values can be 'true' or 'false'";
			log.debug("Please specify protected access value. Values can be 'true' or 'false'");
		}
		
		
		// Error flag has been set to true, return a error response with appropriate message
		if(errorFlag){
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			errResponse = MessageFactory.convertToXMLString(responseMessageType);
			return errResponse;	
		}
		
		int response = -1;
		try {
				response = foldersDao.setProtectedAccess(requestType, projectInfo, this.getDbInfo(), userId);
		} catch (Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "Database error");
		}
		
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == -11111){
				errResponse = "Protected access hasn't been set. User does not have correct privileges to the file";
				log.error(errResponse);
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			}
			else if (response < 0) {
				errResponse = "Protected access hasn't been set";
				log.error(errResponse);
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader, null);
				}
			}
			String responseWdo = null;
			responseWdo = MessageFactory.convertToXMLString(responseMessageType);
        	return responseWdo;		
	}    	
}
