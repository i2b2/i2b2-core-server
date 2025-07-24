/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
public class ObserverXMLWriterUtil extends PatientDataXMLWriterUtil {

	private Date updateDate, downloadDate;
	private String sourceSystemCd;

	private DTOFactory dtoFactory = new DTOFactory();

	// XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	public ObserverXMLWriterUtil(XMLEventWriter eventWriter)
			throws XMLStreamException {

		this.eventWriter = eventWriter;
	}

	@Override
	public void startSet() throws XMLStreamException {
		StartElement conceptSetStartElement = eventFactory.createStartElement(
				"", "ns1", "observer_set");
		eventWriter.add(conceptSetStartElement);

	}

	@Override
	public void endSet() throws XMLStreamException {
		EndElement conceptSetEndElement = eventFactory.createEndElement("",
				"ns1", "observer_set");
		eventWriter.add(conceptSetEndElement);
		XMLEvent end = eventFactory.createCharacters("\n");
		eventWriter.add(end);

	}

	@Override
	public void buildConcept(ResultSet conceptResultSet) throws Exception {
		// Create a EventFactory
		XMLEvent end = eventFactory.createCharacters("\n");

		// Create config open tag
		StartElement configStartElement = eventFactory.createStartElement("",
				"", "observer");

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
		createNode(eventWriter, "observer_path", conceptResultSet
				.getString("c_dimcode"));
		createNode(eventWriter, "observer_cd", conceptResultSet
				.getString("c_basecode"));
		createNode(eventWriter, "name_char", conceptResultSet
				.getString("c_name"));

		eventWriter.add(eventFactory.createEndElement("", "", "observer"));
		eventWriter.add(end);

	}

}
