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
import edu.harvard.i2b2.crc.axis2.GetAllDblookupsDataMessage;
import edu.harvard.i2b2.crc.dao.DblookupDao;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupsType;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetAllDblookupsHandler extends DbLookupReqHandler {
	private static Log log = LogFactory.getLog(GetAllDblookupsHandler.class);
	private GetAllDblookupsDataMessage dblookupsDataMsg = null;
	private MessageHeaderType msgHdrType = null;
	private DblookupDao dblookupDao = null;
	private boolean isAdmin = false;

	public GetAllDblookupsHandler(GetAllDblookupsDataMessage requestMsg) throws I2B2Exception{
		try {
			dblookupsDataMsg = requestMsg;
			msgHdrType = dblookupsDataMsg.getMessageHeaderType();
			isAdmin = isAdmin(msgHdrType);	
			dblookupDao = new DblookupDao(msgHdrType);
		} catch (JAXBUtilException e) {
			log.error("error setting up GetAllDblookupsHandler");
			throw new I2B2Exception("GetAllDblookupsHandler not configured");
		}
	}
	
	@Override
	public String execute() throws I2B2Exception {
		//log.info(" execute()");
		MessageHeaderType msgHdr = MessageFactory.createResponseMessageHeader(msgHdrType);          
		ResponseMessageType responseMessageType = null;
		if (!isAdmin) {
			String response = null;
			String accessDenied = "Access denied, user not an admin!";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, accessDenied);
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.warn(accessDenied);
			return response;	
		}
		DblookupsType dblus = new DblookupsType();
		List<DblookupType> response = null;
		try {
			response = dblookupDao.findDblookups();
		} catch (I2B2DAOException e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		}
		if (responseMessageType == null) { // no db error
			if (null == response) { // but response is empty
				String emptyResult = "query results are empty";
				log.warn(emptyResult);
				responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, emptyResult);
			} else { 
				Iterator<DblookupType> it = response.iterator();
				if (it.hasNext()) {
					do {
						DblookupType dblu = (DblookupType) it.next();
						dblus.getDblookup().add(dblu);
					} while (it.hasNext());
					responseMessageType = MessageFactory.createBuildResponse(msgHdr, dblus);
				} else {
					String msg = "No dblookup row was found!";
					log.info(msg);
					responseMessageType = MessageFactory.createNonStandardResponse(msgHdr, msg);					
				}
			}
		}
        String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}    	
}
