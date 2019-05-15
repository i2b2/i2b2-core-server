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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.axis2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;

import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FactOutputOptionType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FactPrimaryKeyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetObservationFactByPrimaryKeyRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionNameType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionSelectType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PdoRequestTypeType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RequestType;

import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;

/**
 * Class to test different pdo requests 
 * @author rkuttan
 */
public class PdoQueryTest extends CRCAxisAbstract {


	private static QueryResultInstanceType queryResultInstance = null;
	private  static String testFileDir = null;
	//:TODO accept server url as runtime parameter 
	private static String setfinderTargetEPR = null;	

	private static String pdoTargetEPR = null;
	//private static String patientSetId = null;


	@BeforeClass
	public static void setUp() throws Exception {
		String host = (System.getProperty("testhost") == null ? "http://127.0.0.1:9090/i2b2/services" : System.getProperty("testhost") ) ;
		setfinderTargetEPR = 
				host + "/QueryToolService/request";	
		pdoTargetEPR = 
				host + "/QueryToolService/pdorequest";	
		testFileDir = "testfiles"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}





	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(PdoQueryTest.class);
	}

	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}


	@Test
	public void GetPDOFromInputList_requestType_labs() throws Exception {


		try { 

			String filename = testFileDir + "/GetPDOFromInputList_requestType_Labs.xml";
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
			String patientSetId = null; 

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);

			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			//System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			boolean found = false;
			for (PatientType results : patientDataResponseType.getPatientData().getPatientSet().getPatient() )
			{
				if (found)
					break;
				if (results.getPatientId().getValue().equals("1000000001"))
				{

					for (ParamType params : results.getParam() )
					{

						if (params.getColumn().equals("language_cd")){

							assertEquals("Checking patient 1000000001 Language of english", params.getValue(), "english");
							found = true;
							break;
						}
					}


				}
			}
			assertTrue(found);
		} catch (Exception e2) { 
			System.out.println("Error in onemodifier:" + e2.getMessage());
			e2.printStackTrace();
			assertTrue(false);
		}
	}




	@Test
	public void pdo_onemodifier() throws Exception {


		try { 
			String filename = testFileDir + "/MQP1I1_[65]_2320ms.xml";
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);

			String patientSetId = null;
			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull("Did not get masterInstanceResult", masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENTSET"))
				{
					assertEquals(results.getSetSize(), 65);
					patientSetId = results.getResultInstanceId();
				}
			}

			assertNotNull("Did not get Patientsetid", patientSetId);

			System.out.println("My patientsetID: " + patientSetId);

			filename = testFileDir + "/pdo_onemodifier.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{patientSetId}", patientSetId);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();



			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			//System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			boolean found = false;
			for (PatientType results : patientDataResponseType.getPatientData().getPatientSet().getPatient() )
			{
				if (found)
					break;
				if (results.getPatientId().getValue().equals("1000000003"))
				{

					for (ParamType params : results.getParam() )
					{

						if (params.getColumn().equals("language_cd")){

							assertEquals("Checking patient 1000000003 Language of germae", params.getValue(), "german");
							found = true;
							break;
						}
					}


				}
			}
			assertTrue(found);

		} catch (Exception e2) { 
			System.out.println("Error in onemodifier:" + e2.getMessage());
			e2.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void pdo_minvalue() throws Exception {
		try { 
			String filename = testFileDir + "/MQP1I1_[65]_2320ms.xml";
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);

			String patientSetId = null;
			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull("Did not get masterInstanceResult", masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENTSET"))
				{
					assertEquals(results.getSetSize(), 65);
					patientSetId = results.getResultInstanceId();
				}
			}

			assertNotNull("Did not get Patientsetid", patientSetId);
			filename = testFileDir + "/pdo_minvalue.xml";


			System.out.println("My patientsetID: " + patientSetId);
			requestString = getQueryString(filename);
			requestString = requestString.replace("{patientSetId}", patientSetId);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();



			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			//System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			boolean found = false;
			int count = 0;
			for (ObservationType results : patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation() )
			{
				if (results.getPatientId().getValue().equals("1000000016") && results.getEventId().getValue().equals("473653") &&
						results.getConceptCd().getValue().equals("LOINC:2086-7"))
				{
					found = true;
					if (results.getNvalNum().getValue().toPlainString().equals("44"))				
						assertEquals("Checking patient 100000003", results.getNvalNum().getValue().toPlainString(), "44");
					else
						assertEquals("Checking patient 1000000016", results.getNvalNum().getValue().toPlainString(), "44.00000");
					count++;
				}
			}
			assertEquals("Checking patient 1000000016 had 2 records", count, 2); 
			assertTrue(found);
		} catch (Exception e) { 
			System.out.println("Error in minvalue:" + e.getMessage());

			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void pdo_maxvalue() throws Exception {

		try {

			String filename = testFileDir + "/MQP1I1_[65]_2320ms.xml";
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);

			String patientSetId = null;
			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull("Did not get masterInstanceResult", masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENTSET"))
				{
					assertEquals(results.getSetSize(), 65);
					patientSetId = results.getResultInstanceId();
				}
			}


			filename = testFileDir + "/pdo_maxvalue.xml";
			assertNotNull("Did not get Patientsetid", patientSetId);

			requestString = getQueryString(filename);
			requestString = requestString.replace("{patientSetId}", patientSetId);
			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();



			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);

			boolean found = false;
			for (ObservationType results : patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation() )
			{

				if (results.getPatientId().getValue().equals("1000000003") && results.getEventId().getValue().equals("474080") &&
						results.getConceptCd().getValue().equals("LOINC:2086-7"))
				{
					found = true;
					if (results.getNvalNum().getValue().toPlainString().equals("46"))
						assertEquals("Checking patient 100000003", results.getNvalNum().getValue().toPlainString(), "46");
					else
						assertEquals("Checking patient 100000003", results.getNvalNum().getValue().toPlainString(), "46.00000");
				}
			}
			assertTrue(found);

		} catch (Exception e) { 
			System.out.println("Error in maxvalue:" + e.getMessage());

			e.printStackTrace();
			assertTrue(false);
		}
	}



	@Test
	public void pdo_firstvalue_modtfalse() throws Exception {
		try {

			String filename = testFileDir + "/MQP1I1_[65]_2320ms.xml";
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);

			String patientSetId = null;

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull("Did not get masterInstanceResult", masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENTSET"))
				{
					assertEquals(results.getSetSize(), 65);
					patientSetId = results.getResultInstanceId();
				}
			}

			assertNotNull("Did not get Patientsetid", patientSetId);

			filename = testFileDir + "/pdo_firstvalue_modtfalse.xml";

			requestString = getQueryString(filename);
			requestString = requestString.replace("{patientSetId}", patientSetId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();



			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			boolean found = false;
			for (ObservationType results : patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation() )
			{
				if (results.getPatientId().getValue().equals("1000000003"))
				{
					found = true;
					assertEquals("Checking patient 1000000003", results.getStartDate().toString(), "1997-11-26T00:00:00.000-05:00");
				}
				if (found)
					break;
			}
			assertTrue("Did not find patient 1000000003", found);

		} catch (Exception e) { 
			System.out.println("Error in modtfalse:" + e.getMessage());

			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void pdo_lastvalue_modtfalse() throws Exception {
		try {
			String filename = testFileDir + "/MQP1I1_[65]_2320ms.xml";
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);
			String patientSetId = null;

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);

			assertNotNull("Did not get masterInstanceResult", masterInstanceResult);
			for (QueryResultInstanceType results :masterInstanceResult.getQueryResultInstance() )
			{
				if (results.getQueryResultType().getName().equals("PATIENTSET"))
				{
					assertEquals(results.getSetSize(), 65);
					patientSetId = results.getResultInstanceId();
				}
			}

			assertNotNull("Did not get Patientsetid", patientSetId);

			filename = testFileDir + "/pdo_lastvalue_modfalse.xml";
			requestString = getQueryString(filename);
			requestString = requestString.replace("{patientSetId}", patientSetId);

			requestElement = convertStringToOMElement(requestString); 
			responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			r = (ResponseMessageType)responseJaxb.getValue();
			helper = new  JAXBUnWrapHelper();



			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			boolean found = false;
			for (ObservationType results : patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation() )
			{
				if (results.getPatientId().getValue().equals("1000000003") && results.getConceptCd().getValue().equals("LOINC:2090-9"))
				{
					found = true;
					assertEquals("Checking patient 1000000003", results.getStartDate().toString(), "2005-12-09T00:00:00.000-05:00");
				}
				if (found)
					break;

			}
			assertTrue(found);

		} catch (Exception e) { 
			System.out.println("Error in lastmodtfalse:" + e.getMessage());

			e.printStackTrace();
			assertTrue(false);
		}
	}



}
