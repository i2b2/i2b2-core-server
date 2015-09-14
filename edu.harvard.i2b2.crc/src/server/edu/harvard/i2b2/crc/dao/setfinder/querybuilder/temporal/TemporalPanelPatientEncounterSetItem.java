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

public class TemporalPanelPatientEncounterSetItem extends TemporalPanelItem {
	
	public TemporalPanelPatientEncounterSetItem(TemporalPanel parent, ItemType item)
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
		
			String[] encounterSetId = itemKey.split(":");
			if (encounterSetId[1] != null) {
				conceptType = new ConceptType();
				conceptType.setColumnname(" result_instance_id ");
				conceptType.setOperator(" = ");
				conceptType.setFacttablecolumn(" encounter_num ");
				conceptType.setTablename("qt_patient_enc_collection  ");
				conceptType.setDimcode(encounterSetId[1]);
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
		} else if (this.returnEncounterNum()) {
			return "visit_dimension";
		} else {
			return "qt_patient_set_collection";
		}
	}

}
