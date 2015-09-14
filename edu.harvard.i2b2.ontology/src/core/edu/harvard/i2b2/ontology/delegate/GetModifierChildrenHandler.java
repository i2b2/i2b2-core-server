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
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifierChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifierType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifiersType;
import edu.harvard.i2b2.ontology.ws.GetModifierChildrenDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;


public class GetModifierChildrenHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetModifierChildrenHandler.class);
	private GetModifierChildrenDataMessage  getModifierChildrenMsg = null;
	private GetModifierChildrenType getModifierChildrenType = null;
	private ProjectType project = null; 

	public GetModifierChildrenHandler(GetModifierChildrenDataMessage requestMsg) throws I2B2Exception{
			try {
				getModifierChildrenMsg = requestMsg;
				getModifierChildrenType = requestMsg.getModifierChildrenType();
				// test multi level project
	//			getModifierChildrenMsg.getMessageHeaderType().setProjectId("Demo/RA_demo/sub_demo/");
				setDbInfo(requestMsg.getMessageHeaderType());
				
				// test case for bad user
	//					getModifierChildrenMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
				project = getRoleInfo(getModifierChildrenMsg.getMessageHeaderType());
			} catch (JAXBUtilException e) {
				log.error("error setting up getModifierChildrenHandler");
				throw new I2B2Exception("GetModifierChildrenHandler not configured");
			}
	}
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptDao conceptDao = new ConceptDao();
		ModifiersType modifiers = new ModifiersType();
		ResponseMessageType responseMessageType = null;
		
		// if project == null, user was not validated or PM service problem

		if(project == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getModifierChildrenMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	 
		} 

		List response = null;
		try {
			response = conceptDao.findChildrenByParent(getModifierChildrenType, project, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getModifierChildrenMsg.getMessageHeaderType(), "Database error");
		} catch (I2B2Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getModifierChildrenMsg.getMessageHeaderType(), "Database error");
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(getModifierChildrenMsg.getMessageHeaderType(), "Query results are empty");
			}
//			 No errors, non-empty response received
			// If max is specified, check that response is not > max
			else if(getModifierChildrenType.getMax() != null) {
				// if max exceeded send error message
				if(response.size() > getModifierChildrenType.getMax()){
					log.debug("Max request size of " + getModifierChildrenType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(getModifierChildrenMsg.getMessageHeaderType(), "MAX_EXCEEDED");
				}
				// otherwise send results
				else {
					Iterator it = response.iterator();
					while (it.hasNext())
					{
						ModifierType node = (ModifierType)it.next();
						modifiers.getModifier().add(node);
					}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getModifierChildrenMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,modifiers);
				}       
			}

			// max not specified so send results
			else {
				Iterator it = response.iterator();
				while (it.hasNext())
				{
					ModifierType node = (ModifierType)it.next();
					modifiers.getModifier().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getModifierChildrenMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,modifiers);
			}     
		}
        String responseVdo = null;
        responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}
    
}