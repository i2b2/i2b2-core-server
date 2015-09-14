/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
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
import edu.harvard.i2b2.workplace.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.FoldersType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetReturnType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.GetFoldersDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;

public class GetFoldersByUserIdHandler extends RequestHandler {
	private GetFoldersDataMessage  getFoldersMsg = null;
	private GetReturnType getReturnType = null;
	private String userId = null;
	private ProjectType projectInfo = null;

	public GetFoldersByUserIdHandler(GetFoldersDataMessage requestMsg) throws I2B2Exception{
		getFoldersMsg = requestMsg;
		getReturnType = requestMsg.getReturnType();
		userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());
	}
	
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		FolderDao foldersDao = new FolderDao();
		FoldersType folders = new FoldersType();
		ResponseMessageType responseMessageType = null;

		
		// check to see if we have userId(if not indicates problem)
		if(userId == null) {
			log.error("user Id is null");
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "PM service is not responding");
		}
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}	
		
		List response = null;
		try {
			response = foldersDao.findRootFoldersByUser(getReturnType, userId, projectInfo, this.getDbInfo());
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
			Iterator it = response.iterator();
			while (it.hasNext())
			{
				FolderType node = (FolderType)it.next();
				folders.getFolder().add(node);
			}
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getFoldersMsg.getMessageHeaderType());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,folders);
		}        
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}    	
}
