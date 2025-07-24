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


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobType;
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
public class GetQuartzJobHandler extends RequestHandler {
	private JobType setJobRequestType = null;
 
	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public GetQuartzJobHandler(String requestXml) throws I2B2Exception {
		try {
			setJobRequestType = (JobType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.setfinder.query.JobType.class);
			this.setDataSourceLookup(requestXml);
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
		// call ejb and pass input object
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

			QueryInfoBean query = new QueryInfoBean();



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
	}

	// function to build StatusType for the given message
	private StatusType buildStatusType(String statusTypeString, String message) {
		StatusType statusType = new StatusType();
		statusType.setType(statusTypeString);
		statusType.setValue(message);

		return statusType;
	}
}
