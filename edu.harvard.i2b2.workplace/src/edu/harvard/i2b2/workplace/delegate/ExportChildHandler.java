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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.ExportChildType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.FolderDao;
import edu.harvard.i2b2.workplace.ws.ExportChildDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;


public class ExportChildHandler extends RequestHandler {
	private  ExportChildDataMessage  exportChildMsg = null;
	private ExportChildType exportChildType = null;
	private ProjectType projectInfo = null;
	
	public ExportChildHandler(ExportChildDataMessage requestMsg) throws I2B2Exception {
			exportChildMsg = requestMsg;
			exportChildType = requestMsg.getExportChildType();	

			// test bad username   -- good 2/1/08	
		//	exportChildMsg.getMessageHeaderType().getSecurity().setUsername("bad");
			projectInfo = getRoleInfo( requestMsg.getMessageHeaderType());	
			setDbInfo(requestMsg.getMessageHeaderType());
	}
	
	@Override
	public String execute() throws I2B2Exception {
		// call ejb and pass input object
		FolderDao exportChildDao = new FolderDao();
		ResponseMessageType responseMessageType = null;
		String numExportd = null;
		
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(exportChildMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		else {	
			
			try {
				numExportd = exportChildDao.exportNode(exportChildType, projectInfo, getSecurityType());
			} catch (I2B2DAOException e) {
				log.error("ExportChildHandler received I2B2DAO exception from DAO");
				responseMessageType = MessageFactory.doBuildErrorResponse(exportChildMsg.getMessageHeaderType(), "Database error");
			} catch (I2B2Exception e) {
				log.error("ExportChildHandler received I2B2 exception from DAO");
				responseMessageType = MessageFactory.doBuildErrorResponse(exportChildMsg.getMessageHeaderType(), "Database error");
			}
		
		}
		
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numExportd == null) { //.size() == 0) {
				log.error("export object not found");
				responseMessageType = MessageFactory.doBuildErrorResponse(exportChildMsg.getMessageHeaderType(), "Node not found");
			}
			else {
				log.debug("export object  found");

				//	String jaxb = XMLUtil.convertDOMElementToString((Element) numExportd.getContent().get(0));
							
//				log.debug("from raj " + XMLUtil.convertDOMToString((Document) numExportd.getContent().get(0)));
				
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(exportChildMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponseRequestXML(messageHeader, numExportd);
			}
		}
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
	}
    
}
