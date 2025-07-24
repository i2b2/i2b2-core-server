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
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;

public interface IQueryInstanceDao {

	/**
	 * Function to create query instance
	 * 
	 * @param queryMasterId
	 * @param userId
	 * @param groupId
	 * @param batchMode
	 * @param statusId
	 * @return query instance id
	 */
	public String createQueryInstance(String queryMasterId, String userId,
			String groupId, String batchMode, int statusId);

	/**
	 * Returns list of query instance for the given master id
	 * 
	 * @param queryMasterId
	 * @return List<QtQueryInstance>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryInstance> getQueryInstanceByMasterId(String queryMasterId);

	/**
	 * Find query instance by id
	 * 
	 * @param queryInstanceId
	 * @return QtQueryInstance
	 */
	public QtQueryInstance getQueryInstanceByInstanceId(String queryInstanceId);

	/**
	 * Update query instance
	 * 
	 * @param queryInstance
	 * @return QtQueryInstance
	 */
	public QtQueryInstance update(QtQueryInstance queryInstance,
			boolean appendMessageFlag) throws I2B2DAOException;
	

	/**
	 * Update query instance message
	 * 
	 * @param queryInstanceId
	 * @param message
	 * @param appendMessageFlag
	 * @return 
	 */
	public void updateMessage(String  queryInstanceId, String message,
			boolean appendMessageFlag) throws I2B2DAOException ;

}
