/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo.input;

import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.EidFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PidFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;

public class OutputOptionFactRelatedHelper {

	private VisitFactRelated visitFactRelated = null;
	/** Observer helper class to build Observer section in pdo **/
	private ProviderFactRelated providerFactRelated = null;
	/** Patient helper class to build patient section in pdo **/
	private PatientFactRelated patientFactRelated = null;
	/** Concept helper class to build concept section in pdo **/
	private ConceptFactRelated conceptFactRelated = null;
	/** Observation fact helper class to build observationfact **/
	private ObservationFactFactRelated obsFactFactRelated = null;
	private PidFactRelated pidFactRelated = null;
	private EidFactRelated eidFactRelated = null;

	public OutputOptionFactRelatedHelper(OutputOptionListType outputOptionList) {
		visitFactRelated = new VisitFactRelated(outputOptionList.getEventSet());
		providerFactRelated = new ProviderFactRelated(outputOptionList
				.getObserverSetUsingFilterList());
		patientFactRelated = new PatientFactRelated(outputOptionList
				.getPatientSet());
		conceptFactRelated = new ConceptFactRelated(outputOptionList
				.getConceptSetUsingFilterList());
		obsFactFactRelated = new ObservationFactFactRelated(outputOptionList
				.getObservationSet());
		pidFactRelated = new PidFactRelated(outputOptionList.getPidSet());
		eidFactRelated = new EidFactRelated(outputOptionList.getEidSet());
	}

	public boolean isFactRelated() {
		// check if obsrvation_fact tag present
		boolean obsFactSelected = obsFactFactRelated.isSelected();

		// check if provider or concept present
		boolean providerSelected = providerFactRelated.isSelected();
		boolean conceptSelected = conceptFactRelated.isSelected();

		boolean patientFromFact = patientFactRelated.isFactRelated();
		boolean visitFromFact = visitFactRelated.isFactRelated();
		boolean pidFromFact = pidFactRelated.isFactRelated();
		boolean eidFromFact = eidFactRelated.isFactRelated();

		if (obsFactSelected || providerSelected || conceptSelected
				|| patientFromFact || visitFromFact || pidFromFact
				|| eidFromFact) {
			return true;
		} else {
			return false;
		}

	}
}
