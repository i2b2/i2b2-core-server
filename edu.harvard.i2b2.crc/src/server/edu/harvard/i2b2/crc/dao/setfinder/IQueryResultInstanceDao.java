package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;

public interface IQueryResultInstanceDao {

	public final String OBTOTAL = "OBTOTAL";
	public final String OBSUBTOTAL = "OBSUBTOTAL";

	/**
	 * Function to create result instance for given query instance id. The
	 * result instance status is set to running. Use updatePatientSet function
	 * to change the status to completed or error
	 * 
	 * @param queryInstanceId
	 * @return
	 */
	public String createPatientSet(String queryInstanceId, String resultName)
			throws I2B2DAOException;

	/**
	 * Function used to update result instance Particularly its status and size
	 * 
	 * @param resultInstanceId
	 * @param statusTypeId
	 * @param setSize
	 */
	public void updatePatientSet(String resultInstanceId, int statusTypeId,
			int setSize);

	/**
	 * Function used to update result instance Particularly its status and size
	 * 
	 * @param resultInstanceId
	 * @param statusTypeId
	 * @param setSize
	 */
	public void updatePatientSet(String resultInstanceId, int statusTypeId,
			String message, int setSize, int obsSetSize, String obsSizeType);

	/**
	 * Function used to update result instance description
	 * 
	 * @param resultInstanceId
	 * @param description
	 */
	public void updateResultInstanceDescription(String resultInstanceId,
			String description);

	/**
	 * Return list of query result instance by query instance id
	 * 
	 * @param queryInstanceId
	 * @return List<QtQueryResultInstance>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryResultInstance> getResultInstanceList(
			String queryInstanceId);

	/**
	 * Return list of query result instance by query result id
	 * 
	 * @param queryResultId
	 * @return QtQueryResultInstance
	 */
	public QtQueryResultInstance getResultInstanceById(String queryResultId)
			throws I2B2DAOException;

	/**
	 * Return list of query result instance by query instance id and result name
	 * 
	 * @param queryInstanceId
	 * @param resultName
	 * @return QtQueryResultInstance
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultInstance getResultInstanceByQueryInstanceIdAndName(
			String queryInstanceId, String resultName);

	/**
	 * Return a list of query result instance with waiting status
	 * 
	 * @param queueName
	 * @param maxListSize
	 * @return
	 */
	public List<QtQueryResultInstance> getUnfinishedInstanceByQueue(
			String queueName, int maxListSize);

	/**
	 * Get result instance count by set size
	 * 
	 * @param userId
	 * @param compareDays
	 * @param setSize
	 * @param totalCount
	 * @return
	 * @throws I2B2DAOException
	 */
	public int getResultInstanceCountBySetSize(String userId, int compareDays,
			int resultTypeId, int setSize, int totalCount)
			throws I2B2DAOException;

}