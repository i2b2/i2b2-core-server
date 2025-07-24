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
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.workplace.delegate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
//import edu.harvard.i2b2.workplace.datavo.wdo.DeleteChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.AddChildDataMessage;
//import edu.harvard.i2b2.workplace.ws.DeleteChildDataMessage;
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
	
	@Override
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
//				responseMessageType = MessageFactory.createBuildResponse(messageHeader, null);
				responseMessageType = MessageFactory.createBuildResponse(messageHeader);
			}
		}
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}
    
}
