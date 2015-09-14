/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
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
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;

public class SchemesDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(SchemesDao.class);
	final static String DEFAULT = " c_key, c_name ";
	
    private SimpleJdbcTemplate jt;
    
	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new SimpleJdbcTemplate(ds);
	}
	
	private String getMetadataSchema() throws I2B2Exception{

		return OntologyUtil.getInstance().getMetaDataSchemaName();
	}
	
	public List findSchemes(final GetReturnType returnType, final DBInfoType dbInfo) throws DataAccessException{

		// find return parameters
		String parameters = DEFAULT;		

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		
		String schemesSql = "select distinct " + parameters  + " from " + metadataSchema + "schemes order by c_name";
 
		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            ConceptType scheme = new ConceptType();

	            //assume key is "" unless we explicitly determine otherwise
	            // "" is valid
	            scheme.setKey("");	            
	            String c_key = rs.getString("c_key");
	            if((c_key!=null)&&(c_key.trim().length()>0)&&(!c_key.equals("(null)")))
	            	scheme.setKey(c_key);
	            scheme.setName(rs.getString("c_name"));
	            return scheme;
	        }
	    };

		
		List queryResult = null;
		try {
		    queryResult = jt.query(schemesSql, mapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		
		return queryResult;

	}
	



}
