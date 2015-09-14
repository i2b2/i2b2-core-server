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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtXmlResult;
import edu.harvard.i2b2.crc.datavo.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlValueType;

/**
 * Ejb manager class for query operation
 * 
 * 
 * @ejb.bean description="QueryTool Query Result"
 *  			display-name="QueryTool Query Result" 
 *  		  jndi-name="ejb.querytool.QueryResult"
 *           local-jndi-name="ejb.querytool.QueryResultLocal"
 *           name="querytool.QueryResult" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryResultRemote"
 * 
 * 
 */
public class QueryResultBean { // implements SessionBean {
	private static Log log = LogFactory.getLog(QueryResultBean.class);
	
	/**
	 * 
	 * 
	 * @throws I2B2DAOException 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * 
	 */
	public ResultResponseType getResultInstanceFromQueryInstanceId(DataSourceLookup dataSourceLookup,String userId, String  queryInstanceId) throws I2B2DAOException {
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(dataSourceLookup.getDomainId(), dataSourceLookup.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryResultInstanceDao patientSetResultDao = sfDaoFactory.getPatientSetResultDAO();
		log.debug("got resultinstancesdao" + patientSetResultDao.toString());
		List<QtQueryResultInstance> queryResultInstanceList = patientSetResultDao.getResultInstanceList( queryInstanceId);
		log.debug("got QtQueryResultInstance" + queryResultInstanceList.size());
		ResultResponseType resultResponseType = new ResultResponseType();
		DTOFactory dtoFactory = new DTOFactory(); 
		for(QtQueryResultInstance resultInstance: queryResultInstanceList) { 
			
			QueryResultInstanceType queryResultInstanceType = PSMFactory.buildQueryResultInstanceType(resultInstance);
			//System.out.println("RESULT INSTANCE " + resultInstance.getResultInstanceId() );
			resultResponseType.getQueryResultInstance().add(queryResultInstanceType);
		}
		//System.out.print("SIZE OF RESULT INSTANCE "+ resultResponseType.getQueryResultInstance().size());
		return resultResponseType;
	}
	
	/**
	 * 
	 * 
	 * @throws I2B2DAOException 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 */
	public CrcXmlResultResponseType getXmlResultFromResultInstanceId(DataSourceLookup dataSourceLookup,String  queryResultInstanceId) throws I2B2DAOException  { 
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(dataSourceLookup.getDomainId(), dataSourceLookup.getProjectPath(), dataSourceLookup.getOwnerId());
		IXmlResultDao xmlResultDao = sfDaoFactory.getXmlResultDao();
		QtXmlResult xmlResult = xmlResultDao.getXmlResultByResultInstanceId(queryResultInstanceId);
		CrcXmlResultResponseType resultResponseType = new CrcXmlResultResponseType();

		IQueryResultInstanceDao queryResultInstanceDao = sfDaoFactory.getPatientSetResultDAO(); 
		QtQueryResultInstance resultInstance = queryResultInstanceDao.getResultInstanceById(queryResultInstanceId);
		QueryResultInstanceType resultInstanceType = new QueryResultInstanceType();
		if (resultInstance != null) { 
			resultInstanceType = PSMFactory.buildQueryResultInstanceType(resultInstance);
		}
		resultResponseType.setQueryResultInstance(resultInstanceType);
		if (xmlResult !=null) {
			XmlResultType xmlResultType = new XmlResultType();
			xmlResultType.setXmlResultId(xmlResult.getXmlResultId());
			
			String xmlValue = xmlResult.getXmlValue();
			if (xmlValue != null) {
				XmlValueType xmlValueType = new XmlValueType();
				xmlValueType.getContent().add(xmlValue);
				xmlResultType.setXmlValue(xmlValueType);
			}
			
			xmlResultType.setResultInstanceId(xmlResult.getQtQueryResultInstance().getResultInstanceId());	
			resultResponseType.setCrcXmlResult(xmlResultType);
		}
		return resultResponseType;
	}
	
	private SetFinderDAOFactory getSetFinderDaoFactory(String domainId,String projectPath,String ownerId) throws I2B2DAOException { 
	   	 DAOFactoryHelper helper = new DAOFactoryHelper(domainId,projectPath,ownerId); 
	        SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory().getSetFinderDAOFactory();
	        return sfDaoFactory;
	   }
	
	
}
