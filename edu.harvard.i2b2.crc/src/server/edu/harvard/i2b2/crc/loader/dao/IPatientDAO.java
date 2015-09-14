package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface IPatientDAO {

	public int getRecordCountByUploadId(int uploadId);

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempPatientTableName,
			String tempPatientMappingTableName) throws I2B2Exception;

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempPatientDimensionInsertHandler createTempPatientDimensionInsert(
			String tempTableName);

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createPatientFromTempTable(String tempTableName,
			String tempMapTableName, int uploadId) throws I2B2Exception;

}