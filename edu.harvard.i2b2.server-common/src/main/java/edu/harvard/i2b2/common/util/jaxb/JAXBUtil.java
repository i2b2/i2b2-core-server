/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util.jaxb;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jaxb.core.marshaller.NoEscapeHandler;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;

//import jakarta.xml.bind.JAXBContext;
//import jakarta.xml.bind.JAXBElement;
//import jakarta.xml.bind.JAXBException;
//import jakarta.xml.bind.Marshaller;
//import jakarta.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;


public class JAXBUtil {
	private static Log log = LogFactory.getLog(JAXBUtil.class);
	private String allPackageName = null;
	private Class jaxbClass = null;
	private JAXBContext jaxbContext = null;

	/**
	 * Default Constructor
	 *
	 */
	protected JAXBUtil() {
	}

	/**
	 * Constructor to accept package name in String array
	 *
	 * @param packageName
	 */
	public JAXBUtil(String[] packageName) {
		StringBuffer givenPackageName = new StringBuffer();

		for (int i = 0; i < packageName.length; i++) {
			givenPackageName.append(packageName[i]);

			if ((i + 1) < packageName.length) {
				givenPackageName.append(":");
			}
		}

		allPackageName = givenPackageName.toString();
	}

	public JAXBUtil(Class jaxbClass)  {
		this.jaxbClass = jaxbClass;
	}

	private JAXBContext getJAXBContext() throws JAXBException {

		if (jaxbContext == null) {
			if (jaxbClass != null) { 
				log.debug("JaxbClass is " + jaxbClass);
				jaxbContext = JAXBContext.newInstance(jaxbClass);
			}
			else { 
				log.debug("AllPackageName is " + allPackageName);
				jaxbContext = JAXBContext.newInstance(allPackageName,getClass().getClassLoader());
			}
		}

		return jaxbContext;
	}

	/**
	 *
	 * @param requestMessageType
	 * @param doc
	 * @throws JAXBUtilException
	 */
	public void marshaller(JAXBElement<?> jaxbElement, Document doc)
			throws JAXBUtilException {
		try {
			JAXBContext jaxbContext = getJAXBContext();
			Marshaller marshaller = jaxbContext.createMarshaller();
			/* MM
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.setProperty( "jaxb.encoding", "UTF-8" );
            marshaller.setProperty(
                    "com.sun.xml.bind.characterEscapeHandler",
                    new XmlCharacterEscapeHandler() );
			 */
			marshaller.marshal(jaxbElement, doc);
		} catch (JAXBException jaxbEx) {
			jaxbEx.printStackTrace();
			throw new JAXBUtilException("Error during marshalling ", jaxbEx);
		}
	}

	public void marshallerWithCDATA(Object element, Writer strWriter, String[] cdataElements)
			throws JAXBUtilException {
		marshallerWithCDATA( element,  strWriter,cdataElements, false);

	}

	public void marshallerWithCDATA(Object element, Writer strWriter, String[] cdataElements, boolean useGlassFish)
			throws JAXBUtilException {
		try {
			JAXBContext jaxbContext = getJAXBContext();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);

			if (useGlassFish) {
				marshaller.setProperty("org.glassfish.jaxb.characterEscapeHandler", new NoEscapeHandler());
			} 
			//else {
			//	marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.TRUE);
			//}

			marshaller.marshal(element, strWriter);

		} catch (Exception jaxbEx) {
			jaxbEx.printStackTrace();
			throw new JAXBUtilException("Error during marshalling ", jaxbEx);
		}

	}


	/**
	 *
	 * @param requestMessageType
	 * @param strWriter
	 * @param splCharFilterFlag
	 * @throws JAXBUtilException
	 */
	public void marshaller(Object element, Writer strWriter, boolean splCharFilterFlag)
			throws JAXBUtilException {
		try {

			JAXBContext jaxbContext = getJAXBContext();
			Marshaller marshaller = jaxbContext.createMarshaller();
			//marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);


			//character escape
			if (splCharFilterFlag) { 
				marshaller.setProperty( "jaxb.encoding", "UTF-8" );
				//  marshaller.setProperty(
				//         "com.sun.xml.bind.characterEscapeHandler",
				//        new XmlCharacterEscapeHandler() );
			}



			marshaller.marshal(element, strWriter);
		} catch (Exception jaxbEx) {
			jaxbEx.printStackTrace();
			throw new JAXBUtilException("Error during marshalling ", jaxbEx);
		}

	}

	/**
	 *
	 * @param requestMessageType
	 * @param strWriter
	 * @throws JAXBUtilException
	 */
	public void marshaller(Object element, Writer strWriter)
			throws JAXBUtilException {
		marshaller(element, strWriter,false);
	}



	public JAXBElement unMashallFromString(String xmlString)
			throws JAXBUtilException {
		if (xmlString == null) {
			throw new JAXBUtilException("String value is Null");
		}

		JAXBElement jaxbElement = unmashalFromString(xmlString);

		return jaxbElement;
	}

	public JAXBElement unMarshalFromInputStream(InputStream is)
			throws JAXBUtilException {
		if (is == null) {
			throw new JAXBUtilException("Input Stream is Null");
		}

		JAXBElement jaxbElement = unmarshalFromInputStream(is);

		return jaxbElement;
	}

	public JAXBElement unMashallFromDocument(Document doc)
			throws JAXBUtilException {
		if (doc == null) {
			throw new JAXBUtilException("Document value is Null");
		}

		JAXBElement jaxbElement = unmashalFromDocument(doc);

		return jaxbElement;
	}

	public JAXBElement unMashallerRequest(String fileName)
			throws JAXBUtilException {
		if (fileName == null) {
			throw new JAXBUtilException("File name is Null");
		}

		JAXBElement jaxbElement = null;

		try {
			JAXBContext jaxbContext = getJAXBContext();
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			jaxbElement = (JAXBElement) unmarshaller.unmarshal(new File(
					fileName));
		} catch (JAXBException jaxbEx) {
			throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
		}

		return jaxbElement;
	}

	private JAXBElement unmashalFromDocument(Document doc)
			throws JAXBUtilException {
		JAXBElement unMarshallObject = null;

		try {
			JAXBContext jaxbContext = getJAXBContext();
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unMarshallObject = (JAXBElement) unmarshaller.unmarshal(doc);
		} catch (JAXBException jaxbEx) {
			throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
		}

		return unMarshallObject;
	}

	private JAXBElement unmashalFromString(String xmlString)
			throws JAXBUtilException {
		JAXBElement unMarshallObject = null;

		try {
			//Disable XXE
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);


			//Do unmarshall operation
			Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(),
					new InputSource(new StringReader(xmlString)));


			JAXBContext jaxbContext = getJAXBContext();
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unMarshallObject = (JAXBElement) unmarshaller.unmarshal(new StringReader(
					xmlString));
			log.debug("object.toString()" +
					unMarshallObject.getDeclaredType().getCanonicalName());
		} catch (JAXBException | SAXException  | ParserConfigurationException jaxbEx) {
			throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
		}

		return unMarshallObject;
	}

	private JAXBElement unmarshalFromInputStream(InputStream is)    throws JAXBUtilException {
		JAXBElement unMarshallObject = null;

		try {
			JAXBContext jaxbContext = getJAXBContext();
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			unMarshallObject = (JAXBElement) unmarshaller.unmarshal(is);
			log.debug("object.toString()" +
					unMarshallObject.getDeclaredType().getCanonicalName());
		} catch (JAXBException jaxbEx) {
			throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
		}

		return unMarshallObject;
	}


}
