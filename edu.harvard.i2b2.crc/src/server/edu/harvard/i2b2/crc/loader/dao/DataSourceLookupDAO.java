package edu.harvard.i2b2.crc.loader.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public  abstract class DataSourceLookupDAO extends JdbcTemplate {
	public abstract List<DataSourceLookup> getDbLookupByHiveOwner(String hiveId,String ownerId);
	//public abstract DataSourceLookup getDataSourceByHiveProjectOwner(String hive,String projectId, String owner);
	public abstract List<DataSourceLookup> getDbLookupByHiveProjectOwner(String hiveId, String projectId,
			String ownerId);
	
}
