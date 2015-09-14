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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.FoldersType;
import edu.harvard.i2b2.workplace.datavo.wdo.FindByChildType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.GetNameInfoDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;

public class GetNameInfoHandler extends RequestHandler {
	private GetNameInfoDataMessage  getFoldersMsg = null;
	private FindByChildType getReturnType = null;
	private String userId = null;
	private ProjectType projectInfo = null;

	public GetNameInfoHandler(GetNameInfoDataMessage requestMsg) throws I2B2Exception{
		try {
			
			getFoldersMsg = requestMsg;
			getReturnType = requestMsg.getFindByRequestType();
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
		FoldersType folders = new FoldersType();
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
		
		// validating all the request parameters
		if(!errorFlag && getReturnType.getMatchStr() == null){
			errorFlag = true;
			errResponse = "'match_str' element is missing. Please verify your request";
			log.debug("'match_str' is missing. Please verify your request");
		}
		
		// Validating Search String to ensure its not empty or has only spaces as string
		if(!errorFlag && (getReturnType.getMatchStr().getValue().isEmpty() || getReturnType.getMatchStr().getValue().trim().length()<=0)){
			errorFlag = true;
			errResponse = "Please enter a valid search string. String should be atleast 1 character long";
			log.debug("Please enter a valid search string. String should be atleast 1 character long");
		}
	
		// Validating Strategy to ensure its not null or empty or has only spaces as string
		if(!errorFlag && (getReturnType.getMatchStr().getStrategy() ==null || getReturnType.getMatchStr().getStrategy().isEmpty() 
				|| getReturnType.getMatchStr().getStrategy().trim().length()<=0)){
			errorFlag = true;
			errResponse = "Strategy is missing. Please select a valid strategy";
			log.debug("Strategy is missing. Please select a valid strategy");
		}
		
		if (!errorFlag && (getReturnType.getMatchStr().getStrategy()!=null)){
			String strategy = getReturnType.getMatchStr().getStrategy();
			if(!strategy.equalsIgnoreCase("exact") && !strategy.equalsIgnoreCase("left") && !strategy.equalsIgnoreCase("right") && !strategy.equalsIgnoreCase("contains") )
				errorFlag = true;
				errResponse = "Incorrect strategy provided. Please verify your request";
				log.debug("Incorrect strategy provided. Please verify your request");			
		}
		
		// Validating Category to ensure its not null or empty or has only spaces as string
		if(!errorFlag && (getReturnType.getCategory() == null || getReturnType.getCategory().isEmpty() || getReturnType.getCategory().trim().length()<=0)){
			errorFlag = true;
			errResponse = "Please select a valid category to perform search in";
			log.debug("Please select a valid category to perform search in");			
		}
		
		if(!errorFlag && getReturnType.getMax()!=null && (getReturnType.getMax()<=0 )){
			errorFlag = true;
			errResponse = "Please enter a valid 'max' value. Max number should be greater than 0";
			log.debug("Please enter a valid 'max' value. Max number should be greater than 0");			
		}
		
		// If userid is same as category then user is accessing his/her own directory
		// if userid is not the same as category
		// then either user is trying to access a shared directory
		// or user is a manager
		if(!errorFlag && (!userId.toLowerCase().equals(getReturnType.getCategory().toLowerCase())) && !getReturnType.getCategory().equals("@")){
			
			// Check if user is a manager
			boolean managerRole = false;
			for(String param :projectInfo.getRole()) {
				if(param.equalsIgnoreCase("manager")) {
					managerRole = true;
					break;
				}
			}
			
			// if user is not manager and user is not accessing the shared directory then throw error
			if(managerRole == false && !foldersDao.isShared(getReturnType.getCategory(), projectInfo, this.getDbInfo())) {
				errorFlag = true;
				errResponse =  "User does not have correct privileges";
				log.debug( "User does not have correct privileges");
			}
		}
		
		// Error flag has been set to true, return a error response with appropriate message
		if(errorFlag){
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			errResponse = MessageFactory.convertToXMLString(responseMessageType);
			return errResponse;	
		}
		
		List response = null;
		try {
				response = foldersDao.findWorkplaceByKeyword(getReturnType, userId, projectInfo, this.getDbInfo());
		} catch (Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "Database error");
		}
		
		// no db error, but response is empty
		if ((response == null) && (responseMessageType == null)) {
			log.debug("query results are empty");
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "Query results are empty");
		}
		// no db error; non-empty response received
		else if(responseMessageType == null) {
			// No errors, non-empty response received
			// If max is specified, check that response is not > max
			if(getReturnType.getMax() != null && response.size() > getReturnType.getMax()) {
				// max exceeded send error message
					log.debug("Max request size of " + getReturnType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "MAX_EXCEEDED");
			}
				// otherwise send results
			else {
					Iterator it = response.iterator();
					while (it.hasNext())
					{
						FolderType node = (FolderType)it.next();
						folders.getFolder().add(node);
					}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,folders);
			}  
		}        
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}    	
}
