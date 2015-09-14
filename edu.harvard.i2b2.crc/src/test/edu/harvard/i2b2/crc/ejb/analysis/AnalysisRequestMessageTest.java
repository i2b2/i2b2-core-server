package edu.harvard.i2b2.crc.ejb.analysis;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisResultOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisResultOptionType;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;

public class AnalysisRequestMessageTest {
	static AnalysisDefinitionType analysisDef = null;
	String xml = null;

	@BeforeClass
	public static void init() {
		analysisDef = new AnalysisDefinitionType();
		analysisDef.setAnalysisPluginName("PLUGIN_ONE");
		AnalysisResultOptionListType resultOutputList = new AnalysisResultOptionListType();
		AnalysisResultOptionType resultOptionType = new AnalysisResultOptionType();
		resultOutputList.getResultOutput().add(resultOptionType);
		analysisDef.setCrcAnalysisResultList(resultOutputList);
	}

	@Test
	public void testMarshalling() throws JAXBUtilException {
		xml = I2B2RequestMessageHelper
				.getAnalysisDefinitionXml(analysisDef);
		System.out.println("XML " + xml);

	}

	@Test
	public void testUnMarshalling() throws JAXBUtilException {
		xml = I2B2RequestMessageHelper
				.getAnalysisDefinitionXml(analysisDef);
		I2B2RequestMessageHelper.getAnalysisDefinitionFromXml(xml);
	}

}
