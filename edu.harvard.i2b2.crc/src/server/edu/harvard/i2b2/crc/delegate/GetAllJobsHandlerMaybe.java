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
package edu.harvard.i2b2.crc.delegate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.axis2.MessageFactory;
import edu.harvard.i2b2.crc.axis2.GetAllDblookupsDataMessage;
import edu.harvard.i2b2.crc.axis2.GetAllJobsDataMessage;
import edu.harvard.i2b2.crc.dao.DblookupDao;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobType;
import edu.harvard.i2b2.crc.delegate.quartz.SchedulerFactory;
import edu.harvard.i2b2.crc.delegate.quartz.SchedulerInfoBean;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetAllJobsHandlerMaybe extends JobReqHandler {
	private static Log log = LogFactory.getLog(GetAllJobsHandlerMaybe.class);
	private GetAllJobsDataMessage jobsDataMsg = null;
	private MessageHeaderType msgHdrType = null;
	private SchedulerInfoBean scheduler = null;
	private boolean isAdmin = false;
	private SecurityType userRequestType = null;

	public GetAllJobsHandlerMaybe(GetAllJobsDataMessage requestMsg) throws I2B2Exception{
		try {
			jobsDataMsg = requestMsg;
			msgHdrType = jobsDataMsg.getMessageHeaderType();
			isAdmin = isAdmin(msgHdrType);	
			//dblookupDao = new DblookupDao(msgHdrType);
			
			
		} catch (Exception e) {
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
		//DblookupsType dblus = new DblookupsType();
		
		
		//masterResponseType = scheduler.getAllJobsScheduler(SchedulerFactory.getDefaultScheduler(), getDataSourceLookup(),userRequestType);

		List<JobType> response = null;
		try {
			 scheduler = new SchedulerInfoBean();
//			response = scheduler.getAllJobsScheduler(SchedulerFactory.getDefaultScheduler(), getDataSourceLookup(),userRequestType);
					//dblookupDao.findDblookups();
		} catch ( Exception e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
		}
		if (responseMessageType == null) { // no db error
			if (null == response) { // but response is empty
				String emptyResult = "query results are empty";
				log.warn(emptyResult);
				responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, emptyResult);
			} /* else { 
				Iterator<DblookupType> it = response.iterator();
				if (it.hasNext()) {
					do {
						DblookupType dblu = it.next();
						dblus.getDblookup().add(dblu);
					} while (it.hasNext());
					responseMessageType = MessageFactory.createBuildResponse(msgHdr, dblus);
				} else {
					String msg = "No dblookup row was found!";
					log.info(msg);
					responseMessageType = MessageFactory.createNonStandardResponse(msgHdr, msg);					
				}
			} */
		}
        String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}    	
}
