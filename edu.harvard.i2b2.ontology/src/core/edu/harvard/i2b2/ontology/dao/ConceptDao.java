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
package edu.harvard.i2b2.ontology.dao;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import antlr.StringUtils;
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetCategoriesType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifierChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifierInfoType;

import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifierType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifiersType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.NodeType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.util.StringUtil;
import edu.harvard.i2b2.ontology.ws.GetChildrenDataMessage;

public class ConceptDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(ConceptDao.class);
	final static String CAT_CORE = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_dimtablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip, valuetype_cd, c_protected_access, c_ontology_protection ";
	final static String CAT_DEFAULT = " c_fullname, c_name ";
	final static String CAT_LIMITED =  " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_tooltip, valuetype_cd, c_protected_access, c_ontology_protection ";

	final static String MOD_DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip, m_applied_path ";
	final static String MOD_CORE = MOD_DEFAULT;
	final static String MOD_LIMITED = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_tooltip, m_applied_path ";


	final static String DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip, valuetype_cd ";
	final static String CORE = DEFAULT;
	final static String LIMITED = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_tooltip, valuetype_cd ";

	final static String ALL = ", update_date, download_date, import_date, sourcesystem_cd ";
	final static String BLOB = ", c_metadataxml, c_comment ";

	final static String NAME_DEFAULT = " c_name ";

	private JdbcTemplate jt;

	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new  JdbcTemplate(ds);

	}

	private String getMetadataSchema() throws I2B2Exception{

		return OntologyUtil.getInstance().getMetaDataSchemaName();
	}

	public List findRootCategories(final GetCategoriesType returnType, final ProjectType projectInfo, final DBInfoType dbInfo) throws I2B2Exception, I2B2DAOException{

		// find return parameters
		String parameters = CAT_DEFAULT;		
		if (returnType.getType().equals("limited")){
			parameters = CAT_LIMITED;
		}
		else if(returnType.getType().equals("core")){
			parameters = CAT_CORE;
		}

		/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
		 */
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());


		//		 First step is to call PM to see what roles user belongs to.

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		/*
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}
		 */

		final boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);


		List queryResult = null;


		String categoriesSql = "select c_table_cd, " + parameters + " from " +  metadataSchema +  "table_access ";

		String hidden = "";
		if(returnType.isHiddens() == false)
			hidden = " where c_visualattributes not like '_H%'";

		String synonym = "";
		if(returnType.isSynonyms() == false)
			synonym = " c_synonym_cd = 'N'";

		String whereClause = hidden;
		if((whereClause.length() > 2) && (synonym.length() > 2))
			whereClause = whereClause + " and " + synonym;

		else if (synonym.length() > 2)
			whereClause = " where " + synonym;

		categoriesSql = categoriesSql + whereClause + " order by upper(c_name)";			
		log.debug(categoriesSql);

		try {
			queryResult = jt.query(categoriesSql, getConceptFullNameMapper(returnType, projectInfo, obfuscatedUserFlag));
		} catch (DataAccessException e) {
			log.error("Get Categories " +e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		//}
		log.debug("result size = " + queryResult.size());

		if (returnType.isBlob() == true && queryResult != null){
			Iterator itr = queryResult.iterator();
			while (itr.hasNext()){
				ConceptType child = (ConceptType) itr.next();
				String clobSql = "select c_metadataxml, c_comment from "+  metadataSchema +  "table_access where c_table_cd = ?";

				List clobResult = null;
				try {
					clobResult = jt.query(clobSql, getConceptXMLMapper(dbInfo), StringUtil.getTableCd(child.getKey()));
				} catch (DataAccessException e) {
					log.error(e.getMessage());
					throw new I2B2DAOException("Database Error");
				}
				if(clobResult != null)  {
					child.setMetadataxml(((ConceptType)(clobResult.get(0))).getMetadataxml());
					child.setComment(((ConceptType)(clobResult.get(0))).getComment());
				}
				else {
					child.setMetadataxml(null);
					child.setComment(null);
				}

			}
		}
		return queryResult;
	}



	public List findChildrenByParent(final GetChildrenDataMessage childrenMsg, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception, JAXBUtilException{

		final GetChildrenType childrenType = childrenMsg.getChildrenType();

		// find return parameters
		String parameters = DEFAULT;		
		if (childrenType.getType().equals("limited")){
			parameters = LIMITED;
		}

		else if (childrenType.getType().equals("core")){
			parameters = CORE;
		}
		else if (childrenType.getType().equals("all")){
			parameters = CORE + ALL;
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

		/*
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}
		 */


		//extract table code
		String tableCd = StringUtil.getTableCd(childrenType.getParent());
		String tableName=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Children " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String path = StringUtil.getPath(childrenType.getParent());
		String searchPath = path + "%";

		// Lookup to get chlevel + 1 ---  dont allow synonyms so we only get one result back

		String levelSql = "select c_hlevel from " + metadataSchema+tableName  + " where c_fullname = ?  and c_synonym_cd = 'N'";

		int level = 0;
		try {
			level = jt.queryForObject(levelSql, Integer.class, path);
		} catch (DataAccessException e1) {
			// should only get 1 result back  (path == c_fullname which should be unique)
			log.error("Get Children " + e1.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String hidden = "";
		if(childrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		String synonym = "";
		if(childrenType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		//ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(childrenType),obfuscatedUserFlag, dbInfo.getDb_serverType());

		if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
			searchPath = StringUtil.escapeSQLSERVER(path);
			searchPath += "%";
			//			log.info("escaped searchPath is " + searchPath);
		}

		else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
			searchPath = StringUtil.escapeORACLE(path); 
			searchPath += "%";
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
			searchPath = StringUtil.escapePOSTGRESQL(path); 
			searchPath += "%";
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
			searchPath = StringUtil.escapeSNOWFLAKE(path);
			searchPath += "%";
		}

		// get all children if the numLevel is less then zero
		int numLevel = childrenType.getNumLevel();
		String sql = "select " + parameters + " from " + metadataSchema + tableName + " where c_fullname like '"+ searchPath + "' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "");
		sql += (numLevel >= 0) ? " and c_hlevel > ? and c_hlevel <= ? " : " and c_hlevel > ? ";
		sql = sql + hidden + synonym + " order by c_hlevel,upper(c_name) ";


		List<ConceptType> queryResult = null;
		try {
            queryResult = (numLevel >= 0)
                    ? jt.query(sql, getConceptNodeMapper(new NodeType(childrenType), obfuscatedUserFlag, dbInfo.getDb_serverType()), level, (level + numLevel))
                    : jt.query(sql, getConceptNodeMapper(new NodeType(childrenType), obfuscatedUserFlag, dbInfo.getDb_serverType()), level);
		} catch (Exception e) {
			log.error("Get Children " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		if ((Float.parseFloat(				
				childrenMsg.getMessageHeaderType().getSendingApplication().getApplicationVersion()) > 1.5) &&
				(queryResult.size() > 0)) {
			Iterator<ConceptType>  it2 = queryResult.iterator();
			while (it2.hasNext()){
				ConceptType concept = it2.next();
				// if a leaf has modifiers report it with visAttrib == F
				if(concept.getVisualattributes().startsWith("L")){
					String modPath = StringUtil.getPath(concept.getKey());
					// I have to do this the hard way because there are a dynamic number of applied paths to check
					//   prevent SQL injection
					if(modPath.contains("'")){
						modPath = modPath.replaceAll("'", "''");
					}
					if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
						modPath = StringUtil.escapeSNOWFLAKE(modPath);
					}
					String sqlCount = "select count(*) from " + metadataSchema+ tableName  + " where m_exclusion_cd is null and c_fullname in";
					int queryCount = 0;
					// build m_applied_path sub-query
					String m_applied_pathSql = "(m_applied_path = '" + modPath +"'";
					while (modPath.length() > 3) {
						if(modPath.endsWith("%")){
							modPath = modPath.substring(0, modPath.length()-2);
							modPath = modPath.substring(0, modPath.lastIndexOf("\\") + 1) + "%";			
						}
						else
							modPath = modPath + "%";
						m_applied_pathSql = m_applied_pathSql + " or m_applied_path = '" + modPath + "'" ;
					}
					sqlCount = sqlCount + "(select c_fullname from " + metadataSchema+ tableName  + " where c_hlevel = 1 and m_exclusion_cd is null and " + m_applied_pathSql + " )";

					if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE"))
						sqlCount = sqlCount + " MINUS ";
					else
						sqlCount = sqlCount + " EXCEPT ";

					sqlCount = sqlCount+ " (select c_fullname from " + metadataSchema+ tableName  + " where m_exclusion_cd is not null and " + m_applied_pathSql + " )))";


					try {
						queryCount = jt.queryForObject(sqlCount, Integer.class);
					} catch (DataAccessException e) {
						log.error("Get Children " + e.getMessage());
						throw new I2B2DAOException("Database Error");
					}
					//				log.debug("COUNT " + queryCount + " for " +sqlCount);


					if(queryCount > 0){
						concept.setVisualattributes(concept.getVisualattributes().replace('L', 'F'));
						log.debug("changed " + concept.getName() + " from leaf to folder: modCount > 0");
					}
				}
			}
		}
		//		log.debug("Find Children By Parent " + sql);
		log.debug("get_children result size = " + queryResult.size());
		return queryResult;
		// tested statement with aqua data studio   verified output from above against this. 
		// select  c_fullname, c_name, c_synonym_cd, c_visualattributes  from metadata.testrpdr 
		// where c_fullname like '\RPDR\Diagnoses\Circulatory system (390-459)\Arterial vascular disease (440-447)\(446) Polyarteritis nodosa and al%' 
		// and c_hlevel = 5  and c_visualattributes not like '_H%' and c_synonym_cd = 'N'

		// verified both with and without hiddens and synonyms.

		// clob test   level = 4
		//   <parent>\\testrpdr\RPDR\HealthHistory\PHY\Health Maintenance\Mammogram\Mammogram - Deferred</parent> 
	}

	public List findByFullname(final GetTermInfoType termInfoType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = DEFAULT;		
		if (termInfoType.getType().equals("limited")){
			parameters = LIMITED;
		}

		else if (termInfoType.getType().equals("core")){
			parameters = CORE;
		}
		else if (termInfoType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(termInfoType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}


		boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		//tableCd to table name conversion


		String hidden = "";
		if(termInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		//extract table code
		String tableCd = StringUtil.getTableCd(termInfoType.getSelf());
		String tableName=null;
		String protectedAccess=null;
		String ontologyProtection = null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?" + hidden;
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Term Info " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}                            
		tableSql = "select distinct(c_protected_access) from " + metadataSchema + "table_access where c_table_cd = ?" + hidden;
		try {
			protectedAccess = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Term Info " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		tableSql = "select c_ontology_protection from " + metadataSchema + "table_access where c_table_cd = ?" + hidden;
		try {
			ontologyProtection = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Term Info " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String path = StringUtil.getPath(termInfoType.getSelf());
		/*
		if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
			//path = path.replaceAll("\\[", "[[]");
			path = StringUtil.escapeSQLSERVER(path);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
			path = StringUtil.escapeORACLE(path);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
			path = StringUtil.escapePOSTGRESQL(path); 
		}		
		 */

		String searchPath = path;

		String synonym = "";
		if(termInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		//		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ? " + (!dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") ? "{ESCAPE '?'}" : "" ) + ""; 
		String sql = "select '" + protectedAccess + "' as c_protected_access, '" + ontologyProtection + "' as c_ontology_protection, "  + parameters +" from " + metadataSchema+tableName  + " where c_fullname = ? "; 
		sql = sql + hidden + synonym + " order by upper(c_name) ";

		//log.info(sql + " " + path + " " + level);

		//ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(termInfoType), ofuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			queryResult = jt.query(sql, getConceptNodeMapper(new NodeType(termInfoType), ofuscatedUserFlag, dbInfo.getDb_serverType()), searchPath );
		} catch (DataAccessException e) {
			log.error("Get Term Info " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		log.debug("Term Info result size = " + queryResult.size());


		return queryResult;

	}



	public List findNameInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;		

		if (vocabType.getType().equals("limited")){
			parameters = LIMITED;
		}

		else if (vocabType.getType().equals("core")){
			parameters = CORE;
		}

		else if (vocabType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		//	log.info(metadataSchema);

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}



		//tableCd to table name + fullname conversion



		//extract table code
		String tableCd = vocabType.getCategory();
		List<ConceptType> categoryResult;

		List<ConceptType> queryResult = null;
		if (tableCd.equals("@"))
		{
			String tableSql = "select distinct(c_table_name), c_fullname, c_name from " + metadataSchema + "table_access where c_visualattributes not like '_H%'" ;
			try {
				categoryResult = jt.query(tableSql, new GetConceptNameMapper());	    
			} catch (DataAccessException e) {
				log.error("Search by Name " + e.getMessage());
				throw new I2B2DAOException("Database Error");
			}


		} else { 
			String tableSql = "select distinct(c_table_name), c_fullname, c_name from " + metadataSchema + "table_access where c_table_cd = ? " ;
			try {
				categoryResult = jt.query(tableSql, new GetConceptNameMapper(), tableCd);	    
			} catch (DataAccessException e) {
				log.error("Search by Name " + e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String nameInfoSql = null;
		String compareName = null;

		String value = vocabType.getMatchStr().getValue();
		//		using JDBCtemplate so dont need to do apostrophe replace   
		//		if(value.contains("'")){
		//			value = value.replaceAll("'", "''");
		//		}

		if (categoryResult.size() == 0){
			log.error("Non existent tableCd category passed in getNameInfo request " + tableCd);
			return null;
		} 



		for (int i=0; i < categoryResult.size(); i++) {
			String category = categoryResult.get(i).getKey();
			if(category.contains("'")){
				category = category.replaceAll("'", "''");
			}

			if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				category = StringUtil.escapeSQLSERVER(category);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
				category = StringUtil.escapeORACLE(category);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
				category = StringUtil.escapePOSTGRESQL(category); 
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
				category = StringUtil.escapeSNOWFLAKE(category);
			}


			// dont do the sql injection replace; it breaks the service.
			if(vocabType.getMatchStr().getStrategy().equals("exact")) {
				nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(i).getTablename() + " where upper(c_name) = ? and c_fullname like '" + category +	"%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
				compareName = value.toUpperCase();  	
			}

			else if(vocabType.getMatchStr().getStrategy().equals("left")){
				nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(i).getTablename() +" where upper(c_name) like ? " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" ) + " and c_fullname like '" + category +"%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")||dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
				if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
					compareName = StringUtil.escapeSQLSERVER(vocabType.getMatchStr().getValue().toUpperCase());
					//compareName = compareName.replaceAll("\\[", "[[]");
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
					compareName = StringUtil.escapeORACLE(vocabType.getMatchStr().getValue().toUpperCase());
					//compareName = compareName.replaceAll("\\[", "[[]");
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
					compareName = StringUtil.escapePOSTGRESQL(vocabType.getMatchStr().getValue().toUpperCase());
					//compareName = compareName.replaceAll("\\[", "[[]");
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
					compareName = StringUtil.escapeSNOWFLAKE(vocabType.getMatchStr().getValue().toUpperCase());
					//compareName = compareName.replaceAll("\\[", "[[]");
				}
				compareName = compareName + "%";

			}

			else if(vocabType.getMatchStr().getStrategy().equals("right")) {
				nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(i).getTablename() +" where upper(c_name) like ? " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" ) + " and c_fullname like '" + category +"%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";     {ESCAPE '?'}";
				if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
					compareName = StringUtil.escapeSQLSERVER(vocabType.getMatchStr().getValue().toUpperCase());
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
					compareName = StringUtil.escapeORACLE(vocabType.getMatchStr().getValue().toUpperCase());
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
					compareName = StringUtil.escapePOSTGRESQL(vocabType.getMatchStr().getValue().toUpperCase());
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
					compareName = StringUtil.escapeSNOWFLAKE(vocabType.getMatchStr().getValue().toUpperCase());
				}

				compareName =  "%" + compareName;
				//   	if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				//		compareName = compareName.replaceAll("\\[", "[[]");
				//	}
			}

			else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
				if(!(value.contains(" "))){
					nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(i).getTablename() +" where upper(c_name) like ? " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" ) + " and c_fullname like '" + category +"%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" ) + "";
					if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
						compareName = StringUtil.escapeSQLSERVER(vocabType.getMatchStr().getValue().toUpperCase());
					}
					else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
						compareName = StringUtil.escapeORACLE(vocabType.getMatchStr().getValue().toUpperCase());
					}
					else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
						compareName = StringUtil.escapePOSTGRESQL(vocabType.getMatchStr().getValue().toUpperCase());
					}
					else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
						compareName = StringUtil.escapeSNOWFLAKE(vocabType.getMatchStr().getValue().toUpperCase());
					}
					compareName =  "%" + compareName + "%";
					//if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
					//		compareName = compareName.replaceAll("\\[", "[[]");
					//	}
				}else{
					nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(i).getTablename();
					if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
						compareName = StringUtil.escapeSQLSERVER(vocabType.getMatchStr().getValue().toUpperCase());
					}
					else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
						compareName = StringUtil.escapeORACLE(vocabType.getMatchStr().getValue().toUpperCase());
					}
					else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
						compareName = StringUtil.escapePOSTGRESQL(vocabType.getMatchStr().getValue().toUpperCase());
					}
					else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
						compareName = StringUtil.escapeSNOWFLAKE(vocabType.getMatchStr().getValue().toUpperCase());
					}

					//		if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
					//			value = value.replaceAll("\\[", "[[]");
					//		}
					//	WAS
					//		nameInfoSql = nameInfoSql + parseMatchString(value)+ " and c_fullname like '" + category +"%'" + (!dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") ? "{ESCAPE '?'}" : "" ) + "";;
					// !dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") ? compareName.replaceAll("'", "''") : compareName 
					nameInfoSql = nameInfoSql + parseMatchString((compareName.replaceAll("'", "''")), dbInfo)+ " and c_fullname like '" + category +"%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" ) + " ";;

					compareName = null;
				}
			}


			String hidden = "";// and c_totalnum != 0 ";
			if(vocabType.isHiddens() == false)
				hidden += " and c_visualattributes not like '_H%'";


			String synonym = "";
			if(vocabType.isSynonyms() == false)
				synonym = " and c_synonym_cd = 'N'";

			nameInfoSql = nameInfoSql + hidden + synonym + " order by c_hlevel, c_totalnum, upper(c_name) asc ";  

			log.info("nameInfoSql:" + nameInfoSql + " " +compareName);
			boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
			//ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType());


			try {
				List<ConceptType> list = null;
				if(compareName != null) {
					list = jt.query(nameInfoSql, getConceptNodeMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType()), compareName);
					//queryResult.addAll(list);
				} else {
					list = jt.query(nameInfoSql, getConceptNodeMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType()));
					//queryResult.addAll(list);
				}
				
				
				// Add parent poaths
				
				String tableName=categoryResult.get(i).getTablename();
				String name = categoryResult.get(i).getName();
				/*
				String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
				try {
					tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
				} catch (DataAccessException e) {
					log.error("Get Children " + e.getMessage());
					throw new I2B2DAOException("Database Error");
				}
				*/
				
				//jgk
				// This does a linear search through fullnames for each previous fullname, O(n^2) :(
				// BUT it assumes its sorted by hlevel so it only has to search through whats already seen - n(n+1)/2 operations 
				if (list.size()>0 && vocabType.isReducedResults()!=null && vocabType.isReducedResults()) {
					ArrayList<String> seen = new ArrayList<String>(); 
					ArrayList<ConceptType> keep = new ArrayList<ConceptType>();
					Iterator<ConceptType> it = list.iterator();
					while (it.hasNext())
					{
						ConceptType node = (ConceptType)it.next();
						String key = node.getKey();
						boolean bAbort = false;
						for (String k : seen) {
							if(key.startsWith(k) && !key.equals(k) /* <-- don't kill the synonyms */ ) {
								bAbort = true;
								break;
							}
						}
						if (!bAbort) { 
							// Add nodes that were not subsumed to the keep list
							keep.add(node);
						}
						// Hidden and inactive should not subsume other nodes - exclude them
						if (node.getVisualattributes().contains("A")) 
							seen.add(node.getKey());
					}
					log.debug("Reduced find terms from "+list.size()+" to "+keep.size());
					list = keep;
				}
				
				if (vocabType.isKeyname()!=null && vocabType.isKeyname()) {
					// Only do keyname lookups if we haven't exceeded the max				
					HashMap<String,String> KeynameCache = new HashMap<String,String>();
					int skipCount = 0; // for debug, number of cache hits
					//int skipPathCount = category.split("\\\\").length -2; // preamble elements in path, not to be output in key name (everything but final element in category path)
					
					// A little code to ignore a path in the category name, if there is more than one element.
					// e.g., \\i2b2_MED\Medications\ will ignore i2b2_MED
					String[] skipPaths = category.split("\\\\");
					String skipPath = "";
					for (int j=1;j<skipPaths.length-1;j++) skipPath=skipPath+"\\"+skipPaths[j];
					skipPath=skipPath+"\\";
					
					String sql = "";
					int keynameCount = 0;
					for (ConceptType cType: list) {
						//String path = cType.getDimcode(); //StringUtil.getPath(childrenType.getParent());
						String parentPath = StringUtil.getParentPath(cType.getKey().substring(tableCd.length()+2));
											
						// Only do keyname lookups up to the max return result size
						keynameCount++;
						if (keynameCount>vocabType.getMax()) break;
							
						if (KeynameCache.containsKey(parentPath)) {
							cType.setKeyName(KeynameCache.get(parentPath));
							skipCount++;
						}
						else {
							if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
								sql = "WITH pathnames ";
								sql += " AS";
								sql += " (";
								sql += "    select c_name, c_fullname,";
								sql += "        substring(c_fullname, 1, len(c_fullname) - charindex('\\', reverse(c_fullname), 2) + 1) as c_path,";
								sql += "        1 as c_pathorder";
								sql += "    from " + metadataSchema+tableName  + " where c_fullname =  '"+ parentPath + "' and c_synonym_cd='N'";
								sql += "    UNION ALL";
								sql += "    select m.c_name, m.c_fullname,  substring(m.c_fullname, 1, len(m.c_fullname) - charindex('\\', reverse(m.c_fullname), 2) + 1) as c_path, c_pathorder + 1 as c_pathorder";
								sql += "    from " + metadataSchema+tableName  + "  m";
								sql += "        inner join pathnames p on m.c_fullname = p.c_path where c_synonym_cd='N'";   
								sql += " )";
								sql += " SELECT distinct c_name, c_fullname, c_pathorder as c_hlevel";
								sql += " FROM   pathnames";
								sql += " order by c_pathorder desc ";
		
							}
		
							else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
		
		
								sql = "WITH pathnames (c_name, c_fullname, c_path, c_pathorder) ";
								sql += " AS ";
								sql += " ( ";
								sql += "   select c_name, c_fullname, ";
								sql += "        substr(c_fullname, 1, length(c_fullname) - instr(reverse(c_fullname),'\\',  2) + 1) as c_path,";
								sql += "       1 as c_pathorder";
								sql += "    from " + metadataSchema+tableName  + "  where c_fullname =  '"+ parentPath + "' and c_synonym_cd='N'";
								sql += "   UNION ALL";
								sql += "   select m.c_name, m.c_fullname,  substr(m.c_fullname, 1, length(m.c_fullname) - instr(reverse(m.c_fullname), '\\',  2) + 1) as c_path, c_pathorder + 1 as c_pathorder";
								sql += "  from " + metadataSchema+tableName  + "   m";
								sql += "       inner join pathnames p on m.c_fullname = p.c_path where c_synonym_cd='N'";
		
								sql += " )";
								sql += " SELECT distinct c_name, c_fullname, c_pathorder as c_hlevel";
								sql += " FROM   pathnames";
								sql += " order by c_pathorder desc ";
							} 		else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") ){
		
								sql  = "WITH RECURSIVE pathnames ";
								sql += " AS";
								sql += " (";
								sql += "    select c_name, c_fullname,";
								sql += "      substr(c_fullname, 1, length(c_fullname) - strpos(substr(reverse(c_fullname), 2), '\\') ) as c_path,";
								sql += "      1 as c_pathorder";
								sql += "    from " + metadataSchema+tableName  + "  where c_fullname =  '"+ parentPath + "' and c_synonym_cd='N'";
								sql += "    UNION ALL";
								sql += "    select m.c_name, m.c_fullname,  ";
								sql += "      substr(m.c_fullname, 1, length(m.c_fullname) - strpos(substr(reverse(m.c_fullname), 2), '\\') ) as c_path,   c_pathorder + 1 as c_pathorder";
		
								sql += "    from " + metadataSchema+tableName  + "  m";
								sql += "        inner join pathnames p on m.c_fullname = p.c_path where c_synonym_cd='N'";
		
								sql += " ) ";
								sql += " SELECT distinct c_name, c_fullname, c_pathorder as c_hlevel";
								sql += " FROM   pathnames";
								sql += " order by c_pathorder desc";
							} else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE") ){
								parentPath = StringUtil.escapeSNOWFLAKE(parentPath);
								sql  = "WITH RECURSIVE pathnames ";
								sql += " AS";
								sql += " (";
								sql += "    select c_name, c_fullname,";
								sql += "      substr(c_fullname, 1, length(c_fullname) - position( '\\\\', substr(reverse(c_fullname), 2)) ) as c_path,";
								sql += "      1 as c_pathorder";
								sql += "    from " + metadataSchema+tableName  + "  where c_fullname =  '"+ parentPath + "' and c_synonym_cd='N'";
								sql += "    UNION ALL";
								sql += "    select m.c_name, m.c_fullname,  ";
								sql += "      substr(m.c_fullname, 1, length(m.c_fullname) - position('\\\\', substr(reverse(m.c_fullname), 2)) ) as c_path,   c_pathorder + 1 as c_pathorder";

								sql += "    from " + metadataSchema+tableName  + "  m";
								sql += "        inner join pathnames p on m.c_fullname = p.c_path where c_synonym_cd='N'";

								sql += " ) ";
								sql += " SELECT distinct c_name, c_fullname, c_pathorder as c_hlevel";
								sql += " FROM   pathnames";
								sql += " order by c_pathorder desc";
							}
							
							
							//List  rows = jt.queryForList(sql, path);
		
							/*
							 * 			List<String> names = jt.query(sql,  new RowMapper() {
							      public Object mapRow(ResultSet resultSet, int i) throws SQLException {
							        return resultSet.getString(1);
							      }
							    }, path);
							 */
							List<ConceptType> names = jt.query(sql, new RowMapper<ConceptType>() {
									public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
										ConceptType category = new ConceptType();	 

										category.setKey(rs.getString("c_fullname"));
										category.setLevel(rs.getInt("c_hlevel"));
										category.setName(rs.getString("c_name"));
										return category;
									}
								}/*new GetConceptParentMapper()*/);
							
							cType.setKeyName("\\");
							for (int y=0; y< names.size(); y++) {
								if(names.get(y).getKey().equals(skipPath)) continue; // only one path component for the category is ever included
								if(names.get(y).getKey().equals(category)) 
									cType.setKeyName(cType.getKeyName() + name); // Use the category name instead of the db row name, for clarity
								else cType.setKeyName(cType.getKeyName() + names.get(y).getName());
								if ((y + 1) < names.size())
									cType.setKeyName(cType.getKeyName() + "\\" );
							//+  \\ ");
							
							}
							// In the event that the category does not have a row in the ontology, insert an entry for it manually
							// TODO: Is the actual category name anywhere? (Currently using the code)
							if (names.size()+skipPaths.length-2<cType.getLevel()) cType.setKeyName("\\"+name+cType.getKeyName());
						}
						KeynameCache.put(parentPath, cType.getKeyName());
						cType.setKeyName(cType.getKeyName()+"\\"+cType.getName()+"\\");
					}
					if (skipCount>0) log.debug("Skipped keyname lookups due to caching ="+skipCount);
				}
				
				// Add list to results after adding parent list names
				if (queryResult == null)
					queryResult = list;
				else
					queryResult.addAll(list);

			} catch (DataAccessException e) {
				log.error("Search by Name " + e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		log.debug("search by NameInfo result size = " + queryResult.size());


		return queryResult;

	}

	public List findCodeInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;	

		if (vocabType.getType().equals("limited")){
			parameters = LIMITED;
		}

		else if (vocabType.getType().equals("core")){
			parameters = CORE;
		}

		else if (vocabType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		String dbType = dbInfo.getDb_serverType();

		//		log.info(metadataSchema);

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}




		String hidden = "";
		String whereHidden = "";
		if(vocabType.isHiddens() == false) {
			hidden = " and c_visualattributes not like '_H%'";	
			whereHidden = " where c_visualattributes not like '_H%'";
		}
		//no table code provided so check all tables user has access to
		List tableNames=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access " + whereHidden;
		try {
			tableNames = jt.queryForList(tableSql, String.class);	    
		} catch (DataAccessException e) {
			log.error("Search by Code " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		// I have to do this the hard way because there are a dynamic number of codes to pass in
		//   prevent SQL injection
		String value = vocabType.getMatchStr().getValue();
		if(value.contains("'")){
			value = vocabType.getMatchStr().getValue().replaceAll("'", "''");
		}
		String whereClause = null;

		String compareCode = value.toUpperCase();

		if(vocabType.getMatchStr().getStrategy().equals("exact")) {
			whereClause = " where upper(c_basecode) = '" + compareCode+ "'";
		}

		else { // need escape logic for like operator

			if(dbType.toUpperCase().equals("SQLSERVER")){
				compareCode = StringUtil.escapeSQLSERVER(compareCode);
			}
			else if(dbType.toUpperCase().equals("ORACLE")){
				compareCode = StringUtil.escapeORACLE(compareCode);
			}
			else if(dbType.toUpperCase().equals("POSTGRESQL")){
				compareCode = StringUtil.escapePOSTGRESQL(compareCode);
			}
			else if(dbType.toUpperCase().equals("SNOWFLAKE")){
				compareCode = StringUtil.escapeSNOWFLAKE(compareCode);
			}

			if(vocabType.getMatchStr().getStrategy().equals("left")){
				whereClause = " where upper(c_basecode) like '" + compareCode + "%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
			}

			else if(vocabType.getMatchStr().getStrategy().equals("right")) {
				compareCode = compareCode.replaceFirst(":", ":%");
				whereClause = " where upper(c_basecode) like '" +  compareCode + "' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
			}

			else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
				compareCode = compareCode.replaceFirst(":", ":%");
				whereClause = " where upper(c_basecode) like '" + compareCode + "%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
			}
		}
		//	log.debug(vocabType.getMatchStr().getStrategy() + whereClause);

		String codeInfoSql = null;
		if(tableNames != null){
			Iterator itTn = tableNames.iterator();
			String table = (String)itTn.next();
			// the following (distinct) doesnt work for a flattened hierarchy but is left for
			//  dbs other than sqlserver or oracle.   [c_table_cd is needed for key]
			String tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
			if(dbType.toUpperCase().equals("SQLSERVER"))
				tableCdSql = ", (select top 1(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
			else if (dbType.toUpperCase().equals("ORACLE"))
				tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' and rownum <= 1) as tableCd"; 
			else if(dbType.toUpperCase().equals("POSTGRESQL"))
				tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' limit 1) as tableCd";
			else if(dbType.toUpperCase().equals("SNOWFLAKE"))
				tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' limit 1) as tableCd";
			codeInfoSql = "select " + parameters + tableCdSql + " from " + metadataSchema + table + whereClause	+ hidden + synonym;;
			while(itTn.hasNext()){		
				table = (String)itTn.next();
				// the following (distinct) doesnt work for a flattened hierarchy but is left for
				//  dbs other than sqlserver or oracle.    [c_table_cd is needed for key]
				tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
				if(dbType.toUpperCase().equals("SQLSERVER"))
					tableCdSql = ", (select top 1(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
				else if (dbType.toUpperCase().equals("ORACLE"))
					tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' and rownum <= 1) as tableCd"; 
				else if(dbType.toUpperCase().equals("POSTGRESQL"))
					tableCdSql = ", (select  c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' limit 1) as tableCd";
				else if(dbType.toUpperCase().equals("SNOWFLAKE"))
					tableCdSql = ", (select  c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' limit 1) as tableCd";

				codeInfoSql = codeInfoSql +  " union all (select "+ parameters + tableCdSql + " from " + metadataSchema + table + whereClause
						+ hidden + synonym + ")";
			}
			codeInfoSql = codeInfoSql + " order by (c_name) ";
		}
		else
			return null;

		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		//	ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			String sqlFinal = codeInfoSql;
			queryResult = jt.query(sqlFinal, getConceptNodeMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType()));
		} catch (DataAccessException e) {
			log.error("Search by Code " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		log.debug("searchByCodeInfo result size = " + queryResult.size());


		return queryResult;

	}


	/*
	private ParameterizedRowMapper<String> getColumnMapper() {

		ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String derivedFactTableColumn;          
				derivedFactTableColumn=(rs.getString("c_facttablecolumn"));
				return derivedFactTableColumn;
			}
		};
		return mapper;
	}
	 */



	private String parseMatchString(String match, DBInfoType dbInfo){
		String whereClause = null;

		String[] terms = match.split(" ");
		ArrayList<String> goodWords = new ArrayList<String>();

		String word = getStopWords();
		for(int i=0; i< terms.length; i++){			
			if(word.contains(terms[i]))
				;
			else{
				goodWords.add(terms[i]);
			}
		}			

		if(goodWords.isEmpty())
			return null;

		Iterator it = goodWords.iterator();
		while(it.hasNext()){
			if(whereClause == null)	
				whereClause = " where upper(c_name) like " + "'%"  + ((String)it.next()).toUpperCase() + "%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
			else
				whereClause = whereClause + " AND upper(c_name) like " + "'% " + ((String)it.next()).toUpperCase() + "%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";
		}	
		return whereClause;
	}

	private String getStopWords(){

		String stopWord = null;
		try {
			stopWord = OntologyUtil.getInstance().getStopWord();
		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				//"a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";	
		//		String[] stopWords = stopWord.split("'");	
		return stopWord;
	}

	public List findModifiers(final GetModifiersType modifierType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = MOD_DEFAULT;	
		if (modifierType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		else if (modifierType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		else if (modifierType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(modifierType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}





		String hidden = "";
		if(modifierType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		//extract table code
		String tableCd = StringUtil.getTableCd(modifierType.getSelf());
		String tableName=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? " + hidden;
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Find Modifiers " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}




		//     Original sql before exclusions		
		//		String path = StringUtil.getLiteralPath(modifierType.getSelf());
		/*		String sql = "select " + parameters +" from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and c_hlevel = 1";
		while (path.length() > 2) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			sql = sql + " union all (select " + parameters +" from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and c_hlevel = 1)";
		}
		 */
		String path = StringUtil.getLiteralPath(modifierType.getSelf());
		// I have to do this the hard way because there are a dynamic number of applied paths to check
		//   prevent SQL injection
		if(path.contains("'")){
			path = path.replaceAll("'", "''");
		}

		if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) {
			path = StringUtil.escapeSNOWFLAKE(path);
		}

		String synonym = "";
		if(modifierType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";		


		//		String sql = "select " + parameters + " from "+ metadataSchema+ tableName + " where m_exclusion_cd is null and c_fullname in (";
		String inclusionSql = "select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and c_hlevel = 1 and m_exclusion_cd is null " + hidden + synonym;
		String modifier_select =  " and m_applied_path in ('" + path + "'";
		while (path.length() > 2) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			inclusionSql = inclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path +  "' and c_hlevel = 1 and m_exclusion_cd is null " + hidden + synonym +")";
			modifier_select = modifier_select + ", '" + path + "'";
		}

		String sql = "select " + parameters + " from "+ metadataSchema+ tableName + " where m_exclusion_cd is null " + synonym + modifier_select +") and c_fullname in (";

		if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE"))
			sql = sql + inclusionSql + " MINUS (";
		else
			sql = sql + inclusionSql + " EXCEPT (";

		path = StringUtil.getLiteralPath(modifierType.getSelf());
		// I have to do this the hard way because there are a dynamic number of applied paths to check
		//   prevent SQL injection
		if(path.contains("'")){
			path = path.replaceAll("'", "''");
		}

		if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) {
			path = StringUtil.escapeSNOWFLAKE(path);
		}
		String exclusionSql = "select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and m_exclusion_cd is not null";
		while (path.length() > 2) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			exclusionSql = exclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and m_exclusion_cd is not null)";
		}
		/*		// applied paths on exclusions dont end in %
		while (path.length() > 2) {
			path = path.substring(0, path.length()-2);		
			path = path.substring(0, path.lastIndexOf("\\") +1) ;		
			exclusionSql = exclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and m_exclusion_cd = 'X')";
		}
		 */
		sql = sql + exclusionSql + "))";

		sql = sql + " order by (c_name) ";

		//	("findMods: " + sql );
		final boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		//		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType (modifierType), ofuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;

		try {
			//			queryResult = jt.query(sql, modMapper, path );
			queryResult = jt.query(sql, getModNodeMapper(new NodeType(modifierType),ofuscatedUserFlag, dbInfo.getDb_serverType()));
		} catch (DataAccessException e) {
			log.error("Find Modifiers " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		log.debug("findModifiers result size " + queryResult.size());
		return queryResult;
	}


	public List findChildrenByParent(final GetModifierChildrenType modifierChildrenType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		//	("MOD: " + modifierChildrenType.getParent());
		//	log.debug("MOD: " + modifierChildrenType.getAppliedPath());

		// find return parameters
		String parameters = MOD_DEFAULT;		
		if (modifierChildrenType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		else if (modifierChildrenType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		else if (modifierChildrenType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(modifierChildrenType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}



		String hidden = "";
		if(modifierChildrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		//extract table code
		String tableCd = StringUtil.getTableCd(modifierChildrenType.getParent());
		String tableName=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? " + hidden;
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Modifier Children " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String path = StringUtil.getPath(modifierChildrenType.getParent());

		String searchPath = path;
		if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
			searchPath = StringUtil.escapeSQLSERVER(searchPath);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
			searchPath = StringUtil.escapeORACLE(searchPath);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
			searchPath = StringUtil.escapePOSTGRESQL(searchPath);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
			searchPath = StringUtil.escapeSNOWFLAKE(searchPath);
		}

		searchPath = searchPath + "%";

		String levelSql = "select c_hlevel from " + metadataSchema+tableName  + " where c_fullname = ?  and c_synonym_cd = 'N' and m_applied_path = ? and m_exclusion_cd is null";

		int level = 0;
		try {
			level = jt.queryForObject(levelSql, Integer.class, path, modifierChildrenType.getAppliedPath());
		} catch (DataAccessException e1) {
			// should only get 1 result back  (path == c_fullname which should be unique)
			log.error("Get Modifier Children " + e1.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String synonym = "";
		if(modifierChildrenType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";		

		String appliedConcept = StringUtil.getLiteralPath(modifierChildrenType.getAppliedConcept());
		// I have to do this the hard way because there are a dynamic number of applied paths to check
		//   prevent SQL injection
		if(appliedConcept.contains("'")){
			appliedConcept = appliedConcept.replaceAll("'", "''");
		}


		String inclusionSql = "select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = ? and c_hlevel = " + (level+1) + " and m_exclusion_cd is null";
		String modifier_select =  " and m_applied_path in ('" + appliedConcept + "'";
		while (appliedConcept.length() > 2) {
			if(appliedConcept.endsWith("%")){
				appliedConcept = appliedConcept.substring(0, appliedConcept.length()-2);
				appliedConcept = appliedConcept.substring(0, appliedConcept.lastIndexOf("\\") + 1) + "%";			
			}
			else
				appliedConcept = appliedConcept + "%";
			inclusionSql = inclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + appliedConcept + "' and c_hlevel = " + (level+1) + " and m_exclusion_cd is null)";
			modifier_select = modifier_select + ", '" + appliedConcept + "'";
		}

		String sql = "select " + parameters + " from "+ metadataSchema+ tableName + " where m_exclusion_cd is null and c_hlevel = ? and c_fullname like ? "  + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" ) + hidden + synonym
				+ modifier_select +") and c_fullname in (";


		if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE"))
			sql = sql + inclusionSql + " MINUS (";
		else
			sql = sql + inclusionSql + " EXCEPT (";

		String exclusionSql = "select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = ? and m_exclusion_cd is not null";
		String appliedExclConcept = StringUtil.getLiteralPath(modifierChildrenType.getAppliedConcept());
		// I have to do this the hard way because there are a dynamic number of applied paths to check
		//   prevent SQL injection
		if(appliedExclConcept.contains("'")){
			appliedExclConcept = appliedExclConcept.replaceAll("'", "''");
		}
		while (appliedExclConcept.length() > 2) {
			if(appliedExclConcept.endsWith("%")){
				appliedExclConcept = appliedExclConcept.substring(0, appliedExclConcept.length()-2);
				appliedExclConcept = appliedExclConcept.substring(0, appliedExclConcept.lastIndexOf("\\") + 1) + "%";			
			}
			else
				appliedExclConcept = appliedExclConcept + "%";
			exclusionSql = exclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + appliedExclConcept + "' and m_exclusion_cd is not null)";
		}

		sql = sql + exclusionSql + "))";

		sql = sql + " order by (c_name) ";

		//	log.debug("findModChildren: " + sql + (level+1) + searchPath +  StringUtil.getLiteralPath(modifierChildrenType.getAppliedConcept()));


		final boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		//ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType (modifierChildrenType), ofuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;

		try {
			queryResult = jt.query(sql, getModNodeMapper(new NodeType(modifierChildrenType),ofuscatedUserFlag, dbInfo.getDb_serverType()), (level+1), searchPath,  StringUtil.getLiteralPath(modifierChildrenType.getAppliedConcept()),
					StringUtil.getLiteralPath(modifierChildrenType.getAppliedConcept()));
		} catch (DataAccessException e) {
			log.error("Get Modifier Children " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}


		log.debug("Get Mod children result size = " + queryResult.size());


		return queryResult;

	}



	public List findByFullname(final GetModifierInfoType modifierInfoType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = MOD_DEFAULT;	
		if (modifierInfoType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		else if (modifierInfoType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		else if (modifierInfoType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(modifierInfoType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}

		//tableCd to table name conversion


		String hidden = "";
		if(modifierInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";


		//extract table code
		String tableCd = StringUtil.getTableCd(modifierInfoType.getSelf());
		String tableName=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? " + hidden;
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Modifier " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String path = StringUtil.getPath(modifierInfoType.getSelf());
		String searchPath = path;


		String synonym = "";
		if(modifierInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		//	Removed dependency on m_applied_path 8/4/14 lcp
		String sqlWpath = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname = ? and m_applied_path = ?"; 
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname = ? "; 

		// Putting applied path back..  1/4/16   CORE-203
		// Was originally omitted for SHRINE (paths would be invalid) but result is that MANY modifiers return and messes up i2b2
		// So first search via applied path; if that returns zero then search w/o applied path


		sqlWpath = sqlWpath + hidden + synonym + " order by upper(c_name) ";

		//log.info(sql + " " + path + " " + level);

		final boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		//		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType (modifierInfoType), ofuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			queryResult = jt.query(sqlWpath, getModNodeMapper(new NodeType(modifierInfoType),ofuscatedUserFlag, dbInfo.getDb_serverType()), searchPath, modifierInfoType.getAppliedPath());

		} catch (DataAccessException e) {
			log.error("Get Modifier " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		if(queryResult.size() == 0){
			sql = sql + hidden + synonym + " order by upper(c_name) ";

			try {
				queryResult = jt.query(sql, getModNodeMapper(new NodeType(modifierInfoType),ofuscatedUserFlag, dbInfo.getDb_serverType()), searchPath);

			} catch (DataAccessException e) {
				log.error("Get Modifier " + e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		log.debug("Get ModInfo result size = " + queryResult.size());


		return queryResult;

	}

	public List findModifierNameInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;		

		if (vocabType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}

		else if (vocabType.getType().equals("core")){
			parameters = MOD_CORE;
		}

		else if (vocabType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		//	log.info(metadataSchema);

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		//tableCd to table name conversion


		//extract table code
		String tableCd = StringUtil.getTableCd(vocabType.getSelf());
		String tableName=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? " ;
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Modifier by name " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		//   prevent SQL injection and also catch case where the value contains an (')
		String value = vocabType.getMatchStr().getValue();
		if(value.contains("'")){
			value = vocabType.getMatchStr().getValue().replaceAll("'", "''");
		}
		String nameInfoSql = null;
		String compareName = null;
		String modifierPath = StringUtil.getLiteralPath(vocabType.getSelf());
		if(modifierPath.contains("'")){
			modifierPath = modifierPath.replaceAll("'", "''");
		}

		if(vocabType.getMatchStr().getStrategy().equals("exact")) {
			compareName = value.toUpperCase();
			nameInfoSql = "select c_fullname from " + metadataSchema + tableName + " where upper(c_name) = '" + compareName + "'" ;//and m_applied_path = '" + path + "'";	  
		}

		else if(vocabType.getMatchStr().getStrategy().equals("left")){ 
			compareName = value.toUpperCase();
			if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				//compareName = compareName.replaceAll("\\[", "[[]");
				compareName = StringUtil.escapeSQLSERVER(compareName);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
				compareName = StringUtil.escapeORACLE(compareName);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
				compareName = StringUtil.escapePOSTGRESQL(compareName);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
				compareName = StringUtil.escapeSNOWFLAKE(compareName);
			}
			compareName +=  "%";
			nameInfoSql = "select c_fullname from " + metadataSchema + tableName +" where upper(c_name) like '" + compareName + "' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";      and m_applied_path = '" + path + "'";
		}

		else if(vocabType.getMatchStr().getStrategy().equals("right")) {
			compareName = value.toUpperCase();
			if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				//compareName = compareName.replaceAll("\\[", "[[]");
				compareName = StringUtil.escapeSQLSERVER(compareName);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
				compareName = StringUtil.escapeORACLE(compareName);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
				compareName = StringUtil.escapePOSTGRESQL(compareName);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
				compareName = StringUtil.escapeSNOWFLAKE(compareName);
			}
			compareName =  "%" + compareName;
			nameInfoSql = "select c_fullname from " + metadataSchema + tableName +" where upper(c_name) like '" + compareName + "' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";     {ESCAPE '?'}";//and m_applied_path = '" + path + "'";
		}

		else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
			if(!(value.contains(" "))){
				//	compareName =  "%" + value.toUpperCase() + "%";
				compareName = value.toUpperCase();
				if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
					//compareName = compareName.replaceAll("\\[", "[[]");
					compareName = StringUtil.escapeSQLSERVER(compareName);
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
					compareName = StringUtil.escapeORACLE(compareName);
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
					compareName = StringUtil.escapePOSTGRESQL(compareName);
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
					compareName = StringUtil.escapeSNOWFLAKE(compareName);
				}
				compareName =  "%" + compareName + "%";
				nameInfoSql = "select c_fullname from " + metadataSchema + tableName +" where upper(c_name) like '" + compareName + "' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";    {ESCAPE '?'}";  //and m_applied_path = '" + path + "'";

			}else{
				nameInfoSql = "select c_fullname from " + metadataSchema + tableName ;
				//	if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				//		value = value.replaceAll("\\[", "[[]");
				//	}
				if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
					value = StringUtil.escapeSQLSERVER(value);
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
					value = StringUtil.escapeORACLE(value);
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
					value = StringUtil.escapePOSTGRESQL(value);
				}
				else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
					value = StringUtil.escapeSNOWFLAKE(value);
				}
				nameInfoSql = nameInfoSql + parseMatchString(value, dbInfo);// + "and m_applied_path = '" + path + "'";
				compareName = null;
			}
		}

		String appliedPath   = " and m_applied_path = '" + modifierPath + "' ";

		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%' ";

		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N' ";

		String inclusionSql = nameInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is null ";
		String modifierSelect = "'" +	modifierPath + "'";
		while (modifierPath.length() > 3) {
			if(modifierPath.endsWith("%")){
				modifierPath = modifierPath.substring(0, modifierPath.length()-2);
				modifierPath = modifierPath.substring(0, modifierPath.lastIndexOf("\\") + 1) + "%";			
			}
			else
				modifierPath = modifierPath + "%";
			modifierSelect = modifierSelect + ", '" + modifierPath + "'";
			appliedPath = " and m_applied_path = '" + modifierPath + "' ";
			inclusionSql = inclusionSql + " union all " + nameInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is null ";
		}

		String exclusionSql = nameInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is not null";
		modifierPath = StringUtil.getLiteralPath(vocabType.getSelf());
		if(modifierPath.contains("'")){
			modifierPath = modifierPath.replaceAll("'", "''");
		}
		while (modifierPath.length() > 3) {
			if(modifierPath.endsWith("%")){
				modifierPath = modifierPath.substring(0, modifierPath.length()-2);
				modifierPath = modifierPath.substring(0, modifierPath.lastIndexOf("\\") + 1) + "%";			
			}
			else
				modifierPath = modifierPath + "%";
			appliedPath = " and m_applied_path = '" + modifierPath + "' ";
			exclusionSql = exclusionSql + " union all " + nameInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is not null ";
		}

		String exceptSql =  " EXCEPT (";
		if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE"))
			exceptSql =	" MINUS (";


		String 	modNameInfoSql = " select " + parameters + " from "  + metadataSchema + tableName +  " where m_exclusion_cd is null and m_applied_path in ("
				+ modifierSelect +") and c_fullname in ("	+ inclusionSql	+ exceptSql + exclusionSql 	+ ")) order by (c_name) ";

		//		log.debug("MODnameInfo: " + modNameInfoSql + " " +compareName);
		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		//		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType(vocabType), obfuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			if(compareName != null)
				queryResult = jt.query(modNameInfoSql, getModNodeMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType()));
			else
				queryResult = jt.query(modNameInfoSql, getModNodeMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType()));
		} catch (DataAccessException e) {
			log.error("Get Modifier by name " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		log.debug("Mod search by name result size = " + queryResult.size());


		return queryResult;

	}

	public List findModifierCodeInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;	

		if (vocabType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}

		else if (vocabType.getType().equals("core")){
			parameters = MOD_CORE;
		}

		else if (vocabType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		String dbType = dbInfo.getDb_serverType();

		//		log.info(metadataSchema);

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		//tableCd to table name conversion


		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		//extract table code
		String tableCd = StringUtil.getTableCd(vocabType.getSelf());
		String tableName=null;
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? " + hidden;
		try {
			tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
		} catch (DataAccessException e) {
			log.error("Get Modifier by Code " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		// I have to do this the hard way because there are a dynamic number of codes to pass in
		//   prevent SQL injection
		String value = vocabType.getMatchStr().getValue();
		if(value.contains("'")){
			value = vocabType.getMatchStr().getValue().replaceAll("'", "''");
		}
		String whereClause = null;

		if(vocabType.getMatchStr().getStrategy().equals("exact")) {
			whereClause = " where upper(c_basecode) = '" + value.toUpperCase()+ "'";
		}

		else if(vocabType.getMatchStr().getStrategy().equals("left")){
			if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				value = StringUtil.escapeSQLSERVER(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
				value = StringUtil.escapeORACLE(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
				value = StringUtil.escapePOSTGRESQL(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
				value = StringUtil.escapeSNOWFLAKE(value);
			}
			whereClause = " where upper(c_basecode) like '" + value.toUpperCase() + "%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";    {ESCAPE '?'}";
		}

		else if(vocabType.getMatchStr().getStrategy().equals("right")) {
			if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				value = StringUtil.escapeSQLSERVER(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
				value = StringUtil.escapeORACLE(value);
			}
			value = value.replaceFirst(":", ":%");
			whereClause = " where upper(c_basecode) like '%" +  value.toUpperCase() + "' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";    {ESCAPE '?'}";

		}

		else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
			if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
				value = StringUtil.escapeSQLSERVER(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
				value = StringUtil.escapeORACLE(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
				value = StringUtil.escapePOSTGRESQL(value);
			}
			else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
				value = StringUtil.escapeSNOWFLAKE(value);
			}
			value = value.replaceFirst(":", ":%");
			whereClause = " where upper(c_basecode) like '%" + value.toUpperCase() + "%' " + (!(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL") || dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")) ? "{ESCAPE '?'}" : "" )	;  //{ESCAPE '?'}";    {ESCAPE '?'}";
		}

		String codeInfoSql = "select c_fullname from " + metadataSchema + tableName + whereClause;


		String modifierPath = StringUtil.getLiteralPath(vocabType.getSelf());
		if(modifierPath.contains("'")){
			modifierPath = modifierPath.replaceAll("'", "''");
		}
		String appliedPath   = " and m_applied_path = '" + modifierPath + "' ";
		String inclusionSql = codeInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is null ";
		String modifierSelect = "'" +	modifierPath + "'";
		while (modifierPath.length() > 3) {
			if(modifierPath.endsWith("%")){
				modifierPath = modifierPath.substring(0, modifierPath.length()-2);
				modifierPath = modifierPath.substring(0, modifierPath.lastIndexOf("\\") + 1) + "%";			
			}
			else
				modifierPath = modifierPath + "%";
			modifierSelect = modifierSelect + ", '" + modifierPath + "'";
			appliedPath = " and m_applied_path = '" + modifierPath + "' ";
			inclusionSql = inclusionSql + " union all " + codeInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is null ";
		}

		String exclusionSql = codeInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is not null";
		modifierPath = StringUtil.getLiteralPath(vocabType.getSelf());
		if(modifierPath.contains("'")){
			modifierPath = modifierPath.replaceAll("'", "''");
		}
		while (modifierPath.length() > 3) {
			if(modifierPath.endsWith("%")){
				modifierPath = modifierPath.substring(0, modifierPath.length()-2);
				modifierPath = modifierPath.substring(0, modifierPath.lastIndexOf("\\") + 1) + "%";			
			}
			else
				modifierPath = modifierPath + "%";
			appliedPath = " and m_applied_path = '" + modifierPath + "' ";
			exclusionSql = exclusionSql + " union all " + codeInfoSql + appliedPath + hidden + synonym + " and m_exclusion_cd is not null ";
		}

		String exceptSql =  " EXCEPT (";
		if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE"))
			exceptSql =	" MINUS (";


		String 	modCodeInfoSql = " select " + parameters + " from "  + metadataSchema + tableName +  " where m_exclusion_cd is null and m_applied_path in ("
				+ modifierSelect +") and c_fullname in (" 	+ inclusionSql	+ exceptSql + exclusionSql 	+ ")) order by (c_name) ";


		//		log.debug("MODCodeInfo " + modCodeInfoSql);
		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		//ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			queryResult = jt.query(modCodeInfoSql, getModNodeMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType()));
		} catch (DataAccessException e) {
			log.error("Get Modifier by Code " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}


		log.debug("Mod search by code result size = " + queryResult.size());

		return queryResult;

	} 

	public List findDerivedFactColumns(final GetTermInfoType termInfoType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}

		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}
		boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		//tableCd to table name conversion



		String hidden = "";
		if(termInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		//extract table code
		String tableCd = StringUtil.getTableCd(termInfoType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? " + hidden;
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error("Get Derived Fact Columns " + e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? " + hidden;
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error("Get Derived Fact Columns " + e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String path = StringUtil.getPath(termInfoType.getSelf());
		/*
		if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
			//path = path.replaceAll("\\[", "[[]");
			path = StringUtil.escapeSQLSERVER(path);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
			path = StringUtil.escapeORACLE(path);
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
			path = StringUtil.escapePOSTGRESQL(path); 
		}		
		 */

		String searchPath = path;


		String synonym = "";
		if(termInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		String sql = "select distinct(c_facttablecolumn) from " + metadataSchema+tableName  + " where c_facttablecolumn is not null and c_fullname like ? {ESCAPE '?'}" ;
		sql = sql + hidden + synonym ;

		if(dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER")){
			searchPath = StringUtil.escapeSQLSERVER(path);
			searchPath += "%";
		}

		else if(dbInfo.getDb_serverType().toUpperCase().equals("ORACLE")){
			searchPath = StringUtil.escapeORACLE(path); 
			searchPath += "%";
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL")){
			searchPath = StringUtil.escapePOSTGRESQL(path); 
			searchPath += "%";
		}
		else if(dbInfo.getDb_serverType().toUpperCase().equals("SNOWFLAKE")){
			searchPath = StringUtil.escapeSNOWFLAKE(path);
			searchPath += "%";
		}

		//ParameterizedRowMapper<String> columnMapper = getColumnMapper();

		List queryResult = null;
		try {
			queryResult = jt.queryForList(sql, String.class, searchPath );
		} catch (DataAccessException e) {
			log.error("Get Derived Fact Columna " + e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		log.debug("Derived Fact columns result size = " + queryResult.size());


		return queryResult;

	}


	private GetConceptXMLMapper getConceptXMLMapper(DBInfoType dbInfo) {
		GetConceptXMLMapper mapper= new GetConceptXMLMapper();
		mapper.setdbInfo(dbInfo);
		return mapper;
	}


	private GetModNodeMapper getModNodeMapper(NodeType nodeType, boolean obfuscatedUserFlag,
			String db_serverType) {
		// TODO Auto-generated method stub
		GetModNodeMapper mapper = new GetModNodeMapper();
		mapper.setDbType(db_serverType);
		mapper.setNodeType(nodeType);
		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);
		return mapper;
	}


	private GetConceptNodeMapper getConceptNodeMapper(NodeType nodeType, boolean obfuscatedUserFlag,
			String db_serverType) {
		// TODO Auto-generated method stub
		GetConceptNodeMapper mapper = new GetConceptNodeMapper();
		mapper.setDbType(db_serverType);
		mapper.setNodeType(nodeType);
		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);
		return mapper;
	}

	private GetConceptFullNameMapper getConceptFullNameMapper(GetCategoriesType returnType, ProjectType projectInfo,
			boolean obfuscatedUserFlag) {
		GetConceptFullNameMapper mapper = new GetConceptFullNameMapper();
		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);
		mapper.setProjectInfo(projectInfo);
		mapper.setReturnType(returnType);
		return mapper;
	}


}


class GetConceptNodeMapper implements RowMapper<ConceptType> {

	boolean ofuscatedUserFlag;
	NodeType node;
	String dbType;


	public void setObfuscatedUserFlag(boolean obfuscatedUserFlag) {
		this.ofuscatedUserFlag = obfuscatedUserFlag;
	}


	public void setNodeType(NodeType nodeType) {
		this.node = nodeType;
	}


	public void setDbType(String dbType) {
		this.dbType = dbType;
	}


	@Override
	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType child = new ConceptType();	          
		child.setName(rs.getString("c_name"));
		if(!(node.getType().equals("default"))){
			child.setBasecode(rs.getString("c_basecode"));
			child.setLevel(rs.getInt("c_hlevel"));
			if (node.getParent().equals("terminfo")) {
				child.setProtectedAccess(rs.getString("c_protected_access"));
				child.setOntologyProtection(rs.getString("c_ontology_protection"));
			}
			// cover get Code Info case where we dont know the vocabType.category apriori
			if ((node.getNode() != null) && !node.getNode().equals("@"))
				child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
			else
				child.setKey("\\\\" + rs.getString("tableCd") + rs.getString("c_fullname")); 
			child.setSynonymCd(rs.getString("c_synonym_cd"));
			child.setVisualattributes(rs.getString("c_visualattributes"));
			Integer totalNumValue = rs.getInt("c_totalnum");
			boolean nullFlag = rs.wasNull();

			/*
				if (nullFlag) { 
					("null in totalnum flag ");
				} else { 
					("not null in totalnum flag ");
				}

				if (rs.getString("c_totalnum") == null) { 
					("null in totalnum flag using getString method");
				} else { 
					("not null in totalnum flag using getString method  [" + rs.getString("c_totalnum") + "]");
				}
			 */
			if ( ofuscatedUserFlag == false && nullFlag == false) { 
				child.setTotalnum(totalNumValue);
			}
			child.setTooltip(rs.getString("c_tooltip"));
			child.setValuetypeCd(rs.getString("valuetype_cd"));
			if(!(node.getType().equals("limited"))) {
				child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
				child.setTablename(rs.getString("c_tablename")); 
				child.setColumnname(rs.getString("c_columnname")); 
				child.setColumndatatype(rs.getString("c_columndatatype")); 
				child.setOperator(rs.getString("c_operator")); 
				child.setDimcode(rs.getString("c_dimcode")); 
			}
		}
		if(node.isBlob() == true){
			try {
				if (dbType.equals("POSTGRESQL"))
				{
					if(rs.getString("c_comment") == null)
						child.setComment(null);
					else
						child.setComment(rs.getString("c_comment"));
				}
				else if (dbType.equals("SNOWFLAKE"))
				{
					if(rs.getString("c_comment") == null)
						child.setComment(null);
					else
						child.setComment(rs.getString("c_comment"));
				}
				else {

					if(rs.getClob("c_comment") == null)
						child.setComment(null);
					else 
						child.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
				}
			} catch (IOException e) {
				child.setComment(null);
			} 

			String c_xml = null;
			try {

				if (dbType.equals("POSTGRESQL"))
					c_xml = rs.getString("c_metadataxml");
				else if (dbType.equals("SNOWFLAKE"))
					c_xml = rs.getString("c_metadataxml");
				else if (rs.getClob("c_metadataxml") != null)
					c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
			} catch (IOException e) {
				child.setMetadataxml(null);
			}

			if(c_xml == null){
				child.setMetadataxml(null);
			}else {
				if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
				{
					Element rootElement = null;
					try {
						Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
						rootElement = doc.getDocumentElement();
					} catch (IOException e) {
						child.setMetadataxml(null);
					} catch (Exception e1) {
						child.setMetadataxml(null);
					}
					if (rootElement != null) {
						XmlValueType xml = new XmlValueType();
						xml.getAny().add(rootElement);
						child.setMetadataxml(xml);
					}
				}else {
					child.setMetadataxml(null);
				}
			}	

		}
		if((node.getType().equals("all"))){
			DTOFactory factory = new DTOFactory();
			// make sure date isnt null before converting to XMLGregorianCalendar
			Date date = rs.getDate("update_date");
			if (date == null)
				child.setUpdateDate(null);
			else 
				child.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("download_date");
			if (date == null)
				child.setDownloadDate(null);
			else 
				child.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("import_date");
			if (date == null)
				child.setImportDate(null);
			else 
				child.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 

			child.setSourcesystemCd(rs.getString("sourcesystem_cd"));

		}
		return child;
	}

}



class GetConceptNameMapper implements RowMapper<ConceptType> {
	@Override
	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType category = new ConceptType();	 

		category.setTablename(rs.getString("c_table_name"));
		category.setKey(rs.getString("c_fullname"));
		category.setName(rs.getString("c_name"));
		return category;
	}
}

class GetConceptParentMapper implements RowMapper<ConceptType> {
	@Override
	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType category = new ConceptType();	 

		//category.setLevel(rs.getInt("c_hevel"));
		category.setName(rs.getString("c_name"));
		return category;
	}
}


class GetConceptFullNameMapper implements RowMapper<ConceptType> {
	boolean obfuscatedUserFlag;
	GetCategoriesType returnType;
	public void setReturnType(GetCategoriesType returnType) {
		this.returnType = returnType;
	}


	public void setProjectInfo(ProjectType projectInfo) {
		this.projectInfo = projectInfo;
	}


	ProjectType projectInfo;

	public void setObfuscatedUserFlag(boolean obfuscatedUserFlag) {
		this.obfuscatedUserFlag = obfuscatedUserFlag;
	}


	@Override
	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType child = new ConceptType();
		//TODO fix this for all
		child.setKey("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_fullname")); 
		child.setName(rs.getString("c_name"));
		if(returnType.getType().equals("limited")) {
			child.setBasecode(rs.getString("c_basecode"));
			child.setLevel(rs.getInt("c_hlevel"));
			child.setSynonymCd(rs.getString("c_synonym_cd"));
			child.setVisualattributes(rs.getString("c_visualattributes"));

			child.setTooltip(rs.getString("c_tooltip"));
			child.setValuetypeCd(rs.getString("valuetype_cd"));
			child.setProtectedAccess(rs.getString("c_protected_access"));
			child.setOntologyProtection(rs.getString("c_ontology_protection"));

		}
		else if(returnType.getType().equals("core")) {
			child.setBasecode(rs.getString("c_basecode"));
			child.setLevel(rs.getInt("c_hlevel"));
			child.setSynonymCd(rs.getString("c_synonym_cd"));
			child.setVisualattributes(rs.getString("c_visualattributes"));
			child.setProtectedAccess(rs.getString("c_protected_access"));
			child.setOntologyProtection(rs.getString("c_ontology_protection"));

			Integer totalNum = rs.getInt("c_totalnum");
			boolean nullFlag = rs.wasNull();


			if (nullFlag) { 
			} else { 
			}

			if (rs.getString("c_totalnum") == null) { 
			} else { 
			}

			if (obfuscatedUserFlag == false && nullFlag == false) {
				child.setTotalnum(totalNum);
			} 


			child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
			child.setTablename(rs.getString("c_dimtablename")); 
			child.setColumnname(rs.getString("c_columnname")); 
			child.setColumndatatype(rs.getString("c_columndatatype")); 
			child.setOperator(rs.getString("c_operator")); 
			child.setDimcode(rs.getString("c_dimcode")); 
			child.setTooltip(rs.getString("c_tooltip"));
			child.setValuetypeCd(rs.getString("valuetype_cd"));
		}
		if (child.getProtectedAccess().equalsIgnoreCase("Y"))
		{
			Boolean protectedAccess = false;
			String[] dataProt = {"DATA_PROT"};
			List<String> ontologyProtection = Arrays.asList(child.getOntologyProtection() == null || child.getOntologyProtection().equals("")?dataProt:child.getOntologyProtection().split(","));
			for (String s: projectInfo.getRole()) {
				if (ontologyProtection.contains(s))
					protectedAccess = true;

			}
			if (protectedAccess == false)
				child = null;
		}

		return child;
	}
}




class GetModNodeMapper implements RowMapper<ModifierType> {

	boolean ofuscatedUserFlag;
	NodeType node;
	String dbType;


	public void setObfuscatedUserFlag(boolean obfuscatedUserFlag) {
		this.ofuscatedUserFlag = obfuscatedUserFlag;
	}


	public void setNodeType(NodeType nodeType) {
		this.node = nodeType;
	}


	public void setDbType(String dbType) {
		this.dbType = dbType;
	}


	@Override
	public ModifierType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ModifierType child = new ModifierType();	          
		if(node.getType().equals("limited")){
			child.setName(rs.getString("c_name"));
			child.setAppliedPath(rs.getString("m_applied_path"));
			child.setBasecode(rs.getString("c_basecode"));
			child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
			child.setLevel(rs.getInt("c_hlevel"));
			child.setFullname(rs.getString("c_fullname"));  
			child.setVisualattributes(rs.getString("c_visualattributes"));
			child.setSynonymCd(rs.getString("c_synonym_cd"));
			child.setTooltip(rs.getString("c_tooltip"));
		}else{
			child.setName(rs.getString("c_name"));
			child.setAppliedPath(rs.getString("m_applied_path"));
			child.setBasecode(rs.getString("c_basecode"));
			child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
			child.setLevel(rs.getInt("c_hlevel"));
			child.setFullname(rs.getString("c_fullname"));  
			child.setVisualattributes(rs.getString("c_visualattributes"));
			child.setSynonymCd(rs.getString("c_synonym_cd"));
			child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
			child.setTooltip(rs.getString("c_tooltip"));
			child.setTablename(rs.getString("c_tablename")); 
			child.setColumnname(rs.getString("c_columnname")); 
			child.setColumndatatype(rs.getString("c_columndatatype")); 
			child.setOperator(rs.getString("c_operator")); 
			child.setDimcode(rs.getString("c_dimcode")); 
		}

		if(node.isBlob() == true){
			try {
				if (dbType.equals("POSTGRESQL"))
				{
					if(rs.getString("c_comment") == null)
						child.setComment(null);
					else
						child.setComment(rs.getString("c_comment"));

				}
				else if (dbType.equals("SNOWFLAKE"))
				{
					if(rs.getString("c_comment") == null)
						child.setComment(null);
					else
						child.setComment(rs.getString("c_comment"));

				}
				else {
					if(rs.getClob("c_comment") == null)
						child.setComment(null);
					else
						child.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
				}
			} catch (IOException e) {
				child.setComment(null);
			} 


			String c_xml = null;
			try {

				if (dbType.equals("POSTGRESQL"))
					c_xml = rs.getString("c_metadataxml");
				else if (dbType.equals("SNOWFLAKE"))
					c_xml = rs.getString("c_metadataxml");
				else  if (rs.getClob("c_metadataxml") != null)
					c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
			} catch (IOException e) {
				child.setMetadataxml(null);
			}

			if(c_xml == null){
				child.setMetadataxml(null);
			}else {

				if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
				{
					Element rootElement = null;
					try {
						Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
						rootElement = doc.getDocumentElement();
					} catch (IOException e) {
						child.setMetadataxml(null);
					} catch (Exception e1) {
						child.setMetadataxml(null);
					}
					if (rootElement != null) {
						XmlValueType xml = new XmlValueType();
						xml.getAny().add(rootElement);
						child.setMetadataxml(xml);
					}
				}else {
					child.setMetadataxml(null);
				}
			}	

		}
		if((node.getType().equals("all"))){
			DTOFactory factory = new DTOFactory();
			// make sure date isnt null before converting to XMLGregorianCalendar
			Date date = rs.getDate("update_date");
			if (date == null)
				child.setUpdateDate(null);
			else 
				child.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("download_date");
			if (date == null)
				child.setDownloadDate(null);
			else 
				child.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("import_date");
			if (date == null)
				child.setImportDate(null);
			else 
				child.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 

			child.setSourcesystemCd(rs.getString("sourcesystem_cd"));

		}
		return child;
	}

}

class GetConceptXMLMapper implements RowMapper<ConceptType> {

	DBInfoType dbInfo;





	public void setdbInfo(DBInfoType dbInfo) {
		this.dbInfo = dbInfo;
	}


	@Override	

	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType concept = new ConceptType();
		//			        	ResultSetMetaData rsmd = rs.getMetaData();
		//			        	rsmd.get

		String c_xml = null;
		try {

			if (dbInfo.getDb_serverType().equals("POSTGRESQL"))
				c_xml = rs.getString("c_metadataxml");
			else if (dbInfo.getDb_serverType().equals("SNOWFLAKE"))
				c_xml = rs.getString("c_metadataxml");
			else  if (rs.getClob("c_metadataxml") != null)
				c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
		} catch (IOException e) {
			concept.setMetadataxml(null);
		}

		if(c_xml == null){
			concept.setMetadataxml(null);
		}else {

			if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
			{
				//SAXBuilder parser = new SAXBuilder();
				java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
				Element rootElement = null;
				try {
					Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
					rootElement = doc.getDocumentElement();
				} catch (IOException e) {
					concept.setMetadataxml(null);
				} catch (Exception e) {
					concept.setMetadataxml(null);
				}
				if(rootElement != null) {
					XmlValueType xml = new XmlValueType();									
					xml.getAny().add(rootElement);								
					concept.setMetadataxml(xml);
				}
			}else {
				concept.setMetadataxml(null);
			}
		}	

		try {
			if (dbInfo.getDb_serverType().equals("POSTGRESQL"))
			{
				concept.setComment(rs.getString("c_comment"));
			}
			else if (dbInfo.getDb_serverType().equals("SNOWFLAKE"))
			{
				concept.setComment(rs.getString("c_comment"));
			}
			else  if (rs.getClob("c_comment") != null)
			{
				concept.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
			}
		}
		catch (Exception e)
		{
			concept.setComment(null);
		}

		return concept;
	}
}