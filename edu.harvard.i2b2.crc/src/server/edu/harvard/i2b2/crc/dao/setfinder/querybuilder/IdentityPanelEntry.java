/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

/**
 * Panel bean class
 * $Id: IdentityPanelEntry.java,v 1.4 2008/07/21 20:04:21 rk903 Exp $
 * @author chris
 */
public class IdentityPanelEntry extends PanelEntry {
    private static int identityValue = 1;

    public IdentityPanelEntry() {
        this.Panel = identityValue;
        identityValue++;
    }

    public IdentityPanelEntry(PanelEntry p) {
        this.AllShort = p.AllShort;
        this.EstPanelSize = p.EstPanelSize;
        this.FirstPanel = p.FirstPanel;
        this.ForInsert = p.ForInsert;
        this.Invert = p.Invert;
        this.Items = p.Items;
        this.OldPanel = p.Panel;
        this.totalItemOccurrences = p.totalItemOccurrences;
        this.totalItemOccurrencesOperator = p.totalItemOccurrencesOperator;
        this.Panel = identityValue;
        identityValue++;
    }

    public boolean equals(Object o) {
        if ((o != null) && (o.getClass().equals(this.getClass()))) {
            IdentityPanelEntry p = (IdentityPanelEntry) o;

            if ((this.OldPanel == p.OldPanel) && (this.Invert == p.Invert) &&
                    (this.EstPanelSize == p.EstPanelSize) &&
                    (this.Items == p.Items) && (this.AllShort == p.AllShort) &&
                    (this.ForInsert == p.ForInsert) &&
                    (this.FirstPanel == p.FirstPanel) &&
                    (this.totalItemOccurrences == p.totalItemOccurrences)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
