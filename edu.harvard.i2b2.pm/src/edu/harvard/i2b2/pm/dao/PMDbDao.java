/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.pm.dao;

import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.pm.ejb.DBInfoType;
//import edu.harvard.i2b2.pm.services.EnvironmentData;
//import edu.harvard.i2b2.pm.services.HiveParamData;
//import edu.harvard.i2b2.pm.services.ProjectUserParamData;
//import edu.harvard.i2b2.pm.services.RegisteredCellParam;
//import edu.harvard.i2b2.pm.services.RoleData;
//import edu.harvard.i2b2.pm.services.VariableData;
import edu.harvard.i2b2.pm.services.HiveParamData;
import edu.harvard.i2b2.pm.services.ProjectUserParamData;
import edu.harvard.i2b2.pm.services.SessionData;
import edu.harvard.i2b2.pm.services.UserParamData;
//import edu.harvard.i2b2.pm.services.RegisteredCellParam;
//import edu.harvard.i2b2.pm.services.RoleData;
//import edu.harvard.i2b2.pm.services.VariableData;
import edu.harvard.i2b2.pm.util.PMUtil;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pm.datavo.pm.ApprovalType;
import edu.harvard.i2b2.pm.datavo.pm.BlobType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ParamsType;
import edu.harvard.i2b2.pm.datavo.pm.PasswordType;
import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDataType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectRequestType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;
import edu.harvard.i2b2.pm.datavo.pm.RoleType;
import edu.harvard.i2b2.pm.datavo.pm.RolesType;
import edu.harvard.i2b2.pm.datavo.pm.UserType;



