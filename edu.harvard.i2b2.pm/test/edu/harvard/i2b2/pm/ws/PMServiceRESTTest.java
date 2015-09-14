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

public class PMServiceRESTTest extends PMAxisAbstract{
	private static String testFileDir = "";

	private static String pmTargetEPR = 
			"http://127.0.0.1:9090/i2b2/services/PMService/getServices";			
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(PMServiceRESTTest.class);
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
	public void CreateUserRoleforCRC() throws Exception {
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
			assertNotNull(masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role1_for_crc.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Add Role 1
			filename = testFileDir + "/pm_set_role2_for_crc.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

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
			assertNotNull(masterInstanceResult);
			//Add Role 1
			filename = testFileDir + "/pm_set_role3_for_work.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);
			
			//Add Role 1
			filename = testFileDir + "/pm_set_role1_for_im.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);
			//Add Role 1
			filename = testFileDir + "/pm_set_role2_for_im.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);		
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void validSessionUnvalidUsernoXML() throws Exception {
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
	public void validUsernoXML() throws Exception {
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
	public void wrongPassword() throws Exception {
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
	public void validUser() throws Exception {
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
	public void invalidUser() throws Exception {
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
	public void CRUDUserWithValidUser() throws Exception {
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
	public void createUserWithInValidUser() throws Exception {
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
	public void CRUDCellWithValidUser() throws Exception {
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
			assertNotNull(masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ctype = (CellDataType)helper.getObjectByClass(r.getMessageBody().getAny(),CellDataType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getId(),"Bamboo");
			assertEquals(ctype.getProjectPath(),"/Bamboo");
			assertEquals(ctype.getUrl(),"http://127.0.0.1/bamboo");
			assertEquals(ctype.getName(),"Bamboo test");
			assertEquals(ctype.getMethod(),"REST");

			//Update the cell
			filename = testFileDir + "/pm_update_cell_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (CellDataType)helper.getObjectByClass(r.getMessageBody().getAny(),CellDataType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getId(),"Bamboo");
			assertEquals(ctype.getProjectPath(),"/Bamboo");
			assertEquals(ctype.getUrl(),"http://127.0.0.1/bamboo2");
			assertEquals(ctype.getName(),"Bamboo test2");
			assertEquals(ctype.getMethod(),"REST2");

			//Delete cell
			filename = testFileDir + "/pm_delete_cell_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

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
			assertNotNull(masterInstanceResult);

			//Check to see if really reenabled
			filename = testFileDir + "/pm_create_cell_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (CellDataType)helper.getObjectByClass(r.getMessageBody().getAny(),CellDataType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getId(),"Bamboo");
			assertEquals(ctype.getProjectPath(),"/Bamboo");
			assertEquals(ctype.getUrl(),"http://127.0.0.1/bamboo");
			assertEquals(ctype.getName(),"Bamboo test");
			assertEquals(ctype.getMethod(),"REST");
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void createCellWithInValidUser() throws Exception {
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
	public void CRUDProjectWithValidUser() throws Exception {
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
			assertNotNull(masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			ctype = (ProjectType)helper.getObjectByClass(r.getMessageBody().getAny(),ProjectType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getId(),"BAMBOO");
			assertEquals(ctype.getName(),"Bamboo Test");
			assertEquals(ctype.getKey(), "ca2");
			assertEquals(ctype.getWiki(),"http://127.0.0.1/wiki");
			assertEquals(ctype.getDescription(),"This is a message");
			assertEquals(ctype.getPath(),"/bamboo");

			//Update the project
			filename = testFileDir + "/pm_update_project_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

			//Check to see if really added
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (ProjectType)helper.getObjectByClass(r.getMessageBody().getAny(),ProjectType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getId(),"BAMBOO");
			assertEquals(ctype.getName(),"Bamboo Test2");
			assertEquals(ctype.getKey(), "e82");
			assertEquals(ctype.getWiki(),"http://127.0.0.1/wiki2");
			assertEquals(ctype.getDescription(),"This is a message2");
			assertEquals(ctype.getPath(),"/bamboo");

			//Delete project
			filename = testFileDir + "/pm_delete_project_with_valid_user.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);

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
			assertNotNull(masterInstanceResult);

			//Check to see if really reenabled
			filename = testFileDir + "/pm_create_project_with_valid_user_check.xml";
			requestString = getQueryString(filename);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pmTargetEPR).sendReceive(requestElement);
			responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();			
			ctype = (ProjectType)helper.getObjectByClass(r.getMessageBody().getAny(),ProjectType.class);
			assertNotNull(ctype);
			assertEquals(ctype.getId(),"BAMBOO");
			assertEquals(ctype.getName(),"Bamboo Test");
			assertEquals(ctype.getWiki(),"http://127.0.0.1/wiki");
			assertEquals(ctype.getDescription(),"This is a message");
			assertEquals(ctype.getPath(),"/bamboo");
			assertEquals(ctype.getKey(), "ca2");
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void createProjectWithInValidUser() throws Exception {
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
	public void CRUDGlobalParamWithValidUser() throws Exception {
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
	public void createGlobalParamWithInValidUser() throws Exception {
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
	public void createCelllParamWithValidUser() throws Exception {
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
		assertNotNull(masterInstanceResult);
		assertNotNull(ctype);
		assertEquals(ctype.getName(),"Bamboo_Param");
		assertEquals(ctype.getValue(),"my test");
		assertEquals(ctype.getDatatype(),"T");
	}	


	@Test
	public void createCellParamWithInValidUser() throws Exception {
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
	public void createProjetUserParamWithValidUser() throws Exception {
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



}





