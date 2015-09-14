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
public class PidFactRelated extends FactRelated {
	/**
	 * Constructor accepts OutputOptionType
	 * 
	 * @param outputOptionType
	 */
	public PidFactRelated(OutputOptionType outputOptionType) {
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
			selectClause = "  pm.patient_ide pm_patient_ide, pm.patient_ide_source pm_patient_ide_source, pm.patient_num pm_patient_num";

			if (isSelectDetail()) {
				selectClause += " ,pm.patient_ide_status pm_patient_ide_status";
			}

			if (isSelectStatus()) {
				selectClause += " , pm.update_date pm_update_date, pm.download_date pm_download_date, pm.import_date pm_import_date, pm.sourcesystem_cd pm_sourcesystem_cd, pm.upload_id pm_upload_id  ";
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
			return " left join PATIENT_MAPPING pat_map on (obs.patient_num = pat_map.patient_num) ";
		} else {
			return "";
		}
	}

}
