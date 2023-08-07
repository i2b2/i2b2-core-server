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

public class DataCollectionInstrument  {
	@SerializedName("record")
    protected String record;
	
	@SerializedName("event_id")
    protected String eventId;
	
    @SerializedName("subject_id")
    protected String subjectId;  

    public DataCollectionInstrument() {
    }

    public DataCollectionInstrument(String record, String eventName) {
        this.record = record;
        this.subjectId = record;
        this.eventId = eventName;
    }

    public String getSubjectIsd() {
        return subjectId;
    }

    public void setSubjsectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getRecordId() {
        return record;
    }

    public void setEventId(String eventName) {
        this.eventId = eventName;
    }

    public String getEventId() {
        return eventId;
    }

}
