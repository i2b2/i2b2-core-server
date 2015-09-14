/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.delegate;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.InfoType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PollingUrlType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;


/**
 * Top level class to process the request. There will be
 * seperate requesthandler class for each request type.
 * The main processing of for the request will be done inside
 * execute function
 * $Id: RequestHandler.java,v 1.4 2008/06/03 20:48:19 rk903 Exp $
 * @author rkuttan
 */
public abstract class RequestHandler {
    /** log **/
    protected final Log log = LogFactory.getLog(getClass());
    protected DataSourceLookup dataSourceLookup = null;

    /**
     * Function to perform operation on the given
     * request
     * @return response xml message
     */
    public abstract BodyType execute() throws I2B2Exception;

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
        Object returnObject = null;

        JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
        BodyType bodyType = requestMessageType.getMessageBody();
        JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
        //get request header type
        returnObject = unWrapHelper.getObjectByClass(bodyType.getAny(),
                classname);
        return returnObject;
    }
    
    protected MessageHeaderType getMessageHeaderType(String requestXml) 
    throws JAXBUtilException { 
    	JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
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
        JAXBUtil util = CRCLoaderJAXBUtil.getJAXBUtil();
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

        edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
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
    
    protected edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType buildCRCStausType(String statusType,String message) {
    	edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType st = new edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType();
    	edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType.Condition condition = new edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType.Condition();
    	condition.setType(statusType);
    	condition.setValue(message);
    	st.getCondition().add(condition);
    	return st;
    }
    
    protected void setDataSourceLookup(String requestXml) throws JAXBUtilException { 
    	RequestMessageType reqMessage = getI2B2RequestMessageType(requestXml);
    	String projectId = reqMessage.getMessageHeader().getProjectId();
    	String domainId = reqMessage.getMessageHeader().getSecurity().getDomain();
    	String userId = reqMessage.getMessageHeader().getSecurity().getUsername();
    	dataSourceLookup = new DataSourceLookup();
    	dataSourceLookup.setProjectPath(projectId);
    	dataSourceLookup.setDomainId(domainId);
    	dataSourceLookup.setOwnerId(userId);
    	
    }
    
    protected DataSourceLookup getDataSourceLookup() { 
    	return dataSourceLookup;
    }
   
}
