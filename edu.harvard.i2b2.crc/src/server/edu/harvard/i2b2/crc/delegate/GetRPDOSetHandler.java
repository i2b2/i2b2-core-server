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
 * 		Mike Mendis
 */
package edu.harvard.i2b2.crc.delegate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.axis2.MessageFactory;
import edu.harvard.i2b2.crc.axis2.GetRPDODataMessage;
import edu.harvard.i2b2.crc.dao.RPDODao;
import edu.harvard.i2b2.crc.dao.pdo.RpdoTable;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConceptTableType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RpdoType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RpdosType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.loader.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.loader.delegate.pm.PMServiceDriver;
import jakarta.xml.bind.JAXBElement;

import java.util.Iterator;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetRPDOSetHandler extends RPDOReqHandler {
	private static Log log = LogFactory.getLog(GetRPDOSetHandler.class);
	private GetRPDODataMessage RPDOsDataMsg = null;
	private MessageHeaderType msgHdrType = null;
	private RPDODao RPDODao = null;
	private boolean isAdmin = false;
	private RpdoType rpdoType = null;
	private String requestXml = null;



	public GetRPDOSetHandler(GetRPDODataMessage requestMsg, String requestElementString) throws I2B2Exception{
		try {
			RPDOsDataMsg = requestMsg;
			rpdoType = RPDOsDataMsg.getRPDOTType();
			msgHdrType = RPDOsDataMsg.getMessageHeaderType();
			requestXml = requestElementString;

			
			//isAdmin = isAdmin(msgHdrType);	
			RPDODao = new RPDODao(msgHdrType);
		} catch (JAXBUtilException e) {
			log.error("error setting up GetAllRPDOsHandler");
			throw new I2B2Exception("GetAllRPDOsHandler not configured");
		}
	}

	@Override
	public String execute() throws I2B2Exception {

		// Call PM cell to validate user
		StatusType procStatus = null;
		ProjectType projectType = null;
		String projectId = null;

		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
		BodyType bodyType = null;
		
		try {

			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();
			 bodyType = requestMessageType.getMessageBody();

			if (bodyType == null) {
				log.error("null value in body type");
				throw new I2B2Exception("null value in body type");
			}
			
			SecurityType securityType = null;
			if (msgHdrType != null) {
				if (msgHdrType.getSecurity() != null) {
					securityType = msgHdrType
							.getSecurity();
				}
				projectId = msgHdrType
						.getProjectId();
			}
			if (securityType == null) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus
						.setValue("Request message missing user/password");
				return I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				
				//throw new I2B2Exception("Request message missing user/password");
			}

			PMServiceDriver pmServiceDriver = new PMServiceDriver();
			projectType = pmServiceDriver.checkValidUser(securityType,
					projectId);
			if (projectType == null) {
				procStatus = new StatusType();
				procStatus.setType("ERROR");
				procStatus
						.setValue("Invalid user/password for the given domain");
				return I2B2MessageResponseFactory.buildResponseMessage(
						requestXml, procStatus, bodyType);
				//throw new I2B2Exception("Request message missing user/password");

			}

			log.debug("project name from PM " + projectType.getName());
			//logesapi.debug("project id from PM " + projectType.getId());
			if (projectType.getRole().get(0) != null) {
				log.debug("Project role from PM "
						+ projectType.getRole().get(0));
			} else {
				log.warn("project role not set for user ["
						+ securityType.getUsername() + "]");
			}

		} catch (AxisFault e) {
			procStatus = new StatusType();
			procStatus.setType("ERROR");
			procStatus.setValue("Could not connect to server");
			//return I2B2MessageResponseFactory.buildResponseMessage(
			//		requestXml, procStatus, bodyType);
			throw new I2B2Exception("Request message missing user/password");

		} catch (I2B2Exception e) {
			procStatus = new StatusType();
			procStatus.setType("ERROR");
			procStatus
					.setValue("Message error connecting Project Management cell");
			//return I2B2MessageResponseFactory.buildResponseMessage(
			//		requestXml, procStatus, bodyType);
			throw new I2B2Exception("Request message missing user/password");

		} catch (JAXBUtilException e) {
			procStatus = new StatusType();
			procStatus.setType("ERROR");
			procStatus
					.setValue("Message error from Project Management cell");
			//return I2B2MessageResponseFactory.buildResponseMessage(
			//		requestXml, procStatus, bodyType);
			throw new I2B2Exception("Request message missing user/password");

		}

		
		//log.info(" execute()");
		MessageHeaderType msgHdr = MessageFactory.createResponseMessageHeader(msgHdrType);          
		ResponseMessageType responseMessageType = null;
		//RpdosType dblus = new RpdosType();
		int response = -1;
		try {
			response = RPDODao.setRPDO(rpdoType);
		} catch (Exception e1) {
			String dbError = "Database error";
			responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, dbError);
			log.error(dbError);
			e1.printStackTrace();
		}
		if (responseMessageType == null) { // no db error
			if (response == -1) { // but response is empty
				String emptyResult = "ID does not exist or owner";
				log.warn(emptyResult);
				responseMessageType = MessageFactory.doBuildErrorResponse(msgHdr, emptyResult);
			} else { 
				RpdosType dblus = new RpdosType();
				RpdoType dblu = new RpdoType();
				dblu.setId(response);

				dblus.getRpdo().add(dblu);
				responseMessageType = MessageFactory.createBuildResponse(msgHdr, dblus);

			}
		}
		String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}    	
}
