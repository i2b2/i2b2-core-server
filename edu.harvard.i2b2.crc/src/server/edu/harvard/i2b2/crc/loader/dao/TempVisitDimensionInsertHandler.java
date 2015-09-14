package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.crc.datavo.pdo.EventType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class TempVisitDimensionInsertHandler {
	private VisitDAO.TempEncounterVisitInsert tempEncounterVisitInsert = null;

	public TempVisitDimensionInsertHandler(
			VisitDAO.TempEncounterVisitInsert tempEncounterVisitInsert) {
		this.tempEncounterVisitInsert = tempEncounterVisitInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertVisitDimension(EventType event) {
		tempEncounterVisitInsert.insert(event);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		tempEncounterVisitInsert.flush();
		tempEncounterVisitInsert.reset();
	}

}
