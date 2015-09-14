package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.crc.datavo.pdo.PatientType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class TempPatientDimensionInsertHandler extends JdbcDaoSupport {
	private PatientDAO.TempPatientInsert tempPatientInsert = null;

	public TempPatientDimensionInsertHandler(DataSource ds) {
		setDataSource(ds);
	}

	public TempPatientDimensionInsertHandler(
			PatientDAO.TempPatientInsert tempPatientInsert) {
		this.tempPatientInsert = tempPatientInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertPatientDimension(PatientType patient) {
		tempPatientInsert.insert(patient);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		tempPatientInsert.flush();
		tempPatientInsert.reset();
	}

}
