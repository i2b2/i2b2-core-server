/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.getnameinfo;


import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * GetObservationFactFromPrimaryKeyHandler class.
 * $Id: GetObservationFactFromPrimaryKeyHandler.java,v 1.8 2008/07/21 19:56:56 rk903 Exp $
 * @author rkuttan
 */
public class GeNameInfoHandler extends RequestHandler {
	private FindByChildType getFindByChildType =
			null;
    private SecurityType userRequestType = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public GeNameInfoHandler(String requestXml)
			throws I2B2Exception {
		try {
			this.getFindByChildType = (FindByChildType) this.getRequestType(requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType.class);
			RequestMessageType requestMsg =  getI2B2RequestMessageType( requestXml);
			this.userRequestType = requestMsg.getMessageHeader()
					.getSecurity();
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request and
	 * return response
	 */
	public BodyType execute() throws I2B2Exception {
		// call ejb and pass input object
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String responseString = null;
		BodyType bodyType = new BodyType();
		MasterResponseType masterResponseType = null;
		try {
			long initialTime = System.currentTimeMillis();
			//           masterResponseType = queryInfoLocal.getQueryMasterListFromUserId(getDataSourceLookup(),userRequestType);
			QueryInfoBean query = new QueryInfoBean();
			masterResponseType = query.getQueryMasterListFromNameInfo(getDataSourceLookup(),userRequestType, getFindByChildType);
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
	}
}    	


