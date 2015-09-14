package edu.harvard.i2b2.crc.loader.xml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;

public class TypePullParser {
	StartElementListener listener = null;
	String filename = null;
	String typeName = null;
	Class jaxbClass = null;

	public TypePullParser(StartElementListener listener, String typeName,
			Class jaxbClass, String filename) {
		this.listener = listener;
		this.typeName = typeName;
		this.jaxbClass = jaxbClass;
		this.filename = filename;
	}

	public void doParsing() throws I2B2Exception {

		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		FileReader fr = null;
		try {
			fr = new FileReader(filename);
			XMLEventReader xmler = xmlif.createXMLEventReader(fr);
			EventFilter filter = new EventFilter() {
				public boolean accept(XMLEvent event) {
					return event.isStartElement();
				}
			};
			XMLEventReader xmlfer = xmlif.createFilteredReader(xmler, filter);

			// Jump to the first element in the document, the enclosing
			// BugCollection
			StartElement e = (StartElement) xmlfer.nextEvent();
			// System.out.println(e.getName().getLocalPart());

			JAXBContext ctx = JAXBContext
					.newInstance("edu.harvard.i2b2.crc.datavo.i2b2message:edu.harvard.i2b2.crc.datavo.pdo");
			Unmarshaller um = ctx.createUnmarshaller();
			Marshaller ma = ctx.createMarshaller();
			int bugs = 0;

			while (xmlfer.hasNext()) {

				StartElement startElement = (StartElement) xmlfer.peek();
				if (startElement == null) {
					break;
				}

				if (startElement.getName().getLocalPart().trim().equals(
						typeName)) {

					JAXBElement<ObservationType> element = um.unmarshal(xmler,
							jaxbClass);
					listener.process(element.getValue());
				} else {
					xmlfer.nextEvent();
				}

			}
		} catch (JAXBException jaxEx) {
			throw new I2B2Exception("<" + typeName + "> parsing failed", jaxEx);
		} catch (XMLStreamException streamEx) {
			throw new I2B2Exception("<" + typeName + "> parsing failed",
					streamEx);
		} catch (FileNotFoundException fileEx) {
			throw new I2B2Exception("<" + typeName + "> parsing failed", fileEx);
		} finally {
			try {
				fr.close();
			} catch (IOException ioe) {
				;
			}
		}

	}
}
