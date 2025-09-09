/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.im.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.im.datavo.pdo.ParamType;
import edu.harvard.i2b2.im.datavo.pdo.PatientType;
import edu.harvard.i2b2.im.datavo.pdo.PidType;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public  class EMPIOpenEMPI  implements EMPI {
	private static Log log = LogFactory.getLog(EMPIOpenEMPI.class.getName());

	String authenticate = null;
	String person = null;
	public String findPerson(String username,
			String source, String value) throws Exception {

		if (authenticate == null)
			Authenticate();


		person = getPersonById(source, value);

		return person;
	}

	private String getPersonById(String source, String value) throws Exception {
		// TODO Auto-generated method stub
		try {
			String getRequestString = "";


			// First step is to get PM endpoint reference from properties file.
			String imEPR = "";
			try {
				imEPR = IMUtil.getInstance().getOpenEMPIWebService() + "/person-query-resource/findPersonById";
				getRequestString = 
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
								"<personIdentifier>" +
								"<identifier>"+ value + "</identifier>" +
								"<identifierDomain>" +
								"<namespaceIdentifier>" + source + "</namespaceIdentifier>" +
								"<universalIdentifier>" + source + "</universalIdentifier>" +
								"<universalIdentifierTypeCode>" + source + "</universalIdentifierTypeCode>" +
								"</identifierDomain>" +
								"</personIdentifier>";

			} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				throw e1;
			}

			URL url = new URL(imEPR);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty(
					"Content-Type", "application/xml" );
			httpCon.addRequestProperty("OPENEMPI_SESSION_KEY", authenticate);
			OutputStreamWriter out = new OutputStreamWriter(
					httpCon.getOutputStream());

			// String to XML Document
			Document document = convertStringToXml(getRequestString);

			// XML Document to String
			out.write(convertXmlToString(document));
			out.close();
			return IOUtils.toString(httpCon.getInputStream());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} 

	}

	public void parse(PatientType ptype) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
		factory.setXIncludeAware(false);

		String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		factory.setFeature(FEATURE, true);

		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document =
				builder.parse((new InputSource(new StringReader(person))));
		List<ParamType> paramList = new ArrayList<ParamType>();

		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (node instanceof Element) {


				String content = node.getLastChild().
						getTextContent().trim();
				if (!node.getNodeName().equals("personIdentifiers")) {
					ParamType param = new ParamType();
					param.setName(node.getNodeName());
					param.setValue(content);
					param.setType("T");
					ptype.getParam().add(param);
				}
			}

		}
		//return paramList;

	}

	private static String convertXmlToString(Document doc) throws Exception {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		Transformer transformer = transformerFactory.newTransformer();
		StringWriter stringWriter = new StringWriter();
		//transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
		return (stringWriter.toString());

		/*
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            tf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            transformer = tf.newTransformer();
           // tf.setXIncludeAware(false);

            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
		 */
	}

	private static Document convertStringToXml(String xmlString) {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// optional, but recommended
			// process XML securely, avoid attacks like XML External Entities (XXE)
			//dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			dbf.setFeature(FEATURE, true);
			dbf.setXIncludeAware(false);
			DocumentBuilder builder = dbf.newDocumentBuilder();

			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

			return doc;

		} catch (ParserConfigurationException | IOException | SAXException e) {
			throw new RuntimeException(e);
		}

	}


	private void Authenticate() throws Exception {
		// TODO Auto-generated method stub
		try {
			String getRequestString = "";


			// First step is to get PM endpoint reference from properties file.
			String imEPR = "";
			try {
				imEPR = IMUtil.getInstance().getOpenEMPIWebService() + "/security-resource/authenticate";
				getRequestString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><authenticationRequest><password>"+ IMUtil.getInstance().getOpenEMPIPassword() +"</password><username>" + IMUtil.getInstance().getOpenEMPIUsername() + "</username></authenticationRequest>";
			} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				throw e1;
			}

			URL url = new URL(imEPR);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setRequestProperty(
					"Content-Type", "application/xml" );
			OutputStreamWriter out = new OutputStreamWriter(
					httpCon.getOutputStream());
			out.write(getRequestString);
			out.close();
			authenticate =IOUtils.toString(httpCon.getInputStream());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} 

	}

	public void getIds(PidType newPidType) throws SAXException, IOException, ParserConfigurationException {


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
		factory.setXIncludeAware(false);

		DocumentBuilder builder = factory.newDocumentBuilder();

		String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		factory.setFeature(FEATURE, true);

		Document document =
				builder.parse((new InputSource(new StringReader(person))));
		List<ParamType> paramList = new ArrayList<ParamType>();

		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (node instanceof Element) {


				String value = null;
				if (node.getNodeName().equals("personIdentifiers")) {
					for(Node childNode=node.getFirstChild(); childNode!=null; childNode=childNode.getNextSibling()){
						if (childNode.getNodeName().equals("identifier"))
						{
							value = childNode.getLastChild().getTextContent().trim();
						}
						if (childNode.getNodeName().equals("identifierDomain"))
						{
							for(Node childNode2=childNode.getFirstChild(); childNode2!=null; childNode2=childNode2.getNextSibling()){
								if (childNode2.getNodeName().equals("namespaceIdentifier"))
								{
									PidType.PatientMapId patientMapId = new PidType.PatientMapId();
									patientMapId.setSource(childNode2.getLastChild().getTextContent().trim());
									patientMapId.setValue(value);
									newPidType.getPatientMapId().add(patientMapId);
								}
							}
						}
					}

					//			pidType.
				}
			}

		}

	}



}
