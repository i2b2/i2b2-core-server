/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.workplace.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetReturnType;
import edu.harvard.i2b2.workplace.datavo.wdo.XmlValueType;
import edu.harvard.i2b2.workplace.ejb.DBInfoType;
import edu.harvard.i2b2.workplace.util.WorkplaceUtil;


public class WorkplaceDbDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(WorkplaceDbDao.class);
   
    private SimpleJdbcTemplate jt;
    
    public WorkplaceDbDao() throws I2B2Exception{
		DataSource ds = null;
		try {
			ds = WorkplaceUtil.getInstance().getDataSource("java:/WorkplaceBootStrapDS");
	//		log.info(ds.toString());
		} catch (I2B2Exception e2) {
			log.error("bootstrap ds failure: " + e2.getMessage());
			throw e2;
		} 
		this.jt = new SimpleJdbcTemplate(ds);
	}
	
	private String getMetadataSchema() throws I2B2Exception{

		return WorkplaceUtil.getInstance().getMetaDataSchemaName();
	}
	
	
	public List<DBInfoType> getDbLookupByHiveOwner(String domainId,String ownerId) throws I2B2Exception, I2B2DAOException { 
		String metadataSchema = getMetadataSchema();
		String sql =  "select * from " + metadataSchema + "work_db_lookup where LOWER(c_domain_id) = ? and c_project_path = ? and (LOWER(c_owner_id) = ? or c_owner_id ='@') order by c_project_path";
		String projectId = "@";
//		log.info(sql + domainId + projectId + ownerId);
		List queryResult = null;
		try {
			queryResult = jt.query(sql, getMapper(), domainId.toLowerCase(),projectId,ownerId.toLowerCase());
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
		String sql = "select * from " + metadataSchema + "work_db_lookup where LOWER(c_domain_id) = ? and LOWER(c_project_path) like  ? and (LOWER(c_owner_id) =? or c_owner_id = '@') order by c_project_path"; // desc  c_owner_id desc"; 
//		List<DBInfoType> dataSourceLookupList = this.query(sql, new Object[]{domainId,projectId+"%",ownerId},new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR},new mapper()  );
//		return dataSourceLookupList;
//		log.info(sql + domainId + projectId + ownerId);
		List queryResult = null;
		try {
			queryResult = jt.query(sql, getMapper(), domainId.toLowerCase(),projectId.toLowerCase(),ownerId.toLowerCase());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error");
		}
		return queryResult;
		
	}
	private ParameterizedRowMapper getMapper() {
		ParameterizedRowMapper<DBInfoType> map = new ParameterizedRowMapper<DBInfoType>() {
			public DBInfoType mapRow(ResultSet rs, int rowNum) throws SQLException {
				DBInfoType dataSourceLookup = new DBInfoType();
				dataSourceLookup.setHive(rs.getString("c_domain_id"));
				dataSourceLookup.setProjectId(rs.getString("c_project_path"));
				dataSourceLookup.setOwnerId(rs.getString("c_owner_id"));
//				dataSourceLookup.setDatabaseName(rs.getString("c_db_datasource"));
				dataSourceLookup.setDb_fullSchema(rs.getString("c_db_fullschema"));
				dataSourceLookup.setDb_dataSource(rs.getString("c_db_datasource"));
				dataSourceLookup.setDb_serverType(rs.getString("c_db_servertype"));

				return dataSourceLookup;
			} 
		};
		return map;
	}

}
