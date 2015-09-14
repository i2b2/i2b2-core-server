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

import java.util.Comparator;


/**
 * Implement comparator for panelentry
 * $Id: PanelEntryComparator.java,v 1.3 2007/08/31 14:46:23 rk903 Exp $
 * @author chris
 */
public class PanelEntryComparator implements Comparator {
    public int compare(Object element1, Object element2) {
        if ((element1.getClass().equals(PanelEntry.class)) &&
                (element2.getClass().equals((PanelEntry.class)))) {
            PanelEntry p1 = (PanelEntry) element1;
            PanelEntry p2 = (PanelEntry) element2;
            int compare = new Integer(p1.Invert).compareTo(new Integer(
                        p2.Invert));

            if (compare == 0) {
                compare = new Long(p1.EstPanelSize).compareTo(new Long(
                            p2.EstPanelSize));

                if (compare == 0) {
                    return p1.Panel.compareTo(p2.Panel);
                } else {
                    return compare;
                }
            } else {
                return compare;
            }
        } else {
            return element1.toString().compareTo(element2.toString());
        }
    }
}
