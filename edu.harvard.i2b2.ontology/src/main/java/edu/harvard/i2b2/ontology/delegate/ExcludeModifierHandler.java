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

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.ws.AddChildDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.vdo.*;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.*;
import edu.harvard.i2b2.ontology.dao.*;

public class ExcludeModifierHandler extends RequestHandler {
	private AddChildDataMessage  addChildMsg = null;
	private ModifierType addChildType = null;
	private ProjectType projectInfo = null;
	
	public ExcludeModifierHandler(AddChildDataMessage requestMsg) throws I2B2Exception{
		
		addChildMsg = requestMsg;
		addChildType = requestMsg.getModifier();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());

	}
	
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptPersistDao addChildDao = new ConceptPersistDao();
		ResponseMessageType responseMessageType = null;
		int numAdded = -1;

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		if(!Roles.getInstance().isRoleValid(projectInfo)){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "User does not have correct privileges");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("INVALID_USER_PRIV");
			return response;	
		}
		
		else {	
			try {
				numAdded = addChildDao.excludeNode(addChildType, projectInfo, this.getDbInfo());
			} catch (Exception e1) {
				e1.printStackTrace();
				log.error("ExcludeModifierHandler received exception");
				responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numAdded == 0) {
				log.error("exclusion modifier not inserted");
				responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "Database insertion error");
			}
			else if (numAdded == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(addChildMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(addChildMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader);

				// child was added
				// update status message to indicate that EDIT occurred if non-synonym added
				//
				if(addChildType.getSynonymCd().equals("N")){
					OntProcessStatusDao ontProcessStatusDao = new OntProcessStatusDao(
							getDataSource(this.getDbInfo().getDb_dataSource()),
							projectInfo, this.getDbInfo());

					OntologyProcessStatusType ontProcessStatusType = new OntologyProcessStatusType();
					SecurityType securityType = addChildMsg.getMessageHeaderType().getSecurity();

					int rowsAdded = ontProcessStatusDao
					.createOntologyProcessType(OntologyProcessType.ONT_ADD_CONCEPT,
							securityType.getUsername());
				}
			}
		}
        String responseVdo = null;
        responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}
	
	private DataSource getDataSource(String dataSourceName) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSourceName);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
			;
		}
		return ds;
	}
    
}
