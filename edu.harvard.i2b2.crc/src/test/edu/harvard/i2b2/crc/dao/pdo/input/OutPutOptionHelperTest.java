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
