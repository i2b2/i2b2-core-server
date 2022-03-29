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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;

/**
 * Class to manager persistance operation of QtQueryMaster $Id:
 * QueryResultTypeSpringDao.java,v 1.4 2008/05/07 21:38:56 rk903 Exp $
 * 
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryResultTypeSpringDao extends CRCDAO implements
IQueryResultTypeDao {

	JdbcTemplate jdbcTemplate = null;
	NamedParameterJdbcTemplate namedParameterJdbcTemplate = null;

	QtResultTypeRowMapper queryResultTypeMapper = new QtResultTypeRowMapper();

	private DataSourceLookup dataSourceLookup = null;
	public QueryResultTypeSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public QtQueryResultType getQueryResultTypeById(int resultTypeId) {

		String sql = "select * from " + getDbSchemaName()
		+ "qt_query_result_type where result_type_id = ?";
		QtQueryResultType queryResultType = (QtQueryResultType) jdbcTemplate
				.queryForObject(sql, new Object[] { resultTypeId },
						queryResultTypeMapper);
		return queryResultType;
	}

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String getQueryResultTypeClassname(String resultName) {

		String sql = "select * from " + getDbSchemaName()
		+ "qt_query_result_type where name = ?";
		QtQueryResultType queryResultType = (QtQueryResultType) jdbcTemplate
				.queryForObject(sql, new Object[] { resultName },
						queryResultTypeMapper);
		return queryResultType.getClassname();
	}

	
	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<QtQueryResultType> getQueryResultTypeByName(String resultName,List<String> roles) {

		List<QtQueryResultType> queryResultType = null;

		if (roles != null)
		{
			String sql = "select * from <from>"
			+ "qt_query_result_type where name = '<resultName>' and (user_role_cd = '@' or user_role_cd is null or user_role_cd in (:roleCd))";
			Map myRoles = Collections.singletonMap("roleCd", roles);
			

			String sqlFinal =  sql.replace("<from>",   this.getDbSchemaName()  );
			sqlFinal = sqlFinal.replace("<resultName>", resultName);

			queryResultType = namedParameterJdbcTemplate.query(sqlFinal,
					myRoles,
					queryResultTypeMapper);
		} else
		{
			String sql = "select * from " + getDbSchemaName()
			+ "qt_query_result_type where name = ?";
			queryResultType = jdbcTemplate.query(sql,
					new Object[] { resultName.toUpperCase() },
					queryResultTypeMapper);
		}
		return queryResultType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<QtQueryResultType> getAllQueryResultType(List<String> roles) {

		List<QtQueryResultType> queryResultTypeList = null;
		if (roles != null)
		{
			String sql = "select * from " + getDbSchemaName()
			+ "qt_query_result_type where user_role_cd = '@' or user_role_cd is null or user_role_cd in (:roleCd) order by result_type_id";
			Map myRoles = Collections.singletonMap("roleCd", roles);
			queryResultTypeList = namedParameterJdbcTemplate.query(sql,
					myRoles ,
					queryResultTypeMapper);
		} else {
			String sql = "select * from " + getDbSchemaName()
			+ "qt_query_result_type order by result_type_id";
			queryResultTypeList = jdbcTemplate.query(sql,
					queryResultTypeMapper);
		}
		return queryResultTypeList;
	}

	private static class QtResultTypeRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryResultType queryResultType = new QtQueryResultType();
			queryResultType.setResultTypeId(rs.getInt("RESULT_TYPE_ID"));
			queryResultType.setName(rs.getString("NAME"));
			queryResultType.setDescription(rs.getString("DESCRIPTION"));
			queryResultType.setDisplayType(rs.getString("DISPLAY_TYPE_ID"));
			queryResultType.setVisualAttributeType(rs
					.getString("VISUAL_ATTRIBUTE_TYPE_ID"));
			queryResultType.setUserRoleCd(rs.getString("USER_ROLE_CD"));
			queryResultType.setClassname(rs.getString("CLASSNAME"));
			return queryResultType;
		}
	}

}
