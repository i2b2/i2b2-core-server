package edu.harvard.i2b2.ontology.util;

import java.sql.ResultSet;
import java.util.Date;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;

/**
 * StAX class to write concept set of the PatientData Object.
 * 
 * @author rkuttan
 * 
 */
public class ConceptXMLWriterUtil extends PatientDataXMLWriterUtil {

	private Date updateDate, downloadDate;
	private String sourceSystemCd;

	private DTOFactory dtoFactory = new DTOFactory();

	// XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	public ConceptXMLWriterUtil(XMLEventWriter eventWriter)
			throws XMLStreamException {

		this.eventWriter = eventWriter;
	}

	@Override
	public void startSet() throws XMLStreamException {
		// Create and write Start Tag
		StartElement conceptSetStartElement = eventFactory.createStartElement(
				"", "ns1", "concept_set");
		eventWriter.add(conceptSetStartElement);

	}

	@Override
	public void endSet() throws XMLStreamException {
		EndElement conceptSetEndElement = eventFactory.createEndElement("",
				"ns1", "concept_set");
		eventWriter.add(conceptSetEndElement);

	}

	@Override
	public void buildConcept(ResultSet conceptResultSet) throws Exception {
		// Create a EventFactory
		XMLEvent end = eventFactory.createCharacters("\n");

		// Create config open tag
		StartElement configStartElement = eventFactory.createStartElement("",
				"", "concept");

		eventWriter.add(configStartElement);
		updateDate = conceptResultSet.getDate("update_date");
		if (updateDate != null) {
			eventWriter.add(eventFactory.createAttribute("update_date",
					dtoFactory.getXMLGregorianCalendar(updateDate.getTime())
							.toString()));

		}
		downloadDate = conceptResultSet.getDate("download_date");
		if (downloadDate != null) {
			eventWriter.add(eventFactory.createAttribute("download_date",
					dtoFactory.getXMLGregorianCalendar(downloadDate.getTime())
							.toString()));

		}
		sourceSystemCd = conceptResultSet.getString("sourcesystem_cd");
		if (sourceSystemCd != null) {
			eventWriter.add(eventFactory.createAttribute("sourcesystem_cd",
					sourceSystemCd));
		}

		eventWriter.add(end);

		// Write the different nodes
		createNode(eventWriter, "concept_path", conceptResultSet
				.getString("c_dimcode"));
		createNode(eventWriter, "concept_cd", conceptResultSet
				.getString("c_basecode"));
		createNode(eventWriter, "name_char", conceptResultSet
				.getString("c_name"));

		eventWriter.add(eventFactory.createEndElement("", "", "concept"));
		eventWriter.add(end);

	}

	public void close() throws XMLStreamException {
		eventWriter.add(eventFactory.createEndDocument());
		eventWriter.close();
	}

}
