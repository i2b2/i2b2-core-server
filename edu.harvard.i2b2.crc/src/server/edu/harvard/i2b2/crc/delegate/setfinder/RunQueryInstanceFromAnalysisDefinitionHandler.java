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
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.analysis.StartAnalysis;
import edu.harvard.i2b2.crc.ejb.analysis.StartAnalysisLocal;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * RunQueryInstanceFromQueryDefinitionHandler class implements execute method
 * $Id: RunQueryInstanceFromQueryDefinitionHandler.java,v 1.6 2008/03/19
 * 22:36:37 rk903 Exp $
 * 
 * @author rkuttan
 */
public class RunQueryInstanceFromAnalysisDefinitionHandler extends
		RequestHandler {
	AnalysisDefinitionRequestType analysisDefRequestType = null;
	String requestXml = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public RunQueryInstanceFromAnalysisDefinitionHandler(String requestXml)
			throws I2B2Exception {
		try {
			analysisDefRequestType = (AnalysisDefinitionRequestType) getRequestType(
					requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionRequestType.class);
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
	public BodyType execute() throws I2B2Exception {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String response = null;
		BodyType bodyType = new BodyType();
		MasterInstanceResultResponseType masterInstanceResponse = null;
		try {
			//StartAnalysisLocal startAnalysisLocal = qpUtil
		//			.getStartAnalysisLocal();
			
			StartAnalysisLocal startAnalysisLocal = new StartAnalysis();

			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(this
					.getDataSourceLookup().getDomainId(), getDataSourceLookup()
					.getProjectPath(), getDataSourceLookup().getOwnerId());
			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();

			// response = queryManagerLocal.processQuery(requestXml);
			masterInstanceResponse = startAnalysisLocal.start(daoFactory,
					requestXml);
			// processQuery(this.getDataSourceLookup(), requestXml);
			// masterInstanceResponse.setStatus(this.buildCRCStausType(
			// RequestHandlerDelegate.DONE_TYPE, "DONE"));

			// response = this.buildResponseMessage(requestXml, bodyType);
		} catch (I2B2Exception e) {
			masterInstanceResponse = new MasterInstanceResultResponseType();
			masterInstanceResponse.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));

		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory psmObjFactory = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(
					psmObjFactory.createResponse(masterInstanceResponse));
		}

		return bodyType;
	}
}
