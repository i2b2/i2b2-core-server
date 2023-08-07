/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class BuildInvertSql extends CRCDAO  {

	private String tempTableName = null;
	private DataSourceLookup dataSourceLookup = null;

	public BuildInvertSql(DataSourceLookup dataSourceLookup, String tempTableName) {
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.tempTableName = tempTableName;
	}
	
	public String buildInsertInvertSql(String queryTiming) {
		String insertInvertSql = "";

		String selectEncounterNum = " ", selectPatientNum = " patient_num ";
		String invertTableName = "patient_dimension pat ";
		
		QueryTimingHandler queryTimingHandler = new QueryTimingHandler();
		if (queryTimingHandler.isSameVisit(queryTiming)) {
			selectEncounterNum = " , encounter_num ";
			invertTableName = "visit_dimension visit ";
			
		}

		insertInvertSql = " insert into " + this.dbSchemaName
				+ this.tempTableName + " ( " + selectPatientNum
				+ selectEncounterNum + ", panel_count ) select distinct " + selectPatientNum
				+ selectEncounterNum + ",1  from " + this.dbSchemaName
				+ invertTableName ;
		insertInvertSql += "\n<*>\n";
		return insertInvertSql;

	}
}
