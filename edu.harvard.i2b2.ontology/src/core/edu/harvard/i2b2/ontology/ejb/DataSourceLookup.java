package edu.harvard.i2b2.ontology.ejb;

import javax.sql.DataSource;

public class DataSourceLookup {
    private String hive;
    private String projectId;
    private String ownerId;
    private DataSource dataSource;
    private String  databaseName;
	public String getHive() {
		return hive;
	}
	public void setHive(String hive) {
		this.hive = hive;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
    
}
