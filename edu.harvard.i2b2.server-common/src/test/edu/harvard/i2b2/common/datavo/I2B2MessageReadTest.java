package edu.harvard.i2b2.common.datavo;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.core.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.datavo.i2b2message.RequestMessageType;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBElement;


public class I2B2MessageReadTest extends TestCase {
    public void testReadI2B2RequestMessage() throws Exception {
        RequestMessageType rmType = unMarshaller();
        System.out.println("Sending app name: " +
            rmType.getMessageHeader().getSendingApplication().getApplicationName());

        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        PatientDataType pdType = (PatientDataType) helper.getObjectByClass(rmType.getMessageBody()
                                                                                 .getAny(),
                PatientDataType.class);
        
        System.out.println("Observation blob " +
            pdType.getObservationFactSet().get(0).getObservationFact().get(0)
                  .getObservationBlob());

        JAXBElement je = (JAXBElement) rmType.getMessageBody().getAny().get(0);
        PatientDataType pd = (PatientDataType) je.getValue();
        System.out.println("Concept cd " +
            pd.getObservationFactSet().get(0).getObservationFact().get(0)
              .getConceptCd());
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
