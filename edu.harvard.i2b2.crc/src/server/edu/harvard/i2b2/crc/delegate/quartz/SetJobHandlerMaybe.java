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
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryBreakdownTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.SetFinderConnection;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
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
public class SetJobHandlerMaybe extends RequestHandler  {
	private JobType setJobRequestType = null;
	private SecurityType userRequestType = null;
	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public SetJobHandlerMaybe(String requestXml) throws I2B2Exception {
		try {
			setJobRequestType = (JobType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.setfinder.query.JobType.class);
			this.setDataSourceLookup(requestXml);



			RequestMessageType requestMsg =  getI2B2RequestMessageType( requestXml);
			this.userRequestType = requestMsg.getMessageHeader()
					.getSecurity();

		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 * 
	 * @see edu.harvard.i2b2.crc.delegate.GetQuartzJobHandler#execute()
	 */
	@Override
	public BodyType execute() {

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String responseString = null;
		BodyType bodyType = new BodyType();
		MasterResponseType masterResponseType = null;
		try {
			long initialTime = System.currentTimeMillis();
			//           masterResponseType = queryInfoLocal.getQueryMasterListFromUserId(getDataSourceLookup(),userRequestType);
			SchedulerInfoBean scheduler = new SchedulerInfoBean();
			masterResponseType = scheduler.setScheduler(SchedulerFactory.getDefaultScheduler(), getDataSourceLookup(),userRequestType, setJobRequestType);
			long finalTime = System.currentTimeMillis();
			long diffTimeMill = finalTime - initialTime;
			long diffTime = diffTimeMill / 1000;
			log.debug(" EJB Diff mill =" + diffTimeMill + " diffTime =" +
					diffTime);
			masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
		} catch (Exception e) {
			log.debug(e.getMessage());
			masterResponseType = new MasterResponseType();
			masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(of.createResponse(masterResponseType));
		}

		return bodyType;		


		// call ejb and pass input object
		/*
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		ResponseMessageType responseMessageType = new ResponseMessageType();
		StatusType statusType = null;
		String responseString = null;
		InstanceResultResponseType instanceResultResponseType = null;
		BodyType bodyType = new BodyType();
		try {
			//TODO removed ejbs
			//		QueryInfoLocalHome queryInfoLocalHome = qpUtil
			//				.getQueryInfoLocalHome();
			//		QueryInfoLocal queryInfoLocal = queryInfoLocalHome.create();
			//JobType job = setJobRequestType.getName(); //.getJob();

			SchedulerInfoBean query = new SchedulerInfoBean();

			query.setScheduler(dataSourceLookup, null, null);
			//	QuartzExec quartz = new QuartzExec();

			//	instanceResultResponseType = query.cancelQueryInstance(
			//			this.getDataSourceLookup(), instanceId);
			//	instanceResultResponseType.setStatus(this.buildCRCStausType(
			//			RequestHandlerDelegate.DONE_TYPE, "DONE"));

		} catch (Exception e) {
			instanceResultResponseType = new InstanceResultResponseType();
			instanceResultResponseType.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny()
			.add(of.createResponse(instanceResultResponseType));

		}
		return bodyType;
		*/
	}

	// function to build StatusType for the given message
	private StatusType buildStatusType(String statusTypeString, String message) {
		StatusType statusType = new StatusType();
		statusType.setType(statusTypeString);
		statusType.setValue(message);

		return statusType;
	}
}
