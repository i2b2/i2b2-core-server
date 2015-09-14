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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
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
 * Class to test different CRC Loader request's 
 * @author Mike Mendis
 */
public class CRCLoaderTest  extends CRCAxisAbstract {

	private static QueryMasterType queryMaster = null; 
	private static QueryInstanceType queryInstance = null;
	private static String masterInstanceResult = null;
	private static String testFileDir = null;
	 private static EndpointReference frUrl = new EndpointReference(
			 "http://infra6.mgh.harvard.edu:9090/i2b2/services/FRService");
	private static  String setfinderUrl = 
			//System.getProperty("testhost") 
			"http://infra6.mgh.harvard.edu:9090/i2b2/services"
			+ "/QueryToolService/request";	

	  private static String requestXml = "";
	  
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
		return new JUnit4TestAdapter(CRCLoaderTest.class);
	}

	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}
	
    public static String getRequestString() throws Exception {
        StringBuffer queryStr = new StringBuffer();
        DataInputStream dataStream = new DataInputStream(new FileInputStream(
                    requestXml));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    dataStream));
        String singleLine = null;

        while ((singleLine = reader.readLine()) != null) {
            queryStr.append(singleLine + "\n");
        }

//        System.out.println("queryStr " + queryStr);

        return queryStr.toString();
    }
    
    public static OMElement getRequestPayLoad() throws Exception {
        OMElement method = null;

        try {

            StringReader strReader = new StringReader(getRequestString());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(strReader);

            StAXOMBuilder builder = new StAXOMBuilder(reader);
            method = builder.getDocumentElement();

        } catch (FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            // No log because its a thread?
            e.printStackTrace();
            throw new Exception(e);
        }
        
        //System.out.println(method.toString());

        return method;
    }

	@Test
	public void UploadPatientSet() throws Exception {
		requestXml = testFileDir + "/import_ptMap.xml";
		try { 
			
			
			
			
            OMElement getRequestElmt = getRequestPayLoad();
            Options options = new Options();
            options.setTo(frUrl);

            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
//            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
            options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            options.setTimeOutInMilliSeconds(50000);
			options.setAction("urn:sendfileRequest");
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);

            OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);
            
			SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope env = sfac.getDefaultEnvelope();
			env.getBody().addChild(getRequestElmt);
			MessageContext mc = new MessageContext();
			mc.setEnvelope(env);
            
            FileDataSource fileDataSource = new FileDataSource( testFileDir + "/ptMap_8-15-2012.csv");
            DataHandler dataHandler = new DataHandler(fileDataSource);
            mc.addAttachment(fileDataSource.getName(), dataHandler);
            
          //  fileDataSource = new FileDataSource(testFileDir + "ptMap_8-15-2012.csv");
           // dataHandler = new DataHandler(fileDataSource);
           // mc.addAttachment(fileDataSource.getName(), dataHandler);
            
            Attachments attachments = mc.getAttachmentMap();
            System.out.println("# of attachments: " + attachments.getAllContentIDs().length);
           
            mepClient.addMessageContext(mc);
            mepClient.execute(true);			
			
            
    		MessageContext inMsgtCtx = mepClient.getMessageContext("In");
    		SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();
    		
    		OMElement soapResponse = responseEnv.getBody().getFirstElement();
    		
    		OMElement soapResult = soapResponse.getFirstElement();

    		String i2b2Response = soapResponse.toString();
			
    		
    		
    		System.out.println(i2b2Response);
			
    		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(i2b2Response);


			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			masterInstanceResult = (String)helper.getObjectByClass(r.getMessageBody().getAny(),String.class);
			assertNotNull(masterInstanceResult);
    		

			
			
			
			
			
			
			
			
			
			
			
			
			
			
			/*
			
			
			
			
			
			
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

			*/

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
