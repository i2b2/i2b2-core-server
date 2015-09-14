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
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetCategoriesType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.ws.GetCategoriesDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

public class GetCategoriesHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetCategoriesHandler.class);
	private GetCategoriesDataMessage  getCategoriesMsg = null;
	private GetCategoriesType getCategoriesType = null;
	private ProjectType projectInfo = null;

	public GetCategoriesHandler(GetCategoriesDataMessage requestMsg) throws I2B2Exception{
		try {
			getCategoriesMsg = requestMsg;
			getCategoriesType = requestMsg.getCatType();
			projectInfo = getRoleInfo(getCategoriesMsg.getMessageHeaderType());	
			// test sub project with owner = @
		//	getCategoriesMsg.getMessageHeaderType().setProjectId("Demo/RA_demo/sub_demo/sub-sub/");
			setDbInfo(getCategoriesMsg.getMessageHeaderType());
		} catch (JAXBUtilException e) {
			log.error("error setting up getCategoriesHandler");
			throw new I2B2Exception("GetCategoriesHandler not configured");
		}

	}
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptDao conceptDao = new ConceptDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		
		List response = null;
		try {
			response = conceptDao.findRootCategories(getCategoriesType, projectInfo, this.getDbInfo());
		}catch (I2B2DAOException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "Database error");
		} catch (I2B2Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "Database error");
		}

		// no errors found
		if(responseMessageType == null) {
			// no db error, but response is empty
			if (response == null)  {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "Query results are empty");
			}

			// no db error; non-empty response received
			else { 
				Iterator it = response.iterator();
				while (it.hasNext())
				{
					ConceptType node = (ConceptType)it.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getCategoriesMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
			}
		}
        String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}    	
}
