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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.OntProcessStatusDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.vdo.DirtyValueType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.ws.GetDirtyStateDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

public class GetDirtyStateHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetDirtyStateHandler.class);
	private GetDirtyStateDataMessage  getDirtyStateMsg = null;
	private GetReturnType getReturnType = null;
	private ProjectType projectInfo = null;

	public GetDirtyStateHandler(GetDirtyStateDataMessage requestMsg) throws I2B2Exception{
		try {
			getDirtyStateMsg = requestMsg;
			getReturnType = requestMsg.getReturnType();		
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());
			setDbInfo(requestMsg.getMessageHeaderType());
		} catch (JAXBUtilException e) {
			log.error("error setting up getDirtyStateHandler");
			throw new I2B2Exception("GetDirtyStateHandler not configured");
		} 

	}
	@Override
	public String execute()throws I2B2Exception{
		// call ejb and pass input object
		SecurityType securityType = getDirtyStateMsg.getMessageHeaderType().getSecurity();

		// update the process status
		OntProcessStatusDao dirtyStateDao = new OntProcessStatusDao(
				getDataSource(this.getDbInfo().getDb_dataSource()),
				projectInfo, this.getDbInfo());
		
		ResponseMessageType responseMessageType = null;
	
		try {
			DirtyValueType dirtyType = dirtyStateDao.getDirtyState(getReturnType, this.getDbInfo());
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getDirtyStateMsg.getMessageHeaderType());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,dirtyType);
		} catch (DataAccessException e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getDirtyStateMsg.getMessageHeaderType(), "Database error");
		}
	
		String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		log.debug(responseVdo);
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
