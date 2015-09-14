/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;

public class GetTermInfoDataMessage extends RequestDataMessage {
	private static Log log = LogFactory.getLog(GetTermInfoDataMessage.class);

	public GetTermInfoDataMessage(String requestVdo) throws I2B2Exception {
		super(requestVdo);
	}

	/**
	 * Function to get vocabRequestType object from i2b2 request message type
	 * @return
	 * @throws JAXBUtilException
	 */
	public GetTermInfoType getTermInfoType() throws JAXBUtilException {
		BodyType bodyType = reqMessageType.getMessageBody();
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		GetTermInfoType termInfoType = (GetTermInfoType) helper.getObjectByClass(bodyType.getAny(),
				GetTermInfoType.class);        
		return termInfoType;
	}



}
