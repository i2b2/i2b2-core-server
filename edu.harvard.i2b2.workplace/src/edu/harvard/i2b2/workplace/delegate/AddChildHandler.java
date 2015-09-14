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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.DeleteChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.AddChildDataMessage;
import edu.harvard.i2b2.workplace.ws.DeleteChildDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;

public class AddChildHandler extends RequestHandler {
	private AddChildDataMessage  addChildMsg = null;
	private FolderType addChildType = null;
	private ProjectType projectInfo = null;
	
	public AddChildHandler(AddChildDataMessage requestMsg) throws I2B2Exception{
		
		addChildMsg = requestMsg;
		addChildType = requestMsg.getAddChildType();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());

	}
	
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		FolderDao addChildDao = new FolderDao();
		ResponseMessageType responseMessageType = null;
		int numAdded = -1;

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		
		else {	
			try {
				numAdded = addChildDao.addNode(addChildType, projectInfo, this.getDbInfo());
			} catch (Exception e1) {
				e1.printStackTrace();
				log.error("AddChildHandler received exception");
				responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numAdded == 0) {
				log.error("object not inserted");
				responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "Node not found");
			}
			else if (numAdded == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(addChildMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader, null);
			}
		}
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}
    
}