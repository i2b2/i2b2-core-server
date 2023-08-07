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
 * 	   Chris
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

/**
 * Implement comparator for ResultEntry
 * $Id: ResultEntry.java,v 1.4 2008/07/21 20:04:21 rk903 Exp $
 * @author chris
 */
public class ResultEntry {
    public int Enct_Panels;
    public int Invert;
    public long EstPanelSize;
    public int Items;
    public int AllShort;
    public int ForInsert;
    public int FirstPanel;
    public Integer Panel;
    public String TableName;
    public String FieldName;
    public long EstItemSize;
    public int SqlX;
    public String Sql0;
    public String Sql1;
    public int totalItemOccurrences = 0;
    public String totalItemoccurrencesOperator = "";

    public ResultEntry(int enct_panels, IdentityPanelEntry p, ItemEntry v) {
        this.AllShort = p.AllShort;
        this.Enct_Panels = enct_panels;
        this.EstItemSize = v.EstItemSize;
        this.EstPanelSize = p.EstPanelSize;
        this.FieldName = v.FieldName;
        this.FirstPanel = p.FirstPanel;
        this.ForInsert = p.ForInsert;
        this.Invert = p.Invert;
        this.Items = p.Items;
        this.Panel = p.Panel;
        totalItemOccurrences = p.totalItemOccurrences;
        totalItemoccurrencesOperator = p.totalItemOccurrencesOperator;
        this.Sql0 = v.Sql0;
        this.Sql1 = v.Sql1;
        this.SqlX = v.SqlX;
        this.TableName = v.TableName;
    }
}

