/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryManagerBean;
import edu.harvard.i2b2.crc.ejb.QueryManagerBeanUtil;
import edu.harvard.i2b2.crc.ejb.role.PriviledgeLocal;
import edu.harvard.i2b2.crc.util.CacheUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * RunQueryInstanceFromQueryDefinitionHandler class implements execute method
 * $Id: RunQueryInstanceFromQueryDefinitionHandler.java,v 1.6 2008/03/19
 * 22:36:37 rk903 Exp $
 * 
 * @author rkuttan
 */
public class RunQueryInstanceFromQueryDefinitionHandler extends RequestHandler {
	QueryDefinitionRequestType queryDefRequestType = null;
	String requestXml = null;
	boolean lockedoutFlag = false,errorFlag = false;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public RunQueryInstanceFromQueryDefinitionHandler(String requestXml)
			throws I2B2Exception {
		try {
			queryDefRequestType = (QueryDefinitionRequestType) getRequestType(
					requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType.class);
			this.requestXml = requestXml;
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 * 
	 * @throws I2B2Exception
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
	 */
	public BodyType execute() throws I2B2Exception {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String response = null;
		BodyType bodyType = new BodyType();
		MasterInstanceResultResponseType masterInstanceResponse = null;
		try {
			log.debug("Running Execute");

			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(this
					.getDataSourceLookup().getDomainId(), getDataSourceLookup()
					.getProjectPath(), getDataSourceLookup().getOwnerId());

			String rolePath = getDataSourceLookup().getDomainId() + "/"
					+ getDataSourceLookup().getProjectPath() + "/"
					+ getDataSourceLookup().getOwnerId();

			//List<String> roles = (List<String>) cache.getRoot().get(rolePath);
			log.debug("Roles from get " + rolePath);
			List<String> roles = (List<String>) CacheUtil.get(rolePath);
			if (roles != null) {
				log.debug("Roles from size " + roles.size());
			} else {
				log.debug("Roles from get is null ");
			}

			//TODO check previlieges
			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			PriviledgeLocal privilegeLocal = qpUtil.getPriviledgeLocal();

			privilegeLocal.checkPriviledge(daoFactory,
					"SETFINDER_QRY_WITH_DATAOBFSC", roles);
        	//TODO removed ejbs
//			QueryManagerLocalHome queryManagerLocalHome = qpUtil
//					.getQueryManagerLocalHome();

//			QueryManagerLocal queryManagerLocal = queryManagerLocalHome
//					.create();

			log.debug("Calling QueryManager ");
			QueryManagerBean queryManagerLocal = new QueryManagerBean();
			masterInstanceResponse = queryManagerLocal.processQuery(this
					.getDataSourceLookup(), requestXml);
			if (masterInstanceResponse.getStatus() != null) {
				StatusType status = masterInstanceResponse.getStatus();
				if (status.getCondition().get(0) != null) {
					if (status.getCondition().get(0).getValue() != null) {
						if (status.getCondition().get(0).getValue().indexOf(
								"LOCKEDOUT") > -1) {
							lockedoutFlag = true;
						}
					}
				}
			}
			log.debug("Calling QueryInstance ");
			QueryInstanceType queryInstance = masterInstanceResponse.getQueryInstance();
			QueryStatusTypeType statusType = queryInstance.getQueryStatusType();
			log.debug("checking status type: " + statusType.getStatusTypeId());
			//masterInstanceResponse.getStatus().getCondition().get(0).g
			if (statusType.getStatusTypeId() != null && statusType.getStatusTypeId().trim().equals("4")) { 
				log.debug("Changing status type to ERROR");
				StatusType status =  new StatusType();
				StatusType.Condition condition = new StatusType.Condition();
				condition.setType("ERROR");
				condition.setValue("ERROR");
				status.getCondition().add(condition);
				masterInstanceResponse.setStatus(status);
				errorFlag = true;
			} 
			
			else if (statusType.getStatusTypeId() != null && statusType.getStatusTypeId().trim().equals("5")) {
				//why is '5' (incomplete) == error ?
				
				QueryStatusTypeType newStatusType = new QueryStatusTypeType();
				newStatusType.setName("ERROR");
				newStatusType.setDescription("ERROR");
				newStatusType.setStatusTypeId("4");
				masterInstanceResponse.getQueryInstance().setQueryStatusType(newStatusType);

								masterInstanceResponse.setStatus(this.buildCRCStausType(
										 "ERROR", "ERROR"));
			/*
			} else if (statusType.getStatusTypeId() != null && statusType.getStatusTypeId().trim().equals("3")) {
						masterInstanceResponse.setStatus(this.buildCRCStausType(
								 "DONE", "DONE"));
									
			} else if (statusType.getStatusTypeId() != null && !statusType.getStatusTypeId().trim().equals("6")) {
				masterInstanceResponse.setStatus(this.buildCRCStausType(
						 "RUNNING", "RUNNING"));
			 */
			} else {
			 masterInstanceResponse.setStatus(this.buildCRCStausType(
			 RequestHandlerDelegate.DONE_TYPE, "DONE"));
			}
			//If Batchmode is equal to PROCESSING then set to finished
			if (masterInstanceResponse.getQueryInstance().getBatchMode().equals(QueryManagerBeanUtil.PROCESSING))
			{
				masterInstanceResponse.setStatus(this.buildCRCStausType(
						 RequestHandlerDelegate.DONE_TYPE, "DONE"));
				
				if (masterInstanceResponse.getQueryResultInstance().get(0).getQueryStatusType().getName().equals("QUEUED")
						|| masterInstanceResponse.getQueryResultInstance().get(0).getQueryStatusType().getName().equals("TIMEDOUT"))
				{
					
					masterInstanceResponse.getStatus().getCondition().get(0).setType("RUNNING");
					masterInstanceResponse.getStatus().getCondition().get(0).setValue("RUNNING");

					
					masterInstanceResponse.getQueryInstance().setBatchMode("MEDIUM_QUEUE");
					QueryStatusTypeType newStatusType = new QueryStatusTypeType();
					newStatusType.setName("MEDIUM_QUEUE");
					newStatusType.setDescription("MEDIUM_QUEUE");
					newStatusType.setStatusTypeId("7");
					masterInstanceResponse.getQueryInstance().setQueryStatusType(newStatusType);
				} else 	if (masterInstanceResponse.getQueryResultInstance().get(0).getQueryStatusType().getName().equals("ERROR"))
				{
					masterInstanceResponse.getStatus().getCondition().get(0).setType("ERROR");
					masterInstanceResponse.getStatus().getCondition().get(0).setValue("ERROR");
					masterInstanceResponse.getQueryInstance().setBatchMode("ERROR");
					QueryStatusTypeType newStatusType = new QueryStatusTypeType();
					newStatusType.setName("ERROR");
					newStatusType.setDescription("ERROR");
					newStatusType.setStatusTypeId("4");
					masterInstanceResponse.getQueryInstance().setQueryStatusType(newStatusType);
				} else {
					masterInstanceResponse.getQueryInstance().setBatchMode(masterInstanceResponse.getQueryInstance().getQueryStatusType().getName());
				}
			}
			 response = this.buildResponseMessage(requestXml, bodyType);
		} catch (Exception ee) {
			log.debug("Ran into a error: " + ee.getMessage());
			masterInstanceResponse = new MasterInstanceResultResponseType();
			masterInstanceResponse.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.ERROR_TYPE, ee.getMessage()));
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory psmObjFactory = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(
					psmObjFactory.createResponse(masterInstanceResponse));
		}

		return bodyType;
	}


	public boolean getLockedoutFlag() {
		return lockedoutFlag;
	}
	
	public boolean getErrorFlag() { 
		return errorFlag;
	}
}
