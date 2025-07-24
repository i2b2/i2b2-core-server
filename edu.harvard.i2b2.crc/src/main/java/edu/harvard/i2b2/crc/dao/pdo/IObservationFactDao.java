/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo;

import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.pdo.query.FactPrimaryKeyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;

public interface IObservationFactDao {

	/**
	 * Function returns Observation fact from the primary key.
	 * <p>Required fields : <b>patient_num, concept_cd, encounter_num</b>
	 * <p>Optional field  : <b>provider_id,start_date</b>
	 * @param factPrimaryKey
	 * @param factOutputOption
	 * @return PatientDataType
	 * @throws I2B2Exception
	 */
	public PatientDataType getObservationFactByPrimaryKey(
			FactPrimaryKeyType factPrimaryKey, OutputOptionType factOutputOption)
			throws I2B2DAOException;

}
