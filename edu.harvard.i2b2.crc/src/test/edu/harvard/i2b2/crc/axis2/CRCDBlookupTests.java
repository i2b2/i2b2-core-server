/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 *
 * Contributors:
 *     S.W.Chan
 */
package edu.harvard.i2b2.crc.axis2;

import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import jakarta.xml.bind.JAXBElement;
import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.axiom.om.OMElement;

/**
 * Class to test different CRC DBLookup request's 
 * @author S.W. Chan
 */
public class CRCDBlookupTests  extends CRCAxisAbstract {
	private static String testFileDir = "";
	private static String crcTargetEPR = "http://localhost:9090/i2b2/services/QueryToolService/";
	private static String getAllDBlookups = crcTargetEPR + "getAllDblookups";
	private static String setDBlookup = crcTargetEPR + "setDblookup";
	private static String getDBlookup = crcTargetEPR + "getDblookup";
	private static String deleteDBlookup = crcTargetEPR + "deleteDblookup";

	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "testfiles/DBlookup"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);
		if (testFileDir == null || 0 == testFileDir.trim().length()) {
			throw new Exception("please provide test file directory info -Dtestfiledir");
		}
	}

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(CRCDBlookupTests.class);
	}

	@Test
	public void GetAllDBlookups_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/getAllDBlookups_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getAllDBlookups).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetAllDBlookups_non_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/getAllDBlookups_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getAllDBlookups).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void SetDBlookup_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/setDBlookup_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);			
			DeleteDBlookup_admin(); //clean it up (in case this gets run after the DeleteDBlookup_admin()
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void SetDBlookup_non_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/setDBlookup_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetDBlookup_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/getDBlookup_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetDBlookup_schema_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/getDBlookup_schema_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetDBlookup_non_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/getDBlookup_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/deleteDBlookup_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_non_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/deleteDBlookup_non_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_nonexist_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/deleteDBlookup_nonexist_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			StatusType st = r.getResponseHeader().getResultStatus().getStatus();
			assertEquals("DONE", st.getType());
			if (st.getValue().contains("no dblookup row was deleted (could be due to no target row found)!")) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_missingAttrib_admin() throws Exception { //swc20160722
		String filename = testFileDir + "/deleteDBlookup_missing_attrib_admin.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
}
