/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;
/**
 * Panel Entry bean class
 * $Id: PanelEntry.java,v 1.4 2008/07/21 20:04:21 rk903 Exp $ 
 * @author chris
 */
public class PanelEntry
{
	public Integer Panel = 0;
	public Integer OldPanel = -1;
	public int Invert= 0;;
	public long EstPanelSize = 0;
	public int Items = 0;
	public int AllShort = 0;
	public int ForInsert = 0;
	public int FirstPanel = 0;
	public int totalItemOccurrences = 0;
	public String totalItemOccurrencesOperator = "";
	
	@Override
	public boolean equals(Object o)
	{
		if ((o!=null)&&(o.getClass().equals(this.getClass())))
		{
			PanelEntry p = (PanelEntry) o;
			if ((this.Panel==p.Panel)&&
					(this.Invert==p.Invert)&&
					(this.EstPanelSize==p.EstPanelSize)&&
					(this.Items==p.Items)&&
					(this.AllShort==p.AllShort)&&
					(this.ForInsert==p.ForInsert)&&
					(this.FirstPanel==p.FirstPanel) && 
					(this.totalItemOccurrences == p.totalItemOccurrences))
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	
}

