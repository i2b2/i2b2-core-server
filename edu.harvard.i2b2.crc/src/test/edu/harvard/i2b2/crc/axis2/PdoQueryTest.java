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
	private static String setfinderTargetEPR = 
			"http://127.0.0.1:9090/i2b2/rest/QueryToolService/request";			

	private static String pdoTargetEPR = 
			"http://127.0.0.1:9090/i2b2/services/QueryToolService/pdorequest";			
	
	

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
		return new JUnit4TestAdapter(PdoQueryTest.class);
	}

	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}



	@Test
	public void pdo_onemodifier() throws Exception {
		String filename = testFileDir + "/pdo_onemodifier.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			
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
					assertEquals("Checking patient 1000000003", results.getStartDate().toString(), "2007-05-23T00:00:00.000-04:00");
				}
			}
			assertTrue(found);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	
	@Test
	public void pdo_minvalue() throws Exception {
		String filename = testFileDir + "/pdo_minvalue.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			
			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			
			boolean found = false;
			int count = 0;
			for (ObservationType results : patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation() )
			{
				if (results.getPatientId().getValue().equals("1000000026") && results.getEventId().getValue().equals("475614") &&
						results.getConceptCd().getValue().equals("LOINC:2086-7"))
				{
					found = true;
					assertEquals("Checking patient 1000000026", results.getNvalNum().getValue().toPlainString(), "36");
					count++;
				}
			}
			assertEquals("Checking patient 1000000026 had 2 records", count, 2); 
			assertTrue(found);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}


	@Test
	public void pdo_maxvalue() throws Exception {
		String filename = testFileDir + "/pdo_maxvalue.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			
			PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
			//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
			//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
			assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
			System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());

			boolean found = false;
			for (ObservationType results : patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation() )
			{
				if (results.getPatientId().getValue().equals("1000000001"))
				{
					found = true;
					assertEquals("Checking patient 1000000001", results.getNvalNum().getValue().toPlainString(), "160");
				}
			}
			assertTrue(found);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void pdo_firstvalue() throws Exception {
		String filename = testFileDir + "/pdo_firstvalue.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			
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
			}
			assertTrue(found);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	@Test
	public void pdo_lastvalue() throws Exception {
		String filename = testFileDir + "/pdo_lastvalue.xml";
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);

			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();


			
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
					assertEquals("Checking patient 1000000003", results.getStartDate().toString(), "2007-05-23T00:00:00.000-04:00");
				}
			}
			assertTrue(found);

		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	/*
	
	@BeforeClass public  static void runQueryInstanceFromQueryDefinition() throws Exception  {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);
		if (!(testFileDir != null && testFileDir.trim().length()>0)) {
			throw new Exception("please provide test file directory info -Dtestfiledir");
		}
		//read test file and store query master;
		String filename = testFileDir +"/setfinder_query.xml";
		try { 
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);
		
		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);
		queryResultInstance = masterInstanceResult.getQueryResultInstance().get(0);
		assertNotNull(queryResultInstance);
		System.out.println(queryResultInstance.getResultInstanceId());
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
		//queryResultInstance = new  QueryResultInstanceType();
		//queryResultInstance.setResultInstanceId("4801");
		
		
	}
	
	@Test public void testPatienSetId() throws Exception {
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType(); 
		InputOptionListType inputType = getInputOptionType();

		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		
		GetPDOFromInputListRequestType pdoRequestType = new GetPDOFromInputListRequestType();
		pdoRequestType.setFilterList(filterType);
		pdoRequestType.setInputList(inputType);
		pdoRequestType.setOutputOption(ouputType);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,pdoRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		System.out.println(responseElement.toString());
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		//StatusType.Condition condition = patientDataResponseType.getStatus().getCondition().get(0);
		//assertEquals(condition.getType(),"DONE","checking crc message status 'DONE'");
		assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
		System.out.println(patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().get(0).getPatientId().getSource());
	}
	
	@Test public void testWholePatient() throws Exception  {
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType(); 
		InputOptionListType inputType = getInputOptionType();
		
		inputType.getPatientList().setEntirePatientSet(true);
		inputType.getPatientList().setPatientSetCollId(null);
		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		
		GetPDOFromInputListRequestType pdoRequestType = new GetPDOFromInputListRequestType();
		pdoRequestType.setFilterList(filterType);
		pdoRequestType.setInputList(inputType);
		pdoRequestType.setOutputOption(ouputType);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,pdoRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		assertTrue("checking patient set size > 0 ",patientDataResponseType.getPatientData().getPatientSet().getPatient().size()>0);
	}
	
	@Test public void testConceptFilter() throws Exception {
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType();
		
		InputOptionListType inputType = getInputOptionType();

		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		
		GetPDOFromInputListRequestType pdoRequestType = new GetPDOFromInputListRequestType();
		pdoRequestType.setFilterList(filterType);
		pdoRequestType.setInputList(inputType);
		pdoRequestType.setOutputOption(ouputType);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,pdoRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		assertTrue("checking concept set size > 0",patientDataResponseType.getPatientData().getConceptSet().getConcept().size()>0);
		assertTrue("checking observatiob set size > 0",patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().size()>0);
		
	}
	
	@Ignore
	@Test public void testProviderFilter() throws Exception  {
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType(); 
		InputOptionListType inputType = getInputOptionType();

		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		
		GetPDOFromInputListRequestType pdoRequestType = new GetPDOFromInputListRequestType();
		pdoRequestType.setFilterList(filterType);
		pdoRequestType.setInputList(inputType);
		pdoRequestType.setOutputOption(ouputType);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,pdoRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		assertTrue("checking observer set size > 0",patientDataResponseType.getPatientData().getObserverSet().getObserver().size()>0);
		assertTrue("checking observation set size > 0",patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().size()>0);
	}
	
	@Test public void testNameAsAttributesOutput() throws Exception { 
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType();
		
		InputOptionListType inputType = getInputOptionType();

		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		
		GetPDOFromInputListRequestType pdoRequestType = new GetPDOFromInputListRequestType();
		pdoRequestType.setFilterList(filterType);
		pdoRequestType.setInputList(inputType);
		pdoRequestType.setOutputOption(ouputType);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,pdoRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		assertTrue("checking observation set size > 0",patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().size()>0);
		
	}
	
	@Test public void testFactRelatedOutput() throws Exception {
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType(); 
		InputOptionListType inputType = getInputOptionType();

		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		
		GetPDOFromInputListRequestType pdoRequestType = new GetPDOFromInputListRequestType();
		pdoRequestType.setFilterList(filterType);
		pdoRequestType.setInputList(inputType);
		pdoRequestType.setOutputOption(ouputType);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,pdoRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		assertTrue("checking observation set size > 0",patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().size()>0);
	}
    @Ignore
	@Test public void testFactPrimaryKey() throws Exception {
		FilterListType filterType = getFilterListType();
		OutputOptionListType ouputType = getOutputOptionListType(); 
		InputOptionListType inputType = getInputOptionType();

		
		PdoQryHeaderType requestHeaderType = new PdoQryHeaderType();
		requestHeaderType.setRequestType(PdoRequestTypeType.GET_OBSERVATIONFACT_BY_PRIMARY_KEY);
		

		GetObservationFactByPrimaryKeyRequestType observationReqType = new GetObservationFactByPrimaryKeyRequestType();
		FactPrimaryKeyType factPrimaryKey = new FactPrimaryKeyType();
        
		factPrimaryKey.setConceptCd("ICD9:410.9");
		factPrimaryKey.setEventId("1000000011");
		factPrimaryKey.setModifierCd("@");
		factPrimaryKey.setObserverId("@");
		factPrimaryKey.setPatientId("1000000011");
		//1999-09-24T00:00:00.000-04:00
		GregorianCalendar gc = new GregorianCalendar(1999, 9, 24, 0,0, 0);
		DatatypeFactory df = DatatypeFactory.newInstance(); 
		XMLGregorianCalendar cal =  df.newXMLGregorianCalendar(gc);
		factPrimaryKey.setStartDate(cal);
		observationReqType.setFactPrimaryKey(factPrimaryKey);
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,observationReqType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(pdoTargetEPR).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		PatientDataResponseType patientDataResponseType = (PatientDataResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),PatientDataResponseType.class);
		assertTrue("checking observation set size > 0",patientDataResponseType.getPatientData().getObservationSet().get(0).getObservation().size()>0);
	}
	
	
	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(PdoQueryTest.class);
	}

	
	
		
	
	
	private InputOptionListType getInputOptionType() {
		InputOptionListType inputOptionListType = new InputOptionListType();
		
		PatientListType patientListType = new PatientListType();
		patientListType.setMax(new Integer(15));
		patientListType.setMin(new Integer(0));
		patientListType.setPatientSetCollId(queryResultInstance.getResultInstanceId());
		inputOptionListType.setPatientList(patientListType);
		return inputOptionListType;
	}
	
	private OutputOptionListType getOutputOptionListType() {
		OutputOptionListType outputOptionListType = new OutputOptionListType();
		//outputOptionListType.setNames(OutputOptionNameType.ASATTRIBUTES);
		OutputOptionType outputOptionType = new OutputOptionType(); 
		outputOptionType.setOnlykeys(false);
		outputOptionType.setSelect(OutputOptionSelectType.USING_INPUT_LIST);
		outputOptionListType.setPatientSet(outputOptionType);
		FactOutputOptionType  factOutputOptionType = new FactOutputOptionType();
		factOutputOptionType.setOnlykeys(false);
		outputOptionListType.setObservationSet(factOutputOptionType);
		outputOptionListType.setConceptSetUsingFilterList(outputOptionType);
		return outputOptionListType;
	}
	
	private FilterListType getFilterListType() {
		FilterListType filterListType = new FilterListType();
		PanelType panelType = new PanelType();
		panelType.setName("panel1");
		ItemType itemType = new ItemType();
		itemType.setDimDimcode("\\i2b2\\Diagnoses");
		itemType.setDimTablename("concept_dimension");
	//	itemType.setFacttablecolumn("concept_cd");
//		itemType.setDimColumnname("concept_path");
		panelType.getItem().add(itemType);
		itemType.setItemKey("\\i2b2\\Diagnoses");
		panelType.getItem().add(itemType);
		filterListType.getPanel().add(panelType);
		
		
		return filterListType;
	}
	
	public static RequestMessageType buildRequestMessage(PdoQryHeaderType requestHeaderType, RequestType requestType) {
		//create body type
		BodyType bodyType = new BodyType();
		ObjectFactory of = new ObjectFactory();
		bodyType.getAny().add(of.createPdoheader(requestHeaderType));
		bodyType.getAny().add(of.createRequest(requestType));
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageHeader(generateMessageHeader());
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setRequestHeader(generateRequestHeader());
		return requestMessageType;
	}
	
	*/

	
	
}
