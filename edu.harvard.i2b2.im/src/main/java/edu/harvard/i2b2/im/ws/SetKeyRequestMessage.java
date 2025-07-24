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
 * 		Mike Mendis
 */
package edu.harvard.i2b2.im.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;


/**
 * The ExportChildDataMessage class is a helper class to build IM messages in the
 * i2b2 format
 */
public class SetKeyRequestMessage extends RequestDataMessage{
    
	public SetKeyRequestMessage() {
	}
    
    /**
     * Function to get pdo object from i2b2 request message type
     * @return
     * @throws I2B2Exception
     */
    public SetKeyType setKeyType() throws I2B2Exception {
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
