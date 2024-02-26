
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
	@XmlElement(name = "Filename")
	private String Filename;

	@XmlElement(name = "SeperatorCharacter")
	private String SeperatorCharacter;

	@XmlElement(name = "Query")
	private String Query;

	@XmlElement(name = "ResultOutput")
	private String ResultOutput;

    public String getFilename ()
    {
        return Filename;
    }

    public void setFilename (String filename)
    {
        this.Filename = filename;
    }

    public String getSeperatorCharacter ()
    {
        return SeperatorCharacter;
    }

    public void setSeperatorCharacter (String seperator_character)
    {
        this.SeperatorCharacter = seperator_character;
    }

    public String getQuery ()
    {
        return Query;
    }

    public void setQuery (String query)
    {
        this.Query = query;
    }

    public String getResultOutput ()
    {
        return ResultOutput;
    }

    public void setResultOutput (String result_output)
    {
        this.ResultOutput = result_output;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [filename = "+Filename+", seperator_character = "+SeperatorCharacter+", query = "+Query+", result_output = "+ResultOutput+"]";
    }
}
			