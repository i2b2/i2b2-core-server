/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.output;

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;

/**
 * Class to generate select, join, where clause for concept dimenstion based on
 * pdo's OutputOptionType $Id: ConceptFactRelated.java,v 1.3 2007/08/31 14:43:33
 * rk903 Exp $
 * 
 * @author rkuttan
 */
public class EidFactRelated extends FactRelated {
	/**
	 * Constructor accepts OutputOptionType
	 * 
	 * @param outputOptionType
	 */
	public EidFactRelated(OutputOptionType outputOptionType) {
		super(outputOptionType);
	}

	/**
	 * Function which selects fields based on ouput option flag
	 * 
	 * @return select sql clause
	 */
	public String getSelectClause() {
		String selectClause = "";

		if (isSelected()) {
			selectClause = " em.encounter_ide em_encounter_ide, em.encounter_ide_source em_encounter_ide_source, em.patient_ide em_patient_ide, em.patient_ide_source em_patient_ide_source, em.encounter_num em_encounter_num";

			if (isSelectDetail()) {
				selectClause += " ,em.encounter_ide_status em_encounter_ide_status";
			}

			if (isSelectStatus()) {
				selectClause += " , em.update_date em_update_date, em.download_date em_download_date, em.import_date em_import_date, em.sourcesystem_cd em_sourcesystem_cd , em.upload_id em_upload_id ";
			}
		}

		return selectClause;
	}

	/**
	 * Function to generate join clause with concept dimension table
	 * 
	 * @return
	 */
	public String joinClause() {
		if (isSelected()) {
			return " left join ENCOUNTER_MAPPING enc_map on (obs.encounter_num = enc_map.encounter_num) ";
		} else {
			return "";
		}
	}

}
