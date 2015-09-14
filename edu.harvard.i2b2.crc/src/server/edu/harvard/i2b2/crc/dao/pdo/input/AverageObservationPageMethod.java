package edu.harvard.i2b2.crc.dao.pdo.input;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AverageObservationPageMethod implements PageMethod {

	/** log * */
	protected final Log log = LogFactory
			.getLog(AverageObservationPageMethod.class);

	public AverageObservationPageMethod() {
	}

	public int calculateListSize(int maxInputList, long totalObservations,
			long pageSize) {

		// calculateObservationPerPatient
		long observationsPerPatient = calculateObservationPerPatient(
				totalObservations, maxInputList);
		log.debug("Total observation per patient is [" + observationsPerPatient
				+ "]");
		if (observationsPerPatient == 0) {
			return 0;
		}
		maxInputList = (int) (pageSize / observationsPerPatient);
		return maxInputList;
	}

	private long calculateObservationPerPatient(long totalObservations,
			int inputListLength) {
		long observationPerPatient = totalObservations / inputListLength;
		return observationPerPatient;
	}
}
