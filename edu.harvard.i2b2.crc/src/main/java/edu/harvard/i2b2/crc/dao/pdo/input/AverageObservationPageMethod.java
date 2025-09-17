/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo.input;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AverageObservationPageMethod implements PageMethod {

	/** log * */
	protected final Log log = LogFactory
			.getLog(AverageObservationPageMethod.class);

	public AverageObservationPageMethod() {
	}

	@Override
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
		maxInputList = (int) ((pageSize *0.9) / observationsPerPatient);
		return maxInputList;
	}

	private long calculateObservationPerPatient(long totalObservations,
			int inputListLength) {
		long observationPerPatient = totalObservations / inputListLength;
		return observationPerPatient;
	}
}
