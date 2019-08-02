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
import edu.harvard.i2b2.ontology.ws.LoadDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.vdo.*;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.*;
import edu.harvard.i2b2.ontology.dao.*;

public class LoadMetadataHandler extends RequestHandler {
	private LoadDataMessage  loadMsg = null;
	private MetadataLoadType conceptsType = null;
	private ProjectType projectInfo = null;

	public LoadMetadataHandler(LoadDataMessage requestMsg) throws I2B2Exception{

		loadMsg = requestMsg;
		conceptsType = requestMsg.getMetadataLoad();	
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	
		setDbInfo(requestMsg.getMessageHeaderType());

	}

	@Override
	public String execute() throws I2B2Exception{
		// call ejb and pass input object
		ConceptPersistDao persistDao = new ConceptPersistDao();
		ResponseMessageType responseMessageType = null;
		int[] numAdded = {-1};

		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(loadMsg.getMessageHeaderType(), "User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;	
		}
		if(!Roles.getInstance().isRoleValid(projectInfo)){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(loadMsg.getMessageHeaderType(), "User does not have correct privileges");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("INVALID_USER_PRIV");
			return response;	
		}

		else {	
			try {
				//numAdded = 
				persistDao.createMetadataTable(this.getDbInfo(), loadMsg.getMetadataLoad().getTableName());
				persistDao.loadMetadata(this.getDbInfo(), loadMsg.getMetadataLoad().getTableName(),loadMsg.getMetadataLoad().getMetadata());
			} catch (Exception e1) {
				e1.printStackTrace();
				log.error("LoadMetadataHandler received exception");
				responseMessageType = MessageFactory.doBuildErrorResponse(loadMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			/*		if (numAdded == 0) {
				log.error("concept not inserted");
				responseMessageType = MessageFactory.doBuildErrorResponse(loadMsg.getMessageHeaderType(), "Database insertion error");
			}
			else if (numAdded == -1) {
				log.error("database error");
				responseMessageType = MessageFactory.doBuildErrorResponse(loadMsg.getMessageHeaderType(), "Database error");
			}
			 */
			//		else {
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(loadMsg.getMessageHeaderType());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader);

			//	}
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
