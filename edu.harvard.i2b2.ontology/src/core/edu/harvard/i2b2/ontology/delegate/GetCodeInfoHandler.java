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
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.ws.GetCodeInfoDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

	public class GetCodeInfoHandler extends RequestHandler {
	    private static Log log = LogFactory.getLog(GetCodeInfoHandler.class);
		private GetCodeInfoDataMessage  codeInfoMsg = null;
		private VocabRequestType vocabType = null;
		private ProjectType project = null; 
		
		public GetCodeInfoHandler(GetCodeInfoDataMessage requestMsg) throws I2B2Exception {
			try {
				codeInfoMsg = requestMsg;
				vocabType = requestMsg.getVocabRequestType();	
				setDbInfo(requestMsg.getMessageHeaderType());
				// test case for bad user
		//				codeInfoMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
				project = getRoleInfo(codeInfoMsg.getMessageHeaderType());
			}  catch (JAXBUtilException e) {
				log.error("error setting up codeInfoHandler");
				throw new I2B2Exception("GetCodeInfoHandler not configured");
			}
		}
		@Override
		public String execute() throws I2B2Exception {
			// call ejb and pass input object
			ConceptDao conceptDao = new ConceptDao();
			ConceptsType concepts = new ConceptsType();
			ResponseMessageType responseMessageType = null;
			
//			 if project == null, user was not validated or PM service problem

			if(project == null) {
				String response = null;
				responseMessageType = MessageFactory.doBuildErrorResponse(codeInfoMsg.getMessageHeaderType(), "User was not validated");
				response = MessageFactory.convertToXMLString(responseMessageType);
				log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
				return response;	 
			} 
			

			List response = null;
			try {
				response = conceptDao.findCodeInfo(vocabType, project, this.getDbInfo());
			} catch (I2B2DAOException e1) {
				responseMessageType = MessageFactory.doBuildErrorResponse(codeInfoMsg.getMessageHeaderType(), "Ontology database error");
			}  catch (I2B2Exception e1) {
				responseMessageType = MessageFactory.doBuildErrorResponse(codeInfoMsg.getMessageHeaderType(), "Ontology database configuration error");
			}
			
			// no errors found
			if(responseMessageType == null) {
//				 no db error but response is empty	
				if (response == null) {
					log.debug("query results are null");
					responseMessageType = MessageFactory.doBuildErrorResponse(codeInfoMsg.getMessageHeaderType(), "Query results are empty");
				}
//				 No errors, non-empty response received
				// max not specified so send results
				else {
					Iterator itr = response.iterator();
					while (itr.hasNext())
					{
						ConceptType node = (ConceptType)itr.next();
			//			log.info(node.getKey());
						concepts.getConcept().add(node);
					}
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(codeInfoMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
				}    
			}
	        String responseVdo = null;
	        responseVdo = MessageFactory.convertToXMLString(responseMessageType);
			return responseVdo;
		}
}
