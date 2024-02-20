
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import edu.harvard.i2b2.crc.dao.redcap.xml.EnumValues;



@XmlAccessorType(XmlAccessType.FIELD)
public class Item
{
	@XmlElement(name = "filename")
	private String filename;

	@XmlElement(name = "seperator_character")
	private String seperator_character;

	@XmlElement(name = "query")
	private String query;

	@XmlElement(name = "result_output")
	private String result_output;

    public String getFilename ()
    {
        return filename;
    }

    public void setFilename (String filename)
    {
        this.filename = filename;
    }

    public String getSeperator_character ()
    {
        return seperator_character;
    }

    public void setSeperator_character (String seperator_character)
    {
        this.seperator_character = seperator_character;
    }

    public String getQuery ()
    {
        return query;
    }

    public void setQuery (String query)
    {
        this.query = query;
    }

    public String getResult_output ()
    {
        return result_output;
    }

    public void setResult_output (String result_output)
    {
        this.result_output = result_output;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [filename = "+filename+", seperator_character = "+seperator_character+", query = "+query+", result_output = "+result_output+"]";
    }
}
			