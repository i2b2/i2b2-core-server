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
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.DateConstrainHandler;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MatchStrType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserRequestType;
import edu.harvard.i2b2.crc.util.CacheUtil;

/**
 * Class to manager persistance operation of QtQueryMaster $Id:
 * QueryMasterSpringDao.java,v 1.3 2008/04/08 19:36:52 rk903 Exp $
 * 
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryMasterSpringDao extends CRCDAO implements IQueryMasterDao {

	JdbcTemplate jdbcTemplate = null;
	SaveQueryMaster saveQueryMaster = null;
	QtQueryMasterRowMapper queryMasterMapper = new QtQueryMasterRowMapper();

	public final String DELETE_YES_FLAG = "Y";
	public final String DELETE_NO_FLAG = "N";

	private DataSourceLookup dataSourceLookup = null;

	public QueryMasterSpringDao(DataSource dataSource,
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
	public String createQueryMaster(QtQueryMaster queryMaster,
			String i2b2RequestXml, String pmXml) {
		queryMaster.setDeleteFlag(DELETE_NO_FLAG);
		saveQueryMaster = new SaveQueryMaster(getDataSource(),
				getDbSchemaName(), dataSourceLookup);
		saveQueryMaster.save(queryMaster, i2b2RequestXml, pmXml);
		return queryMaster.getQueryMasterId();
	}

	/**
	 * Write query sql for the master id
	 * 
	 * @param masterId
	 * @param generatedSql
	 */
	public void updateQueryAfterRun(String masterId, String generatedSql, String masterType) {
		String sql = "UPDATE "
				+ getDbSchemaName()
				+ "QT_QUERY_MASTER set  GENERATED_SQL = ?, MASTER_TYPE_CD = ? where query_master_id = ?";
		jdbcTemplate.update(sql, new Object[] { generatedSql, masterType, Integer.parseInt(masterId) });
		// jdbcTemplate.update(sql);
	}

	/**
	 * Returns list of query master by find search
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 * @throws I2B2Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByNameInfo(SecurityType userRequestType, FindByChildType findChildType) throws I2B2DAOException {



		String rolePath = dataSourceLookup.getDomainId() 
				+ dataSourceLookup.getProjectPath() 
				+ userRequestType.getUsername();

		//List<String> roles = (List<String>) cache.getRoot().get(rolePath);
		log.debug("Roles from get " + rolePath);
		List<String> roles = (List<String>) CacheUtil.get(rolePath);

		String sql = "select ";

		int fetchSize = findChildType.getMax();
		List<QtQueryMaster> queryMasterList = null;

		String str = "";
		MatchStrType matchStr = findChildType.getMatchStr();
		if (matchStr.getStrategy().toLowerCase().equals("right") || matchStr.getStrategy().toLowerCase().equals("contains"))
			str = "%";
		str += matchStr.getValue();
		if (matchStr.getStrategy().toLowerCase().equals("left") || matchStr.getStrategy().toLowerCase().equals("contains"))
			str += "%";


		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
			sql += " top " + fetchSize;
		}
		if ((findChildType.getCategory().toLowerCase().equals("top")) ||
				(findChildType.getCategory().toLowerCase().equals("@"))) {
			sql += " query_master_id,name,user_id,group_id,create_date,delete_date,null as request_xml,delete_flag,generated_sql, null as i2b2_request_xml, master_type_cd, null as plugin_id from "
					+ getDbSchemaName()
					+ "qt_query_master where ";
			if (roles != null && !roles.contains("MANAGER"))
					sql += "  user_id = ? and ";
			sql += "  LOWER(name) like ? and delete_flag = ? "; //and master_type_cd is NULL";
			if (findChildType.getCreateDate() != null)
			{
				DateConstrainHandler dateConstrainHandler = new DateConstrainHandler(
						dataSourceLookup);

				if (findChildType.isAscending())
					sql +=" and " + dateConstrainHandler
					.constructDateConstrainClause("create_date",
							"create_date", null,
							null, findChildType.getCreateDate() ,
							null);
				else
					sql += " and " +  dateConstrainHandler
					.constructDateConstrainClause("create_date",
							"create_date", null,
							null,null ,
							findChildType.getCreateDate());
				sql += " order by create_date  ";

			} else {
			sql += " order by create_date  ";
			}
		//	if (findChildType.isAscending())
				sql += "desc";
		//	else 
		//		sql += "asc";
		} 
		if ((findChildType.getCategory().equals("@")))
		{
			if (fetchSize > 0) {
				if ( dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.ORACLE)) 
					sql = "select * from ( " + sql + " ) where " + "  rownum <= "
							+ fetchSize;
				else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) 
					sql += " limit " + fetchSize;
			}
			
			if (roles != null && roles.contains("MANAGER"))
				queryMasterList = jdbcTemplate.query(sql,
						new Object[] { str.toLowerCase(), DELETE_NO_FLAG }, queryMasterMapper);
			else
				queryMasterList = jdbcTemplate.query(sql,
						new Object[] { userRequestType.getUsername(),  str.toLowerCase(), DELETE_NO_FLAG }, queryMasterMapper);




			sql = "select ";
			if (fetchSize > 0
					&& dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.SQLSERVER)) {
				sql += " top " + fetchSize;
			}
		}
		if ((findChildType.getCategory().toLowerCase().equals("results")) ||
				(findChildType.getCategory().equals("@"))) {
			sql += " qm.query_master_id,qm.name,qm.user_id,qm.group_id,qm.create_date,qm.delete_date,null as request_xml,qm.delete_flag,qm.generated_sql, null as i2b2_request_xml, qm.master_type_cd, null as plugin_id  from "
					+ getDbSchemaName()
					+ "qt_query_master qm, "
					+ getDbSchemaName()
					+ "qt_query_instance qi, "
					+ getDbSchemaName()
					+ "qt_query_result_instance qri where "
					+ "qm.QUERY_MASTER_ID = qi.QUERY_MASTER_ID and "
					+ "qi.QUERY_INSTANCE_ID = qri.QUERY_INSTANCE_ID and ";
					if (roles != null && !roles.contains("MANAGER"))
						sql += "  qm.user_id = ? and";
					sql+= " LOWER(qri.DESCRIPTION) like ? and qm.delete_flag = ? "; //and qm.master_type_cd is NULL";
			if (findChildType.getCreateDate() != null)
			{
				DateConstrainHandler dateConstrainHandler = new DateConstrainHandler(
						dataSourceLookup);

				if (!findChildType.isAscending())
					sql += " and " + dateConstrainHandler
					.constructDateConstrainClause("create_date",
							"create_date", null,
							null, findChildType.getCreateDate() ,
							null);
				else
					sql += " and " +  dateConstrainHandler
					.constructDateConstrainClause("create_date",
							"create_date", null,
							null,null ,
							findChildType.getCreateDate());
				sql += " order by qm.create_date  ";

			}
			else {
			sql += " order by qm.create_date  ";
			}
		//	if (findChildType.isAscending())
				sql += "desc";
		//	else 
		//		sql += "asc";
		}  
		if ((findChildType.getCategory().toLowerCase().equals("@")))
		{

			if (fetchSize > 0) {
				if ( dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.ORACLE)) 
					sql = "select * from ( " + sql + " ) where " + "  rownum <= "
							+ fetchSize;
				else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) 
					sql += " limit " + fetchSize;

			}
			
			if (roles != null && roles.contains("MANAGER"))
				queryMasterList.addAll(jdbcTemplate.query(sql,
					new Object[] { str.toLowerCase(), DELETE_NO_FLAG }, queryMasterMapper));
			else
				queryMasterList.addAll(jdbcTemplate.query(sql,
						new Object[] {  userRequestType.getUsername(),   str.toLowerCase(), DELETE_NO_FLAG }, queryMasterMapper));

			sql = " select ";
			if (fetchSize > 0
					&& dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.SQLSERVER)) {
				sql += " distinct top " + fetchSize;
			} else
			{
				sql += " distinct ";
			}
		}
		if ((findChildType.getCategory().toLowerCase().equals("pdo")) ||
				(findChildType.getCategory().toLowerCase().equals("@"))) {
			sql += " distinct  qm.query_master_id,qm.name,qm.user_id,qm.group_id,qm.create_date,qm.delete_date,null as request_xml,qm.delete_flag,null as generated_sql, null as i2b2_request_xml, qm.master_type_cd, null as plugin_id  from "
					+ getDbSchemaName()
					+ "qt_query_master qm, "
					+ getDbSchemaName()
					+ "qt_query_instance qi, "
					+ getDbSchemaName()
					+ "QT_PATIENT_SET_COLLECTION qp, "
					+ getDbSchemaName()
					+ "qt_query_result_instance qri where "
					+ "qm.QUERY_MASTER_ID = qi.QUERY_MASTER_ID and "
					+ "qi.QUERY_INSTANCE_ID = qri.QUERY_INSTANCE_ID and "
					+ "qri.RESULT_INSTANCE_ID = qp.RESULT_INSTANCE_ID and ";
			if (roles != null && !roles.contains("MANAGER"))
					sql += "  qm.user_id = ? and ";
			 if ( dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) 
				 sql += " CAST(qp.patient_num AS TEXT) like ? and qm.delete_flag = ? "; 
			 else 
				 sql += " qp.patient_num like ? and qm.delete_flag = ? "; 

			if (findChildType.getCreateDate() != null)
			{
				DateConstrainHandler dateConstrainHandler = new DateConstrainHandler(
						dataSourceLookup);

				if (!findChildType.isAscending())
					sql += " and " + dateConstrainHandler
					.constructDateConstrainClause("create_date",
							"create_date", null,
							null, findChildType.getCreateDate() ,
							null);
				else
					sql += " and " +  dateConstrainHandler
					.constructDateConstrainClause("create_date",
							"create_date", null,
							null,null ,
							findChildType.getCreateDate());

				sql += " order by qm.create_date  ";
			}
			else {
			sql += " order by qm.create_date  ";
			}
		//	if (findChildType.isAscending())
				sql += "desc";
		//	else 
		//		sql += "asc";
		} 

		if (fetchSize > 0) {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) 
				sql = "select * from ( " + sql + " ) where " + "  rownum <= "
						+ fetchSize;
			else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) 
				sql += " limit " + fetchSize;

		}



		//		if (findChildType.getCategory().toLowerCase().equals("@"))
		//			queryMasterList = jdbcTemplate.query(sql,
		//					new Object[] { userRequestType.getUsername(), str, DELETE_NO_FLAG, userRequestType.getUsername(), str, DELETE_NO_FLAG, userRequestType.getUsername(), str, DELETE_NO_FLAG }, queryMasterMapper);
		//		else
		Object[] args = null;
		String userid = userRequestType.getUsername();
		if (findChildType.getUserId() != null && roles != null && roles.contains("MANAGER"))
			userid = findChildType.getUserId();
	//	else if (findChildType.getUserId() != null)
	//		throw new I2B2DAOException ("Permisison denied");
		//		if (findChildType.getCreateDate() != null)
		//			 args = new Object[] { userid, str.toLowerCase(), DELETE_NO_FLAG,findChildType.getCreateDate() };
		//		else
		if (roles != null && roles.contains("MANAGER"))
			args = new Object[] {  str.toLowerCase(), DELETE_NO_FLAG };
		else
			args = new Object[] { userid, str.toLowerCase(), DELETE_NO_FLAG };

		if (!findChildType.getCategory().toLowerCase().equals("@"))
		{
			queryMasterList = jdbcTemplate.query(sql,
					args, queryMasterMapper);
		} else { 
			queryMasterList.addAll(jdbcTemplate.query(sql,
					args, queryMasterMapper));
		}
		return queryMasterList;
	}
	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByUserId(String userId,
			int fetchSize) {

		String sql = "select ";

		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
			sql += " top " + fetchSize;
		}
		sql += " query_master_id,name,user_id,group_id,create_date,delete_date,null as request_xml,delete_flag,generated_sql, null as i2b2_request_xml,  master_type_cd, null as plugin_id from "
				+ getDbSchemaName()
				+ "qt_query_master "
				+ " where user_id = ? and delete_flag = ? ";// and master_type_cd is NULL";

		sql += " order by create_date desc  ";

		if (fetchSize > 0) {
			if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) 
				sql = "select * from ( " + sql + " ) where " + "  rownum <= "
						+ fetchSize;
			else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) 
				sql += " limit " + fetchSize;

		}

		List<QtQueryMaster> queryMasterList = jdbcTemplate.query(sql,
				new Object[] { userId, DELETE_NO_FLAG }, queryMasterMapper);

		return queryMasterList;
	}

	/**
	 * Returns list of query master by group id
	 * 
	 * @param groupId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByGroupId(String groupId,
			int fetchSize) {

		String sql = "select ";
		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
			sql += " top " + fetchSize;
		}
		sql += " query_master_id,name,user_id,group_id,create_date,delete_date,null as request_xml,delete_flag,generated_sql,null as i2b2_request_xml, master_type_cd, null as plugin_id from "
				+ getDbSchemaName()
				+ "qt_query_master "
				+ " where group_id = ? and delete_flag = ? "; //and master_type_cd is NULL";

		sql += " order by create_date desc  ";

		if (fetchSize > 0) {
			if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) 
				sql = " select * from (  " + sql + " ) where  rownum <= "
						+ fetchSize;
			else if ( dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) 
				sql += " limit " + fetchSize;

		}
		List<QtQueryMaster> queryMasterList = jdbcTemplate.query(sql,
				new Object[] { groupId, DELETE_NO_FLAG }, queryMasterMapper);
		return queryMasterList;
	}

	/**
	 * Find Query master by id
	 * 
	 * @param masterId
	 * @return QtQueryMaster
	 */
	public QtQueryMaster getQueryDefinition(String masterId) {
		String sql = "select * from " + getDbSchemaName() + "qt_query_master "
				+ " where query_master_id = ? and delete_flag = ? ";
		QtQueryMaster queryMaster = null;
		try {
			queryMaster = (QtQueryMaster) jdbcTemplate.queryForObject(sql,
					new Object[] { Integer.parseInt(masterId), DELETE_NO_FLAG },
					queryMasterMapper);
		} catch (IncorrectResultSizeDataAccessException inResultEx) {
			log.error("Query doesn't exists for masterId :[" + masterId + "]");
		} catch (DataAccessException e) {
			log.error("Could not execute query master for masterId :["
					+ masterId + "]");
		}
		return queryMaster;
	}


	public List<QtQueryMaster> getQueryByName(String queryName) {
		String sql = "select * from " + getDbSchemaName() + "qt_query_master "
				+ " where name = ? and delete_flag = ? ";
		List<QtQueryMaster> queryMasterList = jdbcTemplate.query(sql,
				new Object[] { queryName, DELETE_NO_FLAG }, queryMasterMapper);
		return queryMasterList;
	}
	/**
	 * Function to rename query master
	 * 
	 * @param masterId
	 * @param queryNewName
	 * @throws I2B2DAOException
	 */
	public void renameQuery(String masterId, String queryNewName)
			throws I2B2DAOException {
		log.debug("Rename  masterId=" + masterId + " new query name"
				+ queryNewName);

		String sql = "update "
				+ getDbSchemaName()
				+ "qt_query_master set name = ? where query_master_id = ? and delete_flag = ?";
		int updatedRow = jdbcTemplate.update(sql, new Object[] { queryNewName,
				Integer.parseInt(masterId), DELETE_NO_FLAG });
		if (updatedRow < 1) {
			throw new I2B2DAOException("Query with master id " + masterId
					+ " not found");
		}

	}

	/**
	 * Function to delete query using user and master id This function will not
	 * delete permanently, it will set delete flag field in query master, query
	 * instance and result instance to true
	 * 
	 * @param masterId
	 * @throws I2B2DAOException
	 */
	@SuppressWarnings("unchecked")
	public void deleteQuery(String masterId) throws I2B2DAOException {
		if (masterId==null){
			log.info("Null master id sent to deleteQuery method");
		}
		else{
			log.info("Delete query for master id=" + masterId);
			String resultInstanceSql = "update " + getDbSchemaName()
			+ "qt_query_result_instance set "
			+ " delete_flag=? where query_instance_id in (select "
			+ "query_instance_id from " + getDbSchemaName()
			+ "qt_query_instance where query_master_id=?) ";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				resultInstanceSql = " update " + getDbSchemaName()
				+ "qt_query_result_instance set  delete_flag=? " + " from "
				+ getDbSchemaName()
				+ "qt_query_result_instance qri inner join "
				+ getDbSchemaName() + "qt_query_instance qi "
				+ " on  qri.query_instance_id = qi.query_instance_id "
				+ " where qi.query_master_id = ?";
			}
			String queryInstanceSql = "update "
					+ getDbSchemaName()
					+ "qt_query_instance set delete_flag = ? where query_master_id = ?  and delete_flag = ?";
			String queryMasterSql = "update "
					+ getDbSchemaName()
					+ "qt_query_master set delete_flag =?,delete_date=? where query_master_id = ? and delete_flag = ?";
			Date deleteDate = new Date(System.currentTimeMillis());
			int queryMasterCount = jdbcTemplate.update(queryMasterSql,
					new Object[] { DELETE_YES_FLAG, deleteDate, Integer.parseInt(masterId),
							DELETE_NO_FLAG });
			if (queryMasterCount < 1 && !dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				throw new I2B2DAOException("Query not found with masterid =["
						+ masterId + "]");
			}

			int queryInstanceCount = jdbcTemplate.update(queryInstanceSql,
					new Object[] { DELETE_YES_FLAG, Integer.parseInt(masterId), DELETE_NO_FLAG });
			log.debug("Total no. of query instance deleted" + queryInstanceCount);
			int queryResultInstanceCount = jdbcTemplate.update(resultInstanceSql,
					new Object[] { DELETE_YES_FLAG, Integer.parseInt(masterId) });
			log.debug("Total no. of query result deleted "
					+ queryResultInstanceCount);
		}
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
						+ "QT_QUERY_MASTER "
						+ "(QUERY_MASTER_ID, NAME, USER_ID, GROUP_ID,MASTER_TYPE_CD,PLUGIN_ID,CREATE_DATE,DELETE_DATE,REQUEST_XML,DELETE_FLAG,GENERATED_SQL,I2B2_REQUEST_XML, PM_XML) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);
				SEQUENCE_ORACLE = "select " + dbSchemaName
						+ "QT_SQ_QM_QMID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				INSERT_SQLSERVER = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_MASTER "
						+ "( NAME, USER_ID, GROUP_ID,MASTER_TYPE_CD,PLUGIN_ID,CREATE_DATE,DELETE_DATE,REQUEST_XML,DELETE_FLAG,GENERATED_SQL,I2B2_REQUEST_XML,PM_XML) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
				this.setSql(INSERT_SQLSERVER);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				this.setReturnGeneratedKeys(true);
				INSERT_POSTGRESQL = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_MASTER "
						+ "(QUERY_MASTER_ID, NAME, USER_ID, GROUP_ID,MASTER_TYPE_CD,PLUGIN_ID,CREATE_DATE,DELETE_DATE,REQUEST_XML,DELETE_FLAG,GENERATED_SQL,I2B2_REQUEST_XML, PM_XML) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_POSTGRESQL);
				SEQUENCE_POSTGRESQL = "select " //+ dbSchemaName
						+ " nextval('qt_query_master_query_master_id_seq') ";
				declareParameter(new SqlParameter(Types.INTEGER));
			}
			this.dataSourceLookup = dataSourceLookup;

			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();

		}

		public void save(QtQueryMaster queryMaster, String i2b2RequestXml, String pmXml) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int masterQueryId = 0;
			Object[] object = null;
			int queryMasterIdentityId = 0;

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				object = new Object[] { queryMaster.getName(),
						queryMaster.getUserId(), queryMaster.getGroupId(),
						queryMaster.getMasterTypeCd(),
						queryMaster.getPluginId(), queryMaster.getCreateDate(),
						queryMaster.getDeleteDate(),
						queryMaster.getRequestXml(),
						queryMaster.getDeleteFlag(),
						queryMaster.getGeneratedSql(), i2b2RequestXml, pmXml };
				update(object);
				queryMasterIdentityId = jdbc.queryForInt("SELECT @@IDENTITY");

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				queryMasterIdentityId = jdbc.queryForInt(SEQUENCE_ORACLE);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getName(), queryMaster.getUserId(),
						queryMaster.getGroupId(),
						queryMaster.getMasterTypeCd(),
						queryMaster.getPluginId(), queryMaster.getCreateDate(),
						queryMaster.getDeleteDate(),
						queryMaster.getRequestXml(),
						queryMaster.getDeleteFlag(),
						queryMaster.getGeneratedSql(), i2b2RequestXml, pmXml };
				update(object);

			}  else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				queryMasterIdentityId = jdbc.queryForInt(SEQUENCE_POSTGRESQL);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getName(), queryMaster.getUserId(),
						queryMaster.getGroupId(),
						queryMaster.getMasterTypeCd(),
						queryMaster.getPluginId(), queryMaster.getCreateDate(),
						queryMaster.getDeleteDate(),
						queryMaster.getRequestXml(),
						queryMaster.getDeleteFlag(),
						queryMaster.getGeneratedSql(), i2b2RequestXml, pmXml };
				update(object);
			}

			queryMaster.setQueryMasterId(String.valueOf(queryMasterIdentityId));

		}
	}

	private static class QtQueryMasterRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryMaster queryMaster = new QtQueryMaster();
			queryMaster.setQueryMasterId(String.valueOf(rs.getInt("QUERY_MASTER_ID")));
			queryMaster.setName(rs.getString("NAME"));
			queryMaster.setUserId(rs.getString("USER_ID"));
			queryMaster.setGroupId(rs.getString("GROUP_ID"));
			queryMaster.setMasterTypeCd(rs.getString("MASTER_TYPE_CD"));
			queryMaster.setPluginId(rs.getString("PLUGIN_ID"));
			queryMaster.setCreateDate(rs.getTimestamp("CREATE_DATE"));
			queryMaster.setDeleteDate(rs.getTimestamp("DELETE_DATE"));
			queryMaster.setRequestXml(rs.getString("REQUEST_XML"));
			queryMaster.setDeleteFlag(rs.getString("DELETE_FLAG"));
			queryMaster.setGeneratedSql(rs.getString("GENERATED_SQL"));
			queryMaster.setI2b2RequestXml(rs.getString("I2B2_REQUEST_XML"));
			return queryMaster;
		}
	}


}
