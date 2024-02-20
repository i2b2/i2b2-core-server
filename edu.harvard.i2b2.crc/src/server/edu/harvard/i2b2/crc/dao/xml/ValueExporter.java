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

/*
 * <?xml version="1.0"?>
<ValueExporter>
  <Version>3.02</Version>
  <CreationDateTime>08/09/2024 12:00:00</CreationDateTime>
  <zipFilename>/tmp/finalfile.zip</zipFilename>
  <zipEncryptMethod>AES</zipEncryptMethod>
  <zipPassword>test123</zipPassword>
  <item>
                <filename>/tmp/tst.csv</filename>
                <query>select * from obser aiont)fact a, {{{DX}}} b where a.patient_num = b.patient_num</query>
                <result_output>DEMOGRAPHIOCS_CSV</result_output>
               <separator_character>\t</separator_character>
  </item>
  <item>
                <filename>/tmp/second.csv</filename>
                <query>select * from cat</query>
                <result_output>OTHER_DATA_CSV</result_output>
               <separator_character>\t</separator_character>
  </item>
</ValueExporter>
 */
                 

@XmlRootElement (name = "ValueExporter")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueExporter
{

    @XmlElement(name = "item")
    private Item[] item;

    @XmlElement(name = "CreationDateTime")
    private String CreationDateTime;

    @XmlElement(name = "letter_filename")
    private String letter_filename;

    @XmlElement(name = "letter")
    private String letter;

    public String getLetter_filename() {
		return letter_filename;
	}

	public void setLetter_filename(String letter_filename) {
		this.letter_filename = letter_filename;
	}

	public String getLetter() {
		return letter;
	}

	public void setLetter(String letter) {
		this.letter = letter;
	}

	@XmlElement(name = "zip_filename")
    private String zip_filename;

    @XmlElement(name = "Version")
    private String Version;

    @XmlElement(name = "zip_encrypt_method")
    private String zip_encrypt_method;


    @XmlElement(name = "zip_password")
    private String zip_password;
    
    public String getZip_password() {
		return zip_password;
	}

	public void setZip_password(String zip_password) {
		this.zip_password = zip_password;
	}

	private String[] content;

    public Item[] getItem ()
    {
        return item;
    }

    public void setItem (Item[] item)
    {
        this.item = item;
    }

    public String getCreationDateTime ()
    {
        return CreationDateTime;
    }

    public void setCreationDateTime (String CreationDateTime)
    {
        this.CreationDateTime = CreationDateTime;
    }

    public String getZip_filename ()
    {
        return zip_filename;
    }

    public void setZip_filename (String zip_filename)
    {
        this.zip_filename = zip_filename;
    }

    public String getVersion ()
    {
        return Version;
    }

    public void setVersion (String Version)
    {
        this.Version = Version;
    }

    public String getZip_encrypt_method ()
    {
        return zip_encrypt_method;
    }

    public void setZip_encrypt_method (String zip_encrypt_method)
    {
        this.zip_encrypt_method = zip_encrypt_method;
    }

    public String[] getContent ()
    {
        return content;
    }

    public void setContent (String[] content)
    {
        this.content = content;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [item = "+item+", CreationDateTime = "+CreationDateTime+", zip_filename = "+zip_filename+", Version = "+Version+", zip_encrypt_method = "+zip_encrypt_method+", content = "+content+"]";
    }
}
			
			

