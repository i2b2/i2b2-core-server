/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PMServiceRESTTest extends PMAxisAbstract{
	private static String testFileDir = "";

	private static  String pmTargetEPR = null;
	private static  String pmGetVersion = null;
	
	
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(PMServiceRESTTest.class);
	}


	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "test"; //System.getProperty("testfiledir");
		String host = (System.getProperty("testhost") == null ? "http://127.0.0.1:9090/i2b2/services" : System.getProperty("testhost") ) ;
		 pmTargetEPR = 
				host + "/PMService/getServices";	
		 pmGetVersion = 
				host + "/PMService/getVersion";	

		System.out.println("test file dir " + testFileDir);
		System.out.println("host " + host);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}

	@Test
	public void A10_SleepForBamboo() throws Exception {
		String filename = testFileDir + "/pm_create_user_for_crc.xml";
		ConfigureType ctype = null;
		String masterInstanceResult = null;
		try { 

			Thread.sleep(60);

			assertTrue(true);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void A11_GetVersion() throws Exception {
		String filename = testFileDir + "/pm_get_i2b2_version.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmGetVersion).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			
			String messageBody = responseElement.toString().toLowerCase().substring(responseElement.toString().indexOf("message_body"));

			PMService pmService = new PMService();
			
			if (messageBody.contains(pmService.getVersion()))
				assertEquals("Get correct i2b2 Version", pmService.getVersion(), pmService.getVersion());
			else
				assertEquals("Get incorrect i2b2 Version", null, pmService.getVersion());

			messageBody = getHTML(pmGetVersion);
			messageBody = messageBody.toLowerCase().substring(messageBody.indexOf("message_body"));
			if (messageBody.contains(pmService.getMessageVersion()))
				assertEquals("Get correct Message Version", pmService.getMessageVersion(), pmService.getMessageVersion());
			else
				assertEquals("Get incorrect Message Version", null, pmService.getMessageVersion());

			
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void A20_CreateUserRoleforCRC() throws Exception {
		String filename = testFileDir + "/pm_create_user_for_crc.xml";
		ConfigureType ctype = null;
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Create CRC User", masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role1_for_crc.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Set Role 1 for CRC", masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role2_for_crc.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Set Role 2 for CRC", masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role1_for_work.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role2_for_work.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Set Role 2 for Work", masterInstanceResult);
			//Add Role 1
			filename = testFileDir + "/pm_set_role3_for_work.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Set Role 3 for Work", masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role1_for_im.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Set Role 1 for IM", masterInstanceResult);
			//Add Role 1
			filename = testFileDir + "/pm_set_role2_for_im.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Set Role 2 for IM", masterInstanceResult);		
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void A30_validSessionUnvalidUsernoXML() throws Exception {
		try { 

			GetUserConfigurationType userConfig = new GetUserConfigurationType();
			userConfig.getProject().add("Demo");

			edu.harvard.i2b2.pm.datavo.pm.ObjectFactory of = new  edu.harvard.i2b2.pm.datavo.pm.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createGetUserConfiguration(userConfig));


			RequestMessageType requestMessageType = buildRequestMessage(bodyType, "i2b2", "demouser", "Demo");
			StringWriter strWriter = new StringWriter();
			edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory of2 = new edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory();
			PMJAXBUtil.getJAXBUtil().marshaller(of2.createRequest(requestMessageType), strWriter);


			OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			ConfigureType masterInstanceResult = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			assertNotNull(masterInstanceResult);

			// try calling with another user
			requestMessageType = buildRequestMessage(bodyType, "demo", masterInstanceResult.getUser().getPassword().getValue(), "Demo");
			strWriter = new StringWriter();
			of2 = new edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory();
			PMJAXBUtil.getJAXBUtil().marshaller(of2.createRequest(requestMessageType), strWriter);


			requestElement = convertStringToOMElement(strWriter.toString()); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);

		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}	
	@Test
	public void A40_validUsernoXML() throws Exception {
		try { 

			GetUserConfigurationType userConfig = new GetUserConfigurationType();
			userConfig.getProject().add("Demo");

			edu.harvard.i2b2.pm.datavo.pm.ObjectFactory of = new  edu.harvard.i2b2.pm.datavo.pm.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createGetUserConfiguration(userConfig));


			RequestMessageType requestMessageType = buildRequestMessage(bodyType, "i2b2", "demouser", "Demo");
			StringWriter strWriter = new StringWriter();
			edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory of2 = new edu.harvard.i2b2.pm.datavo.i2b2message.ObjectFactory();
			PMJAXBUtil.getJAXBUtil().marshaller(of2.createRequest(requestMessageType), strWriter);


			OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			ConfigureType masterInstanceResult = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			assertNotNull(masterInstanceResult);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}	

	@Test
	public void A50_wrongPassword() throws Exception {
		String filename = testFileDir + "/pm_wrongpassword.xml";
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
	public void A60_validUser() throws Exception {
		String filename = testFileDir + "/pm_valid_user.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			ConfigureType masterInstanceResult = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			assertNotNull(masterInstanceResult);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	@Test
	public void A70_invalidUser() throws Exception {
		String filename = testFileDir + "/pm_invalid_user.xml";
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
	public void A80_CRUDUserWithValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_user_with_valid_user.xml";
		ConfigureType ctype = null;
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

			//Check to see if really added
			filename = testFileDir + "/pm_create_user_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ctype = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getUser().getFullName(),"Bamboo User");
			//	assertEquals(ctype.getUser().getEmail(),"bamboo@i2b2.org");
			assertTrue(ctype.getUser().isIsAdmin());

			//Update the user
			filename = testFileDir + "/pm_update_user_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_user_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getUser().getFullName(),"Bamboo User2");
			//	assertEquals(ctype.getUser().getEmail(),"bamboo@i2b2.org");
			assertFalse(ctype.getUser().isIsAdmin());

			//Delete User
			filename = testFileDir + "/pm_delete_user_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really deleted
			filename = testFileDir + "/pm_create_user_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);			

			//Reenable user
			//Update the user
			filename = testFileDir + "/pm_create_user_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really reenabled
			filename = testFileDir + "/pm_create_user_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getUser().getFullName(),"Bamboo User");
			//	assertEquals(ctype.getUser().getEmail(),"bamboo@i2b2.org");
			assertTrue(ctype.getUser().isIsAdmin());

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void A90_createUserWithInValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_user_with_invalid_user.xml";
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
	public void A100_CRUDCellWithValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_cell_with_valid_user.xml";
		CellDataType ctype = null;
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Create Cell", masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ctype = (CellDataType)helper.getObjectByClass(r.getMessageBody().getAny(),CellDataType.class);
			assertNotNull("Check Exists", ctype);
			assertEquals("Check Exists", ctype.getId(),"Bamboo");
			assertEquals("Check Exists", ctype.getProjectPath(),"/Bamboo");
			assertEquals("Check Exists", ctype.getUrl(),"http://127.0.0.1/bamboo");
			assertEquals("Check Exists", ctype.getName(),"Bamboo test");
			assertEquals("Check Exists", ctype.getMethod(),"REST");

			//Update the cell
			filename = testFileDir + "/pm_update_cell_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Update Cell", masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (CellDataType)helper.getObjectByClass(r.getMessageBody().getAny(),CellDataType.class);
			assertNotNull("Check Exists 2", ctype);
			assertEquals("Check Exists 2", ctype.getId(),"Bamboo");
			assertEquals("Check Exists 2", ctype.getProjectPath(),"/Bamboo");
			assertEquals("Check Exists 2", ctype.getUrl(),"http://127.0.0.1/bamboo2");
			assertEquals("Check Exists 2", ctype.getName(),"Bamboo test2");
			assertEquals("Check Exists 2", ctype.getMethod(),"REST2");

			//Delete cell
			filename = testFileDir + "/pm_delete_cell_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Delete Cell", masterInstanceResult);

			//Check to see if really deleted
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);			

			//Reenable cell
			//Update the cell
			filename = testFileDir + "/pm_create_cell_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Create Cell 3", masterInstanceResult);

			//Check to see if really reenabled
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (CellDataType)helper.getObjectByClass(r.getMessageBody().getAny(),CellDataType.class);
			assertNotNull("Check Exists 3", ctype);
			assertEquals("Check Exists 3", ctype.getId(),"Bamboo");
			assertEquals("Check Exists 3", ctype.getProjectPath(),"/Bamboo");
			assertEquals("Check Exists 3", ctype.getUrl(),"http://127.0.0.1/bamboo");
			assertEquals("Check Exists 3", ctype.getName(),"Bamboo test");
			assertEquals("Check Exists 3", ctype.getMethod(),"REST");
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void A110_createCellWithInValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_cell_with_invalid_user.xml";
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
	public void A120_CRUDProjectWithValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_project_with_valid_user.xml";
		ProjectType ctype = null;
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Create Project", masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ctype = (ProjectType)helper.getObjectByClass(r.getMessageBody().getAny(),ProjectType.class);
			assertNotNull("Check exists", ctype);
			assertEquals("Check exists", ctype.getId(),"BAMBOO");
			assertEquals("Check exists", ctype.getName(),"Bamboo Test");
			assertEquals("Check exists", ctype.getKey(), "ca2");
			assertEquals("Check exists", ctype.getWiki(),"http://127.0.0.1/wiki");
			assertEquals("Check exists", ctype.getDescription(),"This is a message");
			assertEquals("Check exists", ctype.getPath(),"/bamboo");

			//Update the project
			filename = testFileDir + "/pm_update_project_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Update Project", masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (ProjectType)helper.getObjectByClass(r.getMessageBody().getAny(),ProjectType.class);
			assertNotNull("Check exists 2", ctype);
			assertEquals("Check exists 2",ctype.getId(),"BAMBOO");
			assertEquals("Check exists 2",ctype.getName(),"Bamboo Test2");
			assertEquals("Check exists 2",ctype.getKey(), "e82");
			assertEquals("Check exists 2",ctype.getWiki(),"http://127.0.0.1/wiki2");
			assertEquals("Check exists 2",ctype.getDescription(),"This is a message2");
			assertEquals("Check exists 2",ctype.getPath(),"/bamboo");

			//Delete project
			filename = testFileDir + "/pm_delete_project_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Delete Porject", masterInstanceResult);

			//Check to see if really deleted
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			String err = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", err);			

			//Reenable project
			//Update the project
			filename = testFileDir + "/pm_create_project_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Create Project 2",masterInstanceResult);

			//Check to see if really reenabled
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (ProjectType)helper.getObjectByClass(r.getMessageBody().getAny(),ProjectType.class);
			assertNotNull("Check exists 3",ctype);
			assertEquals("Check exists 2",ctype.getId(),"BAMBOO");
			assertEquals("Check exists 2",ctype.getName(),"Bamboo Test");
			assertEquals("Check exists 2",ctype.getWiki(),"http://127.0.0.1/wiki");
			assertEquals("Check exists 2",ctype.getDescription(),"This is a message");
			assertEquals("Check exists 2",ctype.getPath(),"/bamboo");
			assertEquals("Check exists 2",ctype.getKey(), "ca2");
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void A130_createProjectWithInValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_project_with_invalid_user.xml";
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
	public void A140_CRUDGlobalParamWithValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_global_param_with_valid_user.xml";
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

			//Check to see if really added
			filename = testFileDir + "/pm_create_global_param_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ParamsType allParams = (ParamsType)helper.getObjectByClass(r.getMessageBody().getAny(),ParamsType.class);

			for (ParamType param : allParams.getParam())
			{
				if (param.getName().equals("Global") && param.getValue().equals("Global Value"))
				{
					assertNotNull(param);
					assertEquals(param.getName(),"Global");
					assertEquals(param.getValue(),"Global Value");
					assertEquals(param.getDatatype(),"T");
					id = param.getId();
					break;
				}
			}


			//Update the project
			filename = testFileDir + "/pm_update_global_param_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{{{id}}}", Integer.toString(id));
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_recreate_global_param_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{{{id}}}", Integer.toString(id));
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			GlobalDataType global = (GlobalDataType)helper.getObjectByClass(r.getMessageBody().getAny(),GlobalDataType.class);

			for (ParamType param : global.getParam())
			{
				if (param.getId() == id)
				{
					assertNotNull(param);
					assertEquals(param.getName(),"Global");
					assertEquals(param.getValue(),"Global Value2");
					assertEquals(param.getDatatype(),"N");
					break;
				}
			}

			//Delete project
			filename = testFileDir + "/pm_delete_global_param_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{{{id}}}", Integer.toString(id));
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
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

			//Reenable project
			//Update the project
			filename = testFileDir + "/pm_update_global_param_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{{{id}}}", Integer.toString(id));			
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really reenabled
			filename = testFileDir + "/pm_recreate_global_param_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{{{id}}}", Integer.toString(id));
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			global = (GlobalDataType)helper.getObjectByClass(r.getMessageBody().getAny(),GlobalDataType.class);

			for (ParamType param : global.getParam())
			{
				if (param.getId() == id)
				{
					assertNotNull(param);
					assertEquals(param.getName(),"Global");
					assertEquals(param.getValue(),"Global Value2");
					assertEquals(param.getDatatype(),"N");
					break;
				}
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void A150_createGlobalParamWithInValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_global_param_with_invalid_user.xml";
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
	public void A160_createCelllParamWithValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_cell_param_with_valid_user.xml";
		ParamType ctype = null;
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);

			//Check to see if really added
			filename = testFileDir + "/pm_create_cell_param_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ParamsType allParams = (ParamsType)helper.getObjectByClass(r.getMessageBody().getAny(),ParamsType.class);

			for (ParamType param : allParams.getParam())
			{
				if (param.getName().equals("Bamboo_Param"))
				{
					ctype = param;
					break;
				}


			}
		} catch (Exception e) { 
		}
		assertNotNull("Null MasterID", masterInstanceResult);
		assertNotNull("Null ctype", ctype);
		assertEquals("not equal name", ctype.getName(),"Bamboo_Param");
		assertEquals("not equal value", ctype.getValue(),"my test");
		assertEquals("not equal type", ctype.getDatatype(),"T");
	}	


	@Test
	public void A170_createCellParamWithInValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_cell_param_with_invalid_user.xml";
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
	public void A180_createProjetUserParamWithValidUser() throws Exception {
		String filename = testFileDir + "/pm_create_project_user_param_with_valid_user.xml";
		ParamType ctype = null;
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);


			//Check to see if really added
			filename = testFileDir + "/pm_create_project_user_param_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ParamsType allParams = (ParamsType)helper.getObjectByClass(r.getMessageBody().getAny(),ParamsType.class);

			assertEquals( allParams.getParam().size(), 1);
			for (ParamType param : allParams.getParam())
			{
				if (param.getName().equals("Bamboo_Param"))
				{
					ctype = param;
					break;
				}


			}

		} catch (Exception e) { 
			e.printStackTrace();
		}
		assertNotNull(masterInstanceResult);
		assertNotNull(ctype);
		assertEquals(ctype.getName(),"Bamboo_Param");
		assertEquals(ctype.getValue(),"my test");
		assertEquals(ctype.getDatatype(),"T");
	}	

	/*
	@Test
	public void Z1_GlobalParamsSetExpiredPassword() throws Exception {
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
			assertNotNull("Setting expired password", masterInstanceResult);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
		
		
	}
	
	
	@Test
	public void Z2_GlobalParamsSetComplexPassword() throws Exception {

		String filename = testFileDir + "/pm_set_global_complex_password.xml";
		//ParamType ctype = null;
		int  id = -1;
		String  masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull("Setting compelx password", masterInstanceResult);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}		
		
	}

	@Test
	public void Z3_setValidPassword() throws Exception {
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
	public void Z4_setInvalidPassword() throws Exception {
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
	public void Z5_GlobalParamsUnSetExpiredPassword() throws Exception {
		String filename = testFileDir + "/pm_setpassword_good_secure.xml";
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


		filename = testFileDir + "/pm_create_global_param_with_valid_user_check_secure.xml";
		requestString = getQueryString(filename);
		requestElement = convertStringToOMElement(requestString); 
		responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		r = (ResponseMessageType)responseJaxb.getValue();
		helper = new  JAXBUnWrapHelper();
		ParamsType allParams = (ParamsType)helper.getObjectByClass(r.getMessageBody().getAny(),ParamsType.class);
		int id = -1;
		int id_complex = -1;
		for (ParamType param : allParams.getParam())
		{
			if (param.getName().equals("PM_EXPIRED_PASSWORD"))
			{
				assertNotNull(param);
				assertEquals(param.getDatatype(),"N");
				id = param.getId();
			} else if (param.getName().equals("PM_COMPLEX_PASSWORD"))
			{
				assertNotNull(param);
				assertEquals(param.getDatatype(),"T");
				id_complex = param.getId();
			}
		}

		//Delete Param
		filename = testFileDir + "/pm_delete_global_param_with_valid_user_secure.xml";
		requestString = getQueryString(filename);
		requestString = requestString.replace("{{{id}}}", Integer.toString(id));
		requestElement = convertStringToOMElement(requestString); 
		responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		r = (ResponseMessageType)responseJaxb.getValue();
		helper = new  JAXBUnWrapHelper();
		masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
		assertNotNull(masterInstanceResult);

		requestString = getQueryString(filename);
		requestString = requestString.replace("{{{id}}}", Integer.toString(id_complex));
		requestElement = convertStringToOMElement(requestString); 
		responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		r = (ResponseMessageType)responseJaxb.getValue();
		helper = new  JAXBUnWrapHelper();
		masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
		assertNotNull(masterInstanceResult);
		


		//Check to see if really deleted
		filename = testFileDir + "/pm_recreate_global_param_with_valid_user_check_secure.xml";
		requestString = getQueryString(filename);
		requestString = requestString.replace("{{{id}}}", Integer.toString(id));			
		requestElement = convertStringToOMElement(requestString); 
		responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
		responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		r = (ResponseMessageType)responseJaxb.getValue();
		String err = r.getResponseHeader().getResultStatus().getStatus().getType();
		assertEquals("ERROR", err);			

		 filename = testFileDir + "/pm_setpassword_good_i2b2.xml";
		 requestString = getQueryString(filename);
		 requestElement = convertStringToOMElement(requestString); 
		 responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		 responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		 r = (ResponseMessageType)responseJaxb.getValue();
		 helper = new  JAXBUnWrapHelper();
		 masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
		assertNotNull(masterInstanceResult);
	}
	*/


}





