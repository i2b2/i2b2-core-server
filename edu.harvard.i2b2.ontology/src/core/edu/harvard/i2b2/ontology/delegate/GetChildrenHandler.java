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
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.ws.GetChildrenDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;


public class GetChildrenHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetChildrenHandler.class);
	private GetChildrenDataMessage  getChildrenMsg = null;
	private GetChildrenType getChildrenType = null;
	private ProjectType project = null; 

	public GetChildrenHandler(GetChildrenDataMessage requestMsg) throws I2B2Exception{
			try {
				getChildrenMsg = requestMsg;
				getChildrenType = requestMsg.getChildrenType();
				// test multi level project
	//			getChildrenMsg.getMessageHeaderType().setProjectId("Demo/RA_demo/sub_demo/");
				setDbInfo(requestMsg.getMessageHeaderType());
				
				// test case for bad user
	//					getChildrenMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
				project = getRoleInfo(getChildrenMsg.getMessageHeaderType());
			} catch (JAXBUtilException e) {
				log.error("error setting up getChildrenHandler");
				throw new I2B2Exception("GetChildrenHandler not configured");
			}
	}
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptDao conceptDao = new ConceptDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;
		
		// if project == null, user was not validated or PM service problem

		if(project == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	 
		} 

		List response = null;
		try {
			response = conceptDao.findChildrenByParent(getChildrenMsg, project, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
		} catch (I2B2Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
		} catch(JAXBUtilException e1){
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Incoming request message error");
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Query results are empty");
			}
//			 No errors, non-empty response received
			// If max is specified, check that response is not > max
			else if(getChildrenType.getMax() != null) {
				// if max exceeded send error message
				if(response.size() > getChildrenType.getMax()){
					log.debug("Max request size of " + getChildrenType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "MAX_EXCEEDED");
				}
				// otherwise send results
				else {
					Iterator it = response.iterator();
					while (it.hasNext())
					{
						ConceptType node = (ConceptType)it.next();
						concepts.getConcept().add(node);
					}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
				}       
			}

			// max not specified so send results
			else {
				Iterator it = response.iterator();
				while (it.hasNext())
				{
					ConceptType node = (ConceptType)it.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
			}     
		}
        String responseVdo = null; 
        responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}
    
}