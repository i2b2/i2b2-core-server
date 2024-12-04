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
 * 		Lori Phillips
 */
package edu.harvard.i2b2.workplace.dao;

import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Date;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.workplace.dao.GetFolderMapper;
import edu.harvard.i2b2.workplace.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.datavo.wdo.AnnotateChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.ChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.DblookupType;
import edu.harvard.i2b2.workplace.datavo.wdo.DeleteChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.ExportChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetChildrenType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetReturnType;
import edu.harvard.i2b2.workplace.datavo.wdo.ProtectedType;
import edu.harvard.i2b2.workplace.datavo.wdo.RenameChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.FindByChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.XmlValueType;
import edu.harvard.i2b2.workplace.delegate.crc.CallCRCUtil;
import edu.harvard.i2b2.workplace.ejb.DBInfoType;
import edu.harvard.i2b2.workplace.util.StringUtil;
import edu.harvard.i2b2.workplace.util.WorkplaceUtil;


public class FolderDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(FolderDao.class);
	protected static Logger logesapi = ESAPI.getLogger(FolderDao.class);

	//    final static String CORE = " c_hierarchy, c_hlevel, c_name, c_user_id, c_group_id, c_share_id, c_index, c_parent_index, c_visualattributes, c_tooltip";
	//	final static String DEFAULT = " c_name, c_hierarchy";
	final static String CORE = " c_name, c_user_id, c_group_id, c_protected_access, c_share_id, c_index, c_parent_index, c_visualattributes, c_tooltip";
	final static String DEFAULT = " c_name, c_index, c_protected_access ";
	final static String ALL = CORE + ", c_entry_date, c_change_date, c_status_cd";
	final static String BLOB = ", c_work_xml, c_work_xml_schema, c_work_xml_i2b2_type ";

	private JdbcTemplate jt;
	private String metadataSchema;

	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = WorkplaceUtil.getInstance().getDataSource(dataSource);
			//			metadataSchema = ds.getConnection().getSchema() + ".";
			Connection conn = ds.getConnection();

			metadataSchema = conn.getSchema() + ".";
			conn.close();
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		this.jt = new JdbcTemplate(ds);
	}

	private String getMetadataSchema() throws I2B2Exception{

		return metadataSchema; //WorkplaceUtil.getInstance().getMetaDataSchemaName();
	}


	public List findRootFoldersByProject(final GetReturnType returnType, final String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = CORE;		
		if (returnType.getType().equals("core")){
			parameters = CORE;
		}
		/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
		 */

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		// 1. check if user already has a folder
		//       if not create one.
		check_addRootNode(metadataSchema, userId, projectInfo, dbInfo);


		//		 First step is to call PM to see what roles user belongs to.

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		//ParameterizedRowMapper<FolderType> mapper = getMapper(returnType.getType(), false, null, dbInfo.getDb_serverType());

		GetFolderMapper mapper = GetTableMapper(returnType.getType(), false, null, dbInfo.getDb_serverType());


		List<FolderType> queryResult = null;		
		if (!protectedAccess){
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where c_protected_access = ? and LOWER(c_group_id) = ? order by c_name"; //c_hierarchy";

			try {
				queryResult = jt.query(tablesSql, mapper, "N", projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error");
			}
		}
		else{
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where LOWER(c_group_id) = ? order by c_name"; //c_hierarchy";

			try {
				queryResult = jt.query(tablesSql, mapper, projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		log.debug("result size = " + queryResult.size());

		return queryResult;
	}

	public List findRootFoldersByUser(final GetReturnType returnType, final String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = CORE;		
		if (returnType.getType().equals("core")){
			parameters = CORE;
		}
		/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
		 */

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());


		// 1. check if user already has a folder
		//       if not create one.
		check_addRootNode(metadataSchema, userId, projectInfo, dbInfo);


		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}


		GetFolderMapper mapper  = GetTableMapper(returnType.getType(), false, null, dbInfo.getDb_serverType());		

		List<FolderType> queryResult = null;		
		if (!protectedAccess){
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where (c_share_id = 'Y' and LOWER(c_group_id) = ?) or (c_protected_access = ? and LOWER(c_user_id) = ? and LOWER(c_group_id) = ?) order by c_name"; //c_hierarchy";

			//		log.info(tablesSql);
			try {
				queryResult = jt.query(tablesSql, mapper, projectInfo.getId().toLowerCase(), "N",userId.toLowerCase(), projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error findRootFoldersByUser");
			}
		}
		else{
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where (c_share_id = 'Y' and LOWER(c_group_id) = ?) or (LOWER(c_user_id) = ? and LOWER(c_group_id) = ?) order by c_name"; //c_hierarchy";
			try {
				queryResult =  jt.query(tablesSql, 
						GetTableMapper(returnType.getType(), false, null, dbInfo.getDb_serverType())
						, projectInfo.getId().toLowerCase(), userId.toLowerCase(), projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error findRootFoldersByUser 2");
			}
		}
		/* 
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
 	            return name;
	        }
		};
	if(queryResult.size() == 0){
			// this means that user is accessing for first time
			// grab tableCd tableName pair 
			//   and then insert an entry for the user
			String tablesSql = "select distinct(c_table_cd), c_table_name from " +  metadataSchema +  "workplace_access"; 

			try {
				queryResult = jt.query(tablesSql, map);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
			if(queryResult.size() == 0)
				throw new I2B2DAOException("Database Error");
			else{
				queryResult= addRootNode((String)queryResult.get(0), userId, projectInfo, dbInfo);
			}	
		}*/
		log.debug("result size = " + queryResult.size());

		return queryResult;
	}

	public void check_addRootNode(String metadataSchema, String userId, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String entriesSql = "select c_name  from " +  metadataSchema +  "workplace_access where LOWER(c_user_id) = ? and LOWER(c_group_id) = ?"; 

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = rs.getString("c_name") ;
				return name;
			}
		};

		String map = jt.queryForObject(entriesSql,String.class);
		 */
		List<String> queryResult = null;
		try {
			//queryResult = jt.query(entriesSql, map, userId.toLowerCase(), projectInfo.getId().toLowerCase());
			queryResult = jt.queryForList(entriesSql, String.class, userId.toLowerCase(), projectInfo.getId().toLowerCase());

		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			log.error(e1.getMessage());
			throw new I2B2DAOException("Database Error check_addRootNode");
		}
		//		log.info("check for root node size = " + queryResult.size());
		if(queryResult.size() > 0)
			return;

		// else queryResult is empty
		//    need to create a new entry for user

		//1. get ProtectedAccess status for user
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		String protectedAccess = "N";
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equals("protected_access")) {
				protectedAccess = "Y";
				break;
			}
		}
		// 2. Get tableCd tableName info 
		String tableSql = "select distinct(c_table_cd), c_table_name from " +  metadataSchema +  "workplace_access"; 

		/*
		ParameterizedRowMapper<String> map2 = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
				return name;
			}
		};
		 */
		String tableInfo = jt.queryForObject(tableSql, new getFolderTableMapper());
		//String tableInfo = (String)queryResult.get(0);

		//extract table code and table name
		String tableCd = StringUtil.getTableCd(tableInfo);
		String tableName = StringUtil.getIndex(tableInfo);

		String addSql = "insert into " + metadataSchema+ "workplace_access "  + 
				"(c_table_cd, c_table_name, c_hlevel, c_protected_access, c_name, c_user_id, c_index, c_visualattributes, c_share_id, c_group_id, c_entry_date) values (?,?,?,?,?,?,?,?,?,?,?)";

		int numRootsAdded = -1;
		String index = StringUtil.generateMessageId();
		try {	
			numRootsAdded = jt.update(addSql, tableCd, tableName, 0, protectedAccess,
					userId, userId,index, "CA", "N", projectInfo.getId(), Calendar.getInstance().getTime()); 

		} catch (DataAccessException e) {
			log.error("Dao addChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error check_addRootNode" , e);
		}

		//		log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of roots added: " + numRootsAdded);

		return;

	}

	public String exportNode(final ExportChildType childrenType, ProjectType projectInfo, SecurityType securityType) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String type = "core";
		String parameters = CORE;		



		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}
		String queryResult = null;

		// Get the query master for the actual query run
		if (childrenType.getType().equalsIgnoreCase("QM")) {
			try {
				log.debug("Start to get QM results from CRC");
				//CallCRCUtil callCRC = new CallCRCUtil(securityType, projectInfo.getId());
				log.debug("getting Response");
				queryResult =  CallCRCUtil.callCRCQueryRequestXML(childrenType.getNode(), securityType, projectInfo.getId());
				logesapi.debug(null,"got response: " + queryResult);
				//if (masterInstanceResultResponseType != null && masterInstanceResultResponseType.getQueryMaster().size() > 0)
				//	queryResult =XMLUtil.convertDOMElementToString((Element) masterInstanceResultResponseType.getQueryMaster().get(0).getRequestXml().getContent().get(0)); ;  //respoonseType.getQueryResultInstance();
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error exportNode");
			}
			//log.debug("result size = " + queryResult.size());
			// Get the analysis breakdowns
		} else 	if (childrenType.getType().equalsIgnoreCase("QR")) {
			try {
				log.debug("Start to get QR results from CRC");
				//CallCRCUtil callCRC = new CallCRCUtil(securityType, projectInfo.getId());
				log.debug("getting Response");
				queryResult =  CallCRCUtil.callCRCResultInstanceXML(childrenType.getNode(), securityType, projectInfo.getId());
				logesapi.debug(null,"got response: " + queryResult);
				//if (masterInstanceResultResponseType != null)
				//	queryResult = (String) masterInstanceResultResponseType.getCrcXmlResult().getXmlValue().getContent().get(0);
				//XMLUtil.convertDOMElementToString((Element) masterInstanceResultResponseType.getCrcXmlResult().getXmlValue().getContent().get(0));  //respoonseType.getQueryResultInstance();
				//queryResult = jt.query(sql, mapper, parentIndex );
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error exportNode2");
			}
			//log.debug("result size = " + queryResult.size());
		}
		logesapi.debug(null,"result is: " + queryResult);
		return queryResult;

	}

	public List findChildrenByParent(final GetChildrenType childrenType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String type = "core";
		String parameters = CORE;		
		if (childrenType.getType().equals("all")){
			parameters = ALL;
			type = "all";
		}
		if(childrenType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};
		 */

		//extract table code
		String tableCd = StringUtil.getTableCd(childrenType.getParent());
		//	log.debug(tableCd);
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			//log.info("getChildren " + tableSql + tableCd);
			try {
				//tableName = jt.queryForObject(tableSql, map, tableCd, "N");	  
				tableName = jt.queryForObject(tableSql,String.class, tableCd, "N");	 
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error findChildrenByParent" );
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				//tableName = jt.queryForObject(tableSql, map, tableCd);	
				tableName = jt.queryForObject(tableSql,String.class, tableCd);	 

			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error findChildrenByParent 2");
			}
		}

		String hidden = "";
		if(childrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where  c_parent_index = ? and (c_status_cd != 'D' or c_status_cd is null)"; 
		sql = sql + hidden + " order by c_name ";

		String parentIndex = StringUtil.getIndex(childrenType.getParent());

		log.debug(sql + " " + parentIndex);
		//		log.info(type + " " + tableCd );

		GetFolderMapper mapper = GetTableMapper(type, childrenType.isBlob(), tableCd, dbInfo.getDb_serverType());

		List<FolderType> queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, parentIndex );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error findChildrenByParent 3");
		}
		log.debug("result size = " + queryResult.size());


		return queryResult;
		// tested statement with aqua data studio   verified output from above against this. 
		// select  c_fullname, c_name, c_synonym_cd, c_visualattributes  from metadata.testrpdr 
		// where c_fullname like '\RPDR\Diagnoses\Circulatory system (390-459)\Arterial vascular disease (440-447)\(446) Polyarteritis nodosa and al%' 
		// and c_hlevel = 5  and c_visualattributes not like '_H%' and c_synonym_cd = 'N'

		// verified both with and without hiddens and synonyms.

		// clob test   level = 4
		//   <parent>\\testrpdr\RPDR\HealthHistory\PHY\Health Maintenance\Mammogram\Mammogram - Deferred</parent> 
	}

	/**
	 * This method finds the workplace with a given keyword. It first searches the WORKPLACE ACCESS table
	 * to find the table where the workplace contents are stored. And then it searches the resulting table
	 * for any content with given name and other parameters
	 * 
	 * @param returnType
	 * @param userId
	 * @param projectInfo
	 * @param dbInfo
	 * @return
	 * @throws DataAccessException
	 * @throws I2B2Exception
	 * 
	 * @author Neha Patel
	 */
	public List findWorkplaceByKeyword(final FindByChildType returnType, String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String type = "core"; // Default Type is core
		String parameters = CORE; // parameters to be used in select statement 
		String category = "";
		String hiddenStr = "";
		String maxString = "";
		String searchWord = "";

		if (returnType!=null){ 

			// determines which columns should be used in select statement
			if(returnType.getType().equals("all")){
				parameters = ALL;
				type = "all";
			}

			// if request parameter blob is set to true then include 
			// columns with xml info in select statement :-
			// c_work_xml, c_work_xml_schema, c_work_xml_i2b2_type
			if(returnType.isBlob() == true)
				parameters = parameters + BLOB;

			// category is the directory where user is looking for the content
			category= returnType.getCategory();

			// request parameter hidden indicates to display hidden files or not
			if(returnType.isHiddens() == false)
				hiddenStr = " and c_visualattributes not like '_H%'";			

			// get strategy if content name starts with given word
			// or it contains given word or it ends with given word
			if(returnType.getMatchStr().getStrategy().equals("exact")) {
				searchWord = returnType.getMatchStr().getValue().toLowerCase();
			}

			else if(returnType.getMatchStr().getStrategy().equals("left")){
				searchWord = returnType.getMatchStr().getValue().toLowerCase()+ "%";
			}

			else if(returnType.getMatchStr().getStrategy().equals("right")) {
				searchWord ="%"+ returnType.getMatchStr().getValue().toLowerCase();
			}

			else if(returnType.getMatchStr().getStrategy().equals("contains")) {
				searchWord =  "%" +	returnType.getMatchStr().getValue().toLowerCase() + "%";
			}

			try {
				// setting max number of rows to be returned
				if(returnType.getMax() !=null && returnType.getMax()>0 && dbInfo!=null){
					int fetchSize = returnType.getMax() +1 ;

					// if server is oracle then use rownum to return max number of rows
					if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE"))				
						maxString = " and rownum>0 and rownum <=" + fetchSize; 

					// if server is SQL SERVER then use 'TOP' clause to return max number of rows 
					else if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
						maxString = "TOP " + fetchSize + " "; 
						parameters = maxString + parameters; // appended maxstring infront of parameters
						maxString = "";
					} 
					//else 	if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL"))				
					//	maxString = " limit " + fetchSize; 

				}
			}
			catch( Exception e){
				log.error(e);
			}
		}

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		if(returnType.getCategory().trim().equalsIgnoreCase("@")){
			return findInAll(returnType, projectInfo, type, parameters, hiddenStr, maxString, searchWord, protectedAccess, dbInfo, userId );
		}
		else {
			return findInCategory(returnType, projectInfo, type, parameters, category, hiddenStr, maxString, searchWord, dbInfo, protectedAccess);
		}

	}

	/**
	 * This method finds the search word in the given category.
	 *  
	 * @param returnType
	 * @param projectInfo
	 * @param type - parameters to be used in the select statement. Can have two values core or all
	 * @param parameters
	 * @param category
	 * @param hiddenStr - string to be used in where clause
	 * @param maxString - string to be used in select or where clause
	 * @param searchWord 
	 * @param dbInfo
	 * @param protectedAccess
	 * @return
	 * @throws I2B2DAOException
	 * 
	 * @author Neha Patel
	 */
	private List findInCategory(final FindByChildType returnType, final ProjectType projectInfo, String type, String parameters, String category, String hiddenStr, String maxString,
			String searchWord,  final DBInfoType dbInfo, boolean protectedAccess) throws I2B2DAOException {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
				return name;
			}
		};

		 */
		// Getting tablename where the content is saved from table 'WORKPLACE'
		List<String> queryResult = null;

		String resultStr=null;
		StringBuilder sqlToRetreiveTableNm = new StringBuilder( "select distinct c_table_cd, c_table_name from " + metadataSchema + "workplace_access where LOWER(c_user_id) = ? and LOWER(c_group_id) = ?");
		if (!protectedAccess){

			sqlToRetreiveTableNm.append(" and c_protected_access = ? ");
			try {
				queryResult = jt.query(sqlToRetreiveTableNm.toString(), new getFolderTableMapper(), category.toLowerCase(), projectInfo.getId().toLowerCase(), "N");	
				resultStr = (String)queryResult.get(0);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("findWorkplaceByKeyword(): Database Error while accessing workplace_access table with protected access");
			}
		}else {
			try {
				queryResult = jt.query(sqlToRetreiveTableNm.toString(), new getFolderTableMapper(), category.toLowerCase(), projectInfo.getId().toLowerCase());	
				resultStr = (String)queryResult.get(0);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("findWorkplaceByKeyword(): Database Error while accessing workplace_access table");
			}
		}

		String tableCd = StringUtil.getTableCd(resultStr);
		String tableName = StringUtil.getIndex(resultStr);

		StringBuilder sql = new StringBuilder ("select " + parameters +" from " + metadataSchema+tableName  + " where LOWER(c_user_id) = ? and LOWER(c_group_id) = ? and LOWER(c_name) like ? and (c_status_cd != 'D' or c_status_cd is null) "); 
		sql.append( hiddenStr + maxString );

		GetFolderMapper mapper = GetTableMapper(type, returnType.isBlob(), tableCd, dbInfo.getDb_serverType());


		/*
		 * commenting out protectedAcess code from workplace table for now
		 * 		
		if (!protectedAccess){
			sql.append(" and (c_protected_access != 'Y' or c_protected_access is null) ");
		}
		 */

		sql.append( " order by c_name ");

		// Executing the query to find the workplace content with the given name 
		List<FolderType> queryResult2;

		try {
			queryResult2 = jt.query(sql.toString(), mapper, category.toLowerCase(), projectInfo.getId().toLowerCase(), searchWord );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			log.error("findWorkplaceByKeyword(): Database Error while accessing workplace table");
			throw new I2B2DAOException("findWorkplaceByKeyword(): Database Error while accessing workplace table");
		}

		log.debug("result size = " + queryResult2.size());

		return queryResult2;
	}

	/**
	 * This method searches for the word in the whole project. If the user has a manager role then it searches in the whole project
	 * if the user doesn't have manager role then it searches with the condition of userid or share = Y
	 * 
	 * @param returnType
	 * @param projectInfo
	 * @param type
	 * @param parameters
	 * @param hiddenStr
	 * @param maxString
	 * @param searchWord
	 * @param protectedAccess
	 * @param dbInfo
	 * @param userId
	 * @return
	 * @throws DataAccessException
	 * @throws I2B2Exception
	 * 
	 * @author Neha Patel
	 */
	private List findInAll(final FindByChildType returnType, final ProjectType projectInfo, String type, String parameters, String hiddenStr, String maxString,
			String searchWord, boolean protectedAccess, final DBInfoType dbInfo, final String userId) throws DataAccessException, I2B2Exception{


		// Check if user is a manager
		boolean managerRole = false;
		for(String param :projectInfo.getRole()) {
			if(param.equalsIgnoreCase("manager")) {
				managerRole = true;
				break;
			}
		}		

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
				return name;
			}
		};
		 */

		// Getting tablename where the content is saved from table 'WORKPLACE'
		List queryResult = null;

		String resultStr=null;
		StringBuilder sqlToRetreiveTableNm = new StringBuilder( "select distinct c_table_cd, c_table_name from " + metadataSchema + "workplace_access where ");

		// if user is a manager then search in the whole project
		if(managerRole){
			sqlToRetreiveTableNm.append("LOWER(c_group_id) = ? ");
		}
		else {
			// if user is not a manager then user should be able to search only in his folder or shared folder of the project 	
			sqlToRetreiveTableNm.append("(LOWER(c_user_id) = ? and LOWER(c_group_id) = ?) or (LOWER(c_group_id) = ? and c_share_id = 'Y') ");
		}

		if (!protectedAccess){
			sqlToRetreiveTableNm.append(" and (c_protected_access = 'N' or c_protected_access is null) ");
		}

		try {
			if(managerRole){
				queryResult = jt.queryForList(sqlToRetreiveTableNm.toString(), new getFolderTableMapper(), projectInfo.getId().toLowerCase());
			}
			else {
				queryResult = jt.queryForList(sqlToRetreiveTableNm.toString(), new getFolderTableMapper(), userId.toLowerCase(), projectInfo.getId().toLowerCase(),  projectInfo.getId().toLowerCase());
			}
			resultStr = (String)queryResult.get(0);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("findWorkplaceByKeyword(): Database Error while accessing workplace_access table with protected access");
		}

		String tableCd = "";
		String tableName = "";
		List returnResult = null;

		// Run the query for each tablename. There could be more than one table where workplace content is stored
		if(queryResult != null && !queryResult.isEmpty()){
			Iterator itr = queryResult.iterator();
			while(itr.hasNext()){
				resultStr = (String)itr.next();
				tableCd = StringUtil.getTableCd(resultStr);
				tableName = StringUtil.getIndex(resultStr);

				StringBuilder sql = new StringBuilder ("select <parameters> from <from> where LOWER(c_name) like ? and (c_status_cd != 'D' or c_status_cd is null) "); 

				if(managerRole){
					sql.append("and LOWER(c_group_id) = ? ");
				}
				else {
					sql.append("and ((LOWER(c_user_id) = ? and LOWER(c_group_id) = ?) or (LOWER(c_group_id) = ? and c_share_id = 'Y')) ");
				}

				sql.append( hiddenStr + maxString );

				GetFolderMapper mapper = GetTableMapper(type, returnType.isBlob(), tableCd, dbInfo.getDb_serverType());

				/*
				 * commenting out protectedAcess code from workplace table for now
				 * 		
				if (!protectedAccess){
					sql.append(" and (c_protected_access != 'Y' or c_protected_access is null) ");
				}
				 */

				sql.append( " order by c_name ");

				// Executing the query to find the workplace content with the given name 
				List<FolderType> workplaceResult=null;

				try {
					
					String sqlFinal = sql.toString().replace("<from>", metadataSchema	+ tableName);
					sqlFinal = sqlFinal.toString().replace("<parameters>", parameters);
					

					if(managerRole){
						workplaceResult = jt.query(sqlFinal, mapper, searchWord, projectInfo.getId().toLowerCase() );
					}
					else {
						workplaceResult = jt.query(sqlFinal, mapper, searchWord, userId.toLowerCase(), projectInfo.getId().toLowerCase(), projectInfo.getId().toLowerCase() );
					}
				} catch (DataAccessException e) {
					log.error(e.getMessage());
					log.error("findWorkplaceByKeyword(): Database Error while accessing workplace table");
					throw new I2B2DAOException("findWorkplaceByKeyword(): Database Error while accessing workplace table");
				}

				if(returnResult==null){
					returnResult=workplaceResult;
				}
				else {
					returnResult.addAll(workplaceResult);
				}
			} // end while (itr.hasNext())

		}
		log.debug("result size = " + returnResult.size());

		return returnResult;
	}

	/**
	 * This method determines if the given category(workplace root folder name) is shared or not by 
	 * checking c_share_id parameter in workplace_access table
	 * 
	 * @param category - the root folder name which is in question if its shared or not
	 * @param projectInfo 
	 * @param dbInfo
	 * @return
	 * @throws DataAccessException
	 * @throws I2B2Exception
	 * 
	 * @author Neha Patel
	 */
	public boolean isShared(String category, final ProjectType projectInfo, final DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		boolean isSharedBool= false;
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = rs.getString("c_share_id") ;
				return name;
			}
		};
		 */

		// Getting column 'c_share_id' to check if the given category/folder is shared
		List queryResult = null;
		String resultStr="";

		String sqlForCheckingShared = "select c_share_id from " + metadataSchema + "workplace_access where LOWER(c_user_id) = ? and LOWER(c_group_id) = ? and (c_status_cd != 'D' or c_status_cd is null)";

		try {
			//queryResult = jt.query(sqlForCheckingShared.toString(), map, category.toLowerCase(), projectInfo.getId().toLowerCase());	
			queryResult = jt.queryForList(sqlForCheckingShared.toString(), String.class, category.toLowerCase(), projectInfo.getId().toLowerCase());	

			if(queryResult !=null && !queryResult.isEmpty()){
				resultStr = (String)queryResult.get(0);
			}
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("isShared(): Database Error while accessing workplace_access table");
		}

		if(resultStr!=null && resultStr.toUpperCase().trim().equals("Y")){
			isSharedBool = true;
		}
		else 
			isSharedBool = false;

		return isSharedBool;
	}

	public int renameNode(final RenameChildType renameChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};
		 */

		//extract table code
		String tableCd = StringUtil.getTableCd(renameChildType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String index = StringUtil.getIndex(renameChildType.getNode());

		// get original name and work xml
		String sql = "select c_name, c_work_xml, c_work_xml_i2b2_type from " + metadataSchema + tableName + " where c_index = ? ";
		/*
		ParameterizedRowMapper<FolderType> map2 = new ParameterizedRowMapper<FolderType>() {
			@Override
			public FolderType mapRow(ResultSet rs, int rowNum) throws SQLException {
				FolderType child = new FolderType();
				child.setName(rs.getString("c_name"));
				//	            child.setTooltip(rs.getString("c_tooltip"));
				child.setWorkXmlI2B2Type(rs.getString("c_work_xml_i2b2_type"));

				return child;
			}
		};
		 */
		List queryResult = null;
		try {
			queryResult = jt.query(sql, new mapToFolderXML(), index);  
		} catch (DataAccessException e) {
			log.error("Dao queryResult failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
		FolderType node = (FolderType)queryResult.get(0);

		//		String newTooltip = StringUtil.replaceEnd(node.getTooltip(),node.getName(), renameChildType.getName());
		//		log.info(newTooltip);
		int numRowsRenamed = -1;
		if(node.getWorkXmlI2B2Type().equals("FOLDER")){
			String updateSql = "update " + metadataSchema+tableName  + " set c_name = ? where c_index = ? ";
			try {
				numRowsRenamed = jt.update(updateSql, renameChildType.getName(),index);
			} catch (DataAccessException e) {
				log.error("Dao renameChild failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		else {
			String updateSql = "update " + metadataSchema+tableName  + " set c_name = ? where c_index = ? ";

			/*
			String newXml = null;
			//			Element newXmlElement = node.getWorkXml().getAny().get(0);
			Element newXmlElement = renameChildType.getWorkXml().getAny().get(0);
			if(newXmlElement != null){
				newXml = XMLUtil.convertDOMElementToString(newXmlElement);
				//				log.debug(newXml);				
			}
			 */
			try {
				numRowsRenamed = jt.update(updateSql, renameChildType.getName(), index);
			} catch (DataAccessException e) {
				log.error("Dao renameChild failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		log.debug("Number of rows renamed: " + numRowsRenamed);
		return numRowsRenamed;

	}

	public int moveNode(final ChildType childType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};
		 */

		//extract table code
		String tableCd = StringUtil.getTableCd(childType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String index = StringUtil.getIndex(childType.getNode());		
		String updateSql = "update " + metadataSchema+tableName  + " set c_parent_index = ? where c_index = ? ";

		int numRowsMoved = -1;
		try {
			numRowsMoved = jt.update(updateSql, childType.getParent(), index);
		} catch (DataAccessException e) {
			log.error("Dao moveChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
		//		log.info(updateSql + " " + path + " " + numRowsAnnotated);
		log.debug("Number of rows moved: " + numRowsMoved);
		return numRowsMoved;

	}

	public int annotateNode(final AnnotateChildType annotateChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};
		 */

		//extract table code
		String tableCd = StringUtil.getTableCd(annotateChildType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String index = StringUtil.getIndex(annotateChildType.getNode());	
		String updateSql = "update " + metadataSchema+tableName  + " set c_tooltip = ? where c_index = ? ";

		int numRowsAnnotated = -1;
		try {
			numRowsAnnotated = jt.update(updateSql, annotateChildType.getTooltip(), index);
		} catch (DataAccessException e) {
			log.error("Dao annotateChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
		//		log.info(updateSql + " " + path + " " + numRowsAnnotated);
		log.debug("Number of rows annotated: " + numRowsAnnotated);
		return numRowsAnnotated;

	}


	public int addNode(final FolderType addChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};
		 */

		//extract table code
		String tableCd = StringUtil.getTableCd(addChildType.getParentIndex());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(tableSql + tableCd);
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		int numRowsAdded = -1;
		try {
			String xml = null;
			XmlValueType workXml=addChildType.getWorkXml();
			if (workXml != null) {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_name, c_user_id, c_index, c_parent_index, c_visualattributes, c_group_id, c_share_id, c_tooltip, c_entry_date, c_work_xml, c_work_xml_i2b2_type) values (?,?,?,?,?,?,?,?,?,?,?)";
				Element element = workXml.getAny().get(0);
				if(element != null)
					xml = XMLUtil.convertDOMElementToString(element);
				numRowsAdded = jt.update(addSql, 
						addChildType.getName(), addChildType.getUserId(),addChildType.getIndex(), StringUtil.getIndex(addChildType.getParentIndex()), 
						addChildType.getVisualAttributes(), addChildType.getGroupId(), addChildType.getShareId(), addChildType.getTooltip(),  Calendar.getInstance().getTime(),
						xml, addChildType.getWorkXmlI2B2Type()); 
			}		
			else {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_name, c_user_id, c_index, c_parent_index, c_visualattributes, c_group_id, c_share_id, c_tooltip, c_entry_date, c_work_xml_i2b2_type) values (?,?,?,?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, 
						addChildType.getName(), addChildType.getUserId(),addChildType.getIndex(), StringUtil.getIndex(addChildType.getParentIndex()), 
						addChildType.getVisualAttributes(), addChildType.getGroupId(), addChildType.getShareId(), addChildType.getTooltip(),  Calendar.getInstance().getTime(),
						addChildType.getWorkXmlI2B2Type()); 
			}
		} catch (DataAccessException e) {
			log.error("Dao addChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}


	public int deleteNode(final DeleteChildType deleteChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
		String metadataSchema = dbInfo.getDb_fullSchema();
		String serverType = dbInfo.getDb_serverType();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};
		 */
		//extract table code
		String tableCd = StringUtil.getTableCd(deleteChildType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		String index = StringUtil.getIndex(deleteChildType.getNode());	
		checkForChildrenDeletion(index, tableName, metadataSchema);
		//Mark node for deletion

		String updateSql = " update " + metadataSchema+tableName  + " set c_change_date = ?, c_status_cd = 'D'  where c_index = ? ";
		log.debug(serverType + "updateSql " + index);
		int numRowsDeleted = -1;
		try {
			//		log.info(sql + " " + w_index);
			numRowsDeleted = jt.update(updateSql, Calendar.getInstance().getTime(),index);
		} catch (DataAccessException e) {
			log.error("Dao deleteChild failed");
			log.error(e.getMessage());
			throw e;
		}
		log.debug("Number of rows deleted " + numRowsDeleted);
		return numRowsDeleted;

	}

	/**
	 * This method is to set protected access on a file/folder in workplace
	 * It first checks if user has correct privileges to the file, that is
	 * either he she is manager or the file is shared or the file belongs
	 * to him/her. The it searches for all the folders under the given 
	 * index. if folders are found then it runs the update query atleast
	 * 3 times to update the root folder in workplace_access table, all the
	 * child folders in workplace table and all the child content in workplace
	 * table.  
	 * 
	 * @param requestType
	 * @param projectInfo
	 * @param dbInfo
	 * @param userId
	 * @return
	 * @throws I2B2DAOException
	 * @throws I2B2Exception
	 * 
	 * @author Neha Patel
	 */
	public int setProtectedAccess(final ProtectedType requestType, final ProjectType projectInfo, final DBInfoType dbInfo, String userId) throws I2B2DAOException, I2B2Exception{

		boolean settingRoot = false;
		int numRowsSet = -1;
		int numParentUpdated = -1;
		int numWorkAccUpdated =-1;
		String sharedStr ="";
		String contentUserId = "";
		String tableName = "";
		boolean managerRole = false;
		boolean isFolder = true;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		// Check if user is a manager
		for(String param :projectInfo.getRole()) {
			if(param.equalsIgnoreCase("manager")) {
				managerRole = true;
				break;
			}
		}

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {

				String resultRow = "\\tablename=" +  rs.getString("c_table_name") + "\\share_id=" + rs.getString("c_share_id") + "\\user_id=" + rs.getString("c_user_id") ;

				return resultRow;
			}
		};
		 */

		//extract table code and index
		String tableCd = StringUtil.getTableCd(requestType.getIndex());
		String index = StringUtil.getIndex(requestType.getIndex());

		List resultString = null;
		StringBuilder sqlToRetrieveTableName = new StringBuilder("select distinct c_table_name, c_share_id, c_user_id from " 	+ metadataSchema 
				+ "workplace_access where LOWER(c_group_id) = ?");	

		// Check if the user is setting access for root directory
		// by looking for index in the current table
		try {
			sqlToRetrieveTableName.append(" and c_index = ? ");
			resultString = jt.query(sqlToRetrieveTableName.toString(), new mapToShareAccess(),projectInfo.getId().toLowerCase(), index);	    
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String resultToSplit= "";
		// if the above query returned any result
		// that means user was setting access for root directory
		if(resultString!=null && !resultString.isEmpty()){

			settingRoot = true;
			isFolder = true;

			// getting tablename, share_id, user_id from the result string
			resultToSplit = (String) resultString.get(0);
			int indexofShared = resultToSplit.indexOf("\\share_id=");
			int indexofUser = resultToSplit.indexOf("\\user_id=");
			tableName = resultToSplit.substring(11, indexofShared);

			// if its not manager check if the file/folder is shared
			// if not shared either, then verify that user is setting
			// privilege for his/her file/folder
			if(managerRole == false){
				sharedStr = resultToSplit.substring(indexofShared+10, indexofUser);
				contentUserId = resultToSplit.substring(indexofUser+9);

				if ((!sharedStr.equalsIgnoreCase("Y")) && (!contentUserId.equalsIgnoreCase(userId))){
					log.debug( "User does not have privileges to set protected access for this content");
					return -11111;
				} // if (sharedStr==null || !sharedStr.equalsIgnoreCase("Y"))
			} // if managerRole == false
		} //if(resultString!=null && !resultString.isEmpty())			
		// query result is null that means item doesn't exist in workplace_access table
		// or user is not setting access for root directory
		// Get tablename using the tablecd given as part of indexString in the request
		else if(resultString == null || resultString.isEmpty()){

			// replace the last condition of 'and c_index=?' with 'and c_table_cd'
			sqlToRetrieveTableName.replace(sqlToRetrieveTableName.lastIndexOf("and"), sqlToRetrieveTableName.length()-1, " and LOWER(c_table_cd) = ? ");

			try {
				resultString = jt.query(sqlToRetrieveTableName.toString(), new mapToShareAccess(), projectInfo.getId().toLowerCase(), tableCd.toLowerCase());	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}

			resultToSplit = (String) resultString.get(0);

			// getting tablename from the query result
			tableName = resultToSplit.substring(11, resultToSplit.indexOf("\\share_id="));

			List result;
			/*
			ParameterizedRowMapper<String> mapTocheckAccess = new ParameterizedRowMapper<String>() {
				@Override
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {

					String resultRow = "\\share_id=" + rs.getString("c_share_id") + "\\user_id=" + rs.getString("c_user_id") + "\\type=" + rs.getString("c_work_xml_i2b2_type") ;

					return resultRow;
				}
			};
			 */

			// Run query in table workplace to find out if the content is shared or does it belong to user
			// Also find the type of the file
			String sql = "select  c_share_id, c_user_id, c_work_xml_i2b2_type from " + metadataSchema + tableName + " where c_index = ? and LOWER(c_group_id) = ?";
			try{	
				result = jt.query(sql, new mapTocheckAccess(), index, projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}

			// get the user id and share_id from result string
			resultToSplit = (String) result.get(0);
			String type = resultToSplit.substring(resultToSplit.lastIndexOf("\\")+6);

			if(!type.equalsIgnoreCase("FOLDER")){
				isFolder= false;
			}
			else 
				isFolder=true;

			// if user is not a manager
			// then check if file/folder is shared
			// if not shared then verify file/folder belongs to user
			if(managerRole == false){

				sharedStr = resultToSplit.substring(10, resultToSplit.indexOf("\\user_id="));
				contentUserId = resultToSplit.substring(resultToSplit.indexOf("\\user_id=")+9, resultToSplit.lastIndexOf("\\"));	

				if ((sharedStr!=null && !sharedStr.equalsIgnoreCase("Y")) && (!contentUserId.equalsIgnoreCase(userId))){
					log.debug( "User does not have privileges to set protected access for this content");
					return -11111;
				} // if (sharedStr==null || !sharedStr.equalsIgnoreCase("Y"))
			} // if managerRole == false		
		}

		StringBuilder indexStr = new StringBuilder();
		String protectedAccVal= "";

		if(requestType.getProtectedAccess().trim().equalsIgnoreCase("true"))
			protectedAccVal = "Y";
		else
			protectedAccVal = "N";

		ArrayList<String> parentIdxList = new ArrayList<String>();
		parentIdxList.add(index);
		indexStr.append("'" + index + "'");

		// if initial request was for a folder only
		// then run this part 
		if(isFolder){

			List resultingIndx;
			/*
			ParameterizedRowMapper<String> mapForIndexes = new ParameterizedRowMapper<String>() {
				@Override
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					String name = (rs.getString("c_index"));
					return name;
				}
			};
			 */

			// Get all the parent indexes (folder indexes under the top level directory)
			// and store it in an arraylist
			String parentIdx = "";
			for (int i = 0; i < parentIdxList.size(); i ++){
				try {
					parentIdx = parentIdxList.get(i);
					if(i>0){
						indexStr.append(", '" + parentIdx + "'");
					}
					String sqlToCollectIndex = "select c_index from " + metadataSchema + tableName + " where c_parent_index = ? and LOWER(c_group_id) = ? and c_work_xml_i2b2_type = 'FOLDER'";
					resultingIndx = jt.queryForList(sqlToCollectIndex, String.class, parentIdx, projectInfo.getId().toLowerCase());	    
				} catch (DataAccessException e) {
					log.error(e.getMessage());
					throw new I2B2DAOException("Database Error");
				}
				if(resultingIndx != null)
					parentIdxList.addAll(resultingIndx);	
			}

			// set the protected access for all the content found under the 
			// parent indexes stored in the arraylist
			numParentUpdated = updateProtectedAccess(metadataSchema,tableName, "c_parent_index" , indexStr.toString(), protectedAccVal);
		}

		if(settingRoot){
			// set the protected access for root directory which is in workplace_access table
			numWorkAccUpdated = updateProtectedAccess(metadataSchema,"workplace_access", "c_index",indexStr.toString(), protectedAccVal);
		}

		// If setting root folder, then set all the folders to protected access
		// if setting one item then still use the same query to set that item to protected_access
		numRowsSet = updateProtectedAccess(metadataSchema,tableName, "c_index",indexStr.toString(), protectedAccVal);

		// Return the correct number of updated rows 
		if(isFolder)
			numRowsSet += numParentUpdated;

		if(settingRoot)
			numRowsSet += numWorkAccUpdated;

		return numRowsSet;
	}

	/**
	 * @param numRowsSet
	 * @param metadataSchema
	 * @param tableName
	 * @param indexStr
	 * @param protectedAccVal
	 * @return
	 * @throws I2B2DAOException
	 * 
	 * @author Neha Patel
	 */
	private int updateProtectedAccess(String metadataSchema,String tableName, String columnName, String indexStr, String protectedAccVal)
			throws I2B2DAOException {

		String updateSql = "update <from> set c_protected_access = ? where <columnName> in ( <indexStr> )";
		int numRowsSet=-1;

		try {
			String sqlFinal = updateSql.replace("<from>", metadataSchema+tableName);
			sqlFinal = sqlFinal.replace("<columnName>", columnName);
			sqlFinal = sqlFinal.replace("<indexStr>", indexStr);
			
			String protectedAccValFinal = protectedAccVal.replace("<indexStr>", indexStr);

			numRowsSet = jt.update(sqlFinal, protectedAccValFinal);
		} catch (DataAccessException e) {
			log.error("Dao updateProtectedAccess failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
		return numRowsSet;
	}


	private void checkForChildrenDeletion(String nodeIndex, String tableName, String metadataSchema) throws DataAccessException {

		// mark children for deletion
		String updateSql = " update " + metadataSchema+tableName  + " set c_change_date = ?, c_status_cd = 'D'  where c_parent_index = ? ";
		int numChildrenDeleted = -1;
		try {
			//		log.info(sql + " " + w_index);
			numChildrenDeleted = jt.update(updateSql, Calendar.getInstance().getTime(),nodeIndex);
		} catch (DataAccessException e) {
			log.error("Dao deleteChild failed");
			log.error(e.getMessage());
			throw e;
		}
		log.debug("Number of children deleted: "+ numChildrenDeleted);
		// look for children that are folders
		String folderSql = "select c_index from " + metadataSchema+tableName + " where c_parent_index = ? and c_visualattributes like 'F%' ";

		/*
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String index = (rs.getString("c_index"));
				return index;
			}
		};
		 */

		List folders = null;
		try{
			folders = jt.queryForList(folderSql, String.class, nodeIndex);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		// recursively check folders for children to delete
		if (folders != null){
			Iterator it = folders.iterator();
			while(it.hasNext()){
				String folderIndex = (String) it.next();
				checkForChildrenDeletion(folderIndex, tableName, metadataSchema);
			}
		}

	}

	private GetFolderMapper GetTableMapper(final String type, final boolean isBlob, final String tableCd, final String dbType) {


		GetFolderMapper folderMapper = new GetFolderMapper();

		folderMapper.setType(type);
		folderMapper.setBlob(isBlob);
		folderMapper.setTableCd(tableCd);
		folderMapper.setDbType(dbType);
		return  folderMapper;
	}

}


class mapToShareAccess implements RowMapper<String> {
	//ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {

		String resultRow = "\\tablename=" +  rs.getString("c_table_name") + "\\share_id=" + rs.getString("c_share_id") + "\\user_id=" + rs.getString("c_user_id") ;

		return resultRow;
	}
};

class mapTocheckAccess implements RowMapper<String> {
	@Override
	//ParameterizedRowMapper<String> mapTocheckAccess = new ParameterizedRowMapper<String>() {
	//	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {

		String resultRow = "\\share_id=" + rs.getString("c_share_id") + "\\user_id=" + rs.getString("c_user_id") + "\\type=" + rs.getString("c_work_xml_i2b2_type") ;

		return resultRow;
	}
};

class mapToFolderXML implements RowMapper<FolderType> {
	//ParameterizedRowMapper<FolderType> map2 = new ParameterizedRowMapper<FolderType>() {
	@Override
	public FolderType mapRow(ResultSet rs, int rowNum) throws SQLException {
		FolderType child = new FolderType();
		child.setName(rs.getString("c_name"));
		//	            child.setTooltip(rs.getString("c_tooltip"));
		child.setWorkXmlI2B2Type(rs.getString("c_work_xml_i2b2_type"));

		return child;
	}
};


class getFolderTableMapper implements RowMapper<String> {
	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		String str =  "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
		return str;
	}

}

class getTableMapper implements RowMapper<DblookupType> {
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


class GetFolderMapper implements RowMapper<FolderType> {

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setBlob(boolean isBlob) {
		this.isBlob = isBlob;
	}
	public void setTableCd(String tableCd) {
		this.tableCd = tableCd;
	}
	String type;
	boolean isBlob;
	String tableCd;
	String dbType;
	@Override
	public FolderType mapRow(ResultSet rs, int rowNum) throws SQLException {



		//private ParameterizedRowMapper<FolderType> getMapper(final String type, final boolean isBlob, final String tableCd, final String dbType){

		FolderType child = new FolderType();
		//TODO fix this for all/+blob
		if (tableCd == null){
			//	            	child.setHierarchy("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_hierarchy")); 
			child.setIndex("\\\\" + rs.getString("c_table_cd")+ "\\" + rs.getString("c_index")); 
		}
		else{
			//	            	child.setHierarchy("\\\\" + tableCd + rs.getString("c_hierarchy")); 
			child.setIndex("\\\\" + tableCd + "\\" + rs.getString("c_index")); 
		}
		//      log.debug("getMapper: " + child.getIndex());
		child.setName(rs.getString("c_name"));

		child.setProtectedAccess(rs.getString("c_protected_access"));

		if(!(type.equals("default"))) {
			child.setUserId(rs.getString("c_user_id"));
			//         	child.setHlevel(rs.getInt("c_hlevel"));
			child.setGroupId(rs.getString("c_group_id"));
			child.setVisualAttributes(rs.getString("c_visualattributes"));
			//         	child.setIndex(rs.getString("c_index"));
			child.setParentIndex(rs.getString("c_parent_index"));
			child.setShareId(rs.getString("c_share_id" ));

			// Building tooltip for the response 
			// eg. project name - cname \n tooltip from db
			String toolTip = rs.getString("c_group_id") + " - " + rs.getString("c_name") ;
			if(rs.getString("c_tooltip")!=null && !rs.getString("c_tooltip").isEmpty()){
				toolTip = toolTip + "\n" + rs.getString("c_tooltip"); 
			}

			//child.setTooltip(rs.getString("c_tooltip"));
			child.setTooltip(toolTip);

		}if(isBlob == true){
			child.setWorkXmlI2B2Type(rs.getString("c_work_xml_i2b2_type"));

			String c_xml = null;
			try {
				if (dbType.equals("POSTGRESQL"))
				{
					c_xml = rs.getString("c_work_xml");
				} else
				{
					c_xml = rs.getString("c_work_xml");  //JDBCUtil.getClobString(rs.getClob("c_work_xml"));
				}
				if (c_xml != null){
					//c_xml = JDBCUtil.getClobString(xml_clob);
					if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
					{
						//SAXBuilder parser = new SAXBuilder();
						//java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
						Element rootElement = null;
						try {
							//org.jdom.Document metadataDoc = parser.build(xmlStringReader);
							Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
							//org.jdom.output.DOMOutputter out = new DOMOutputter(); 
							//Document doc = out.output(metadataDoc);
							rootElement = doc.getDocumentElement();
						} catch (IOException e1) {
							child.setWorkXml(null);
						}
						if (rootElement != null) {
							XmlValueType xml = new XmlValueType();
							xml.getAny().add(rootElement);
							child.setWorkXml(xml);
						}
						else {
							//        					log.debug("rootElement is null");
							child.setWorkXml(null);
						}
					}else {
						//   				log.debug("work xml is null");
						child.setWorkXml(null);
					}
				}
				else {
					//				log.debug("work xml is null");
					child.setWorkXml(null);
				}
			} catch (Exception e) {
				child.setWorkXml(null);
			} 

			try {
				String xml_schema_string = null;
				//column definition clob is in only oracle
				if (dbType.equals("ORACLE")) {
					Clob xml_schema_clob = rs.getClob("c_work_xml_schema");
					if (xml_schema_clob != null)
						xml_schema_string = JDBCUtil.getClobString(xml_schema_clob);

				} else {
					xml_schema_string = rs.getString("c_work_xml_schema");
				}
				if (xml_schema_string != null){
					c_xml = xml_schema_string;
					if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
					{
						Element rootElement = null;
						try {
							//org.jdom.Document metadataDoc = parser.build(xmlStringReader);
							Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
							//org.jdom.output.DOMOutputter out = new DOMOutputter(); 
							//Document doc = out.output(metadataDoc);
							rootElement = doc.getDocumentElement();

						} catch (IOException e1) {
							child.setWorkXmlSchema(null);
						}
						if (rootElement != null) {
							XmlValueType xml = new XmlValueType();
							xml.getAny().add(rootElement);
							child.setWorkXmlSchema(xml);
						}
						else {
							//            					log.debug("rootElement is null");
							child.setWorkXmlSchema(null);
						}
					}else {
						//           				log.debug("work xml schema is null");
						child.setWorkXmlSchema(null);
					}
				}
				else {
					//       				log.debug("work xml schema is null");
					child.setWorkXmlSchema(null);
				}
			} catch (Exception e) {
				child.setWorkXmlSchema(null);
			}
		}
		if((type.equals("all"))){
			DTOFactory factory = new DTOFactory();
			// make sure date isnt null before converting to XMLGregorianCalendar
			Date date = rs.getDate("c_entry_date");
			if (date == null)
				child.setEntryDate(null);
			else 
				child.setEntryDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("c_change_date");
			if (date == null)
				child.setChangeDate(null);
			else 
				child.setChangeDate(factory.getXMLGregorianCalendar(date.getTime())); 

			child.setStatusCd(rs.getString("c_status_cd"));

		}
		return child;
	}


}

