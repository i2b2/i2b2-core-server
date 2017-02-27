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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;

public class TemporalPanelConceptItem extends TemporalPanelItem {

	public TemporalPanelConceptItem(TemporalPanel parent, ItemType item)
			throws I2B2Exception {
		super(parent, item);
	}

	public TemporalPanelConceptItem(TemporalPanel parent, ItemType item, ConceptType concept)
			throws I2B2Exception {
		super(parent, item, concept);
	}
	
	
	@Override
	protected String buildSqlHintClause() {
		if (parent.getServerType().equalsIgnoreCase("ORACLE")&&
				parent.getQueryOptions().useSqlHints()){
			String joinTable = getJoinTable();
			if (joinTable.equalsIgnoreCase("observation_fact")){
				if (tableName.equalsIgnoreCase("provider_dimension")) {
					return " /*+ index(observation_fact observation_fact_pk) */ ";
				} else {
					return " /*+ index(observation_fact fact_cnpt_pat_enct_idx) */ ";
				}
			}
			return "";
		}
		else
			return " ";
	}

	@Override
	protected String getJoinTable() {
		//OMOP WAS..
		//String joinTableName = "observation_fact";

		String joinFact = this.factTable;
		if ((joinFact==null)||(joinFact.trim().isEmpty())){
			joinFact = "observation_fact";
		}
		String joinTableName = joinFact;		
		if (tableName.equalsIgnoreCase("patient_dimension")) {
			joinTableName = "patient_dimension";
			if (parent.hasPanelOccurrenceConstraint()) {
				//OMOP WAS..
				//joinTableName = "observation_fact";
				joinTableName = joinFact;
			} 
			else if (returnInstanceNum()) {
				//OMOP WAS..
				//joinTableName = "observation_fact";
				joinTableName = joinFact;
			}
			else if (returnEncounterNum()) {
				joinTableName = "visit_dimension";
			} 
			else if (parent.hasPanelDateConstraint()){
				joinTableName = "visit_dimension";				
			}
		} else if (tableName.equalsIgnoreCase(
				"visit_dimension")) {
			joinTableName = "visit_dimension";
			if (returnInstanceNum()
					||parent.hasPanelOccurrenceConstraint()) {
				//OMOP WAS..
				//joinTableName = "observation_fact";
				joinTableName = joinFact;
			}
		}

		return joinTableName;
	}

	
	
}
