/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.dao;

import java.util.List;

import javax.sql.DataSource;

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;



public class SQLServerDataSourceLookupDAO extends DataSourceLookupDAO {
	
	public SQLServerDataSourceLookupDAO(DataSource dataSource,String schemaName) { 
		
	}

	@Override
	public List<DataSourceLookup> getDbLookupByHiveOwner(String hiveId,
			String ownerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataSourceLookup> getDbLookupByHiveProjectOwner(String hiveId,
			String projectId, String ownerId) {
		// TODO Auto-generated method stub
		return null;
	}




}
