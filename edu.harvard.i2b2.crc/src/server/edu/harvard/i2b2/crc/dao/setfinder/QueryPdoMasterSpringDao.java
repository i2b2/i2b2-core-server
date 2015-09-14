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

import java.sql.Types;

import javax.sql.DataSource;

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

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				object = new Object[] { queryMaster.getUserId(),
						queryMaster.getGroupId(), queryMaster.getCreateDate(),
						queryMaster.getRequestXml(), i2b2RequestXml };
				update(object);
				queryMasterIdentityId = jdbc.queryForInt("SELECT @@IDENTITY");

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				queryMasterIdentityId = jdbc.queryForInt(SEQUENCE_ORACLE);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getUserId(), queryMaster.getGroupId(),
						queryMaster.getCreateDate(),
						queryMaster.getRequestXml(), i2b2RequestXml };
				update(object);

			} else  if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				queryMasterIdentityId = jdbc.queryForInt(SEQUENCE_POSTGRESQL);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getUserId(), queryMaster.getGroupId(),
						queryMaster.getCreateDate(),
						queryMaster.getRequestXml(), i2b2RequestXml };
				update(object);

			}

			queryMaster.setQueryMasterId(String.valueOf(queryMasterIdentityId));

		}
	}

}
