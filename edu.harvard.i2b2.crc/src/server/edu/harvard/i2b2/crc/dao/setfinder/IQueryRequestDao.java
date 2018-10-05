/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

public interface IQueryRequestDao {

	/**
	 * Function to execute the given setfinder sql And creates query instance
	 * and query result instance
	 * 
	 * @param generatedSql
	 * @param queryInstanceId
	 * @return query result instance id
	 * @throws I2B2DAOException
	 */
	// public String getPatientCount(String generatedSql, String
	// queryInstanceId,
	// String patientSetId) throws I2B2DAOException;
	/**
	 * Function to build sql from given query definition This function uses
	 * QueryToolUtil class to build sql
	 * 
	 * @param queryRequestXml
	 * @return sql string
	 * @throws I2B2DAOException
	 * @throws I2B2Exception 
	 * @throws JAXBUtilException 
	 */
	public String[] buildSql(String queryRequestXml, boolean encounterSetFlag)
			throws I2B2DAOException, I2B2Exception, JAXBUtilException;

	
	/**
	 * set the project param flag
	 * @param Map projectParamMap
	 */
	public void setProjectParam(Map projectParamMap) ; 
	
	
	/**
	 * set the large text value constrain
	 * @param allowLargeTextValueConstrainFlag
	 */
	public void setAllowLargeTextValueConstrainFlag(boolean allowLargeTextValueConstrainFlag) ;
		
	public void setAllowProtectedQueryFlag(boolean allowProtectedQueryFlag);
	/**
	 * set query with temp table optimization
	 * @param allowLargeTextValueConstrainFlag
	 */
	public void setQueryWithoutTempTableFlag(boolean queryWithoutTempTableFlag) ;
		
	
}
