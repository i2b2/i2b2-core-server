/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public  abstract class DataSourceLookupDAO extends JdbcTemplate {
	public abstract List<DataSourceLookup> getDbLookupByHiveOwner(String hiveId,String ownerId);
	//public abstract DataSourceLookup getDataSourceByHiveProjectOwner(String hive,String projectId, String owner);
	public abstract List<DataSourceLookup> getDbLookupByHiveProjectOwner(String hiveId, String projectId,
			String ownerId);
	public abstract List<DataSourceLookup> getDbLookupByHive(String hiveId);	
}
