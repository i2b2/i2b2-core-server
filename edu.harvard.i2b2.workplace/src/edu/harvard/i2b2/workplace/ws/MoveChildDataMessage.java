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
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.workplace.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.workplace.datavo.wdo.ChildType;


/**
 * The CleanupChildrenDataMessage class is a helper class to build Workplace messages in the
 * i2b2 format
 */
public class MoveChildDataMessage extends RequestDataMessage{
  
	public MoveChildDataMessage() {
	}
    
    /**
     * Function to get ChildType object from i2b2 request message type
     * @return
     * @throws I2B2Exception
     */
    public ChildType childType() throws I2B2Exception {
        ChildType childType;
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			childType = (ChildType) helper.getObjectByClass(bodyType.getAny(),
					ChildType.class);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}        
        return childType;
    }
    

}
