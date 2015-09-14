package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.crc.datavo.pdo.EidType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class TempEidInsertHandler extends JdbcDaoSupport {
	private EidDAO.TempEidInsert tempEidInsert = null;

	public TempEidInsertHandler(DataSource ds) {
		setDataSource(ds);
	}

	public TempEidInsertHandler(EidDAO.TempEidInsert tempEidInsert) {
		this.tempEidInsert = tempEidInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertEid(EidType eid) {
		tempEidInsert.insert(eid);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		tempEidInsert.flush();
		tempEidInsert.reset();
	}

}
