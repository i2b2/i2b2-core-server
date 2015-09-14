/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 * 		Raj Kuttan
 * 
 */

package edu.harvard.i2b2.workplace.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.ejb.DBInfoType;
import edu.harvard.i2b2.workplace.ejb.DataSourceLookup;

public class DataSourceLookupHelper {

	 /** log **/
    protected final Log log = LogFactory.getLog(getClass());

	private WorkplaceDbDao dsLookupDao = null;
	
	public DataSourceLookupHelper() throws I2B2Exception { 
		
		dsLookupDao = new WorkplaceDbDao();
	}
	
	public DBInfoType matchDataSource(String hiveId, String projectId,
			String ownerId) throws I2B2Exception {
		DBInfoType matchedDataSourceLookup = null;
		List<DBInfoType> dataSourceLookupList = null; 
		if (projectId.equals("@")) { 
			matchedDataSourceLookup = matchHiveOwner(hiveId,ownerId);
			return matchedDataSourceLookup;
		}
		
		//check if project id contains subprojects, if so then query by first level subproject
		if (projectId.endsWith("/") == false) {
			projectId += "/";
		}
		if (projectId.startsWith("/") == false) { 
			projectId = "/" + projectId;
		}
		String[] projectSplit = projectId.split("/");
		String filterProjectId = "";
		if (projectSplit.length>2) { 
//			filterProjectId = "/" + projectSplit[1] + "/" + projectSplit[2] + "/";
			filterProjectId = projectSplit[1] + "/" + projectSplit[2] + "/";
		}
		else { 
	//		filterProjectId = "/" + projectSplit[1] + "/";
			filterProjectId = projectSplit[1] + "/";
		}
	
		dataSourceLookupList = dsLookupDao.getDbLookupByHiveProjectOwner(hiveId, filterProjectId+"%", ownerId);
	
		if (dataSourceLookupList.size()>0) { 
			DBInfoType matchedDataSource = null;
			int projectLevel = projectId.length();
			boolean matchedFlag = false;
			 String parentProjectId=null;
			while ((projectLevel=projectId.lastIndexOf('/', projectLevel))>0) {
			    parentProjectId = projectId.substring(1, projectLevel+1);   // do not include leading '/' that was added
		//		log.info("Trying with project id: " + parentProjectId);
				matchedDataSource = match(parentProjectId,ownerId,dataSourceLookupList);
				if (matchedDataSource != null) {
					matchedFlag = true;
					break;
				}
				
				projectLevel = projectId.lastIndexOf('/', projectLevel-1);
			}
			if (matchedFlag) { 
					if (ownerId.equalsIgnoreCase(matchedDataSource.getOwnerId())) { 
						log.debug("Located DataSource for hiveId=[" + hiveId + 
								"] projectId=[" + parentProjectId + "] and ownerId =[" + 
								ownerId + "]");
					} else { 
						log.debug("Located DataSource for hiveId=[" + hiveId + 
								"] projectId=[" + parentProjectId + "]");
						
					}
				return matchedDataSource;
				
			}
			else { 
				log.debug("Could not match Project id=[" + projectId+"] Trying with hive =" + hiveId);
				matchedDataSourceLookup = matchHiveOwner(hiveId,ownerId);
				return matchedDataSourceLookup;
			}
			
		} else { 
			log.debug("Could not match Project id=[" + projectId+"] Trying with hive =" + hiveId);
			matchedDataSourceLookup = matchHiveOwner(hiveId,ownerId);
			return matchedDataSourceLookup;
		}
		
		
	
	}
	
	private DBInfoType matchHiveOwner(String hiveId,String ownerId) throws I2B2Exception  { 
		DBInfoType matchedDataSourceLookup = null;
		//check for hive and owner (userid or @)
		List<DBInfoType> dataSourceLookupList = dsLookupDao.getDbLookupByHiveOwner(hiveId, ownerId);
		if (dataSourceLookupList.size() == 0 ) {
			throw new I2B2Exception("Could not locate hive= " + hiveId);
		}
		else if (dataSourceLookupList.size() >0) {
			boolean matchedFlag = false;
			
			for (DBInfoType dsLookup : dataSourceLookupList) { 
				if(dsLookup.getOwnerId() != null && dsLookup.getOwnerId().equalsIgnoreCase(ownerId)) {
					matchedDataSourceLookup = dsLookup;
					matchedFlag = true;
					break;
				}
				else if (dsLookup.getOwnerId() !=null && dsLookup.getOwnerId().equals("@")) { 
					matchedDataSourceLookup = dsLookup;
				}
			}
			if (matchedFlag) { 
				log.debug("Located Datasource matching hive=[" + hiveId + "] and owner=[" + ownerId + "]");
			} else if (matchedDataSourceLookup != null) { 
				log.debug("Located Datasource matching hive=[" + hiveId + " and owner =[@]");
			} else { 
				throw new I2B2Exception("Could not locate Datasource matching hive=[" + hiveId + "] and owner=["+ ownerId + " or @]");
			}
		}
		return matchedDataSourceLookup;
	}
	
	private DBInfoType match(String searchProjectId,String owner, List <DBInfoType> dataSourceLookupList) {
		DBInfoType matchedDataSource = null;
		
		for (DBInfoType dataSourceLookup : dataSourceLookupList) {
			String lookupProjectId = dataSourceLookup.getProjectId();
			String lookupOwnerId = dataSourceLookup.getOwnerId();
	//		log.info("in match: " + lookupProjectId + lookupOwnerId);
			if (searchProjectId.equalsIgnoreCase(lookupProjectId)) {
				matchedDataSource = dataSourceLookup;
				if (owner.equalsIgnoreCase(lookupOwnerId)) { 
		//			log.info("found owner id");
					break;
				}//else if(lookupOwnerId.equals("@")) {
		//			log.info("defaulted to @");
		//			break;
		//		}
			}
		}
		return matchedDataSource;
	}
	
	
}
