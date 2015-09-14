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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.util.EMPI;
import edu.harvard.i2b2.im.util.HighEncryption;
import edu.harvard.i2b2.im.util.IMUtil;
import edu.harvard.i2b2.im.ws.MessageFactory;
import edu.harvard.i2b2.im.ws.PDORequestMessage;
import edu.harvard.i2b2.im.ws.PDOResponseMessage;

import edu.harvard.i2b2.im.dao.IMKey;
import edu.harvard.i2b2.im.dao.PdoDao;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.pdo.BlobType;
import edu.harvard.i2b2.im.datavo.pdo.ParamType;
import edu.harvard.i2b2.im.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.im.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.im.datavo.pdo.PatientSet;
import edu.harvard.i2b2.im.datavo.pdo.PatientType;
import edu.harvard.i2b2.im.datavo.pdo.PidSet;
import edu.harvard.i2b2.im.datavo.pdo.PidType;
import edu.harvard.i2b2.im.datavo.pdo.PidType.PatientMapId;
import edu.harvard.i2b2.im.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoRequestTypeType;
import edu.harvard.i2b2.im.datavo.pdo.query.RequestType;
import edu.harvard.i2b2.im.datavo.pdo.query.PidListType.Pid;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;

public class PdoHandlerHandler extends RequestHandler {
	private String userId = null;

	private PDORequestMessage  getFoldersMsg;
	private PdoQryHeaderType requestType;
	private ProjectType projectInfo = null;
	private PdoDao pdoDao;

