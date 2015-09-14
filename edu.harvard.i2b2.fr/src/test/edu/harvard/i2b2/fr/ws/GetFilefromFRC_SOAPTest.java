/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Bill Wang
 */

package edu.harvard.i2b2.fr.ws;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
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
import org.apache.axis2.wsdl.WSDLConstants;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.activation.DataHandler;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * FR client test  
 */
public class GetFilefromFRC_SOAPTest {
    private static EndpointReference targetEPR = new EndpointReference(
    		"http://localhost:9090/i2b2/services/FRService/recvfileRequest");
//    		"http://localhost:9090/i2b2/services/FRService/getFileList");
    private static String requestXml = "/appdev/FRCalltoGetFile.xml";

    public static OMElement getVersion() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd",
                "tns");

        OMElement method = fac.createOMElement("getVersion", omNs);

        return method;
    }

    /**
     * Test code to generate a PFT requestPdo for a test sample and convert to
     * OMElement called by main below
     *
     * @param requestPdo
     *            String requestPdo to send to PFT web service
     * @return An OMElement containing the PFT web service requestPdo
     */
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

    /**
     * Test code to generate a PFT requestPdo String for a sample PFT report
     * called by main below
     *
     * @return A String containing the sample PFT report
     */
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

        // Log query string
//        System.out.println("queryStr " + queryStr);

        return queryStr.toString();
    }

    /**
     * Test code to generate a PFT requestPdo based on a sample report and make
     * a PFT web service call PFT Response is printed out to console.
     *
     */
    public static void main2(String[] args) {
        try {
            OMElement getRequestElmt = getRequestPayLoad();
            Options options = new Options();
            options.setTo(targetEPR);

            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
            options.setTimeOutInMilliSeconds(50000);
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);

             
            OMElement result = sender.sendReceive(getRequestElmt);
            

            if (result == null) {
                System.out.println("result is null");
            } else {
                String response = result.toString();
                System.out.println("response = " + response);
                OMElement ele = result.getFirstElement();
                OMText binaryNode = (OMText) ele.getFirstOMChild();
                
                // Retrieving the DataHandler & then do whatever the processing to the data
                DataHandler actualDH;
                actualDH = (DataHandler) binaryNode.getDataHandler();       
                
                File receivedFile = new File("/appdev/received_from_FRC.txt");
                FileOutputStream outputStream = new FileOutputStream(receivedFile);
                actualDH.writeTo(outputStream);
            }
            System.exit(0);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
        	System.out.println(" Happy to be here! ");
            OMElement getRequestElmt = getRequestPayLoad();
            Options options = new Options();
            options.setTo(targetEPR);
			options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
//			options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);

			// Increase the time out to receive large attachments
			options.setTimeOutInMilliSeconds(900000);

			options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
                    Constants.VALUE_TRUE);
			options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, "/appdev");
			options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");

			ServiceClient sender = new ServiceClient();

			sender.setOptions(options);
			OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

//			SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
			SOAPFactory sfac = OMAbstractFactory.getSOAP12Factory();
			SOAPEnvelope env = sfac.getDefaultEnvelope();
			env.getBody().addChild(getRequestElmt);
			MessageContext mc = new MessageContext();
			mc.setEnvelope(env);
			mc.setDoingMTOM(false);
			mc.setDoingSwA( true );
			
			mepClient.addMessageContext(mc);
			
			mepClient.execute(true);
			
			MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

			for (String cid: response.getAttachmentMap().getAllContentIDs()) {
				if (cid.indexOf("@apache.org")!=-1) {
					DataHandler dataHandler = response.getAttachment(cid);
					System.out.println(dataHandler.getContent().toString());
					continue;
				}

				DataHandler dataHandler = response.getAttachment(cid);
		        if (dataHandler!=null){
					// Writing the attachment data (graph image) to a file
					File receivedFile = new File("/appdev/" + cid);
					
					FileOutputStream outputStream = new FileOutputStream(receivedFile);
					dataHandler.writeTo(outputStream);
					outputStream.flush();
					System.out.println("Downloaded file saved to :" + receivedFile.getAbsolutePath());
		        }else {
		        	System.out.println("no attachments received.");
		            throw new Exception("Cannot find the data handler.");
		        }
			}
		} catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
