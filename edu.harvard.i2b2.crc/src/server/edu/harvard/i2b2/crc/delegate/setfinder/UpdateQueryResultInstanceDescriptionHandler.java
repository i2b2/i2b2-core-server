/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * RenameQueryMasterHandler class implements execute method $Id:
 * RenameQueryMasterHandler.java,v 1.8 2008/03/19 22:36:37 rk903 Exp $
 * 
 * @author rkuttan
 */
public class UpdateQueryResultInstanceDescriptionHandler extends RequestHandler {
	private ResultRequestType resultRequestType = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public UpdateQueryResultInstanceDescriptionHandler(String requestXml)
			throws I2B2Exception {
		try {
			resultRequestType = (ResultRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.setfinder.query.ResultRequestType.class);
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 * 
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
	 */
	public BodyType execute() {
		// call ejb and pass input object
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		ResponseMessageType responseMessageType = new ResponseMessageType();
		StatusType statusType = null;
		String responseString = null;
		ResultResponseType resultResponseType = null;
		BodyType bodyType = new BodyType();
		try {
        	//TODO removed ejbs
//			QueryInfoLocalHome queryInfoLocalHome = qpUtil
//					.getQueryInfoLocalHome();
//			QueryInfoLocal queryInfoLocal = queryInfoLocalHome.create();
			String resultInstanceId = resultRequestType
					.getQueryResultInstanceId();
			String description = resultRequestType.getDescription();
			QueryInfoBean query = new QueryInfoBean();

			resultResponseType = query
					.updateResultInstanceDescription(
							this.getDataSourceLookup(), resultInstanceId,
							description);
			resultResponseType.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.DONE_TYPE, "DONE"));

		} catch (Exception e) {
			resultResponseType = new ResultResponseType();
			resultResponseType.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(of.createResponse(resultResponseType));

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
