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
import edu.harvard.i2b2.crc.datavo.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultTypeRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultTypeResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;



/**
 * GetRequestXmlFromQueryMasterIdHandler class
 * implements execute method
 * $Id: GetAllQueryResultTypeHandler.java,v 1.1 2008/05/08 14:37:25 rk903 Exp $
 * @author rkuttan
 */
public class GetAllQueryResultTypeHandler extends RequestHandler {
	ResultTypeRequestType resultTypeRequestType = null;
    PsmQryHeaderType headerType = null;
    String requestXml = null;

    /**
    * Constuctor which accepts i2b2 request message xml
    * @param requestXml
    * @throws I2B2Exception
    */
    public GetAllQueryResultTypeHandler(String requestXml)
        throws I2B2Exception {
        this.requestXml = requestXml;

        try {
            headerType = (PsmQryHeaderType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
            resultTypeRequestType = (ResultTypeRequestType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.ResultTypeRequestType.class);
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
        //	 	call ejb and pass input object
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
        String responseString = null;
        BodyType bodyType = new BodyType();
      
        ResultTypeResponseType resultTypeResponseType= new  ResultTypeResponseType();
        try {
        	//TODO removed ejbs
   //         QueryInfoLocalHome queryInfoLocalHome = qpUtil.getQueryInfoLocalHome();
   //         QueryInfoLocal queryInfoLocal = queryInfoLocalHome.create();
   
        	 QueryInfoBean query = new QueryInfoBean();
           resultTypeResponseType = query.getAllResultType(dataSourceLookup);
            
           
            resultTypeResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
            
        } catch (Exception e) {
        	 resultTypeResponseType = new ResultTypeResponseType();
        	 resultTypeResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
        } finally { 
        	edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
            bodyType.getAny().add(of.createResponse(resultTypeResponseType));
        }

        return bodyType;
    }
}
