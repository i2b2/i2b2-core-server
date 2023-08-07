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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.util.List;


import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;

/**
 * Ejb manager class for query operation
 * 
 * @author rkuttan
 * 
 * @ejb.bean description="QueryTool Query Run"
 *           display-name="QueryTool Query Run"
 *           jndi-name="ejb.querytool.QueryRun"
 *           local-jndi-name="ejb.querytool.QueryRunLocal"
 *           name="querytool.QueryRun" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryRunRemote"
 * 
 * 
 */
public class QueryRunBean  { //implements SessionBean {
	// RunQuery

	/**
	 * 
	 * 
	 * @throws I2B2DAOException
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * 
	 */
	public InstanceResponseType getQueryInstanceFromMasterId(
			DataSourceLookup dataSourceLookup, String userId,
			MasterRequestType masterRequestType) throws I2B2DAOException {
		String queryMasterId = masterRequestType.getQueryMasterId();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());

		IQueryInstanceDao queryInstanceDao = sfDaoFactory.getQueryInstanceDAO();
		List<QtQueryInstance> queryInstanceList = queryInstanceDao
				.getQueryInstanceByMasterId(queryMasterId);
		InstanceResponseType instanceResponseType = new InstanceResponseType();

		QueryInstanceType qiType = null;
		for (QtQueryInstance queryInstance : queryInstanceList) {
			qiType = PSMFactory.buildQueryInstanceType(queryInstance);
			instanceResponseType.getQueryInstance().add(qiType);
		}
		return instanceResponseType;
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
