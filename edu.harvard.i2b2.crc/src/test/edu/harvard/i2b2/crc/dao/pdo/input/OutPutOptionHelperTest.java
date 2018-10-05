/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo.input;

import org.junit.Test;

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionSelectType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;

public class OutPutOptionHelperTest {

	@Test
	public void testA() {
		OutputOptionListType opOptionList = new OutputOptionListType();
		OutputOptionType ooType = new OutputOptionType();

		ooType.setSelect(OutputOptionSelectType.USING_INPUT_LIST);
		opOptionList.setPatientSet(ooType);
		OutputOptionFactRelatedHelper OutputOptionFactRelatedHelper = new OutputOptionFactRelatedHelper(
				opOptionList);
		System.out.println(" " + OutputOptionFactRelatedHelper.isFactRelated());

	}
}
