package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class ObservationFactInsertHandle {
	private ObservationFactDAO.ObservationFactInsert observationFactInsert = null;

	public ObservationFactInsertHandle(
			ObservationFactDAO.ObservationFactInsert observationFactInsert) {
		this.observationFactInsert = observationFactInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertObservationFact(ObservationType observation) {
		observationFactInsert.insert(observation);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		observationFactInsert.flush();
		observationFactInsert.reset();
	}

}
