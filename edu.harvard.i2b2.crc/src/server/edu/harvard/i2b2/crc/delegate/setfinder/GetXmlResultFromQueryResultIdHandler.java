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
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryResultBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;



/**
 * GetRequestXmlFromQueryMasterIdHandler class
 * implements execute method
 * $Id: GetXmlResultFromQueryResultIdHandler.java,v 1.1 2008/05/01 16:13:33 rk903 Exp $
 * @author rkuttan
 */
public class GetXmlResultFromQueryResultIdHandler extends RequestHandler {
	ResultRequestType resultRequestType = null;
    PsmQryHeaderType headerType = null;
    String requestXml = null;

    /**
    * Constuctor which accepts i2b2 request message xml
    * @param requestXml
    * @throws I2B2Exception
    */
    public GetXmlResultFromQueryResultIdHandler(String requestXml)
        throws I2B2Exception {
        this.requestXml = requestXml;

        try {
            headerType = (PsmQryHeaderType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
            resultRequestType = (ResultRequestType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.ResultRequestType.class);
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
        CrcXmlResultResponseType xmlResultResponseType = new CrcXmlResultResponseType();
        try {
        	//TODO removed ejbs
//            QueryResultLocalHome queryResultLocalHome = qpUtil.getQueryResultLocalHome();
 //           QueryResultLocal queryResultLocal = queryResultLocalHome.create();
            UserType userType = headerType.getUser();
            String userId = null;

            if (userType != null) {
                userId = userType.getLogin();
            }
          
            QueryResultBean query = new QueryResultBean();
            xmlResultResponseType = query.getXmlResultFromResultInstanceId(dataSourceLookup, resultRequestType.getQueryResultInstanceId());
            
           
            xmlResultResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));
         } catch (Exception e) {
        	xmlResultResponseType = new CrcXmlResultResponseType();
        	xmlResultResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
        } finally { 
        	edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
            bodyType.getAny().add(of.createResponse(xmlResultResponseType));
        }

        return bodyType;
    }
}
