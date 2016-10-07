/*
 * Copyright (c) 2006-2013 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal;
 
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.QueryConstraintStrategy;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.TemporalConstraintStrategy;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryJoinColumnType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryJoinType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QuerySpanConstraintType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryAggregateOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryConstraintType;

public class TemporalSubQuery implements Comparable{

	protected final Log log = LogFactory.getLog(getClass());
	
	public enum TemporalQuerySpanUnits{
		YEAR,
		MONTH,
		DAY,
		HOUR,
		SECOND,
		MINUTE
	};
	
	public enum TemporalQueryReturnColumns {
 		PATIENT, 
 		ENCOUNTER, 
 		INSTANCE, 
 		START_DATE, 
 		END_DATE,
 		FIRST_START_DATE,
 		LAST_START_DATE,
 		FIRST_END_DATE,
 		LAST_END_DATE,
 		ENCOUNTER_START_DATE, 
 		ENCOUNTER_END_DATE,
 		FIRST_ENCOUNTER_START_DATE,
 		LAST_ENCOUNTER_START_DATE,
 		FIRST_ENCOUNTER_END_DATE,
 		LAST_ENCOUNTER_END_DATE
 	};
	
	private List<TemporalPanel> panelList = null;
	private TemporalQuery parent;
	private int invertedPanelCount = 0;
	private int startPanelIndex = 0;
	private int endPanelIndex = -1;
	
	private EnumSet<TemporalQueryReturnColumns> rtnColumns = EnumSet.of(TemporalQueryReturnColumns.PATIENT);
	private QueryDefinitionType subQuery = null;
	private String subQueryId = null;
	private HashMap<String, List<QueryConstraintType>> constraints = null;
	private boolean aggregateApplied = false;
	private List<String> preProcessingSql = null;
	private List<String> postProcessingSql = null;


	public TemporalSubQuery(TemporalQuery parent, List<PanelType> groupPanels) throws I2B2Exception{
		this.parent = parent;
		this.subQuery = new QueryDefinitionType();
		this.subQuery.setQueryId(parent.generateUniqueId());
		this.subQuery.getPanel().addAll(groupPanels);	

		this.preProcessingSql = new ArrayList<String>();
		this.postProcessingSql = new ArrayList<String>();
		parsePanels(groupPanels);
	}
		
	public TemporalSubQuery(TemporalQuery parent, QueryDefinitionType event) throws I2B2Exception{
		this.parent = parent;
		this.subQuery = event;
		if (event.getQueryId()==null||event.getQueryId().trim().length()==0)
			event.setQueryId(parent.generateUniqueId());

		this.preProcessingSql = new ArrayList<String>();
		this.postProcessingSql = new ArrayList<String>();
		parsePanels(event.getPanel());
	}
		
	public TemporalSubQuery(TemporalQuery parent, QueryDefinitionType event, HashMap<String, List<QueryConstraintType>> constraintList) throws I2B2Exception{
		this.parent = parent;
		this.subQuery = event;
		if (event.getQueryId()==null||event.getQueryId().trim().length()==0)
			event.setQueryId(parent.generateUniqueId());
		if (constraintList!=null)
			this.constraints = constraintList;

		this.preProcessingSql = new ArrayList<String>();
		this.postProcessingSql = new ArrayList<String>();
		parsePanels(event.getPanel());
	}
		
	public TemporalSubQuery(TemporalQuery parent, QueryDefinitionType event, HashMap<String, List<QueryConstraintType>> constraintList, EnumSet<TemporalQueryReturnColumns> returnColumns) throws I2B2Exception{
		this.parent = parent;
		this.subQuery = event;
		if (event.getQueryId()==null||event.getQueryId().trim().length()==0)
			event.setQueryId(parent.generateUniqueId());
		if (constraintList!=null)
			this.constraints = constraintList;
		if (returnColumns!=null)
			this.rtnColumns = returnColumns;

		this.preProcessingSql = new ArrayList<String>();
		this.postProcessingSql = new ArrayList<String>();
		parsePanels(event.getPanel());
	}
		
	private void parsePanels(List<PanelType> grpList) throws I2B2Exception{
		TreeSet<TemporalPanel> panelSet = new TreeSet<TemporalPanel>();
		for (PanelType panelType : grpList) {
			TemporalPanel panelItem = new TemporalPanel(this, panelType);
			if (panelItem.isPanelInverted())
				invertedPanelCount++;
			panelSet.add(panelItem);			
		}
		
		panelList = new ArrayList<TemporalPanel>();
		panelList.addAll(panelSet);		
	}
			
	public String buildSql() throws I2B2DAOException{
		StringBuffer subQuerySqlBuffer = new StringBuffer();
		int currentPanelIndex = 0;
		for (TemporalPanel panel : panelList){
			String panelSql = panel.buildSql(currentPanelIndex);
			subQuerySqlBuffer.append(panelSql);					
						

			if (!panel.isPanelInverted()){
				endPanelIndex = currentPanelIndex;
				currentPanelIndex++;
			}
			
			subQuerySqlBuffer.append(getSqlDelimiter());
		}
		
		if (parent.getSubQueryCount()>1){
			int level = 0;
			if (this.isFirstSubQuery())
				level = 1;
			subQuerySqlBuffer.append(buildMoveToMasterSql(level)); 
			subQuerySqlBuffer.append(getSqlDelimiter());
	
			if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
				parent.addPostProcessingSql(getDeleteTempMasterSql(this.getSubQueryId(), 0));
						
			subQuerySqlBuffer.append(getDeleteTempTableSql());
			subQuerySqlBuffer.append(getSqlDelimiter());			
		}
		
		if (!this.isFirstSubQuery()){
			subQuerySqlBuffer.append(buildMasterTableTimingConstraintUpdate(1));
			subQuerySqlBuffer.append(getSqlDelimiter());
		}
		
		StringBuffer sqlBuffer = new StringBuffer();
		if (this.preProcessingSql!=null&&this.preProcessingSql.size()>0){
			for(String sql : this.preProcessingSql){
				sqlBuffer.append(sql);
				sqlBuffer.append(getSqlDelimiter());
			}
		}
		sqlBuffer.append(subQuerySqlBuffer.toString());
		if (this.postProcessingSql!=null&&this.postProcessingSql.size()>0){
			for(String sql : this.postProcessingSql){
				sqlBuffer.append(sql);
				sqlBuffer.append(getSqlDelimiter());
			}
		}
			
		
		return sqlBuffer.toString();
	}
		
	public boolean isFirstSubQuery(){
		return parent.isFirstSubQuery(this);
	}
	
	public boolean isLastSubQuery(){
		return parent.isLastSubQuery(this);
	}

	
	
	protected TemporalQueryReturnColumns getStartDateReturnType(){
		//currently, we only support one start and end date type per query.  if both instance and encounter start dates
		//are specified in a query, we'll use the instance date
		
		if (rtnColumns.contains(TemporalQueryReturnColumns.START_DATE)
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_START_DATE) && rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_START_DATE))
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.END_DATE) && (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_START_DATE) || rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_START_DATE)))) {
			return TemporalQueryReturnColumns.START_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.FIRST_START_DATE)) {
			return TemporalQueryReturnColumns.FIRST_START_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.LAST_START_DATE)) {
			return TemporalQueryReturnColumns.LAST_START_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.ENCOUNTER_START_DATE)
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE) && rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE))
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.ENCOUNTER_END_DATE) && (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE) || rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE)))) {
			return TemporalQueryReturnColumns.ENCOUNTER_START_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE)) {
			return TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE)) {
			return TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE;
		}
		return null;
	}
	
	
	
	protected TemporalQueryReturnColumns getEndDateReturnType(){
		if (rtnColumns.contains(TemporalQueryReturnColumns.END_DATE)
				|| (rtnColumns.contains(TemporalQueryReturnColumns.FIRST_END_DATE) && rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_END_DATE))
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.START_DATE) && (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_END_DATE) || rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_END_DATE)))) {
			return TemporalQueryReturnColumns.END_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.FIRST_END_DATE)) {
			return TemporalQueryReturnColumns.FIRST_END_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.LAST_END_DATE)) {
			return TemporalQueryReturnColumns.LAST_END_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.ENCOUNTER_END_DATE)
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE) && rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE))
				|| (rtnColumns
						.contains(TemporalQueryReturnColumns.ENCOUNTER_START_DATE) && (rtnColumns
						.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE) || rtnColumns
						.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE)))) {
			return TemporalQueryReturnColumns.ENCOUNTER_END_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE)) {
			return TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE;
		} else if (rtnColumns
				.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE)) {
			return TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE;
		}
		return null;
	}
	
	protected String buildMasterTableTimingConstraintUpdate(int level){
		String timingConstraintSql = buildTimingConstraintSql(level);
		
		StringBuilder updateSql = new StringBuilder();
		if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER)){
			if (useSqlServerTempTables()){
				updateSql.append("update #m" + getSubQueryId() + " ");
				updateSql.append("set level_no = " + String.valueOf(level) + " ");
				updateSql.append("from #m" + getSubQueryId() + " t ");				
				updateSql.append(timingConstraintSql + " ");
				updateSql.append("and t.level_no = " + String.valueOf(level - 1) + " ");
			}
			else {
				updateSql.append("update " + parent.getMasterTempTableName() + " ");
				updateSql.append("set level_no = " + String.valueOf(level) + " ");
				updateSql.append("from " + parent.getMasterTempTableName() + " t ");
				updateSql.append(timingConstraintSql + " ");
				updateSql.append("and t.level_no = " + String.valueOf(level - 1) + " ");
				updateSql.append("and t.master_id = '" + this.getSubQueryId() + "'");
			}
		}
		else {
			updateSql.append("update " + parent.getMasterTempTableName() + " t ");
			updateSql.append("set level_no = " + String.valueOf(level) + " ");
			updateSql.append(timingConstraintSql + " ");
			updateSql.append("and t.level_no = " + String.valueOf(level - 1) + " ");
			updateSql.append("and t.master_id = '" + this.getSubQueryId() + "'");
		}
		

		return updateSql.toString();
	}
	
	protected String buildTimingConstraintUpdate(int panelCount){
		String timingConstraintSql = buildTimingConstraintSql(panelCount);
		
		StringBuilder updateSql = new StringBuilder();
		if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER)){
			updateSql.append("update " + parent.getTempTableName() + " ");
			updateSql.append("set panel_count = " + String.valueOf(panelCount) + " ");
			updateSql.append("from " + parent.getTempTableName() + " t ");
		}
		else {
			updateSql.append("update " + parent.getTempTableName() + " t ");
			updateSql.append("set panel_count = " + String.valueOf(panelCount) + " ");
		}
		
		updateSql.append(timingConstraintSql + " ");
		updateSql.append("and t.panel_count = " + String.valueOf(panelCount - 1));

		return updateSql.toString();
	}
	
	private String addTimingConstraintSql(String panelSql){
		String timingConstraint = buildTimingConstraintSql();
		int lastIndex = panelSql.lastIndexOf(this.getSqlDelimiter());
		if (lastIndex==-1){
			panelSql += timingConstraint;
		}
		else if (parent.getQueryOptions().getQueryConstraintLogic()==QueryConstraintStrategy.TEMP_TABLES &&
				parent.getServerType().equals(DAOFactoryHelper.SQLSERVER)){
			panelSql = addToRegExExpression(panelSql, "insert into " + parent.getTempTableName() + "(.*?)(?:(" + getSqlDelimiter().replace("*", "\\*") + "|$))", timingConstraint);		
		}
		else { 
			panelSql = panelSql.replace(getSqlDelimiter(), timingConstraint + getSqlDelimiter()) + timingConstraint;
		}
		return panelSql;
	}

	private String addToRegExExpression(String sqlString, String regEx, String newSql){
		StringBuilder sql = new StringBuilder();
		Pattern p = Pattern.compile(regEx, Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sqlString);
		int lastIndex = 0;
		while (m.find()){		
			int endIndex = m.end();
			String text = sqlString.substring(lastIndex, endIndex - getSqlDelimiter().length());
			sql.append(text);			
			sql.append(newSql);
			sql.append(getSqlDelimiter());
			lastIndex = endIndex;
		}
		sql.append(sqlString.substring(lastIndex));
		return sql.toString();
	}
	
	protected String buildTimingConstraintSql(){
		return buildTimingConstraintSql(-1);
	}
	
	protected String buildTimingConstraintSql(int panelCount){
		StringBuffer timingSqlBuf = new StringBuffer("");
		
		boolean firstConstraint = true;
		
		boolean encounterConstraint = false;
		if (parent.getQueryTiming()!=null&&
				(parent.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAME)||
				parent.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)))
			encounterConstraint = true;
		
		String lastSubQueryId = parent.getLastProcessedSubQueryId();
		if (lastSubQueryId!=null&&lastSubQueryId.trim().length()>0){
			if (useSqlServerTempTables()){
				timingSqlBuf.append(" where exists (select 1 from " + 
						"#m" + lastSubQueryId + " m " +
						"where m.patient_num = t.patient_num " +
						(encounterConstraint?"and m.encounter_num = t.encounter_num ":"") +
						"and m.level_no = 1 " +
						//"group by m.patient_num" + 
						") \n");				
			}
			else {
				timingSqlBuf.append(" where exists (select 1 from " + parent.getMasterTempTableName() + " m " +
							"where m.master_id = '" + lastSubQueryId + "' " +
							"and m.patient_num = t.patient_num " +
							(encounterConstraint?"and m.encounter_num = t.encounter_num ":"") +
							"and m.level_no = 1 " +
							//"group by m.patient_num " +
							") \n");
			}
			firstConstraint = false;
		}
		
		if (constraints!=null){
			for (List<QueryConstraintType> constraintLists: constraints.values()){
				for (QueryConstraintType constraint: constraintLists){
				
					QueryJoinType masterTableQuery = constraint.getFirstQuery();
					QueryJoinType tempTableQuery = constraint.getSecondQuery();
					
					//first, get the right column comparison
					String firstColumn = getColumnNameFromType(constraint.getFirstQuery().getJoinColumn());
					String secondColumn = getColumnNameFromType(constraint.getSecondQuery().getJoinColumn());
				
					String firstPrefix = "m";
					String secondPrefix = "t";
					boolean switchedPrefix = false; 
			
					String otherQueryId = constraint.getFirstQuery().getQueryId();
					if (constraint.getFirstQuery().getQueryId().equals(getQueryId())){
						firstPrefix = secondPrefix;
						secondPrefix = "m";
						otherQueryId = constraint.getSecondQuery().getQueryId();
						masterTableQuery = tempTableQuery;
						tempTableQuery = constraint.getFirstQuery();
						switchedPrefix = true;
					}
			
					String dateConstraintSql = buildDateSqlForMaster(masterTableQuery, encounterConstraint);
					
					if (dateConstraintSql!=null&&dateConstraintSql.trim().length()>0){
					
						if (firstConstraint){
							timingSqlBuf.append(" where ");
							firstConstraint = false;
						}
						else
							timingSqlBuf.append(" and ");
						
						
						timingSqlBuf.append(" exists (" + 
								"select 1 " +
								"from (" + dateConstraintSql + ") m "); 
				
						
						String newPrefix = "t";
						String columnName = getColumnNameFromType(tempTableQuery.getJoinColumn());
						if (tempTableQuery.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST)){
							timingSqlBuf.append(", (select m.patient_num " + 
									(encounterConstraint?", m.encounter_num ":"") +
									", min(m." + columnName + ") " + columnName + " ");
							if (useSqlServerTempTables()){
								timingSqlBuf.append("from #m" + this.buildSubQueryId(tempTableQuery.getQueryId()) + " m ");
							}
							else {
								timingSqlBuf.append("from " + parent.getMasterTempTableName() + " m " +
									"where m.master_id = '" + this.buildSubQueryId(tempTableQuery.getQueryId()) + "' ");
							}
							timingSqlBuf.append("group by m.patient_num" +
									(encounterConstraint?", m.encounter_num ":"") +
									") t2 " +
									"where m.patient_num = t2.patient_num " + 
									(encounterConstraint?"and m.encounter_num = t2.encounter_num ":"") +
									"and t.patient_num = t2.patient_num " +
									(encounterConstraint?"and t.encounter_num = t2.encounter_num ":"") +
									"and ");	
							newPrefix = "t2";
						}
						else if (tempTableQuery.getAggregateOperator().equals(QueryAggregateOperatorType.LAST)){
							timingSqlBuf.append(", (select m.patient_num, max(m." + columnName + ") " + columnName + " ");
							if (useSqlServerTempTables()){
								timingSqlBuf.append("from #m" + this.buildSubQueryId(tempTableQuery.getQueryId()) + " m ");
							}
							else {
								timingSqlBuf.append("from " + parent.getMasterTempTableName() + " m " +
									"where m.master_id = '" + this.buildSubQueryId(tempTableQuery.getQueryId()) + "' ");
							}
							timingSqlBuf.append("group by m.patient_num) t2 " +
									"where m.patient_num = t2.patient_num " + 
									(encounterConstraint?"and m.encounter_num = t2.encounter_num ":"") +
									"and t.patient_num = t2.patient_num " +
									(encounterConstraint?"and t.encounter_num = t2.encounter_num ":"") +
									"and ");	
							newPrefix = "t2";
						}
						else {
							timingSqlBuf.append("where ");
						}
						
						if (firstPrefix.equals("t"))
							firstPrefix = newPrefix;
						else
							secondPrefix = newPrefix;
				
						timingSqlBuf.append("m.patient_num = t.patient_num " +
								(encounterConstraint?"and m.encounter_num = t.encounter_num ":""));
						timingSqlBuf.append("and " + firstPrefix + "." + firstColumn + " ");
						
						QueryOperatorType dateOperator = constraint.getOperator();
						if (dateOperator.equals(QueryOperatorType.EQUAL))
							timingSqlBuf.append("= ");
						else if (dateOperator.equals(QueryOperatorType.GREATER))
							timingSqlBuf.append("> ");
						else if (dateOperator.equals(QueryOperatorType.GREATEREQUAL))
							timingSqlBuf.append(">= ");
						else if (dateOperator.equals(QueryOperatorType.LESS))
							timingSqlBuf.append("< ");
						else if (dateOperator.equals(QueryOperatorType.LESSEQUAL))
							timingSqlBuf.append("<= ");
				
						timingSqlBuf.append(secondPrefix + "." + secondColumn + " ");
										
						if (constraint.getSpan()!=null&&constraint.getSpan().size()>0){
							for (QuerySpanConstraintType spanConstraint : constraint.getSpan())
							{
								String spanFirstPrefix = firstPrefix;
								String spanFirstColumn = firstColumn;
								String spanSecondPrefix = secondPrefix;
								String spanSecondColumn = secondColumn;
									
								
								String spanOperator = "=";
								QueryOperatorType spanOpType = spanConstraint.getOperator();
								if (spanConstraint.getOperator()!=null){
									if (spanOpType.equals(QueryOperatorType.EQUAL))
										spanOperator = "= ";
									else if (spanOpType.equals(QueryOperatorType.GREATER))
										spanOperator = "> ";
									else if (spanOpType.equals(QueryOperatorType.GREATEREQUAL))
										spanOperator = ">= ";
									else if (spanOpType.equals(QueryOperatorType.LESS))
										spanOperator = "< ";
									else if (spanOpType.equals(QueryOperatorType.LESSEQUAL))
										spanOperator = "<= ";				
								}
								
								String units = spanConstraint.getUnits();
								String sqlUnits = "";
								int span = spanConstraint.getSpanValue();
								
								if (units!=null){
									
									if (dateOperator.equals(QueryOperatorType.GREATER)||dateOperator.equals(QueryOperatorType.GREATEREQUAL)){
										String spanPrefix = spanFirstPrefix;
										String spanColumn = spanFirstColumn;
										spanFirstPrefix = spanSecondPrefix;
										spanFirstColumn = spanSecondColumn;
										spanSecondPrefix = spanPrefix;
										spanSecondColumn = spanColumn;
									}
									
									if (TemporalQuerySpanUnits.valueOf(units)==TemporalQuerySpanUnits.YEAR){
										if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
											sqlUnits = "ADD_MONTHS(" + spanFirstPrefix + "." + spanFirstColumn + ", (12 * " + span  + "))";
										else if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
											sqlUnits = "DATEADD(YEAR, (" + span + "), " + spanFirstPrefix + "." + spanFirstColumn + ")";
										else if (parent.getServerType().equals(DAOFactoryHelper.POSTGRESQL))
											sqlUnits = " " + spanFirstPrefix + "." + spanFirstColumn + " + cast('" + span + " years' as interval) ";

									}
									else if (TemporalQuerySpanUnits.valueOf(units)==TemporalQuerySpanUnits.MONTH){
										if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
											sqlUnits = "ADD_MONTHS(" + spanFirstPrefix + "." + spanFirstColumn + ", (" + span  + "))";
										else if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
											sqlUnits = "DATEADD(MONTH, (" + span + "), " + spanFirstPrefix + "." + spanFirstColumn + ")";
										else if (parent.getServerType().equals(DAOFactoryHelper.POSTGRESQL))
											sqlUnits = " " + spanFirstPrefix + "." + spanFirstColumn + " + cast('" + span + " months' as interval) ";
									}
									else if (TemporalQuerySpanUnits.valueOf(units)==TemporalQuerySpanUnits.DAY){
										if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
											sqlUnits = "(" + spanFirstPrefix + "." + spanFirstColumn + " + (" + span  + "))";
										else if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
											sqlUnits = "DATEADD(DAY, (" + span  + "), " + spanFirstPrefix + "." + spanFirstColumn + ")";
										else if (parent.getServerType().equals(DAOFactoryHelper.POSTGRESQL))
											sqlUnits = " " + spanFirstPrefix + "." + spanFirstColumn + " + cast('" + span + " days' as interval) ";
									}
									else if (TemporalQuerySpanUnits.valueOf(units)==TemporalQuerySpanUnits.HOUR){
										if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
											sqlUnits = "(" + spanFirstPrefix + "." + spanFirstColumn + " + (1/24 * " + span  + "))";
										else if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
											sqlUnits = "DATEADD(HOUR, (" + span  + "), " + spanFirstPrefix + "." + spanFirstColumn + ")";	
										else if (parent.getServerType().equals(DAOFactoryHelper.POSTGRESQL))
											sqlUnits = " " + spanFirstPrefix + "." + spanFirstColumn + " + cast('" + span + " hours' as interval) ";
									}
									else if (TemporalQuerySpanUnits.valueOf(units)==TemporalQuerySpanUnits.MINUTE){
										if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
											sqlUnits = "(" + spanFirstPrefix + "." + spanFirstColumn + " + (1/1440 * " + span  + "))";
										else if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
											sqlUnits = "DATEADD(MINUTE, (" + span  + "), " + spanFirstPrefix + "." + spanFirstColumn + ")";	
										else if (parent.getServerType().equals(DAOFactoryHelper.POSTGRESQL))
											sqlUnits = " " + spanFirstPrefix + "." + spanFirstColumn + " + cast('" + span + " minutes' as interval) ";
									}
									else if (TemporalQuerySpanUnits.valueOf(units)==TemporalQuerySpanUnits.SECOND){
										if (parent.getServerType().equals(DAOFactoryHelper.ORACLE))
											sqlUnits = "(" + spanFirstPrefix + "." + spanFirstColumn + " + (1/86400 * " + span  + "))";
										else if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
											sqlUnits = "DATEADD(SECOND, (" + span  + "), " + spanFirstPrefix + "." + spanFirstColumn + ")";	
										else if (parent.getServerType().equals(DAOFactoryHelper.POSTGRESQL))
											sqlUnits = " " + spanFirstPrefix + "." + spanFirstColumn + " + cast('" + span + " seconds' as interval) ";
									}
								}
								
								if (sqlUnits.trim().length()>0)
									timingSqlBuf.append("and " + spanSecondPrefix + "." + spanSecondColumn + " " + spanOperator + " " + sqlUnits);
							}
							
						}
					}
					
					timingSqlBuf.append(")");
				}
			}
		}
		return timingSqlBuf.toString();
	}
	
	private String getTempQueryConstraint(QueryJoinType tempJoin, int panelCount){				
		boolean useTempTables = false;
		if (parent.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)&&
				parent.getQueryOptions().getQueryConstraintLogic()==QueryConstraintStrategy.TEMP_TABLES){
			useTempTables = true;
		}
		
		StringBuilder tempClause = new StringBuilder();
		if (tempJoin!=null){
			String columnName = "";
			String columnAlias = "";
			String temporalTableAlias = "f";
			String temporalJoinTable = "observation_fact";
			if (tempJoin.getJoinColumn().equals(QueryJoinColumnType.STARTDATE)){
				columnName = "start_date";
				columnAlias = "temporal_start_date";
				temporalTableAlias = "f";
				temporalJoinTable = "observation_fact";
			}
			else if (tempJoin.getJoinColumn().equals(QueryJoinColumnType.ENDDATE)){
				columnName = "end_date";
				columnAlias = "temporal_end_date";
				temporalTableAlias = "f";
				temporalJoinTable = "observation_fact";
			}
			else if (tempJoin.getJoinColumn().equals(QueryJoinColumnType.ENCOUNTER_STARTDATE)){
				columnName = "start_date";
				columnAlias = "temporal_start_date";
				temporalTableAlias = "v";
				temporalJoinTable = "visit_dimension";
			}
			else if (tempJoin.getJoinColumn().equals(QueryJoinColumnType.ENCOUNTER_ENDDATE)){
				columnName = "end_date";
				columnAlias = "temporal_end_date";
				temporalTableAlias = "v";
				temporalJoinTable = "visit_dimension";
			}
						
			if (columnName.trim().length()>0){
				if (tempJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST)){
					tempClause.append("select " + temporalTableAlias + ".patient_num, " + 
							"min(" + temporalTableAlias + "." + columnName + ") " + columnAlias + " " +
							"from " + parent.getDatabaseSchema() + temporalJoinTable + " " + temporalTableAlias + " ");
				}
				else if (tempJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST)){
					tempClause.append("select " + temporalTableAlias + ".patient_num, max(" + temporalTableAlias + "." + columnName + ") " + columnAlias + " " +
							"from " + parent.getDatabaseSchema() + temporalJoinTable + " " + temporalTableAlias + " ");
				}
				else{
					tempClause.append("select " + temporalTableAlias + ".patient_num, " + temporalTableAlias + ".encounter_num, " + temporalTableAlias + "." + columnName + " " + columnAlias + " " +
							"from " + parent.getDatabaseSchema() + temporalJoinTable + " " + temporalTableAlias + " ");
				}
				
				String tableAlias = "cnst";
			
				if (parent.getQueryOptions().getTemporalConstraintStrategy()==TemporalConstraintStrategy.TEMP_TABLE_UPDATE){
					tempClause.append(", " + getTempTableName() + " cnst ");						
				}
				else if (useTempTables){
					tempClause.append(", #t cnst ");
				}
				else {
					tempClause.append(", t cnst ");
				}
				
				
				tempClause.append("where " + tableAlias + ".patient_num = " + temporalTableAlias + ".patient_num ");
				if (this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.ANY)){
					tempClause.append("and " + tableAlias + ".encounter_num = " + temporalTableAlias + ".encounter_num ");						
				}							
				if (parent.getQueryOptions().getTemporalConstraintStrategy()==TemporalConstraintStrategy.TEMP_TABLE_UPDATE){
					tempClause.append("and " + tableAlias + ".panel_count = " + String.valueOf(panelCount) + " ");
				}
			
				if ((tempJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST))||
						(tempJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST)))
					tempClause.append("group by " + temporalTableAlias + ".patient_num ");	
				else
					tempClause.append("group by " + temporalTableAlias + ".patient_num, " + temporalTableAlias + ".encounter_num, v." + columnName + " ");
			}
		}
		return tempClause.toString();
	}
	
	private String buildDateSqlForMaster(QueryJoinType masterJoin, boolean includeEncounter){
		String columnName = getColumnNameFromType(masterJoin.getJoinColumn());

		String encounterColumn = (includeEncounter?", m.encounter_num ":"");
		
		String selectStatement = "select m.patient_num " + encounterColumn + ", m." + columnName + " ";
		String fromStatement = "from " + parent.getMasterTempTableName() + " m ";
		String whereStatement = "where m.master_id = '" + this.buildSubQueryId(masterJoin.getQueryId()) + "' ";
		String groupByStatement = "group by m.patient_num " + encounterColumn + " ";
		
		if (useSqlServerTempTables()){
			fromStatement = "from #m" + this.buildSubQueryId(masterJoin.getQueryId()) + " m ";
			whereStatement = "";
		}
		
		if (!masterJoin.getJoinColumn().equals(QueryJoinColumnType.INSTANCE)){
			if (masterJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST)){
				selectStatement = "select m.patient_num " + encounterColumn + ", min(m." + columnName + ") " + columnName + " ";
			}
			else if (masterJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST)){
				selectStatement = "select m.patient_num " + encounterColumn + ", max(m." + columnName + ") " + columnName + " ";
			}
			else {
				selectStatement = "select m.patient_num " + encounterColumn + ", m." + columnName + " ";
				if (whereStatement.trim().length()>0)
					whereStatement += "and m.level_no = 1 ";
				else
					whereStatement = "where m.level_no = 1 ";
				groupByStatement = "";
			}
		}
		else {
			selectStatement = "select " + columnName + ", temporal_start_date, temporal_end_date ";
			groupByStatement = "";
		}		
		
		return selectStatement + " " + 
			fromStatement + " " +
			whereStatement + " " +
			groupByStatement;
	}
	
	/*private String getConstrainedDateSqlFromMaster(QueryJoinType masterJoin){
		String columnName = getColumnNameFromType(masterJoin.getJoinColumn());

		if (!masterJoin.getJoinColumn().equals(QueryJoinColumnType.INSTANCE)){
			if (masterJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST))
				return "select m.patient_num, min(m." + columnName + ") " + columnName + " " +
						"from " + parent.getMasterTempTableName() + " m " +
						"where m.master_id = '" + this.buildSubQueryId(masterJoin.getQueryId()) + "' " +
						"group by m.patient_num";	
			else if (masterJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST))
				return "select m.patient_num, max(m." + columnName + ") " + columnName + " " +
					"from " + parent.getMasterTempTableName() + " m " +
					"where m.master_id = '" + this.buildSubQueryId(masterJoin.getQueryId()) + "' " +
					"group by m.patient_num";	
			else
				return "select m.patient_num, m." + columnName + " " +
					"from " + parent.getMasterTempTableName() + " m " +
					"where m.master_id = '" + this.buildSubQueryId(masterJoin.getQueryId()) + "' " +
					"and m.level_no = 1 ";
		}
		else
			return "select " + columnName + ", temporal_start_date, temporal_end_date " +
				"from " + parent.getMasterTempTableName() + " m " +
				"where m.master_id = '" + this.buildSubQueryId(masterJoin.getQueryId()) + "' ";
	}*/
	
	protected String getColumnNameFromType(QueryJoinColumnType columnType){
		if (columnType!=null){
			if (columnType.equals(QueryJoinColumnType.ENCOUNTER))
				return "encounter_num";
			else if (columnType.equals(QueryJoinColumnType.STARTDATE))
				return "temporal_start_date";
			else if (columnType.equals(QueryJoinColumnType.ENDDATE))
				return "temporal_end_date";
			else if (columnType.equals(QueryJoinColumnType.ENCOUNTER_STARTDATE))
				return "temporal_start_date";
			else if (columnType.equals(QueryJoinColumnType.ENCOUNTER_ENDDATE))
				return "temporal_end_date";
			else if (columnType.equals(QueryJoinColumnType.INSTANCE))
				return "provider_id, start_date, concept_cd, instance_num, encounter_num, patient_num";
		}
		return "patient_num";
	}
	
	protected String buildMoveToMasterSql(int level)   {
		String tempTableName = parent.getTempTableName();
		String masterTableName = parent.getMasterTempTableName();
		StringBuilder masterSql = new StringBuilder();
		
		StringBuilder insertClause = new StringBuilder();
		if (!useSqlServerTempTables()){
			insertClause.append(" insert into " + masterTableName);
			insertClause.append("(master_id, patient_num, level_no");
			if (rtnColumns.contains(TemporalQueryReturnColumns.ENCOUNTER)){
				insertClause.append(", encounter_num");			
			}
			if (rtnColumns.contains(TemporalQueryReturnColumns.INSTANCE)){
				if (!rtnColumns.contains(TemporalQueryReturnColumns.ENCOUNTER)){
					insertClause.append(", encounter_num");			
				}
				insertClause.append(", provider_id, start_date, concept_cd, instance_num");			
			}		
			if (returnInstanceStartDate()||returnEncounterStartDate()){
				insertClause.append(", temporal_start_date");
			}
			if (returnInstanceEndDate()||returnEncounterEndDate()){
				insertClause.append(", temporal_end_date");
			}
			insertClause.append(") ");
		}
		else {
			masterSql.append(parent.buildTempTableCheckDrop("#m" + getSubQueryId()));
			masterSql.append(parent.getSqlDelimiter());
		}
		
		
		String schema = getDatabaseSchema();
		if (schema==null)
			schema = "";
		else if (!schema.endsWith("."))
			schema += ".";
		
		String primaryTableAlias = "t.";
		StringBuilder fromClause = new StringBuilder("from " + tempTableName + " t ");
		StringBuilder whereClause = new StringBuilder(" where t.panel_count = " + this.getEndPanelIndex() + " ");	
		if ((((rtnColumns.contains(TemporalQueryReturnColumns.INSTANCE)||
			returnInstanceStartDate()))&&
			(!this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)))||
			(returnInstanceEndDate())){			
			primaryTableAlias = "f.";			
			fromClause.append(", " + schema + "observation_fact f ");
			whereClause.append("and f.patient_num = t.patient_num ");
			if (this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAME)||
				this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)){
				whereClause.append("and f.encounter_num = t.encounter_num ");
			}
			else if (this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)){
				whereClause.append("and f.encounter_num = t.encounter_num ");
				whereClause.append("and f.provider_id = t.provider_id ");
				whereClause.append("and f.start_date = t.start_date ");
				whereClause.append("and f.instance_num = t.instance_num ");
				whereClause.append("and f.concept_cd = t.concept_cd ");
			}
			
			if (returnEncounterEndDate()||returnEncounterStartDate()){
				fromClause.append(", " + schema + "visit_dimension v ");
				whereClause.append("and f.patient_num = v.patient_num ");
				whereClause.append("and f.encounter_num = v.encounter_num ");
			}
		}
		else if ((rtnColumns.contains(TemporalQueryReturnColumns.ENCOUNTER)&&
				!this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAME)&&
				!this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)&&
				!this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM))||
				returnEncounterStartDate()||
				returnEncounterEndDate()){
			primaryTableAlias = "v.";			
			fromClause.append(", " + schema + "visit_dimension v ");
			whereClause.append("and v.patient_num = t.patient_num ");
			if (!this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.ANY)){
				whereClause.append("and v.encounter_num = t.encounter_num ");
			}
		}

		boolean aggregate = false;
		StringBuilder selectClause = new StringBuilder();
		if (useSqlServerTempTables()){
			selectClause.append("select t.patient_num, " + String.valueOf(level) + " level_no ");
		}
		else {
			selectClause.append("select '" + getSubQueryId() + "', t.patient_num, " + String.valueOf(level) + " ");
		}
		StringBuilder groupByClause = new StringBuilder("group by t.patient_num ");
		if (rtnColumns.contains(TemporalQueryReturnColumns.INSTANCE)){
			selectClause.append(", " + primaryTableAlias + "encounter_num, " + 
						primaryTableAlias + "provider_id, " + 
						primaryTableAlias + "start_date, " + 
						primaryTableAlias + "concept_cd, " + 
						primaryTableAlias + "instance_num");
			groupByClause.append(", " + primaryTableAlias + "encounter_num, " + 
					primaryTableAlias + "provider_id, " + 
					primaryTableAlias + "start_date, " + 
					primaryTableAlias + "concept_cd, " + 
					primaryTableAlias + "instance_num");
		}
		else if (rtnColumns.contains(TemporalQueryReturnColumns.ENCOUNTER)){			
			selectClause.append(", " + primaryTableAlias + "encounter_num");
			groupByClause.append(", " + primaryTableAlias + "encounter_num");
		}
		
		TemporalQueryReturnColumns startDateRtn = getStartDateReturnType();
		if (startDateRtn!=null){
			if (startDateRtn==TemporalQueryReturnColumns.FIRST_START_DATE){
				selectClause.append(", min(" + primaryTableAlias + "start_date) temporal_start_date ");
				aggregate = true;
			}
			else if (startDateRtn==TemporalQueryReturnColumns.LAST_START_DATE){
				selectClause.append(", max(" + primaryTableAlias + "start_date) temporal_start_date ");
				aggregate = true;
			}
			else if (startDateRtn==TemporalQueryReturnColumns.START_DATE){
				selectClause.append(", " + primaryTableAlias + "start_date temporal_start_date ");			
				groupByClause.append(", " + primaryTableAlias + "start_date");
			}
			else if (startDateRtn==TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE){
				selectClause.append(", min(" + primaryTableAlias + "start_date) temporal_start_date ");
				aggregate = true;
			}
			else if (startDateRtn==TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE){
				selectClause.append(", max(" + primaryTableAlias + "start_date) temporal_start_date ");
				aggregate = true;
			}
			else if (startDateRtn==TemporalQueryReturnColumns.ENCOUNTER_START_DATE){
				selectClause.append(", " + primaryTableAlias + "start_date temporal_start_date ");			
				groupByClause.append(", " + primaryTableAlias + "start_date");
			}
		}
		
		TemporalQueryReturnColumns endDateRtn = getEndDateReturnType();
		if (endDateRtn!=null){
			if (endDateRtn==TemporalQueryReturnColumns.FIRST_END_DATE){
				selectClause.append(", min(" + primaryTableAlias + "end_date) temporal_end_date ");
				aggregate = true;
			}
			else if (endDateRtn==TemporalQueryReturnColumns.LAST_END_DATE){
				selectClause.append(", max(" + primaryTableAlias + "end_date) temporal_end_date ");
				aggregate = true;
			}
			else if (endDateRtn==TemporalQueryReturnColumns.END_DATE){
				selectClause.append(", " + primaryTableAlias + "end_date temporal_end_date ");					
				groupByClause.append(", " + primaryTableAlias + "end_date");
			}
			else if (endDateRtn==TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE){
				selectClause.append(", min(" + primaryTableAlias + "end_date) temporal_end_date ");
				aggregate = true;
			}
			else if (endDateRtn==TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE){
				selectClause.append(", max(" + primaryTableAlias + "end_date) temporal_end_date ");
				aggregate = true;
			}
			else if (endDateRtn==TemporalQueryReturnColumns.ENCOUNTER_END_DATE){
				selectClause.append(", " + primaryTableAlias + "end_date temporal_end_date ");					
				groupByClause.append(", " + primaryTableAlias + "end_date");
			}
		}

		String intoClause = "";
		if (useSqlServerTempTables()){
			intoClause = " into #m" + getSubQueryId() + " ";
		}
		
		String indexClause = "";
		if (useSqlServerTempTables()){
			indexClause = "CREATE NONCLUSTERED INDEX m" + getSubQueryId() + "_idx on #m" + getSubQueryId() + " (patient_num, " + 
					(startDateRtn!=null ? "temporal_start_date, " : "") + 
					(endDateRtn!=null ? "temporal_end_date, " : "") + 
					"level_no)";
		}
		
		masterSql.append(insertClause.toString());
		masterSql.append(selectClause.toString());
		masterSql.append(intoClause);
		masterSql.append(fromClause.toString());
		masterSql.append(whereClause.toString());
		if (aggregate)
			masterSql.append(groupByClause.toString());
		
		if (indexClause.trim().length()>0){
			masterSql.append(getSqlDelimiter());
			masterSql.append(indexClause);
		}
		
		return masterSql.toString();
	}
	
	private boolean useSqlServerTempTables(){
		return parent.useSqlServerTempTables();
	}
	
	protected String getDeleteTempTableSql()  { 
		String tempTableName = parent.getTempTableName();
		
		if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
			return "truncate table "+ tempTableName;
		else
			return "delete  "+
					(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)? " from " : "") + tempTableName;
	}
	
	protected String getDeleteDxTempTableSql()  { 
		String tempTableName = parent.getDxTempTableName();
		
		if (parent.getServerType().equals(DAOFactoryHelper.SQLSERVER))
			return "truncate table "+ tempTableName;
		else
			return "delete  "+
					(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)? " from " : "") + tempTableName;
	}
	
	protected String getDeleteTempMasterSql(String masterId, int level) {
		String masterTableName = parent.getMasterTempTableName();
		
		if (useSqlServerTempTables()){
			return "if (object_id(#m" + masterId + ") is not null) drop table #m" + masterId + "\n" + 
					" else delete " + masterTableName + " " +
					"where master_id = '" + masterId + "' " + 
					"and level_no >= " + String.valueOf(level);			
		}
		else
			return "delete " +
			(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)? " from " : "") +
			masterTableName + " " +
					"where master_id = '" + masterId + "' " + 
					"and level_no >= " + String.valueOf(level);
	}

	public List<TemporalPanel> getPanelList() {
		return panelList;
	}

	public int getPanelCount(){
		return this.panelList.size();
	}
	
	public boolean allAreInverted(){
		return this.invertedPanelCount == this.panelList.size();
	}
	
	public int getInvertedPanelCount(){
		return this.invertedPanelCount;
	}
	
	public int getNonInvertedPanelCount(){
		return this.panelList.size() - this.invertedPanelCount;
	}
	
	protected SecurityType getSecurityType() {
		return parent.getSecurityType();
	}

	protected SecurityType getRequestorSecurityType() {
		return parent.getRequestorSecurityType();
	}

	protected String getProjectId() {
		return parent.getProjectId();
	}
	
	protected DataSourceLookup getDataSourceLookup(){
		return parent.getDataSourceLookup();
	}
	
	protected String getDatabaseSchema(){
		return parent.getDatabaseSchema();
	}
	
	protected String getParentQueryTiming(){
		return parent.getQueryTiming();
	}
	
	protected String getQueryTiming(){
		String timing = subQuery.getQueryTiming();
		if (timing==null||timing.trim().length()==0)
			timing = parent.getQueryTiming();
		if (timing==null||timing.trim().length()==0)
			timing = QueryTimingHandler.ANY;
		return timing;
	}
	
	protected String getServerType(){
		return parent.getServerType();
	}
	
	protected Map getProjectParameterMap(){
		return parent.getProjectParameterMap();
	}
	
	protected QueryTimingHandler getTimingHandler(){
		return parent.getTimingHandler();
	}
	
	protected int getPanelIndex(TemporalPanel panel){
		if (panel==null||panelList==null)
			return -1;
		else
			return this.panelList.indexOf(panel);
	}
	
	protected String getTempTableName(){
		return parent.getTempTableName();
	}
	
	protected boolean allowLargeTextValueConstrainFlag(){
		return parent.allowLargeTextValueConstrainFlag();
	}
	
	protected boolean hasInvertedPanel(){
		return invertedPanelCount>0;
	}
	
	private String buildSubQueryId(String eventId){
		if (eventId==null)
			return null;
		else{
			int index = parent.getSubQueryIndex(eventId);
			return parent.getQueryId() + "_S" + String.valueOf(index);
		}
	}
	
	protected String getSubQueryId(){
		if (this.subQueryId==null)
			subQueryId = buildSubQueryId(getQueryId());
		return subQueryId;
	}
	
	protected String getQueryId(){
		if (subQuery==null)
			return null;
		else
			return subQuery.getQueryId();
	}
	
	protected String buildTempTableCheckDrop(String tempTableName) {
		return parent.buildTempTableCheckDrop(tempTableName);
	}
	
	protected boolean returnEncounterNum(){
		if (this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)||
				this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAME))
			return true;
		else
			return false;
	}
		
	protected boolean returnInstanceNum(){
		if (this.getQueryTiming().equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM))
			return true;
		else
			return false;
	}
		
	protected boolean returnEncounterStartDate(){
		if (rtnColumns.contains(TemporalQueryReturnColumns.ENCOUNTER_START_DATE) ||
			rtnColumns.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE) ||
			rtnColumns.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE))
			return true;
		else
			return false;
	}
		
	protected boolean returnEncounterEndDate(){
		if (rtnColumns.contains(TemporalQueryReturnColumns.ENCOUNTER_END_DATE) ||
				rtnColumns.contains(TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE) ||
				rtnColumns.contains(TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE))
			return true;
		else
			return false;
	}
	
	protected boolean returnInstanceStartDate(){
		if (rtnColumns.contains(TemporalQueryReturnColumns.START_DATE) ||
			rtnColumns.contains(TemporalQueryReturnColumns.FIRST_START_DATE) ||
			rtnColumns.contains(TemporalQueryReturnColumns.LAST_START_DATE))
			return true;
		else
			return false;
	}
		
	protected boolean returnInstanceEndDate(){
		if (rtnColumns.contains(TemporalQueryReturnColumns.END_DATE) ||
				rtnColumns.contains(TemporalQueryReturnColumns.FIRST_END_DATE) ||
				rtnColumns.contains(TemporalQueryReturnColumns.LAST_END_DATE))
			return true;
		else
			return false;
	}
	
	protected int getProcessingLevel(){
		return parent.getProcessingLevel();
	}
		
	public String getSqlDelimiter() {
		return parent.getSqlDelimiter();
	}
	
	protected int getEndPanelIndex() {
		if (endPanelIndex==-1){
			int nonInvertedPanels = getPanelCount() - getInvertedPanelCount();
			if (!this.isFirstSubQuery()&&(parent.getQueryOptions().getTemporalConstraintStrategy()==TemporalConstraintStrategy.TEMP_TABLE_UPDATE))
				nonInvertedPanels++;
			endPanelIndex = this.startPanelIndex + (nonInvertedPanels>0?nonInvertedPanels-1:0);
		}
		return endPanelIndex;
	}

	
	private List<QueryConstraintType> getConstraintsForSubQuery(TemporalSubQuery subQuery){
		if (subQuery!=null){
			String eventId = subQuery.getQueryId();
			if (this.constraints!=null){
				return constraints.get(eventId);
			}
		}
		return null;
	}
	
	protected TemporalQueryOptions getQueryOptions() {
		return parent.getQueryOptions();
	}
	
	public void addIgnoredMessage(String errorMessage) {
		parent.addIgnoredMessage(errorMessage);
	}

	protected void addPreProcessingSql(String sql){
		if (sql!=null&&sql.trim().length()>0)
			this.preProcessingSql.add(sql);
	}

	protected void addPostProcessingSql(String sql){
		if (sql!=null&&sql.trim().length()>0)
			this.postProcessingSql.add(sql);
	}
	
	protected QueryDefinitionType searchForQueryInRequestDefinition(String subQueryId){
		return parent.searchForSubQuery(subQueryId);
	}

	
	public boolean occursTogether(TemporalSubQuery subQuery){
		String eventId = subQuery.getQueryId();
		List<QueryConstraintType> constraintList = getConstraintsForSubQuery(subQuery);
		if (constraintList!=null){
			for(QueryConstraintType constraint : constraintList){
				if (((constraint.getFirstQuery().getQueryId()!=null&constraint.getFirstQuery().getQueryId().equalsIgnoreCase(eventId))||
						(constraint.getSecondQuery().getQueryId()!=null&constraint.getSecondQuery().getQueryId().equalsIgnoreCase(eventId)))&&
					(constraint.getOperator().equals(QueryOperatorType.EQUAL)||constraint.getOperator().equals(QueryOperatorType.GREATEREQUAL)||constraint.getOperator().equals(QueryOperatorType.LESSEQUAL))){
					return true;
				}													
			}
		}
		return false;
	}
	
	public boolean occursAfter(TemporalSubQuery subQuery){
		String eventId = subQuery.getQueryId();
		List<QueryConstraintType> constraintList = getConstraintsForSubQuery(subQuery);
		if (constraintList!=null){
			for(QueryConstraintType constraint : constraintList){
				if ((constraint.getSecondQuery().getQueryId()!=null&&constraint.getSecondQuery().getQueryId().equalsIgnoreCase(eventId))&&
					(constraint.getOperator().equals(QueryOperatorType.GREATER)||constraint.getOperator().equals(QueryOperatorType.GREATEREQUAL))){
					return true;
				}							
				else if ((constraint.getFirstQuery().getQueryId()!=null&&constraint.getFirstQuery().getQueryId().equalsIgnoreCase(eventId))&&
						(constraint.getOperator().equals(QueryOperatorType.LESS)||constraint.getOperator().equals(QueryOperatorType.LESSEQUAL))){
					return true;
				}
			}
			
		}
		return false;
	}
	
	public boolean occursBefore(TemporalSubQuery subQuery){
		String eventId = subQuery.getQueryId();
		List<QueryConstraintType> constraintList = getConstraintsForSubQuery(subQuery);
		if (constraintList!=null){
			for(QueryConstraintType constraint : constraintList){
				if ((constraint.getSecondQuery().getQueryId()!=null&&constraint.getSecondQuery().getQueryId().equalsIgnoreCase(eventId))&&
					(constraint.getOperator().equals(QueryOperatorType.LESS)||constraint.getOperator().equals(QueryOperatorType.LESSEQUAL))){
					return true;
				}							
				else if ((constraint.getFirstQuery().getQueryId()!=null&&constraint.getFirstQuery().getQueryId().equalsIgnoreCase(eventId))&&
						(constraint.getOperator().equals(QueryOperatorType.GREATER)||constraint.getOperator().equals(QueryOperatorType.GREATEREQUAL))){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int compareTo(Object element) {		
		if (element.getClass().equals((TemporalSubQuery.class))) {
			TemporalSubQuery tp2 = (TemporalSubQuery) element;
			boolean before = occursBefore(tp2);
			boolean after = occursAfter(tp2);
			if (before&&!after)
				return -1;
			else if (after&&!before)
				return 1;
			else {
				return this.panelList.get(0).compareTo(tp2.panelList.get(0));
			}
		} else {
            return this.toString().compareTo(element.toString());
        }
	}
	

}
