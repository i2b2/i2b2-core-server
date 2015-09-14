package edu.harvard.i2b2.ontology.delegate.fr;

import java.io.StringReader;
import java.io.StringWriter;

import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.fr.ObjectFactory;
import edu.harvard.i2b2.ontology.datavo.fr.SendfileRequestType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

/**
 * 
 * @author rk903
 * 
 */
public class CallFileRepositoryUtil {

	/** log **/
	protected static Log log = LogFactory.getLog(CallFileRepositoryUtil.class);

	//private SecurityType securityType = null;
	//private String projectId = null;
	private static String frUrl = null;
	private static OntologyUtil ontologyUtil = OntologyUtil.getInstance();

	
	public static String callFileRepository(String fileRepositoryFileName,  SecurityType securityType,  String projectId)
			throws I2B2Exception {
		String localFileName = null;
		RequestMessageType requestMessageType = getI2B2RequestMessage(fileRepositoryFileName, securityType, projectId);
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("FileRespository request message ["
				+ requestElement.toString() + "]");
		// MessageContext response = getResponseSOAPBody(requestElement);
		uploadConceptFile(fileRepositoryFileName, securityType, projectId);
		return fileRepositoryFileName;
	}

	private static ResultStatusType getI2B2ResponseStatus(OMElement response)
			throws JAXBUtilException {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(response.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		return r.getResponseHeader().getResultStatus();
	}

	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws I2B2Exception {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory();
		OMElement request = null;
		try {
			OntologyJAXBUtil.getJAXBUtil().marshaller(
					hiveof.createRequest(requestMessageType), strWriter);
			StringReader strReader = new StringReader(strWriter.toString());
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);
			StAXOMBuilder builder = new StAXOMBuilder(reader);
			request = builder.getDocumentElement();
		} catch (XMLStreamException xmlEx) {
			throw new I2B2Exception("FileRepository request omelement failed ["
					+ xmlEx.getMessage() + "]");
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("FileRepository request omelement failed ["
					+ jaxbEx.getMessage() + "]");
		}
		return request;
	}

	private static RequestMessageType getI2B2RequestMessage(String sendFileName,  SecurityType securityType,  String projectId) {

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
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
		SendfileRequestType sendfileRequestType = new SendfileRequestType();
		edu.harvard.i2b2.ontology.datavo.fr.File sendFile = new edu.harvard.i2b2.ontology.datavo.fr.File();
		sendFile.setName(sendFileName);
		sendfileRequestType.setUploadFile(sendFile);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createSendfileRequest(sendfileRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private static void uploadConceptFile(String conceptFile,  SecurityType securityType,  String projectId)  throws I2B2Exception {
		org.apache.axis2.client.ServiceClient sender = null;
		try {
			
			Options options = new Options();
			options.setTo(new EndpointReference(frUrl));
			options.setAction("urn:sendfileRequest");
			options
					.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

			// Increase the time out to receive large attachments
			options.setTimeOutInMilliSeconds(10000);

			options.setProperty(Constants.Configuration.ENABLE_SWA,
					Constants.VALUE_TRUE);
			options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
					Constants.VALUE_TRUE);
			options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,
					"temp");
			options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD,
					"4000");

			sender = ServiceClient
					.getServiceClient();
			sender.setOptions(options);
			OperationClient mepClient = sender
					.createClient(org.apache.axis2.client.ServiceClient.ANON_OUT_IN_OP);

			MessageContext mc = new MessageContext();
			javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(
					new FileDataSource(conceptFile));

			mc.addAttachment("cid", dataHandler);
			mc.setDoingSwA(true);

			SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope env = sfac.getDefaultEnvelope();

			RequestMessageType requestMessageType = getI2B2RequestMessage(conceptFile, securityType, projectId);
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("File repository request message from ontology ["
					+ requestElement + "]");
			env.getBody().addChild(requestElement);

			// SOAPEnvelope env = createEnvelope("fileattachment");
			mc.setEnvelope(env);
			mepClient.addMessageContext(mc);
			mepClient.execute(true);

			MessageContext response = mepClient
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			SOAPBody body = response.getEnvelope().getBody();

			response = mepClient
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			log.debug("File Repository response envelope: "
					+ response.toString() + "]");
			OMElement frResponse = (OMElement) response.getEnvelope().getBody()
					.getFirstOMChild();
			log.debug("File Repository response body [: " + frResponse + "]");
			
			
			
			
			// read header status
			ResultStatusType resultStatusType = getI2B2ResponseStatus(frResponse);

			// if the status type is error, then throw i2b2exception
			if (resultStatusType.getStatus() != null
					&& resultStatusType.getStatus().getType() != null
					&& resultStatusType.getStatus().getType().equalsIgnoreCase(
							"error")) {
				String errorMsg = resultStatusType.getStatus().getValue();

				throw new I2B2Exception(
						"Unable to send file to file repository [" + errorMsg
								+ "]");
			}

		} catch (AxisFault axisFault) {
			throw new I2B2Exception(
					"Unable to send file to file repository :Axisfault ["
							+ axisFault.getCause().getMessage() + "]");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new I2B2Exception(
					"Unable to send file to file repository :Axisfault ["
							+ t.getMessage() + "]");
		} finally {
			if (sender != null) {
				try{
					sender.cleanupTransport();
					sender.cleanup();
				} catch (AxisFault e) {
					log.debug("Error .", e);
				}
			}
		}	
	}


}
