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
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.ws.ModifyChildDataMessage;
import edu.harvard.i2b2.ontology.datavo.vdo.*;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.*;
import edu.harvard.i2b2.ontology.dao.*;

public class ModifyChildHandler extends RequestHandler {
	private ModifyChildDataMessage  childMsg = null;
	private ModifyChildType childType = null;
	private ProjectType projectInfo = null;
	
	public ModifyChildHandler(ModifyChildDataMessage requestMsg) throws I2B2Exception{
		
		childMsg = requestMsg;
		childType = requestMsg.modifyChildType();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());

	}
	
	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptPersistDao modifyChildDao = new ConceptPersistDao();
		ResponseMessageType responseMessageType = null;
		int numAdded = -1;
		int numDirty = -1;

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		if(!Roles.getInstance().isRoleValid(projectInfo)){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "User does not have correct privileges");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("INVALID_USER_PRIV");
			return response;	
		}
		
		else {	

			try {
				// find out if fullname, name or concept code or visAttrib was modified.
				// If numDirty == 0 then one of these fields was modified.
				numDirty = modifyChildDao.dirtyCandidate(childType, projectInfo, this.getDbInfo());
				
				numAdded = modifyChildDao.modifyNode(childType, projectInfo, this.getDbInfo());
			} catch (Exception e1) {
				e1.printStackTrace();
				log.error("ModifyChildHandler received exception");
				responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (numAdded == 0) {
				log.error("concept not modified");
				responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "Database insertion error");
			}
			else if (numAdded == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(childMsg.getMessageHeaderType(), "Database error");
			}
			else {
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(childMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader);

				// child was modified 
				// update status message to indicate that EDIT occurred
				// iff node's name or basecode was modified

				if(numDirty == 0){
					OntProcessStatusDao ontProcessStatusDao = new OntProcessStatusDao(
							getDataSource(this.getDbInfo().getDb_dataSource()),
							projectInfo, this.getDbInfo());

					OntologyProcessStatusType ontProcessStatusType = new OntologyProcessStatusType();
					SecurityType securityType = childMsg.getMessageHeaderType().getSecurity();

					int rowsAdded = ontProcessStatusDao
					.createOntologyProcessType(OntologyProcessType.ONT_EDIT_CONCEPT,
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