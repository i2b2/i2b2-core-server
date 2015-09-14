package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

public class DxTableHandler {
	private String dbSchemaName = null;
	private String dxTempTableName = null;
	private String tempTableName = null;

	public DxTableHandler(String dbSchemaName, String dxTempTableName,
			String tempTableName) {
		this.dbSchemaName = dbSchemaName;
		this.dxTempTableName = dxTempTableName;
		this.tempTableName = tempTableName;

	}

	public String buildDxInsertSql(
			boolean encounterNumFlag, boolean instanceNumFlag, int maxPanelNum) {
		String dxInsertSql = "";

		
			String selectEncounterNum = " ", selectPatientNum = " patient_num ";
			if (encounterNumFlag) {
				selectEncounterNum = " , encounter_num ";
			}

			dxInsertSql = " insert into " + this.dbSchemaName
					+ this.dxTempTableName + " ( " + selectPatientNum
					+ selectEncounterNum + " ) select * from ("
					+ " select distinct " + selectPatientNum
					+ selectEncounterNum + "from " + this.dbSchemaName
					+ this.tempTableName + " where panel_count = "
					+ maxPanelNum + " ) q";
		
		return dxInsertSql;
	}

	private String createDxInsertInvertSql(boolean encounterNumFlag,
			int maxPanelNum) {
		String dxInsertInvertSql = "";

		String selectEncounterNum = " ", selectPatientNum = " patient_num ";
		String invertTableName = "patient_dimension pat ";
		String invertWhereClause = " temp.patient_num = pat.patient_num ";
		if (encounterNumFlag) {
			selectEncounterNum = " , encounter_num ";
			invertTableName = "visit_dimension visit ";
			invertWhereClause = " temp.encounter_num = visit.encounter_num and temp.patient_num = visit.patient_num ";

		}

		dxInsertInvertSql = " insert into " + this.dbSchemaName
				+ this.dxTempTableName + " ( " + selectPatientNum
				+ selectEncounterNum + " ) select " + selectPatientNum
				+ selectEncounterNum + " from " + this.dbSchemaName
				+ invertTableName + "  where not exists (select 1 from "
				+ this.dbSchemaName + this.tempTableName + " temp where "
				+ invertWhereClause + " ) ";

		return dxInsertInvertSql;

	}

	private String addEncounterFromVisitDimension(int maxPanelNumber) {
		String encounterSetDxSql = " ";

		encounterSetDxSql = " insert into dx (encounter_num,patient_num) "
				+ " select encounter_num, patient_num from visit_dimension  "
				+ " where patient_num in (select distinct patient_num from dx) ";

		return encounterSetDxSql;
	}

}
