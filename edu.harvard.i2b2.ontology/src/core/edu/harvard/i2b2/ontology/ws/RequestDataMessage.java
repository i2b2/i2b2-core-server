/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.bind.JAXBElement;


/**
 * The RequestDataMessage class is a helper class to build Ontology messages in the
 * i2b2 format
 */
public abstract class RequestDataMessage{
	private static Log log = LogFactory.getLog(RequestDataMessage.class);

	RequestMessageType reqMessageType = null;

	/**
	 * The constructor
	 */
	public RequestDataMessage(String requestVdo) throws I2B2Exception {
		try {
			JAXBElement jaxbElement = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(requestVdo);

			if (jaxbElement == null) {
				throw new I2B2Exception(
						"Null value from unmarshall for VDO xml : " + requestVdo);
			}

			this.reqMessageType = (RequestMessageType) jaxbElement.getValue();
		} catch (JAXBUtilException e) {
			log.error(e.getMessage(), e);
			throw new I2B2Exception("Umarshaller error: " + e.getMessage() +
					requestVdo, e);
		}
	}



	public RequestMessageType getRequestMessageType() { 
		return reqMessageType;
	}

	public MessageHeaderType getMessageHeaderType() {
		return reqMessageType.getMessageHeader();
	}

}