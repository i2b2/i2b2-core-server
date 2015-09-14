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
 * Class to generate select, join, where clause for ObservationFact based on
 * pdo's OutputOptionType $Id: ObservationFactFactRelated.java,v 1.4 2007/08/31
 * 14:43:33 rk903 Exp $
 * 
 * @author rkuttan
 */
public class ObservationFactFactRelated extends FactRelated {

	public ObservationFactFactRelated(OutputOptionType outputOptionType) {
		super(outputOptionType);
	}

	public String getDefaultSelectClause() {
		return " obs.encounter_num obs_encounter_num, obs.patient_num obs_patient_num, obs.concept_cd obs_concept_cd,obs.provider_id obs_provider_id, obs.start_date obs_start_date , obs.modifier_cd obs_modifier_cd , obs.instance_num obs_instance_num ";
	}

	/**
	 * Function which selects observation fact fields based on ouput option flag
	 * 
	 * @return
	 */
	public String getSelectClause() {
		String selectClause = "";

		if (isSelected()) {
			selectClause = " obs.encounter_num obs_encounter_num, obs.patient_num obs_patient_num, obs.concept_cd obs_concept_cd, obs.provider_id obs_provider_id, obs.start_date obs_start_date , obs.modifier_cd obs_modifier_cd , obs.instance_num obs_instance_num ";

			// Adding modifier_cd slow down the query , obs.modifier_cd
			// obs_modifier_cd "
			if (isSelectDetail()) {
				selectClause += ", obs.valtype_cd obs_valtype_cd, obs.tval_char obs_tval_char, obs.nval_num obs_nval_num, obs.valueflag_cd obs_valueflag_cd,obs.quantity_num obs_quantity_num, obs.units_cd obs_units_cd, obs.end_date obs_end_date,obs.location_cd obs_location_cd, obs.confidence_num   obs_confidence_num";
			}

			if (isSelectBlob()) {
				selectClause += ", obs.observation_blob obs_observation_blob ";
			}

			if (isSelectStatus()) {
				selectClause += " , obs.update_date obs_update_date, obs.download_date obs_download_date, obs.import_date obs_import_date, obs.sourcesystem_cd obs_sourcesystem_cd, obs.upload_id obs_upload_id ";
			}
		}

		return selectClause;
	}

	/**
	 * Function which selects observation fact fields based on ouput option flag
	 * 
	 * @return
	 */
	public String getSelectClauseWithoutBlob() {
		String selectClause = "";

		if (isSelected()) {
			selectClause = " obs.encounter_num obs_encounter_num, obs.patient_num obs_patient_num, obs.concept_cd obs_concept_cd, obs.provider_id obs_provider_id, obs.start_date obs_start_date , obs.modifier_cd obs_modifier_cd, obs.instance_num obs_instance_num ";

			// Adding modifier_cd slow down the query , obs.modifier_cd
			// obs_modifier_cd "
			if (isSelectDetail()) {
				selectClause += ", obs.valtype_cd obs_valtype_cd, obs.tval_char obs_tval_char, obs.nval_num obs_nval_num, obs.valueflag_cd obs_valueflag_cd,obs.quantity_num obs_quantity_num, obs.units_cd obs_units_cd, obs.end_date obs_end_date,obs.location_cd obs_location_cd, obs.confidence_num   obs_confidence_num";
			}

			// if (isSelectBlob()) {
			// selectClause +=
			// ", to_clob(obs.observation_blob) obs_observation_blob ";
			// }

			if (isSelectStatus()) {
				selectClause += " , obs.update_date obs_update_date, obs.download_date obs_download_date, obs.import_date obs_import_date, obs.sourcesystem_cd obs_sourcesystem_cd, obs.upload_id obs_upload_id ";
			}
		}

		return selectClause;
	}

	public String getLookupTableSelectClause() {
		String lookupTableSelectClause = " ";
		lookupTableSelectClause = " , modifier_lookup.name_char modifier_name ";

		return lookupTableSelectClause;
	}

	public String joinClause() {
		if (isSelected()) {
			return " left join VISIT_DIMENSION   visit   on (obs.encounter_num = visit.encounter_num) ";
		} else {
			return "";
		}
	}
}
