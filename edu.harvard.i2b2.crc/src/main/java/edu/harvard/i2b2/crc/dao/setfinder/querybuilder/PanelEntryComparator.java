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

import java.util.Comparator;


/**
 * Implement comparator for panelentry
 * $Id: PanelEntryComparator.java,v 1.3 2007/08/31 14:46:23 rk903 Exp $
 * @author chris
 */
public class PanelEntryComparator implements Comparator {
    @Override
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
