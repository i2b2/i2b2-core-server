package edu.harvard.i2b2.crc.loader.xml;

import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;



public class StaxParser {
	
	@Test
	public void start() throws Exception { 
	 XMLInputFactory xmlif = XMLInputFactory.newInstance();
     FileReader fr = new FileReader("testfiles/stax.xml");
     XMLEventReader xmler = xmlif.createXMLEventReader(fr);
     EventFilter filter = new EventFilter() {
         public boolean accept(XMLEvent event) {
             return event.isStartElement();
         }
     };
     XMLEventReader xmlfer = xmlif.createFilteredReader(xmler, filter);

     // Jump to the first element in the document, the enclosing BugCollection
     StartElement e = (StartElement) xmlfer.nextEvent();
     System.out.println(e.getName().getLocalPart());
     
      
     JAXBContext ctx = JAXBContext.newInstance("edu.harvard.i2b2.crc.datavo.i2b2message");
     Unmarshaller um = ctx.createUnmarshaller();
     int bugs = 0;
     
     while (xmlfer.peek() != null) {
         Object o = um.unmarshal(xmler);
         if (o instanceof BodyType) {
        	 BodyType bi = (BodyType) o;
        	 System.out.println("QueryDefinitionRequestType found....");
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
