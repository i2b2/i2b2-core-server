/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

public class MetadataDbDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(MetadataDbDao.class);
   
    private JdbcTemplate jt;
    
    public MetadataDbDao() throws I2B2Exception{
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource("java:/OntologyBootStrapDS");
	//		log.info(ds.toString());
		} catch (I2B2Exception e2) {
			log.error("bootstrap ds failure: " + e2.getMessage());
			throw e2;
		} 
		this.jt = new JdbcTemplate(ds);
	}
	
	private String getMetadataSchema() throws I2B2Exception{

		return OntologyUtil.getInstance().getMetaDataSchemaName();
	}
	
	
	public List<DBInfoType> getDbLookupByHiveOwner(String domainId,String ownerId) throws I2B2Exception, I2B2DAOException { 
		String metadataSchema = getMetadataSchema();
		String sql =  "select * from " + metadataSchema + "ont_db_lookup where LOWER(c_domain_id) = ? and c_project_path = ? and (LOWER(c_owner_id) = ? or c_owner_id ='@') order by c_project_path";
		String projectId = "@";
//		log.info(sql + domainId + projectId + ownerId);
		List queryResult = null;
		try {
			queryResult = jt.query(sql, new getMapper(), domainId.toLowerCase(),projectId,ownerId.toLowerCase());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error");
		}
		return queryResult;
		
//		List<DBInfoType> dataSourceLookupList = 
//			this.query(sql, new Object[]{domainId,projectId,ownerId}, new mapper());
//		return dataSourceLookupList;
	}
	
	@SuppressWarnings("unchecked")
	public List<DBInfoType> getDbLookupByHiveProjectOwner(String domainId, String projectId,
			String ownerId) throws I2B2Exception, I2B2DAOException{
		String metadataSchema = getMetadataSchema();
		String sql = "select * from " + metadataSchema + "ont_db_lookup where LOWER(c_domain_id) = ? and LOWER(c_project_path) like  ? and (LOWER(c_owner_id) =? or c_owner_id = '@') order by c_project_path"; // desc  c_owner_id desc"; 
//		List<DBInfoType> dataSourceLookupList = this.query(sql, new Object[]{domainId,projectId+"%",ownerId},new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR},new mapper()  );
//		return dataSourceLookupList;
//		log.info(sql + domainId + projectId + ownerId);
		List queryResult = null;
		try {
			queryResult = jt.query(sql, new getDBInfoMapper(), domainId.toLowerCase(),projectId.toLowerCase(),ownerId.toLowerCase());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error");
		}
		return queryResult;
		
	}


}



class getDBInfoMapper implements RowMapper<DBInfoType> {
	@Override
		public DBInfoType mapRow(ResultSet rs, int rowNum) throws SQLException {
			DBInfoType dataSourceLookup = new DBInfoType();
			dataSourceLookup.setHive(rs.getString("c_domain_id"));
			dataSourceLookup.setProjectId(rs.getString("c_project_path"));
			dataSourceLookup.setOwnerId(rs.getString("c_owner_id"));
//			dataSourceLookup.setDatabaseName(rs.getString("c_db_datasource"));
			dataSourceLookup.setDb_fullSchema(rs.getString("c_db_fullschema"));
			dataSourceLookup.setDb_dataSource(rs.getString("c_db_datasource"));
			dataSourceLookup.setDb_serverType(rs.getString("c_db_servertype"));

			return dataSourceLookup;
		} 

}
