/*
 * Copyright (c) 2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Creator:
 * 		Neha Patel
 */
package edu.harvard.i2b2.workplace.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.workplace.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.workplace.datavo.wdo.ProtectedType;


public class ProtectedDataMessage extends RequestDataMessage{
	
	public ProtectedDataMessage() throws I2B2Exception {
	}

	/**
	 * Function to get set_protectedType object from i2b2 request message type
	 * @return
	 * @throws JAXBUtilException
	 */

	public ProtectedType getProtectedRequestType() throws JAXBUtilException {
		BodyType bodyType = reqMessageType.getMessageBody();
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ProtectedType protectedReqType = (ProtectedType) helper.getObjectByClass(bodyType.getAny(),
				ProtectedType.class);        
		return protectedReqType;
	}

}