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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.OntProcessStatusDao;
import edu.harvard.i2b2.ontology.dao.OntologyProcessType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.datavo.vdo.UpdateCrcConceptType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.ws.CRCUpdateConceptMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class CRCConceptUpdateHandler extends RequestHandler {
	private CRCUpdateConceptMessage updateCrcConceptMsg = null;
	private UpdateCrcConceptType updateCrcConceptType = null;
	private ProjectType projectInfo = null;
	private MessageHeaderType messageHeaderType = null;

	public CRCConceptUpdateHandler(CRCUpdateConceptMessage requestMsg)
			throws I2B2Exception {

		updateCrcConceptMsg = requestMsg;
		updateCrcConceptType = requestMsg.getChild();
		this.messageHeaderType = requestMsg.getMessageHeaderType();
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());
		setDbInfo(requestMsg.getMessageHeaderType());

	}

	@Override
	public String execute() throws I2B2Exception {
		// call ejb and pass input object

		ResponseMessageType responseMessageType = null;

		String errorMessage = null;

		// check to see if we have projectInfo (if not indicates PM service
		// problem)
		OntologyProcessStatusType ontProcessStatusType = null;
		if (projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(
					updateCrcConceptMsg.getMessageHeaderType(),
					"User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;
		}
		if(!Roles.getInstance().isRoleValid(projectInfo)){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(updateCrcConceptMsg.getMessageHeaderType(), "User does not have correct privileges");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("INVALID_USER_PRIV");
			return response;	
		}
		
		else {
			try {
				SecurityType securityType = getSecurityType(messageHeaderType);

				// update the process status
				OntProcessStatusDao ontProcessStatusDao = new OntProcessStatusDao(
						getDataSource(this.getDbInfo().getDb_dataSource()),
						projectInfo, this.getDbInfo());

				ontProcessStatusType = new OntologyProcessStatusType();
				if (updateCrcConceptType.getOperationType().equalsIgnoreCase(
						"synchronize_all")) {
					ontProcessStatusType
							.setProcessTypeCd(OntologyProcessType.ONT_SYNCALL_CRC_CONCEPT);
				} else {
					ontProcessStatusType
							.setProcessTypeCd(OntologyProcessType.ONT_UPDATE_CRC_CONCEPT);
				}

				ontProcessStatusType.setProcessStepCd("ONT_BUILD_PDO_START");
				ontProcessStatusType.setMessage("");

				ontProcessStatusType = ontProcessStatusDao
						.createOntologyProcessStatus(ontProcessStatusType,
								securityType.getUsername());
				System.out.println("process id "
						+ ontProcessStatusType.getProcessId());
				int processId = Integer.parseInt(ontProcessStatusType
						.getProcessId());
				ontProcessStatusType = ontProcessStatusDao.findById(processId);
				// start this process in a thread
				ExecutorRunnable er = new ExecutorRunnable();

				Map parameterMap = new HashMap();
				boolean synchronizeAllFlag = updateCrcConceptType
						.getOperationType().equalsIgnoreCase("synchronize_all");
				boolean hiddenConceptFlag = updateCrcConceptType.isHiddens();

				parameterMap.put("ProjectType", projectInfo);
				parameterMap.put("DBInfoType", this.getDbInfo());
				parameterMap.put("MessageHeaderType", messageHeaderType);
				parameterMap.put("ProcessId", processId);
				parameterMap.put("SynchronizeAllFlag", synchronizeAllFlag);
				parameterMap.put("HiddenConceptFlag", hiddenConceptFlag);
				parameterMap.put("RequestRunnable",new CRCConceptUpdateRunnable());
				er.setParameter(parameterMap);
				Thread worker = new Thread(er);
				worker.start();
			} catch (Throwable t) {
				t.printStackTrace();
				errorMessage = t.toString();
			}
			// return the message

		}
		// no errors found
		if (ontProcessStatusType != null) {
			// no db error but response is empty
			MessageHeaderType messageHeader = MessageFactory
					.createResponseMessageHeader(updateCrcConceptMsg
							.getMessageHeaderType());
			responseMessageType = MessageFactory.createProcessStatusResponse(
					messageHeader, ontProcessStatusType);

		} else {
			MessageHeaderType messageHeader = MessageFactory
					.createResponseMessageHeader(updateCrcConceptMsg
							.getMessageHeaderType());
			responseMessageType = MessageFactory.doBuildErrorResponse(
					messageHeaderType, errorMessage);
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

	private SecurityType getSecurityType(MessageHeaderType messageHeaderType) {
		SecurityType securityType = messageHeaderType.getSecurity();
		return securityType;
	}

	private String getProjectId(MessageHeaderType messageHeaderType) {
		return messageHeaderType.getProjectId();
	}

}