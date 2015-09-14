package edu.harvard.i2b2.crc.delegate.ontology;
 
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptsType;
import edu.harvard.i2b2.crc.datavo.ontology.GetChildrenType;
import edu.harvard.i2b2.crc.datavo.ontology.GetModifierInfoType;
import edu.harvard.i2b2.crc.datavo.ontology.GetTermInfoType;
import edu.harvard.i2b2.crc.datavo.ontology.ModifierType;
import edu.harvard.i2b2.crc.datavo.ontology.ModifiersType;
import edu.harvard.i2b2.crc.datavo.ontology.ObjectFactory;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class CallOntologyUtil {

	//	private SecurityType securityType = null;
	//	private String projectId = null;
	//	private String ontologyUrl = null;

	private static Log log = LogFactory.getLog(CallOntologyUtil.class);

	private static JAXBUtil jaxbUtil =   CRCJAXBUtil.getJAXBUtil();


	public static ConceptType callOntology(String itemKey, SecurityType securityType,  String projectId, String ontologyUrl ) throws XMLStreamException,
	JAXBUtilException, AxisFault, I2B2Exception {
		RequestMessageType requestMessageType = getI2B2RequestMessage(itemKey, securityType, projectId.replaceAll("/", ""));
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("CRC Ontology call's request xml from callOntology:  " + requestElement);
		log.debug("URL: " + ontologyUrl);
		ConceptType conceptType = null;
		try {
			String response = ServiceClient.sendREST(ontologyUrl, requestElement);
			conceptType = getConceptFromResponse(response);
		} catch (Exception e)
		{

		}
		return conceptType;
	}

	public static ConceptsType callGetChildren(String itemKey, SecurityType securityType,  String projectId, String ontologyUrl )
			throws XMLStreamException, JAXBUtilException, AxisFault,I2B2Exception  {
		RequestMessageType requestMessageType = getChildrenI2B2RequestMessage(itemKey, securityType, projectId.replaceAll("/", ""));
		OMElement requestElement = buildOMElement(requestMessageType);
		ConceptsType conceptsType = null;
		log.debug("CRC Ontology call's request xml from callGetChildren: " + requestElement);
		log.debug("URL: " + ontologyUrl);
		try {
			String response = ServiceClient.sendREST(ontologyUrl, requestElement);
			conceptsType = getChildrenFromResponse(response);
		} catch (Exception e)
		{

		}
		return conceptsType;
	}

	public static ModifierType callGetModifierInfo(String modifierKey, String appliedPath, SecurityType securityType,  String projectId, String ontologyUrl )
			throws XMLStreamException, JAXBUtilException, AxisFault, I2B2Exception  {
		RequestMessageType requestMessageType = getModifierI2B2RequestMessage(modifierKey,appliedPath, securityType, projectId.replaceAll("/", ""));
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("CRC Ontology call's request xml from callGetModifierInfo: " + requestElement);
		log.debug("URL: " + ontologyUrl);
		ModifierType modifierType = null;
		try {
			String response = ServiceClient.sendREST(ontologyUrl, requestElement);
			modifierType = getModifierFromResponse(response);
		} catch (Exception e)
		{

		}
		return modifierType;
	}


	public static ConceptsType callGetChildrenWithHttpClient(String itemKey, SecurityType securityType,  String projectId )
			throws XMLStreamException, JAXBUtilException,I2B2Exception, HttpException, IOException  {
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(QueryProcessorUtil.getInstance().getOntologyUrl());

		client.setConnectionTimeout(8000);

		// Send any XML file as the body of the POST request

		// postMethod.setRequestBody(new FileInputStream(f));
		RequestMessageType requestMessageType = getChildrenI2B2RequestMessage(itemKey, securityType, projectId.replaceAll("/", ""));
		String requestXml = buildRequestXml(requestMessageType);
		postMethod.setRequestBody(requestXml);
		postMethod.setRequestHeader("Content-type",
				"text/xml; charset=ISO-8859-1");
		String responseXml = null;
		int statusCode1 = client.executeMethod(postMethod);
		responseXml = postMethod.getResponseBodyAsString();

		log.debug("CRC's Ontology call response xml " + responseXml);

		postMethod.releaseConnection();
		ConceptsType conceptsType = getChildrenFromResponse(responseXml);
		return conceptsType;
	}


	private static ModifierType getModifierFromResponse(String responseXml)
			throws JAXBUtilException, I2B2DAOException {
		JAXBElement responseJaxb =   
				jaxbUtil.unMashallFromString(responseXml); //CRCJAXBUtil.getJAXBUtil()
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml from ModiferType: " + responseXml);
		if (r.getResponseHeader() != null && r.getResponseHeader().getResultStatus() !=null) { 
			if (r.getResponseHeader().getResultStatus().getStatus().getType().equalsIgnoreCase("ERROR")) {
				throw new I2B2DAOException("Error when getting modifier from ontology [" + r.getResponseHeader().getResultStatus().getStatus().getValue() +"]");
			}
		}
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ModifiersType modifiersType = (ModifiersType) helper.getObjectByClass(r
				.getMessageBody().getAny(), ModifiersType.class);

		if (modifiersType != null && modifiersType.getModifier() != null
				&& modifiersType.getModifier().size() > 0) {
			return modifiersType.getModifier().get(0);
		} else {
			return null;
		}

	}

	private static ConceptsType getChildrenFromResponse(String responseXml)
			throws JAXBUtilException, I2B2DAOException {
		JAXBElement responseJaxb = 
				jaxbUtil.unMashallFromString(responseXml); //CRCJAXBUtil.getJAXBUtil()
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml from getChildrenFromResponse: " + responseXml);

		if (r.getResponseHeader() != null && r.getResponseHeader().getResultStatus() !=null) { 
			if (r.getResponseHeader().getResultStatus().getStatus().getType().equalsIgnoreCase("ERROR")) {
				throw new I2B2DAOException("Error when getting children from ontology [" + r.getResponseHeader().getResultStatus().getStatus().getValue() +"]");
			}
		}
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConceptsType conceptsType = (ConceptsType) helper.getObjectByClass(r
				.getMessageBody().getAny(), ConceptsType.class);
		return conceptsType;
	}

	private static ConceptType getConceptFromResponse(String response)
			throws JAXBUtilException, I2B2DAOException {
		JAXBElement responseJaxb =// CRCJAXBUtil.getJAXBUtil()
				jaxbUtil.unMashallFromString(response);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml from getConceptFromResponse: " + response);
		if (r.getResponseHeader() != null && r.getResponseHeader().getResultStatus() !=null) { 
			if (r.getResponseHeader().getResultStatus().getStatus().getType().equalsIgnoreCase("ERROR")) {
				throw new I2B2DAOException("Error when getting metadata from ontology [" + r.getResponseHeader().getResultStatus().getStatus().getValue() +"]");
			}
		}
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConceptsType conceptsType = (ConceptsType) helper.getObjectByClass(r
				.getMessageBody().getAny(), ConceptsType.class);
		if (conceptsType != null && conceptsType.getConcept() != null
				&& conceptsType.getConcept().size() > 0) {
			return conceptsType.getConcept().get(0);
		} else {
			return null;
		}

	}

	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		//CRCJAXBUtil.getJAXBUtil()
		jaxbUtil.marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement request = builder.getDocumentElement();
		return request;
	}

	private static String buildRequestXml(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		//CRCJAXBUtil.getJAXBUtil()
		jaxbUtil.marshaller(
				hiveof.createRequest(requestMessageType), strWriter);

		return strWriter.toString();
	}

	private static RequestMessageType getI2B2RequestMessage(String conceptPath, SecurityType securityType,  String projectId ) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType = (MessageHeaderType) queryUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		GetTermInfoType getTermInfo = new GetTermInfoType();
		getTermInfo.setSelf(conceptPath);
		// max="300" hiddens="false" synonyms="false" type="core" blob="true"
		getTermInfo.setMax(300);
		getTermInfo.setHiddens(true);
		getTermInfo.setSynonyms(false);
		getTermInfo.setType("core");
		getTermInfo.setBlob(true);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetTermInfo(getTermInfo));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private static RequestMessageType getChildrenI2B2RequestMessage(String conceptPath, SecurityType securityType,  String projectId ) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType = (MessageHeaderType) queryUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		GetChildrenType getChildren = new GetChildrenType();
		getChildren.setParent(conceptPath);
		// max="300" hiddens="false" synonyms="false" type="core" blob="true"
		// getChildren.setMax(300);
		getChildren.setHiddens(true);
		getChildren.setSynonyms(false);
		// getChildren.setType("core");
		getChildren.setBlob(true);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetChildren(getChildren));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private static RequestMessageType getModifierI2B2RequestMessage(String modifierPath, String appliedPath, SecurityType securityType,  String projectId ) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType = (MessageHeaderType) queryUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body

		GetModifierInfoType modifierInfoType = new GetModifierInfoType(); 
		modifierInfoType.setSelf(modifierPath);
		modifierInfoType.setAppliedPath(appliedPath);

		// max="300" hiddens="false" synonyms="false" type="core" blob="true"
		// getChildren.setMax(300);
		modifierInfoType.setHiddens(true);
		modifierInfoType.setBlob(true);
		modifierInfoType.setSynonyms(false);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetModifierInfo(modifierInfoType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}


}
