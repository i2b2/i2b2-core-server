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
import edu.harvard.i2b2.crc.axis2.MessageFactory;
import edu.harvard.i2b2.crc.axis2.SetDblookupDataMessage;
import edu.harvard.i2b2.crc.dao.DblookupDao;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.SetDblookupType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SetDblookupHandler extends DbLookupReqHandler {
    private static Log log = LogFactory.getLog(SetDblookupHandler.class);
	private SetDblookupDataMessage  setdblookupDataMsg = null;
	private MessageHeaderType msgHdrType = null;
	private SetDblookupType dblookupType = null;
	private DblookupDao dblookupDao = null;
	private boolean isAdmin = false;
	private int numInserted = -1;

	public SetDblookupHandler(SetDblookupDataMessage requestMsg) throws I2B2Exception{
		try {
			setdblookupDataMsg = requestMsg;
			dblookupType = requestMsg.setDblookupType();
			msgHdrType = setdblookupDataMsg.getMessageHeaderType();
			isAdmin = isAdmin(msgHdrType);	
			dblookupDao = new DblookupDao(msgHdrType);
		} catch (JAXBUtilException e) {
			log.error("error setting up SetDblookupHandler");
			throw new I2B2Exception("SetDblookupHandler not configured");
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
		log.info(dblookupType.getProjectPath() + ", " + dblookupType.getDomainId() + ", " + dblookupType.getOwnerId() + ", " + 
				 dblookupType.getDbFullschema() + ", " + dblookupType.getDbDatasource() + ", " + dblookupType.getDbServertype() + ", " + 
				 dblookupType.getDbNicename());
		if (null == dblookupType.getDomainId() || dblookupType.getDomainId().trim().equals("") ||
			null == dblookupType.getProjectPath() || dblookupType.getProjectPath().trim().equals("") ||
			null == dblookupType.getOwnerId() || dblookupType.getOwnerId().trim().equals("") ||
			null == dblookupType.getDbFullschema() || dblookupType.getDbFullschema().trim().equals("") ||
			null == dblookupType.getDbDatasource() || dblookupType.getDbDatasource().trim().equals("") ||
			null == dblookupType.getDbServertype() || dblookupType.getDbServertype().trim().equals("") ||
			null == dblookupType.getDbNicename() || dblookupType.getDbNicename().trim().equals("")) {
			String fields = "'project_path', 'domain_id', 'owner_id', 'db_fullschema', 'db_datasource', 'db_servertype', or 'db_nicename' can't be missing or blank!";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, fields);
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.warn(fields);
			return response;				
		}
		try {
			numInserted = dblookupDao.setDblookup(dblookupType);
		} catch (I2B2DAOException e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		} catch (I2B2Exception e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		}
		if (-1 == numInserted) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		} else if (0 == numInserted) {
			String notInserted = "dblookup row not inserted!";
			log.error(notInserted);
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, notInserted);
		} else {
			log.info(numInserted + " row set");
			responseMessageType = MessageFactory.createBuildResponse(msgHdr);
		}
		response = MessageFactory.convertToXMLString(responseMessageType);
		return response;
	}    	
}
