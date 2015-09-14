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

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;

/**
 * Class to generate select, join, where clause
 * for patient dimenstion based on pdo's  OutputOptionType
 * $Id: PatientFactRelated.java,v 1.3 2007/08/31 14:43:33 rk903 Exp $
 * @author rkuttan
 */
public class PatientFactRelated extends FactRelated {
	
	List<ParamType> metaDataParamList ; 
	
    public PatientFactRelated(OutputOptionType outputOptionType) {
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
            selectClause = "  patient.patient_num patient_patient_num";

            if (isSelectDetail()) {
            	selectClause += ", patient.Vital_Status_Cd  patient_Vital_Status_Cd, patient.birth_date patient_birth_date";
                selectClause += "," +  buildCustomSelectClause("patient");
            }

            if (isSelectBlob()) {
                selectClause += ", patient.patient_blob patient_patient_blob ";
            }

            if (isSelectStatus()) {
                selectClause += " , patient.update_date patient_update_date, patient.download_date patient_download_date, patient.import_date patient_import_date, patient.sourcesystem_cd patient_sourcesystem_cd, patient.upload_id patient_upload_id ";
            }
        }

        return selectClause;
    }

    
    public String joinClause() {
        if (isSelected()) {
            return " left join PATIENT_DIMENSION patient on (obs.patient_num = patient.patient_num) ";
        } else {
            return "";
        }
    }

    public int getPatientNumFromResultSet(ResultSet resultSet)
        throws SQLException {
        return resultSet.getInt("obs_patient_num");
    }
}
