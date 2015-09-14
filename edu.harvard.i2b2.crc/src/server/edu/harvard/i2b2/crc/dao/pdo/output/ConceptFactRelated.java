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

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;

/**
 * Class to generate select, join, where clause for concept dimenstion based on
 * pdo's OutputOptionType $Id: ConceptFactRelated.java,v 1.3 2007/08/31 14:43:33
 * rk903 Exp $
 * 
 * @author rkuttan
 */
public class ConceptFactRelated extends FactRelated {
	/**
	 * Constructor accepts OutputOptionType
	 * 
	 * @param outputOptionType
	 */
	public ConceptFactRelated(OutputOptionType outputOptionType) {
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
			selectClause = "  concept.concept_cd concept_concept_cd";

			if (isSelectDetail()) {
				selectClause += " ,concept.concept_path concept_concept_path, concept.name_char concept_name_char ";
			}

			if (isSelectBlob()) {
				selectClause += ", concept.concept_blob concept_concept_blob ";
			}

			if (isSelectStatus()) {
				selectClause += " , concept.update_date concept_update_date, concept.download_date concept_download_date, concept.import_date concept_import_date, concept.sourcesystem_cd concept_sourcesystem_cd, concept.upload_id concept_upload_id  ";
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
			return " left join CONCEPT_DIMENSION concept on (obs.concept_cd = concept.concept_cd) ";
		} else {
			return "";
		}
	}

	public String getConceptCdFromResultSet(ResultSet resultSet)
			throws SQLException {
		return resultSet.getString("obs_concept_cd");
	}
}
