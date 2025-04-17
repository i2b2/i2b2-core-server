/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryMasterDaoTest {

	static QueryMasterSpringDao qmDAO = null;
	static String queryMasterId = null;

	@BeforeClass
	public static void setUp() throws Exception {
		/*
		 * String hiveId = "HIVE",projectId="/Asthma/1/1/",ownerId="@";
		 * DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(hiveId,
		 * projectId, ownerId); IDAOFactory daoFactory =
		 * daoFactoryHelper.getDAOFactory(); sfDaoFactory =
		 * daoFactory.getSetFinderDAOFactory(); IQueryMasterDao qmDAO =
		 * sfDaoFactory.getQueryMasterDAO();
		 */

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		DataSourceLookup dataSourceLookup = null;
		// instanciate datasource
		DataSource dataSource = QueryProcessorUtil.getInstance()
				.getSpringDataSource(dataSourceLookup.getDataSource());

		// create QueryMasterDao
		qmDAO = new QueryMasterSpringDao(dataSource, dataSourceLookup);
		QtQueryMaster queryMaster = new QtQueryMaster();
		queryMaster.setUserId("test_user");
		queryMaster.setGroupId("test_group_id");
		queryMaster.setName("test_name");
		queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
		queryMaster.setRequestXml("test_request_xml");
		queryMaster.setDeleteFlag("N");
		queryMasterId = qmDAO.createQueryMaster(queryMaster, "i2b2xml", null);
		System.out.println("Query master id " + queryMasterId);
	}

	@Test
	public void getQueryMasterByUserId() {

		List<QtQueryMaster> queryMasterList = qmDAO.getQueryMasterByUserId(
				"test_user","test_group", 100, null, false);
		org.junit.Assert.assertTrue(queryMasterList.size() > 0);
	}

	@Test
	public void getQueryMasterByGroup() {

		List<QtQueryMaster> queryMasterList = qmDAO.getQueryMasterByGroupId(
				"test_group_id", 5, null, false);
		org.junit.Assert.assertTrue(queryMasterList.size() > 0);
	}

	@Test
	public void getQueryDefinition() {

		QtQueryMaster queryMaster = qmDAO.getQueryDefinition(queryMasterId);
		System.out.println(queryMaster.getQueryMasterId());
		org.junit.Assert.assertEquals("test_request_xml", queryMaster
				.getRequestXml());
	}

	@Test
	public void renameQuery() throws Exception {

		qmDAO.renameQuery(queryMasterId, "test_rename");
	}

	@Test
	public void deleteQuery() throws Exception {

		qmDAO.deleteQuery(queryMasterId);
	}

}
