/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.util;

//import javax.management.MalformedObjectNameException;

//import org.jboss.cache.Cache;
//import org.jboss.cache.Node;

import edu.harvard.i2b2.crc.datavo.pm.ParamType;

public class ParamUtil {

	public static final String PM_ENABLE_PROCESS_TIMING = "PM_ENABLE_PROCESS_TIMING";
	public static final String CRC_ENABLE_UNITCD_CONVERSION = "CRC_ENABLE_UNITCD_CONVERSION";
	
	public  void putParam(String projectId, String userId, String domainId,String paramName,
			ParamType paramType) {
		// get cache
		try {
			//TODO removed cache
			//Cache cache = CacheUtil.getCache();
			//Node rootNode = cache.getRoot();
			String roleTree = domainId + "/" + projectId + "/" + userId + "/" + paramName;
			//rootNode.put(roleTree, paramType.getValue());
			CacheUtil.put(roleTree, paramType.getValue());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public  String getParam(String projectId, String userId, String domainId, String paramName) {
		String processTimingPMFlag = "NONE";
		// get cache
		try {
			
			//Node rootNode = CacheUtil.getCache().getRoot();
			//processTimingPMFlag = (String) rootNode
			//		.get(domainId + "/" + projectId
			//				+ "/" + userId + "/" + paramName );
			processTimingPMFlag = (String) CacheUtil.get(domainId + "/" + projectId
							+ "/" + userId + "/" + paramName );

		} catch (Exception e) {
			e.printStackTrace();
		}
		return processTimingPMFlag;
	}
	
	public   String clearParam(String projectId, String userId, String domainId, String paramName) {
		String processTimingPMFlag = "NONE";
		// get cache
		try {
			
			//Node rootNode = CacheUtil.getCache().getRoot();
			//processTimingPMFlag = (String) rootNode
			//		.put(domainId + "/" + projectId
			//				+ "/" + userId + "/" + paramName, null );
			CacheUtil.remove(domainId + "/" + projectId
							+ "/" + userId + "/" + paramName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return processTimingPMFlag;
	}
}
