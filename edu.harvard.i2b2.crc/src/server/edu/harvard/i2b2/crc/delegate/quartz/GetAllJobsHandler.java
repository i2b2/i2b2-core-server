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
 *     Mike Mendis
 */
package edu.harvard.i2b2.crc.delegate.quartz;


import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.axis2.MessageFactory;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryBreakdownTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.SetFinderConnection;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.delegate.JobReqHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * GetQuartzJobHandler class implements execute method $Id:
 * GetQuartzJobHandler.java,v 
 * 
 *
 */
public class GetAllJobsHandler extends JobReqHandler  {
	private JobType setJobRequestType = null;
	private SecurityType userRequestType = null;
	private MessageHeaderType msgHdrType = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public GetAllJobsHandler(String requestXml) throws I2B2Exception {
		try {
			setJobRequestType = (JobType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.setfinder.query.JobType.class);
			this.setDataSourceLookup(requestXml);



			RequestMessageType requestMsg =  getI2B2RequestMessageType( requestXml);
			this.userRequestType = requestMsg.getMessageHeader()
					.getSecurity();
			//requestMsg.

		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 * @throws I2B2Exception 
	 * 
	 * @see edu.harvard.i2b2.crc.delegate.GetQuartzJobHandler#execute()
	 */
	@Override
	public String execute() throws I2B2Exception {
		//log.info(" execute()");
		MessageHeaderType msgHdr = MessageFactory.createResponseMessageHeader(msgHdrType);          
		ResponseMessageType responseMessageType = null;

		//JobsType dblus = new JobsType();
		JobsType response = null;
		SchedulerInfoBean scheduler = new SchedulerInfoBean();
		
		try {
			 scheduler = new SchedulerInfoBean();
				response = scheduler.getAllJobsScheduler(SchedulerFactory.getDefaultScheduler(), getDataSourceLookup(),userRequestType);

			//response = dblookupDao.findDblookups();
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
			} else { 
				//Iterator<JobType> it = response.iterator();
				if (response.getJob() != null) {
					
					responseMessageType = MessageFactory.createBuildResponse(msgHdr, response);
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
	/*
	@Override
	public String execute() throws I2B2Exception {
		MessageHeaderType msgHdr = MessageFactory.createResponseMessageHeader(msgHdrType);          

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String responseString = null;
		BodyType bodyType = new BodyType();
		JobsType masterResponseType = null;
		
		SchedulerInfoBean scheduler = new SchedulerInfoBean();

	//	MessageHeaderType msgHdr = MessageFactory.createResponseMessageHeader(msgHdrType);          
		ResponseMessageType responseMessageType = null;
		JobsType dblus = new JobsType();

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
			}  else { 
				Iterator<JobType> it = response.iterator();
				if (it.hasNext()) {
					do {
						JobType dblu = it.next();
						dblus.getJob().add(dblu);
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
		
		
		
		/*
		
		try {
			long initialTime = System.currentTimeMillis();
			//           masterResponseType = queryInfoLocal.getQueryMasterListFromUserId(getDataSourceLookup(),userRequestType);
			//SchedulerInfoBean scheduler = new SchedulerInfoBean();
			masterResponseType = scheduler.getAllJobsScheduler(SchedulerFactory.getDefaultScheduler(), getDataSourceLookup(),userRequestType);
			long finalTime = System.currentTimeMillis();
			long diffTimeMill = finalTime - initialTime;
			long diffTime = diffTimeMill / 1000;
			log.debug(" EJB Diff mill =" + diffTimeMill + " diffTime =" +
					diffTime);
			//masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
		} catch (Exception e) {
			log.debug(e.getMessage());
			//masterResponseType = new MasterResponseType();
			//masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(masterResponseType); //of.createResponse(masterResponseType));
		}

		return bodyType;		
		*/
	//}
 

	// function to build StatusType for the given message
	private StatusType buildStatusType(String statusTypeString, String message) {
		StatusType statusType = new StatusType();
		statusType.setType(statusTypeString);
		statusType.setValue(message);

		return statusType;
	}
}
