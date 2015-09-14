package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class TempTableNameMap {

	private String tempTableName = "";
	private String tempDxTableName = "";
	private String tempMasterTableName = "";
	private String noLockSqlServer = " ";
	
	
	public TempTableNameMap(String serverType) { 
		if (serverType.equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			tempTableName = "#global_temp_table";
			tempDxTableName = "#dx";
			tempMasterTableName = "#master_global_temp_table";
		} else if (serverType.equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || serverType.equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) {
			tempTableName = "QUERY_GLOBAL_TEMP";
			tempDxTableName = "DX";
			tempMasterTableName = "MASTER_QUERY_GLOBAL_TEMP";
			
		}
	}
	
	public String getTempTableName() { 
		return this.tempTableName;
	}
	
	public String getTempDxTableName() { 
		return this.tempDxTableName;
	}
	
	public String getTempMasterTable() { 
		return this.tempMasterTableName;
	}
	
	
}
