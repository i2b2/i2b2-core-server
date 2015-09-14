/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 *     Mike Mendis
 *     Bill Wang
 */
package edu.harvard.i2b2.fr.ws;

import java.io.StringReader;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.fr.delegate.RecvfileRequestHandler;
import edu.harvard.i2b2.fr.delegate.LoaderQueryRequestDelegate;
import edu.harvard.i2b2.fr.delegate.SendfileRequestHandler;

public class FileRepositoryService {
	protected final Log log = LogFactory.getLog(getClass());

	public OMElement sendfileRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			if (requestXml.indexOf("<soapenv:Body>") > -1) {
				requestXml = requestXml.substring(
						requestXml.indexOf("<soapenv:Body>") + 14,
						requestXml.indexOf("</soapenv:Body>"));
			}

			SendfileRequestHandler handler = new SendfileRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);

			responseElement = buildOMElementFromString(response, "");

		} catch (XMLStreamException e) {
			log.error("xml stream exception", e);
		} catch (I2B2Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;
	}

	public OMElement recvfileRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;

		FileDataSource fileDataSource;
		DataHandler fileDataHandler;

		try {
			String requestXml = request.toString();
			if (requestXml.indexOf("<soapenv:Body>") > -1) {
				requestXml = requestXml.substring(
						requestXml.indexOf("<soapenv:Body>") + 14,
						requestXml.indexOf("</soapenv:Body>"));
			}
			RecvfileRequestHandler handler = new RecvfileRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);

			String filename = handler.getFilename();
			// We can obtain the request (incoming) MessageContext as follows
			MessageContext inMessageContext = MessageContext
					.getCurrentMessageContext();
			// We can obtain the operation context from the request message
			// context
			OperationContext operationContext = inMessageContext
					.getOperationContext();
			MessageContext outMessageContext = operationContext
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			outMessageContext.setDoingSwA(true);
			outMessageContext.setDoingREST(false);
			if (!filename.equals("")) {
				fileDataSource = new FileDataSource(filename);
				fileDataHandler = new DataHandler(fileDataSource);
				// use requested filename as content ID
				outMessageContext.addAttachment(handler.getRequestedFilename(), fileDataHandler);
				outMessageContext.setDoingMTOM(false);
				outMessageContext.setDoingSwA(true);
				responseElement = buildOMElementFromString(response,
						fileDataHandler.getName());
			} else {
				log.error("where did the file go? ");
			}
		} catch (XMLStreamException e) {
			log.error("xml stream exception", e);
		} catch (I2B2Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;
	}

	private OMElement buildOMElementFromString(String xmlString,
			String contentID) throws XMLStreamException {
		OMElement returnElement = null;

		try {
			StringReader strReader = new StringReader(xmlString);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			returnElement = builder.getDocumentElement();

			OMFactory factory = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = factory.createOMNamespace(
					"http://www.i2b2.org/xsd", "swa");

			OMElement fileElement = factory.createOMElement("file", omNs,
					returnElement);
			fileElement.addAttribute("href", contentID, null);
		} catch (XMLStreamException ex) {
			log.error("Error while converting FR response PDO to OMElement", ex);
			throw ex;
		}

		return returnElement;
	}

	public OMElement getFile(OMElement getFileElement) throws I2B2Exception {
		return recvfileRequest(getFileElement);
	}

	public OMElement putFile(OMElement putFileElement) throws I2B2Exception {
		return sendfileRequest(putFileElement);
	}
}