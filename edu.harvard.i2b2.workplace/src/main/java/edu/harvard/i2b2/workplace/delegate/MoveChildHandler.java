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
import edu.harvard.i2b2.workplace.datavo.wdo.ChildType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.MoveChildDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;

public class MoveChildHandler extends RequestHandler {
	private MoveChildDataMessage  childMsg = null;
	private ChildType childType = null;
	private ProjectType projectInfo = null;
	
	public MoveChildHandler(MoveChildDataMessage requestMsg) throws I2B2Exception{
		
		childMsg = requestMsg;
		childType = requestMsg.childType();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());

	}
	
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		FolderDao moveChildDao = new FolderDao();
		ResponseMessageType responseMessageType = null;
		int numMoved= -1;

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		
		else {	
			try {
				numMoved = moveChildDao.moveNode(childType, projectInfo, this.getDbInfo());
			} catch (Exception e1) {
				log.error("MoveChildHandler received exception");
				responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numMoved == 0) {
				log.error("mvoe object not found");
				responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "Node not found");
			}
			else if (numMoved == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(childMsg.getMessageHeaderType());          
//				responseMessageType = MessageFactory.createBuildResponse(messageHeader, null);
				responseMessageType = MessageFactory.createBuildResponse(messageHeader);
			}
		}
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}
    
}
