/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Mike Mendis
 */
package edu.harvard.i2b2.im.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.wdo.IsKeySetType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;


/**
 * The ExportChildDataMessage class is a helper class to build IM messages in the
 * i2b2 format
 */
public class IsKeySetRequestMessage extends RequestDataMessage{
    
	public IsKeySetRequestMessage() {
	}
    
    /**
     * Function to get pdo object from i2b2 request message type
     * @return
     * @throws I2B2Exception
     */
    public SetKeyType isKeySet() throws I2B2Exception {
    	SetKeyType setKeyType;
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			setKeyType = (SetKeyType) helper.getObjectByClass(bodyType.getAny(),
					SetKeyType.class);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}        
        return setKeyType;
    }
    

}
