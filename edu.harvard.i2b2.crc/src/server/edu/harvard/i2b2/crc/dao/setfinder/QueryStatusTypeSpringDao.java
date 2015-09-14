/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.IntegerStringUserType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;


/**
 * Class to manager persistance operation of
 * QtQueryMaster
 * $Id: QueryStatusTypeSpringDao.java,v 1.3 2008/05/07 21:39:08 rk903 Exp $
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryStatusTypeSpringDao extends CRCDAO implements IQueryStatusTypeDao {
	
	JdbcTemplate jdbcTemplate = null;
	
	QtStatusTypeRowMapper queryStatusTypeMapper = new QtStatusTypeRowMapper();
	

	

	private DataSourceLookup dataSourceLookup = null;
	
	
	public QueryStatusTypeSpringDao(DataSource dataSource,DataSourceLookup dataSourceLookup) { 
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;
		
	}
	
    /**
     * Returns list of query master by user id
     * @param userId
     * @return List<QtQueryMaster>
     */
    @SuppressWarnings("unchecked")
    public QtQueryStatusType getQueryStatusTypeById(int statusTypeId) {
    	
        String sql = "select * from " + getDbSchemaName() + "qt_query_status_type where status_type_id = ?" ;
        QtQueryStatusType queryStatusType = (QtQueryStatusType)jdbcTemplate.queryForObject(sql,new Object[]{statusTypeId},queryStatusTypeMapper );
        return queryStatusType;
    }
   
  private static class QtStatusTypeRowMapper implements RowMapper {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    	  QtQueryStatusType queryStatusType = new QtQueryStatusType();
    	  queryStatusType.setStatusTypeId(rs.getInt("STATUS_TYPE_ID"));
    	  queryStatusType.setName(rs.getString("NAME"));
    	  queryStatusType.setDescription(rs.getString("DESCRIPTION"));
          
          return queryStatusType;
      }
  }
    
    
}
