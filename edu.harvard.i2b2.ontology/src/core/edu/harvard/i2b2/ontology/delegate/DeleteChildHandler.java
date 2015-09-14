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

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.ConceptPersistDao;
import edu.harvard.i2b2.ontology.dao.OntProcessStatusDao;
import edu.harvard.i2b2.ontology.dao.OntologyProcessType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.DeleteChildType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.ws.DeleteChildDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;


public class DeleteChildHandler extends RequestHandler {
	private DeleteChildDataMessage  deleteChildMsg = null;
	private DeleteChildType deleteChildType = null;
	private ProjectType projectInfo = null;
	
	public DeleteChildHandler(DeleteChildDataMessage requestMsg) throws I2B2Exception{
		
		deleteChildMsg = requestMsg;
		deleteChildType = requestMsg.deleteChildType();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());

	}
	
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptPersistDao deleteChildDao = new ConceptPersistDao();
		ResponseMessageType responseMessageType = null;
		int numDeleted = -1;

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(deleteChildMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		if(!Roles.getInstance().isRoleValid(projectInfo)){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(deleteChildMsg.getMessageHeaderType(), "User does not have correct privileges");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("INVALID_USER_PRIV");
			return response;	
		}
		
		else {	
			try {
				numDeleted = deleteChildDao.deleteNode(deleteChildType, projectInfo, this.getDbInfo());
			} catch (Exception e1) {
				log.error("DeleteChildHandler received exception");
				responseMessageType = MessageFactory.doBuildErrorResponse(deleteChildMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numDeleted == 0) {
				log.error("delete object not found");
				responseMessageType = MessageFactory.doBuildErrorResponse(deleteChildMsg.getMessageHeaderType(), "Node not found");
			}
			else if (numDeleted == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(deleteChildMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(deleteChildMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader);

				// child was deleted
				// update status message to indicate that DELETE occurred

				OntProcessStatusDao ontProcessStatusDao = new OntProcessStatusDao(
						getDataSource(this.getDbInfo().getDb_dataSource()),
						projectInfo, this.getDbInfo());

				OntologyProcessStatusType ontProcessStatusType = new OntologyProcessStatusType();
				SecurityType securityType = deleteChildMsg.getMessageHeaderType().getSecurity();

				int rowsAdded = ontProcessStatusDao
					.createOntologyProcessType(OntologyProcessType.ONT_DELETE_CONCEPT,
						securityType.getUsername());
			}
		}
		
		
        String responseWdo = null;
        responseWdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseWdo;
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