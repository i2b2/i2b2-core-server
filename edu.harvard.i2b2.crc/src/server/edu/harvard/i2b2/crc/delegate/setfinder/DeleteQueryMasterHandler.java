/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterDeleteRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;



/**
 * DeleteQueryMasterHandler class
 * implements execute method
 * $Id: DeleteQueryMasterHandler.java,v 1.7 2008/03/19 22:36:37 rk903 Exp $
 * @author rkuttan
 */
public class DeleteQueryMasterHandler extends RequestHandler {
    private MasterDeleteRequestType masterDeleteRequestType = null;

    /**
     * Constuctor which accepts i2b2 request message xml
     * @param requestXml
     * @throws I2B2Exception
     */
    public DeleteQueryMasterHandler(String requestXml)
        throws I2B2Exception {
        try {
            masterDeleteRequestType = (MasterDeleteRequestType) getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.MasterDeleteRequestType.class);
            this.setDataSourceLookup(requestXml);
        } catch (JAXBUtilException jaxbUtilEx) {
            throw new I2B2Exception("Error ", jaxbUtilEx);
        }
    }

    /**
     * Perform operation for the given request
     * using business class(ejb) and return response
     */
    public BodyType execute() throws I2B2Exception {
        // call ejb and pass input object
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
        ResponseMessageType responseMessageType = new ResponseMessageType();
        StatusType statusType = null;
        String responseStr = null;
        BodyType bodyType = new BodyType();
        MasterResponseType masterResponseType = new MasterResponseType();
        try {
        	//TODO removed EJBS
//            QueryInfoLocalHome queryInfoLocalHome = qpUtil.getQueryInfoLocalHome();
 //           QueryInfoLocal queryInfoLocal = queryInfoLocalHome.create();
            String userId = masterDeleteRequestType.getUserId();
            String masterId = masterDeleteRequestType.getQueryMasterId();
            if (masterId==null)
            	throw new I2B2Exception("Null master id sent to DeleteQueryMasterHandler");
            QueryInfoBean query = new QueryInfoBean();
            masterResponseType = query.deleteQueryMaster(getDataSourceLookup(),userId,masterId);

            
            masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
         } catch (Exception ex) {
            masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, ex.getMessage()));
        	ex.printStackTrace();
        }
        finally { 
        	edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
            bodyType.getAny().add(of.createResponse(masterResponseType));
        }

//        ResultStatusType resultStatusType = new ResultStatusType();
//        resultStatusType.setStatus(statusType);
//
//        ResponseHeaderType responseHeader = new ResponseHeaderType();
//        responseHeader.setResultStatus(resultStatusType);
//        responseMessageType.setResponseHeader(responseHeader);
//
//        responseStr = getResponseString(responseMessageType);

        return bodyType;
    }
}
