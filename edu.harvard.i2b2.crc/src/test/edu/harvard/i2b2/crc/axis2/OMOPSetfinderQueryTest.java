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

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;

import javax.xml.bind.JAXBElement;
import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.axiom.om.OMElement;

/**
 * Class to test different CRC DBLookup request's 
 * @author S.W. Chan
 */
public class OMOPSetfinderQueryTest  extends CRCAxisAbstract {
	private static String testFileDir = "";
	private static String crcTargetEPR = "http://services.i2b2.org/i2b2/services/QueryToolService/request";


	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "testfiles/omop/setfinder"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);
		if (testFileDir == null || 0 == testFileDir.trim().length()) {
			throw new Exception("please provide test file directory info -Dtestfiledir");
		}
	}

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(OMOPSetfinderQueryTest.class);
	}

	@Test
	public void TwoPanelAND() throws Exception { //swc20160722
		String filename = testFileDir + "/HYP_and_ISCH_[609].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 609);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TwoPanelOR() throws Exception { //swc20160722
		String filename = testFileDir + "/HYP_or_ISCH_[770].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 770);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void TwoPanelAND_2Domains() throws Exception { //swc20160722
		String filename = testFileDir + "/HYP_and_Cardio_[635].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 635);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TwoPanelOR_2Domains() throws Exception { //swc20160722
		String filename = testFileDir + "/HYP_or_Cardio_[878].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 878);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void Exclude_Demographic() throws Exception { //swc20160722
		String filename = testFileDir + "/Exclude_White_[181].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 181);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void ValueConstraintLab() throws Exception { //swc20160722
		String filename = testFileDir + "/CBC_GT_[5].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 5);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void EnumConstraintLab() throws Exception { 
		String filename = testFileDir + "/Poly_TRACE_[5].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 5);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void DateConstraintProc() throws Exception { 
		String filename = testFileDir + "/Cardio_proc_after_2009_[15].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 15);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void Temporal_3Domains() throws Exception { 
		String filename = testFileDir + "/Cardio_Meds_before_Operation_[42].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(crcTargetEPR).sendReceive(requestElement);
			String response = responseElement.toString();
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 42);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
