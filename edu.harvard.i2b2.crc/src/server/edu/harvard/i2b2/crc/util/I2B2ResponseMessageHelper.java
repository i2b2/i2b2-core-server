/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.ejb.QueryResultBean;

public class I2B2ResponseMessageHelper {

	private SetFinderDAOFactory sfDAOFactory = null;

	public I2B2ResponseMessageHelper(SetFinderDAOFactory sfDAOFactory) {
		this.sfDAOFactory = sfDAOFactory;
	}

	public MasterInstanceResultResponseType buildResponse(String queryMasterId,
			String queryInstanceId, String userId, StatusType statusType)
			throws I2B2DAOException {
		MasterInstanceResultResponseType masterInstanceResultType = new MasterInstanceResultResponseType();

		if (queryMasterId != null && queryInstanceId != null) {
			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			QtQueryMaster queryMaster = queryMasterDao
					.getQueryDefinition(queryMasterId);

			QueryMasterType queryMasterType = PSMFactory
					.buildQueryMasterType(queryMaster);
			// set query master
			masterInstanceResultType.setQueryMaster(queryMasterType);
			// fetch query instance by queryinstance id and build response
			IQueryInstanceDao queryInstanceDao = sfDAOFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			QueryInstanceType queryInstanceType = PSMFactory
					.buildQueryInstanceType(queryInstance);
			// set query instance
			masterInstanceResultType.setQueryInstance(queryInstanceType);

			QueryResultBean queryResultBean = new QueryResultBean();
			ResultResponseType responseType1 = queryResultBean
					.getResultInstanceFromQueryInstanceId(sfDAOFactory
							.getDataSourceLookup(), userId, queryInstanceId, false);

			// set result instance
			masterInstanceResultType.getQueryResultInstance().addAll(
					responseType1.getQueryResultInstance());

		}

		// set status
		masterInstanceResultType.setStatus(statusType);

		return masterInstanceResultType;
	}
}
