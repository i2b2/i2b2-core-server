package edu.harvard.i2b2.ontology.util;

import java.io.Writer;
import java.sql.ResultSet;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class PatientDataXMLWriterUtil {

	protected Writer conceptWriter;
	protected XMLEventWriter eventWriter;
	protected XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	public void startDocument() throws XMLStreamException {
		StartDocument startDocument = eventFactory.createStartDocument();
		eventFactory.createStartDocument("UTF", "1.0", true);
		eventWriter.setPrefix("ns1", "http://www.i2b2.org/xsd/hive/pdo/1.1/");
		eventWriter.add(startDocument);
		XMLEvent end = eventFactory.createCharacters("\n");
		eventWriter.add(end);
		StartElement patientDataStartElement = eventFactory.createStartElement(
				"", "ns1", "patient_data");
		eventWriter.add(patientDataStartElement);
		eventWriter.add(end);
	}

	public void endDocument() throws XMLStreamException {
		EndElement patientDataEndElement = eventFactory.createEndElement("",
				"ns1", "patient_data");
		// XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		// Create XMLEventWriter
		// eventWriter = outputFactory.createXMLEventWriter(conceptWriter);
		eventWriter.add(patientDataEndElement);

		eventWriter.add(eventFactory.createEndDocument());
		eventWriter.close();
	}

	protected void createNode(XMLEventWriter eventWriter, String name,
			String value) throws XMLStreamException {

		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent end = eventFactory.createCharacters("\n");
		XMLEvent tab = eventFactory.createCharacters("\t");
		// Create Start node
		StartElement sElement = eventFactory.createStartElement("", "", name);

		eventWriter.add(tab);
		eventWriter.add(sElement);

		// Create Content
		Characters characters = eventFactory.createCharacters(value);
		eventWriter.add(characters);
		// Create End node
		EndElement eElement = eventFactory.createEndElement("", "", name);
		eventWriter.add(eElement);
		eventWriter.add(end);

	}

	public abstract void startSet() throws XMLStreamException;

	public abstract void endSet() throws XMLStreamException;

	public abstract void buildConcept(ResultSet conceptResultSet)
			throws Exception;

	public static XMLEventWriter createXMLEventWriter(Writer writer)
			throws XMLStreamException {
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		// Create XMLEventWriter
		return outputFactory.createXMLEventWriter(writer);
	}

}
