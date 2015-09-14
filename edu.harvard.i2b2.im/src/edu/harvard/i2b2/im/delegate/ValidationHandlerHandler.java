/*
 * Copyright (c) 2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Creator:
 * 		Neha Patel
 */
package edu.harvard.i2b2.im.delegate;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.util.HighEncryption;
import edu.harvard.i2b2.im.ws.MessageFactory;
import edu.harvard.i2b2.im.ws.PDORequestMessage;
import edu.harvard.i2b2.im.ws.PDOResponseMessage;

import edu.harvard.i2b2.im.dao.IMKey;
import edu.harvard.i2b2.im.dao.PdoDao;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.im.datavo.pdo.PidSet;
import edu.harvard.i2b2.im.datavo.pdo.PidType;
import edu.harvard.i2b2.im.datavo.pdo.PidType.PatientMapId;
import edu.harvard.i2b2.im.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.im.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoRequestTypeType;
import edu.harvard.i2b2.im.datavo.pdo.query.RequestType;
import edu.harvard.i2b2.im.datavo.pdo.query.PidListType.Pid;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;

public class ValidationHandlerHandler extends RequestHandler {
	private String userId = null;

	private PDORequestMessage  pdoRequestMsg;
	private PdoQryHeaderType requestType;
	private ProjectType projectInfo = null;
	private PdoDao pdoDao;

	public ValidationHandlerHandler(PDORequestMessage requestMsg) throws I2B2Exception{

		try {
			pdoDao = new PdoDao();
			pdoRequestMsg = requestMsg;
			requestType = requestMsg.getPdoQryHeaderType();
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	

			userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
			setDbInfo(requestMsg.getMessageHeaderType());

		} catch (Exception e) {
			log.error("error setting up ValidationHandlerHandler");
			throw new I2B2Exception("ValidationHandlerHandler not configured");
		} 
	}

	public String execute() throws I2B2Exception{

		// call ejb and pass input object
		ResponseMessageType responseMessageType = null;
		String errResponse = "";
		Boolean errorFlag = false;

		// check to see if we have userId(if not indicates problem)
		if(userId == null) {
			log.error("user Id is null");
			responseMessageType = MessageFactory.doBuildErrorResponse(pdoRequestMsg.getMessageHeaderType(), "PM service is not responding");
		}



		// Error flag has been set to true, return a error response with appropriate message
		if(errorFlag){
			responseMessageType = MessageFactory.doBuildErrorResponse(pdoRequestMsg.getMessageHeaderType(), errResponse);
			errResponse = MessageFactory.convertToXMLString(responseMessageType);
			return errResponse;	
		}

		String response = null;
		// Verify key is set
		try {
			if (IMKey.isKeySet(projectInfo) == -11111)
				responseMessageType = MessageFactory.doBuildErrorResponse(pdoRequestMsg.getMessageHeaderType(), "User does not have correct privileges");
			else if (IMKey.isKeySet(projectInfo) == 0)
				responseMessageType = MessageFactory.doBuildErrorResponse(pdoRequestMsg.getMessageHeaderType(), "Key not set");
		} catch (Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(pdoRequestMsg.getMessageHeaderType(), "Database error");
		}


		//	String responseWdo = null;

		PDOResponseMessage protectedDataMsg = new PDOResponseMessage();

		try {
			PatientDataResponseType patientDataResponsse = new PatientDataResponseType();
					//protectedDataMsg.getPatientDataFromResponseXML(response);

			GetPDOFromInputListRequestType getPDOFromInputListRequestType = pdoRequestMsg.getgetPDOFromInputListRequestType();

			//Go through the patients and encrypt them
			List<Pid> pids = getPDOFromInputListRequestType.getInputList().getPidList().getPid(); 

			// create a new pid set 
			PidType pidType = new PidType();

			for (Pid pid: pids) {

				PatientMapId pmapId = new  PatientMapId();
				pmapId.setSource(pid.getSource());
				pmapId.setValue(pid.getValue());
			
				pidType.getPatientMapId().add(pmapId);
			}
			
			PidSet pidset = new PidSet();
			pidset.getPid().add(pidType);
			PidSet newids = pdoDao.findPidsByProject(pidset, userId, projectInfo, this.getDbInfo());
			
			//Save pids in Audit Table
			pdoDao.addAudit(projectInfo.getId(), newids, userId, null, this.getDbInfo());
			
			PatientDataType patientDataType = new PatientDataType();
			patientDataType.setPidSet(newids);
					
			patientDataResponsse.setPatientData(patientDataType);
					//patientDataResponsse.s.getPatientData();

			responseMessageType = MessageFactory.createBuildResponsePdo(pdoRequestMsg.getMessageHeaderType(), null);
			responseMessageType.setMessageBody(protectedDataMsg.getBodyType(patientDataResponsse));
			response = MessageFactory.convertToXMLString(responseMessageType);
	

			//response = protectedDataMsg.doBuildXML(patientDataResponsse,pdoRequestMsg.getMessageHeaderType());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return response;		
	}
}
