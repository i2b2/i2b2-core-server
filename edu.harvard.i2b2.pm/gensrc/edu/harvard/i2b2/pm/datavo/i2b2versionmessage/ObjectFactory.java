//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.17 at 02:57:35 PM EDT 
//


package edu.harvard.i2b2.pm.datavo.i2b2versionmessage;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.harvard.i2b2.pm.datavo.i2b2versionmessage package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Request_QNAME = new QName("http://www.i2b2.org/xsd/hive/msg/version/", "request");
    private final static QName _Response_QNAME = new QName("http://www.i2b2.org/xsd/hive/msg/version/", "response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.harvard.i2b2.pm.datavo.i2b2versionmessage
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResponseMessageType }
     * 
     */
    public ResponseMessageType createResponseMessageType() {
        return new ResponseMessageType();
    }

    /**
     * Create an instance of {@link RequestMessageType }
     * 
     */
    public RequestMessageType createRequestMessageType() {
        return new RequestMessageType();
    }

    /**
     * Create an instance of {@link ResponseMessageType.MessageBody }
     * 
     */
    public ResponseMessageType.MessageBody createResponseMessageTypeMessageBody() {
        return new ResponseMessageType.MessageBody();
    }

    /**
     * Create an instance of {@link RequestMessageType.MessageBody }
     * 
     */
    public RequestMessageType.MessageBody createRequestMessageTypeMessageBody() {
        return new RequestMessageType.MessageBody();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/hive/msg/version/", name = "request")
    public JAXBElement<RequestMessageType> createRequest(RequestMessageType value) {
        return new JAXBElement<RequestMessageType>(_Request_QNAME, RequestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/hive/msg/version/", name = "response")
    public JAXBElement<ResponseMessageType> createResponse(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_Response_QNAME, ResponseMessageType.class, null, value);
    }

}
