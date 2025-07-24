/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.delegate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.GetMissingTermRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermReportResponseType;
import edu.harvard.i2b2.crc.loader.ejb.MissingTermReportBean;

public class GetMissingTermRequestHandler extends RequestHandler {

	GetMissingTermRequestType getMissingTermRequest = null;
	MessageHeaderType messageHeaderType = null;

	public GetMissingTermRequestHandler(String requestXml) throws I2B2Exception {
		try {
			getMissingTermRequest = (GetMissingTermRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.loader.datavo.loader.query.GetMissingTermRequestType.class);
			messageHeaderType = getMessageHeaderType(requestXml);
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	@Override
	public BodyType execute() throws I2B2Exception {
		edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory objectFactory = new edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory();
		// call ejb and pass input object
		

		BodyType bodyType = new BodyType();

		//MissingTermReportBeanLocal missingTermReportBean = CRCLoaderUtil
		//		.getInstance().getMissingTermReportBean();
		
		MissingTermReportBean missingTermReportBean = new MissingTermReportBean();

		MissingTermReportResponseType response = missingTermReportBean
				.getMissingTermReport(dataSourceLookup, getMissingTermRequest);

		bodyType.getAny().add(
				objectFactory.createMissingTermReportResponse(response));

		return bodyType;

	}

}