public class PMDbDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(PMDbDao.class);


	private SimpleJdbcTemplate jt;
	private String database = "";
	public PMDbDao() throws I2B2Exception{
		DataSource ds = null;
		Connection conn = null;
		try {
			ds = PMUtil.getInstance().getDataSource("java:/PMBootStrapDS");
	//		database = ds.getConnection().getMetaData().getDatabaseProductName();
			log.debug(ds.toString());
		} catch (I2B2Exception e2) {
			log.error("bootstrap ds failure: " + e2.getMessage());
			throw e2;
//		} catch (SQLException e2) {
//			log.error("bootstrap ds failure: " + e2.getMessage());
			//throw e2;
		} 
		
		try {
			conn  = ds.getConnection(); 
			database = conn.getMetaData().getDatabaseProductName();
			conn.close();
			conn = null;
		} catch (Exception e)
		{
			conn = null;
			log.error("Error geting database name:" + e.getMessage());
		} finally
		{
			conn = null;
		}
		
		this.jt = new SimpleJdbcTemplate(ds);
	//	ds = null;
	}



	@SuppressWarnings("unchecked")
	public List<DBInfoType> getUser(String userId, String caller) throws I2B2Exception, I2B2DAOException { 
		return getUser(userId, caller, null, true);
		/*String sql =  "select * from pm_user_data where user_id = ? and status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getUser(), userId);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error in getting User Data");
		}
		return queryResult;	
		 */
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getEnvironmentData(String domainId) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from pm_hive_params where domain_id = ? and status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getEnvironmentParams(), domainId);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getEnvironment(String domainId) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from pm_hive_data where active='1' and status_cd <> 'D'";

		if (domainId != null) 
			sql += " and domain_id = ?";

		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			log.debug("Start query");
			if (domainId == null)
				queryResult = jt.query(sql, getEnvironment());
			else
				queryResult = jt.query(sql, getEnvironment(), domainId);
			log.debug("Query Size: " + queryResult.size());
			log.debug("End query");
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}


	@SuppressWarnings("unchecked")
	public List<DBInfoType> getRole(String userId, String project ) throws I2B2Exception, I2B2DAOException { 
		String sql = "select  distinct" +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pur.PROJECT_ID" +
				"         when 'PROJECT_ID' then pur.PROJECT_ID" +
				"         else null" +
				"    end as PROJECT_ID," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pur.USER_ID" +
				"         when 'USER_ID' then pur.USER_ID" +
				"         else null" +
				"    end as USER_ID," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pur.USER_ROLE_CD" +
				"         when 'USER_ROLE_CD' then pur.USER_ROLE_CD" +
				"         else null" +
				"    end as USER_ROLE_CD" +
				" from " +
				"    pm_project_user_roles pur, pm_role_requirement rr" +
				" where " +
				"    pur.status_cd<>'D' and" +
				"    rr.status_cd<>'D' and" +
				"    pur.user_id = ? and" +
				(project!=null?"    pur.project_id = ? and":"") +
				"    (rr.read_hivemgmt_CD = '@') OR (upper(rr.read_hivemgmt_CD) =  upper(pur.USER_ROLE_CD)) and" +
				"    upper(rr.table_cd) =  'PM_PROJECT_USER_ROLES'";
		//		String sql =  "select * from pm_project_user_roles where user_id=? and project_id=? and status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			if (project == null)
				queryResult = jt.query(sql, getRole(), userId);
			else
				queryResult = jt.query(sql, getRole(), userId, project);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}
	@SuppressWarnings("unchecked")
	public List<DBInfoType> getRole(String userId) throws I2B2Exception, I2B2DAOException { 
		return getRole(userId, null);
		/*
		String sql =  "select * from pm_project_user_roles where user_id=? and status_cd<>'D' order by project_id ";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getRole(), userId);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
		 */
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getProject(Object utype, boolean ignoreDeleted) throws I2B2Exception, I2B2DAOException { 
		String sql = "select  distinct" +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_ID" +
				"         when 'PROJECT_ID' then pd.PROJECT_ID" +
				"         else null" +
				"    end as PROJECT_ID," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_NAME" +
				"         when 'PROJECT_NAME' then pd.PROJECT_NAME" +
				"         else null" +
				"    end as PROJECT_NAME," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_WIKI" +
				"         when 'PROJECT_WIKI' then pd.PROJECT_WIKI" +
				"         else null" +
				"    end as PROJECT_WIKI," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_PATH" +
				"         when 'PROJECT_PATH' then pd.PROJECT_PATH" +
				"         else null" +
				"    end as PROJECT_PATH," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_KEY" +
				"         when 'PROJECT_KEY' then pd.PROJECT_KEY" +
				"         else null" +
				"    end as PROJECT_KEY," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_DESCRIPTION" +
				"         when 'PROJECT_DESCRIPTION' then pd.PROJECT_DESCRIPTION" +
				"         else null" +
				"    end as PROJECT_DESCRIPTION" +
				" from " +
				"    pm_project_data pd, pm_project_user_roles pur, pm_role_requirement rr" +
				" where " +
				"    pur.status_cd<>'D' and" +
				(ignoreDeleted?"    pd.STATUS_CD<>'D' and ":"") +
				"    rr.status_cd<>'D' and" +
				" 	 pd.project_ID = ? and" +
				//" 	 pd.project_path = ? and" +
				"	 (rr.read_hivemgmt_CD = '@') OR (upper(rr.read_hivemgmt_CD) =  upper(pur.USER_ROLE_CD)) and" +
				"    pd.PROJECT_ID = pur.project_id and" +
				"    upper(rr.table_cd) =  'PM_PROJECT_USER_ROLES'";

		//		String sql =  "select * from pm_project_data where project_id=? and status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getProject(), ((ProjectType) utype).getId()); //, ((ProjectType) utype).getPath());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	public List<DBInfoType> getUserProject(String user) throws I2B2Exception, I2B2DAOException { 

		String sql = "select  distinct" +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_ID" +
				"         when 'PROJECT_ID' then pd.PROJECT_ID" +
				"         else null" +
				"    end as PROJECT_ID," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_NAME" +
				"         when 'PROJECT_NAME' then pd.PROJECT_NAME" +
				"         else null" +
				"    end as PROJECT_NAME," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_WIKI" +
				"         when 'PROJECT_WIKI' then pd.PROJECT_WIKI" +
				"         else null" +
				"    end as PROJECT_WIKI," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_PATH" +
				"         when 'PROJECT_PATH' then pd.PROJECT_PATH" +
				"         else null" +
				"    end as PROJECT_PATH," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_KEY" +
				"         when 'PROJECT_KEY' then pd.PROJECT_KEY" +
				"         else null" +
				"    end as PROJECT_KEY," +
				"    case  upper(rr.COLUMN_CD)" +
				"         when '@'   then pd.PROJECT_DESCRIPTION" +
				"         when 'PROJECT_DESCRIPTION' then pd.PROJECT_DESCRIPTION" +
				"         else null" +
				"    end as PROJECT_DESCRIPTION" +
				" from " +
				"    pm_project_data pd, pm_project_user_roles pur, pm_role_requirement rr" +
				" where " +
				"    pd.status_cd<>'D'  and" +
				"    pur.status_cd<>'D' and" +
				"    rr.status_cd<>'D' and" +
				" 	 pur.user_ID = ? and" +
				"    pd.PROJECT_ID = pur.PROJECT_ID and" +
				"	 (rr.read_hivemgmt_CD = '@') OR (upper(rr.read_hivemgmt_CD) =  upper(pur.USER_ROLE_CD)) and" +
				"    pd.PROJECT_ID = pur.project_id and" +
				"    upper(rr.table_cd) =  'PM_PROJECT_DATA'";

		//		String sql =  "select distinct pd.* from pm_project_data pd, pm_project_user_roles pur where pd.project_id=pur.project_id and pur.user_id = ?  and pur.status_cd<>'D' and pd.status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getProject(), user);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getProjectUserParams(String projectId, String userId) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from pm_project_user_params where project_id=? and user_id=? and status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getProjectUserParams(), projectId, userId);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getProjectParams(String projectId) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from pm_project_params where project_id=? and status_cd<>'D'";
		//log.debug(sql  + projectId );
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getProjectParams(), projectId);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getCell(String cell, String project, boolean ignoreDeleted) throws I2B2Exception, I2B2DAOException { 
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		log.debug("Searching for cell: " + cell + " within project " + project);
		try {
			if (cell.equals("@"))
			{
				String sql =  "select * from pm_cell_data where project_path = ?";
				if (ignoreDeleted)
					sql += " and status_cd<>'D'";
				queryResult = jt.query(sql, getCell(), project);				
			} else if ((cell.equals("@") && !project.equals("/")))				
			{
				String sql =  "select * from pm_cell_data where project_path = ?";
				if (ignoreDeleted)
					sql += " and status_cd<>'D'";
				queryResult = jt.query(sql, getCell(), project);

			} else
			{
				String sql =  "select * from pm_cell_data where cell_id = ? and project_path = ?";
				if (ignoreDeleted)
					sql += " and status_cd<>'D'";

				queryResult = jt.query(sql, getCell(), cell, project);
			}
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	@SuppressWarnings("unchecked")
	public List<DBInfoType> getApproval(ApprovalType approval, boolean ignoreDeleted) throws I2B2Exception, I2B2DAOException { 
		//		log.info(sql + domainId + projectId + ownerId);
		String sql = "select a.* from pm_approvals a ";
		String sqlWhere = " where a.object_cd = 'APPROVAL' ";
		String sqlFrom = "";
		List<DBInfoType> queryResult = null;
		try {
			ArrayList al = new ArrayList();
			if (approval.getId() != null)
			{
				sqlWhere +=  "and a.approval_id = ? ";
				if (ignoreDeleted)
					sqlWhere += "and a.status_cd<>'D' ";
				al.add(approval.getId());
			} 
			else {
				// Search if user and project are set
				String foundUser = "", foundProject = "";
				for (int i=0; i < approval.getSearch().size(); i++)
				{
					if (approval.getSearch().get(i).getBy().equalsIgnoreCase("USER"))
						foundUser = approval.getSearch().get(i).getValue().toUpperCase();
					if (approval.getSearch().get(i).getBy().equalsIgnoreCase("PROJECT"))
						foundUser = approval.getSearch().get(i).getValue().toUpperCase();
				}

				if ((foundUser != "") && (foundProject != ""))
				{
					sqlFrom += ", pm_project_user_params p ";
					sqlWhere += "and a.STATUS_CD = 'A' and p.STATUS_CD = 'A' and " +
							"p.PARAM_NAME_CD = 'APPROVAL_ID' and p.VALUE = a.APPROVAL_ID and p.USER_ID = ? and p.PROJECT_ID = ?";
					al.add(foundUser);
					al.add(foundProject);

				}
				for (int i=0; i < approval.getSearch().size(); i++)
				{
					if ((approval.getSearch() != null) && (approval.getSearch().get(i).getBy().equalsIgnoreCase("NAME")))
					{
						sqlWhere +=  "and UPPER(a.approval_name) = ? ";
						if (ignoreDeleted)
							sqlWhere += "and a.status_cd<>'D' ";
						al.add(approval.getSearch().get(i).getValue().toUpperCase());
						//	queryResult = jt.query(sql, getApproval(), approval.getActivationDate());				
					} 

					if ((approval.getSearch() != null) && (approval.getSearch().get(i).getBy().equalsIgnoreCase("ACTIVATION_DATE")))
					{
						sqlWhere +=  "and a.activation_date = ? ";
						if (ignoreDeleted)
							sqlWhere += "and a.status_cd<>'D' ";
						al.add(approval.getSearch().get(i).getValue().toUpperCase());
						//	queryResult = jt.query(sql, getApproval(), approval.getActivationDate());				

					} else if ((approval.getSearch() != null) && ((foundUser == "") || (foundProject == "")) && (approval.getSearch().get(i).getBy().equalsIgnoreCase("USER")))
					{
						sqlFrom += ", pm_user_params p ";
						sqlWhere += "and a.STATUS_CD = 'A' and p.STATUS_CD = 'A' and " +
								"p.PARAM_NAME_CD = 'APPROVAL_ID' and p.VALUE = a.APPROVAL_ID and p.USER_ID = ? ";
						al.add(approval.getSearch().get(i).getValue());
						//sql =  "select a.* from pm_approvals a, pm_user_params p where a.STATUS_CD = 'A' and p.STATUS_CD = 'A' and " +
						//"p.PARAM_NAME_CD = 'APPROVAL' and p.VALUE = a.OBJECT_CD and p.USER_ID = ? ";
						//	queryResult = jt.query(sql, getApproval(), approval.getSearch().get(0).getValue());				
					} else if ((approval.getSearch() != null) && ((foundUser == "") || (foundProject == "")) && (approval.getSearch().get(i).getBy().equalsIgnoreCase("PROJECT")))
					{
						sqlFrom += ", pm_project_params p ";
						sqlWhere += "and a.STATUS_CD = 'A' and p.STATUS_CD = 'A' and " +
								"p.PARAM_NAME_CD = 'APPROVAL_ID' and p.VALUE = a.APPROVAL_ID and p.PROJECT_ID = ? ";
						al.add(approval.getSearch().get(i).getValue());

						//sql =  "select a.* from pm_approvals a, pm_project_params p where a.STATUS_CD = 'A' and p.STATUS_CD = 'A' and " +
						//"p.PARAM_NAME_CD = 'APPROVAL' and p.VALUE = a.OBJECT_CD and p.PROJECT_ID = ? ";
						//	queryResult = jt.query(sql, getApproval(), approval.getSearch().get(0).getValue());				
					}
				}
			}
			sql += sqlFrom + sqlWhere;
			log.debug("My sql statement: " + sql);
			queryResult = jt.query(sql, getApproval(), al.toArray());				

		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}


	@SuppressWarnings("unchecked")
	public List<DBInfoType> getCellParam(String cellId, String project) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from  pm_cell_params where cell_id = ? and  project_path = ? and status_cd<>'D'";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getParam(), cellId, project);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	private  boolean validateRole(String caller, String isrole, String project) throws I2B2DAOException
	{

		List response = null;	

		if (isrole.equalsIgnoreCase("admin"))
		{
			try {
				String sql =  "select * from pm_project_user_roles where project_id=? and user_id=? and status_cd<>'D' order by project_id";
				response = jt.query(sql, getRole(), "@", caller);
				//response = getRole(caller, project);
			} catch (Exception e1) {
				throw new I2B2DAOException ("Database error in getting role data for validateRole");
			}

			Iterator it = response.iterator();

			while (it.hasNext()){
				RoleType user = (RoleType) it.next();
				if(user.getRole().equalsIgnoreCase("ADMIN")) {
					return(true);
				}
			}
			return false;
		}

		try {
			response = getRole(caller, project);
		} catch (I2B2DAOException e1) {
			throw new I2B2DAOException ( "Database error in getting user data for setuser");
		} catch (I2B2Exception e1) {
			throw new I2B2DAOException ("Database error in getting user data for setuser");
		}

		Iterator it = response.iterator();

		while (it.hasNext()){
			RoleType role = (RoleType) it.next();
			if(role.getRole().toLowerCase().equals(isrole)) {
				return(true);
			}
		}
		return false;
	}

	// All user Process
	public List<DBInfoType> getUser(String user, String caller,String password, boolean ignoreDeleted) throws I2B2Exception, I2B2DAOException { 

		String sql = null;
		List<DBInfoType> queryResult = null;

		if (caller == null)
		{
			sql =  "select * from pm_user_data where user_id = ?  "+  (password!=null?"    and password = ? ":"");
			if (ignoreDeleted)
				sql += " and status_cd<>'D'";

			try {
				if (password == null) 
					queryResult = jt.query(sql, getUser(true), user);
				else 
					queryResult = jt.query(sql, getUser(false), user, password);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error in getting userdata with password");
			}
		} else  {
			sql = "select  distinct" +
					"    case  upper(rr.COLUMN_CD)" +
					"         when '@'   then pud.USER_ID" +
					"         when 'USER_ID' then pud.USER_ID" +
					"         else null" +
					"    end as USER_ID," +
					"    case  upper(rr.COLUMN_CD)" +
					"         when '@'   then pud.FULL_NAME" +
					"         when 'FULL_NAME' then pud.FULL_NAME" +
					"         else null" +
					"    end as FULL_NAME," +
					"    case  upper(rr.COLUMN_CD)" +
					"         when '@'   then pud.PASSWORD" +
					"         when 'PASSWORD' then pud.PASSWORD" +
					"         else null" +
					"    end as PASSWORD," +
					"    case  upper(rr.COLUMN_CD)" +
					"         when '@'   then pud.EMAIL" +
					"         when 'EMAIL' then pud.EMAIL" +
					"         else null" +
					"    end as EMAIL " +
					" from " +
					"     pm_user_data pud, pm_role_requirement rr" +
					" where " +
					//"    pur.status_cd<>'D' and" +
					"    rr.status_cd<>'D' and" +
					(ignoreDeleted?"    pud.STATUS_CD<>'D' and ":"") +
					//"    pur.USER_ID = ? and " +
					"    pud.user_id = ? and" +
					(password!=null?"    password = ? and":"") +
					"    (rr.read_hivemgmt_CD = '@') and" + //OR (upper(rr.read_hivemgmt_CD) =  upper(pur.USER_ROLE_CD)) and" +
					"    upper(rr.table_cd) =  'PM_USER_DATA'";


			try {
				if (password == null) 
					queryResult = jt.query(sql, getUser(true), user);
				else 
					queryResult = jt.query(sql, getUser(false), user, password);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error in getting userdata with password");
			}
		}
		//		String sql =  "select * from pm_user_data where user_id = ? and password  = ? and status_cd <> 'D'";
		//		log.info(sql + domainId + projectId + ownerId);

		return queryResult;	
	}

	public List<DBInfoType> getAllProjectRequest(String project, String caller) throws I2B2Exception, I2B2DAOException { 
		String sql = null;
		List<DBInfoType> queryResult = null;

		if ((validateRole(caller, "admin", null)) || (validateRole(caller, "admin", null)))
		{
			sql =  "select * from pm_project_request where status_cd<>'D'";
			queryResult = jt.query(sql, getProjectRequest());
		}

		return queryResult;	
	}


	public List<DBInfoType> getAllUser(String project, String caller) throws I2B2Exception, I2B2DAOException { 
		String sql = null;
		List<DBInfoType> queryResult = null;

		if ((validateRole(caller, "admin", null)) || (validateRole(caller, "admin", null)))
		{
			sql =  "select * from pm_user_data where status_cd<>'D'";
			queryResult = jt.query(sql, getUser(false));
		}

		return queryResult;	
	}

	public int setUser(final UserType userdata, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if (validateRole(caller, "admin", null))
		{
			try {
				if ((getUser(userdata.getUserName(), caller, null, false) == null) || (getUser(userdata.getUserName(), caller,null, false).size() == 0))
				{
					String addSql = "insert into pm_user_data " + 
							"(user_id, full_name, email, password, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
					numRowsAdded = jt.update(addSql, 
							userdata.getUserName(),
							userdata.getFullName(),
							userdata.getEmail(),
							PMUtil.getInstance().getHashedPassword(userdata.getPassword().getValue()),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),				
							caller,
							"A");
				} else if (userdata.getPassword() != null)
				{
					//user already exists, lets try to update
					String addSql = "update pm_user_data " + 
							"set full_name = ?, email = ?, password = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where user_id = ?";

					numRowsAdded = jt.update(addSql, 
							userdata.getFullName(),
							userdata.getEmail(),
							PMUtil.getInstance().getHashedPassword(userdata.getPassword().getValue()),
							Calendar.getInstance().getTime(),
							caller,
							userdata.getUserName());					
				} else 
				{
					//user already exists, lets try to update
					String addSql = "update pm_user_data " + 
							"set full_name = ?, email = ?,  change_date = ?, changeby_char = ?,  status_cd = 'A' where user_id = ?";

					numRowsAdded = jt.update(addSql, 
							userdata.getFullName(),
							userdata.getEmail(),
							Calendar.getInstance().getTime(),
							caller,
							userdata.getUserName());					
				}

				// Deal with is_admin
				String addSql = "update pm_project_user_roles " + 
						" set status_cd = 'D', change_date = ?, changeby_char = ?  where  user_id = ? and user_role_cd = ?";
				numRowsAdded += jt.update(addSql, 
						Calendar.getInstance().getTime(),
						caller,
						userdata.getUserName(),
						"ADMIN");	
				if (userdata.isIsAdmin() == true) {
					try {
						addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								"@",
								userdata.getUserName(),
								"ADMIN",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					} catch (Exception e) {
						addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								"@",
								userdata.getUserName(),
								"ADMIN");	
					}
				}

			} catch (DataAccessException e) {
				log.error("Dao update setuser failed for: " + userdata.getUserName());
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}

	public int deleteUser(final String user, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if (validateRole(caller, "admin", null))
		{
			try {
				String addSql = "update pm_user_data " + 
						"set status_cd = 'D', change_date = ?, changeby_char = ? where user_id = ?";

				numRowsAdded = jt.update(addSql, 
						Calendar.getInstance().getTime(),
						caller,
						user);

				if (numRowsAdded ==0)
					throw new I2B2DAOException("User not updated, does it exist?");

			} catch (DataAccessException e) {
				log.error("Dao deleteuser failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows deleted: " + numRowsAdded);

		return numRowsAdded;

	}



	public List<DBInfoType> setProjectRequest(final ProjectRequestType groupdata,String project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		List<DBInfoType> queryResult = null;
		try {
			String clob = null;
			
			if (groupdata.getRequestXml() != null)
			{
				clob = groupdata.getRequestXml();
				/*
				BlobType blobType = (BlobType)groupdata.getRequestXml();
				for (int i=0; i < blobType.getContent().size(); i++)
				{
					clob  = (String) blobType.getContent().get(i);

					//Clob myclob = (Clob) blobType.getContent().get(i);
					//	int len = (int) myclob.length();
					//	clob = myclob.getSubString(Long.parseLong("1"),len);
					//				blobType.getContent().add(
					//					JDBCUtil.getClobString(clob));
					//		rData.setRequestXml(blobType);
				}
				*/
			}
			String addSql = "insert into pm_project_request " + 
					"(title, request_xml, project_id, change_date, entry_date, submit_char, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
			numRowsAdded = jt.update(addSql, 
					groupdata.getTitle(), 
					clob,
					(project == null? "@" : project),
					Calendar.getInstance().getTime(),
					Calendar.getInstance().getTime(),
					caller,
					caller,
					"A");

			if (numRowsAdded != 0)
			{
				addSql = "select  *  from pm_project_request where id =  ( select max(id) from pm_project_request)";

				queryResult = jt.query(addSql, getProjectRequest());
			}

		} catch (Exception e) {
			log.error("Dao deleteuser failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		return queryResult;

	}



	public int setPassword(final String password, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		try {
			String addSql = "update pm_user_data " + 
					"set password = ?, change_date = ?, changeby_char = ? where user_id = ?";

			numRowsAdded = jt.update(addSql, 
					password,
					Calendar.getInstance().getTime(),
					caller,
					caller);

			if (numRowsAdded ==0)
				throw new I2B2DAOException("User not updated, does it exist?");

		} catch (DataAccessException e) {
			log.error("Dao deleteuser failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows deleted: " + numRowsAdded);

		return numRowsAdded;

	}


	// All Cell Process
	/*
	public List<DBInfoType> getCell(String cell, String project, String owner) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from cell_data where cell_id = ? and project_path = ? and owner_id = ?";
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			queryResult = jt.query(sql, getCell(), cell, project, owner);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}
	 */


	public List<DBInfoType> getAllApproval(String project, String caller) throws I2B2Exception, I2B2DAOException { 
		String sql = null;
		List<DBInfoType> queryResult = null;

		sql =  "select * from pm_approvals where status_cd<>'D'";
		queryResult = jt.query(sql, getApproval());

		return queryResult;	
	}

	public List<DBInfoType> getAllCell(String project, String caller) throws I2B2Exception, I2B2DAOException { 
		String sql = null;
		List<DBInfoType> queryResult = null;

		sql =  "select * from pm_cell_data where status_cd<>'D'";
		queryResult = jt.query(sql, getCell());

		return queryResult;	
	}

	public List<DBInfoType>  getSession(String userId, String sessionID) throws I2B2Exception, I2B2DAOException { 
		String sql =  "select * from pm_user_session where user_id = ? and session_id = ?";
		List<DBInfoType> queryResult = null;
		log.debug("Searching for " + userId + " with session id of " + sessionID);
		queryResult = jt.query(sql, getSession(), userId, sessionID);
		return queryResult;	
	}

	public boolean verifyNotLockedOut(String userId)
	{
		
		
		String sql = null;
		//get results count max
		sql = "select * from pm_global_params where status_cd = 'A' and param_name_cd ='PM_LOCKED_MAX_COUNT'";

		int resultmax = 10;
		
		try {
			List<DBInfoType> queryResult  = jt.query(sql, getParam());
			Iterator it = queryResult.iterator();
			while (it.hasNext())
			{
				ParamType user = (ParamType)it.next();
				resultmax = Integer.parseInt(user.getValue());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			
		}
		
		sql = "select * from pm_global_params where status_cd = 'A' and param_name_cd ='PM_LOCKED_WAIT_TIME'";

		int waittime = 2;
		
		try {
			List<DBInfoType> queryResult  = jt.query(sql, getParam());
			Iterator it = queryResult.iterator();
			while (it.hasNext())
			{
				ParamType user = (ParamType)it.next();
				waittime = Integer.parseInt(user.getValue());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			
		}

		
		if (database.equalsIgnoreCase("oracle"))
			sql =  "select count(*) as badlogin from pm_user_login where user_id = ? and " +
				" attempt_cd = 'BADPASSWORD' and " +
				"(entry_date + interval '" + waittime + "' minute)  >= CURRENT_TIMESTAMP ";
		else if (database.equalsIgnoreCase("Microsoft sql server"))
			sql =  "select count(*) as badlogin from pm_user_login where user_id = ? and " +
					" attempt_cd = 'BADPASSWORD' and " +
					"dateadd(minute, " + waittime + ", entry_date)  >= getdate() ";
		else if (database.equalsIgnoreCase("postgresql"))
			sql =  "select count(*) as badlogin from pm_user_login where user_id = ? and " +
					" attempt_cd = 'BADPASSWORD' and " +
					"(entry_date + cast('" + waittime + " minutes' as interval))  >= now() ";
		
		int results = jt.queryForInt(sql, userId);

		//int results = 0;
		
		if (results >= resultmax)
			return true;
		else 
			return false;
	}
	
	public int setLoginAttempt(String userId, String attemptCd) {
		String addSql = "insert into pm_user_login " + 
				"(user_id, attempt_cd, changeby_char, entry_date, status_cd) values (?,?,?,?,'A')";

		int numRowsAdded =
				jt.update(addSql, 
				userId,
				attemptCd,
				userId,
				Calendar.getInstance().getTime());	

		return numRowsAdded;		
	}

	public int setSession(String userId, String sessionId, int timeout)
	{
		
		String addSql = "";

		if (database.equalsIgnoreCase("oracle"))
			 addSql = "insert into pm_user_session " + 
					"(user_id, session_id, changeby_char, entry_date, expired_date) values (?,?,?, systimestamp, systimestamp+numtodsinterval(" + (timeout * 1000) + ",'SECOND'))";
		else if (database.equalsIgnoreCase("Microsoft sql server"))
			 addSql = "insert into pm_user_session " + 
					"(user_id, session_id, changeby_char, entry_date, expired_date) values (?,?,?, getdate(), DATEADD(ms," + timeout + ",getdate()))";
		else if (database.equalsIgnoreCase("postgresql"))
			 addSql = "insert into pm_user_session " + 
					"(user_id, session_id, changeby_char, entry_date, expired_date) values (?,?,?,now(),  now() + interval '" + timeout + " millisecond')";

		Calendar now = Calendar.getInstance();
		now.add(Calendar.MILLISECOND, timeout);
		int numRowsAdded = jt.update(addSql, 
				userId,
				sessionId,
				userId);
				//Calendar.getInstance().getTime(),
				//now.getTime());	

		return numRowsAdded;
	}
	public int removeSession(String userId, String sessionId)
	{
		String addSql = "delete from pm_user_session " + 
				" where session_id = ? and user_id =?";
		int numRowsAdded = jt.update(addSql, 
				userId,
				sessionId);	

		addSql = "delete from pm_user_session " + 
				" where expired_date  > ?";
		int numRowsAdded2 = jt.update(addSql,
				Calendar.getInstance().getTime());



		return numRowsAdded;
	}

	public int updateSession(String userId, String sessionId, int timeout)
	{
		int numRowsAdded  = -1;
		String addSql = "update pm_user_session set expired_date = ? " + 
				" where session_id = ? and user_id =?";
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MILLISECOND, timeout);

		try {

			numRowsAdded = jt.update(addSql, 
					now.getTime(),
					sessionId,
					userId);	
		} catch (Exception e)
		{ try {
			if (e.getMessage().contains("deadlock")
					|| e.getMessage().contains("try restarting transaction")
					|| e.getMessage().contains(
							"failed to resume the transaction")) {
				int tosleep = new Random().nextInt(2000);
				log.warn("Transaction rolled back. Restarting transaction.");
				Thread.sleep(tosleep);
				numRowsAdded = jt.update(addSql, 
						now.getTime(),
						sessionId,
						userId);	
			} else {
				throw e;
			} } catch (Exception ee) {}

		}

		return numRowsAdded;
	}



	public int setCell(final CellDataType groupdata, String project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if ((validateRole(caller, "admin", null)) || (validateRole(caller, "manager", project)))
		{
			log.debug("Setting cell with ID of " + groupdata.getId() + " and path of " + groupdata.getProjectPath());
			if ((getCell(groupdata.getId(), groupdata.getProjectPath(), false) == null) || (getCell(groupdata.getId(), groupdata.getProjectPath(), false).size() == 0))
			{

				String addSql = "insert into pm_cell_data " + 
						"(cell_id, project_path, name, url, method_cd, can_override, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, 
						groupdata.getId(),
						(groupdata.getProjectPath() == null? "@" : groupdata.getProjectPath()),
						groupdata.getName(),
						groupdata.getUrl(),
						groupdata.getMethod(),
						(groupdata.isCanOverride() == null? 1 : groupdata.isCanOverride() ? 1 : 0),
						Calendar.getInstance().getTime(),
						Calendar.getInstance().getTime(),
						caller,
						"A");
			} else 
			{
				//user already exists, lets try to update
				String addSql = "update pm_cell_data " + 
						"set name = ?, url = ?, method_cd = ?, can_override = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where cell_id = ? and  project_path = ?";

				numRowsAdded = jt.update(addSql, 
						groupdata.getName(),
						groupdata.getUrl(),
						groupdata.getMethod(),
						(groupdata.isCanOverride() == null? 1 : groupdata.isCanOverride() ? 1 : 0),
						Calendar.getInstance().getTime(),
						caller,
						groupdata.getId(),
						groupdata.getProjectPath());					
			}
		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}

	public int deleteCell(String cell, String project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if ((validateRole(caller, "admin", null)) || (validateRole(caller, "manager", project)))
		{
			try {
				String addSql = "update pm_cell_data " + 
						"set status_cd = 'D' where project_path = ? and cell_id = ?";

				numRowsAdded = jt.update(addSql, 
						((project == null || project.equals(""))? "@": project), 
						cell);

				if (numRowsAdded ==0)
					throw new I2B2DAOException("Cell not updated, does it exist?");				
			} catch (DataAccessException e) {
				log.error("Dao deleteuser failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows deleted: " + numRowsAdded);

		return numRowsAdded;

	}



	public int setApproval(final ApprovalType groupdata, String project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if ((validateRole(caller, "admin", null)) || (validateRole(caller, "manager", project)))
		{

			if ((getApproval(groupdata, false) == null) || (getApproval(groupdata, false).size() == 0))
			{

				String addSql = "insert into pm_approvals " + 
						"(approval_id, approval_name, approval_description, approval_activation_date, approval_expiration_date, object_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, 
						groupdata.getId(),
						groupdata.getName(),
						groupdata.getDescription(),
						groupdata.getActivationDate(),
						groupdata.getExpirationDate(),
						groupdata.getObjectCd(),
						Calendar.getInstance().getTime(),
						Calendar.getInstance().getTime(),
						caller,
						"A");
			} else 
			{
				//user already exists, lets try to update
				String addSql = "update pm_approvals " + 
						"set approval_name = ?, approval_description = ?, approval_activation_date = ?, approval_expiration_date = ?, object_cd = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where approval_id = ?";

				numRowsAdded = jt.update(addSql, 
						groupdata.getName(),
						groupdata.getDescription(),
						groupdata.getActivationDate(),
						groupdata.getExpirationDate(),
						groupdata.getObjectCd(),
						Calendar.getInstance().getTime(),
						caller,
						groupdata.getId());

			}
		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}

	public int deleteApproval(String id, String project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if ((validateRole(caller, "admin", null)) || (validateRole(caller, "manager", project)))
		{
			try {
				String addSql = "update pm_approvals " + 
						"set status_cd = 'D' where approval_id = ?";

				numRowsAdded = jt.update(addSql, 
						id);

				if (numRowsAdded ==0)
					throw new I2B2DAOException("approval not updated, does it exist?");				
			} catch (DataAccessException e) {
				log.error("Dao deleteuser failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows deleted: " + numRowsAdded);

		return numRowsAdded;

	}


	public List<DBInfoType> getAllProject(String project, String caller) throws I2B2Exception, I2B2DAOException { 
		String sql = null;
		List<DBInfoType> queryResult = null;

		if (validateRole(caller, "admin", null))
		{
			sql =  "select * from pm_project_data where status_cd<>'D'";
			queryResult = jt.query(sql, getProject());
		}

		return queryResult;	
	}

	public int setProject(final ProjectType groupdata, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if (validateRole(caller, "admin", null))
		{

			if ((getProject(groupdata, false) == null) || (getProject(groupdata, false).size() == 0))
			{
				String addSql = "insert into pm_project_data " + 
						"(project_id, project_name, project_key, project_path, project_description, project_wiki, changeby_char, change_date, entry_date, status_cd) values (?,?,?,?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, 
						groupdata.getId(),
						groupdata.getName(),
						groupdata.getKey(),
						groupdata.getPath(),
						groupdata.getDescription(),						
						groupdata.getWiki(),
						caller,
						Calendar.getInstance().getTime(),
						Calendar.getInstance().getTime(),				
						"A");
			} else 
			{
				//project already exists, lets try to update
				String addSql = "update pm_project_data " + 
						"set project_name = ?, project_key = ?, project_wiki = ?, project_description = ?, project_path = ?, changeby_char = ?, change_date = ?,  status_cd = 'A' where project_id = ?";

				numRowsAdded = jt.update(addSql, 
						groupdata.getName(),
						groupdata.getKey(),
						groupdata.getWiki(),
						groupdata.getDescription(),
						groupdata.getPath(),
						caller,
						Calendar.getInstance().getTime(),
						groupdata.getId());					
			}
		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}

	public List<DBInfoType> getAllParam(Object utype, String project, String caller) throws I2B2Exception, I2B2DAOException { 
		String sql = null;
		List<DBInfoType> queryResult = null;

		//		if (validateRole(caller, "admin", null))
		//	{
		if (utype instanceof ProjectType)
		{
			if (((ProjectType) utype).getUserName() == null)
			{
				sql =  "select * from pm_project_params where status_cd<>'D' and project_id = ? order by project_id";
				queryResult = jt.query(sql, getParam(), ((ProjectType) utype).getId());

			} else 
			{
				ArrayList al = new ArrayList();

				sql = "select * from pm_project_user_params where status_cd<>'D' ";

				if (((ProjectType) utype).getUserName() != null) // || !((UserType) utype).getUserName().equals(""))
				{
					sql += " and user_id=?";
					al.add(((ProjectType) utype).getUserName());
					//sql =  "select * from pm_user_params where status_cd<>'D' order by user_id";
					//queryResult = jt.query(sql, getUserParams());				
				} 

				if (((ProjectType) utype).getPath() != null)
				{
					sql +=  " and project_id=?";
					al.add(((ProjectType) utype).getPath());

				}
				if (((ProjectType) utype).getParam() != null)
				{
					for (int i=0; i < ((ProjectType) utype).getParam().size(); i++)
					{
						if (((ProjectType) utype).getParam().get(i).getName() != null)
						{
							sql +=  " and param_name_cd=?";
							al.add((((ProjectType) utype).getParam().get(i).getName()));
						}
						if (((ProjectType) utype).getParam().get(i).getValue() != null)
						{
							sql +=  " and value=?";
							al.add((((ProjectType) utype).getParam().get(i).getValue()));
						}

					}
				}
				log.debug("My SQL: " + sql);
				queryResult = jt.query(sql, getParam(), al.toArray());

				/*
				if (((ProjectType) utype).getUserName() != null && !((ProjectType) utype).getUserName().equals("") )
				{
					sql =  "select * from pm_project_user_params where project_id=? and user_id = ? and status_cd<>'D'";
					queryResult = jt.query(sql, getParam(), ((ProjectType) utype).getId(), ((ProjectType) utype).getUserName());

				} else {
					sql =  "select * from pm_project_params where project_id=? and status_cd<>'D'";
					queryResult = jt.query(sql, getParam(), ((ProjectType) utype).getId());
				}
				 */
			}
		}
		else if (utype instanceof UserType)
		{
			ArrayList al = new ArrayList();

			sql = "select * from pm_user_params where status_cd<>'D' ";

			if (((UserType) utype).getUserName() != null) // || !((UserType) utype).getUserName().equals(""))
			{
				sql += " and user_id=?";
				al.add(((UserType) utype).getUserName());
				//sql =  "select * from pm_user_params where status_cd<>'D' order by user_id";
				//queryResult = jt.query(sql, getUserParams());				
			} 

			if (((UserType) utype).getParam() != null)
			{
				for (int i=0; i < ((UserType) utype).getParam().size(); i++)
				{
					if (((UserType) utype).getParam().get(i).getName() != null)
					{
						sql +=  " and param_name_cd=?";
						al.add((((UserType) utype).getParam().get(i).getName()));
					}
					if (((UserType) utype).getParam().get(i).getValue() != null)
					{
						if (database.equalsIgnoreCase("oracle"))
							sql +=  " and to_char(value)=?";
						else
							sql +=  " and value=?";
							
						al.add((((UserType) utype).getParam().get(i).getValue()));
					}

				}
			}
			queryResult = jt.query(sql, getUserParams(), al.toArray());
		}
		else if (utype instanceof ApprovalType)
		{
			if (((ApprovalType) utype).getId() == null)
			{
				sql =  "select * from pm_approvals where  status_cd<>'D' order by id";
				queryResult = jt.query(sql, getApproval());
			} else {
				sql =  "select * from pm_approvals_params where id=? and status_cd<>'D'";
				queryResult = jt.query(sql, getParam(), ((ApprovalType) utype).getId());
			}
		}
		else if (utype instanceof ConfigureType)
		{
			if (((ConfigureType) utype).getDomainId() == null)
			{
				sql =  "select * from pm_hive_data where status_cd<>'D' order by domain_id";
				queryResult = jt.query(sql, getEnvironment());
			} else {
				sql =  "select * from pm_hive_params where domain_id=? and status_cd<>'D'";
				queryResult = jt.query(sql, getParam(), ((ConfigureType) utype).getDomainId());
			}
		}
		else if (utype instanceof GlobalDataType)
		{
			if (((GlobalDataType) utype).getProjectPath() == null)
			{
				sql =  "select * from pm_global_params where  status_cd<>'D'";
				queryResult = jt.query(sql, getParam());
			}
			else
			{
				sql =  "select * from pm_global_params where project_path = ? and status_cd<>'D'";
				queryResult = jt.query(sql, getParam(), ((GlobalDataType) utype).getProjectPath());

			}
		}
		else if (utype instanceof CellDataType)
		{
			if (((CellDataType) utype).getProjectPath() == null)
			{
				sql =  "select * from pm_cell_params where status_cd<>'D' order by project_path";
				queryResult = jt.query(sql, getParam());

			} else {
				sql =  "select * from pm_cell_params where project_path=? and cell_id=?  and status_cd<>'D'";
				queryResult = jt.query(sql, getParam(), ((CellDataType) utype).getProjectPath(), ((CellDataType) utype).getId());
			}
		}
		else if (utype instanceof RoleType)
		{
			String addsql = " and user_id = '" + caller + "' ";
			if ((validateRole(caller, "admin", null)) || (validateRole(caller, "manager", project)))
			{
				addsql = "";				
			}
			if (((RoleType) utype).getProjectId() == null)
			{
				sql =  "select * from pm_project_user_roles where status_cd<>'D' " + addsql + " order by project_id";
				queryResult = jt.query(sql, getRole());

			} else if (((RoleType) utype).getUserName() != null)
			{
				sql =  "select * from pm_project_user_roles where project_id=? and user_id=? and status_cd<>'D' " + addsql + " order by project_id";
				queryResult = jt.query(sql, getRole(), ((RoleType) utype).getProjectId(), ((RoleType) utype).getUserName());

			}  
			else {
				sql =  "select * from pm_project_user_roles where project_id=? and status_cd<>'D' " + addsql;
				queryResult = jt.query(sql, getRole(), ((RoleType) utype).getProjectId());
			}
			//	}

		}
		return queryResult;	
	}


	public List<DBInfoType> getParam(Object utype, boolean showStatus) throws I2B2Exception, I2B2DAOException { 
		//		log.info(sql + domainId + projectId + ownerId);
		List<DBInfoType> queryResult = null;
		try {
			if (utype instanceof ProjectType)
			{
				if (((ProjectType) utype).getUserName() != null && !((ProjectType) utype).getUserName().equals("") )
				{
					String sql =  "select * from pm_project_user_params where id=?  " + 	(showStatus == false? "" :" and status_cd<>'D'");

					if (((ProjectType) utype).getParam().get(0).getId() != null)
						queryResult = jt.query(sql, getParam(), 						
								((ProjectType) utype).getParam().get(0).getId());

				} else {
					String sql =  "select * from pm_project_params where id=?  " + 	(showStatus == false? "" :" and status_cd<>'D'");

					if (((ProjectType) utype).getParam().get(0).getId() != null)
						queryResult = jt.query(sql, getParam(), 						
								((ProjectType) utype).getParam().get(0).getId());
				}
			}
			else if (utype instanceof GlobalDataType)
			{
				String sql =  "select * from pm_global_params where id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

				if (((GlobalDataType) utype).getParam().get(0).getId() != null)
					queryResult = jt.query(sql, getGlobal(), 						
							((GlobalDataType) utype).getParam().get(0).getId());
			}
			else if (utype instanceof ApprovalType)
			{
				String sql =  "select * from pm_approval_params where id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

				if (((ApprovalType) utype).getParam().get(0).getId() != null)
					queryResult = jt.query(sql, getParam(), 						
							((UserType) utype).getParam().get(0).getId());
			}
			else if (utype instanceof UserType)
			{
				String sql =  "select * from pm_user_params where id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

				if (((UserType) utype).getParam().get(0).getId() != null)
					queryResult = jt.query(sql, getParam(), 						
							((UserType) utype).getParam().get(0).getId());
			}
			else if (utype instanceof CellDataType)
			{
				String sql =  "select * from pm_cell_params where id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

				if (((CellDataType) utype).getParam().get(0).getId() != null)
					queryResult = jt.query(sql, getParam(), 						
							((CellDataType) utype).getParam().get(0).getId());
			}
			else if (utype instanceof RoleType)
			{
				String sql =  "select * from pm_project_user_roles where project_id=? and user_id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

				queryResult = jt.query(sql, getRole(), 						
						((RoleType) utype).getProjectId(),
						((RoleType) utype).getUserName());

			}		
			else if (utype instanceof ConfigureType)
			{
				if (((ConfigureType) utype).getParam().isEmpty() == false) // || (((ConfigureType) utype).getDomainId()).size() == 0))
				{
					String sql =  "select * from pm_hive_params where id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

					if (((ConfigureType) utype).getParam().get(0).getId() != null)
						queryResult = jt.query(sql, getParam(), 						
								((ConfigureType) utype).getParam().get(0).getId());

				} else {
					String sql =  "select * from pm_hive_data where active = '1' and domain_id=? " + 	(showStatus == false? "" :" and status_cd<>'D'");

					if (((ConfigureType) utype).getParam().get(0).getId() != null)
						queryResult = jt.query(sql, getEnvironment(), 						
								((ConfigureType) utype).getDomainId());
				}
			}
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	public int setParam(Object utype, String project, String name, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;
		log.debug("Caller: "+ caller);
		log.debug("Project: " + project);

		if ((utype instanceof UserType) && (caller.equals(((UserType) utype).getUserName())))
		{
			log.debug("Searching for existing User Param");

			if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
			{
				String addSql = "insert into pm_user_params " + 
						"(user_id, datatype_cd, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
				log.debug("Ading new User Param");
				numRowsAdded = jt.update(addSql, 
						((UserType) utype).getUserName(),
						((UserType) utype).getParam().get(0).getDatatype(),
						((UserType) utype).getParam().get(0).getName(),
						((UserType) utype).getParam().get(0).getValue(),
						Calendar.getInstance().getTime(),
						Calendar.getInstance().getTime(),	
						caller,
						"A");
			} else 
			{
				//user already exists, lets try to update
				String addSql = "update pm_user_params " + 
						"set value = ?, datatype_cd = ?, change_date = ?,  status_cd = 'A' where changeby_char = ? and id = ? ";
				log.debug("Updating  User Param");

				numRowsAdded = jt.update(addSql, 
						((UserType) utype).getParam().get(0).getValue(),
						((UserType) utype).getParam().get(0).getDatatype(),
						Calendar.getInstance().getTime(),
						caller,
						((UserType) utype).getParam().get(0).getId());
                if (numRowsAdded == 0)
                    throw new I2B2DAOException("Record does not exist or access denied.");
				
			}
		}  		
		else if ((utype instanceof ProjectType) && (((ProjectType) utype).getUserName() != null) && (((ProjectType) utype).getUserName().equals(caller) ))
		{
			if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
			{
				String addSql = "insert into pm_project_user_params " + 
						"(project_id, user_id, datatype_cd, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, 
						((ProjectType) utype).getId(),
						((ProjectType) utype).getUserName(),								
						((ProjectType) utype).getParam().get(0).getDatatype(),
						((ProjectType) utype).getParam().get(0).getName(),
						((ProjectType) utype).getParam().get(0).getValue(),
						Calendar.getInstance().getTime(),
						Calendar.getInstance().getTime(),	
						caller,
						"A");
			} else 
			{
				//user already exists, lets try to update
				String addSql = "update pm_project_user_params " + 
						"set value = ?, datatype_cd = ?, change_date = ?,   status_cd = 'A' where changeby_char = ? and id = ?";

				numRowsAdded = jt.update(addSql, 
						((ProjectType) utype).getParam().get(0).getValue(),
						((ProjectType) utype).getParam().get(0).getDatatype(),
						Calendar.getInstance().getTime(),			
						caller,
						((ProjectType) utype).getParam().get(0).getId());
                if (numRowsAdded == 0)
                    throw new I2B2DAOException("Record does not exist or access denied.");
			}

		}	else if (validateRole(caller, "admin", null) || validateRole(caller, "manager", project))
		{
			if (utype instanceof ParamsType)
			{


			}	else if (utype instanceof UserType) 
			{
				log.debug("Searching for existing User Param");

				if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
				{
					String addSql = "insert into pm_user_params " + 
							"(user_id, datatype_cd, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
					log.debug("Ading new User Param");
					numRowsAdded = jt.update(addSql, 
							((UserType) utype).getUserName(),
							((UserType) utype).getParam().get(0).getDatatype(),
							((UserType) utype).getParam().get(0).getName(),
							((UserType) utype).getParam().get(0).getValue(),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),	
							caller,
							"A");
				} else 
				{
					//user already exists, lets try to update
					String addSql = "update pm_user_params " + 
							"set value = ?, datatype_cd = ?, change_date = ?, changeby_char = ?, status_cd = 'A' where id = ? ";
					log.debug("Updating  User Param");

					numRowsAdded = jt.update(addSql, 
							((UserType) utype).getParam().get(0).getValue(),
							((UserType) utype).getParam().get(0).getDatatype(),
							Calendar.getInstance().getTime(),
							caller,
							((UserType) utype).getParam().get(0).getId());
				}
			}		
			else if (utype instanceof ProjectType)
			{
				log.debug("Testing to see if username is set: " + ((ProjectType) utype).getUserName() );
				if ((((ProjectType) utype).getUserName() != null) && (!((ProjectType) utype).getUserName().equals("") ))
				{
					if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
					{
						String addSql = "insert into pm_project_user_params " + 
								"(project_id, user_id, datatype_cd, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?)";
						numRowsAdded = jt.update(addSql, 
								((ProjectType) utype).getId(),
								((ProjectType) utype).getUserName(),								
								((ProjectType) utype).getParam().get(0).getDatatype(),
								((ProjectType) utype).getParam().get(0).getName(),
								((ProjectType) utype).getParam().get(0).getValue(),
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),	
								caller,
								"A");
					} else 
					{
						//user already exists, lets try to update
						String addSql = "update pm_project_user_params " + 
								"set value = ?, datatype_cd = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where id = ?";

						numRowsAdded = jt.update(addSql, 
								((ProjectType) utype).getParam().get(0).getValue(),
								((ProjectType) utype).getParam().get(0).getDatatype(),
								Calendar.getInstance().getTime(),			
								caller,
								((ProjectType) utype).getParam().get(0).getId());
					}
				} else {
					if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
					{
						String addSql = "insert into pm_project_params " + 
								"(project_id, datatype_cd, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
						numRowsAdded = jt.update(addSql, 
								((ProjectType) utype).getId(),
								((ProjectType) utype).getParam().get(0).getDatatype(),
								((ProjectType) utype).getParam().get(0).getName(),
								((ProjectType) utype).getParam().get(0).getValue(),
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),	
								caller,
								"A");
					} else 
					{
						//user already exists, lets try to update
						String addSql = "update pm_project_params " + 
								"set value = ?, datatype_cd = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where id = ?";

						numRowsAdded = jt.update(addSql, 
								((ProjectType) utype).getParam().get(0).getValue(),
								((ProjectType) utype).getParam().get(0).getDatatype(),
								Calendar.getInstance().getTime(),			
								caller,
								((ProjectType) utype).getParam().get(0).getId());
					}
				}
			} else if (utype instanceof CellDataType)
			{
				if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
				{
					String addSql = "insert into pm_cell_params " + 
							"(cell_id, datatype_cd, project_path, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?)";
					numRowsAdded = jt.update(addSql, 
							((CellDataType) utype).getId(),
							((CellDataType) utype).getParam().get(0).getDatatype(),
							((CellDataType) utype).getProjectPath(),
							((CellDataType) utype).getParam().get(0).getName(),
							((CellDataType) utype).getParam().get(0).getValue(),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),		
							caller,
							"A");
				} else 
				{
					//user already exists, lets try to update
					String addSql = "update pm_cell_params " + 
							"set value = ?, datatype_cd = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where id = ?";

					numRowsAdded = jt.update(addSql, 
							((CellDataType) utype).getParam().get(0).getValue(),
							((CellDataType) utype).getParam().get(0).getDatatype(),
							Calendar.getInstance().getTime(),
							caller,
							((CellDataType) utype).getParam().get(0).getId());
				}
			} else if (utype instanceof ApprovalType)
			{
				if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
				{
					String addSql = "insert into pm_approval_params " + 
							"(approval_id, datatype_cd, object_cd, param_name_cd, value, activation_date, expiration_date, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?,?,?)";
					numRowsAdded = jt.update(addSql, 
							((ApprovalType) utype).getId(),
							((ApprovalType) utype).getParam().get(0).getDatatype(),
							((ApprovalType) utype).getObjectCd(),
							((ApprovalType) utype).getParam().get(0).getName(),
							((ApprovalType) utype).getParam().get(0).getValue(),
							((ApprovalType) utype).getActivationDate(),
							((ApprovalType) utype).getExpirationDate(),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),		
							caller,
							"A");
				} else 
				{
					//user already exists, lets try to update
					String addSql = "update pm_approval_params " + 
							"set value = ?, datatype_cd = ?, change_date = ?, changeby_char = ?,  status_cd = 'A' where id = ?";

					numRowsAdded = jt.update(addSql, 
							((ApprovalType) utype).getParam().get(0).getValue(),
							((ApprovalType) utype).getParam().get(0).getDatatype(),
							Calendar.getInstance().getTime(),
							caller,
							((ApprovalType) utype).getParam().get(0).getId());
				}
			} else if (utype instanceof GlobalDataType)
			{
				if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
				{
					String addSql = "insert into pm_global_params " + 
							"(  param_name_cd, datatype_cd, project_path, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
					numRowsAdded = jt.update(addSql, 
							((GlobalDataType) utype).getParam().get(0).getName(),
							((GlobalDataType) utype).getParam().get(0).getDatatype(),
							((GlobalDataType) utype).getProjectPath(),
							((GlobalDataType) utype).getParam().get(0).getValue(),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),				
							caller,
							"A");
				} else 
				{
					//user already exists, lets try to update
					String addSql = "update pm_global_params " + 
							"set value = ?, datatype_cd = ?, project_path = ?, change_date = ?, changeby_char = ?, status_cd = 'A' where id = ?";

					numRowsAdded = jt.update(addSql, 
							((GlobalDataType) utype).getParam().get(0).getValue(),
							((GlobalDataType) utype).getParam().get(0).getDatatype(),
							((GlobalDataType) utype).getProjectPath(),
							Calendar.getInstance().getTime(),	
							caller,
							((GlobalDataType) utype).getParam().get(0).getId());
				}
			} else if (utype instanceof ConfigureType)
			{
				if (((ConfigureType) utype).getParam().isEmpty() == false) // || (((ConfigureType) utype).getDomainId()).size() == 0))
				{
					if ((getParam(utype, false) == null) || (getParam(utype, false).size() == 0))
					{
						String addSql = "insert into pm_hive_params " + 
								"(domain_id, datatype_cd, param_name_cd, value, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?)";
						log.debug("Ading new Hive Param");
						numRowsAdded = jt.update(addSql, 
								((ConfigureType) utype).getDomainId(),
								((ConfigureType) utype).getParam().get(0).getDatatype(),
								((ConfigureType) utype).getParam().get(0).getName(),
								((ConfigureType) utype).getParam().get(0).getValue(),
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),	
								caller,
								"A");
					} else 
					{
						//user already exists, lets try to update
						String addSql = "update pm_hive_params " + 
								"set value = ?, datatype_cd = ?, change_date = ?, changeby_char = ?, status_cd = 'A' where id = ? ";
						log.debug("Updating  Hive Param");

						numRowsAdded = jt.update(addSql, 
								((ConfigureType) utype).getParam().get(0).getValue(),
								((ConfigureType) utype).getParam().get(0).getDatatype(),
								Calendar.getInstance().getTime(),
								caller,
								((ConfigureType) utype).getParam().get(0).getId());
					}
				}
				else if (((ConfigureType) utype).getDomainId() == null) // || (((ConfigureType) utype).getDomainId()).size() == 0))
				{
					//user already exists, lets try to update

					String addSql = "";
					if ((((ConfigureType) utype).isActive() != null) && (((ConfigureType) utype).isActive() == true) ) {
						addSql = "update pm_hive_data " + 
								"set  status_cd = 'D', active = 0, change_date = ?, changeby_char = ? where status_cd = 'A'";

						numRowsAdded = jt.update(addSql, 
								Calendar.getInstance().getTime(),	
								caller);
					}

					addSql = "insert into pm_hive_data " + 
							"(  domain_id, environment_cd, domain_name, helpurl, active, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?,?,?)";
					numRowsAdded = jt.update(addSql, 
							PMUtil.getInstance().generateMessageId(),
							((ConfigureType) utype).getEnvironment(),
							((ConfigureType) utype).getDomainName(),
							((ConfigureType) utype).getHelpURL(),
							((ConfigureType) utype).isActive(),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),				
							caller,
							"A");
				} else 
				{

					String addSql = "update pm_hive_data " + 
							"set environment_cd = ?,  domain_name = ?,  helpurl = ?,  active = ?, change_date = ?, changeby_char = ?, status_cd = 'A' where domain_id = ?";

					numRowsAdded = jt.update(addSql, 
							((ConfigureType) utype).getEnvironment(),
							((ConfigureType) utype).getDomainName(),
							((ConfigureType) utype).getHelpURL(),
							((ConfigureType) utype).isActive(),
							Calendar.getInstance().getTime(),	
							caller,
							((ConfigureType) utype).getDomainId());
				}				
			} else if (utype instanceof RoleType)
			{
				try 
				{ 
					// First try to insert if fails than update
					String addSql2 = "insert into pm_project_user_roles " + 
							"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
					numRowsAdded += jt.update(addSql2, 
							((RoleType) utype).getProjectId().trim(),
							((RoleType) utype).getUserName().trim(),
							((RoleType) utype).getRole().trim(),
							Calendar.getInstance().getTime(),
							Calendar.getInstance().getTime(),
							caller,
							"A");
				} catch (Exception e) {
					String addSql2 = "update pm_project_user_roles " + 
							" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
					numRowsAdded += jt.update(addSql2, 
							Calendar.getInstance().getTime(),
							caller,
							((RoleType) utype).getProjectId(),
							((RoleType) utype).getUserName(),
							((RoleType) utype).getRole());
				}

				try {
					if ((((RoleType) utype).getRole().equals("DATA_AGG")) || (((RoleType) utype).getRole().equals("DATA_LDS")) ||
							(((RoleType) utype).getRole().equals("DATA_DEID")) || (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								((RoleType) utype).getProjectId().trim(),
								((RoleType) utype).getUserName().trim(),
								"DATA_OBFSC",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					}
				} catch (Exception e) {
					if ((((RoleType) utype).getRole().equals("DATA_AGG")) || (((RoleType) utype).getRole().equals("DATA_LDS")) ||
							(((RoleType) utype).getRole().equals("DATA_DEID")) || (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_OBFSC");	
					}
				}					

				try {
					if ((((RoleType) utype).getRole().equals("DATA_LDS"))  ||
							(((RoleType) utype).getRole().equals("DATA_DEID")) || (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_AGG",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					}
				} catch (Exception e) {
					if ((((RoleType) utype).getRole().equals("DATA_LDS"))  ||
							(((RoleType) utype).getRole().equals("DATA_DEID")) || (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_AGG");	
					}
				}					

				try {					
					if ((((RoleType) utype).getRole().equals("DATA_DEID")) || (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_LDS",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					}
				} catch (Exception e) {
					if ((((RoleType) utype).getRole().equals("DATA_DEID")) || (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_LDS");	
					}
				}

				try {
					if ( (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_DEID",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					}
				} catch (Exception e) {
					if ( (((RoleType) utype).getRole().equals("DATA_PROT"))) 
					{
						String addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"DATA_DEID");	
					}
				}


				//admin track
				try {
					if ((((RoleType) utype).getRole().equals("MANAGER")) ||(((RoleType) utype).getRole().equals("ADMIN")) ) 
					{
						String addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"USER",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					}
				} catch (Exception e) {
					//admin track
					if ((((RoleType) utype).getRole().equals("MANAGER")) ||(((RoleType) utype).getRole().equals("ADMIN")) ) 
					{
						String addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"USER");	
					}
				}					

				try {
					if ((((RoleType) utype).getRole().equals("ADMIN")) ) 
					{
						String addSql = "insert into pm_project_user_roles " + 
								"(  project_id, user_id, user_role_cd, change_date, entry_date, changeby_char, status_cd) values (?,?,?,?,?,?,?)";
						numRowsAdded += jt.update(addSql, 
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"MANAGER",
								Calendar.getInstance().getTime(),
								Calendar.getInstance().getTime(),
								caller,
								"A");
					}	
				} catch (Exception e) {
					if ((((RoleType) utype).getRole().equals("ADMIN")) ) 
					{
						String addSql = "update pm_project_user_roles " + 
								" set status_cd = 'A', change_date = ?, changeby_char = ?  where  project_id = ? and user_id = ? and user_role_cd = ?";
						numRowsAdded += jt.update(addSql, 
								Calendar.getInstance().getTime(),
								caller,
								((RoleType) utype).getProjectId(),
								((RoleType) utype).getUserName(),
								"MANAGER");	
					}
				} 
			}

		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}


	public int deleteParam(Object utype, final String project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;
		if (validateRole(caller, "admin", null) || validateRole(caller, "manager", project))
		{
			try {

				if (utype instanceof UserType)
				{
					String addSql = "update pm_user_params " + 
							"set status_cd = 'D', change_date = ?, changeBy_char = ? where id = ?";

					numRowsAdded = jt.update(addSql, 
							Calendar.getInstance().getTime(),
							caller,
							((UserType) utype).getParam().get(0).getId());
				} else

					if (utype instanceof ProjectType)
					{
						if ((((ProjectType) utype).getUserName() != null) && (!((ProjectType) utype).getUserName().equals("") ))
						{
							String addSql = "update pm_project_user_params " + 
									"set status_cd = 'D', change_date = ?, changeby_char = ? where id = ?";

							numRowsAdded = jt.update(addSql, 
									Calendar.getInstance().getTime(),				
									caller,
									((ProjectType) utype).getParam().get(0).getId());

						} else {
							String addSql = "update pm_project_params " + 
									"set status_cd = 'D', change_date = ?, changeby_char = ? where id = ?";

							numRowsAdded = jt.update(addSql, 
									Calendar.getInstance().getTime(),				
									caller,
									((ProjectType) utype).getParam().get(0).getId());
						}
					} else if (utype instanceof ConfigureType)
					{

						if (((ConfigureType) utype).getParam().isEmpty() == false) // || (((ConfigureType) utype).getDomainId()).size() == 0))
						{
							String addSql = "update pm_hive_params " + 
									"set status_cd = 'D', change_date = ?, changeby_char = ? where id = ?";

							numRowsAdded = jt.update(addSql, 
									Calendar.getInstance().getTime(),				
									caller,
									((ConfigureType) utype).getParam().get(0).getId());

						} else {
							String addSql = "update pm_hive_data " + 
									"set status_cd = 'D', change_date = ?, changeby_char = ? where domain_id = ? and active = 0";

							numRowsAdded = jt.update(addSql, 
									Calendar.getInstance().getTime(),				
									caller,
									((ConfigureType) utype).getDomainId());
						}
					} else if (utype instanceof CellDataType)
					{
						String addSql = "update pm_cell_params " + 
								"set status_cd = 'D', change_date = ?, changeby_char = ? where id = ?";

						numRowsAdded = jt.update(addSql, 
								Calendar.getInstance().getTime(),				
								caller,
								((CellDataType) utype).getParam().get(0).getId());
					} else if (utype instanceof ApprovalType)
					{
						String addSql = "update pm_approval_params " + 
								"set status_cd = 'D', change_date = ?, changeby_char = ? where id = ?";

						numRowsAdded = jt.update(addSql, 
								Calendar.getInstance().getTime(),				
								caller,
								((ApprovalType) utype).getParam().get(0).getId());
					} else if (utype instanceof GlobalDataType)
					{
						String addSql = "update pm_global_params " + 
								"set status_cd = 'D', change_date = ?, changeby_char = ? where id = ?";

						numRowsAdded = jt.update(addSql, 
								Calendar.getInstance().getTime(),				
								caller,
								((GlobalDataType) utype).getParam().get(0).getId());
					}  else if (utype instanceof RoleType)
					{

						if (((RoleType) utype).getRole().equals("DATA_PROT"))
						{
							numRowsAdded += executeRemoveRole("DATA_PROT", caller, utype);
						}
						else if (((RoleType) utype).getRole().equals("DATA_DEID")) 
						{
							numRowsAdded += executeRemoveRole("DATA_PROT", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_DEID", caller, utype);
						}
						else if (((RoleType) utype).getRole().equals("DATA_LDS")) 
						{
							numRowsAdded += executeRemoveRole("DATA_PROT", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_DEID", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_LDS", caller, utype);
						}
						else if (((RoleType) utype).getRole().equals("DATA_AGG")) 
						{
							numRowsAdded += executeRemoveRole("DATA_PROT", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_DEID", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_LDS", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_AGG", caller, utype);
						}					
						else if (((RoleType) utype).getRole().equals("DATA_OBFSC")) 
						{
							numRowsAdded += executeRemoveRole("DATA_PROT", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_DEID", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_LDS", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_AGG", caller, utype);
							numRowsAdded += executeRemoveRole("DATA_OBFSC", caller, utype);
						}					
						//admin track
						if ((((RoleType) utype).getRole().equals("MANAGER")) ) 
						{
							numRowsAdded += executeRemoveRole("MANAGER", caller, utype);
						}					
						else if ((((RoleType) utype).getRole().equals("USER")) ) 
						{
							numRowsAdded += executeRemoveRole("MANAGER", caller, utype);
							numRowsAdded += executeRemoveRole("USER", caller, utype);
						} else 
						{
							numRowsAdded += executeRemoveRole(((RoleType) utype).getRole(), caller, utype);
						}	

					}

				if (numRowsAdded == 0)
					throw new I2B2DAOException("not updated, does it exist?");
			} catch (DataAccessException e) {
				log.error("Dao deleteuser failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		} else if ((utype instanceof UserType) && (caller.equals(((UserType) utype).getUserName())))
		{
			String addSql = "update pm_user_params " + 
					"set status_cd = 'D', change_date = ?  where user_id = ? and changeby_char = ? and id = ?";

			numRowsAdded = jt.update(addSql, 
					Calendar.getInstance().getTime(),
					caller,
					caller,
					((UserType) utype).getParam().get(0).getId());
            if (numRowsAdded == 0)
                throw new I2B2DAOException("Record does not exist or access denied.");
		} else if ((utype instanceof ProjectType) && (((ProjectType) utype).getUserName() != null) && (((ProjectType) utype).getUserName().equals(caller) ))
		{
			String addSql = "update pm_project_user_params " + 
					"set status_cd = 'D', change_date = ? where user_id = ? and changeby_char = ? and id = ?";

			numRowsAdded = jt.update(addSql, 
					Calendar.getInstance().getTime(),				
					caller,
					caller,
					((ProjectType) utype).getParam().get(0).getId());
            if (numRowsAdded == 0)
                throw new I2B2DAOException("Record does not exist or access denied.");

		} 
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows deleted: " + numRowsAdded);

		return numRowsAdded;

	}

	public int executeRemoveRole(String role, String caller, Object utype)
	{
		String addSql = "update pm_project_user_roles " + 
				"set status_cd = 'D', change_date = ?, changeby_char = ? where user_role_cd = ? and project_id = ? and user_id = ?";

		return  jt.update(addSql, 
				Calendar.getInstance().getTime(),				
				caller,
				role,
				((RoleType) utype).getProjectId(),
				((RoleType) utype).getUserName());
	}

	public int deleteProject(final Object project, String caller) throws I2B2DAOException, I2B2Exception{
		int numRowsAdded = 0;

		if (validateRole(caller, "admin", null))
		{
			try {
				String addSql = "update pm_project_data " + 
						"set status_cd = 'D', change_date = ? where project_id = ? and project_path = ? and changeby_char = ?";

				numRowsAdded = jt.update(addSql, 
						Calendar.getInstance().getTime(),
						((ProjectType) project).getId(),
						((ProjectType) project).getPath(),
						caller
						);

				if (numRowsAdded == 0)
					throw new I2B2DAOException("Project not updated, does it exist?");
			} catch (DataAccessException e) {
				log.error("Dao deleteuser failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		else 
		{
			throw new I2B2DAOException("Access Denied for " + caller);
		}
		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows deleted: " + numRowsAdded);

		return numRowsAdded;

	}


	private ParameterizedRowMapper getEnvironmentParams() {
		ParameterizedRowMapper<HiveParamData> map = new ParameterizedRowMapper<HiveParamData>() {
			public HiveParamData mapRow(ResultSet rs, int rowNum) throws SQLException {
				HiveParamData eData = new HiveParamData();
				eData.setDomain(rs.getString("domain_id"));
				eData.setName(rs.getString("param_name_cd"));
				eData.setValue(rs.getString("value"));

				return eData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getProject() {
		ParameterizedRowMapper<ProjectType> map = new ParameterizedRowMapper<ProjectType>() {
			public ProjectType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProjectType rData = new ProjectType();
				DTOFactory factory = new DTOFactory();
				rData.setKey(rs.getString("project_key"));
				rData.setName(rs.getString("project_name"));
				rData.setPath(rs.getString("project_path"));
				rData.setDescription(rs.getString("project_description"));
				rData.setId(rs.getString("project_id"));
				rData.setWiki(rs.getString("project_wiki"));
				return rData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getCell() {
		ParameterizedRowMapper<CellDataType> map = new ParameterizedRowMapper<CellDataType>() {
			public CellDataType mapRow(ResultSet rs, int rowNum) throws SQLException {
				CellDataType rData = new CellDataType();
				DTOFactory factory = new DTOFactory();
				rData.setId(rs.getString("cell_id"));
				rData.setName(rs.getString("name"));
				rData.setProjectPath(rs.getString("project_path"));
				rData.setCanOverride(rs.getBoolean("can_override"));
				rData.setMethod(rs.getString("method_cd"));
				rData.setUrl(rs.getString("url"));
				return rData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getProjectRequest() {
		ParameterizedRowMapper<ProjectRequestType> map = new ParameterizedRowMapper<ProjectRequestType>() {
			public ProjectRequestType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProjectRequestType rData = new ProjectRequestType();
				DTOFactory factory = new DTOFactory();
				rData.setId(Integer.toString(rs.getInt("id")));
				rData.setProjectId(rs.getString("project_id"));
				rData.setTitle(rs.getString("title"));
				rData.setSubmitChar(rs.getString("submit_char"));
				Date date = rs.getDate("entry_date");

				if (date == null)
					rData.setEntryDate(null);
				else 
					rData.setEntryDate(long2Gregorian(date.getTime())); 

				rData.setRequestXml(rs.getString("request_xml"));
				/*
				Clob clob = rs.getClob("request_xml");

				if (clob != null) {
					try {
						BlobType blobType = new BlobType();
						blobType.getContent().add(
								JDBCUtil.getClobString(clob));
						rData.setRequestXml(blobType);
					} catch (IOException ioe)
					{
						log.debug(ioe.getMessage());
					}
				}
				*/
				//rData.setRequestXml(rs.getClob("request_xml"));
				return rData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getApproval() {
		ParameterizedRowMapper<ApprovalType> map = new ParameterizedRowMapper<ApprovalType>() {
			public ApprovalType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ApprovalType rData = new ApprovalType();
				DTOFactory factory = new DTOFactory();
				rData.setId(rs.getString("approval_id"));
				rData.setName(rs.getString("approval_name"));
				rData.setDescription(rs.getString("approval_description"));
				rData.setObjectCd(rs.getString("object_cd"));
				Date date = rs.getDate("approval_activation_date");

				if (date == null)
					rData.setActivationDate(null);
				else 
					rData.setActivationDate(long2Gregorian(date.getTime())); 

				date = rs.getDate("approval_expiration_date");
				if (date == null)
					rData.setExpirationDate(null);
				else 
					rData.setExpirationDate(long2Gregorian(date.getTime())); 


				return rData;
			} 
		};
		return map;
	}
	private ParameterizedRowMapper getParam() {
		ParameterizedRowMapper<ParamType> map = new ParameterizedRowMapper<ParamType>() {
			public ParamType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ParamType eData = new ParamType();
				log.debug("setting name");
				eData.setName(rs.getString("param_name_cd"));
				eData.setValue(rs.getString("value"));
				eData.setId(rs.getInt("id"));
				eData.setDatatype(rs.getString("datatype_cd"));
				return eData;
			} 
		};
		return map;
	}

	public static XMLGregorianCalendar long2Gregorian(long date) {
		DatatypeFactory dataTypeFactory;
		try {
			dataTypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date);
		return dataTypeFactory.newXMLGregorianCalendar(gc);
	}

	private ParameterizedRowMapper getGlobal() {
		ParameterizedRowMapper<GlobalDataType> map = new ParameterizedRowMapper<GlobalDataType>() {
			public GlobalDataType mapRow(ResultSet rs, int rowNum) throws SQLException {
				DTOFactory factory = new DTOFactory();

				GlobalDataType eData = new GlobalDataType();

				log.debug("setting name");
				ParamType param = new ParamType();
				param.setId(rs.getInt("id"));
				param.setName(rs.getString("param_name_cd"));
				param.setValue(rs.getString("value"));
				param.setDatatype(rs.getString("datatype_cd"));
				eData.getParam().add(param);
				eData.setProjectPath(rs.getString("project_path"));
				eData.setCanOverride(rs.getBoolean("can_override"));
				return eData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getUserParams() {
		ParameterizedRowMapper<UserParamData> map = new ParameterizedRowMapper<UserParamData>() {
			public UserParamData mapRow(ResultSet rs, int rowNum) throws SQLException {
				UserParamData eData = new UserParamData();
				eData.setId(rs.getInt("id"));
				eData.setDatatype(rs.getString("datatype_cd"));
				eData.setUser(rs.getString("user_id"));
				eData.setName(rs.getString("param_name_cd"));
				eData.setValue(rs.getString("value"));
				log.debug("Found a user/param: " + rs.getString("user_id") + ":" + rs.getString("param_name_cd"));
				return eData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getProjectUserParams() {
		ParameterizedRowMapper<ProjectUserParamData> map = new ParameterizedRowMapper<ProjectUserParamData>() {
			public ProjectUserParamData mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProjectUserParamData eData = new ProjectUserParamData();
				eData.setProject(rs.getString("project_path"));
				eData.setUser(rs.getString("user_id"));
				eData.setName(rs.getString("param_name"));
				eData.setValue(rs.getString("value"));

				return eData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getProjectParams() {
		ParameterizedRowMapper<ParamType> map = new ParameterizedRowMapper<ParamType>() {
			public ParamType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ParamType eData = new ParamType();
				//eData.setProject(rs.getString("project_path"));
				eData.setName(rs.getString("param_name_cd"));
				eData.setValue(rs.getString("value"));

				return eData;
			} 
		};
		return map;
	}


	private ParameterizedRowMapper getSession() {
		ParameterizedRowMapper<SessionData> map = new ParameterizedRowMapper<SessionData>() {
			public SessionData mapRow(ResultSet rs, int rowNum) throws SQLException {
				SessionData rData = new SessionData();
				//				DTOFactory factory = new DTOFactory();

				rData.setSessionID(rs.getString("session_id"));

				Date date = rs.getTimestamp("expired_date");
				if (date == null)
					rData.setExpiredDate(null);
				else 
					rData.setExpiredDate(date); 

				date = rs.getTimestamp("entry_date");
				if (date == null)
					rData.setIssuedDate(null);
				else 
					rData.setIssuedDate(date); 


				return rData;
			} 
		};
		return map;
	}
	

	private ParameterizedRowMapper getUserLogin() {
		ParameterizedRowMapper<SessionData> map = new ParameterizedRowMapper<SessionData>() {
			public SessionData mapRow(ResultSet rs, int rowNum) throws SQLException {
				SessionData rData = new SessionData();
				//				DTOFactory factory = new DTOFactory();

				rData.setSessionID(rs.getString("session_id"));

				Date date = rs.getTimestamp("expired_date");
				if (date == null)
					rData.setExpiredDate(null);
				else 
					rData.setExpiredDate(date); 

				date = rs.getTimestamp("entry_date");
				if (date == null)
					rData.setIssuedDate(null);
				else 
					rData.setIssuedDate(date); 


				return rData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getRole() {
		ParameterizedRowMapper<RoleType> map = new ParameterizedRowMapper<RoleType>() {
			public RoleType mapRow(ResultSet rs, int rowNum) throws SQLException {
				RoleType rData = new RoleType();
				rData.setProjectId(rs.getString("project_id"));
				rData.setUserName(rs.getString("user_id"));
				rData.setRole(rs.getString("user_role_cd"));

				return rData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getEnvironment() {
		ParameterizedRowMapper<ConfigureType> map = new ParameterizedRowMapper<ConfigureType>() {
			public ConfigureType mapRow(ResultSet rs, int rowNum) throws SQLException {
				DTOFactory factory = new DTOFactory();
				ConfigureType eData = new ConfigureType();
				eData.setActive(rs.getBoolean("active"));
				eData.setDomainId(rs.getString("domain_id"));
				eData.setDomainName(rs.getString("domain_name"));
				eData.setHelpURL(rs.getString("helpurl"));
				eData.setEnvironment(rs.getString("environment_cd"));

				return eData;
			} 
		};
		return map;
	}

	private ParameterizedRowMapper getUser(final boolean includePassword) {
		ParameterizedRowMapper<UserType> map = new ParameterizedRowMapper<UserType>() {
			public UserType mapRow(ResultSet rs, int rowNum) throws SQLException {
				DTOFactory factory = new DTOFactory();
				UserType userData = new UserType();
				userData.setFullName(rs.getString("full_name"));
				userData.setUserName(rs.getString("user_id"));
				try {
					userData.setIsAdmin(validateRole(userData.getUserName(), "ADMIN",null));
				} catch (I2B2DAOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (includePassword) {
					PasswordType pass = new PasswordType();
					pass.setValue(rs.getString("password"));
					userData.setPassword(pass);
				}
				userData.setEmail(rs.getString("email"));

				return userData;
			} 
		};
		return map;
	}

}
