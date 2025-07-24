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
 * 		Wayne Chan
 */
package edu.harvard.i2b2.workplace.delegate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.workplace.dao.DblookupDao;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.DeleteDblookupType;
import edu.harvard.i2b2.workplace.ws.DeleteDblookupDataMessage;
import edu.harvard.i2b2.workplace.ws.MessageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteDblookupHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(DeleteDblookupHandler.class);
	private DeleteDblookupDataMessage deldblookupDataMsg = null;
	private MessageHeaderType msgHdrType = null;
	private DeleteDblookupType deldblookupType = null;
	private DblookupDao dblookupDao = null;
	private boolean isAdmin = false;
	private int numDeleted = -1;

	public DeleteDblookupHandler(DeleteDblookupDataMessage requestMsg) throws I2B2Exception{
		try {
			deldblookupDataMsg = requestMsg;
			deldblookupType = deldblookupDataMsg.DeleteDblookupType();
			msgHdrType = deldblookupDataMsg.getMessageHeaderType();
			isAdmin = isAdmin(msgHdrType);	
			dblookupDao = new DblookupDao(msgHdrType);
		} catch (JAXBUtilException e) {
			log.error("error setting up DeleteDblookupHandler");
			throw new I2B2Exception("DeleteDblookupHandler not configured");
		}
	}
	
	@Override
	public String execute() throws I2B2Exception {
		MessageHeaderType msgHdr = MessageFactory.createResponseMessageHeader(msgHdrType);          
		ResponseMessageType responseMessageType = null;
        String response = null;
		if (!isAdmin) {
			String accessDenied = "Access denied, user not an admin!";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, accessDenied);
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.warn(accessDenied);
			return response;	
		}
		log.info(deldblookupType.getProjectPath() + ", " + deldblookupType.getDomainId() + ", " + deldblookupType.getOwnerId());
		if (null == deldblookupType.getDomainId() || deldblookupType.getDomainId().trim().equals("") ||
			null == deldblookupType.getProjectPath() || deldblookupType.getProjectPath().trim().equals("") ||
			null == deldblookupType.getOwnerId() || deldblookupType.getOwnerId().trim().equals("")) {
			String fields = "'project_path', 'domain_id', or 'owner_id' can't be missing or blank!";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, fields);
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.warn(fields);
			return response;				
		}
		try {
			numDeleted = dblookupDao.deleteDblookup(deldblookupType);
		} catch (I2B2DAOException e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		} catch (I2B2Exception e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		}
		if (-1 == numDeleted) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		} else if (0 == numDeleted) {
			response = "no dblookup row was deleted (could be due to no target row found)!";
			log.warn(response);
			responseMessageType = MessageFactory.createNonStandardResponse(msgHdr, response);
		} else {
			log.info(numDeleted + " row deleted");
			responseMessageType = MessageFactory.createBuildResponse(msgHdr);
		}
		response = MessageFactory.convertToXMLString(responseMessageType);
		return response;		
	}    	
}
