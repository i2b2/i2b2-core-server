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

import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalSubQuery.TemporalQueryReturnColumns;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryJoinColumnType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryJoinType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryAggregateOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryConstraintType;

public class TemporalQueryConstraintMapping {

	private HashMap<String, HashMap<String, List<QueryConstraintType>>> constraints = new HashMap<String, HashMap<String, List<QueryConstraintType>>>();
	private HashMap<String, EnumSet<TemporalSubQuery.TemporalQueryReturnColumns>> returnColumns = new HashMap<String, EnumSet<TemporalSubQuery.TemporalQueryReturnColumns>>();
	private ArrayList<String> orderedSet = new ArrayList<String>();
	
	public TemporalQueryConstraintMapping(QueryDefinitionType queryDef){
		parseConstraints(queryDef);
	}
	
	public HashMap<String, List<QueryConstraintType>> getConstraintsForQuery(String eventId){
		if (eventId==null)
			return null;
		else
			return constraints.get(eventId);
	}
	
	public EnumSet<TemporalSubQuery.TemporalQueryReturnColumns> getReturnColumnsForQuery(String eventId){
		if (eventId==null)
			return null;
		else
			return returnColumns.get(eventId);
	}
	
	public List<String> getOrderedQueryList(){
		return orderedSet;
	}
	
	private void parseConstraints(QueryDefinitionType queryDef){
		if (queryDef.getSubqueryConstraint()!=null&&queryDef.getSubqueryConstraint().size()>0){
			
			//first, preprocess constraints to figure out which queries have the most
			HashMap<String, Integer> constraintCounts = new HashMap<String, Integer>();
			for (QueryConstraintType constraint : queryDef.getSubqueryConstraint()){

				Integer firstCount = constraintCounts.get(constraint.getFirstQuery().getQueryId());
				if (firstCount==null)
					firstCount = 1;
				else 
					firstCount++;
				constraintCounts.put(constraint.getFirstQuery().getQueryId(), firstCount);
				
				Integer secondCount = constraintCounts.get(constraint.getSecondQuery().getQueryId());
				if (secondCount==null)
					secondCount = 1;
				else 
					secondCount++;
				constraintCounts.put(constraint.getSecondQuery().getQueryId(), secondCount);				
			}
			
			for (QueryConstraintType constraint : queryDef.getSubqueryConstraint()){
				
				//rule one: process first and last prior to an any
				//rule two: query with fewer constraints should be processed first
				//rule three: first occurring query in constraint should go first
				
				String firstQuery = constraint.getFirstQuery().getQueryId();
				String secondQuery = constraint.getSecondQuery().getQueryId();
				if ((constraint.getFirstQuery().getAggregateOperator()==QueryAggregateOperatorType.ANY)&&
					((constraint.getSecondQuery().getAggregateOperator()==QueryAggregateOperatorType.FIRST)||
					(constraint.getSecondQuery().getAggregateOperator()==QueryAggregateOperatorType.LAST))){
					firstQuery = constraint.getSecondQuery().getQueryId();
					secondQuery = constraint.getFirstQuery().getQueryId();					
				}
				else if ((constraint.getSecondQuery().getAggregateOperator()==QueryAggregateOperatorType.ANY)&&
					((constraint.getFirstQuery().getAggregateOperator()==QueryAggregateOperatorType.FIRST)||
					(constraint.getFirstQuery().getAggregateOperator()==QueryAggregateOperatorType.LAST))){
					firstQuery = constraint.getFirstQuery().getQueryId();
					secondQuery = constraint.getSecondQuery().getQueryId();					
				}
				else {
					Integer firstConstraints = -1;
					Integer secondConstraints = -1;
					if (constraintCounts.get(firstQuery)!=null)
						firstConstraints = constraintCounts.get(firstQuery);
					if (constraintCounts.get(secondQuery)!=null)
						secondConstraints = constraintCounts.get(secondQuery);
					
					if (firstConstraints==null)
						firstConstraints = -1;
					if (secondConstraints==null)
						secondConstraints = -1;
					
					if (firstConstraints<secondConstraints){
						firstQuery = constraint.getFirstQuery().getQueryId();
						secondQuery = constraint.getSecondQuery().getQueryId();											
					}
					else if (secondConstraints<firstConstraints){
						firstQuery = constraint.getSecondQuery().getQueryId();
						secondQuery = constraint.getFirstQuery().getQueryId();											
					}					
					else if (constraint.getOperator()==QueryOperatorType.GREATER||
							constraint.getOperator()==QueryOperatorType.GREATEREQUAL){
						firstQuery = constraint.getSecondQuery().getQueryId();
						secondQuery = constraint.getFirstQuery().getQueryId();
					}
				}

				int firstIndex = orderedSet.indexOf(firstQuery);
				int secondIndex = orderedSet.indexOf(secondQuery);
				if (firstIndex<0&&secondIndex<0){
					orderedSet.add(firstQuery);
					orderedSet.add(secondQuery);
				}
				else if (firstIndex<0){
					orderedSet.add(secondIndex, firstQuery);
				}
				else if (secondIndex<0){
					if ((firstIndex+1)>=orderedSet.size())
						orderedSet.add(secondQuery);
					else
						orderedSet.add((firstIndex+1), secondQuery);
				}
				else if (secondIndex>firstIndex){
					orderedSet.remove(secondIndex);
					orderedSet.add(firstIndex, secondQuery);
				}
									
				//attach constraints to event that came last				

				HashMap<String, List<QueryConstraintType>> constraintMapping = constraints.get(secondQuery);
				if (constraintMapping==null)
					constraintMapping = new HashMap<String, List<QueryConstraintType>>();
				List<QueryConstraintType> constraintList = constraintMapping.get(firstQuery);
				if (constraintList==null)
					constraintList = new ArrayList<QueryConstraintType>();
				constraintList.add(constraint);
				constraintMapping.put(firstQuery, constraintList);
				constraints.put(secondQuery, constraintMapping);					
				
				parseReturnColumns(constraint.getFirstQuery(), queryDef.getQueryTiming());
				parseReturnColumns(constraint.getSecondQuery(), queryDef.getQueryTiming());
			}
		}
	}
	
