package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class BuildPanelSql extends CRCDAO {
	private String tempTableName = null;
	private DataSourceLookup dataSourceLookup = null;

	public BuildPanelSql(DataSourceLookup dataSourceLookup, String tempTableName) {
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.tempTableName = tempTableName;
	}

	public String buildPanelSql(List<String> itemSqlList, int panelCount,int oldPanelCount,
			boolean firstPanelFlag, boolean encounterFlag,
			boolean instanceNumFlag, boolean panelInvertFlag, boolean invertQueryFlag, boolean invertOnlyQueryFlag, String firstItemJoinTiming) {
		StringBuffer panelSqlBuffer = new StringBuffer();
		boolean firstItemFlag = true;
		
		
		for (String itemSql : itemSqlList) {
			
			// add the item sql to the temp table
			if (firstPanelFlag && invertOnlyQueryFlag == false ) {
				panelSqlBuffer.append(firstPanelItemSql(itemSql,
						getTempTableName(), encounterFlag, instanceNumFlag,firstItemJoinTiming));

			} else {
				panelSqlBuffer.append(nonFirstPanelItemSql(itemSql,
						getTempTableName(), panelCount, oldPanelCount, encounterFlag,
						instanceNumFlag,panelInvertFlag,firstPanelFlag, invertQueryFlag,firstItemFlag));
			}
			panelSqlBuffer.append(this.getSqlDelimitor());
			firstItemFlag = false;
		}
		return panelSqlBuffer.toString();
	}

	private String getTempTableName() {
		return this.tempTableName;
	}

	public String getSqlDelimitor() {
		return "\n<*>\n";
	}

	private String firstPanelItemSql(String totalOccuranceSql,
			String tempTableName, boolean encounterFlag, boolean instanceNumFlag, String firstItemJoinTiming) {
		String selectClause = " patient_num , panel_count";
		String selectInvJoinClause = " invjoinof.patient_num , invjoinitem.panel_count";
		if (instanceNumFlag) {
			selectClause = " provider_id, start_date, concept_cd, instance_num, encounter_num, " + selectClause;
			selectInvJoinClause = " invjoinof.provider_id, invjoinof.start_date, invjoinof.concept_cd, invjoinof.instance_num, invjoinof.encounter_num, " + selectClause;
		} else if (encounterFlag) {
			selectClause = " encounter_num, " + selectClause;
			selectInvJoinClause = " invjoinof.encounter_num, " + selectClause;
		}
		String firstPanelItemSql = ""; 
		
		if (!firstItemJoinTiming.equalsIgnoreCase(QueryTimingHandler.ANY)) {
			String invJoinOf = this.getDbSchemaName() + "visit_dimension "; 
			String whereEncounter = " ", onEncounter = " ";
			if (firstItemJoinTiming.equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)) {
				invJoinOf = this.getDbSchemaName() + "observation_fact ";
				whereEncounter = " and invjoinof.encounter_num = invjoinitem.encounter_num ";
				onEncounter = whereEncounter;
			}
			firstPanelItemSql  = " insert into " + this.getDbSchemaName()+ tempTableName + " (" + selectClause + ")" + "\n" + 
					" select " + selectInvJoinClause + " from " + invJoinOf  + " invjoinof left outer join (" + 
					totalOccuranceSql + " ) invjoinitem " +
					" on invjoinof.patient_num = invjoinitem.patient_num " + 
					onEncounter + 
					" where invjoinof.patient_num = invjoinitem.patient_num " + 
					whereEncounter;
		} else { 
			firstPanelItemSql  = " insert into " + this.getDbSchemaName()
			+ tempTableName + " (" + selectClause + ")" + "\n"
			+ totalOccuranceSql;
		}
		
		return firstPanelItemSql;
	}

	private String nonFirstPanelItemSql(String totalOccuranceSql,
			String tempTableName, int panelCount, int oldPanelCount,  boolean encounterFlag,
			boolean instanceNumFlag, boolean panelInvertFlag,  boolean firstPanelFlag, boolean invertQueryFlag, boolean firstItemFlag) {
		String encounterNumClause = " ", instanceNumClause = " ";
		if (instanceNumFlag) {
			instanceNumClause = " and  " + this.getDbSchemaName()
					+ tempTableName + ".encounter_num = t.encounter_num and "
					+ this.getDbSchemaName() + tempTableName
					+ ".instance_num = t.instance_num  and " 
					+ this.getDbSchemaName() + tempTableName
					+ ".start_date = t.start_date  and " 
					+ this.getDbSchemaName() + tempTableName
					+ ".concept_cd = t.concept_cd  and " 
					+ this.getDbSchemaName() + tempTableName
					+ ".provider_id = t.provider_id ";
		} else if (encounterFlag) {
			encounterNumClause = " and " + this.getDbSchemaName()
					+ tempTableName + ".encounter_num = t.encounter_num ";
		}
		String nonFirstPanelItemSql = " ";
		if (panelInvertFlag) {
			if (firstItemFlag) { 
				nonFirstPanelItemSql = " update " +   this.getDbSchemaName()
				+ tempTableName + " set panel_count = " + panelCount + " where " + this.getDbSchemaName() + tempTableName
				+ ".panel_count =  " + oldPanelCount  + "\n<*>\n";
			}
			if (firstPanelFlag) {
				oldPanelCount = 1;
			}
			String groupByClause = getGroupBy( encounterFlag, instanceNumFlag);
			
			 nonFirstPanelItemSql +=  " update " + this.getDbSchemaName()
				+ tempTableName + " set panel_count = -1 " 
				+ " where " + this.getDbSchemaName() + tempTableName
				+ ".panel_count =  " + panelCount + " and   exists ( " + totalOccuranceSql + " where "
				+ this.getDbSchemaName() + tempTableName
				+ ".patient_num = t.patient_num " + encounterNumClause
				+ instanceNumClause + " group by " +  groupByClause + "  )  "; //group by patient_num
			
		} else {
			String notExists = "  ";
		 if (invertQueryFlag && firstPanelFlag) { 
			 oldPanelCount = 1;
			 panelCount = -1;
			  notExists = " not ";
		 }
		 nonFirstPanelItemSql =  " update " + this.getDbSchemaName()
				+ tempTableName + " set panel_count =" + panelCount
				+ " where " + notExists + " exists ( " + totalOccuranceSql + " where "
				+ this.getDbSchemaName() + tempTableName
				+ ".panel_count =  " + oldPanelCount + " and "
				+ this.getDbSchemaName() + tempTableName
				+ ".patient_num = t.patient_num " + encounterNumClause
				+ instanceNumClause + " ) ";
		}
		return nonFirstPanelItemSql;
	}
	
	private String getGroupBy(boolean encounterFlag, boolean instanceNumFlag) { 
		String groupbyClausePrefix ="", groupbyClause = "";
		if (instanceNumFlag) {
			groupbyClause = " " + groupbyClausePrefix + "encounter_num ," + groupbyClausePrefix + "instance_num, " + groupbyClausePrefix + "concept_cd," +
					 groupbyClausePrefix + "start_date," + groupbyClausePrefix + "provider_id,";
		} else if (encounterFlag) { 
			groupbyClause = " "  + groupbyClausePrefix + "encounter_num ,";
		} 
		groupbyClause += " " +  groupbyClausePrefix + "patient_num ";
		return groupbyClause;
	}

}
