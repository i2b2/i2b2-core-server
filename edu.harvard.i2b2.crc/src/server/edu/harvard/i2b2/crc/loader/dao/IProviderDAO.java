package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface IProviderDAO {

	public int getRecordCountByUploadId(int uploadId);

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempProviderTableName)
			throws I2B2Exception;

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempProviderInsertHandler createTempProviderInsert(
			String tempTableName);

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createProviderFromTempTable(String tempProviderTableName,
			int uploadId) throws I2B2Exception;

	/**
	 * Function to backup and clear provider dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void backupAndSyncProviderDimensionTable(
			String tempConceptTableName,
			String backupProviderDimensionTableName, int uploadId)
			throws I2B2Exception;

}