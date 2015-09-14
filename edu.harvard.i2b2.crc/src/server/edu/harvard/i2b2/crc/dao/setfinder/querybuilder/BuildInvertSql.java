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
