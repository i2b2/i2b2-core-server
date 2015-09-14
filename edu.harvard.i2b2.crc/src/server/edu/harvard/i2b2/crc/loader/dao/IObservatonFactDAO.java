package edu.harvard.i2b2.crc.loader.dao;

import java.io.BufferedWriter;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface IObservatonFactDAO {

	public int getRecordCountByUploadId(int uploadId);

	/**
	 * Create batch insert handle for temp observation fact table.
	 * 
	 * @param tempTableName
	 * @return
	 */
	public ObservationFactInsertHandle createObservationFactInserter(
			String tempTableName);

	/**
	 * Function to check if given table exists
	 * 
	 * @param tableName
	 * @return boolean
	 * @throws Exception
	 */
	public boolean checkTableExists(String tableName) throws I2B2Exception;

	/**
	 * Function to call MERGE_TEMP_OBSERVATION_FACT(?) stored procedure.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void doTempTableMerge(String tempTableName, int uploadId,
			boolean appendFlag) throws I2B2Exception;

	/**
	 * Function to call remove temp table stored procedure.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void removeTempTable(String tempTableName) throws I2B2Exception;

	/**
	 * Function to create create temp table stored proc.
	 * 
	 * @param tempTableName
	 * @throws Exception
	 */
	public void createTempTable(String tempTableName) throws I2B2Exception;

	public void writeMissedDataLog(BufferedWriter bufWriter,
			String tempTableName) throws I2B2Exception;

}