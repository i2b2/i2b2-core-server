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
import edu.harvard.i2b2.workplace.datavo.wdo.AnnotateChildType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.AnnotateChildDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;


public class AnnotateChildHandler extends RequestHandler {
	private  AnnotateChildDataMessage  annotateChildMsg = null;
	private AnnotateChildType annotateChildType = null;
	private ProjectType projectInfo = null;
	
	public AnnotateChildHandler(AnnotateChildDataMessage requestMsg) throws I2B2Exception {
			annotateChildMsg = requestMsg;
			annotateChildType = requestMsg.getAnnotateChildType();	

			// test bad username   -- good 2/1/08	
		//	annotateChildMsg.getMessageHeaderType().getSecurity().setUsername("bad");
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
			setDbInfo(requestMsg.getMessageHeaderType());
	}
	
	public String execute() throws I2B2Exception {
		// call ejb and pass input object
		FolderDao annotateChildDao = new FolderDao();
		ResponseMessageType responseMessageType = null;
		int numAnnotated = -1;
		
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(annotateChildMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		else {	
			try {
				numAnnotated = annotateChildDao.annotateNode(annotateChildType, projectInfo, this.getDbInfo());
			} catch (I2B2DAOException e) {
				log.error("AnnotateChildHandler received I2B2DAO exception from DAO");
				responseMessageType = MessageFactory.doBuildErrorResponse(annotateChildMsg.getMessageHeaderType(), "Database error");
			} catch (I2B2Exception e) {
				log.error("AnnotateChildHandler received I2B2 exception from DAO");
				responseMessageType = MessageFactory.doBuildErrorResponse(annotateChildMsg.getMessageHeaderType(), "Database error");
			}
		}
		
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numAnnotated == 0) {
				log.error("annotate object not found");
				responseMessageType = MessageFactory.doBuildErrorResponse(annotateChildMsg.getMessageHeaderType(), "Node not found");
			}
			else if (numAnnotated == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(annotateChildMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(annotateChildMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader, null);
			}
		}
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}
    
}