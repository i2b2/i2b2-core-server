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

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;

/**
 * Class to manager operation of QtBreakdownPath $Id:
 * QueryBreakdownTypeSpringDao.java,v 1.3 2008/05/07 21:39:08 rk903 Exp $
 * 
 * @author rkuttan
 * 
 */
public class QueryBreakdownTypeSpringDao extends CRCDAO implements
		IQueryBreakdownTypeDao {

	JdbcTemplate jdbcTemplate = null;

	QtBreakdownTypeRowMapper queryBreakdownTypeMapper = new QtBreakdownTypeRowMapper();

	private DataSourceLookup dataSourceLookup = null;

	public QueryBreakdownTypeSpringDao(DataSource dataSource,
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
	public QtQueryBreakdownType getBreakdownTypeByName(String name) {

		String sql = "select * from " + getDbSchemaName()
				+ "qt_breakdown_path where name = ?";
		QtQueryBreakdownType queryStatusType = (QtQueryBreakdownType) jdbcTemplate
				.queryForObject(sql, new Object[] { name },
						queryBreakdownTypeMapper);
		return queryStatusType;
	}

	private static class QtBreakdownTypeRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryBreakdownType queryBreakdownType = new QtQueryBreakdownType();
			queryBreakdownType.setCreateDate(rs.getDate("CREATE_DATE"));
			queryBreakdownType.setCreateDate(rs.getDate("UPDATE_DATE"));
			queryBreakdownType.setName(rs.getString("NAME"));
			queryBreakdownType.setValue(rs.getString("VALUE"));
			queryBreakdownType.setUserId(rs.getString("USER_ID"));

			return queryBreakdownType;
		}
	}

}
