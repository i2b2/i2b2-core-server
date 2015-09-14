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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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

	QtResultTypeRowMapper queryResultTypeMapper = new QtResultTypeRowMapper();

	private DataSourceLookup dataSourceLookup = null;

	public QueryResultTypeSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
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
	@SuppressWarnings("unchecked")
	public List<QtQueryResultType> getQueryResultTypeByName(String resultName) {

		String sql = "select * from " + getDbSchemaName()
				+ "qt_query_result_type where name = ?";
		List<QtQueryResultType> queryResultType = jdbcTemplate.query(sql,
				new Object[] { resultName.toUpperCase() },
				queryResultTypeMapper);
		return queryResultType;
	}

	@SuppressWarnings("unchecked")
	public List<QtQueryResultType> getAllQueryResultType() {
		String sql = "select * from " + getDbSchemaName()
				+ "qt_query_result_type order by result_type_id";
		List<QtQueryResultType> queryResultTypeList = jdbcTemplate.query(sql,
				queryResultTypeMapper);
		return queryResultTypeList;
	}

	private static class QtResultTypeRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryResultType queryResultType = new QtQueryResultType();
			queryResultType.setResultTypeId(rs.getInt("RESULT_TYPE_ID"));
			queryResultType.setName(rs.getString("NAME"));
			queryResultType.setDescription(rs.getString("DESCRIPTION"));
			queryResultType.setDisplayType(rs.getString("DISPLAY_TYPE_ID"));
			queryResultType.setVisualAttributeType(rs
					.getString("VISUAL_ATTRIBUTE_TYPE_ID"));
			return queryResultType;
		}
	}

}
