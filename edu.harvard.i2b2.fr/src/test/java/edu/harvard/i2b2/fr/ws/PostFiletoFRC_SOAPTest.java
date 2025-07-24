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
 *     Bill Wang
 */

package edu.harvard.i2b2.fr.ws;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
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

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * FR client test  
 */
public class PostFiletoFRC_SOAPTest {
    private static EndpointReference targetEPR = new EndpointReference(
    		"http://localhost:9090/i2b2/services/FRService/putFile");
    
    private static String requestXml = "/appdev/FRcallToPostFile.xml";

    
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

//        System.out.println("queryStr " + queryStr);

        return queryStr.toString();
    }

    /**
     * Test code to generate a PFT requestPdo based on a sample report and make
     * a PFT web service call PFT Response is printed out to console.
     *
     */
    public static void main(String[] args) {
        try {
            OMElement getRequestElmt = edu.harvard.i2b2.common.util.axis2.ServiceClient.getPayLoad(getRequestString());

            Options options = new Options();
            options.setTo(targetEPR);

            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
//            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
            options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            options.setTimeOutInMilliSeconds(50000000);
			options.setAction("urn:sendfileRequest");
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);

            OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);
            
			SOAPFactory sfac = OMAbstractFactory.getSOAP12Factory();
			SOAPEnvelope env = sfac.getDefaultEnvelope();
			env.getBody().addChild(getRequestElmt);
			MessageContext mc = new MessageContext();
			mc.setEnvelope(env);
            
            FileDataSource fileDataSource = new FileDataSource("/appdev/bigfileToFRC.zip");
            DataHandler dataHandler = new DataHandler(fileDataSource);
            mc.addAttachment(fileDataSource.getName(), dataHandler);
            
            fileDataSource = new FileDataSource("/appdev/file_to_FRC_copy.txt");
            dataHandler = new DataHandler(fileDataSource);
            mc.addAttachment(fileDataSource.getName(), dataHandler);
            
            Attachments attachments = mc.getAttachmentMap();
            System.out.println("# of attachments: " + attachments.getAllContentIDs().length);
           
            mepClient.addMessageContext(mc);
            mepClient.execute(true);
            System.exit(0);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
