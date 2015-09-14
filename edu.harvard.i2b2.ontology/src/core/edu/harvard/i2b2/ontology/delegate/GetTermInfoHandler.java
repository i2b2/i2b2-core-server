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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.ConceptDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.ws.GetTermInfoDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetTermInfoHandler extends RequestHandler{
	private static Log log = LogFactory.getLog(GetTermInfoHandler.class);
	private GetTermInfoDataMessage  getTermInfoMsg = null;
	private GetTermInfoType getTermInfoType = null;
	private ProjectType project = null;

	public GetTermInfoHandler(GetTermInfoDataMessage requestMsg) throws I2B2Exception {
		try {
			getTermInfoMsg = requestMsg;
			getTermInfoType = requestMsg.getTermInfoType();
			setDbInfo(requestMsg.getMessageHeaderType());
			// test case for bad user
			//			getTermInfoMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
			project = getRoleInfo(getTermInfoMsg.getMessageHeaderType());

		} catch (JAXBUtilException e) {
			log.error("error setting up getTermInfoHandler");
			throw new I2B2Exception("GetTermInfoHandler not configured");
		} 
	}

	@Override
	public String execute() throws I2B2Exception {
		// call ejb and pass input object
		ConceptDao conceptDao = new ConceptDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;

		// if project == null, user was not validated or PM service problem

		if(project == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	 
		} 

		List response = null;
		try {
			response = conceptDao.findByFullname(getTermInfoType, project, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Ontology database error");
		} catch (I2B2Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Ontology database configuration error");
		} catch (DataAccessException dataAccessEx) { 
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Could not locate record in table_access table");
		}
		//no errors found
		if(responseMessageType == null) {
//			no db error but response is empty
			if (response == null) {
				log.debug("query results are null");
				responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Query results are empty");
			}
			// No errors, non-empty response received
			// max not specified so send results
			else {
				Iterator itr = response.iterator();
				while (itr.hasNext())
				{
					ConceptType node = (ConceptType)itr.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getTermInfoMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
			}        
		}
		String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}
}
