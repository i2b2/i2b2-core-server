/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.common.util.xml.XMLOperatorLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.TotOccuranceOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;

public class TotalItemOccurrenceHandler {

	public String buildTotalItemOccurrenceClause(
			TotalItemOccurrences totalItemOccurances) {
		String totalItemOccuranceStr = null;

		int totalItemOccurrenceValue = 1;
		String totalItemOccurrenceOperator = ">=";
		if (totalItemOccurances != null) {
			totalItemOccurrenceValue = totalItemOccurances.getValue();
			TotOccuranceOperatorType totalOccuranceOperator = totalItemOccurances
					.getOperator();
			if (totalOccuranceOperator == null) {
				totalItemOccurrenceOperator = ">=";
			} else {
				totalItemOccurrenceOperator = XMLOperatorLookup
						.getComparisonOperatorFromAcronum(totalOccuranceOperator
								.toString());
			}
		}
		totalItemOccuranceStr = totalItemOccurrenceOperator + " "
				+ totalItemOccurrenceValue;
		return totalItemOccuranceStr;

	}

}
