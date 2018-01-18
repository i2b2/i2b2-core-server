package edu.harvard.i2b2.pm.ws;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.pm.datavo.i2b2message.*;
import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDataType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ParamsType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;
import edu.harvard.i2b2.pm.util.PMJAXBUtil;

public class PMServiceRESTSecurityTest extends PMAxisAbstract{
	private static String testFileDir = "";

	private static String pmTargetEPR = 
			"http://127.0.0.1:9090/i2b2/services/PMService/getServices";			
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(PMServiceRESTSecurityTest.class);
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
	public void SleepForBamboo() throws Exception {
		String filename = testFileDir + "/pm_create_user_for_crc.xml";
		ConfigureType ctype = null;
		String masterInstanceResult = null;
		try { 

			 Thread.sleep(60000);

			assertTrue(true);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	
	@Test
	public void GlobalParamsSetExpiredPassword() throws Exception {
		String filename = testFileDir + "/pm_set_global_expired_password.xml";
		//ParamType ctype = null;
		int id = -1;
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void setInvalidPassword() throws Exception {
		String filename = testFileDir + "/pm_setpassword_bad.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void setValidPassword() throws Exception {
		String filename = testFileDir + "/pm_setpassword_good.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			String masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void GlobalParamsUnSetExpiredPassword() throws Exception {
		
		String filename = testFileDir + "/pm_create_global_param_with_valid_user_check.xml";
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		ParamsType allParams = (ParamsType)helper.getObjectByClass(r.getMessageBody().getAny(),ParamsType.class);
		int id = -1;
		for (ParamType param : allParams.getParam())
		{
			if (param.getName().equals("PM_EXPIRED_PASSWORD"))
			{
				assertNotNull(param);
				assertEquals(param.getDatatype(),"N");
				id = param.getId();
				break;
			}
		}

		//Delete Param
		filename = testFileDir + "/pm_delete_global_param_with_valid_user.xml";
		requestString = getQueryString(filename);
		requestString = requestString.replace("{{{id}}}", Integer.toString(id));
		requestElement = convertStringToOMElement(requestString); 
		responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		r = (ResponseMessageType)responseJaxb.getValue();
		helper = new  JAXBUnWrapHelper();
		String masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
		assertNotNull(masterInstanceResult);

		//Check to see if really deleted
		filename = testFileDir + "/pm_recreate_global_param_with_valid_user_check.xml";
		requestString = getQueryString(filename);
		requestString = requestString.replace("{{{id}}}", Integer.toString(id));			
		requestElement = convertStringToOMElement(requestString); 
		responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		r = (ResponseMessageType)responseJaxb.getValue();
		String err = r.getResponseHeader().getResultStatus().getStatus().getType();
		assertEquals("ERROR", err);			

		
	}

}





