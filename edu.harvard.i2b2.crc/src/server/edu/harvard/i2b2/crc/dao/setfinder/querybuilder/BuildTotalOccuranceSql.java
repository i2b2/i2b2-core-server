package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.TotOccuranceOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;

public class BuildTotalOccuranceSql {
	protected final Log log = LogFactory.getLog(BuildTotalOccuranceSql.class);

	DataSourceLookup dataSourceLookup = null;

	public BuildTotalOccuranceSql(DataSourceLookup dataSourceLookup) { 
		this.dataSourceLookup = dataSourceLookup;
	}

	public String buildTotalOccuranceSql(String dimensionJoinSql,
			boolean encounterFlag, boolean instanceNumFlag, String queryTiming, int panelNumber,
			TotalItemOccurrences totalOccurances, boolean panelInvertFlag) {

		String selectClause = " ", groupbyClause = " ";
		TotalItemOccurrenceHandler totalItemOccurrencHandler = new TotalItemOccurrenceHandler();
		String totalItemOccurrenceClause = totalItemOccurrencHandler
				.buildTotalItemOccurrenceClause(totalOccurances);

		if (panelNumber != 1) {
			selectClause = " 1 as panel_count ";
		} else {
			if (instanceNumFlag) {
				selectClause = "provider_id, start_date, concept_cd, instance_num, encounter_num, ";
			} else if (encounterFlag) {
				selectClause = "encounter_num, ";
			}
			selectClause += " patient_num ," + panelNumber + " as panel_count ";
		}

		//check if the dimensionJoinSql is query in query with fact constrains
		String groupbyClausePrefix = "";
		if (dimensionJoinSql.indexOf("j1.")>0) {
			groupbyClausePrefix = "j1.";
		}

		//if (instanceNumFlag) {
		//	groupbyClause = " " + groupbyClausePrefix + "encounter_num ," + groupbyClausePrefix + "instance_num,";
		//} else if (encounterFlag) {
		//	groupbyClause = " "  + groupbyClausePrefix + "encounter_num ,";
		//}
		//groupbyClause += " " +  groupbyClausePrefix + "patient_num ";


		QueryTimingHandler timingHandler = new QueryTimingHandler();
		if (timingHandler.isSameInstanceNum(queryTiming)) {
			groupbyClause = " " + groupbyClausePrefix + "encounter_num ," + groupbyClausePrefix + "instance_num, " + groupbyClausePrefix + "concept_cd," +
					groupbyClausePrefix + "start_date," + groupbyClausePrefix + "provider_id,";
		} else if (timingHandler.isSameVisit(queryTiming)) { 
			groupbyClause = " "  + groupbyClausePrefix + "encounter_num ,";
		} 
		groupbyClause += " " +  groupbyClausePrefix + "patient_num ";

		String totalOccuranceSql = "select " + selectClause + " from ("
				+ dimensionJoinSql ;

		//if (panelInvertFlag == false) { 
			totalOccuranceSql += "  group by " + groupbyClause ;
		//}

		if (totalOccurances != null) { 
			// TotOccuranceOperatorType

			if ((totalOccurances.getOperator() != null && totalOccurances.getOperator().value() != null && totalOccurances.getOperator().value().equalsIgnoreCase(TotOccuranceOperatorType.GE.value())) && totalOccurances.getValue()==1) {
			} else {
				log.debug("Setfinder query total occurrences operator value [" + totalOccurances.getOperator().value() + "]");
				String countDistinct = "*";
				if (this.dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) { 
					countDistinct = " distinct cast(patient_num as varchar) + '|' +  cast(encounter_num as varchar) + '|' + " + 
							" provider_id + '|' + cast(start_date as varchar) + '|' + cast(instance_num as varchar) + '|' +concept_cd"; 
				} else if (this.dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)  ||
						this.dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)	) { 
					countDistinct = " distinct patient_num || '|' || encounter_num || '|' || provider_id || '|' || instance_num || '|' ||concept_cd || '|' ||cast(start_date as  varchar(50) ) ";
				}
				totalOccuranceSql +=  " having count("+countDistinct+") " + totalItemOccurrenceClause;//  + " group by " + selectClause;
				/*if (panelInvertFlag == true && totalItemOccurrenceClause.trim() != "")
				{
					if (instanceNumFlag) 
						selectClause = " group by provider_id, start_date, concept_cd, instance_num, encounter_num ";
					else if (encounterFlag) 
						selectClause = " group by encounter_num ";
					else 
						totalOccuranceSql += " group by patient_num ";
				}*/
			}
		}
		totalOccuranceSql +=" ) t";

		return totalOccuranceSql;
	}
}
