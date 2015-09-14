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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;

public class TemporalPanelEncounterItem extends TemporalPanelItem {
	
	public TemporalPanelEncounterItem(TemporalPanel parent, ItemType item)
				throws I2B2Exception {
			super(parent, item);
		}
	
	
	@Override
	protected String buildSql() throws I2B2DAOException {
		if (this.returnInstanceNum()||
				this.hasItemDateConstraint()||
				this.hasModiferConstraint()||
				this.hasPanelDateConstraint()||
				this.hasPanelOccurrenceConstraint()||
				this.hasValueConstraint()
				){
			return super.buildSql();
		}
		else{
			String selectClause = "select patient_num ";
			if (this.returnEncounterNum())
				selectClause = "select encounter_num, patient_num ";
			return selectClause + " from " + noLockSqlServer
					+ parent.getDatabaseSchema() + this.tableName
					+ "  " + " where " + this.columnName + " "
					+ this.operator + " " + this.dimCode
					+ "";
		}
	}

	
	@Override
	protected ConceptType getConceptType() throws ConceptNotFoundException,
			OntologyException {
		if (conceptType==null){
			String itemKey = baseItem.getItemKey();
		
			String[] encounterId = itemKey.split(":");
			if (encounterId[1] != null) {
				String encounterNum = encounterId[1];
				conceptType = new ConceptType();
				conceptType.setColumnname(" encounter_num ");
				conceptType.setOperator(" = ");
				conceptType.setFacttablecolumn(" encounter_num ");
				conceptType.setTablename("visit_dimension  ");
				conceptType.setDimcode(encounterNum);
			}
		}
		
		return conceptType;
	}

	@Override
	protected String getJoinTable() {
		if (this.returnInstanceNum()||
				hasItemDateConstraint()||
				hasPanelDateConstraint()||
				hasValueConstraint()||
				hasPanelOccurrenceConstraint()) {
			return "observation_fact";
		} else {
			return "visit_dimension";
		} 
	}
	
	
	@Override
	protected String buildDimensionJoinSql(String tableAlias) {
		String dimensionSql = "";

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";
		
		String itemKey = baseItem.getItemKey();
		
		String[] encounterId = itemKey.split(":");
		if (encounterId.length > 2 &&
				encounterId[1] != null && 
				encounterId[2] != null) {
			
			dimensionSql = tableAlias + this.factTableColumn + " IN (select "
					+ "encounter_num from " + noLockSqlServer
					+ parent.getDatabaseSchema() + "encounter_mapping "
					+ "where encounter_ide_source = '" + encounterId[1] + "' "
					+ "and encounter_ide = '" + encounterId[2] + "' "
					+ ")";
		}

		return dimensionSql;
	}

}
