/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap.xml;

public class UnitValues
{
    private String NormalUnits  = "";

    private String ExcludingUnits = "";

    private ConvertingUnits ConvertingUnits = new ConvertingUnits();

    private String EqualUnits = "";

    public String getNormalUnits ()
    {
        return NormalUnits;
    }

    public void setNormalUnits (String NormalUnits)
    {
        this.NormalUnits = NormalUnits;
    }

    public String getExcludingUnits ()
    {
        return ExcludingUnits;
    }

    public void setExcludingUnits (String ExcludingUnits)
    {
        this.ExcludingUnits = ExcludingUnits;
    }

    public ConvertingUnits getConvertingUnits ()
    {
        return ConvertingUnits;
    }

    public void setConvertingUnits (ConvertingUnits ConvertingUnits)
    {
        this.ConvertingUnits = ConvertingUnits;
    }

    public String getEqualUnits ()
    {
        return EqualUnits;
    }

    public void setEqualUnits (String EqualUnits)
    {
        this.EqualUnits = EqualUnits;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [NormalUnits = "+NormalUnits+", ExcludingUnits = "+ExcludingUnits+", ConvertingUnits = "+ConvertingUnits+", EqualUnits = "+EqualUnits+"]";
    }
}
			
			