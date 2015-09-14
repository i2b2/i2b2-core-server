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
