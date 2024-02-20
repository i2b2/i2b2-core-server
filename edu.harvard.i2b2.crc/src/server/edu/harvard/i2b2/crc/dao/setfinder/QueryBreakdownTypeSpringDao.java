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

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.pdo.BlobType;

/**
 * Class to manager operation of QtBreakdownPath $Id:
 * QueryBreakdownTypeSpringDao.java,v 1.3 2008/05/07 21:39:08 rk903 Exp $
 * 
 * @author rkuttan
 * 
 */
public class QueryBreakdownTypeSpringDao extends CRCDAO implements
IQueryBreakdownTypeDao {

	static JdbcTemplate jdbcTemplate = null;

	QtBreakdownTypeRowMapper queryBreakdownTypeMapper = new QtBreakdownTypeRowMapper();

	static DataSourceLookup dataSourceLookup = null;

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
	@Override
	@SuppressWarnings("unchecked")
	public QtQueryBreakdownType getBreakdownTypeByName(String name) {

		String sql = "select  b.VALUE,   b.CREATE_DATE  ,   b.UPDATE_DATE   ,  b.USER_ID , a.name, a.user_role_cd, a.classname from " + getDbSchemaName()
		+ "qt_query_result_type a left join " + getDbSchemaName()
		+ "qt_breakdown_path b on  a.name = b.name where a.name = ? ";
		QtQueryBreakdownType queryStatusType  = (QtQueryBreakdownType) jdbcTemplate
				.queryForObject(sql, new Object[] { name },
						queryBreakdownTypeMapper);

		return queryStatusType;
	}

	private static class QtBreakdownTypeRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryBreakdownType queryBreakdownType = new QtQueryBreakdownType();
			queryBreakdownType.setCreateDate(rs.getDate("CREATE_DATE"));
			queryBreakdownType.setCreateDate(rs.getDate("UPDATE_DATE"));
			queryBreakdownType.setName(rs.getString("NAME"));
			queryBreakdownType.setValue(rs.getString("VALUE"));
			/*
			String dsLookup = dataSourceLookup.getServerType();
					//jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
			if (dsLookup.equalsIgnoreCase("POSTGRESQL"))// || dsLookup.equalsIgnoreCase("ORACLE"))
			{
				String clob = rs.getString("VALUE_BLOB");
				if (clob !=null)
				{
					BlobType blobType = new BlobType();
					blobType.getContent().add(clob);
					queryBreakdownType.setValue(blobType);

				}

			} else {
				String clob = rs.getString("VALUE_BLOB");
				Clob observationClob = rs.getClob("VALUE_BLOB");

				if (observationClob != null) {
					BlobType blobType = new BlobType();
					try {
						blobType.getContent().add(
								JDBCUtil.getClobStringWithLinebreak(observationClob));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					queryBreakdownType.setValue(blobType);
				}
			}
			*/
			
			queryBreakdownType.setUserId(rs.getString("USER_ID"));
			queryBreakdownType.setUserRoleCd(rs.getString("USER_ROLE_CD"));
			queryBreakdownType.setClassname(rs.getString("CLASSNAME"));
			return queryBreakdownType;
		}
	}

}
