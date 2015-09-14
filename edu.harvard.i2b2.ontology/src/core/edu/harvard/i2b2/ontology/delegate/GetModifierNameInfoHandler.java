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
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.ConceptDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifierType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifiersType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.ws.GetNameInfoDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetModifierNameInfoHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetModifierNameInfoHandler.class);
	private GetNameInfoDataMessage  nameInfoMsg = null;
	private VocabRequestType vocabType = null;
	private ProjectType project = null; 

	public GetModifierNameInfoHandler(GetNameInfoDataMessage requestMsg) throws I2B2Exception  {
		try {
			nameInfoMsg = requestMsg;
			vocabType = requestMsg.getVocabRequestType();
			setDbInfo(requestMsg.getMessageHeaderType());
			// test case for bad user
			//		nameInfoMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
			project = getRoleInfo(requestMsg.getMessageHeaderType());

		} catch (JAXBUtilException e) {
			log.error("error setting up getNameInfoHandler");
			throw new I2B2Exception("GetNameInfoHandler not configured");
		} 
	}
	@Override
	public String execute() throws I2B2Exception {
		// call ejb and pass input object
		ConceptDao conceptDao = new ConceptDao();
		ModifiersType modifiers = new ModifiersType();
		ResponseMessageType responseMessageType = null;
		
		// if project == null, user was not validated or PM service problem

		if(project == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response; 
		} 
	
		List response = null;
		try {
			response = conceptDao.findModifierNameInfo(vocabType, project, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			log.error(e1.getMessage());
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Database error");
		} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Database configuration error");
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Query results are empty");
			}
			// No errors, non-empty response received
			// If max is specified, check that response is not > max
			else if(vocabType.getMax() != null) {
				// if max exceeded send error message
				if(response.size() > vocabType.getMax()){
					log.debug("Max request size of " + vocabType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "MAX_EXCEEDED");
				}
				// otherwise send results
				else {
					Iterator itr = response.iterator();
					while (itr.hasNext())
					{
						ModifierType node = (ModifierType)itr.next();
						modifiers.getModifier().add(node);
				}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(nameInfoMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,modifiers);
				}  
			}
			//max not specified so send results
			else {
				Iterator itr = response.iterator();
				while (itr.hasNext())
				{
					ModifierType node = (ModifierType)itr.next();
					modifiers.getModifier().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(nameInfoMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,modifiers);
			}        
		}
        String responseVdo = null;
        responseVdo = MessageFactory.convertToXMLString(responseMessageType);
 //       log.info("MODnameInfoResponse: " + responseVdo);
		return responseVdo;
	}
}
