package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface IVisitDAO {

	public int getRecordCountByUploadId(int uploadId);

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public TempVisitDimensionInsertHandler createTempVisitDimensionInsert(
			String tempTableName);

	/**
	 * Function to create temp visit dimension table using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempTableName) throws I2B2Exception;

	/**
	 * Function to create new encounter/visit from temp_visit_dimension table
	 * using stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createVisitFromTempTable(String tempTableName, int uploadId)
			throws I2B2Exception;

}