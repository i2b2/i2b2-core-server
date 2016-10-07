/*
 * Copyright (c) 2016-2017 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Wayne Chan
 */
package edu.harvard.i2b2.crc.axis2;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.SetDblookupType;
//import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class SetDblookupDataMessage extends RequestDataMessage { //swc20160523

	//private static Log log = LogFactory.getLog(SetDblookupDataMessage.class);
    
	public SetDblookupDataMessage(String requestElementString) throws I2B2Exception {
        super.setRequestMessageType(requestElementString);
	}
    
    /**
     * Function to get SetDblookupType object from i2b2 request message type
     * @return
     * @throws JAXBUtilException
     */
    public SetDblookupType setDblookupType() throws JAXBUtilException {
        BodyType bodyType = reqMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        SetDblookupType setDblookupType = (SetDblookupType) helper.getObjectByClass(bodyType.getAny(), SetDblookupType.class);        
        return setDblookupType;
    }

}
