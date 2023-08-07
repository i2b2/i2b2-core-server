/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap.xml;


public class Analysis
{
    private String New = "";

    private String Enums = "";

    private String Counts = "";

    public String getNew ()
    {
        return New;
    }

    public void setNew (String New)
    {
        this.New = New;
    }

    public String getEnums ()
    {
        return Enums;
    }

    public void setEnums (String Enums)
    {
        this.Enums = Enums;
    }

    public String getCounts ()
    {
        return Counts;
    }

    public void setCounts (String Counts)
    {
        this.Counts = Counts;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [New = "+New+", Enums = "+Enums+", Counts = "+Counts+"]";
    }
}
			
			