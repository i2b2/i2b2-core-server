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
 * 		Wayne Chan
 */
package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.vdo.DeleteDblookupType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteDblookupDataMessage extends RequestDataMessage { //swc20160515

	private static Log log = LogFactory.getLog(DeleteDblookupDataMessage.class);
    
	public DeleteDblookupDataMessage(String requestVdo) throws I2B2Exception {
		super(requestVdo);
	}
    
    /**
     * Function to get DeleteDblookupType object from i2b2 request message type
     * @return
     * @throws JAXBUtilException
     */
    public DeleteDblookupType DeleteDblookupType() throws JAXBUtilException {
        BodyType bodyType = reqMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        DeleteDblookupType delDblookupType = (DeleteDblookupType) helper.getObjectByClass(bodyType.getAny(), DeleteDblookupType.class);        
        return delDblookupType;
    }


}
