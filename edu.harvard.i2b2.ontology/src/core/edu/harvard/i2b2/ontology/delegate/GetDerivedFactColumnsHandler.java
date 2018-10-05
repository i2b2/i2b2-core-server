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
import edu.harvard.i2b2.ontology.datavo.vdo.DerivedFactColumnsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.ws.GetTermInfoDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetDerivedFactColumnsHandler extends RequestHandler{
	private static Log log = LogFactory.getLog(GetDerivedFactColumnsHandler.class);
	private GetTermInfoDataMessage  getTermInfoMsg = null;
	private GetTermInfoType getTermInfoType = null;
	private ProjectType project = null;

	public GetDerivedFactColumnsHandler(GetTermInfoDataMessage requestMsg) throws I2B2Exception {
		try {
			getTermInfoMsg = requestMsg;
			getTermInfoType = requestMsg.getTermInfoType();
			setDbInfo(requestMsg.getMessageHeaderType());
			// test case for bad user
			//			getTermInfoMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
			project = getRoleInfo(getTermInfoMsg.getMessageHeaderType());

		} catch (JAXBUtilException e) {
			log.error("error setting up getDerivedFactColumnsHandler");
			throw new I2B2Exception("GetDerivedFactColumnsHandler not configured");
		} 
	}

	@Override
	public String execute() throws I2B2Exception {
		// call ejb and pass input object
		ConceptDao conceptDao = new ConceptDao();
		DerivedFactColumnsType columns = new DerivedFactColumnsType();
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
			response = conceptDao.findDerivedFactColumns(getTermInfoType, project, this.getDbInfo());
		} catch (I2B2DAOException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Ontology database error");
		} catch (I2B2Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Ontology database configuration error");
		} catch (DataAccessException dataAccessEx) { 
			responseMessageType = MessageFactory.doBuildErrorResponse(getTermInfoMsg.getMessageHeaderType(), "Could not locate record in table_access table");
		}
		//no errors found
		if(responseMessageType == null) {

			Iterator itr = response.iterator();
			while (itr.hasNext())
			{
				String column = (String)itr.next();
				columns.getDerivedFactTableColumn().add(column);
			}
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getTermInfoMsg.getMessageHeaderType());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,columns);

		}
		String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}
}
