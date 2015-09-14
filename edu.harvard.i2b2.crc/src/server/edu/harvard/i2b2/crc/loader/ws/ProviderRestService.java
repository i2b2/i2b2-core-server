package edu.harvard.i2b2.crc.loader.ws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.delegate.GetLoadStatusRequestHandler;
import edu.harvard.i2b2.crc.loader.delegate.GetMissingTermRequestHandler;
import edu.harvard.i2b2.crc.loader.delegate.LoaderQueryRequestDelegate;
import edu.harvard.i2b2.crc.loader.delegate.PublishDataRequestHandler;

/**
 * Test a Provider<Source>
 * 
 * @see
 * @author rkuttan
 * 
 */

//@WebServiceProvider(serviceName = "ProviderService", portName = "ProviderPort", targetNamespace = "http://org.jboss.ws/provider", wsdlLocation = "WEB-INF/wsdl/Provider.wsdl")
//@ServiceMode(value = Service.Mode.PAYLOAD)

// - PAYLOAD is implicit
public class ProviderRestService {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	public OMElement publishDataRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			PublishDataRequestHandler handler = new PublishDataRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);
			responseElement = buildOMElementFromString(response);

		} catch (XMLStreamException e) {
			log.error("xml stream exception", e);
		} catch (I2B2Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;
	}

	public OMElement getLoadDataStatusRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			GetLoadStatusRequestHandler handler = new GetLoadStatusRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);
			responseElement = buildOMElementFromString(response);

		} catch (XMLStreamException e) {
			log.error("xml stream exception", e);
		} catch (I2B2Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;
	}

	
	public OMElement getMissingTermRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			
			
			GetMissingTermRequestHandler handler = new GetMissingTermRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);
			responseElement = buildOMElementFromString(response);

		} catch (XMLStreamException e) {
			log.error("xml stream exception", e);
		} catch (I2B2Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;
	}
	
	public OMElement mtomSample(OMElement element) throws Exception {
		OMElement _fileNameEle = null;
		OMElement _imageElement = null;

		System.out.println("server side request element " + element);

		MessageContext inMessageContext = MessageContext
				.getCurrentMessageContext();
		OperationContext operationContext = inMessageContext
				.getOperationContext();

		System.out.println(inMessageContext.isServerSide() + " "
				+ inMessageContext.isDoingREST() + "  "
				+ inMessageContext.isDoingSwA());
		;
		DataHandler dataHandler = inMessageContext
				.getAttachment("fileattachment");
		if (dataHandler == null) {
			System.out.println("attachement is null");
		} else {
			System.out.println("attachement is not null cool");
		}
		System.out
				.println("Attachment length "
						+ inMessageContext.getAttachmentMap()
								.getAllContentIDs().length);

		// OMText firstText = (OMText) element.getFirstOMChild();
		// System.out.println(firstText);

		OMText binaryNode = (OMText) (element.getFirstElement())
				.getFirstOMChild();

		// OMText binaryNode = firstText;
		binaryNode.setOptimize(true);
		System.out.println("server side request element1 ");
		DataHandler actualDH;
		// actualDH = (DataHandler) binaryNode.getDataHandler();
		actualDH = dataHandler;
		System.out.println("server side request element2 ");
		ByteArrayInputStream is = (ByteArrayInputStream) actualDH
				.getDataSource().getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String fileSaveName = "FileAttachment";
		File file = new File(fileSaveName);
		PrintWriter filetmp = new PrintWriter(new BufferedWriter(
				new FileWriter(file, true)));
		String ligne = null;
		while ((ligne = br.readLine()) != null)
			filetmp.println(ligne);
		filetmp.flush();
		filetmp.close();

		// setting response

		MessageContext outMessageContext = operationContext
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
		String attachmentfilename = "/crcapp/jdbc.properties";
		javax.activation.DataHandler outDataHandler = new javax.activation.DataHandler(
				new FileDataSource(attachmentfilename));

		outMessageContext.addAttachment(outDataHandler);
		outMessageContext.addAttachment("f2", outDataHandler);

		return element;
	}

	/**
	 * Function constructs OMElement for the given String
	 * 
	 * @param xmlString
	 * @return OMElement
	 * @throws XMLStreamException
	 */
	private OMElement buildOMElementFromString(String xmlString)
			throws XMLStreamException {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		StringReader strReader = new StringReader(xmlString);
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement element = builder.getDocumentElement();
		return element;
	}

}