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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceMessageRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryManagerBean;
import edu.harvard.i2b2.crc.ejb.QueryManagerBeanUtil;
import edu.harvard.i2b2.crc.ejb.role.PriviledgeLocal;
import edu.harvard.i2b2.crc.util.CacheUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * RunQueryInstanceFromQueryDefinitionHandler class implements execute method
 * $Id: RunQueryInstanceFromQueryDefinitionHandler.java,v 1.6 2008/03/19
 * 22:36:37 rk903 Exp $
 * 
 * @author rkuttan
 */
public class RunExportFromQueryInstanceHandler extends RequestHandler {
	private InstanceMessageRequestType instanceMessageRequestType = null;
	private PsmQryHeaderType headerType = null;


	String requestXml = null;
	//boolean lockedoutFlag = false
	boolean errorFlag = false;
	protected final Log logesapi = LogFactory.getLog(getClass());

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public RunExportFromQueryInstanceHandler(String requestXml)
			throws I2B2Exception {
		try {
			instanceMessageRequestType = (InstanceMessageRequestType) this.getRequestType(requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceMessageRequestType.class);
			headerType = (PsmQryHeaderType) this.getRequestType(requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
			this.requestXml = requestXml;
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 * 
	 * @throws I2B2Exception
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
	 */
	@Override	public BodyType execute() throws I2B2Exception {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String response = null;
		BodyType bodyType = new BodyType();
		InstanceResponseType masterResponseType = null;
		try {
			//get userId and timeout from request xml
			RequestMessageType requestMessageType = getI2B2RequestMessageType(requestXml);
			long timeout = requestMessageType.getRequestHeader()
					.getResultWaittimeMs();
			String userId = headerType.getUser().getLogin();
			DataSourceLookup dataSource = this.getDataSourceLookup();
			String instanceId = instanceMessageRequestType.getQueryInstanceId();

			QueryManagerBean query = new QueryManagerBean();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						query.runExportQueryInstance(dataSource,userId,
								instanceId, requestXml);
					} catch (Exception e) {
						log.error("i2b2 exception", e);
					} catch (Throwable e) {
						log.error("Throwable", e);
					}
				}
			});
			t.setDaemon(true);
			t.start();
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



	public boolean getErrorFlag() { 
		return errorFlag;
	}
}
