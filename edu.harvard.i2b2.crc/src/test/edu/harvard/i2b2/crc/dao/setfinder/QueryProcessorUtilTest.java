/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.axis2.CRCAxisAbstract;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryToolUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryProcessorUtilTest {

	private static String testFileDir = null;

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
	}

	/*
	@Ignore
	@Test
	
	public void testQueryDefinitionDOM() throws Exception {
		QueryToolUtil queryUtil = new QueryToolUtil();
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		Connection conn = qpUtil.getManualConnection();
		assertNotNull("check database connection not null", conn);

		String filename = testFileDir
				+ "\\edu.harvard.i2b2.crc.dao.setfinder.querybuilder\\setfinder_query.xml";
		String xml = CRCAxisAbstract.getQueryString(filename);
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		RequestMessageType reqMsgType = (RequestMessageType) jaxbUtil
				.unMashallFromString(xml).getValue();
		System.out.println(reqMsgType.getMessageHeader().getMessageControlId());
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		QueryDefinitionRequestType queryDefinitionType = (QueryDefinitionRequestType) unWrapHelper
				.getObjectByClass(
						reqMsgType.getMessageBody().getAny(),
						edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType.class);
		System.out.println("query namef"
				+ queryDefinitionType.getQueryDefinition().getQueryName());

		JAXBContext jc = JAXBContext
				.newInstance(edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType.class);
		Marshaller m = jc.createMarshaller();
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.newDocument();
		m
				.marshal(
						(new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory())
								.createQueryDefinition(queryDefinitionType
										.getQueryDefinition()), doc);

		String domString = edu.harvard.i2b2.common.util.xml.XMLUtil
				.convertDOMToString(doc);
		System.out.println("string output" + domString);

		Document doc1 = edu.harvard.i2b2.common.util.xml.XMLUtil
				.convertStringToDOM(domString);
		System.out.println("string output"
				+ edu.harvard.i2b2.common.util.xml.XMLUtil
						.convertDOMToString(doc1));

		JAXBContext jc1 = JAXBContext
				.newInstance(edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory.class);
		Unmarshaller unMarshaller = jc1.createUnmarshaller();
		JAXBElement jaxbElement = (JAXBElement) unMarshaller
				.unmarshal(new StringReader(domString));
		QueryDefinitionType qftype = (QueryDefinitionType) jaxbElement
				.getValue();
		System.out.println("query name " + qftype.getQueryName());

	}
*/
	@Test
	public void testXmlToSqlDateConverion() throws Exception {
		DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
		XMLGregorianCalendar cal = dataTypeFactory
				.newXMLGregorianCalendar("2004-07-15T00:00:00.000-04:00");
		System.out.println("XMLCalendar Hour " + cal.getHour()
				+ " Gregorian calendar hour "
				+ cal.toGregorianCalendar().get(Calendar.HOUR_OF_DAY));
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MMM-yyyy HH:mm:ss");
		System.out.println("SimpleDate Format "
				+ dateFormat.format(cal.toGregorianCalendar().getTime())
				+ " orginal XML string " + "2004-07-15T00:00:00.000-04:00");
		System.out.println("DateFormat Format "
				+ DateFormat.getInstance().format(
						cal.toGregorianCalendar().getTime()));
	}

}
