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
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetAllDblookupsDataMessage extends RequestDataMessage { //swc20160523

	private static Log log = LogFactory.getLog(GetAllDblookupsDataMessage.class);
    
	public GetAllDblookupsDataMessage(String requestElementString) throws I2B2Exception {
        super.setRequestMessageType(requestElementString);
	}
    
    /**
     * Function to get DblookupsType object from i2b2 request message type
     * @return
     * @throws JAXBUtilException
     */
    public DblookupsType getDblookupsType() throws JAXBUtilException {
    	//log.info(" getDblookupsType()");
        BodyType bodyType = reqMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        DblookupsType dblookupsType = (DblookupsType) helper.getObjectByClass(bodyType.getAny(), DblookupsType.class);        
        return dblookupsType;
    }

}
