package edu.harvard.i2b2.common.datavo;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.core.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.datavo.i2b2message.MessageTypeType;
import edu.harvard.i2b2.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.datavo.i2b2message.ResponseMessageType;

import junit.framework.TestCase;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;


public class I2B2MessageWriteTest extends TestCase {
    private MessageHeaderType createMessageHeader() {
        MessageHeaderType messageHeader = new MessageHeaderType();
        messageHeader.setAcceptAcknowledgementType(new String("messageId"));

        MessageControlIdType mcIdType = new MessageControlIdType();
        mcIdType.setInstanceNum(1);
        mcIdType.setMessageNum("1");
        mcIdType.setSessionId("1");
        messageHeader.setMessageControlId(mcIdType);

        MessageTypeType messageTypeType = new MessageTypeType();
        messageTypeType.setEventType("eventype");
        messageTypeType.setMessageCode("messageCode");
        messageHeader.setMessageType(messageTypeType);

        ApplicationType appType = new ApplicationType();
        appType.setApplicationName("appname");
        appType.setApplicationVersion("1.0");
        messageHeader.setSendingApplication(appType);
        messageHeader.setReceivingApplication(appType);

        return messageHeader;
    }

    public RequestHeaderType createRequestHeader() {
        RequestHeaderType reqHeader = new RequestHeaderType();
        reqHeader.setResultWaittimeMs(1000);

        return reqHeader;
    }

    private BodyType createBodyWithPDO() {
        PatientDataMessageTest pdoTest = new PatientDataMessageTest();
        PatientDataType pdo = pdoTest.createPatientDataType();
        edu.harvard.i2b2.core.datavo.pdo.ObjectFactory of = new edu.harvard.i2b2.core.datavo.pdo.ObjectFactory();
        BodyType bodyType = new BodyType();
        bodyType.getAny().add(of.createPatientData(pdo));

        return bodyType;
    }

    private String getXMLString(JAXBElement<?> jaxbElement)
        throws Exception {
        JAXBUtil jaxbUtil = new JAXBUtil(new String[] {
                    "edu.harvard.i2b2.datavo.i2b2message",
                    "edu.harvard.i2b2.core.datavo.pdo"
                });
        StringWriter strWriter = new StringWriter();

        jaxbUtil.marshaller(jaxbElement, strWriter);

        return strWriter.toString();
    }

    public void testWritei2b2RequestMessage() throws Exception {
        RequestMessageType reqMsgType = new RequestMessageType();
        reqMsgType.setMessageHeader(createMessageHeader());
        reqMsgType.setMessageBody(createBodyWithPDO());
        reqMsgType.setRequestHeader(createRequestHeader());

        edu.harvard.i2b2.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.datavo.i2b2message.ObjectFactory();
        JAXBElement<?> jaxbElement = of.createRequest(reqMsgType);
        String xmlMessage = getXMLString(jaxbElement);
        System.out.println("Request Message");
        System.out.println(xmlMessage);
    }

    public void testWritei2b2ResponseMessage() throws Exception {
    	ResponseMessageType resMsgType = new ResponseMessageType();
    	resMsgType.setMessageHeader(createMessageHeader());
    	resMsgType.setMessageBody(createBodyWithPDO());
    	
    	edu.harvard.i2b2.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.datavo.i2b2message.ObjectFactory();
        JAXBElement<?> jaxbElement = of.createResponse(resMsgType);
        String xmlMessage = getXMLString(jaxbElement);
        System.out.println("Response Message");
        System.out.println(xmlMessage);
    }
}
