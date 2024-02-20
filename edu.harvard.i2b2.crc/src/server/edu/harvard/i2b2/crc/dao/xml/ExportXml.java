package edu.harvard.i2b2.crc.dao.xml;
/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/



public class ExportXml
{
	private ValueExport ValueExport;

	public ValueExport getValueExport ()
	{
		return ValueExport;
	}

	public void setValueExport (ValueExport ValueExport)
	{
		this.ValueExport = ValueExport;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [ValueExport = "+ValueExport+"]";
	}
}