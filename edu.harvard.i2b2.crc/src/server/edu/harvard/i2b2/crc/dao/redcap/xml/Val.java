/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
/*@XmlType(name = "Val", propOrder = {
    "value"
})
*/
public class Val
{
	@XmlAttribute
    private String description = "";
	
	@XmlValue
    private String value = "";

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
    @Override
    public String toString()
    {
        return "ClassPojo [description = "+description+", content = "+value+"]";
    }


}
			
			