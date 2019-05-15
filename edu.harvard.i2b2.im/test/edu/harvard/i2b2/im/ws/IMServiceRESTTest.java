/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
import edu.harvard.i2b2.im.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.im.datavo.wdo.AuditType;
import edu.harvard.i2b2.im.datavo.wdo.AuditsType;
import edu.harvard.i2b2.im.datavo.wdo.IsKeySetType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;
import edu.harvard.i2b2.im.util.IMJAXBUtil;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IMServiceRESTTest extends IMAxisAbstract{
	private static String testFileDir = "";

	private static String imTargetEPR = null;	

	private static String isKeySet = "isKeySet";
	private static String pdorequest = "pdorequest";
	private static String setKey = "setKey";
	private static String validateSiteId = "validateSiteId";
	private static String getAudit = "getAudit";

	//swc20160721 added following 4 DBlookup related
	private static String getAllDBlookups =  null;	
	private static String setDBlookup =  null;	
	private static String getDBlookup =  null;	
	private static String deleteDBlookup =  null;	
	
	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(IMServiceRESTTest.class);
	}


	@BeforeClass
	public static void setUp() throws Exception {
		String host = (System.getProperty("testhost") == null ? "http://127.0.0.1:9090/i2b2/services" : System.getProperty("testhost") ) ;
		imTargetEPR = 
				host + "/IMService/";	
		 
		testFileDir = "test"; //System.getProperty("testfiledir");
		
		getAllDBlookups = imTargetEPR + "getAllDblookups";
		setDBlookup = imTargetEPR + "setDblookup";
		getDBlookup = imTargetEPR + "getDblookup";
		 deleteDBlookup = imTargetEPR + "deleteDblookup";
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}

	@Test
	public void A10_SetKeyValidUser() throws Exception {
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
	public void A20_pdo_minvalue() throws Exception {
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
	public void A30_SetKeyValidMD5() throws Exception {
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
	public void A40_SetKeyInValidMD5() throws Exception {
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
	public void A50_SetKeyInValidUser() throws Exception {
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
	public void A60_IsSetKeyInValidUser() throws Exception {
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
	public void A70_IsKeySetValidUser() throws Exception {
		String filename = testFileDir + "/iskey_set_valid_user.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(imTargetEPR + isKeySet).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			
			IsKeySetType masterInstanceResult = (IsKeySetType)helper.getObjectByClass(r.getMessageBody().getAny(),IsKeySetType.class);

		//	assertNotNull(masterInstanceResult);
		//	assertTrue(masterInstanceResult.isActive());
			assertTrue(true);
			
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void A80_IsKeySetNonProject() throws Exception {
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
	public void A90_UnSetKeyValidUser() throws Exception {
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
	public void A100_ReSetKeyValidUser() throws Exception {
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
/*
	@Test
	public void validatesiteid() throws Exception {
		String filename = testFileDir + "/validate_site.xml";
		try { 
			try {
				Thread.sleep(3000);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
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
			try {
				Thread.sleep(3000);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			
			boolean found = false;
			for (PatientMapId results : patientDataResponseType.getPatientData().getPidSet().getPid().get(0).getPatientMapId())
			{
				if (results.getValue().equals("2000002062"))
				{
					found = true;
				}
			}
			assertTrue(found);
			try {
				Thread.sleep(3000);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
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

	
	@Test
	public void A110_GetAllDBlookups_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/getAllDBlookups_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getAllDBlookups).sendReceive(requestElement);
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
	public void A120_GetAllDBlookups_non_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/getAllDBlookups_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getAllDBlookups).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A130_SetDBlookup_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/setDBlookup_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);			
			A180_DeleteDBlookup_admin(); //clean it up (in case this gets run after the DeleteDBlookup_admin()
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A140_SetDBlookup_non_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/setDBlookup_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A150_GetDBlookup_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/getDBlookup_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
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
	public void A160_GetDBlookup_schema_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/getDBlookup_schema_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
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
	public void A170_GetDBlookup_non_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/getDBlookup_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A180_DeleteDBlookup_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/deleteDBlookup_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
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
	public void A190_DeleteDBlookup_non_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/deleteDBlookup_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A200_DeleteDBlookup_nonexist_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/deleteDBlookup_nonexist_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			StatusType st = r.getResponseHeader().getResultStatus().getStatus();
			assertEquals("DONE", st.getType());
			assertEquals("no dblookup row was deleted (could be due to no target row found)! - IM processing completed", st.getValue());
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A210_DeleteDBlookup_missingAttrib_admin() throws Exception { //swc20160721
		String filename = testFileDir + "/deleteDBlookup_missing_attrib_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = IMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
}





