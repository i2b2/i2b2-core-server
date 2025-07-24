/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.axis2.CRCAxisAbstract;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.PDOFactory;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class TablePdoQueryPatientDaoTest {

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
		String filename = testFileDir + "/pdo_patientsinglequote.xml";
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
		QueryProcessorUtil queryProcessorUtil = QueryProcessorUtil
				.getInstance();
		DataSourceLookup dataSourceLookup = null;
		DataSource dataSource = queryProcessorUtil
				.getSpringDataSource("TestDataSource");
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

		TablePdoQueryPatientDao tablePdoQueryPatientDao = new TablePdoQueryPatientDao(
				dataSourceLookup, dataSource);
		List<String> panelSqlList = new java.util.ArrayList<String>();
		FilterListType filterListType = pdoRequestType.getFilterList();
		String panelSql = null;
		List<Integer> sqlParamCountList = new ArrayList<Integer>();
		for (PanelType panel : filterListType.getPanel()) {
			panelSql = pdoQueryHandler.buildQuery(panel,
					PdoQueryHandler.PLAIN_PDO_TYPE);
			System.out.println("Panel Sql " + panelSql);
			panelSqlList.add(panelSql);

			int sqlParamCount = panel.getItem().size();
			if (panel.getInvert() == 1) {
				sqlParamCount++;
			}
			sqlParamCountList.add(sqlParamCount);

		}
		IInputOptionListHandler inputOptionListHandler = PDOFactory
				.buildInputListHandler(pdoRequestType.getInputList(),
						dataSourceLookup);

		PatientSet patientSet = tablePdoQueryPatientDao.getPatientByFact(
				panelSqlList, sqlParamCountList, inputOptionListHandler, true,
				true, true);
		System.out.println(patientSet.getPatient().size());
		// System.out.println("Generated Sql" + pdoSql);
	}
}
