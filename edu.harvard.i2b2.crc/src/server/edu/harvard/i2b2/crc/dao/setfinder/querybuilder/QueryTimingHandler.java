package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;

public class QueryTimingHandler {

	public static final String ANY = "ANY";
	public static final String SAME = "SAME";
	public static final String SAMEVISIT = "SAMEVISIT";
	public static final String SAMEINSTANCENUM = "SAMEINSTANCENUM";

	public boolean isSameVisit(QueryDefinitionType queryDefType) {
		String queryTiming = queryDefType.getQueryTiming();
		return isSameVisit(queryTiming);
	}

	public boolean isSameInstanceNum(QueryDefinitionType queryDefType) {
		String queryTiming = queryDefType.getQueryTiming();
		return isSameInstanceNum(queryTiming);
	}
	
	public boolean isSameVisit(String queryTiming) { 
		if (queryTiming == null) {
			return false;
		}
		if (queryTiming.equalsIgnoreCase("SAME")
				|| queryTiming.equalsIgnoreCase("SAMEVISIT")) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isSameInstanceNum(String queryTiming) {
		
		if (queryTiming == null) {
			return false;
		}
		if (queryTiming.equalsIgnoreCase("SAMEINSTANCENUM")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isAny(String queryTiming) {
		
		if (queryTiming == null) {
			return false;
		}
		if (queryTiming.equalsIgnoreCase("ANY")) {
			return true;
		} else {
			return false;
		}
	}
	

}
