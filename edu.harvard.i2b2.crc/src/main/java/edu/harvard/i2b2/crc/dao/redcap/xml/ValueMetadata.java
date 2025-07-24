/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement (name = "ValueMetadata")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueMetadata
{
    private CommentsDeterminingExclusion CommentsDeterminingExclusion;
   
    @XmlElement(name = "TestName")
    private String TestName;

    @XmlElement(name = "LowofHighValue")
    private String LowofHighValue;

    @XmlElement(name = "Oktousevalues")
    private String Oktousevalues = "";

    @XmlElement(name = "UnitValues")
    private UnitValues UnitValues; 

    @XmlElement(name = "HighofToxicValue")
    private String HighofToxicValue;

    @XmlElement(name = "HighofHighValue")
    private String HighofHighValue;

    @XmlElement(name = "EnumValues")
    private EnumValues EnumValues;

    @XmlElement(name = "LowofToxicValue")
    private String LowofToxicValue;

    @XmlElement(name = "TestID")
    private String TestID;

    @XmlElement(name = "MaxStringLength")
    private String MaxStringLength ="";

    @XmlElement(name = "HighofLowValue")
    private String HighofLowValue;

    @XmlElement(name = "CreationDateTime")
    private String CreationDateTime;

    @XmlElement(name = "Version")
    private String version;

    @XmlElement(name = "Analysis")
    private Analysis Analysis;
    
    @XmlElement(name = "LowofLowValue")
    private String LowofLowValue;

    @XmlElement(name = "Loinc")
    private String Loinc;

    @XmlElement(name = "DataType")
    private String DataType;

    @XmlElement(name = "CodeType")
    private String CodeType;

    @XmlElement(name = "Flagstouse")
    private String Flagstouse ="";
    
    public CommentsDeterminingExclusion getCommentsDeterminingExclusion ()
    {
        return CommentsDeterminingExclusion;
    }

    public void setCommentsDeterminingExclusion (CommentsDeterminingExclusion CommentsDeterminingExclusion)
    {
        this.CommentsDeterminingExclusion = CommentsDeterminingExclusion;
    }

    public String getTestName ()
    {
        return TestName;
    }

    public void setTestName (String TestName)
    {
        this.TestName = TestName;
    }

    public String getLowofHighValue ()
    {
        return LowofHighValue;
    }

    public void setLowofHighValue (String LowofHighValue)
    {
        this.LowofHighValue = LowofHighValue;
    }

    public String getOktousevalues ()
    {
        return Oktousevalues;
    }

    public void setOktousevalues (String Oktousevalues)
    {
        this.Oktousevalues = Oktousevalues;
    }

    public UnitValues getUnitValues ()
    {
        return UnitValues;
    }

    public void setUnitValues (UnitValues UnitValues)
    {
        this.UnitValues = UnitValues;
    }

    public String getHighofToxicValue ()
    {
        return HighofToxicValue;
    }

    public void setHighofToxicValue (String HighofToxicValue)
    {
        this.HighofToxicValue = HighofToxicValue;
    }

    public String getHighofHighValue ()
    {
        return HighofHighValue;
    }

    public void setHighofHighValue (String HighofHighValue)
    {
        this.HighofHighValue = HighofHighValue;
    }

    public EnumValues getEnumValues ()
    {
        return EnumValues;
    }

    public void setEnumValues (EnumValues EnumValues)
    {
        this.EnumValues = EnumValues;
    }

    public String getLowofToxicValue ()
    {
        return LowofToxicValue;
    }

    public void setLowofToxicValue (String LowofToxicValue)
    {
        this.LowofToxicValue = LowofToxicValue;
    }

    public String getTestID ()
    {
        return TestID;
    }

    public void setTestID (String TestID)
    {
        this.TestID = TestID;
    }

    public String getMaxStringLength ()
    {
        return MaxStringLength;
    }

    public void setMaxStringLength (String MaxStringLength)
    {
        this.MaxStringLength = MaxStringLength;
    }

    public String getHighofLowValue ()
    {
        return HighofLowValue;
    }

    public void setHighofLowValue (String HighofLowValue)
    {
        this.HighofLowValue = HighofLowValue;
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

    public Analysis getAnalysis ()
    {
        return Analysis;
    }

    public void setAnalysis (Analysis Analysis)
    {
        this.Analysis = Analysis;
    }

    public String getLowofLowValue ()
    {
        return LowofLowValue;
    }

    public void setLowofLowValue (String LowofLowValue)
    {
        this.LowofLowValue = LowofLowValue;
    }

    public String getLoinc ()
    {
        return Loinc;
    }

    public void setLoinc (String Loinc)
    {
        this.Loinc = Loinc;
    }

    public String getDataType ()
    {
        return DataType;
    }

    public void setDataType (String DataType)
    {
        this.DataType = DataType;
    }

    public String getCodeType ()
    {
        return CodeType;
    }

    public void setCodeType (String CodeType)
    {
        this.CodeType = CodeType;
    }

    public String getFlagstouse ()
    {
        return Flagstouse;
    }

    public void setFlagstouse (String Flagstouse)
    {
        this.Flagstouse = Flagstouse;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [CommentsDeterminingExclusion = "+CommentsDeterminingExclusion+", TestName = "+TestName+", LowofHighValue = "+LowofHighValue+", Oktousevalues = "+Oktousevalues+", UnitValues = "+UnitValues+", HighofToxicValue = "+HighofToxicValue+", HighofHighValue = "+HighofHighValue+", EnumValues = "+EnumValues+", LowofToxicValue = "+LowofToxicValue+", TestID = "+TestID+", MaxStringLength = "+MaxStringLength+", HighofLowValue = "+HighofLowValue+", CreationDateTime = "+CreationDateTime+", Version = "+version+", Analysis = "+Analysis+", LowofLowValue = "+LowofLowValue+", Loinc = "+Loinc+", DataType = "+DataType+", CodeType = "+CodeType+", Flagstouse = "+Flagstouse+"]";
    }
}
			
			