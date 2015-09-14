package edu.harvard.i2b2.im.ws;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.im.datavo.pdo.ObservationType;
import edu.harvard.i2b2.im.datavo.pdo.PidType;
import edu.harvard.i2b2.im.datavo.pdo.PidType.PatientMapId;
import edu.harvard.i2b2.im.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.wdo.AuditType;
import edu.harvard.i2b2.im.datavo.wdo.AuditsType;
import edu.harvard.i2b2.im.datavo.wdo.IsKeySetType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;
import edu.harvard.i2b2.im.util.IMJAXBUtil;

public class IMServiceRESTTest extends IMAxisAbstract{
	private static String testFileDir = "";

	private static String imTargetEPR = 
			"http://127.0.0.1:9090/i2b2/services/IMService/";			

	private static String isKeySet = "isKeySet";
	private static String pdorequest = "pdorequest";
	private static String setKey = "setKey";
	private static String validateSiteId = "validateSiteId";
	private static String getAudit = "getAudit";

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(IMServiceRESTTest.class);
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
	public void SetKeyValidUser() throws Exception {
		String filename = testFileDir + "/set_key_valid_user.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + setKey).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	@Test
	public void pdo_minvalue() throws Exception {
		String filename = testFileDir + "/get_pdo.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + pdorequest).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

/*MM TODO
			PatientDataResponseType patientDataResponseType = 
					(PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);

			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPidSet().getPid().size()>0);
*/
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}




	@Test
	public void SetKeyValidMD5() throws Exception {
		String filename = testFileDir + "/set_key_valid_md5.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + setKey).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void SetKeyInValidMD5() throws Exception {
		String filename = testFileDir + "/set_key_invalid_md5.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + setKey).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	
	@Test
	public void SetKeyInValidUser() throws Exception {
		String filename = testFileDir + "/set_key_invalid_user.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + setKey).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void IsSetKeyInValidUser() throws Exception {
		String filename = testFileDir + "/iskey_set_invalid_user.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + isKeySet).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void IsKeySetValidUser() throws Exception {
		String filename = testFileDir + "/iskey_set_valid_user.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + isKeySet).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			
			IsKeySetType masterInstanceResult = (IsKeySetType)helper.getObjectByClass(r.getMessageBody().getAny(),IsKeySetType.class);

			assertNotNull(masterInstanceResult);
			assertTrue(masterInstanceResult.isActive());
			
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void IsKeySetNonProject() throws Exception {
		String filename = testFileDir + "/iskey_set_non_project.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + isKeySet).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);
			
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	@Test
	public void UnSetKeyValidUser() throws Exception {
		String filename = testFileDir + "/unset_key_valid_user.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + setKey).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);

			
			
			 filename = testFileDir + "/iskey_set_valid_user.xml";
				 requestString = getQueryString(filename);
				 requestElement = convertStringToOMElement(requestString); 
				 responseElement = getServiceClient(imTargetEPR + isKeySet).sendReceive(requestElement);
				 responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
				 r = (ResponseMessageType)responseJaxb.getValue();
				 helper = new  JAXBUnWrapHelper();
				
				IsKeySetType key = (IsKeySetType)helper.getObjectByClass(r.getMessageBody().getAny(),IsKeySetType.class);

				assertNotNull(key);
				assertFalse(key.isActive());
		
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void ReSetKeyValidUser() throws Exception {
		String filename = testFileDir + "/set_key_valid_user.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + setKey).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void validatesiteid() throws Exception {
		String filename = testFileDir + "/validate_site.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + validateSiteId).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			
			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size = 2 ",patientDataResponseType.getPatientData().getPidSet().getPid().get(0).getPatientMapId().size()==2);
			//System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			
			boolean found = false;
			for (PatientMapId results : patientDataResponseType.getPatientData().getPidSet().getPid().get(0).getPatientMapId())
			{
				if (results.getValue().equals("2000002062"))
				{
					found = true;
				}
			}
			assertTrue(found);
			 found = false;
				for (PatientMapId results : patientDataResponseType.getPatientData().getPidSet().getPid().get(0).getPatientMapId())
			{
				if (results.getValue().equals("2000002017"))
				{
					found = true;
				}
			}
			assertTrue(found);
			
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
/*
	@Test
	public void ValidteThanGetAudit() throws Exception {
		String filename = testFileDir + "/get_audit.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + getAudit).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			AuditsType auditsType = 
					(AuditsType)helper.getObjectByClass(r.getMessageBody().getAny(),AuditsType.class);

//	
			
			System.out.println(r.toString());
			boolean found = false;
			for (AuditType results : auditsType.getAudit())
			{
				if (results.getPid().equals("2000002062"))
				{
					found = true;
					break;
				}
			}
			assertTrue("Searching for 2000002062", found);
			 found = false;
				for (AuditType results : auditsType.getAudit())
			{
				if (results.getPid().equals("2000002017"))
				{
					found = true;
					break;
				}
			}
			assertTrue("Searching for 2000002017", found);
			
			
//			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPidSet().getPid().size()>0);

			
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
*/
	public static String getQueryString(String filename) throws Exception {


		//StringBuffer queryStr = new StringBuffer();
		String queryStr = "";
		DataInputStream dataStream = new DataInputStream(new FileInputStream(
				filename));
		while (dataStream.available() > 0) {
			queryStr += (dataStream.readLine() + "\n");
		}
		return queryStr;
	}	

}





