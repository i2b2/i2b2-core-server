/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.delegate;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.XMLGregorianCalendarDeserializer;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.ConceptDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.ontology.ws.GetNameInfoDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetNameInfoHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetNameInfoHandler.class);
	private GetNameInfoDataMessage  nameInfoMsg = null;
	private VocabRequestType vocabType = null;
	private ProjectType project = null; 

	public GetNameInfoHandler(GetNameInfoDataMessage requestMsg) throws I2B2Exception  {
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
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;
		
		// if project == null, user was not validated or PM service problem

		if(project == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response; 
		} 
	
		List<ConceptType> response = null;
		try {
			response = conceptDao.findNameInfo(vocabType, project, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			log.error(e1.getMessage());
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Database error");
		} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Database configuration error");
		}
		
		//jgk
		// This does a linear search through fullnames for each previous fullname, O(n^2) :(
		// BUT it assumes its sorted by hlevel so it only has to search through whats already seen - n(n+1)/2 operations 
		if (response.size()>0 && vocabType.isReducedResults()!=null && vocabType.isReducedResults()) {
			ArrayList<String> seen = new ArrayList<String>(); 
			ArrayList<ConceptType> keep = new ArrayList<ConceptType>();
			Iterator it = response.iterator();
			while (it.hasNext())
			{
				ConceptType node = (ConceptType)it.next();
				String key = node.getKey();
				boolean bAbort = false;
				for (String k : seen) {
					if(key.startsWith(k) && !key.equals(k) /* <-- don't kill the synonyms */ ) {
						bAbort = true;
						break;
					}
				}
				if (!bAbort) { 
					/*// This section annotates node names with a category prefix, either PREFIX:code from basecode or CATEGORY\etc\ in the key
					String nodeType = "";
					if (node.getBasecode().contains(":")) nodeType=node.getBasecode().substring(0,node.getBasecode().indexOf(":"));
					else if (node.getKey().contains("\\")) node.getKey().substring(0, node.getKey().indexOf("\\"));
					node.setName("("+nodeType+") "+node.getName());
					*/
					// Add nodes that were not subsumed to the keep list
					keep.add(node);
				}
				// Hidden and inactive should not subsume other nodes - exclude them
				if (node.getVisualattributes().contains("A")) 
					seen.add(node.getKey());
			}
			log.debug("Reduced find terms from "+response.size()+" to "+keep.size());
			response = keep;
		}
		
		// no errors found 
		 String responseVdo = null;
		 
		 Gson gson = new GsonBuilder().setPrettyPrinting()
	                .registerTypeAdapter(
	                     XMLGregorianCalendar.class,
	                     new XMLGregorianCalendarDeserializer() )
	                .create();
		 
		 
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
						ConceptType node = (ConceptType)itr.next();
						concepts.getConcept().add(node);
					}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(nameInfoMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
				}  
			}
			//max not specified so send results
			else {
				Iterator itr = response.iterator();
				while (itr.hasNext())
				{
					ConceptType node = (ConceptType)itr.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(nameInfoMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
				
							 
				//responseVdo = gson.toJson(response);
			}        
		}
       
		if (responseVdo == null)
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}
}
