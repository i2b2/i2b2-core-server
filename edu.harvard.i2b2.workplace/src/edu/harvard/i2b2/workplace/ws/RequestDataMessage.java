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
package edu.harvard.i2b2.workplace.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.workplace.util.WorkplaceJAXBUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.bind.JAXBElement;


/**
 * The RequestDataMessage class is a helper class to build Workplace messages in the
 * i2b2 format
 */
public abstract class RequestDataMessage{
	protected final Log log = LogFactory.getLog(getClass());

	public RequestMessageType reqMessageType = null;
	
	public void setRequestMessageType(String requestWdo) throws I2B2Exception {
		try {
			JAXBElement jaxbElement = WorkplaceJAXBUtil.getJAXBUtil().unMashallFromString(requestWdo);
			this.reqMessageType = (RequestMessageType) jaxbElement.getValue();
			
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Umarshaller error: " + e.getMessage() +
					requestWdo, e);
		}
	}



	public RequestMessageType getRequestMessageType() { 
		return reqMessageType;
	}

	public MessageHeaderType getMessageHeaderType() {
		return reqMessageType.getMessageHeader();
	}

}