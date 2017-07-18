/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultTypeDao;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestXmlType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultTypeResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserRequestType;

/**
 * Ejb manager class for query operation
 * 
 * @author rkuttan
 * 
 * @ejb.bean description="QueryTool Query Info"
 *           display-name="QueryTool Query Info"
 *           jndi-name="ejb.querytool.QueryInfo"
 *           local-jndi-name="ejb.querytool.QueryInfoLocal"
 *           name="querytool.QueryInfo" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryInfoRemote"
 * 
 * 
 */
public class QueryInfoBean { //implements SessionBean {
	private static Log log = LogFactory.getLog(QueryInfoBean.class);

	/**
	 * Function to return master query list for the given user id
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param userRequestType
	 *            user_id
	 * 
	 * @return String publish response XML
	 */
	public MasterResponseType getQueryMasterListFromUserId(
			DataSourceLookup dataSourceLookup, UserRequestType userRequestType)
			throws I2B2DAOException {

		String userId = userRequestType.getUserId();
		int fetchSize = userRequestType.getFetchSize();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao.getQueryMasterByUserId(
				userId, fetchSize);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		return masterResponseType;
	}

	
	/**
	 * Function to return master query list for the give group id
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param userRequestType
	 *            group_id
	 * 
	 * @return String publish response XML
	 * @throws I2B2Exception 
	 */
	public MasterResponseType getQueryMasterListFromNameInfo(
			DataSourceLookup dataSourceLookup, SecurityType userRequestType, FindByChildType findChildType)
			throws I2B2Exception {
		//String groupId = userRequestType.getGroupId();
		//int fetchSize = userRequestType.getFetchSize();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao
				.getQueryMasterByNameInfo(userRequestType, findChildType);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		return masterResponseType;
	}

	
	/**
	 * Function to return master query list for the give group id
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param userRequestType
	 *            group_id
	 * 
	 * @return String publish response XML
	 * @throws I2B2DAOException
	 */
	public MasterResponseType getQueryMasterListFromGroupId(
			DataSourceLookup dataSourceLookup, UserRequestType userRequestType)
			throws I2B2DAOException {
		String groupId = userRequestType.getGroupId();
		int fetchSize = userRequestType.getFetchSize();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao
				.getQueryMasterByGroupId(groupId, fetchSize);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		return masterResponseType;
	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param int session id publish request XML fileName
	 * 
	 * @return String publish response XML
	 */
	public MasterResponseType getRequestXmlFromMasterId(
			DataSourceLookup dataSourceLookup, String userId,
			MasterRequestType masterRequestType) throws I2B2Exception {
		String queryMasterId = masterRequestType.getQueryMasterId();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();

		QtQueryMaster qtQueryMaster = queryMasterDao
				.getQueryDefinition(queryMasterId);
		MasterResponseType masterResponseType = new MasterResponseType();
		if (qtQueryMaster != null) {
			QueryMasterType queryMasterType = new QueryMasterType();
			queryMasterType.setQueryMasterId(qtQueryMaster.getQueryMasterId());
			queryMasterType.setName(qtQueryMaster.getName());
			queryMasterType.setUserId(qtQueryMaster.getUserId());
			String requestXml = qtQueryMaster.getRequestXml();

			if (requestXml != null) {
				Document doc = null;
				RequestXmlType requestXmlType = new RequestXmlType();
				try {
					/*
					 * //get jaxb object JAXBContext jc1 =
					 * JAXBContext.newInstance
					 * (edu.harvard.i2b2.crc.datavo.setfinder
					 * .query.ObjectFactory.class); Unmarshaller unMarshaller =
					 * jc1.createUnmarshaller(); JAXBElement jaxbElement =
					 * (JAXBElement)unMarshaller.unmarshal(new
					 * StringReader(requestXml)); QueryDefinitionType
					 * queryDefinition =
					 * (QueryDefinitionType)jaxbElement.getValue();
					 * 
					 * 
					 * //marshall to dom JAXBContext jc =
					 * JAXBContext.newInstance
					 * (edu.harvard.i2b2.crc.datavo.setfinder
					 * .query.QueryDefinitionType.class); Marshaller m =
					 * jc.createMarshaller(); DocumentBuilderFactory f =
					 * DocumentBuilderFactory.newInstance(); DocumentBuilder
					 * builder = f.newDocumentBuilder(); doc =
					 * builder.newDocument(); m.marshal((new
					 * edu.harvard.i2b2.crc
					 * .datavo.setfinder.query.ObjectFactory(
					 * )).createQueryDefinition(queryDefinition), doc);
					 */

					doc = edu.harvard.i2b2.common.util.xml.XMLUtil
							.convertStringToDOM(requestXml);
					log.debug("query definition xml prefix "
							+ doc.getDocumentElement().getPrefix());
					requestXmlType.getContent().add(doc.getDocumentElement());
				} catch (Exception i2b2) {
					i2b2.printStackTrace();
					throw new I2B2Exception(
							"Error converting request xml to dom "
									+ i2b2.getMessage(), i2b2);
				}
				queryMasterType.setRequestXml(requestXmlType);
			}
			masterResponseType.getQueryMaster().add(queryMasterType);
		} else {
			throw new I2B2Exception("Could not find query for masterId: ["
					+ queryMasterId + "]");
		}
		return masterResponseType;
	}

	/**
	 * Function to delete master query
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param string
	 *            user id
	 * @param int master id
	 * 
	 * @return String Master Query response XML
	 */
	public MasterResponseType deleteQueryMaster(
			DataSourceLookup dataSourceLookup, String userId, String masterId)
			throws I2B2Exception {
		if (masterId==null){
			log.debug("Null master id sent to deleteQueryMaster method");
			return null;
		}
		else {
			SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
					dataSourceLookup.getDomainId(), dataSourceLookup
					.getProjectPath(), dataSourceLookup.getOwnerId());
			IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();

			queryMasterDao.deleteQuery(masterId);

			MasterResponseType masterResponseType = new MasterResponseType();
			QueryMasterType queryMasterType = new QueryMasterType();
			queryMasterType.setQueryMasterId(masterId);
			masterResponseType.getQueryMaster().add(queryMasterType);
			return masterResponseType;
		}
	}

