/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;


public class CalulateQueryTiming {

	public String getQueryTiming(List<PanelType> panelList) { 
		String calcQueryTiming = "ANY";
		for (PanelType panel : panelList) { 
			if(panel.getPanelTiming() != null && panel.getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)) { 
				calcQueryTiming = QueryTimingHandler.SAMEINSTANCENUM;
			  break;
			} else if (panel.getPanelTiming() != null && (panel.getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAMEVISIT) ||
					panel.getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME))) { 
				calcQueryTiming = QueryTimingHandler.SAMEVISIT ; 
			}
		}
		return calcQueryTiming;
	}
}
