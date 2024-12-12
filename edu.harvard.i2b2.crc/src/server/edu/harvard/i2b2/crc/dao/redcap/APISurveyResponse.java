/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap;

import java.util.ArrayList;

import org.apache.hc.core5.http.HttpStatus;

//import org.apache.http.HttpStatus;

public class APISurveyResponse {
    private HttpStatus status;
    private ArrayList<SurveyRecord> records = new ArrayList<>();
    private ArrayList<DataCollectionInstrumentMetadata> metadatas =  new ArrayList<>();

    public APISurveyResponse() {
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ArrayList<SurveyRecord> getRecords() {
        return records;
    }

	public ArrayList<DataCollectionInstrumentMetadata> getMetadata() {
		return metadatas;
	}
	public DataCollectionInstrumentMetadata getMetadata(String metadata) {
		for (DataCollectionInstrumentMetadata surveyMetadata: metadatas ) {
			if (surveyMetadata.getFieldName().equals(metadata))
				return surveyMetadata;
		}
		return null;
	}

	public void process() {
		
		for (int i=0; i < records.size(); i++)
		{
			for ( int j=i+1; j < records.size(); j++) {
				if (records.get(i).getFieldName().equals(records.get(j).getFieldName())) {
					records.get(i).setModiferCd(records.get(i).getValue());
					records.get(j).setModiferCd(records.get(j).getValue());
				}
			}
		}
			
		
	}

}
