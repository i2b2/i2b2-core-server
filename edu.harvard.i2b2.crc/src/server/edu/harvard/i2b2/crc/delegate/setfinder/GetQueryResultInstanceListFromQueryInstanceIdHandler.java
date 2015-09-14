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
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.ejb.QueryResultBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * GetQueryResultInstanceListFromQueryInstanceIdHandler class
 * implements execute method
 * $Id: GetQueryResultInstanceListFromQueryInstanceIdHandler.java,v 1.7 2008/03/19 22:36:37 rk903 Exp $
 * @author rkuttan
 */
public class GetQueryResultInstanceListFromQueryInstanceIdHandler
    extends RequestHandler {
    String requestXml = null;
    InstanceRequestType queryInstanceReqType = null;
    PsmQryHeaderType headerType = null;

    /**
    * Constuctor which accepts i2b2 request message xml
    * @param requestXml
    * @throws I2B2Exception
    */
    public GetQueryResultInstanceListFromQueryInstanceIdHandler(
        String requestXml) throws I2B2Exception {
        this.requestXml = requestXml;

        try {
            headerType = (PsmQryHeaderType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
            queryInstanceReqType = (InstanceRequestType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType.class);
            this.setDataSourceLookup(requestXml);
        } catch (JAXBUtilException jaxbUtilEx) {
            throw new I2B2Exception("Error ", jaxbUtilEx);
        }
    }

    /**
    * Perform operation for the given request
    * using business class(ejb) and return response
     * @throws I2B2Exception
    * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
    */
    public BodyType execute() throws I2B2Exception {
        // call ejb and pass input object
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
        String responseString = null;
        
        BodyType bodyType = new BodyType();
        ResultResponseType resultResponseType = null;
        try {
        	//TODO removed ejbs
   //         QueryResultLocalHome queryResultLocalHome = qpUtil.getQueryResultLocalHome();
   //         QueryResultLocal queryResultLocal = queryResultLocalHome.create();
            
            UserType userType = headerType.getUser();
            String userId = null;

            if (userType != null) {
                userId = userType.getLogin();
            }

            if (queryInstanceReqType == null) {
                //build response message with error information
                resultResponseType = new ResultResponseType();
            }

            QueryResultBean query = new QueryResultBean();
            resultResponseType = query.getResultInstanceFromQueryInstanceId(this.getDataSourceLookup(),userId,
                    queryInstanceReqType.getQueryInstanceId());

            resultResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
        } catch (Exception e) {
        	 resultResponseType = new ResultResponseType();
        	 resultResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
        } finally { 
        	edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
            bodyType.getAny().add(of.createResponse(resultResponseType));
        }
        

        return bodyType;
    }
}