	private void parseReturnColumns(QueryJoinType eventJoin, String queryTiming){
		String eventId = eventJoin.getQueryId();
		EnumSet<TemporalSubQuery.TemporalQueryReturnColumns> columns = returnColumns.get(eventId);
		
		QueryJoinColumnType joinColumn = eventJoin.getJoinColumn();
		if (joinColumn!=null){
			TemporalSubQuery.TemporalQueryReturnColumns value = null;
			if (joinColumn.equals(QueryJoinColumnType.ENCOUNTER))
				value = TemporalSubQuery.TemporalQueryReturnColumns.ENCOUNTER;
			else if (joinColumn.equals(QueryJoinColumnType.PATIENT))
				value = TemporalSubQuery.TemporalQueryReturnColumns.PATIENT;
			else if (joinColumn.equals(QueryJoinColumnType.INSTANCE))
				value = TemporalSubQuery.TemporalQueryReturnColumns.INSTANCE;
			else if (joinColumn.equals(QueryJoinColumnType.STARTDATE)){
				if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.FIRST_START_DATE;
				else if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.LAST_START_DATE;
				else
					value = TemporalSubQuery.TemporalQueryReturnColumns.START_DATE;
			}
			else if (joinColumn.equals(QueryJoinColumnType.ENDDATE))
				if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.FIRST_END_DATE;
				else if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.LAST_END_DATE;
				else
					value = TemporalSubQuery.TemporalQueryReturnColumns.END_DATE;
			else if (joinColumn.equals(QueryJoinColumnType.ENCOUNTER_STARTDATE)){
				if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.FIRST_ENCOUNTER_START_DATE;
				else if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.LAST_ENCOUNTER_START_DATE;
				else
					value = TemporalSubQuery.TemporalQueryReturnColumns.ENCOUNTER_START_DATE;
			}
			else if (joinColumn.equals(QueryJoinColumnType.ENCOUNTER_ENDDATE))
				if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.FIRST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.FIRST_ENCOUNTER_END_DATE;
				else if (eventJoin.getAggregateOperator().equals(QueryAggregateOperatorType.LAST))
					value = TemporalSubQuery.TemporalQueryReturnColumns.LAST_ENCOUNTER_END_DATE;
				else
					value = TemporalSubQuery.TemporalQueryReturnColumns.ENCOUNTER_START_DATE;
			else	
				value = TemporalSubQuery.TemporalQueryReturnColumns.PATIENT;
			
			if (columns==null)
				columns = EnumSet.of(value);
			else
				columns.add(value);
		}	
		else
			columns = EnumSet.of(TemporalSubQuery.TemporalQueryReturnColumns.PATIENT);

		
		if (queryTiming!=null&&
				(queryTiming.equalsIgnoreCase(QueryTimingHandler.SAME)||
				queryTiming.equalsIgnoreCase(QueryTimingHandler.SAMEVISIT))){
			columns.add(TemporalSubQuery.TemporalQueryReturnColumns.ENCOUNTER);					
		}
		
		returnColumns.put(eventId, columns);
		
	}
}
