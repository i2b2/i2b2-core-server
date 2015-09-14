package edu.harvard.i2b2.ontology.ws;

import static org.junit.Assert.*;


import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;

public class OntologyServiceRESTTest extends OntologyAxisAbstract{
	private static String testFileDir = "";

	private static String ontologyTargetEPR = 
			"http://localhost:9090/i2b2/services/OntologyService/getSchemes";			
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(OntologyServiceRESTTest.class);
	}


	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "test"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}

	@Test
	public void GetSchemes() throws Exception {
		String filename = testFileDir + "/schemes.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(ontologyTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			ConceptsType folders = (ConceptsType)helper.getObjectByClass(r.getMessageBody().getAny(),ConceptsType.class);
			assertNotNull(folders);
			assertTrue(folders.getConcept().size() > 5);



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	@Test
	public void GetCategories() throws Exception {
		String filename = testFileDir + "/categories.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(ontologyTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			ConceptsType folders = (ConceptsType)helper.getObjectByClass(r.getMessageBody().getAny(),ConceptsType.class);
			assertNotNull(folders);
			assertTrue(folders.getConcept().size() > 10);



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

}





