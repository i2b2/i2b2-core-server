package edu.harvard.i2b2.crc.datavo.setfinder.query;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.axis2.PdoQueryTest;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;

public class AnalysisDefinitionTypeTest {

	private static String testFileDir = null;

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
	}

	@Test
	public void analysisRequestMarshallTest() throws JAXBUtilException {

		AnalysisDefinitionType analysisDefType = new AnalysisDefinitionType();
		analysisDefType.setAnalysisPluginName("analysis_name");

		AnalysisDefinitionRequestType analysisDef = new AnalysisDefinitionRequestType();
		analysisDef.setAnalysisDefinition(analysisDefType);

		BodyType bodyType = new BodyType();

		ObjectFactory of = new ObjectFactory();

		bodyType.getAny().add(of.createRequest(analysisDef));
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of1 = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();

		RequestMessageType requestMsgType = new RequestMessageType();
		requestMsgType.setMessageBody(bodyType);
		StringWriter strWriter = new StringWriter();
		CRCJAXBUtil.getJAXBUtil().marshaller(of1.createRequest(requestMsgType),
				strWriter);
		System.out.println("request str :" + strWriter.toString());

	}

	@Test
	public void analysisRequestUnMarshallTest() throws Exception {
		String filename = testFileDir + "/setfinder_analysis_query.xml";
		// read file as string
		String requestString = PdoQueryTest.getQueryString(filename);
		// unmarshall
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(requestString);
		RequestMessageType r = (RequestMessageType) responseJaxb.getValue();
		BodyType bodyType = r.getMessageBody();
		// get body and search for analysis definition
		JAXBUnWrapHelper unWraphHelper = new JAXBUnWrapHelper();
		AnalysisDefinitionRequestType analysisDefReqType = (AnalysisDefinitionRequestType) unWraphHelper
				.getObjectByClass(bodyType.getAny(),
						AnalysisDefinitionRequestType.class);
		System.out.println("analysis name "
				+ analysisDefReqType.getAnalysisDefinition()
						.getAnalysisPluginName());

	}
}
