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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.delegate;

import java.io.StringWriter;

import jakarta.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;


/**
 * Class to delegate i2b2 requests to appropriate
 * {@link RequestHandler}
 * Class unwraps i2b2 request message and based on the
 * request type, the request will be delegated to appropriate
 * request handler by calling execute function. The return value
 * from execute function is just passed back to the client
 * $Id: RequestHandlerDelegate.java,v 1.2 2008/02/04 22:14:28 rk903 Exp $
 * @author rkuttan
 */
public abstract class RequestHandlerDelegate {
	 /** log **/
    protected final Log log = LogFactory.getLog(getClass());
    
    public static final String ERROR_TYPE = "ERROR";
    public static final String DONE_TYPE = "DONE";

    /**
     * Function to delegate request to appropriate
     * request handler class and passes back the
     * response message output back to client
     * @param requestXml
     * @return response message xml
     * @throws I2B2Exception 
     */
    public abstract String handleRequest(String requestXml,RequestHandler requestHandler) throws I2B2Exception;
    
    
    
    /**
     * Function to unmarshall i2b2 request message type
     * @param requestXml
     * @return RequestMessageType
     * @throws JAXBUtilException
     */
    protected RequestMessageType getI2B2RequestMessageType(String requestXml)
        throws JAXBUtilException {
        JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();

        return requestMessageType;
    }

    /**
     * Function marshall i2b2 response message type
     * @param responseMessageType
     * @return
     */
    protected String getResponseString(ResponseMessageType responseMessageType) {
        StringWriter strWriter = new StringWriter();

        try {
            edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
            JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
            jaxbUtil.marshaller(of.createResponse(responseMessageType),
                strWriter);
        } catch (JAXBUtilException e) {
            log.error("Error while generating response message" +
                e.getMessage());
        }

        return strWriter.toString();
    }
}
