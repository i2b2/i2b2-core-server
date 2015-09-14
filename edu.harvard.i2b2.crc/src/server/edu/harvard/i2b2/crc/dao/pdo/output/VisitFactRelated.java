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
import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;

/**
 * Class to generate select, join, where clause for Visit dimenstion based on
 * pdo's OutputOptionType $Id: VisitFactRelated.java,v 1.4 2007/08/31 14:43:33
 * rk903 Exp $
 * 
 * @author rkuttan
 */
public class VisitFactRelated extends FactRelated {
	List<ParamType> metaDataParamList ; 
	
	public VisitFactRelated(OutputOptionType outputOptionType) {
		super(outputOptionType);
	}
	
	public void setMetaDataParamList(List<ParamType> metaDataParamList) { 
		this.metaDataParamList = metaDataParamList; 
	}
    
	 private String buildCustomSelectClause(String prefix) {
	    	String detailSelectClause = " ";
	    	for (Iterator<ParamType> iterator = this.metaDataParamList.iterator();iterator.hasNext();) { 
	    		ParamType paramType = iterator.next();
	    		detailSelectClause += prefix + "." + paramType.getColumn() + "  " + prefix + "_" + paramType.getColumn();
	    		if (iterator.hasNext()) { 
	    			detailSelectClause += " , ";
	    		}
	    	}
	    	return detailSelectClause;
	    }

	public String getSelectClause() {
		String selectClause = "";

		if (isSelected()) {
			selectClause = " visit.encounter_num visit_encounter_num, visit.patient_num visit_patient_num ";

			if (isSelectDetail()) {
				selectClause += ",  visit.start_date visit_start_date,visit.end_date visit_end_date ";
				selectClause += "," +  buildCustomSelectClause("visit");
			}

			if (isSelectBlob()) {
				selectClause += ", visit.visit_blob visit_visit_blob ";
			}

			if (isSelectStatus()) {
				selectClause += " , visit.update_date visit_update_date, visit.download_date visit_download_date, visit.import_date visit_import_date, visit.sourcesystem_cd visit_sourcesystem_cd, visit.upload_id visit_upload_id ";
			}
		}

		return selectClause;
	}

	public String joinClause() {
		if (isSelected()) {
			return " left join VISIT_DIMENSION   visit   on (obs.encounter_num = visit.encounter_num and  obs.patient_num = visit.patient_num) ";
		} else {
			return "";
		}
	}

	public String[] getAliasFieldName() {
		// get select clause and fetch corresponding data to build visit
		// dimension
		String selectClause = getSelectClause();
		String[] fields = selectClause.split(",");
		int i = 0;
		String[] aliasFieldName = new String[fields.length];

		while (i <= fields.length) {
			aliasFieldName[i] = fields[i].substring(fields[i].indexOf(' '),
					fields[i].length());
			i++;
		}

		return aliasFieldName;
	}

	public int getEncounterNumFromResultSet(ResultSet resultSet)
			throws SQLException {
		return resultSet.getInt("obs_encounter_num");
	}
}
