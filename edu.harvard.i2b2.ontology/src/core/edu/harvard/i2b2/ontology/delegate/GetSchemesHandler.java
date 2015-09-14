/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.delegate;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.SchemesDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.ws.GetSchemesDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetSchemesHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetSchemesHandler.class);
	private GetSchemesDataMessage  getSchemesMsg = null;
	private GetReturnType getReturnType = null;

	public GetSchemesHandler(GetSchemesDataMessage requestMsg) throws I2B2Exception{
		try {
			getSchemesMsg = requestMsg;
			getReturnType = requestMsg.getReturnType();		
			setDbInfo(requestMsg.getMessageHeaderType());
		} catch (JAXBUtilException e) {
			log.error("error setting up getSchemesHandler");
			throw new I2B2Exception("GetSchemesHandler not configured");
		} 

	}
	@Override
	public String execute()throws I2B2Exception{
		// call ejb and pass input object
		SchemesDao schemesDao = new SchemesDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;
		
		List response = null;
		try {
			response = schemesDao.findSchemes(getReturnType, this.getDbInfo());
		} catch (DataAccessException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getSchemesMsg.getMessageHeaderType(), "Database error");
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(getSchemesMsg.getMessageHeaderType(), "Query results are empty");
			}
			// No errors, non-empty response received
			// max not specified so send results
			else {
				Iterator it = response.iterator();
				while (it.hasNext())
				{
					ConceptType node = (ConceptType)it.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getSchemesMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
			}      
		}
		String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}    
 
}
