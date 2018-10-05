/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
