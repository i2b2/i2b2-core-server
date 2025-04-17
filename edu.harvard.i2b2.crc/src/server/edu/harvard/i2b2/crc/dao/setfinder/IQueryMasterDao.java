/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;

public interface IQueryMasterDao {

	/**
	 * Function to create query master By default sets delete flag to false
	 * 
	 * @param queryMaster
	 * @return query master id
	 */
	public String createQueryMaster(QtQueryMaster queryMaster,
			String i2b2RequestXml, String pmXml);

	/**
	 * Write query sql for the master id
	 * 
	 * @param masterId
	 * @param generatedSql
	 */
	public void updateQueryAfterRun(String masterId, String generatedSql, String masterType);

	
	/**
	 * Write query sql for the master id
	 * 
	 * @param masterId
	 */
	public void updateMasterTypeAfterRun(String masterId,  String masterType);

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByUserId(String userId, String groupId,
			int fetchSize, String masterTypeCd, boolean includeQueryInstance);

	/**
	 * Returns list of query master by find search
	 * 
	 * @param groupId
	 * @return List<QtQueryMaster>
	 * @throws I2B2Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByNameInfo(SecurityType userRequestType, FindByChildType find) throws I2B2Exception;
	
	/**
	 * Returns list of query master by group id
	 * 
	 * @param groupId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByGroupId(String groupId,
			int fetchSize, String masterTypeCd, boolean includeQueryInstance);

	/**
	 * Find Query master by id
	 * 
	 * @param masterId
	 * @return QtQueryMaster
	 */
	public QtQueryMaster getQueryDefinition(String masterId);
	
	/**
	 * Find query by name
	 * @param queryName
	 * @return
	 */
	public List<QtQueryMaster> getQueryByName(String queryName);

	/**
	 * Function to rename query master
	 * 
	 * @param masterId
	 * @param queryNewName
	 * @throws I2B2DAOException
	 */
	public void renameQuery(String masterId, String queryNewName)
			throws I2B2DAOException;

	/**
	 * Function to delete query using user and master id This function will not
	 * delete permanently, it will set delete flag field in query master, query
	 * instance and result instance to true
	 * 
	 * @param masterId
	 * @throws I2B2DAOException
	 */
	@SuppressWarnings("unchecked")
	public void deleteQuery(String masterId) throws I2B2DAOException;

}
