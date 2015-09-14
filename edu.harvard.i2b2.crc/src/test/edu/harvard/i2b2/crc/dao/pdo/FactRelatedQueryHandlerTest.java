/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.axis2.CRCAxisAbstract;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;

/**
 * Class to test pdo sql query creation
 * 
 * @author rkuttan
 */
public class FactRelatedQueryHandlerTest {
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

	@Test
	public void testPdoBuildQuery() throws Exception {
		// String filename = testFileDir
		// + "/edu.harvard.i2b2.crc.dao.pdo/pdo_query.xml";
		String filename = testFileDir
				+ "/edu.harvard.i2b2.crc.dao.pdo/pdo_query.xml";
		// String filename = testFileDir + "/mikeincrement.xml";
		String xml = CRCAxisAbstract.getQueryString(filename);
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		RequestMessageType reqMsgType = (RequestMessageType) jaxbUtil
				.unMashallFromString(xml).getValue();
		System.out.println(reqMsgType.getMessageHeader().getMessageControlId());
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		GetPDOFromInputListRequestType pdoRequestType = (GetPDOFromInputListRequestType) unWrapHelper
				.getObjectByClass(reqMsgType.getMessageBody().getAny(),
						GetPDOFromInputListRequestType.class);
		DataSourceLookup dataSourceLookup = new DataSourceLookup();
		dataSourceLookup.setDataSource("java:QueryToolDs");
		dataSourceLookup.setServerType("SQLSERVER");

		IFactRelatedQueryHandler pdoQueryHandler = null;
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			pdoQueryHandler = new SQLServerFactRelatedQueryHandler(
					dataSourceLookup, pdoRequestType.getInputList(),
					pdoRequestType.getFilterList(), pdoRequestType
							.getOutputOption());
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			pdoQueryHandler = new FactRelatedQueryHandler(dataSourceLookup,
					pdoRequestType.getInputList(), pdoRequestType
							.getFilterList(), pdoRequestType.getOutputOption());
		}

		FilterListType filterListType = pdoRequestType.getFilterList();
		PanelType panel = filterListType.getPanel().get(0);
		String pdoSql = pdoQueryHandler.buildQuery(panel,
				PdoQueryHandler.PLAIN_PDO_TYPE);
		System.out.println("Generated Sql" + pdoSql);
	}

}
