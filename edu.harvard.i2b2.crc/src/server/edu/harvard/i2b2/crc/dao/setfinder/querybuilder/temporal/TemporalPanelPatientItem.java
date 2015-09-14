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

public class TemporalPanelPatientItem extends TemporalPanelItem {

	public TemporalPanelPatientItem(TemporalPanel parent, ItemType item)
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
			String itemKey = baseItem.getItemKey();
			
			String[] patientId = itemKey.split(":");
			if (patientId.length > 2 &&
					patientId[1] != null && 
					patientId[2] != null) {
				return "select "
						+ "patient_num from " + noLockSqlServer
						+ parent.getDatabaseSchema() + "patient_mapping "
						+ "where patient_ide_source = '" + patientId[1] + "' "
						+ "and patient_ide = '" + patientId[2] + "' "
						+ "";
			}
			else
				return null;
		}
	}


	@Override
	protected ConceptType getConceptType() throws ConceptNotFoundException,
			OntologyException {
		if (conceptType==null){
			String itemKey = baseItem.getItemKey();
	
			String[] patientId = itemKey.split(":");
			if (patientId.length > 2 &&
					patientId[1] != null && 
					//patientId[1].trim().toUpperCase().equals("HIVE") && 
					patientId[2] != null) {
				String patientNum = patientId[2];
				conceptType = new ConceptType();
				conceptType.setColumnname(" patient_num ");
				conceptType.setOperator(" = ");
				conceptType.setFacttablecolumn(" patient_num ");
				conceptType.setTablename("patient_mapping ");
				conceptType.setDimcode(patientNum);
			}
		}

		return conceptType;
	}


	@Override
	protected String buildDimensionJoinSql(String tableAlias) {
		String dimensionSql = "";

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";
		
		String itemKey = baseItem.getItemKey();
		
		String[] patientId = itemKey.split(":");
		if (patientId.length > 2 &&
				patientId[1] != null && 
				patientId[2] != null) {
			
			dimensionSql = tableAlias + this.factTableColumn + " IN (select "
					+ "patient_num from " + noLockSqlServer
					+ parent.getDatabaseSchema() + "patient_mapping "
					+ "where patient_ide_source = '" + patientId[1] + "' "
					+ "and patient_ide = '" + patientId[2] + "' "
					+ ")";
		}

		return dimensionSql;
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
			return "patient_dimension";
		}
	}
}
