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
  <ZipFilename>/tmp/finalfile.zip</ZipFilename>
  <ZipEncryptMethod>AES</ZipEncryptMethod>
  <ZipPassword>test123</ZipPassword>
  <DataManagerEmail>admin@site.org</DataManagerEmail>
  <ReuqestLetter>This user requested a query with ID of {{{ID} and name of {{{NAME}} ....
	</ReuqestLetter>
  <LetterFilename>/tmp/readme.txt</LetterFilename>
  <Letter>sdfdsfsd</Letter>
  <Item>
                <Filename>/tmp/tst.csv</Filename>
                <Query>select * from obser aiont)fact a, {{{DX}}} b where a.patient_num = b.patient_num</Query>
               <SeparatorCharacter>\t</SeparatorCharacter>
  </Item>
  <Item>
                <Filename>/tmp/second.csv</Filename>
                <Query>select * from cat</Query>
               <SeparatorCharacter>\t</SeparatorCharacter>
  </Item>
</ValueExporter>
 */
                 

@XmlRootElement (name = "ValueExporter")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueExporter
{

    @XmlElement(name = "Item")
    private Item[] item;

    @XmlElement(name = "CreationDateTime")
    private String CreationDateTime;

    @XmlElement(name = "LetterFilename")
    private String LetterFilename;

    @XmlElement(name = "Letter")
    private String Letter;

    @XmlElement(name = "ReuqestLetter")
    private String ReuqestLetter;

    @XmlElement(name = "DataManagerEmail")
    private String DataManagerEmail;

    @XmlElement(name = "ZipFilename")
    private String ZipFilename;

    @XmlElement(name = "Version")
    private String Version;

    @XmlElement(name = "ZipEncryptMethod")
    private String ZipEncryptMethod;

    @XmlElement(name = "ZipPassword")
    private String ZipPassword;
    
    public String getLetterFilename() {
		return LetterFilename;
	}

	public void setLetterFilename(String letter_filename) {
		this.LetterFilename = letter_filename;
	}

	public String getLetter() {
		return Letter;
	}

	public void setLetter(String letter) {
		this.Letter = letter;
	}
    
    public String getReuqestLetter() {
		return ReuqestLetter;
	}

	public void setReuqestLetter(String reuqestLetter) {
		ReuqestLetter = reuqestLetter;
	}

	public String getDataManagerEmail() {
		return DataManagerEmail;
	}

	public void setDataManagerEmail(String dataManagerEmail) {
		DataManagerEmail = dataManagerEmail;
	}

	public String getZipPassword() {
		return ZipPassword;
	}

	public void setZipPassword(String zip_password) {
		this.ZipPassword = zip_password;
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

    public String getZipFilename ()
    {
        return ZipFilename;
    }

    public void setZipFilename (String zip_filename)
    {
        this.ZipFilename = zip_filename;
    }

    public String getVersion ()
    {
        return Version;
    }

    public void setVersion (String Version)
    {
        this.Version = Version;
    }

    public String getZipEncryptMethod ()
    {
        return ZipEncryptMethod;
    }

    public void setZipEncryptMethod (String zip_encrypt_method)
    {
        this.ZipEncryptMethod = zip_encrypt_method;
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
        return "ClassPojo [item = "+item+", CreationDateTime = "+CreationDateTime+", zip_filename = "+ZipFilename+", Version = "+Version+", zip_encrypt_method = "+ZipEncryptMethod+", content = "+content+"]";
    }
}
			
			

