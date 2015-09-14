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
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserRequestType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;



/**
 * GetQueryMasterListFromGroupIdHandler class
 * implements execute method
 * $Id: GetQueryMasterListFromGroupIdHandler.java,v 1.4 2008/03/19 22:36:37 rk903 Exp $
 * @author rkuttan
 */
public class GetQueryMasterListFromGroupIdHandler extends RequestHandler {
    private UserRequestType userRequestType = null;

    /**
     * Constuctor which accepts i2b2 request message xml
     * @param requestXml
     * @throws I2B2Exception
     */
    public GetQueryMasterListFromGroupIdHandler(String requestXml)
        throws I2B2Exception {
        try {
            userRequestType = (UserRequestType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.UserRequestType.class);
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
        // call ejb and pass input object
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
        String responseString = null;
        BodyType bodyType = new BodyType();
        MasterResponseType masterResponseType = null;
        try {
        	//TODO removed ejbs
    //        QueryInfoLocalHome queryInfoLocalHome = qpUtil.getQueryInfoLocalHome();
    //        QueryInfoLocal queryInfoLocal = queryInfoLocalHome.create();
            long initialTime = System.currentTimeMillis();
     //       masterResponseType = queryInfoLocal.getQueryMasterListFromGroupId(this.getDataSourceLookup(),userRequestType);
            QueryInfoBean query = new QueryInfoBean();
            masterResponseType = query.getQueryMasterListFromGroupId(getDataSourceLookup(),userRequestType);

            long finalTime = System.currentTimeMillis();
            long diffTimeMill = finalTime - initialTime;
            long diffTime = diffTimeMill / 1000;
            log.debug(" EJB Diff mill =" + diffTimeMill + " diffTime =" +
                diffTime);
            masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
            log.debug("Size of list " + masterResponseType.getQueryMaster().size());
        } catch (Exception e) {
        	masterResponseType = new MasterResponseType();
        	masterResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
        	log.equals(e);
        } finally { 
        	edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
            bodyType.getAny().add(of.createResponse(masterResponseType));
        }

        return bodyType;
    }
}
