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

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.setProperty( "jaxb.encoding", "UTF-8" );
            marshaller.setProperty(
                    "com.sun.xml.bind.characterEscapeHandler",
                    new XmlCharacterEscapeHandler() );
            
            //marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
            //    new NamespacePrefixMapperImpl());

            // get an Apache XMLSerializer configured to generate CDATA
            marshaller.marshal(jaxbElement, doc);
        } catch (JAXBException jaxbEx) {
            jaxbEx.printStackTrace();
            throw new JAXBUtilException("Error during marshalling ", jaxbEx);
        }
    }
    
    public void marshallerWithCDATA(Object element, Writer strWriter, String[] cdataElements)
    	       throws JAXBUtilException {
    	try {
          	
            JAXBContext jaxbContext = getJAXBContext();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            
           // marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
           //     new NamespacePrefixMapperImpl());
            
          
            /* TODO mm old
            // get an Apache XMLSerializer configured to generate CDATA
             XMLSerializer serializer = getXMLSerializer(strWriter,cdataElements);
            
            // marshal using the Apache XMLSerializer
            marshaller.marshal(element,
            serializer.asContentHandler());
           */
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
          marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.TRUE);
          marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
              Boolean.TRUE);
          
          // marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
          //   new NamespacePrefixMapperImpl());
          
          
          //character escape
          if (splCharFilterFlag) { 
	           marshaller.setProperty( "jaxb.encoding", "UTF-8" );
	           marshaller.setProperty(
	                   "com.sun.xml.bind.characterEscapeHandler",
	                   new XmlCharacterEscapeHandler() );
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
    
    /* MM removed

    private  XMLSerializer getXMLSerializer(Writer strWriter, String[] cdataElements) {
        // configure an OutputFormat to handle CDATA
        OutputFormat of = new OutputFormat();

        // specify which of your elements you want to be handled as CDATA.
        // The use of the '^' between the namespaceURI and the localname
        // seems to be an implementation detail of the xerces code.
        // When processing xml that doesn't use namespaces, simply omit the
        // namespace prefix as shown in the third CDataElement below.
        ArrayList<String> elementNameList = new ArrayList<String>();
        int i=0;
        while (i<cdataElements.length) { 
        	elementNameList.add("^" +cdataElements[i] );
        	i++;
        }
        of.setCDataElements(elementNameList.toArray(new String[]{})); // <baz>

        // set any other options you'd like
        of.setPreserveSpace(true);
        of.setIndenting(true);
        of.setIndent(4);
        of.setLineSeparator(System.getProperty("line.separator"));

        // create the serializer
        XMLSerializer serializer = new XMLSerializer(of);

        // serializer.setOutputByteStream(strWriter);
        serializer.setOutputCharStream(strWriter);

        return serializer;
    }
    */
}
