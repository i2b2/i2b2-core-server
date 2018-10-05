/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public class OracleDataSourceLookupDAO extends DataSourceLookupDAO  {
   
	 /** log **/
    protected final Log log = LogFactory.getLog(OracleDataSourceLookupDAO.class);

	private String schemaName = null;
	
	public OracleDataSourceLookupDAO(DataSource dataSource,String schemaName) {
		setDataSource(dataSource);
		this.schemaName = schemaName;
	}
	
	@Override
	public List<DataSourceLookup> getDbLookupByHiveOwner(String domainId,String ownerId) { 
		String sql =  "select * from crc_db_lookup where LOWER(c_domain_id) = ? and c_project_path = ? and (LOWER(c_owner_id) = ? or c_owner_id ='@') order by c_project_path";
		String projectId = "@";
		List<DataSourceLookup> dataSourceLookupList = 
			this.query(sql, new Object[]{domainId.toLowerCase(),projectId,ownerId.toLowerCase()}, new mapper());
		return dataSourceLookupList;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DataSourceLookup> getDbLookupByHiveProjectOwner(String domainId, String projectId,
			String ownerId) {
		String sql = "select * from crc_db_lookup where LOWER(c_domain_id) = ? and c_project_path like  ? and (LOWER(c_owner_id) =? or c_owner_id = '@') order by c_project_path"; 
		List<DataSourceLookup> dataSourceLookupList = this.query(sql, new Object[]{domainId.toLowerCase(),projectId+"%",ownerId.toLowerCase()},new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR},new mapper()  );
		return dataSourceLookupList;
	}
	
	
	
	
	
	public static void main(String args[]) { 
		OracleDataSourceLookupDAO dao = new OracleDataSourceLookupDAO(null,null);
		
	}

	public class mapper implements RowMapper {

	    @Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
	        DataSourceLookup dataSourceLookup = new DataSourceLookup();
	        dataSourceLookup.setDomainId(rs.getString("c_domain_id"));
	        dataSourceLookup.setProjectPath(rs.getString("c_project_path"));
	        dataSourceLookup.setOwnerId(rs.getString("c_owner_id"));
	        dataSourceLookup.setFullSchema(rs.getString("c_db_fullschema"));
	        dataSourceLookup.setDataSource(rs.getString("c_db_datasource"));
	        dataSourceLookup.setServerType(rs.getString("c_db_servertype")); 
	        dataSourceLookup.setNiceName(rs.getString("c_db_nicename"));
	        dataSourceLookup.setToolTip(rs.getString("c_db_tooltip"));
	        dataSourceLookup.setComment(rs.getString("c_comment"));
	        dataSourceLookup.setEntryDate(rs.getDate("c_entry_date"));
	        dataSourceLookup.setChangeDate(rs.getDate("c_change_date"));
	        dataSourceLookup.setStatusCd(rs.getString("c_status_cd"));
	        return dataSourceLookup;
	    }
	}
}



