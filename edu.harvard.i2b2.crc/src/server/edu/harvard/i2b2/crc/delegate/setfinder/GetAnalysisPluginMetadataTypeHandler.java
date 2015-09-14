/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * 
 */
package edu.harvard.i2b2.crc.delegate.setfinder;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.analysis.AnalysisPluginInfoLocal;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * GetAnalysisPluginMetadataTypeHandler class. Returns plugin's metadata by
 * plugin name and project
 */
public class GetAnalysisPluginMetadataTypeHandler extends RequestHandler {
	AnalysisPluginMetadataRequestType apMetadataRequestType = null;
	String projectId = "";
	PsmQryHeaderType headerType = null;
	String requestXml = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public GetAnalysisPluginMetadataTypeHandler(String requestXml)
			throws I2B2Exception {
		this.requestXml = requestXml;

		try {
			headerType = (PsmQryHeaderType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
			apMetadataRequestType = (AnalysisPluginMetadataRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisPluginMetadataRequestType.class);
			this.setDataSourceLookup(requestXml);
			projectId = this.getI2B2RequestMessageType(requestXml)
					.getMessageHeader().getProjectId();
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
		// call ejb and pass input object
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String responseString = null;
		BodyType bodyType = new BodyType();

		AnalysisPluginMetadataResponseType resultTypeResponseType = new AnalysisPluginMetadataResponseType();
		try {
			AnalysisPluginInfoLocal apInfoLocal = qpUtil
					.getAnalysisPluginInfoLocal();

			resultTypeResponseType = apInfoLocal.getAnalysisPluginMetadata(
					dataSourceLookup, apMetadataRequestType, projectId);

			resultTypeResponseType.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.DONE_TYPE, "DONE"));

		} catch (I2B2Exception e) {
			resultTypeResponseType = new AnalysisPluginMetadataResponseType();
			resultTypeResponseType.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(of.createResponse(resultTypeResponseType));
		}

		return bodyType;
	}
}