	/**
	 * Function to rename master query
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param int session id publish request XML fileName
	 * 
	 * @return Master Query response XML
	 */
	public MasterResponseType renameQueryMaster(
			DataSourceLookup dataSourceLookup, String userId, String masterId,
			String queryNewName) throws I2B2Exception {
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();

		queryMasterDao.renameQuery(masterId, queryNewName);

		MasterResponseType masterResponseType = new MasterResponseType();
		QueryMasterType queryMasterType = new QueryMasterType();
		queryMasterType.setQueryMasterId(masterId);
		queryMasterType.setUserId(userId);
		queryMasterType.setName(queryNewName);
		masterResponseType.getQueryMaster().add(queryMasterType);
		return masterResponseType;
	}

	/**
	 * Function to update query result instance description
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            resultInstanceId
	 * @param Strign
	 *            description
	 * 
	 * @return Master Query response XML
	 */
	public ResultResponseType updateResultInstanceDescription(
			DataSourceLookup dataSourceLookup, String resultInstanceId,
			String newDescription) throws I2B2Exception {
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryResultInstanceDao queryResultInstanceDao = sfDaoFactory
				.getPatientSetResultDAO();

		queryResultInstanceDao.updateResultInstanceDescription(
				resultInstanceId, newDescription);
		QtQueryResultInstance updatedQueryResultInstance = queryResultInstanceDao
				.getResultInstanceById(resultInstanceId);

		ResultResponseType resultResponseType = new ResultResponseType();
		QueryResultInstanceType resultInstanceType = PSMFactory
				.buildQueryResultInstanceType(updatedQueryResultInstance);
		resultResponseType.getQueryResultInstance().add(resultInstanceType);
		return resultResponseType;
	}

