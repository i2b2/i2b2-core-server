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

public class TemporalPanelPatientSetItem extends TemporalPanelItem {

	public TemporalPanelPatientSetItem(TemporalPanel parent, ItemType item)
			throws I2B2Exception {
		super(parent, item);
	}


	@Override
	protected String buildSql() throws I2B2DAOException {
		if (this.returnEncounterNum()||
				this.returnInstanceNum()||
				this.hasItemDateConstraint()||
				this.hasModiferConstraint()||
				this.hasPanelDateConstraint()||
				this.hasPanelOccurrenceConstraint()||
				this.hasValueConstraint()
				){
			return super.buildSql();
		}
		else{
			return "select "
					+ this.factTableColumn + " from " + noLockSqlServer
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
	
			String[] patientSetId = itemKey.split(":");
			if (patientSetId[1] != null) {
				conceptType = new ConceptType();
				conceptType.setColumnname(" result_instance_id ");
				conceptType.setOperator(" = ");
				conceptType.setFacttablecolumn(" patient_num ");
				conceptType.setTablename("qt_patient_set_collection ");
				conceptType.setDimcode(patientSetId[1]);
			}
		}

		return conceptType;
	}


	@Override
	protected String getJoinTable() {
		if (returnInstanceNum()||
				hasItemDateConstraint()||
				hasPanelDateConstraint()||
				hasValueConstraint()||
				hasPanelOccurrenceConstraint()) {
			return "observation_fact";
		} else if (returnEncounterNum()) {
			return "visit_dimension";
		} else {
			return "qt_patient_set_collection";
		}
	}
}
