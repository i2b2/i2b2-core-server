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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.RenameChildType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.RenameChildDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;


public class RenameChildHandler extends RequestHandler {
	private RenameChildDataMessage  renameChildMsg = null;
	private RenameChildType renameChildType = null;
	private ProjectType projectInfo = null;
	
	public RenameChildHandler(RenameChildDataMessage requestMsg) throws I2B2Exception {
			renameChildMsg = requestMsg;
			renameChildType = requestMsg.getRenameChildType();	

			// test bad username   -- good 2/1/08	
		//	renameChildMsg.getMessageHeaderType().getSecurity().setUsername("bad");
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
			setDbInfo(requestMsg.getMessageHeaderType());
	}
	
	public String execute() throws I2B2Exception {
		// call ejb and pass input object
		FolderDao renameChildDao = new FolderDao();
		ResponseMessageType responseMessageType = null;
		int numRenamed = -1;
		
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(renameChildMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		else {	
			try {
				numRenamed = renameChildDao.renameNode(renameChildType, projectInfo, this.getDbInfo());
			} catch (I2B2DAOException e) {
				log.error("RenameChildHandler received I2B2DAO exception from DAO");
				responseMessageType = MessageFactory.doBuildErrorResponse(renameChildMsg.getMessageHeaderType(), "Database error");
			} catch (I2B2Exception e) {
				log.error("RenameChildHandler received I2B2 exception from DAO");
				responseMessageType = MessageFactory.doBuildErrorResponse(renameChildMsg.getMessageHeaderType(), "Database error");
			}
		}
		
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numRenamed == 0) {
				log.error("rename object not found");
				responseMessageType = MessageFactory.doBuildErrorResponse(renameChildMsg.getMessageHeaderType(), "Node not found");
			}
			else if (numRenamed == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(renameChildMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(renameChildMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader, null);
			}
		}
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}
    
}