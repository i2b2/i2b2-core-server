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
 *     Mike Mendis - initial API and implementation
 */

package edu.harvard.i2b2.pm.ws;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.pm.datavo.i2b2versionmessage.RequestMessageType;
import edu.harvard.i2b2.pm.util.JAXBConstant;


/**
 * The PatientDataMessage class is a helper class to build PFT messages in the
 * i2b2 format
 */
public class VersionMessage {
    private static Log log = LogFactory.getLog(VersionMessage.class);
    private JAXBUtil jaxbUtil = null;
    RequestMessageType reqMessageType = null;

    /**
     * The constructor
     */
    public VersionMessage(String requestPdo) throws I2B2Exception {
        jaxbUtil = new JAXBUtil(JAXBConstant.VERSION_PACKAGE_NAME);

        try {
            JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestPdo);

            if (jaxbElement == null) {
                throw new I2B2Exception(
                    "Null value from unmashall for PDO xml : " + requestPdo);
            }

            this.reqMessageType = (RequestMessageType) jaxbElement.getValue();
            
        } catch (JAXBUtilException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw new I2B2Exception("Umashaller error: " + e.getMessage() +
                requestPdo, e);
        }
    }

    /**
     * Function to get RequestData object from i2b2 request message type
     * @return
     * @throws JAXBUtilException
    public GetUserConfigurationType getRequestType() throws JAXBUtilException {
        BodyType bodyType = reqMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        GetUserConfigurationType requestType = (GetUserConfigurationType) helper.getObjectByClass(bodyType.getAny(),
        		GetUserConfigurationType.class);

        return requestType;
    }
	     */

    
    public RequestMessageType getRequestMessageType() { 
    	return reqMessageType;
    }
}
