package edu.harvard.i2b2.crc.loader.delegate.fr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import edu.harvard.i2b2.common.util.axis2.ServiceClient;

import org.apache.axiom.attachments.Attachments;
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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
//import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.loader.datavo.fr.ObjectFactory;
import edu.harvard.i2b2.crc.loader.datavo.fr.RecvfileRequestType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

/**
 * 
 * @author rk903
 * 
 */
public class CallFileRepositoryUtil {

	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	private SecurityType securityType = null;
	private String projectId = null;
	private String frUrl = null;

	public CallFileRepositoryUtil(String requestXml) throws JAXBUtilException,
			I2B2Exception {
		JAXBElement responseJaxb = CRCLoaderJAXBUtil.getJAXBUtil()
				.unMashallFromString(requestXml);
		RequestMessageType request = (RequestMessageType) responseJaxb
				.getValue();
		this.securityType = request.getMessageHeader().getSecurity();
		this.projectId = request.getMessageHeader().getProjectId();
		this.frUrl = CRCLoaderUtil.getInstance().getFileManagentCellUrl();

	}

	public CallFileRepositoryUtil(SecurityType securityType, String projectId)
			throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.frUrl = CRCLoaderUtil.getInstance().getFileManagentCellUrl();
		log.debug("file repository url " + frUrl);
	}

	public CallFileRepositoryUtil(String frUrl, SecurityType securityType,
			String projectId) throws I2B2Exception {
		this(securityType, projectId);
		this.frUrl = frUrl;
	}

	public String callFileRepository(int uploadId, String fileRepositoryFileName)
			throws I2B2Exception, AxisFault, JAXBUtilException {
		String localFileName = null;
		RequestMessageType requestMessageType = getI2B2RequestMessage(fileRepositoryFileName);
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("FileRespository request message [" + requestElement + "]");
		MessageContext response = getResponseSOAPBody(requestElement);
		localFileName = writeFileFromResponse(uploadId, response);
		return localFileName;
	}

	private ResultStatusType getI2B2ResponseStatus(OMElement response)
			throws JAXBUtilException {
		JAXBElement responseJaxb = CRCLoaderJAXBUtil.getJAXBUtil()
				.unMashallFromString(response.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		return r.getResponseHeader().getResultStatus();
	}

	private OMElement buildOMElement(RequestMessageType requestMessageType)
			throws I2B2Exception {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		OMElement request = null;
		try {
			CRCLoaderJAXBUtil.getJAXBUtil().marshaller(
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

	private RequestMessageType getI2B2RequestMessage(String recvFileName) {
		CRCLoaderUtil queryUtil = CRCLoaderUtil.getInstance();
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
		RecvfileRequestType recvFileRequestType = new RecvfileRequestType();
		recvFileRequestType.setFilename(recvFileName);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createRecvfileRequest(recvFileRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	public String writeFileFromResponse(int uploadId, MessageContext response)
			throws I2B2Exception {
		String localFileName = null;
		Attachments attach = response.getAttachmentMap(); //
		// String[] ids = attach.getAllContentIDs();
		SOAPBody body = response.getEnvelope().getBody();
		if (attach != null) {
			String attachmentName = CRCLoaderUtil.getInstance()
					.getFileRepositoryAttachmentName();

			javax.activation.DataHandler dataHandler2 = response
					.getAttachment(attachmentName);
			String processFolderName = CRCLoaderUtil.getInstance()
					.getProcessFolderName();
			localFileName = processFolderName + File.separatorChar + uploadId
					+ ".xml";
			log.debug("Local file name [" + localFileName + "]");
			// Writing the attachment data to a file
			File graphFile = new File(localFileName);
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(graphFile);
				dataHandler2.writeTo(outputStream);
				outputStream.flush();
			} catch (FileNotFoundException e) {
				throw new I2B2Exception("Error creating local file ["
						+ localFileName + "] from file repository ", e);
			} catch (IOException e) {
				throw new I2B2Exception(
						"IOException while creating local file ["
								+ localFileName + "] from file repository", e);
			}
		} else {
			throw new I2B2Exception(
					"Unable to create local file using file repository response");
		}
		return localFileName;
	}

	private MessageContext getResponseSOAPBody(OMElement requestElement)
			throws I2B2Exception, AxisFault, JAXBUtilException {
		MessageContext response = null;
		// call

		
	//	ServiceClient serviceClient = new ServiceClient();

		Options options = new Options();
		String frOperationName = CRCLoaderUtil.getInstance()
				.getFileRepositoryOperationName();
		if (frOperationName == null) {
			throw new I2B2Exception(
					"File Repository operation property missing from the property file");
		}
		
		
	  response = ServiceClient.getSOAPFile(frUrl + "recvfileRequest", requestElement, frOperationName, CRCLoaderUtil.getInstance().getFileRepositoryTimeout() );

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
					"Unable to fetch file from file repository ["
							+ errorMsg + "]");
		}

		
		
		/*
		log.debug("File Repository operation property value ["
				+ frOperationName + "]");
		options.setAction(frOperationName);
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		String timeout = CRCLoaderUtil.getInstance().getFileRepositoryTimeout();
		log.debug("File Repository timeout property value [" + timeout + "]");
		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(Integer.parseInt(timeout));
        options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);

		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,
				CRCLoaderUtil.getInstance().getFileRepositoryTempSpace());
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD,
				CRCLoaderUtil.getInstance().getFileRepositoryThreshold());
		options.setTo(new EndpointReference(frUrl));
		serviceClient.setOptions(options);

		try {
			OperationClient mepClient = serviceClient
					.createClient(ServiceClient.ANON_OUT_IN_OP);
			MessageContext mc = new MessageContext();

			//SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
			SOAPFactory sfac = OMAbstractFactory.getSOAP12Factory();

			SOAPEnvelope env = sfac.getDefaultEnvelope();
			env.getBody().addChild(requestElement);

			// SOAPEnvelope env = createEnvelope("fileattachment");
			mc.setEnvelope(env);
			// mc.addAttachment("contentID",dataHandler);
			mc.setDoingSwA(true);
			mc.setDoingMTOM(false);

			mepClient.addMessageContext(mc);
			mepClient.execute(true);
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
						"Unable to fetch file from file repository ["
								+ errorMsg + "]");
			}

			OperationContext operationContext = mc.getOperationContext();
			MessageContext outMessageContext = operationContext
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			log
					.debug("File Repository response has ["
							+ outMessageContext.getAttachmentMap()
									.getAllContentIDs().length
							+ "] attachments");
			
			
		} catch (AxisFault axisFault) {
			throw new I2B2Exception(
					"Unable to fetch file from file repository :Axisfault ["
							+ axisFault.getCause().getMessage() + "]");
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception(
					"Unable to fetch file from file repository :Axisfault ["
							+ jaxbUtilEx.getMessage() + "]");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new I2B2Exception(
					"Unable to fetch file from file repository :Axisfault ["
							+ t.getMessage() + "]");
		}
		*/
		return response;

	}

}
