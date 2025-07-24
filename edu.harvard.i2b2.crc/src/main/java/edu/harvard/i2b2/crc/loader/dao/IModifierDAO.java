/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface IModifierDAO {

	/**
	 * Function to create temp modifier dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempModifierMappingTableName)
			throws I2B2Exception;

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempModifierInsertHandler createTempModifierInsert(String tempTableName);

	/**
	 * Function to create new encounter/visit from temp_modifier_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createModifierFromTempTable(String tempMapTableName, int uploadId)
			throws I2B2Exception;

	public int getRecordCountByUploadId(int uploadId);

	/**
	 * Function to backup and clear concept dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void backupAndSyncModifierDimensionTable(String tempModifierTableName,
			String backupModifierDimensionTableName, int uploadId)
			throws I2B2Exception;

}
