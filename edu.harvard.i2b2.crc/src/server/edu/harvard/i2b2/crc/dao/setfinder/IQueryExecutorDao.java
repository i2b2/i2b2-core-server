/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;


import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;

public interface IQueryExecutorDao {
	public String executeSQL(
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList, boolean allowLargeTextValueConstrainFlag, boolean allowProtectedQueryFlag, String pmXMl)
			throws CRCTimeOutException, I2B2DAOException, I2B2Exception, JAXBUtilException;
}