	/**
	 * Function to update query instance with cancel status
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            instanceId
	 * 
	 * @return Instance Query response XML
	 */
	public InstanceResultResponseType cancelQueryInstance(
			DataSourceLookup dataSourceLookup, String instanceId)
			throws I2B2Exception {
		InstanceResultResponseType instanceResultResponseType = new InstanceResultResponseType();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		// get query instance by id and change the status type to cancelled.
		IQueryInstanceDao queryInstanceDao = sfDaoFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(instanceId);
		QtQueryStatusType queryStatusType = queryInstance
				.getQtQueryStatusType();
		int queryStatusTypeId = queryStatusType.getStatusTypeId();
		// check if the query is already completed
		if ((queryStatusTypeId == 3 || queryStatusTypeId == 4
				|| queryStatusTypeId == 6 || queryStatusTypeId == 10)) {
			log.error("Query instance  [" + instanceId
					+ "] is already finished");
			throw new I2B2Exception("Query instance  [" + instanceId
					+ "] is already finished");
		}
		// check if the query is already cancelled
		if (queryStatusTypeId == 9) {
			log.warn("Already the query [" + instanceId
					+ "] is in Cancelled state");
		}
		queryStatusType.setStatusTypeId(9);

		queryInstance.setQtQueryStatusType(queryStatusType);
		queryInstanceDao.update(queryInstance, false);
		queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(instanceId);

		IQueryResultInstanceDao queryResultInstanceDao = sfDaoFactory
				.getPatientSetResultDAO();
		List<QtQueryResultInstance> resultInstanceList = queryResultInstanceDao
				.getResultInstanceList(instanceId);
		QueryInstanceType queryInstanceResponse = PSMFactory
				.buildQueryInstanceType(queryInstance);
		instanceResultResponseType.setQueryInstance(queryInstanceResponse);

		// update cancelled status to all the result instance
		String resultInstanceId = "";
		int statusTypeId = 0;
		for (QtQueryResultInstance resultInstance : resultInstanceList) {
			resultInstanceId = resultInstance.getResultInstanceId();
			queryResultInstanceDao.updatePatientSet(resultInstanceId, 9, 0);
			resultInstance.getQtQueryStatusType().setStatusTypeId(9);
			QueryResultInstanceType resultInstanceResponse = PSMFactory
					.buildQueryResultInstanceType(resultInstance);
			instanceResultResponseType.getQueryResultInstance().add(
					resultInstanceResponse);
		}

		return instanceResultResponseType;
	}

	/**
	 * Function to return all query result type
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 */
	public ResultTypeResponseType getAllResultType(
			DataSourceLookup dataSourceLookup) throws I2B2Exception {
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryResultTypeDao resultTypeDao = sfDaoFactory
				.getQueryResultTypeDao();
		List<QtQueryResultType> queryResultTypeList = resultTypeDao
				.getAllQueryResultType();
		ResultTypeResponseType resultTypeResponseType = new ResultTypeResponseType();
		List<QueryResultTypeType> returnQueryResultType = new ArrayList<QueryResultTypeType>();
		for (QtQueryResultType queryResultType : queryResultTypeList) {
			returnQueryResultType.add(PSMFactory
					.buildQueryResultType(queryResultType));
		}
		resultTypeResponseType.getQueryResultType().addAll(
				returnQueryResultType);
		return resultTypeResponseType;
	}

	// -------------------------------------------------
	// private functions
	// -------------------------------------------------
	private MasterResponseType buildMasterResponseType(
			List<QtQueryMaster> masterList) {
		MasterResponseType masterResponseType = new MasterResponseType();
		// masterResponseType
		DTOFactory dtoFactory = new DTOFactory();
		for (QtQueryMaster queryMaster : masterList) {
			QueryMasterType queryMasterType = new QueryMasterType();
			queryMasterType.setQueryMasterId(queryMaster.getQueryMasterId());
			java.util.Date createDate = queryMaster.getCreateDate();
			queryMasterType.setCreateDate(dtoFactory
					.getXMLGregorianCalendar(createDate.getTime()));
			java.util.Date deleteDate = queryMaster.getDeleteDate();
			if (deleteDate != null) {
				queryMasterType.setDeleteDate(dtoFactory
						.getXMLGregorianCalendar(deleteDate.getTime()));
			}
			queryMasterType.setName(queryMaster.getName());
			queryMasterType.setGroupId(queryMaster.getGroupId());
			queryMasterType.setUserId(queryMaster.getUserId());
			queryMasterType.setMasterTypeCd(queryMaster.getMasterTypeCd());
			masterResponseType.getQueryMaster().add(queryMasterType);
		}
		return masterResponseType;
	}

	private SetFinderDAOFactory getSetFinderDaoFactory(String domainId,
			String projectPath, String ownerId) throws I2B2DAOException {
		DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectPath,
				ownerId);
		SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory()
				.getSetFinderDAOFactory();
		return sfDaoFactory;
	}

}
