package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

public interface IQueryStatusTypeDao {

	/**
	 * Returns list of query master by user id
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public QtQueryStatusType getQueryStatusTypeById(int statusTypeId);

}