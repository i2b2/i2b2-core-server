/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.axis2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestType;

/**
 * Class to test different setfinder request's 
 * @author rkuttan
 */
public class SetfinderQueryTest  extends CRCAxisAbstract {

	private static QueryMasterType queryMaster = null; 
	private static QueryInstanceType queryInstance = null;
	private static MasterInstanceResultResponseType masterInstanceResult = null;
	private static String testFileDir = null;

	private static  String setfinderUrl = 
			//System.getProperty("testhost") 
			"http://localhost:9090/i2b2/services"
			+ "/QueryToolService/request";	


	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "testfiles"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}





	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(SetfinderQueryTest.class);
	}

	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}




	@Test
	public void QueryInQueryCKMB_OR() throws Exception {
		String filename = testFileDir + "/4Q_CK-MB_OR_CPKGT120_[38].xml";
		try { 
			DataInputStream   dataStream = new DataInputStream(new FileInputStream(
					filename));
			OMElement requestElement = convertStringToOMElement(dataStream); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			String queryMasterId = masterInstanceResult.getQueryMaster().getQueryMasterId();

			// First Query In Query
			String requestString = getQueryString(testFileDir + "/QIQ_4Q_MALE_[28].xml");
			requestString = requestString.replace("masterid:431", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 28);
				else
					assertTrue(false);
			}


			// Second Query In Query
			requestString = getQueryString(testFileDir + "/QIQ_4Q_FEMALE_[10].xml");
			requestString = requestString.replace("masterid:431", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 10);
				else
					assertTrue(false);
			}



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void QueryInQueryCKMB() throws Exception {
		String filename = testFileDir + "/3Q_CK-MB_AND_CPKGT120_[16].xml";
		try { 
			DataInputStream   dataStream = new DataInputStream(new FileInputStream(
					filename));
			OMElement requestElement = convertStringToOMElement(dataStream); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			String queryMasterId = masterInstanceResult.getQueryMaster().getQueryMasterId();

			// First Query In Query
			String requestString = getQueryString(testFileDir + "/QIQ_3Q_MALE_[12].xml");
			requestString = requestString.replace("masterid:427", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 12);
				else
					assertTrue(false);
			}


			// Second Query In Query
			requestString = getQueryString(testFileDir + "/QIQ_3Q_FEMALE_[4].xml");
			requestString = requestString.replace("masterid:427", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void QueryInQueryHypertensionOR() throws Exception {
		String filename = testFileDir + "/2Q_HYP_OR_ISCH_[44].xml";
		try { 
			DataInputStream   dataStream = new DataInputStream(new FileInputStream(
					filename));
			OMElement requestElement = convertStringToOMElement(dataStream); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			String queryMasterId = masterInstanceResult.getQueryMaster().getQueryMasterId();

			// First Query In Query
			String requestString = getQueryString(testFileDir + "/QIQ_2Q_MALE_[26].xml");
			requestString = requestString.replace("masterid:424", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 26);
				else
					assertTrue(false);
			}


			// Second Query In Query
			requestString = getQueryString(testFileDir + "/QIQ_2Q_FEMALE_[18].xml");
			requestString = requestString.replace("masterid:424", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void QueryInQueryHypertension() throws Exception {
		String filename = testFileDir + "/1Q_HYP_AND_ISCH_[13].xml";
		try { 
			DataInputStream   dataStream = new DataInputStream(new FileInputStream(
					filename));
			OMElement requestElement = convertStringToOMElement(dataStream); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			String queryMasterId = masterInstanceResult.getQueryMaster().getQueryMasterId();

			// First Query In Query
			String requestString = getQueryString(testFileDir + "/QIQ_1Q_MALE_[6].xml");
			requestString = requestString.replace("masterid:421", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}


			// Second Query In Query
			requestString = getQueryString(testFileDir + "/QIQ_1Q_FEMALE_[7].xml");
			requestString = requestString.replace("masterid:421", "masterid:"+queryMasterId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();

			masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 7);
				else
					assertTrue(false);
			}



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void ExcludeOccurancesMultiplePanelsSame() throws Exception {
		String filename = testFileDir + "/setfinder_exclude_and_occurances_same_[63]_1432ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 99);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void ExcludeOccurancesMultiplePanelsAny() throws Exception {
		String filename = testFileDir + "/setfinder_exclude_and_occurances_any_[63]_1432ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 63);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void TestOObfuscatedLoockout() throws Exception {
		String filename = testFileDir + "/obfuscated_lockout.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 

			//read test file and store query instance ;
			//unmarshall this response string 
			String lockout = "";
			ArrayList list = new ArrayList();
			for (int i=0; i < 14; i++) {
				OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
				JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
				ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
				JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

				//MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

				System.out.println("Query:"+ i);
				//assertNotNull(masterInstanceResult);

				//if (r.getResponseHeader().getResultStatus().getStatus().getType().equals("ERROR"))
				//{
				//	lockout = r.getResponseHeader().getResultStatus().getStatus().getValue();
				//} else 

				if (r != null) {
					//	assertNotNull(masterInstanceResult);
					//		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
					//		{
					if (r.getResponseHeader().getResultStatus().getStatus().getType().equals("ERROR"))
					{
						lockout = r.getResponseHeader().getResultStatus().getStatus().getValue();
						break;
					}

					//		if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					//		{
					//			if (!list.contains(results.getSetSize()))
					//				list.add(results.getSetSize());
					//		}
					//	}
				}
			}
			//assertTrue(list.size() > 3);
			assertTrue(lockout.startsWith("LOCKEDOUT"));
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	/*
	@Test
	public void Panel1Item3Missing() throws Exception {
		String filename = testFileDir + "/setfinder_panel_1_item_3_missing_[Error].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{


				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}

		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	 */
	/*
	@Test
	public void QueryOver50000() throws Exception {
		String filename = testFileDir + "/SQP1I1_Circulatory_[66]_3016ms.xml";
		try { 
			for (int i=0; i < 50000; i++) {
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 66);
				else
					assertTrue(false);
				System.out.println("Query number: " + i + " total number is " +results.getSetSize());
			}
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	 */


	@Test
	public void QueryLargeTextConstaintwithSpace() throws Exception {
		String filename = testFileDir + "/setfinder_query_largetextconstraint_withspace_[18]_1200ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{

				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				{
					if (results.getSetSize() == 18)
						assertEquals(results.getSetSize(), 18);
					else if (results.getSetSize() == 19)
						assertEquals(results.getSetSize(), 19); 
					else
						assertTrue(false);
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I1P2I13() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1_[48]_6658ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 48);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items1() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_1item_[94]_5800ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 94);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void QueryNumericConstraintGreaterThan() throws Exception {
		String filename = testFileDir + "/setfinder_query_numericconstraint_greatthan_[103]_2100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 103);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void DateContraintEnddateBetween() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_enddate_between_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}



	@Test
	public void CQEx_circDigestNeuro() throws Exception {
		String filename = testFileDir + "/CQEx_circDigestNeuro_[14]_21704ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 14);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TQQemptyStringPSI() throws Exception {
		String filename = testFileDir + "/TQQemptyStringPSI_[0]_4570ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	



	@Test
	public void TQNoQueryTimingPSI() throws Exception {
		// Will default to ANY and should cause a error
		String filename = testFileDir + "/TQNoQueryTimingPSI_[0]_557ms.xml";		
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{
			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 0);
			else
				assertTrue(false);
		}
	}	



	@Test
	public void TQQAPASI() throws Exception {
		String filename = testFileDir + "/TQQAPASI_[Error]_659ms.xml";
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{


			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 7);
			else
				assertTrue(false);
		}
	}


	@Test
	public void TQQSIPSI3() throws Exception {
		String filename = testFileDir + "/TQQSIPSI-a_[0]_557ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void TQNoQueryTimingPAny() throws Exception {
		// Will default to ANY and should cause a error
		String filename = testFileDir + "/TQNoQueryTimingPAny_[18]_6057ms.xml";		
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{
			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 18);
			else
				assertTrue(false);
		}
	}


	@Test
	public void TQQemptyStringPSV() throws Exception {
		String filename = testFileDir + "/TQQemptyStringPSV_[18]_630msxml.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	



	@Test
	public void PanelTimingSameMissingQueryTiming() throws Exception {
		// Will default to ANY and should cause a error
		String filename = testFileDir + "/setfinder_paneltiming_same_[133].xml";		
		//	try { 
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{
			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 133);
		}
	}	

	@Test
	public void TQQSIPSISVc() throws Exception {
		String filename = testFileDir + "/TQQSIPSISV-c_[0]_4105ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void Panel4Item3Missing() throws Exception {
		String filename = testFileDir + "/setfinder_panel_4_item_3_missing_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);

			int size = -2;
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					size = results.getSetSize();
			}
			//assertEquals(size, 1);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TextconstraintIN() throws Exception {
		String filename = testFileDir + "/setfinder_query_textconstraint_IN_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	/* For post 1.6.03
	@Test
	public void NumberConstraintNotEqual() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_NE.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	 */

	@Test
	public void TQQemptyStringPEmptyString() throws Exception {
		String filename = testFileDir + "/TQQemptyStringPEmptyString_[18]_4994ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void TQQAPSV() throws Exception {
		String filename = testFileDir + "/TQQAPSV_[Error]_646ms.xml";
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);
		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{


			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 18);
			else
				assertTrue(false);
		}

	}	

	@Test
	public void TQQemptyStringPAny() throws Exception {
		String filename = testFileDir + "/TQQemptyStringPAny_[18]_603ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test

	public void TQQAPSI() throws Exception {
		String filename = testFileDir + "/TQQAPSI_[Error]_4654ms.xml";
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{


			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 0);
			else
				assertTrue(false);
		}

	}	

	@Test
	public void TQNoQueryOrPanelTiming() throws Exception {
		String filename = testFileDir + "/TQNoQueryOrPanelTiming_[18]_6057ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	
	@Test
	public void TQNoQueryTimingPSV() throws Exception {
		String filename = testFileDir + "/TQNoQueryTimingPSV_[18]_6490ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void TQQSIPSV() throws Exception {
		String filename = testFileDir + "/TQQSIPSV_[18]_4491ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	



	@Test
	public void MQDtBtw() throws Exception {
		String filename = testFileDir + "/MQDtBtw_[4]_3006ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQDtBtw2() throws Exception {
		String filename = testFileDir + "/MQDtBtw2_[4]_3006ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQDtBtw3() throws Exception {
		String filename = testFileDir + "/MQDtBtw3_[4]_3006ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQDtBtw4() throws Exception {
		String filename = testFileDir + "/MQDtBtw4_[4]_3006ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I1() throws Exception {
		String filename = testFileDir + "/MQP1I1_[31]_3207ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 31);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void TQQSVPSV2() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[4]_4578ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1P2I1() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1_[1]_3135ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I12() throws Exception {
		String filename = testFileDir + "/MQP1I1_[48]_2005ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 48);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void TQQSVPSV3() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[5]_5800ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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
	public void MQP1I1P2I12() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1_[10]_3013ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 10);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1b() throws Exception {
		String filename = testFileDir + "/MQP1I1-b_[8]_1855ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 8);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void TQQSVPSVb() throws Exception {
		String filename = testFileDir + "/TQQSVPSV-b_[5]_5761ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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
	public void MQP1I2() throws Exception {
		String filename = testFileDir + "/MQP1I1_[13]_1903ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 13);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValEnmMV() throws Exception {
		String filename = testFileDir + "/MQValEnmMV_[14]_1877ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 14);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I13() throws Exception {
		String filename = testFileDir + "/MQP1I1_[1]_1861ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQFolderf() throws Exception {
		String filename = testFileDir + "/MQFolder-f_[3]_1882ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFolderc() throws Exception {
		String filename = testFileDir + "/MQFolder-c_[3]_1893ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void TQQSIPASI() throws Exception {
		String filename = testFileDir + "/TQQSIPASI_[5]_4492ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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
	public void TQQSVPSV4() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[17]_5659ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFolderd() throws Exception {
		String filename = testFileDir + "/MQFolder-d_[3]_3206ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1P2I14() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1_[6]_1919ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I14() throws Exception {
		String filename = testFileDir + "/MQP1I1_[70]_1997ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 70);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I2P2I2() throws Exception {
		String filename = testFileDir + "/MQP1I2P2I2_[73]_12585ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 73);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1P2I15() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1_[3]_5145ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I15() throws Exception {
		String filename = testFileDir + "/MQP1I1_[7]_1908ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 7);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1P2I16() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1_[7]_3563ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 7);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValNumLT() throws Exception {
		String filename = testFileDir + "/MQValNumLT_[19]_1896.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 19);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I22() throws Exception {
		String filename = testFileDir + "/MQP1I2_[32]_2969ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 32);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I16() throws Exception {
		String filename = testFileDir + "/MQP1I1_[16]_1879ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 16);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQExValNumLTandGTEandE() throws Exception {
		String filename = testFileDir + "/MQExValNumLTandGTEandE_[7]_4389ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 7);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFoldere() throws Exception {
		String filename = testFileDir + "/MQFolder-e_[3]_1882ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I17() throws Exception {
		String filename = testFileDir + "/MQP1I1_[5]_3237ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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
	public void MQP1I18() throws Exception {
		String filename = testFileDir + "/MQP1I1_[4]_2348ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValNumLTandGTEandE() throws Exception {
		String filename = testFileDir + "/MQValNumLTandGTEandE_[9]_4171ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 9);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFolder() throws Exception {
		String filename = testFileDir + "/MQFolder-h_[3]_1919ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFolderb() throws Exception {
		String filename = testFileDir + "/MQFolder-b_[2]_1876ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I19() throws Exception {
		String filename = testFileDir + "/MQP1I1_[65]_2320ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 65);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1P2I1P3I1() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1P3I1_[3]_4523ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I1b2() throws Exception {
		String filename = testFileDir + "/MQP1I1-b_[6]_1896ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I110() throws Exception {
		String filename = testFileDir + "/MQP1I1_[54]_1984ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 54);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void TQQSIPSI() throws Exception {
		String filename = testFileDir + "/TQQSIPSI_[0]_3117ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I11() throws Exception {
		String filename = testFileDir + "/MQP1I1_[89]_1895ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 89);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQDtFrom() throws Exception {
		String filename = testFileDir + "/MQDtFrom_[3]_2988ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValNumLTandEnm() throws Exception {
		String filename = testFileDir + "/MQValNumLTandEnm_[6]_3109ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFolder2() throws Exception {
		String filename = testFileDir + "/MQFolder_[14]_1876ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 14);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValNumLTandGTE() throws Exception {
		String filename = testFileDir + "/MQValNumLTandGTE_[9]_4454ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 9);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQFolder3() throws Exception {
		String filename = testFileDir + "/MQFolder-g_[2]_1866ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQP1I3() throws Exception {
		String filename = testFileDir + "/MQP1I3_[5]_6018ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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
	public void TQQSIPSI2() throws Exception {
		String filename = testFileDir + "/TQQSIPSI_[17]_4310ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValEnmMV2() throws Exception {
		String filename = testFileDir + "/MQValEnmMV_[8]_4578ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 8);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void MQValNumLTandEnm2() throws Exception {
		String filename = testFileDir + "/MQValNumLTandEnm_[17]_5758ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQFolderb2() throws Exception {
		String filename = testFileDir + "/MQFolder-i_[3]_1866ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I111() throws Exception {
		String filename = testFileDir + "/MQP1I1_[12]_3298ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 12);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQValNumLTandEnm3() throws Exception {
		String filename = testFileDir + "/MQValNumLTandEnm_[5]_4585ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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
	public void TQQSVPSV5() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[0]_3557ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I112() throws Exception {
		String filename = testFileDir + "/MQP1I1_[58]_1893ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 58);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQDtTo() throws Exception {
		String filename = testFileDir + "/MQDtTo_[1]_4371ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TQQSVPSV6() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[3]_4944ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQValEnmMV3() throws Exception {
		String filename = testFileDir + "/MQValEnmMV_[6]_2580ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I1b3() throws Exception {
		String filename = testFileDir + "/MQP1I1-b_[7]_1904ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 7);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TQQSIPSIb() throws Exception {
		String filename = testFileDir + "/TQQSIPSI-b_[0]_5704ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I113() throws Exception {
		String filename = testFileDir + "/MQP1I1_[17]_1915ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I114() throws Exception {
		String filename = testFileDir + "/MQP1I1_[8]_3276ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 8);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I1P2I1P3I11() throws Exception {
		String filename = testFileDir + "/MQP1I1P2I1P3I1_[2]_5653ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TQQSVPSV12() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[111]_3098ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 111);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void MQP1I115() throws Exception {
		String filename = testFileDir + "/MQP1I1_[6]_1912ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void TQQSIPSISV() throws Exception {
		String filename = testFileDir + "/TQQSIPSISV_[18]_4491ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}




	@Test
	public void TQQSIPASI2() throws Exception {
		String filename = testFileDir + "/TQQSIPASI_[18]_4681ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void MQQSVPSV() throws Exception {
		String filename = testFileDir + "/MQQSVPSV_[18]_5254ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void TQQSVPASV2() throws Exception {
		String filename = testFileDir + "/TQQSVPASV_[18]_6805ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void TQQSIPSISVb() throws Exception {
		String filename = testFileDir + "/TQQSIPSISV-b_[18]_5873ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void TQQSVPASI() throws Exception {
		// Will default to ANY and should cause a error
		String filename = testFileDir + "/TQQSVPASI_[Error]_6793ms.xml";
		//	try { 
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);
		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{


			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 18);
			else
				assertTrue(false);
		}
	}


	/*
	@Test
	public void PanelTimingSameInstancenumMissingQueryTiming() throws Exception {
		// Will default to ANY and should cause a error
		String filename = testFileDir + "/setfinder_paneltiming_sameinstancenum_[Error].xml";
		//	try { 
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

		assertNotNull(masterInstanceResult);
		for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
		{


			if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				assertEquals(results.getSetSize(), 133);
			else
				assertTrue(false);
		}

		//		} catch (Exception e) { 
		//			e.printStackTrace();
		//			throw e;
		//		}
	}

	 */
	@Test
	public void QueryTimingSame() throws Exception {
		String filename = testFileDir + "/setfinder_querytiming_same_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TextconstraintBetween() throws Exception {
		String filename = testFileDir + "/setfinder_query_textconstraint_BETWEEN_[error].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);			
			assertEquals(masterInstanceResult.getStatus().getCondition().get(0).getType(), "ERROR");

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void Panel4Item3() throws Exception {
		String filename = testFileDir + "/setfinder_panel_4_item_3_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void InvertWithItem() throws Exception {
		String filename = testFileDir + "/setfinder_invert_withitem_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void QueryNumericConstraintBetween() throws Exception {
		String filename = testFileDir + "/setfinder_query_numericconstraint_between_[9]_2800ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 9);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void QueryNumericConstraintEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_numericconstraint_eq_[3]_2300ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void QuerySmallTextConstraintLessThan() throws Exception {
		String filename = testFileDir + "/setfinder_query_numericconstraint_lessthan_[6]_2100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void QuerySmallTextConstraintBegin() throws Exception {
		String filename = testFileDir + "/setfinder_query_smalltextconstraint_begin_[1]_1500ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void QuerySmallTextConstraintEnd() throws Exception {
		String filename = testFileDir + "/setfinder_query_smalltextconstraint_end_[1]_1600ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void QuerySmallTextConstraintContains() throws Exception {
		String filename = testFileDir + "/setfinder_query_smalltextconstraint_contains_[3]_1400ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void EncounterContraint1() throws Exception {
		String filename = testFileDir + "/setfinder_query_enumconstraint_[1]_1700ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void EncounterContraint2() throws Exception {
		String filename = testFileDir + "/setfinder_query_enumconstraint_[3]_1900ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void EncounterContraint3() throws Exception {
		String filename = testFileDir + "/setfinder_query_enumconstraint_[4]_2100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void LargeTextContraint() throws Exception {
		String filename = testFileDir + "/setfinder_query_largetextconstraint_[10]_1700ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 10);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	


	@Test
	public void NumericContraintBetween() throws Exception {
		String filename = testFileDir + "/setfinder_query_numericconstraint_between_[9]_2800ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 9);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void SmallTextContraintContains() throws Exception {
		String filename = testFileDir + "/setfinder_query_smalltextconstraint_contains_[3]_1400ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	


	@Test
	public void getPanel1Items2Panel2InvertItem1() throws Exception {
		String filename = testFileDir + "/setfinder_pane_1_item_2_panel_2_INV_item_1_[33].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 33);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void getInvertPanel1Items2InvertPanel2Item2InvertPanel3Item2() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_invert_2panel_2item_invert_3panel_2item_[50]_7000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 50);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items2InvertPanel2Item2NonInvertPanel3Item1() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_invert_2panel_2item_noninvert_3panel_1item_[6]_6000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items2InvertPanel2Item2NonInvertPanel3Item2() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_invert_2panel_2item_noninvert_3panel_2item_[13]_7100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 13);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items2InvertPanel2Item2() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_invert_2panel_2item_[63]_4100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 63);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items2NonInvertPanel2Item1() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_noninvert_2panel_1item_[11]_7000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items2NonInvertPanel2Item2() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_noninvert_2panel_2item_[20]_6100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 20);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getInvertPanel1Items2() throws Exception {
		String filename = testFileDir + "/setfinder_invert_1panel_2item_[83]_5300ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 83);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getNonInvertPanel1Items1InvertPanel2Item2InvertPanel3Item2() throws Exception {
		String filename = testFileDir + "/setfinder_noninvert_1panel_1item_invert_2panel_2item_invert_3panel_2item_[6]_6000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getNonInvertPanel1Items2InvertPanel2Item1() throws Exception {
		String filename = testFileDir + "/setfinder_noninvert_1panel_2item_invert_2panel_1item_[11]_7000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getNonInvertPanel1Items2InvertPanel2Item2InvertPanel3Item2() throws Exception {
		String filename = testFileDir + "/setfinder_noninvert_1panel_2item_invert_2panel_2item_invert_3panel_2item_[13]_7100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 13);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getNonInvertPanel1Items2InvertPanel2Item2() throws Exception {
		String filename = testFileDir + "/setfinder_noninvert_1panel_2item_invert_2panel_2item_[20]_6100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 20);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void getNonInvertPanel7Items2InvertPanel8Item1() throws Exception {
		String filename = testFileDir + "/setfinder_noninvert_7panel_1item_invert_8panel_1item_[1]_12200ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void AgePanel1Item1a() throws Exception {
		String filename = testFileDir + "/setfinder_age_panel_1_item_1_[20]_4000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					{
						assertTrue("age is to high", 10 >= results.getSetSize());
						  assertTrue("age is to low",  8  <= results.getSetSize());
					}
					else
					{
						assertTrue(false);
					}

			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void AgePanel1Item1b() throws Exception {
		String filename = testFileDir + "/setfinder_age_panel_1_item_1_[14]_5500ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void AgePanel1Item1c() throws Exception {
		String filename = testFileDir + "/setfinder_age_panel_1_item_1_[20]_4000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				{
					assertTrue("age is to high", 10 >= results.getSetSize());
					  assertTrue("age is to low",  8  <= results.getSetSize());
				}
				else
				{
					assertTrue(false);
				}

			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void AgeVisitPanel2Item1() throws Exception {
		String filename = testFileDir + "/setfinder_age_visit_panel_2_item_1_[20]_5900ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				{
					assertTrue("age is to high", 10 >= results.getSetSize());
					  assertTrue("age is to low",  8  <= results.getSetSize());
				}
				else
				{
					assertTrue(false);
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void AgeVisitProviderPanel3Item1() throws Exception {
		String filename = testFileDir + "/setfinder_age_visit_prov_panel_3_item_1_[3]_7700ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void AgeVisitLengthPanel2Item1() throws Exception {
		String filename = testFileDir + "/setfinder_age_visitLen_panel_2_item_1_[3]_6100ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 4);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void IncRaceHypTenPanel3Item1() throws Exception {
		String filename = testFileDir + "/setfinder_inc_race_HypTen_panel_3_item_2_[9]_9200ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 9);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void IncRacePanel2Item2() throws Exception {
		String filename = testFileDir + "/setfinder_inc_race_panel_2_item_2_[26]_6600ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 26);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void IncRacProcedurePanel3Item2() throws Exception {
		String filename = testFileDir + "/setfinder_inc_race_Procedure_panel_3_item_2_[1]_9200ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void IncomePanel1Item1() throws Exception {
		String filename = testFileDir + "/setfinder_income_panel_1_item_1_[42]_5200ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
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


	@Test
	public void SQP1I1_DxMI() throws Exception {
		String filename = testFileDir + "/SQP1I1_DxMI_[7]_1625ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 7);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		


	@Test
	public void MQ_princDx_Diabetes() throws Exception {
		String filename = testFileDir + "/MQ_princDx_Diabetes_[2]_2141ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void MCQValMix_DoseFreqRoute_trimox() throws Exception {
		String filename = testFileDir + "/MCQValMix_DoseFreqRoute_trimox_[17]_6516ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQOcrGt5x_IschHrt() throws Exception {
		String filename = testFileDir + "/CQOcrGt5x_IschHrt_[2]_1641ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQOcrGt7x_CircSys() throws Exception {
		String filename = testFileDir + "/CQOcrGt7x_CircSys_[17]_1672ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	
	@Test
	public void SQP1I1_Circulatory() throws Exception {
		String filename = testFileDir + "/SQP1I1_Circulatory_[66]_3016ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 66);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		
	@Test
	public void CQValFlgH_CPK() throws Exception {
		String filename = testFileDir + "/CQValFlgH_CPK_[0]_1657ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		
	@Test
	public void CQValFlgL_CPK() throws Exception {
		String filename = testFileDir + "/CQValFlgL_CPK_[0]_3032ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQValNum_CPK() throws Exception {
		String filename = testFileDir + "/CQValNum_CPK_[40]_1610ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 40);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		


	@Test
	public void CQValNum110_CPK() throws Exception {
		String filename = testFileDir + "/CQValNum110_CPK_[3]_1672ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQValNumBtw52_250_CPK() throws Exception {
		String filename = testFileDir + "/CQValNumBtw52-250_CPK_[35]_1625ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 35);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQValNumGT110_CPK() throws Exception {
		String filename = testFileDir + "/CQValNumGT110_CPK_[29]_1625ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 29);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQValNumGTE110_CPK() throws Exception {
		String filename = testFileDir + "/CQValNumGTE110_CPK_[30]_1625ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 30);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		
	@Test
	public void CQValNumLT110_CPK() throws Exception {
		String filename = testFileDir + "/CQValNumLT110_CPK_[16]_1594ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 16);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void CQValNumLTE110_CPK() throws Exception {
		String filename = testFileDir + "/CQValNumLTE110_CPK_[17]_3078ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void MCQValEnmMV_Route() throws Exception {
		String filename = testFileDir + "/MCQValEnmMV_Route[63]_2015ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 63);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void MCQValNum_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNum_Dose_[18]_2047ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	


	@Test
	public void MCQValNumBtw100_900_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNumBtw100-900_Dose_[18]_2000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void MCQValNumE500_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNumE500_Dose_[17]_2047ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void MCQValNumGT500_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNumGT500_Dose_[6]_2047ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 6);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		


	@Test
	public void MCQValNumGTE500_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNumGTE500_Dose_[18]_1984ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 18);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void MCQValNumLT500_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNumLT500_Dose_[0]_3391ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		
	@Test
	public void MCQValNumLTE500_Dose() throws Exception {
		String filename = testFileDir + "/MCQValNumLTE500_Dose_[17]_2000ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		
	@Test
	public void TQQAPA() throws Exception {
		String filename = testFileDir + "/TQQAPA_[17]_4219ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	


	@Test
	public void TQQSVPASV() throws Exception {
		String filename = testFileDir + "/TQQSVPASV_[17]_14516ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void TQQSVPSV() throws Exception {
		String filename = testFileDir + "/TQQSVPSV_[17]_5532ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQOcrGt1x() throws Exception {
		String filename = testFileDir + "/CQOcrGt1x_[12]_1671ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 12);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void CQEx_ischHrt_Fem() throws Exception {
		String filename = testFileDir + "/CQEx_ischHrt-Fem_[9]_2485ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 9);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}		

	@Test
	public void CQDt_ischHrt() throws Exception {
		String filename = testFileDir + "/CQDt_ischHrt_[2]_1657ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 2);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@Test
	public void SQP1I1P2I1_ischHrt_Fem() throws Exception {
		String filename = testFileDir + "/SQP1I1P2I1_ischHrt-Fem_[8]_2344ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 8);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void SQP1I1_ischHrt() throws Exception {
		String filename = testFileDir + "/SQP1I1_ischHrt_[17]_1657ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 17);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void getAllResults() throws Exception {
		String filename = testFileDir + "/setfinder_allresulttype_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void DateContraintEnddateEqual() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_enddate_equal_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void DateContraintEnddateGreaterthan() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_enddate_greaterthan_[5].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 5);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 5);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 5);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 5);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void DateContraintEnddateLessthan() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_enddate_lessthan_[3].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void DateContraintStartdateBetween() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_startdate_between_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void DateContraintStartdateEqual() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_startdate_equal_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void DateContraintStartdateGreaterthan() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_startdate_greaterthan_[12].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 12);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 12);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 12);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 12);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void DateContraintStartdateLessthan() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_startdate_lessthan_[3].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void DateContraintStartenddate() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_startenddate_[3].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void DateContraint() throws Exception {
		String filename = testFileDir + "/setfinder_dateconstraint_[131].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 131);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 131);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 131);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 131);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void InvertWithNoItem() throws Exception {
		String filename = testFileDir + "/setfinder_invert_withnoitem_[134].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 134);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 134);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 134);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 134);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void ModifierConstraint() throws Exception {
		String filename = testFileDir + "/setfinder_modifierconstraint_[11].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 11);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void NumberConstraintBetween() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_BETWEEN_[113].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 113);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 113);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 113);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 113);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void NumberConstraintEqual() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_EQ_[11].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 11);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 11);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	@Test
	public void NumberConstraintGreaterEqual() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_GE_[114].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 114);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 114);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 114);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 114);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void NumberConstraintGreaterThan() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_GT_[114].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 114);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 114);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 114);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 114);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	@Test
	public void NumberConstraintLessEqual() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_LE_[122].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 122);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 122);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 122);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 122);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	@Test
	public void NumberConstraintLessThan() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_LT_[121].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 121);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 121);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 121);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 121);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void NumberConstraint() throws Exception {
		String filename = testFileDir + "/setfinder_numberconstraint_[22].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 22);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 22);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 22);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 22);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void Panel1Item3() throws Exception {
		String filename = testFileDir + "/setfinder_panel_1_item_3_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}





	@Test
	public void PanelTimingAny() throws Exception {
		String filename = testFileDir + "/setfinder_paneltiming_any_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}



	@Test
	public void FlagconstraintEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_flagconstraint_EQ_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void FlagconstraintIN() throws Exception {
		String filename = testFileDir + "/setfinder_query_flagconstraint_IN_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void FlagconstraintNotEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_flagconstraint_NE_[3].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 3);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 3);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void Flagconstraint() throws Exception {
		String filename = testFileDir + "/setfinder_query_flagconstraint_[1].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 1);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 1);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void ModifierconstraintEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_modifierconstraint_EQ_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void ModifierconstraintIN() throws Exception {
		String filename = testFileDir + "/setfinder_query_modifierconstraint_IN_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}
	@Test
	public void ModifierconstraintLike() throws Exception {
		String filename = testFileDir + "/setfinder_query_modifierconstraint_LIKE_[10].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 10);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 10);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 10);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 10);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void ModifierconstraintNotEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_modifierconstraint_NE_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void Modifierconstraint() throws Exception {
		String filename = testFileDir + "/setfinder_query_modifierconstraint_[0].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 0);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 0);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void TextconstraintEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_textconstraint_EQ_[23 OR 13].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				{
					if (results.getSetSize() == 13)
						assertEquals(results.getSetSize(), 13);
					else if (results.getSetSize() == 23)
						assertEquals(results.getSetSize(), 23); 
					else
						assertTrue(false);
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void TextconstraintLike() throws Exception {
		String filename = testFileDir + "/setfinder_query_textconstraint_LIKE_[90].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 90);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 90);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 90);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 90);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TextconstraintNotEqual() throws Exception {
		String filename = testFileDir + "/setfinder_query_textconstraint_NE_[99 OR 101].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				{
					if (results.getSetSize() == 101)
						assertEquals(results.getSetSize(), 101);
					else if (results.getSetSize() == 99)
						assertEquals(results.getSetSize(), 99); 
					else
						assertTrue(false);
				}				
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void Textconstraint() throws Exception {
		String filename = testFileDir + "/setfinder_query_textconstraint_[23 OR 13].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
				{
					if (results.getSetSize() == 13)
						assertEquals(results.getSetSize(), 13);
					else if (results.getSetSize() == 23)
						assertEquals(results.getSetSize(), 23); 
					else
						assertTrue(false);
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}



	@Test
	public void QueryTimingSameInstanceNum() throws Exception {
		String filename = testFileDir + "/setfinder_querytiming_sameinstancenum_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TotalItemOccurrencesEqual() throws Exception {
		String filename = testFileDir + "/setfinder_totalitemoccurrences_EQ_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TotalItemOccurrences_GreaterEqual() throws Exception {
		String filename = testFileDir + "/setfinder_totalitemoccurrences_GE_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TotalItemOccurrencesLessEqual() throws Exception {
		String filename = testFileDir + "/setfinder_totalitemoccurrences_LE_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TotalItemOccurrencesNotEqual() throws Exception {
		String filename = testFileDir + "/setfinder_totalitemoccurrences_NE_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void TotalItemOccurrences() throws Exception {
		String filename = testFileDir + "/setfinder_totalitemoccurrences_[133].xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_GENDER_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENT_VITALSTATUS_COUNT_XML"))
					assertEquals(results.getSetSize(), 133);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 133);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void SQP1I1_Circulatory4TemporalAny() throws Exception {
		String filename = testFileDir + "/SQP1I1_Circulatory_Any_[66]_3016ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 66);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 66);
				else if (results.getQueryResultType().getName().equals("PATIENT_ENCOUNTER_SET"))
					assertEquals(results.getSetSize(), 66);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	@Test
	public void SQP1I1_Circulatory4TemporalSame() throws Exception {
		String filename = testFileDir + "/SQP1I1_Circulatory_Same_[66]_3016ms.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull(masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
					assertEquals(results.getSetSize(), 66);
				else if (results.getQueryResultType().getName().equals("PATIENTSET"))
					assertEquals(results.getSetSize(), 66);
				else if (results.getQueryResultType().getName().equals("PATIENT_ENCOUNTER_SET"))
					assertEquals(results.getSetSize(), 66);
				else
					assertTrue(false);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void AllTemporalTests() throws Exception {
		//		String filename = testFileDir + "/SQP1I1_Circulatory_Same_[66]_3016ms.xml";
		try { 
			File f = new File(testFileDir + "/temporal");
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));

			for (String filename: names) {
				if (filename.startsWith("QT_SI_P")) {
					int result = Integer.parseInt(filename.substring(filename.indexOf('[')+1,filename.indexOf(']') ));
					filename = testFileDir + "/temporal/" + filename;
					String requestString = getQueryString(filename);
					OMElement requestElement = convertStringToOMElement(requestString); 
					OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

					//read test file and store query instance ;
					//unmarshall this response string 
					JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
					ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
					JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

					MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

					assertNotNull(masterInstanceResult);
					for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
					{
						if (results.getQueryResultType().getName().equals("PATIENT_COUNT_XML"))
							assertEquals("Working on: " + filename, results.getSetSize(), result);
						else
							assertTrue(false);
					}
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	public static RequestMessageType buildRequestMessage(PsmQryHeaderType requestHeaderType, RequestType requestType) {
		//create body type
		BodyType bodyType = new BodyType();
		ObjectFactory of = new ObjectFactory();
		bodyType.getAny().add(of.createPsmheader(requestHeaderType));
		bodyType.getAny().add(of.createRequest(requestType));
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageHeader(generateMessageHeader());
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setRequestHeader(generateRequestHeader());
		return requestMessageType;
	}

}
