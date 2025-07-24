/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtPriviledge;
 
public class PriviledgeSpringDao extends CRCDAO implements IPriviledgeDao {

	JdbcTemplate jdbcTemplate = null;

	QtPriviledgeRowMapper priviledgeMapper = new QtPriviledgeRowMapper();

	private DataSourceLookup dataSourceLookup = null;

	public PriviledgeSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	@Override
	public QtPriviledge getPriviledgeByProtectionLabel(String protectionLabel)
			throws I2B2DAOException {
		String lookupSql = "select * from "
				+ getDbSchemaName()
				+ "qt_privilege where protection_label_cd = ? and plugin_id is NULL";
		QtPriviledge priviledgeRow = (QtPriviledge) jdbcTemplate
				.queryForObject(lookupSql, new Object[] { protectionLabel },
						priviledgeMapper);
		return priviledgeRow;

	}

	@Override
	public List<QtPriviledge> getPriviledgeByPluginId(String pluginId)
			throws I2B2DAOException {
		String lookupSql = "select * from "
				+ getDbSchemaName()
				+ "qt_privilege where plugin_id = ? and protection_label_cd is NULL";
		List<QtPriviledge> priviledgeRow = jdbcTemplate.query(lookupSql,
				new Object[] { pluginId }, priviledgeMapper);
		return priviledgeRow;
	}

	private static class QtPriviledgeRowMapper implements RowMapper {

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtPriviledge priviledge = new QtPriviledge();
			priviledge
					.setProtectionLabelCd(rs.getString("PROTECTION_LABEL_CD"));
			priviledge.setDataProtCd(rs.getString("DATAPROT_CD"));
			priviledge.setHivemgmtCd(rs.getString("HIVEMGMT_CD"));
			priviledge.setPluginId(rs.getString("PLUGIN_ID"));
			return priviledge;
		}
	}

}
