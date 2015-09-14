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
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.ejb.QueryManagerBean;
import edu.harvard.i2b2.crc.ejb.QueryRunBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * RunQueryInstanceFromQueryMasterHandler class
 * implements execute method
 * $Id: RunQueryInstanceFromQueryMasterHandler.java,v 1.7 2008/03/19 22:36:37 rk903 Exp $
 * @author rkuttan
 */
public class RunQueryInstanceFromQueryMasterHandler extends RequestHandler {
    MasterRequestType masterRequestType = null;
    String requestXml = null;
    PsmQryHeaderType headerType = null;

    /**
    * Constuctor which accepts i2b2 request message xml
    * @param requestXml
    * @throws I2B2Exception
    */
    public RunQueryInstanceFromQueryMasterHandler(String requestXml)
        throws I2B2Exception {
        try {
            masterRequestType = (MasterRequestType) getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType.class);
            headerType = (PsmQryHeaderType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
            this.requestXml = requestXml;
            this.setDataSourceLookup(requestXml);
        } catch (JAXBUtilException jaxbUtilEx) {
            throw new I2B2Exception("Error ", jaxbUtilEx);
        }
    }

    /**
    * Perform operation for the given request
    * using business class(ejb) and return response
    * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
    */
    public BodyType execute() throws I2B2Exception {
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
        String response = null;
        BodyType bodyType = new BodyType();
        InstanceResultResponseType instanceResult = null;
        try {
            //get userId and timeout from request xml
            RequestMessageType requestMessageType = getI2B2RequestMessageType(requestXml);
            long timeout = requestMessageType.getRequestHeader()
                                             .getResultWaittimeMs();
            String userId = headerType.getUser().getLogin();
            String masterId = masterRequestType.getQueryMasterId();

        	//TODO removed ejbs
//            QueryManagerLocalHome queryManagerLocalHome = qpUtil.getQueryManagerLocalHome();
 //           QueryManagerLocal queryManagerLocal = queryManagerLocalHome.create();
            QueryManagerBean query = new QueryManagerBean();
            instanceResult = query.runQueryMaster(this.getDataSourceLookup(),userId,
                    masterId, timeout);
            instanceResult.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
        } catch (Exception e) { 
        	instanceResult = new InstanceResultResponseType();
        	instanceResult.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
        } finally { 
        	edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
            bodyType.getAny().add(of.createResponse(instanceResult));
            
        }

        return bodyType;
    }
}
