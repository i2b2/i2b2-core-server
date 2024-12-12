/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.xml;

import java.io.FileReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;

import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;



public class StaxParser {
	
	@Test
	public void start() throws Exception { 
	 XMLInputFactory xmlif = XMLInputFactory.newInstance();
     FileReader fr = new FileReader("testfiles/stax.xml");
     XMLEventReader xmler = xmlif.createXMLEventReader(fr);
     EventFilter filter = new EventFilter() {
         @Override
		public boolean accept(XMLEvent event) {
             return event.isStartElement();
         }
     };
     XMLEventReader xmlfer = xmlif.createFilteredReader(xmler, filter);

     // Jump to the first element in the document, the enclosing BugCollection
     StartElement e = (StartElement) xmlfer.nextEvent();
      
     JAXBContext ctx = JAXBContext.newInstance("edu.harvard.i2b2.crc.datavo.i2b2message");
     Unmarshaller um = ctx.createUnmarshaller();
     int bugs = 0;
     
     while (xmlfer.peek() != null) {
         Object o = um.unmarshal(xmler);
         if (o instanceof BodyType) {
        	 BodyType bi = (BodyType) o;
             // process the bug instance
             bugs++;
         }
     }
     //assertEquals(180, bugs);
     fr.close();
 
	}
	
	public static void main(String args[]) throws Exception { 
		StaxParser p = new StaxParser();
		p.start();
	}
}
