/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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
public class ValueExport
{
    public String getZipFilename() {
		return zipFilename;
	}

	public void setZipFilename(String zipFilename) {
		this.zipFilename = zipFilename;
	}

	public String getZipEncryptMethod() {
		return zipEncryptMethod;
	}

	public void setZipEncryptMethod(String zipEncryptMethod) {
		this.zipEncryptMethod = zipEncryptMethod;
	}

	public String getZipPassword() {
		return zipPassword;
	}

	public void setZipPassword(String zipPassword) {
		this.zipPassword = zipPassword;
	}

	public Items getItems() {
		return Items;
	}

	public void setItems(Items items) {
		Items = items;
	}


	private CommentsDeterminingExclusion CommentsDeterminingExclusion;
   
    @XmlElement(name = "zipFilename")
    private String zipFilename;

    @XmlElement(name = "zipEncryptMethod")
    private String zipEncryptMethod;

    @XmlElement(name = "zipPassword")
    private String zipPassword;

    @XmlElement(name = "CreationDateTime")
    private String CreationDateTime;

    @XmlElement(name = "Version")
    private String version;

  //  @XmlElement(name = "Analysis")
  //  private Analysis Analysis;
    
    
    public CommentsDeterminingExclusion getCommentsDeterminingExclusion ()
    {
        return CommentsDeterminingExclusion;
    }

    public void setCommentsDeterminingExclusion (CommentsDeterminingExclusion CommentsDeterminingExclusion)
    {
        this.CommentsDeterminingExclusion = CommentsDeterminingExclusion;
    }


    public String getCreationDateTime ()
    {
        return CreationDateTime;
    }

    public void setCreationDateTime (String CreationDateTime)
    {
        this.CreationDateTime = CreationDateTime;
    }

    public String getVersion ()
    {
        return version;
    }

    public void setVersion (String Version)
    {
        this.version = Version;
    }
    

    @XmlElement(name = "Items")
    private Items Items;


    @Override
    public String toString()
    {
        return "";
        //ClassPojo [CommentsDeterminingExclusion = "+CommentsDeterminingExclusion+", TestName = "+TestName+", LowofHighValue = "+LowofHighValue+", Oktousevalues = "+Oktousevalues+", UnitValues = "+UnitValues+", HighofToxicValue = "+HighofToxicValue+", HighofHighValue = "+HighofHighValue+", EnumValues = "+EnumValues+", LowofToxicValue = "+LowofToxicValue+", TestID = "+TestID+", MaxStringLength = "+MaxStringLength+", HighofLowValue = "+HighofLowValue+", CreationDateTime = "+CreationDateTime+", Version = "+version+", Analysis = "+Analysis+", LowofLowValue = "+LowofLowValue+", Loinc = "+Loinc+", DataType = "+DataType+", CodeType = "+CodeType+", Flagstouse = "+Flagstouse+"]";
    }
}
			
			