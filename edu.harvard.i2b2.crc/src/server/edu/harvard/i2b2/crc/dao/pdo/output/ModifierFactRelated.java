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
 * pdo's OutputOptionType $Id: ModifierFactRelated.java,v 1.3 2007/08/31 14:43:33
 * rk903 Exp $
 * 
 * @author rkuttan
 */
public class ModifierFactRelated extends FactRelated {
	/**
	 * Constructor accepts OutputOptionType
	 * 
	 * @param outputOptionType
	 */
	public ModifierFactRelated(OutputOptionType outputOptionType) {
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
			selectClause = "  modifier.modifier_cd modifier_modifier_cd";

			if (isSelectDetail()) {
				selectClause += " ,modifier.modifier_path modifier_modifier_path, modifier.name_char modifier_name_char ";
			}

			if (isSelectBlob()) {
				selectClause += ", modifier.modifier_blob modifier_modifier_blob ";
			}

			if (isSelectStatus()) {
				selectClause += " , modifier.update_date modifier_update_date, modifier.download_date modifier_download_date, modifier.import_date modifier_import_date, modifier.sourcesystem_cd modifier_sourcesystem_cd, modifier.upload_id modifier_upload_id  ";
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
			return " left join MODIFIER_DIMENSION modifier on (obs.modifier_cd = modifier.modifier_cd) ";
		} else {
			return "";
		}
	}

	public String getModifierCdFromResultSet(ResultSet resultSet)
			throws SQLException {
		return resultSet.getString("obs_modifier_cd");
	}
}
