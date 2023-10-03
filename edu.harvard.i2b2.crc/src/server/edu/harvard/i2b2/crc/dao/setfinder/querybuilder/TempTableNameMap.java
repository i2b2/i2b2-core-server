/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class TempTableNameMap {

	private String tempTableName = "";
	private String tempDxTableName = "";
	private String tempMasterTableName = "";
	private String noLockSqlServer = " ";
	
	
	public TempTableNameMap(String serverType) { 
		if (serverType.equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER) ) {
			tempTableName = "#global_temp_table";
			tempDxTableName = "#dx";
			tempMasterTableName = "#master_global_temp_table";
		} else if (serverType.equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || serverType.equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL) || serverType.equalsIgnoreCase(
				DAOFactoryHelper.SNOWFLAKE)) {
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