	public PdoHandlerHandler(PDORequestMessage requestMsg) throws I2B2Exception{

		try {
			pdoDao = new PdoDao();
			getFoldersMsg = requestMsg;
			requestType = requestMsg.getPdoQryHeaderType();
			projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());	

			userId = requestMsg.getMessageHeaderType().getSecurity().getUsername();
			setDbInfo(requestMsg.getMessageHeaderType());

		} catch (Exception e) {
			log.error("error setting up getNameInfoHandler");
			throw new I2B2Exception("GetNameInfoHandler not configured");
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
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "PM service is not responding");
		}



		// Error flag has been set to true, return a error response with appropriate message
		if(errorFlag){
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			errResponse = MessageFactory.convertToXMLString(responseMessageType);
			return errResponse;	
		}

		String response = null;
		// Verify key is set
		try {
			if (IMKey.isKeySet(projectInfo) == -11111)
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "User does not have correct privileges");
			else if (IMKey.isKeySet(projectInfo) == 0)
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), "Key not set");
			else 
				response = pdoDao.getPDO(requestType, projectInfo, getFoldersMsg);
		} catch (Exception e1) {
			responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), e1.getMessage());
		}


		if(responseMessageType != null) {
			// no db error but response is empty
			if (response == null){
				errResponse = responseMessageType.getResponseHeader().getResultStatus().getStatus().getValue();
				log.error(errResponse);
				responseMessageType = MessageFactory.doBuildErrorResponse(getFoldersMsg.getMessageHeaderType(), errResponse);
			}
		}
		//	String responseWdo = null;
		if (response == null) {
			return MessageFactory.convertToXMLString(responseMessageType);
		} else {

			PDOResponseMessage protectedDataMsg = new PDOResponseMessage();


			try {
				//List<Pid> pidsnew =  new ArrayList<Pid>();

				EMPI security = null;
				if (!IMUtil.getInstance().getEMPIService().equalsIgnoreCase("none"))
				{

					String classname = "";
					if (IMUtil.getInstance().getEMPIService().equalsIgnoreCase("OpenEMPI"))
						classname = "edu.harvard.i2b2.im.util.EMPIOpenEMPI";

					ClassLoader classLoader = PdoHandlerHandler.class.getClassLoader();

					try {
						Class empiClass = classLoader.loadClass(classname);


						security =  (EMPI) empiClass.newInstance();

					} catch (ClassNotFoundException e) {
						log.equals("Did not find class: " + e.getMessage());
						throw new Exception ("Error loading class: " + e.getMessage());
					}
				}
				PatientDataResponseType patientDataResponsse = protectedDataMsg.getPatientDataFromResponseXML(response);

				PatientDataType patientDataType = patientDataResponsse.getPatientData();

				List<PidType> pids = patientDataType.getPidSet().getPid();

				//List<PatientType> patientType = new ArrayList<PatientType>();
				PatientSet patientSet = new PatientSet();
				//patientDataType.getPatientSet().getPatient();

				List<PidType> newPidTypes = new ArrayList();				
				HighEncryption highEnc = new  HighEncryption(IMKey.getKey(projectInfo));
				if (highEnc == null)
					throw new I2B2Exception ("High Encrpytion not found.");
				for (PidType pidType : pids) //pids.get(0).getPatientMapId())
				{
					PidType newPidType = new PidType();
					boolean found = false;
					//PidType.PatientMapId patientId = pid.getPatientMapId();
					for (PatientMapId patientId: pidType.getPatientMapId()) {

						//If source ends with _E than decrypt
						if (patientId.getSource().endsWith("_E")) {
							String decrypt = highEnc.mrn_decrypt(patientId.getValue(), true);
							if (decrypt.equals(""))
								decrypt = highEnc.generic_decrypt(patientId.getValue());
							if (decrypt.equals(""))
							{
								patientId.setValue(patientId.getValue());
								patientId.setSource(patientId.getSource());
							} else {

								patientId.setValue(decrypt);
								patientId.setSource(patientId.getSource().substring(0,patientId.getSource().length()-2) );
							}
						} else {
							patientId.setValue(patientId.getValue());
							patientId.setSource(patientId.getSource());
						}

						//Create a Pid for verifying if in project
					//	Pid newpid = new Pid();
					//	newpid.setSource(patientId.getSource());
				//		newpid.setValue(patientId.getValue());
				//		pidsnew.add(newpid);

						//Get Patient Data from EMPI Service
						if (!IMUtil.getInstance().getEMPIService().equalsIgnoreCase("none") && security != null && found == false)
						{

							

							String blog = security.findPerson(userId, patientId.getSource(), patientId.getValue());

							if ((blog != null) && (blog.length() > 0)) {
								
								PatientType ptype = new PatientType();
								PatientIdType patientIdType = new PatientIdType();
								patientIdType.setSource(patientId.getSource());
								patientIdType.setValue(patientId.getValue());

								ptype.setPatientId(patientIdType);

								security.parse(ptype);
								patientSet.getPatient().add(ptype);
								 
								newPidType.setPatientId(pidType.getPatientId());
								security.getIds(newPidType);
								
								newPidTypes.add(newPidType);
								found = true;
							} 

						}
					}
					
					
					// Add a empty patient if not found in the empi system and using a empi system
					/*
					if ( security != null && found == false)
					{
						PatientType ptype = new PatientType();
						PatientIdType patientIdType = new PatientIdType();
						patientIdType.setSource(pidType.getPatientId().getSource());
						patientIdType.setValue(pidType.getPatientId().getValue());

						ptype.setPatientId(patientIdType);

						patientSet.getPatient().add(ptype);
					}
					*/
				}

				if (!IMUtil.getInstance().getEMPIService().equalsIgnoreCase("none") && security != null)
				{
					//Replace pidset with one from empi service
					patientDataType.getPidSet().getPid().clear();
					patientDataType.getPidSet().getPid().addAll(newPidTypes);
				}
				if (IMUtil.getInstance().checkPatientInProject()) {

					//verify that the patients are in the patient group table
					patientDataType.setPidSet(pdoDao.findPidsByProject(patientDataType.getPidSet(), userId, projectInfo, this.getDbInfo()));
				}

				patientDataType.setPatientSet( patientSet);


			


				//Add patient set to audit trail
				pdoDao.addAudit(projectInfo.getId(),  patientDataType.getPidSet(), userId, null, this.getDbInfo());

				response = protectedDataMsg.doBuildXML(patientDataResponsse,getFoldersMsg.getMessageHeaderType());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return response;		
		}
	}    	
}
