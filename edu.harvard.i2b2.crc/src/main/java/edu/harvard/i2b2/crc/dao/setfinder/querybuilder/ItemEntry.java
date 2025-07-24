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
 * ItemEntry bean class
 * $Id: ItemEntry.java,v 1.3 2007/08/31 14:46:23 rk903 Exp $
 * @author chris
 */
public class ItemEntry {
    public Integer Panel;
    public String TableName;
    public String FieldName;
    public long EstItemSize;
    public int SqlX;
    public String Sql0;
    public String Sql1;

    public ItemEntry() {
    }

    /**
     * Override equals function
     */
    @Override
	public boolean equals(Object o) {
        if ((o != null) && (o.getClass().equals(this.getClass()))) {
            ItemEntry p = (ItemEntry) o;

            if ((this.Panel.equals(p.Panel)) &&
                    (this.TableName.equals(p.TableName)) &&
                    (this.FieldName.equals(p.FieldName)) &&
                    (this.EstItemSize == p.EstItemSize) &&
                    (this.SqlX == p.SqlX) && (this.Sql0.equals(p.Sql0)) &&
                    (this.Sql1.equals(p.Sql1))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
