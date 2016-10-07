/*
 * Copyright (c) 2016-2017 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Wayne Chan
 */
package edu.harvard.i2b2.crc.delegate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DblookupDao;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DeleteDblookupType;
import edu.harvard.i2b2.crc.axis2.DeleteDblookupDataMessage;
import edu.harvard.i2b2.crc.axis2.MessageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteDblookupHandler extends DbLookupReqHandler {
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
		log.info(" execute()");
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
