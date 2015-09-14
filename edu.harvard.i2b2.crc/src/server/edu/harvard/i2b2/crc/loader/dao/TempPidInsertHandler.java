package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.crc.datavo.pdo.PidType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class TempPidInsertHandler extends JdbcDaoSupport {
	private PidDAO.TempPidInsert tempPidInsert = null;

	public TempPidInsertHandler(DataSource ds) {
		setDataSource(ds);
	}

	public TempPidInsertHandler(PidDAO.TempPidInsert tempPidInsert) {
		this.tempPidInsert = tempPidInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertPid(PidType pid) {
		tempPidInsert.insert(pid);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		tempPidInsert.flush();
		tempPidInsert.reset();
	}

}
