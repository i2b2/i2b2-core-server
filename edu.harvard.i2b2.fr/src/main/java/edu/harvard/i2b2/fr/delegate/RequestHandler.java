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
package edu.harvard.i2b2.fr.delegate;

import java.io.StringWriter;

import jakarta.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.fr.datavo.FRJAXBUtil;
import edu.harvard.i2b2.fr.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.fr.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.fr.datavo.i2b2message.InfoType;
import edu.harvard.i2b2.fr.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.fr.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.fr.datavo.i2b2message.PollingUrlType;
import edu.harvard.i2b2.fr.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.fr.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.fr.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.fr.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.fr.datavo.i2b2message.StatusType;


/**
 * Top level class to process the request. There will be
 * seperate requesthandler class for each request type.
 * The main processing of for the request will be done inside
 * execute function
 * $Id: RequestHandler.java,v 1.1 2008/05/29 18:01:11 mem61 Exp $
 * @author rkuttan
 */
public abstract class RequestHandler {
    /** log **/
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Function to perform operation on the given
     * request
     * @return response xml message
     * @throws Exception 
     */
    public abstract BodyType execute() throws I2B2Exception, Exception;

    /**
     * Class to fetch specific request message
     * from i2b2 message xml
     * @param requestXml
     * @param classname
     * @return  object which is of type classname
     * @throws JAXBUtilException
     */
    protected Object getRequestType(String requestXml, Class classname)
        throws JAXBUtilException {
		log.debug("RequestHandler - RequestXML: " + requestXml);

        Object returnObject = null;
        log.debug("MM1");

        JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
        BodyType bodyType = requestMessageType.getMessageBody();
        JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
        log.debug("MM6");
        //get request header type
        returnObject = unWrapHelper.getObjectByClass(bodyType.getAny(),
                classname);
        log.debug("MM7");
        return returnObject;
    }
    
    protected MessageHeaderType getMessageHeaderType(String requestXml) 
    throws JAXBUtilException { 
    	JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
        JAXBElement<?> jaxbElement = jaxbUtil.unMashallFromString(requestXml);
        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
       
        return requestMessageType.getMessageHeader();
    }

    /**
     * Function to build response message type
     * using given bodytype and request xml
     * @param requestXml
     * @param bodyType
     * @return i2b2 response message xml
     * @throws JAXBUtilException
     */
    protected String buildResponseMessage(String requestXml, BodyType bodyType)
        throws JAXBUtilException {
        JAXBUtil util = FRJAXBUtil.getJAXBUtil();
        RequestMessageType requestMsgType = getI2B2RequestMessageType(requestXml);
        MessageHeaderType messageHeader = requestMsgType.getMessageHeader();

        //reverse sending and receiving app
        ApplicationType sendingApp = messageHeader.getSendingApplication();
        ApplicationType receiveApp = messageHeader.getReceivingApplication();
        messageHeader.setSendingApplication(receiveApp);
        messageHeader.setReceivingApplication(sendingApp);

        //set instance num
        MessageControlIdType messageControlIdType = messageHeader.getMessageControlId();

        if (messageControlIdType != null) {
            messageControlIdType.setInstanceNum(1);
        }

        StatusType statusType = new StatusType();
        statusType.setType("DONE");

        //:TODO statusType.setValue(sessionId);
        PollingUrlType pollingType = new PollingUrlType();
        pollingType.setIntervalMs(100);

        //:TODO value come from property file
     
        ResultStatusType resultStatusType = new ResultStatusType();
        resultStatusType.setStatus(statusType);
        resultStatusType.setPollingUrl(pollingType);

        InfoType infoType = new InfoType();
        //:TODO value come from property file
     
        infoType.setValue("Log information");

        ResponseHeaderType responseHeader = new ResponseHeaderType();
        responseHeader.setResultStatus(resultStatusType);
        responseHeader.setInfo(infoType);

        ResponseMessageType responseMessageType = new ResponseMessageType();
        responseMessageType.setMessageHeader(messageHeader);
        responseMessageType.setResponseHeader(responseHeader);
        responseMessageType.setMessageBody(bodyType);

        edu.harvard.i2b2.fr.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.fr.datavo.i2b2message.ObjectFactory();
        StringWriter strWriter = new StringWriter();
        util.marshaller(of.createResponse(responseMessageType), strWriter);

        return strWriter.toString();
    }

    /**
     * Function to unmarshall i2b2 request message type
     * @param requestXml
     * @return RequestMessageType
     * @throws JAXBUtilException
     */
    protected RequestMessageType getI2B2RequestMessageType(String requestXml)
        throws JAXBUtilException {
        JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
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
            edu.harvard.i2b2.fr.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.fr.datavo.i2b2message.ObjectFactory();
            JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
            jaxbUtil.marshaller(of.createResponse(responseMessageType),
                strWriter);
        } catch (JAXBUtilException e) {
            log.error("Error while generating response message" +
                e.getMessage());
        }

        return strWriter.toString();
    }
    
    protected edu.harvard.i2b2.fr.datavo.fr.query.StatusType buildCRCStausType(String statusType,String message) {
    	edu.harvard.i2b2.fr.datavo.fr.query.StatusType st = new edu.harvard.i2b2.fr.datavo.fr.query.StatusType();
    	edu.harvard.i2b2.fr.datavo.fr.query.StatusType.Condition condition = new edu.harvard.i2b2.fr.datavo.fr.query.StatusType.Condition();
    	condition.setType(statusType);
    	condition.setValue(message);
    	st.getCondition().add(condition);
    	return st;
    }
    
   
}
