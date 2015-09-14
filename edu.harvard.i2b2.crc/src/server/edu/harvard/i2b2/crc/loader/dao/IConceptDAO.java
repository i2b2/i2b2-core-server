package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface IConceptDAO {

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempPatientMappingTableName)
			throws I2B2Exception;

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempConceptInsertHandler createTempConceptInsert(String tempTableName);

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createConceptFromTempTable(String tempMapTableName, int uploadId)
			throws I2B2Exception;

	public int getRecordCountByUploadId(int uploadId);

	/**
	 * Function to backup and clear concept dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void backupAndSyncConceptDimensionTable(String tempConceptTableName,
			String backupConceptDimensionTableName, int uploadId)
			throws I2B2Exception;

}