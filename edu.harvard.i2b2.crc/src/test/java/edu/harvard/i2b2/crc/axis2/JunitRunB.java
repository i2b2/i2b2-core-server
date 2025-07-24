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
import org.junit.runner.JUnitCore;
import org.junit.experimental.ParallelComputer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.xml.bind.JAXBElement;

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
import edu.harvard.i2b2.crc.datavo.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlResultType;

/**
 * Class to test different setfinder request's 
 * @author rkuttan
 */
public class JunitRunB  extends CRCAxisAbstract {

	private static QueryMasterType queryMaster = null; 
	private static QueryInstanceType queryInstance = null;
	private static MasterInstanceResultResponseType masterInstanceResult = null;
	private static String testFileDir = null;

	private static  String setfinderUrl = "http://127.0.0.1:9090/i2b2/services/QueryToolService/request";	


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
		return new JUnit4TestAdapter(JunitRunB.class);
	}

	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
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
