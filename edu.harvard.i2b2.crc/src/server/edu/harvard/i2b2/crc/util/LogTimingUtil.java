package edu.harvard.i2b2.crc.util;

import java.util.List;

//import javax.management.MalformedObjectNameException;

//import org.jboss.cache.Cache;
//import org.jboss.cache.Node;

import edu.harvard.i2b2.crc.datavo.pm.ParamType;

public class LogTimingUtil {
	public static final String PM_ENABLE_PROCESS_TIMING = "PM_ENABLE_PROCESS_TIMING"; 
	
	
		long startTime = 0 ; 
		long endTime = 0;
		
		public void setStartTime() { 
			this.startTime = System.currentTimeMillis();
		}
		
		public long getStartTime() { 
			return this.startTime;
		}
		
		public void setEndTime() { 
			this.endTime = System.currentTimeMillis();
		}
		
		public long getEndTime() { 
			return this.endTime;
		}
		
		public double getDiffTime() { 
			return new Double((endTime - startTime)/1000.0);
		}
		
		public static void putPocessTiming(String projectId, String userId, String domainId,
				ParamType paramType) {
			// get cache
			try {
				//TODO removed cache
				//Cache cache = CacheUtil.getCache();
				//Node rootNode = cache.getRoot();
				String roleTree = domainId + "/" + projectId + "/" + userId + "/" + PM_ENABLE_PROCESS_TIMING;
				//rootNode.put(roleTree, paramType.getValue());
				CacheUtil.put(roleTree, paramType.getValue());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public static String getPocessTiming(String projectId, String userId, String domainId) {
			String processTimingPMFlag = "NONE";
			// get cache
			try {
				//TODO removed cache
		//		Node rootNode = CacheUtil.getCache().getRoot();
		//		processTimingPMFlag = (String) rootNode
		//				.get(domainId + "/" + projectId
		//						+ "/" + userId + "/" + PM_ENABLE_PROCESS_TIMING );
				processTimingPMFlag = (String) CacheUtil.get(domainId + "/" + projectId
												+ "/" + userId + "/" + PM_ENABLE_PROCESS_TIMING );
			} catch (Exception e) {
				e.printStackTrace();
			}
			return processTimingPMFlag;
		}
		
		public static String clearPocessTiming(String projectId, String userId, String domainId) {
			String processTimingPMFlag = "NONE";
			// get cache
			try {
				//TODO removed cache

				//Node rootNode = CacheUtil.getCache().getRoot();
				// processTimingPMFlag = (String) rootNode
				//		.put(domainId + "/" + projectId
				//				+ "/" + userId + "/" + PM_ENABLE_PROCESS_TIMING, null );
				
				 CacheUtil.remove(domainId + "/" + projectId
										+ "/" + userId + "/" + PM_ENABLE_PROCESS_TIMING );
			} catch (Exception e) {
				e.printStackTrace();
			}
			return processTimingPMFlag;
		}
	
}
