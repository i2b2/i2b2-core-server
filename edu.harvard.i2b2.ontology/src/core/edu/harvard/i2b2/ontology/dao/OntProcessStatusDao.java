/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.ontology.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;

import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.DirtyValueType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusListType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;

/**
 * Class to access table_access table.
 * 
 * @author rkuttan
 * 
 */
public class OntProcessStatusDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(OntProcessStatusDao.class);

	private JdbcTemplate jt = null;
	private DataSource dataSource = null;
	private DBInfoType dbInfoType = null;
	private ProjectType projectType = null;

	public void setDataSourceObject(DataSource dataSource) {
		this.jt = new JdbcTemplate(dataSource);
	}

	public OntProcessStatusDao(DataSource dataSource, ProjectType projectType,
			DBInfoType dbInfo) {
		this.dataSource = dataSource;
		this.projectType = projectType;
		this.dbInfoType = dbInfo;
		this.jt = new JdbcTemplate(dataSource);
	}

	public OntologyProcessStatusType createOntologyProcessStatus(
			final OntologyProcessStatusType ontProcessStatusType, final String userId)
					throws I2B2DAOException {
		int numRowsAdded = 0;
		try {
			Date today = new Date(System.currentTimeMillis());

			String addSql = "insert into "
					+ this.dbInfoType.getDb_fullSchema()
					+ "ONT_PROCESS_STATUS"
					+ "(process_id, process_type_cd, process_step_cd, start_date,  process_status_cd, changedby_char, message,entry_date,status_cd) values (?,?,?,?,?,?,?,?,?)";
			int processId = 0;
			if (this.dbInfoType.getDb_serverType().equals("ORACLE")) {
				log.info(addSql);

				ontProcessStatusType.setProcessId(String.valueOf(processId));
				/*numRowsAdded = jt.update(addSql, ontProcessStatusType
						.getProcessId(), ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), today, "PROCESSING", userId,
						ontProcessStatusType.getMessage(), today, "C");*/

				/*numRowsAdded = jt.update(addSql, new Object[] { ontProcessStatusType
						.getProcessId(), ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), today, "PROCESSING", userId,
						ontProcessStatusType.getMessage(), today, "C" },
						new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
								Types.TIMESTAMP,  Types.VARCHAR,
								Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP,Types.VARCHAR });
				 */
				SaveOntProcessStatus saveOntProcessStatus = new SaveOntProcessStatus(dataSource, 
						dbInfoType);
				saveOntProcessStatus.save(ontProcessStatusType, userId);
				processId = Integer.parseInt(ontProcessStatusType.getProcessId());
				/*numRowsAdded = jt.update(addSql, new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setInt(1, Integer.parseInt(ontProcessStatusType
								.getProcessId()));
						ps.setString(2, ontProcessStatusType
								.getProcessTypeCd());
						ps.setString(3, ontProcessStatusType
								.getProcessStepCd());
						ps.setTimestamp(4, today);
						ps.setString(5, "PROCESSING");
						ps.setString(6, userId);

						ps.setString(7, ontProcessStatusType.getMessage());
						ps
								.setTimestamp(
										8,
										today);
						ps.setString(9, "C");

					}
				});*/
			} else if (this.dbInfoType.getDb_serverType().equals("SQLSERVER")
					|| this.dbInfoType.getDb_serverType().equals("POSTGRESQL")
					|| this.dbInfoType.getDb_serverType().equals("SNOWFLAKE")) {
				addSql = "insert into "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_PROCESS_STATUS"
						+ "(process_type_cd, process_step_cd, start_date,  process_status_cd, changedby_char, message,entry_date,status_cd) values (?,?,?,?,?,?,?,?)";
				/*numRowsAdded = jt.update(addSql, new Object[] {ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), today, "PROCESSING", userId,
						ontProcessStatusType.getMessage(), today, "C"}, new int[] {Types.VARCHAR, Types.VARCHAR,
						Types.TIMESTAMP,  Types.VARCHAR,
						Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP,Types.VARCHAR});*/

				/*numRowsAdded = jt.update(addSql, new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setString(1, ontProcessStatusType.getProcessTypeCd());
						ps.setString(2, ontProcessStatusType
								.getProcessStepCd());
						ps.setDate(3, new java.sql.Date(today.getTime()));

						ps.setString(4, "PROCESSING");
						ps.setString(5, userId);

						ps.setString(6, ontProcessStatusType.getMessage());
						ps
								.setDate(
										7,
										new java.sql.Date(today.getTime()));
						ps.setString(8, "C");

					}
				});*/
				SaveOntProcessStatus saveOntProcessStatus = new SaveOntProcessStatus(dataSource, 
						dbInfoType);
				saveOntProcessStatus.save(ontProcessStatusType, userId);
				processId = Integer.parseInt(ontProcessStatusType.getProcessId());
				//processId = jt.queryForInt("SELECT @@IDENTITY");

			}
			ontProcessStatusType.setProcessId(String.valueOf(processId));
			log.debug("Rows added [" + numRowsAdded + "]");
			return ontProcessStatusType;
		} catch (DataAccessException e) {
			e.printStackTrace();
			log.error("Dao ontProcessStatus failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error ", e);
		}
	}

	public OntologyProcessStatusType findById(int processId) {
		String sql = "select * from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS where process_id = ?";
		OntologyProcessStatusType ontProcessStatusType = jt.queryForObject(sql,
				new geOntologyProcessStatuMapper(), processId);
		return ontProcessStatusType;
	}


	public List<OntologyProcessStatusType> findByProcessTypeAndStatus(String processTypeCd, String processStatusCd) {
		String sql = "select * from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS where process_status_cd = ? and process_type_cd = ? ";
		List<OntologyProcessStatusType> ontProcessStatusType = jt.query(sql,
				new geOntologyProcessStatuMapper(), processStatusCd,processTypeCd);
		return ontProcessStatusType;
	}

	public OntologyProcessStatusListType findProcessStatus(int processId, String processTypeCd, String processStatusCd, Date[] startDate, Date[] endDate, int maxReturnRow) {
		String dbType = this.dbInfoType.getDb_serverType();
		String topClause = " ", andClause = " ";
		if (maxReturnRow == 0) { 
			maxReturnRow = 1000;
			log.debug("setting maximum return rows to 1000");
		}
		if (dbType.equalsIgnoreCase("SQLSERVER")) { 
			topClause = " top " + maxReturnRow;
		}
		String sql = "select " + topClause + " * from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS   ";

		List<Object> paramList = new ArrayList<Object>(); 

		String whereClause = "";
		if (processId > 0) { 

			whereClause += " process_id = ? ";
			paramList.add(processId);
			andClause = " and ";
		}
		if (processTypeCd != null) {
			whereClause += andClause + " process_type_cd = ? ";
			paramList.add(processTypeCd);
			andClause = " and ";
		}
		if (processStatusCd != null) { 
			whereClause += andClause + " process_status_cd = ? ";
			paramList.add(processStatusCd);
			andClause = " and ";
		}
		if (startDate !=null) { 
			if (startDate[0] != null) { 
				whereClause += andClause + " start_date >= ?"; 
				paramList.add(startDate[0]);
				andClause = " and ";
			}
			if (startDate[1] != null) { 
				whereClause += andClause + " start_date <= ?";
				paramList.add(startDate[1]);
				andClause = " and ";
			}
		}
		if (endDate != null) { 
			if (endDate[0] != null) { 
				whereClause += andClause + " end_date >= ?"; 
				paramList.add(endDate[0]);
				andClause = " and ";
			}
			if (endDate[1] != null) { 
				whereClause += andClause + " end_date <= ?";
				paramList.add(endDate[1]);
				andClause = " and ";
			}
		}
		if (whereClause.length()>0) { 
			sql = sql + " where " + whereClause;
		}
		sql += " order by process_id ";

		if (this.dbInfoType.getDb_serverType().equalsIgnoreCase("POSTGRESQL")) { 
			sql += " limit " + maxReturnRow ;
		}
		if (this.dbInfoType.getDb_serverType().equalsIgnoreCase("SNOWFLAKE")) {
			sql += " limit " + maxReturnRow ;
		}
		if (this.dbInfoType.getDb_serverType().equalsIgnoreCase("ORACLE")) { 
			sql = " select * from (" + sql + " ) where rownum <= " + maxReturnRow ;
		}
		List<OntologyProcessStatusType> ontProcessStatusType = 
				jt.query(sql, new geOntologyProcessStatuMapper(), paramList.toArray(new Object[]{})); 
				//jt.getJdbcOperations().query(sql, paramList.toArray(new Object[]{}), getParameterizedRowMapper());
		OntologyProcessStatusListType ontProcessStatusListType = new OntologyProcessStatusListType();
		if (ontProcessStatusType != null) {
			ontProcessStatusListType.getOntologyProcessStatus().addAll(ontProcessStatusType);
		}
		return ontProcessStatusListType;
	}


	public int updateStatus(final int processId, final Date endDate, final String processStateCd,
			final String statusCd) throws I2B2DAOException {
		final Timestamp today = new Timestamp(Calendar.getInstance().getTime().getTime());
		String sql = "update " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS set ";
		if (endDate != null) {
			sql += "end_date = ?,";
		}
		sql += "process_step_cd = ?,process_status_cd = ?,change_date = ?,status_cd = ? where process_id = ? ";
		int recordCount = 0;
		if (endDate != null) {
			recordCount = jt.update(sql, endDate, processStateCd, statusCd,
					today, "U", processId);
		} else {
			recordCount = jt.update(sql, processStateCd, statusCd, today, "U",
					processId);
		}

		return recordCount;
	}

	public int updateStatusMessage(int processId, String message) {
		Date today = Calendar.getInstance().getTime();
		String sql = "update " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS set  message = ? where process_id = ? ";
		int recordCount = jt.update(sql, message, processId);
		return recordCount;
	}

	public int updateCRCUploadId(int processId, String uploadId) {
		Date today = Calendar.getInstance().getTime();
		int uploadIdInt = 0; 
		if (uploadId != null) { 
			uploadIdInt = Integer.parseInt(uploadId);
		}
		String sql = "update "
				+ dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS set  crc_upload_id = ? where process_id = ? ";
		int recordCount = jt.update(sql, uploadIdInt, processId);
		return recordCount;
	}


	public DirtyValueType getDirtyState(GetReturnType returnType, DBInfoType dbInfo) {
		DirtyValueType response;
		int count = getDeleteEditCount(returnType, dbInfo);
		log.debug("Dirty process delete/edit after sync count = " + count);
		if(count > 0){
			response = DirtyValueType.DELETE_EDIT;
		}
		else {
			count = getAddCount(returnType, dbInfo);
			log.debug("Dirty process add after update count = " + count);
			if(count > 0){
				response = DirtyValueType.ADD;
			}
			else {
				response = DirtyValueType.NONE;

			}
		}
		log.debug(response.value());
		return response;
	}

	private int getDeleteEditCount(GetReturnType returnType, DBInfoType dbInfo){
		String startDateSql = "select start_date from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS where process_type_cd = ? " +
		" and status_cd <> 'ERROR' order by start_date desc";

		List<java.sql.Timestamp> queryResult = null;
		try{
			queryResult = jt.queryForList(startDateSql,java.sql.Timestamp.class,"ONT_SYNCALL_CRC_CONCEPT");
		}catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}

		int count = -1;
		String sql = null;
		if(queryResult.isEmpty()){
			sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
			+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)" ;
			count = jt.queryForObject(sql, Integer.class,  "ONT_EDIT_CONCEPT", "ONT_DELETE_CONCEPT");

		}else{

			java.util.Date date2 = new java.util.Date(queryResult.get(0).getTime());
			if (dbInfoType.getDb_serverType().equalsIgnoreCase("ORACLE")){ 
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");	  			
				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)"+
				"and start_date > to_date('" + sqlFormatedStartDate +  "', 'DD-MM-YYYY HH24:MI:SS') ";
			}
			else if(dbInfoType.getDb_serverType().equalsIgnoreCase("SQLSERVER")
					|| dbInfoType.getDb_serverType().equalsIgnoreCase("POSTGRESQL")
					|| dbInfoType.getDb_serverType().equalsIgnoreCase("SNOWFLAKE")){
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)"+
				"and start_date >  '" + sqlFormatedStartDate +  "' ";
			}

			if(sql != null)
				count = jt.queryForObject(sql, Integer.class, "ONT_EDIT_CONCEPT", "ONT_DELETE_CONCEPT");
		}
		return count;
	}

	private int getAddCount(GetReturnType returnType, DBInfoType dbInfo){

		// get last startDate of all syncs and updates
		String startDateSql = "select start_date from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)" +
		" and status_cd <> 'ERROR' order by start_date desc";



		List<java.sql.Timestamp> queryResult = null;
		try{
			queryResult = jt.queryForList(startDateSql,java.sql.Timestamp.class,"ONT_UPDATE_CRC_CONCEPT", "ONT_SYNCALL_CRC_CONCEPT");
		}catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}

		int count = -1;
		String sql = null;
		if(queryResult.isEmpty()){  // no updates or syncs so look for # of adds in general
			sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
			+ "ONT_PROCESS_STATUS where process_type_cd = ? ";
			count = jt.queryForObject(sql, Integer.class, "ONT_ADD_CONCEPT");

		}
		if(count == -1) {  // this means we havent found anything yet so 
			// look for adds after startDate....
			java.util.Date date2 = new java.util.Date(queryResult.get(0).getTime());
			if (dbInfoType.getDb_serverType().equalsIgnoreCase("ORACLE")){ 
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");	  			
				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where process_type_cd = ? and start_date > " +
				" to_date('" + sqlFormatedStartDate +  "', 'DD-MM-YYYY HH24:MI:SS') ";
			}
			else if(dbInfoType.getDb_serverType().equalsIgnoreCase("SQLSERVER") ||
					dbInfoType.getDb_serverType().equalsIgnoreCase("POSTGRESQL")||
					dbInfoType.getDb_serverType().equalsIgnoreCase("SNOWFLAKE")){
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where process_type_cd = ? and start_date > " +
				"'" + sqlFormatedStartDate +  "' ";
			}

			if(sql != null)
				count = jt.queryForObject(sql, Integer.class, "ONT_ADD_CONCEPT");
		}

		return count;
	}

	public int createOntologyProcessType(
			String ontProcessType, String userId)
					throws I2B2DAOException {
		int numRowsAdded = 0;
		try {
			Date today = Calendar.getInstance().getTime();

			String addSql = "insert into "
					+ this.dbInfoType.getDb_fullSchema()
					+ "ONT_PROCESS_STATUS"
					+ "(process_id, process_type_cd, start_date, changedby_char, process_status_cd, status_cd, end_date, entry_date ) values (?,?,?,?,?,?,?,?)";
			int processId = 0;
			if (this.dbInfoType.getDb_serverType().equals("ORACLE")) {
				log.info(addSql);
				processId = jt.queryForObject("select "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_SQ_PS_PRID.nextval from dual", Integer.class);

				numRowsAdded = jt.update(addSql, String.valueOf(processId), ontProcessType, 
						today, userId, "COMPLETED", "C", today, today);
			} else if (this.dbInfoType.getDb_serverType().equals("SQLSERVER") ||
					dbInfoType.getDb_serverType().equalsIgnoreCase("POSTGRESQL") ||
					dbInfoType.getDb_serverType().equalsIgnoreCase("SNOWFLAKE")) {
				addSql = "insert into "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_PROCESS_STATUS"
						+ "(process_type_cd,  start_date, changedby_char, process_status_cd, status_cd, end_date, entry_date) values (?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, ontProcessType,  today, userId, "COMPLETED", "C", today, today);

				processId = jt.queryForObject("SELECT @@IDENTITY", Integer.class);

			}

			log.debug("Rows added [" + numRowsAdded + "]");
			return numRowsAdded;
		} catch (DataAccessException e) {
			//		e.printStackTrace();
			log.error("Dao ontProcessStatus failed");
			//		log.error(e.getMessage());
			throw new I2B2DAOException("Data access error ", e);
		}
	}


	private static class SaveOntProcessStatus extends SqlUpdate {

		private String INSERT_ORACLE = "";
		private String INSERT_SQLSERVER = "";
		private String SEQUENCE_ORACLE = "";
		private DBInfoType dbInfo  = null;

		public SaveOntProcessStatus(DataSource dataSource,
				DBInfoType dbInfo) {

			super();
			this.dbInfo = dbInfo;
			// sqlServerSequenceDao = new
			// SQLServerSequenceDAO(dataSource,dataSourceLookup) ;
			setDataSource(dataSource);
			if (dbInfo.getDb_serverType().equalsIgnoreCase(
					"ORACLE")) {
				INSERT_ORACLE = "insert into "
						+ dbInfo.getDb_fullSchema()
						+ "ONT_PROCESS_STATUS"
						+ "(process_id, process_type_cd, process_step_cd, start_date,  process_status_cd, changedby_char, message,entry_date,status_cd) values (?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);

				SEQUENCE_ORACLE =  "select "
						+ this.dbInfo.getDb_fullSchema()
						+ "ONT_SQ_PS_PRID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));

			} else if (dbInfo.getDb_serverType().equalsIgnoreCase(
					"SQLSERVER") || dbInfo.getDb_serverType().equalsIgnoreCase("POSTGRESQL")
			|| dbInfo.getDb_serverType().equalsIgnoreCase("SNOWFLAKE")) {
				INSERT_SQLSERVER = "insert into "
						+ dbInfo.getDb_fullSchema()
						+ "ONT_PROCESS_STATUS"
						+ "( process_type_cd, process_step_cd, start_date,  process_status_cd, changedby_char, message,entry_date,status_cd) values (?,?,?,?,?,?,?,?)";
				setSql(INSERT_SQLSERVER);
			}


			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));

			compile();
		}

		public void save(OntologyProcessStatusType ontProcessStatusType, String userId) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int processId = 0;
			Object[] object = null;
			if (dbInfo.getDb_serverType().equalsIgnoreCase(
					"SQLSERVER")) {
				object = new Object[] {

						ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), new Date(System.currentTimeMillis()), "PROCESSING", userId,
						ontProcessStatusType.getMessage(), new Date(System.currentTimeMillis()), "C" };

			} else if (dbInfo.getDb_serverType().equalsIgnoreCase(
					"ORACLE")) {
				processId = jdbc.queryForObject(SEQUENCE_ORACLE, Integer.class);
				ontProcessStatusType.setProcessId(String
						.valueOf(processId));
				object = new Object[] { ontProcessStatusType.getProcessId(),
						ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), new Date(System.currentTimeMillis()), "PROCESSING", userId,
						ontProcessStatusType.getMessage(), new Date(System.currentTimeMillis()), "C" };
			}

			update(object);
			if (dbInfo.getDb_serverType().equalsIgnoreCase(
					"SQLSERVER")) {
				int processIdentityId = jdbc
						.queryForObject("SELECT @@IDENTITY", Integer.class);

				ontProcessStatusType.setProcessId(String
						.valueOf(processIdentityId));
				log.debug(processIdentityId);
			}
		}
	}


}


//private ParameterizedRowMapper<OntologyProcessStatusType> getParameterizedRowMapper() {

class geOntologyProcessStatuMapper implements RowMapper<OntologyProcessStatusType> {

	DTOFactory factory = new DTOFactory();
	Date startDate = null, endDate = null;

	@Override
	public OntologyProcessStatusType mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		OntologyProcessStatusType processStatusType = new OntologyProcessStatusType();
		processStatusType.setProcessId(rs.getString("process_id"));
		processStatusType.setProcessTypeCd(rs.getString("process_type_cd"));
		startDate = rs.getTimestamp("start_date");
		if (startDate != null) {
			processStatusType.setStartDate(factory
					.getXMLGregorianCalendar(startDate.getTime()));
		}
		endDate = rs.getTimestamp("end_date");
		if (endDate != null) {
			processStatusType.setEndDate(factory
					.getXMLGregorianCalendar(endDate.getTime()));
		}
		processStatusType.setProcessStepCd(rs
				.getString("process_step_cd"));
		processStatusType.setProcessStatusCd(rs
				.getString("process_status_cd"));
		processStatusType.setCrcUploadId(rs.getString("crc_upload_id"));
		processStatusType.setMessage(rs.getString("message"));
		return processStatusType;
	}

}