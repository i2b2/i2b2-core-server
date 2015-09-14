package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;

public interface IQueryPdoMasterDao {

	/**
	 * Function to create query master By default sets delete flag to false
	 * 
	 * @param queryMaster
	 * @return query master id
	 */
	public String createPdoQueryMaster(QtQueryMaster queryMaster,
			String i2b2RequestXml);

}