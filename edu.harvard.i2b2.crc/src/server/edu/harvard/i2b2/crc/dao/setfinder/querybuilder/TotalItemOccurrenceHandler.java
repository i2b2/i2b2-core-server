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
