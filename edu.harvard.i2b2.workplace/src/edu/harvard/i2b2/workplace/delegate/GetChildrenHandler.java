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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.FoldersType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetChildrenType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.GetChildrenDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;


public class GetChildrenHandler extends RequestHandler {
	private GetChildrenDataMessage  getChildrenMsg = null;
	private GetChildrenType getChildrenType = null;
	private ProjectType projectInfo = null;

	public GetChildrenHandler(GetChildrenDataMessage requestMsg) throws I2B2Exception{

		getChildrenMsg = requestMsg;
		getChildrenType = requestMsg.getChildrenType();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());
	}
	
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		FolderDao childDao = new FolderDao();
		FoldersType folders = new FoldersType();
		ResponseMessageType responseMessageType = null;
		
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		List response = null;	
		try {
			response = childDao.findChildrenByParent(getChildrenType, projectInfo, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			log.error(e1.getMessage());
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
		}

		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Query results are empty");
			}
//			 No errors, non-empty response received
			// If max is specified, check that response is not > max
			else if(getChildrenType.getMax() != null) {
				// if max exceeded send error message
				if(response.size() > getChildrenType.getMax()){
					log.debug("Max request size of " + getChildrenType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "MAX_EXCEEDED");
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
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,folders);
				}       
			}

			// max not specified so send results
			else {
				Iterator it = response.iterator();
				while (it.hasNext())
				{
					FolderType node = (FolderType)it.next();
					if (node.getProtectedAccess() == null)
						node.setProtectedAccess("N");
					folders.getFolder().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,folders);
				
			}     
		}
        String responseWdo = null;
       
		responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		if(responseWdo == null)
			log.error("GetChildren responseWdo is null");
		return responseWdo;
	}
    
}