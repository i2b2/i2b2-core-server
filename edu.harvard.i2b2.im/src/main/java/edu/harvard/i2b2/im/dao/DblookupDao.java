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
 * 		Wayne Chan
 */
package edu.harvard.i2b2.im.dao;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.wdo.DblookupType;
import edu.harvard.i2b2.im.datavo.wdo.DeleteDblookupType;
import edu.harvard.i2b2.im.datavo.wdo.SetDblookupType;
import edu.harvard.i2b2.im.util.IMUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class DblookupDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(DblookupDao.class);
	private static DataSource ds = null;
	private static JdbcTemplate jt;
	private static String dbluTable;
	private static String key = " LOWER(c_domain_id)=LOWER(?) AND (LOWER(c_owner_id)=LOWER(?) OR c_owner_id='@') ";
	private static String keyOrder = " LOWER(c_domain_id)=LOWER(?) AND (LOWER(c_owner_id)=LOWER(?) OR c_owner_id='@') ORDER BY c_project_path ";
	private String domainId = null;
	private String userId = null;

	public DblookupDao() {		
		initDblookupDao();
	} 

	public DblookupDao(MessageHeaderType reqMsgHdr) throws I2B2Exception, JAXBUtilException {
		domainId = reqMsgHdr.getSecurity().getDomain();
		userId = reqMsgHdr.getSecurity().getUsername();
		initDblookupDao();
	}

	private void initDblookupDao() {		
		try {
			ds = IMUtil.getInstance().getDataSource("java:/IMBootStrapDS");
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		jt = new JdbcTemplate(ds);
		String dataSchema = "";
		try {
			dataSchema = IMUtil.getInstance().getIMDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}
		dbluTable = dataSchema + "im_db_lookup ";
		log.info("IM_DB_LOOKUP = " + dbluTable);
	} 

	public String slashEnd(String s) {
		StringBuffer sb = new StringBuffer(s);
		if (!s.endsWith("/")) {
			sb.append('/');
		}
		log.info(sb.toString());
		return sb.toString();
	}



	public List<DblookupType> findDblookups() throws DataAccessException, I2B2DAOException{	
		String sql = "SELECT * FROM " +  dbluTable + " WHERE" + keyOrder;		
		List<DblookupType> queryResult = null;
		try {
			queryResult = jt.query(sql, new getMapper(), domainId, userId);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.info("result size = " + queryResult.size());		
		return queryResult;
	}

	public List<DblookupType> getDblookup(final SetDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		String sql = "SELECT * FROM " +  dbluTable + " WHERE c_project_path=? AND " + keyOrder;		
		List<DblookupType> queryResult = null;
		try {
			queryResult = jt.query(sql, new getMapper(), slashEnd(dblookupType.getProjectPath()), dblookupType.getDomainId(), dblookupType.getOwnerId());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	public List<DblookupType> getDblookup(String column, String value) throws DataAccessException, I2B2Exception {
		String sql = "SELECT * FROM " +  dbluTable + " WHERE ";		
		String v = value, s = column.toLowerCase();
		List<DblookupType> queryResult = null;
		try {
			if (s.equalsIgnoreCase("domain_id")) {
				sql += keyOrder;
				queryResult = jt.query(sql, new getMapper(), value, userId);
			} else if (s.equalsIgnoreCase("owner_id")) {
				sql += keyOrder;
				queryResult = jt.query(sql, new getMapper(), domainId, value);
			} else {
				sql += "c_" + column + "=? AND " + keyOrder;
				if (s.equalsIgnoreCase("project_path")) {
					v = slashEnd(value);
				} else {
				}
				queryResult = jt.query(sql, new getMapper(), v, domainId, userId);
			}
			log.info(sql + "(c_" + column + "=" + v + ", domainId=" + domainId + ", userId=" + userId + ") -- # of entries found: " + queryResult.size());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	public int setDblookup(final SetDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		List<DblookupType> queryResult = getDblookup(dblookupType);
		if (null == queryResult || (0 == queryResult.size())) {
			return insertDblookup(dblookupType);
		} else {
			return updateDblookup(dblookupType);
		}
	}

	public int insertDblookup(final SetDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		int numRowsAdded = 0;
		String sql = "INSERT INTO " + dbluTable +
				"(c_domain_id, c_project_path, c_owner_id, c_db_fullschema, c_db_datasource, c_db_servertype, c_db_nicename, c_db_tooltip, c_comment, c_entry_date, c_change_date, c_status_cd) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";		
		numRowsAdded = jt.update(sql, 
				dblookupType.getDomainId(),  
				slashEnd(dblookupType.getProjectPath()),
				dblookupType.getOwnerId(),
				dblookupType.getDbFullschema(),
				dblookupType.getDbDatasource(),
				dblookupType.getDbServertype(),
				dblookupType.getDbNicename(),
				dblookupType.getDbTooltip(),
				dblookupType.getComment(),
				Calendar.getInstance().getTime(),
				Calendar.getInstance().getTime(),
				dblookupType.getStatusCd()
				);
		log.info("insertDblookup - Number of rows added: " + numRowsAdded);
		return numRowsAdded;
	}

	public int updateDblookup(final SetDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		int numRowsSet = 0;
		String sql = "UPDATE " + dbluTable +
				"SET c_db_fullschema=?, c_db_datasource=?, c_db_servertype=?, c_db_nicename=?, c_db_tooltip=?, c_comment=?, c_change_date=?, c_status_cd=? WHERE c_project_path=? AND " + 
				key;		
		numRowsSet = jt.update(sql, 
				dblookupType.getDbFullschema(),
				dblookupType.getDbDatasource(),
				dblookupType.getDbServertype(),
				dblookupType.getDbNicename(),
				dblookupType.getDbTooltip(),
				dblookupType.getComment(),
				Calendar.getInstance().getTime(),
				dblookupType.getStatusCd(),
				slashEnd(dblookupType.getProjectPath()),
				dblookupType.getDomainId(),  
				dblookupType.getOwnerId()
				);
		log.info("updateDblookup - Number of rows updated: " + numRowsSet);
		return numRowsSet;
	}

	public int deleteDblookup(final DeleteDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		int numRowsDeleted = 0;
		String sql = "DELETE FROM " + dbluTable + " WHERE c_project_path=? AND " + key;		
		try {
			numRowsDeleted = jt.update(sql, slashEnd(dblookupType.getProjectPath()), dblookupType.getDomainId(), dblookupType.getOwnerId());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return numRowsDeleted;	
	}

}


class getMapper implements RowMapper<DblookupType> {
	@Override
		public DblookupType mapRow(ResultSet rs, int rowNum) throws SQLException {
			DblookupType dblu = new DblookupType();
			dblu.setDomainId(rs.getString("c_domain_id"));
			dblu.setProjectPath(rs.getString("c_project_path"));
			dblu.setOwnerId(rs.getString("c_owner_id"));
			dblu.setDbFullschema(rs.getString("c_db_fullschema"));
			dblu.setDbDatasource(rs.getString("c_db_datasource"));
			dblu.setDbServertype(rs.getString("c_db_servertype"));
			dblu.setDbNicename(rs.getString("c_db_nicename"));
			dblu.setDbTooltip(rs.getString("c_db_tooltip"));
			dblu.setComment(rs.getString("c_comment"));
			dblu.setEntryDate(rs.getString("c_entry_date"));
			dblu.setChangeDate(rs.getString("c_change_date"));
			dblu.setStatusCd(rs.getString("c_status_cd"));
			return dblu;
		}

	}
