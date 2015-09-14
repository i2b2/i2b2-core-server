package edu.harvard.i2b2.crc.loader.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;


public class DataSourceLookupHelper {

	 /** log **/
    protected final static Log log = LogFactory.getLog(DataSourceLookupHelper.class);
	 
	private DataSourceLookupDAO dsLookupDao = null;
	
	public DataSourceLookupHelper() throws I2B2Exception { 
		
		dsLookupDao = DataSourceLookupDAOFactory.getDataSourceLookupDAO();
	}
	
	public DataSourceLookup matchDataSource(String hiveId, String projectId,
			String ownerId) throws I2B2Exception {
		DataSourceLookup matchedDataSourceLookup = null;
		List<DataSourceLookup> dataSourceLookupList = null;
		if (projectId == null) { 
			throw new I2B2Exception(" Project id is null, could not perform datasource lookup for given domain["+hiveId+"] and owner[" + ownerId+"]");
		}
		
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
			filterProjectId = "/" + projectSplit[1] + "/" + projectSplit[2] + "/";
		}
		else { 
			filterProjectId = "/" + projectSplit[1] + "/";
		}
		dataSourceLookupList = dsLookupDao.getDbLookupByHiveProjectOwner(hiveId, filterProjectId, ownerId);
		//if the project path with two level fails then try with one level
		if (dataSourceLookupList.size()<1) { 
			filterProjectId = "/" + projectSplit[1] + "/";
			dataSourceLookupList = dsLookupDao.getDbLookupByHiveProjectOwner(hiveId, filterProjectId, ownerId);
		}
		if (dataSourceLookupList.size()>0) { 
			DataSourceLookup matchedDataSource = null;
			int projectLevel = projectId.length();
			boolean matchedFlag = false;
			 String parentProjectId=null;
			while ((projectLevel=projectId.lastIndexOf('/', projectLevel))>0) {
			    parentProjectId = projectId.substring(0, projectLevel+1);
				log.debug("Trying with project id :" + parentProjectId);
				matchedDataSource = match(parentProjectId,ownerId,dataSourceLookupList);
				if (matchedDataSource != null) {
					matchedFlag = true;
					break;
				}
				
				projectLevel = projectId.lastIndexOf('/', projectLevel-1);
			}
			if (matchedFlag) { 
					if (ownerId.equalsIgnoreCase(matchedDataSource.getOwnerId())) { 
						log.info("Located DataSource for hiveId=[" + hiveId + 
								"] projectId=[" + parentProjectId + "] and ownerId =[" + 
								ownerId + "]");
					} else { 
						log.info("Located DataSource for hiveId=[" + hiveId + 
								"] projectId=[" + parentProjectId + "]");
						
					}
				return matchedDataSource;
				
			}
			else { 
				log.info("Could not match Project id=[" + projectId+"] Trying with hive =" + hiveId);
				matchedDataSourceLookup = matchHiveOwner(hiveId,ownerId);
				return matchedDataSourceLookup;
			}
			
		} else { 
			log.info("Could not match Project id=[" + projectId+"] Trying with hive =" + hiveId);
			matchedDataSourceLookup = matchHiveOwner(hiveId,ownerId);
			return matchedDataSourceLookup;
		}
		
		
	
	}
	
	private DataSourceLookup matchHiveOwner(String hiveId,String ownerId) throws I2B2Exception  { 
		DataSourceLookup matchedDataSourceLookup = null;
		//check for hive and owner (userid or @)
		List<DataSourceLookup> dataSourceLookupList = dsLookupDao.getDbLookupByHiveOwner(hiveId, ownerId);
		if (dataSourceLookupList.size() == 0 ) {
			throw new I2B2Exception("Could not locate hive= " + hiveId);
		}
		else if (dataSourceLookupList.size() >0) {
			boolean matchedFlag = false;
			
			for (DataSourceLookup dsLookup : dataSourceLookupList) { 
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
				log.info("Located Datasource matching hive=[" + hiveId + "] and owner=[" + ownerId + "]");
			} else if (matchedDataSourceLookup != null) { 
				log.info("Located Datasource matching hive=[" + hiveId + " and owner =[@]");
			} else { 
				throw new I2B2Exception("Could not locate Datasource matching hive=[" + hiveId + "] and owner=["+ ownerId + " or @]");
			}
		}
		return matchedDataSourceLookup;
	}
	
	private DataSourceLookup match(String searchProjectId,String owner, List <DataSourceLookup> dataSourceLookupList) {
		DataSourceLookup matchedDataSource = null;
		
		for (DataSourceLookup dataSourceLookup : dataSourceLookupList) {
			String lookupProjectId = dataSourceLookup.getProjectPath();
			String lookupOwnerId = dataSourceLookup.getOwnerId();
			if (searchProjectId.equalsIgnoreCase(lookupProjectId)) {
				matchedDataSource = dataSourceLookup;
				if (owner.equalsIgnoreCase(lookupOwnerId)) { 
					break;
				}
				
				
			}
		}
		return matchedDataSource;
	}
	
	
}
