/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Wayne Chan
 */
package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.vdo.DblookupsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetAllDblookupsDataMessage extends RequestDataMessage { //swc20160515

	private static Log log = LogFactory.getLog(GetAllDblookupsDataMessage.class);
    
	public GetAllDblookupsDataMessage(String requestVdo) throws I2B2Exception {
		super(requestVdo);
	}
    
    /**
     * Function to get DblookupsType object from i2b2 request message type
     * @return
     * @throws JAXBUtilException
     */
    public DblookupsType getDblookupsType() throws JAXBUtilException {
        BodyType bodyType = reqMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        DblookupsType dblookupsType = (DblookupsType) helper.getObjectByClass(bodyType.getAny(), DblookupsType.class);        
        return dblookupsType;
    }


}
