/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.xml;

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
public class Item_orig
{
	
	@XmlValue
    private String filename = "";

	@XmlValue
    private String query = "";

	@XmlValue
    private String resultOutput = "";

	@XmlValue
    private String separatorCharacter = "";

	
    public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	public String getResultOutput() {
		return resultOutput;
	}


	public void setResultOutput(String resultOutput) {
		this.resultOutput = resultOutput;
	}


	public String getSeparatorCharacter() {
		return separatorCharacter;
	}


	public void setSeparatorCharacter(String separatorCharacter) {
		this.separatorCharacter = separatorCharacter;
	}


	@Override
    public String toString()
    {
        return "ClassPojo [query = "+query+", content = "+filename+"]";
    }


}
			
			