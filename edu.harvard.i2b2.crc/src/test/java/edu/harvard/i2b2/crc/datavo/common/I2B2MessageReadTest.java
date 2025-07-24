/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.datavo.common;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.ejb.ExecRunnable;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.xml.bind.JAXBElement;


public class I2B2MessageReadTest extends TestCase {
	
	private static Log log = LogFactory.getLog(I2B2MessageReadTest.class);
	
    public void testReadI2B2RequestMessage() throws Exception {
    	
    	
    	
        RequestMessageType rmType = unMarshaller();
        log.debug("Sending app name: " +
            rmType.getMessageHeader().getSendingApplication().getApplicationName());

        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        PatientDataType pdType = (PatientDataType) helper.getObjectByClass(rmType.getMessageBody()
                                                                                 .getAny(),
                PatientDataType.class);
        
        /*log.debug("Observation blob " + 
            pdType.getObservationFactSet().get(0).getObservationFact().get(0)
                  .getObservationBlob());*/

        JAXBElement je = (JAXBElement) rmType.getMessageBody().getAny().get(0);
        PatientDataType pd = (PatientDataType) je.getValue();
        /*log.debug("Concept cd " +
            pd.getObservationFactSet().get(0).getObservationFact().get(0)
              .getConceptCd());*/
    }
    
    

    private String getPFTString() throws Exception {
        StringBuffer queryStr = new StringBuffer();
        DataInputStream dataStream = new DataInputStream(I2B2MessageReadTest.class.getResourceAsStream(
                    "Samplei2b2RequestMessage.xml"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    dataStream));
        String singleLine = null;

        while ((singleLine = reader.readLine()) != null) {
            queryStr.append(singleLine + "\n");
        }
        return queryStr.toString();
    }

    private RequestMessageType unMarshaller() throws Exception {
        String[] pak = new String[] {
                "edu.harvard.i2b2.datavo.i2b2message",
                "edu.harvard.i2b2.core.datavo.pdo"
            };
        JAXBUtil util = new JAXBUtil(pak);
        RequestMessageType rmType = (RequestMessageType) util.unMashallFromString(getPFTString())
                                                             .getValue();

        return rmType;
    }
}
