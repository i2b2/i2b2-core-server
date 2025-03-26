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
package edu.harvard.i2b2.crc.delegate.setfinder;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceStatusRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRenameRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


public class SetQueryInstanceStatusHandler extends RequestHandler {
	private InstanceStatusRequestType instanceMessageRequestType = null;

	private boolean isManager = false;
	private String userId = null;


	/**
	 * Constuctor which accepts i2b2 request message xml
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public SetQueryInstanceStatusHandler(String requestXml, boolean isMgr, String username)
			throws I2B2Exception {
		isManager = isMgr;
		userId = username;
		try {
			instanceMessageRequestType = (InstanceStatusRequestType) this.getRequestType(requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceStatusRequestType.class);
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request
	 * using business class(ejb) and return response
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
	 */
	@Override
	public BodyType execute() {
		// call ejb and pass input object
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		ResponseMessageType responseMessageType = new ResponseMessageType();
		StatusType statusType = null;
		String responseString = null;
		BodyType bodyType = new BodyType();
		InstanceResponseType masterResponseType = null;
		try {
			//TODO removed ejbs
			//            QueryInfoLocalHome queryInfoLocalHome = qpUtil.getQueryInfoLocalHome();
			//            QueryInfoLocal queryInfoLocal = queryInfoLocalHome.create();
			String userId = instanceMessageRequestType.getUserId();
			String instanceId = instanceMessageRequestType.getQueryInstanceId();
			String statusName = instanceMessageRequestType.getStatusName();
			QueryInfoBean query = new QueryInfoBean();
			query.setQueryInstanceStatus(this.getDataSourceLookup(),
					instanceId, statusName, isManager, userId);
			masterResponseType = new InstanceResponseType();
			masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));

		} catch (Exception e) {
			masterResponseType = new InstanceResponseType();
			masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} finally { 
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(of.createResponse(masterResponseType));

		}
		return bodyType;
	}

	//function to build StatusType for the given message 
	private StatusType buildStatusType(String statusTypeString, String message) {
		StatusType statusType = new StatusType();
		statusType.setType(statusTypeString);
		statusType.setValue(message);

		return statusType;
	}
}
