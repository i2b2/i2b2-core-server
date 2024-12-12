/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.im.delegate.crc;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;

import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.util.IMJAXBUtil;
import edu.harvard.i2b2.im.util.IMUtil;
import edu.harvard.i2b2.im.ws.PDORequestMessage;


import edu.harvard.i2b2.im.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.im.datavo.i2b2message.SecurityType;


public class CallCRCUtil {

	//	private SecurityType securityType = null;
	//	private String projectId = null;
	//private String crcUrl = null;
	static IMUtil workplaceUtil = IMUtil.getInstance();
	private static Log log = LogFactory.getLog(CallCRCUtil.class);
	protected static final Log logesapi = LogFactory.getLog(CallCRCUtil.class);


	public static String callCRCPDORequest(PDORequestMessage getFoldersMsg)
			throws I2B2Exception {
		//		ResultResponseType resultResponseType = null;
		
		String response = null;
		try {
			log.debug("begin build element");
			RequestMessageType requestMessageType = getFoldersMsg.getRequestMessageType();//getRequestMessageType();
					
					// buildResultInstanceRequestXMLRequestMessage(resultInstanceID, securityType, projectId);
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's request xml "
					+ requestElement);
			response = getServiceClient("pdorequest", requestElement).toString();
			logesapi.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's response xml " + response.toString());
			//resultResponseType = getResultResponseMessage(response.toString());
			//masterInstanceResultResponseType = getResponseMessage(response
			//		.toString());

		} catch (JAXBUtilException jaxbEx) {
			log.error(jaxbEx.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			throw new I2B2Exception("Error in CRC upload ", e);
		} finally {

		}			

		return response;
	}


	public static String callCRCQueryRequestXML(String queryMasterId, SecurityType securityType,  String projectId)
			throws I2B2Exception {
		//		ResultResponseType resultResponseType = null;
		//MasterResponseType masterInstanceResultResponseType = null;
		String response = null;
		try {
			log.debug("begin build element");
			RequestMessageType requestMessageType = null; //buildSetfinderRequestXMLRequestMessage(queryMasterId, securityType, projectId);
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's request xml "
					+ requestElement);
			response = getServiceClient("/request", requestElement).toString();

			//log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's response xml " + response.toString());
			//resultResponseType = getResultResponseMessage(response.toString());
			//masterInstanceResultResponseType = getMasterInstanceResultResponseMessage(response
			//		.toString());

		} catch (JAXBUtilException jaxbEx) {
			log.error(jaxbEx.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);
		} finally {
	
		}	
		return response;
	}


	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.im.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.im.datavo.i2b2message.ObjectFactory();
		IMJAXBUtil.getJAXBUtil().marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		OMElement request = OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();

		return request;
	}

	private static String getServiceClient(String operationName, OMElement request) throws Exception {
		// call
		String response = null;


		 response = ServiceClient.sendREST(workplaceUtil.getCRCUrl() + operationName, request.toString());

		return response;

	}

}
