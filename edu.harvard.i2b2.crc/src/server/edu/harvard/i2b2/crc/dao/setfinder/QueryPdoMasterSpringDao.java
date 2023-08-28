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

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.SQLException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;

/**
 * Class to manager persistance operation of QtQueryMaster $Id:
 * QueryMasterSpringDao.java,v 1.3 2008/04/08 19:36:52 rk903 Exp $
 * 
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryPdoMasterSpringDao extends CRCDAO implements
		IQueryPdoMasterDao {

	JdbcTemplate jdbcTemplate = null;
	SaveQueryMaster saveQueryMaster = null;

	private DataSourceLookup dataSourceLookup = null;

	public QueryPdoMasterSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Function to create query master By default sets delete flag to false
	 * 
	 * @param queryMaster
	 * @return query master id
	 */
	@Override
	public String createPdoQueryMaster(QtQueryMaster queryMaster,
			String i2b2RequestXml) {

		saveQueryMaster = new SaveQueryMaster(getDataSource(),
				getDbSchemaName(), dataSourceLookup);
		saveQueryMaster.save(queryMaster, i2b2RequestXml);
		return queryMaster.getQueryMasterId();
	}

	private static class SaveQueryMaster extends SqlUpdate {

		private String INSERT_ORACLE = "";
		private String INSERT_SQLSERVER = "";
		private String SEQUENCE_ORACLE = "";
		private String SEQUENCE_POSTGRESQL = "";
		private String INSERT_POSTGRESQL = "";
		private String SEQUENCE_SNOWFLAKE = "";
		private String INSERT_SNOWFLAKE = "";

		private DataSourceLookup dataSourceLookup = null;

		public SaveQueryMaster(DataSource dataSource, String dbSchemaName,
				DataSourceLookup dataSourceLookup) {
			super();
			this.setDataSource(dataSource);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				this.setReturnGeneratedKeys(true);
				INSERT_ORACLE = "INSERT INTO "
						+ dbSchemaName
						+ "QT_PDO_QUERY_MASTER "
						+ "(QUERY_MASTER_ID,  USER_ID, GROUP_ID,CREATE_DATE,REQUEST_XML,I2B2_REQUEST_XML) "
						+ "VALUES (?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);
				SEQUENCE_ORACLE = "select " + dbSchemaName
						+ "QT_SQ_PQM_QMID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) ) {
				INSERT_SQLSERVER = "INSERT INTO "
						+ dbSchemaName
						+ "QT_PDO_QUERY_MASTER "
						+ "( USER_ID, GROUP_ID,CREATE_DATE,REQUEST_XML,I2B2_REQUEST_XML) "
						+ "VALUES (?,?,?,?,?)";
				this.setSql(INSERT_SQLSERVER);
			} else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				this.setReturnGeneratedKeys(true);
				INSERT_POSTGRESQL = "INSERT INTO "
						+ dbSchemaName
						+ "QT_PDO_QUERY_MASTER "
						+ "(QUERY_MASTER_ID,  USER_ID, GROUP_ID,CREATE_DATE,REQUEST_XML,I2B2_REQUEST_XML) "
						+ "VALUES (?,?,?,?,?,?)";
				setSql(INSERT_POSTGRESQL);
				SEQUENCE_POSTGRESQL = "select "// + dbSchemaName
						+ "nextval('qt_pdo_query_master_query_master_id_seq') ";
				declareParameter(new SqlParameter(Types.INTEGER));
			} else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SNOWFLAKE)) {
				this.setReturnGeneratedKeys(true);
				INSERT_SNOWFLAKE = "INSERT INTO "
						+ dbSchemaName
						+ "QT_PDO_QUERY_MASTER "
						+ "(QUERY_MASTER_ID,  USER_ID, GROUP_ID,CREATE_DATE,REQUEST_XML,I2B2_REQUEST_XML) "
						+ "VALUES (?,?,?,?,?,?)";
				setSql(INSERT_SNOWFLAKE);
				SEQUENCE_SNOWFLAKE = "select "
						+ dbSchemaName
						+ "SEQ_QT_PDO_QUERY_MASTER.nextval";
				declareParameter(new SqlParameter(Types.INTEGER));
			}
			this.dataSourceLookup = dataSourceLookup;

			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();

		}

		public void save(QtQueryMaster queryMaster, String i2b2RequestXml) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int masterQueryId = 0;
			Object[] object = null;
			int queryMasterIdentityId = 0;
			
			Pattern p = Pattern.compile("<password.+</password>");
			Matcher m = p.matcher(queryMaster.getRequestXml());
			String getRequestXmlNoPass = m.replaceAll("<password>*********</password>");

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				object = new Object[] { queryMaster.getUserId(),
						queryMaster.getGroupId(), queryMaster.getCreateDate(),
						getRequestXmlNoPass, i2b2RequestXml };
				update(object);
				queryMasterIdentityId = jdbc.queryForObject("SELECT @@IDENTITY", Integer.class);

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				queryMasterIdentityId = jdbc.queryForObject(SEQUENCE_ORACLE, Integer.class);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getUserId(), queryMaster.getGroupId(),
						queryMaster.getCreateDate(),
						getRequestXmlNoPass, i2b2RequestXml };
				update(object);

			} else  if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				queryMasterIdentityId = jdbc.queryForObject(SEQUENCE_POSTGRESQL, Integer.class);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getUserId(), queryMaster.getGroupId(),
						queryMaster.getCreateDate(),
						getRequestXmlNoPass, i2b2RequestXml };
				update(object);

			} else  if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SNOWFLAKE)) {
				try {
					queryMasterIdentityId = jdbc.queryForObject(SEQUENCE_SNOWFLAKE, Integer.class);
					Connection manualConnection = ServiceLocator.getInstance()
							.getAppServerDataSource(dataSourceLookup.getDataSource())
							.getConnection();
					String sql = getSql();
					PreparedStatement pstmt = manualConnection.prepareStatement(sql);
					pstmt.setInt(1, queryMasterIdentityId);
					pstmt.setString(2, queryMaster.getUserId());
					pstmt.setString(3, queryMaster.getGroupId());

					pstmt.setString(5, getRequestXmlNoPass);
					pstmt.setString(6, i2b2RequestXml);

					if (queryMaster.getCreateDate() == null) {
						pstmt.setNull(4, Types.TIMESTAMP);
					} else {
						Timestamp tsCreate = new Timestamp(queryMaster.getCreateDate().getTime());
						pstmt.setTimestamp(4, tsCreate);
					}
					pstmt.executeUpdate();
				} catch (I2B2Exception ex1) {
					//TODO:
					System.out.println(" v- I2B2Exception: " + ex1.getMessage());

				} catch (SQLException ex2) {
					//TODO:
					System.out.println("QueryPdoMasterSpringDao- SQLException: " + ex2.getMessage());
				}
			}

			queryMaster.setQueryMasterId(String.valueOf(queryMasterIdentityId));

		}
	}

}
