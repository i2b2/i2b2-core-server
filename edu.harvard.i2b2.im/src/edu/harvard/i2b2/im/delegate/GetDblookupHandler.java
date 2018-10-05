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
package edu.harvard.i2b2.im.delegate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.dao.DblookupDao;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.wdo.DblookupType;
import edu.harvard.i2b2.im.datavo.wdo.DblookupsType;
import edu.harvard.i2b2.im.datavo.wdo.GetDblookupType;
import edu.harvard.i2b2.im.ws.GetDblookupDataMessage;
import edu.harvard.i2b2.im.ws.MessageFactory;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetDblookupHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetDblookupHandler.class);
	private GetDblookupDataMessage getdblookupDataMsg = null;
	private MessageHeaderType msgHdrType = null;
	private GetDblookupType getdblookupType = null;
	private DblookupDao dblookupDao = null;
	private boolean isAdmin = false;

	public GetDblookupHandler(GetDblookupDataMessage requestMsg) throws I2B2Exception{
		try {
			getdblookupDataMsg = requestMsg;
			getdblookupType = getdblookupDataMsg.getDblookupType();
			msgHdrType = getdblookupDataMsg.getMessageHeaderType();
			isAdmin = isAdmin(msgHdrType);
			dblookupDao = new DblookupDao(msgHdrType);
		} catch (JAXBUtilException e) {
			log.error("error setting up GetDblookupHandler");
			throw new I2B2Exception("GetDblookupHandler not configured");
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
		log.info(getdblookupType.getField() + ", " + getdblookupType.getValue());
		if (null == getdblookupType.getField() || getdblookupType.getField().trim().equals("") ||
			null == getdblookupType.getValue() || getdblookupType.getValue().trim().equals("")) {
			String fields = "'field' can't be blank, or 'value' can't be missing or blank!"; //missing 'field' implies 'project_path
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, fields);
			String response = MessageFactory.convertToXMLString(responseMessageType);
			log.warn(fields);
			return response;				
		}
		DblookupsType dblus = new DblookupsType();
		List<DblookupType> response = null;
		try {
			response = dblookupDao.getDblookup(getdblookupType.getField(), getdblookupType.getValue());
		} catch (I2B2DAOException e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		}
		if (null == responseMessageType) { // no db error
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
					log.warn(msg);
					responseMessageType = MessageFactory.createNonStandardResponse(msgHdr, msg);					
				}
			}
		}
        String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}    	
}
