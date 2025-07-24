
/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap;

import com.google.gson.annotations.SerializedName;

public class SurveyRecord extends DataCollectionInstrument {
    
    @SerializedName("field_name")
    private String fieldName;

    private String value;

    private String modiferCd = "@";
    
    public String getModiferCd() {
		return modiferCd;
	}

	public void setModiferCd(String modiferCd) {
		this.modiferCd = modiferCd;
	}

	public SurveyRecord() {
        super();
    }

    public SurveyRecord(String record, String fieldName, String eventName, String value) {
        super(record, eventName);
        this.fieldName = fieldName;
        this.value = value;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }

}
