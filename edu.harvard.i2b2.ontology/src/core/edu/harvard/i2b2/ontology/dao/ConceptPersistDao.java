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

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifierType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifyChildType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyDataType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.datavo.vdo.DeleteChildType;

import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.StringUtil;


public class ConceptPersistDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(ConceptPersistDao.class);

	private JdbcTemplate jt;

	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new JdbcTemplate(ds);
	}

	public int addNode(final ConceptType addChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
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



		//extract table code
		String tableCd = StringUtil.getTableCd(addChildType.getKey());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(tableSql + tableCd);
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		int numRowsAdded = -1;
		try {
			Date today = Calendar.getInstance().getTime();
			String xml = null;
			XmlValueType metadataXml=addChildType.getMetadataxml();
			if (metadataXml != null) {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_metadataxml, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date, sourcesystem_cd, valuetype_cd, m_applied_path, c_path, c_symbol) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);

				Element element = metadataXml.getAny().get(0);
				if(element != null){
					xml = XMLUtil.convertDOMElementToString(element);
					xml = xml.replaceAll("\n", "");
				}

				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), xml, addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(),today,  today,today, addChildType.getSourcesystemCd() ,addChildType.getValuetypeCd(), "@", StringUtil.getCpath(addChildType.getKey()), StringUtil.getSymbol(addChildType.getKey()));
			}		
			else {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date,sourcesystem_cd, valuetype_cd, m_applied_path, c_path, c_symbol) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(), today, today,today, addChildType.getSourcesystemCd() ,addChildType.getValuetypeCd(), "@", StringUtil.getCpath(addChildType.getKey()), StringUtil.getSymbol(addChildType.getKey()));
			}
		} catch (DataAccessException e) {
			log.error("Dao addChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

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

		Boolean protectedAccess = false;
		Iterator<String> it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		//extract table code
		String tableCd = StringUtil.getTableCd(deleteChildType.getKey());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}	

		String deleteChildrenSql = null;
		String deleteSql = " delete from " + metadataSchema+tableName  + " where c_fullname = ? and c_basecode = ?";
		if(deleteChildType.isIncludeChildren()){	
			deleteChildrenSql =  " delete from " + metadataSchema+tableName  + " where c_fullname like ? and c_visualattributes like '%E'";
		}
		int numRowsDeleted = -1;
		try{	
			numRowsDeleted = jt.update(deleteSql, StringUtil.getPath(deleteChildType.getKey()), deleteChildType.getBasecode());
			if(deleteChildrenSql != null)
				numRowsDeleted += jt.update(deleteChildrenSql, StringUtil.getPath(deleteChildType.getKey())+"%");
		} catch (DataAccessException e) {
			log.error("Dao deleteChild failed");
			log.error(e.getMessage());
			throw e;
		}
		log.debug("Number of rows deleted " + numRowsDeleted);
		return numRowsDeleted;

	}

	public int modifyNode(final ModifyChildType modifyChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
		String metadataSchema = dbInfo.getDb_fullSchema();
		//		String serverType = dbInfo.getDb_serverType();
		setDataSource(dbInfo.getDb_dataSource());

		Date today = Calendar.getInstance().getTime();	
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		Boolean protectedAccess = false;
		Iterator<String> it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}


		//extract table code
		String tableCd = null;
		if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null)){
			tableCd = StringUtil.getTableCd(modifyChildType.getSelf().getKey());
			log.info("path: " + StringUtil.getPath(modifyChildType.getSelf().getKey()));
		}
		else {
			tableCd = StringUtil.getTableCd(modifyChildType.getSelf().getModifier().getKey());
			log.info("path: " + StringUtil.getPath(modifyChildType.getSelf().getModifier().getKey()));
		}
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		//		log.info("path: " + StringUtil.getPath(modifyChildType.getSelf().getKey()));


		String updateSql = " update " + metadataSchema+tableName  + " set update_date = ?, c_visualattributes = ?, c_tooltip = ?, c_name = ?, c_basecode = ?, valuetype_cd = ?, " +
				" c_tablename = ?, c_columnname = ?, c_facttablecolumn = ?, c_operator = ?, c_columndatatype = ?, c_metadataxml = ? where c_fullname = ? and c_synonym_cd = 'N'";

		//		log.info(updateSql);
		String xml = "";
		int numRowsModified= -1;
		try {

			XmlValueType metadataXml = null;
			if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null))
				metadataXml=modifyChildType.getSelf().getMetadataxml();
			else
				metadataXml=modifyChildType.getSelf().getModifier().getMetadataxml();
			if (metadataXml != null){
				Element element = metadataXml.getAny().get(0);
				if(element != null){
					xml = XMLUtil.convertDOMElementToString(element);
					xml = xml.replaceAll("\n", "");
				}
			}
			if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null)){
				//				log.debug("no modifier present");
				numRowsModified = jt.update(updateSql,today, modifyChildType.getSelf().getVisualattributes(), modifyChildType.getSelf().getTooltip(),
						modifyChildType.getSelf().getName(), modifyChildType.getSelf().getBasecode(), modifyChildType.getSelf().getValuetypeCd(), 
						modifyChildType.getSelf().getTablename(), modifyChildType.getSelf().getColumnname(),  modifyChildType.getSelf().getFacttablecolumn(),  modifyChildType.getSelf().getOperator(),  
						modifyChildType.getSelf().getColumndatatype(), xml, StringUtil.getPath(modifyChildType.getSelf().getKey()));
			}
			else {
				//			log.debug("updating modifier " + modifyChildType.getSelf().getModifier().getName());
				numRowsModified = jt.update(updateSql,today, modifyChildType.getSelf().getModifier().getVisualattributes(), modifyChildType.getSelf().getModifier().getTooltip(),
						modifyChildType.getSelf().getModifier().getName(), modifyChildType.getSelf().getModifier().getBasecode(), "", 
						modifyChildType.getSelf().getModifier().getTablename(), modifyChildType.getSelf().getModifier().getColumnname(),  modifyChildType.getSelf().getModifier().getFacttablecolumn(),  modifyChildType.getSelf().getModifier().getOperator(),  
						modifyChildType.getSelf().getModifier().getColumndatatype(), xml, StringUtil.getPath(modifyChildType.getSelf().getModifier().getKey()));

				//	log.debug("1.Number of rows modified " + numRowsModified);
			}
			if(modifyChildType.isInclSynonyms()){
				// apply the modification to the synonyms as well.

				String updateSynonymsSql = " update " + metadataSchema+tableName  + " set update_date = ?, c_visualattributes = ?, c_tooltip = ?,c_basecode = ?, valuetype_cd = ?, " +
						" c_tablename = ?, c_columnname = ?, c_facttablecolumn = ?, c_operator = ?, c_columndatatype = ?, c_metadataxml = ? where c_fullname = ? and c_synonym_cd = 'Y'";

				//		log.info(updateSynonymsSql);


				if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null)){
					//		log.debug("SYN: updating modifier " + modifyChildType.getSelf().getModifier().getName());
					numRowsModified += jt.update(updateSynonymsSql,today, modifyChildType.getSelf().getVisualattributes(), modifyChildType.getSelf().getTooltip(),
							modifyChildType.getSelf().getBasecode(), modifyChildType.getSelf().getValuetypeCd(), 
							modifyChildType.getSelf().getTablename(), modifyChildType.getSelf().getColumnname(),  modifyChildType.getSelf().getFacttablecolumn(),  modifyChildType.getSelf().getOperator(),  
							modifyChildType.getSelf().getColumndatatype(), xml, StringUtil.getPath(modifyChildType.getSelf().getKey()));
					//		
				}

				else{
					//		log.debug("SYN: no modifier present");
					numRowsModified += jt.update(updateSynonymsSql,today, modifyChildType.getSelf().getModifier().getVisualattributes(), modifyChildType.getSelf().getModifier().getTooltip(),
							modifyChildType.getSelf().getModifier().getBasecode(), "", 
							modifyChildType.getSelf().getModifier().getTablename(), modifyChildType.getSelf().getModifier().getColumnname(),  modifyChildType.getSelf().getModifier().getFacttablecolumn(),  modifyChildType.getSelf().getModifier().getOperator(),  
							modifyChildType.getSelf().getModifier().getColumndatatype(), "", StringUtil.getPath(modifyChildType.getSelf().getModifier().getKey()));

				}
				//		log.debug("2. Number of rows modified " + numRowsModified);
			}

			else{  // else we are not including synonyms ; 
				// this is the case where we modified the synonyms list so we dont include them
				//  in the general modify case; we delete them; the client then sends addChild for
				//   each of them
				String deleteSynonymsSql = "delete from "+ metadataSchema+tableName  + " where c_fullname = ? and c_synonym_cd = 'Y'";
				//	log.info(deleteSynonymsSql);
				int numRowsDeleted = -1;
				if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null))
					numRowsDeleted = jt.update(deleteSynonymsSql, StringUtil.getPath(modifyChildType.getSelf().getKey()));

				else
					numRowsDeleted = jt.update(deleteSynonymsSql, StringUtil.getPath(modifyChildType.getSelf().getModifier().getKey()));
				//		log.debug("Number of rows deleted " + numRowsDeleted);
			}

		} catch (DataAccessException e) {
			log.error("Dao modifyChild failed");
			log.error(e.getMessage());
			throw e;
		}

		log.debug("Number of rows modified " + numRowsModified);
		return numRowsModified;

	}
	public int dirtyCandidate(final ModifyChildType modifyChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
		String metadataSchema = dbInfo.getDb_fullSchema();
		//		String serverType = dbInfo.getDb_serverType();
		setDataSource(dbInfo.getDb_dataSource());


		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		Boolean protectedAccess = false;
		Iterator<String> it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		//extract table code
		String tableCd = null;
		if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null))	
			tableCd = StringUtil.getTableCd(modifyChildType.getSelf().getKey());

		else
			tableCd = StringUtil.getTableCd(modifyChildType.getSelf().getModifier().getKey());	
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String countSql = "select count(*) from " + metadataSchema+tableName  + " where c_name = ? and c_basecode = ? and c_fullname = ? and c_visualattributes = ?";

		log.info(countSql);

		int count= -1;
		try {
			if((modifyChildType.getSelf().getModifier() == null)||(modifyChildType.getSelf().getModifier().getName() == null))
				count = jt.queryForObject(countSql,Integer.class, modifyChildType.getSelf().getName(), modifyChildType.getSelf().getBasecode(),
						StringUtil.getPath(modifyChildType.getSelf().getKey()), modifyChildType.getSelf().getVisualattributes());
			else
				count = jt.queryForObject(countSql,Integer.class,modifyChildType.getSelf().getModifier().getName(), modifyChildType.getSelf().getModifier().getBasecode(),
						StringUtil.getPath(modifyChildType.getSelf().getModifier().getKey()), modifyChildType.getSelf().getModifier().getVisualattributes());

		} catch (DataAccessException e) {
			log.error("Dao modifyChild failed");
			log.error(e.getMessage());
			throw e;
		}

		log.debug("Dirty candidate check yielded " + count + " entries");
		return count;
	}

	public int addNode(final ModifierType addChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
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

		//extract table code
		String tableCd = StringUtil.getTableCd(addChildType.getKey());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(tableSql + tableCd);
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		int numRowsAdded = -1;
		try {
			Date today = Calendar.getInstance().getTime();
			String xml = null;
			XmlValueType metadataXml=addChildType.getMetadataxml();
			if (metadataXml != null) {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_metadataxml, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date, sourcesystem_cd, m_applied_path, c_path, c_symbol) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);

				Element element = metadataXml.getAny().get(0);
				if(element != null){
					xml = XMLUtil.convertDOMElementToString(element);
					xml = xml.replaceAll("\n", "");
				}
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), xml, addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(),today,  today,today, addChildType.getSourcesystemCd() ,addChildType.getAppliedPath(),StringUtil.getCpath(addChildType.getKey()), StringUtil.getSymbol(addChildType.getKey()));
			}		
			else {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date,sourcesystem_cd, m_applied_path, c_path, c_symbol) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(), today, today,today, addChildType.getSourcesystemCd() ,addChildType.getAppliedPath(),StringUtil.getCpath(addChildType.getKey()), StringUtil.getSymbol(addChildType.getKey()));
			}
		} catch (DataAccessException e) {
			log.error("Dao addNode failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}

	public int excludeNode(final ModifierType addChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
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

		//extract table code
		String tableCd = StringUtil.getTableCd(addChildType.getKey());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(tableSql + tableCd);
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, String.class, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		int numRowsAdded = -1;
		try {
			Date today = Calendar.getInstance().getTime();
			if(addChildType.getComment() == null)
				addChildType.setComment("");
			String xml = null;
			XmlValueType metadataXml=addChildType.getMetadataxml();
			if (metadataXml != null) {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_metadataxml, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date, sourcesystem_cd, m_applied_path, m_exclusion_cd) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);

				Element element = metadataXml.getAny().get(0);
				if(element != null){
					xml = XMLUtil.convertDOMElementToString(element);
					xml = xml.replaceAll("\n", "");
				}
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), xml, addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(),today,  today,today, addChildType.getSourcesystemCd() ,addChildType.getAppliedPath(), "X");
			}		
			else {
				String addSql = "insert into " + metadataSchema+tableName  + 
						"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date,sourcesystem_cd, m_applied_path, m_exclusion_cd) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(), today, today,today, addChildType.getSourcesystemCd() ,addChildType.getAppliedPath(), "X");
			}
		} catch (DataAccessException e) {
			log.error("Dao excludeNode failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		log.debug("Number of exclusion rows added: " + numRowsAdded);

		return numRowsAdded;

	}


	public int checkForTableExistence(DBInfoType dbInfo, String tableName) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		String checkForTableSql = "SELECT count(*) from information_schema.tables where table_name = ?";

		if(dbInfo.getDb_serverType().equals("ORACLE"))
			checkForTableSql = "SELECT count(*) from user_tab_cols where table_name = ?";


		if(dbInfo.getDb_serverType().equals("SQLSERVER"))
			checkForTableSql = "SELECT count(*) from " + metadataSchema.replace("dbo.", "") + "information_schema.tables where table_name = ?";

		//		log.info(checkForTableSql);

		int count = -1;
		try {
			count = jt.queryForObject(checkForTableSql, Integer.class, tableName)	;
			//			log.info(checkForTableSql + " count " + count);
		} catch (Exception e) {

			throw e;
		}

		return count;
	}

	public int checkForTableAccessExistence(DBInfoType dbInfo, String tableName, String fullName) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		String checkForTableSql = "SELECT count(*) from " + metadataSchema + "table_access  where c_table_name = ? and c_fullname = ?";

		int count = -1;
		try {
			count = jt.queryForObject(checkForTableSql, Integer.class, tableName, fullName)	;
			//			log.info(checkForTableSql + " count " + count);
		} catch (Exception e) {

			throw e;
		}

		return count;
	}

	public void truncateMetadataTable(DBInfoType dbInfo, String tableName) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());

		String checkForTableSql = "TRUNCATE TABLE " + metadataSchema + tableName ;

		try {
			jt.update(checkForTableSql);

		} catch (Exception e) {

		}
	}

	public void createMetadataTable(DBInfoType dbInfo, String tableName) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());



		String checkForTableSql = "SELECT count(*) from " + metadataSchema + tableName ;

		/*		if(dbInfo.getDb_serverType().equals("ORACLE"))
			checkForTableSql = "SELECT count(*) from user_tab_cols where table_name = " + metadataSchema +"?";


		if(dbInfo.getDb_serverType().equals("SQLSERVER"))
			checkForTableSql = "SELECT count(*) from " + metadataSchema.replace("dbo.", "") + "information_schema.tables where table_name = ?";

		//		log.info(checkForTableSql);
		 */
		boolean createTables = false;
		try {
			int count = jt.queryForObject(checkForTableSql, Integer.class); //, metadataSchema + tableName)	;
			//		log.info(checkForTableSql + " count " + count);

		} catch (Exception e) {
			createTables = true;
		}


		if (createTables) {
			String createSql = "CREATE TABLE " + metadataSchema + tableName +
					"  (	C_HLEVEL INT			NOT NULL, C_FULLNAME VARCHAR(700)	NOT NULL, C_NAME VARCHAR(2000)		NOT NULL, "+
					" C_SYNONYM_CD CHAR(1)		NOT NULL, C_VISUALATTRIBUTES CHAR(3)	NOT NULL,  C_TOTALNUM INT			NULL, " +
					" C_BASECODE VARCHAR(50)	NULL, C_METADATAXML VARCHAR(MAX)		NULL,   C_FACTTABLECOLUMN VARCHAR(50)	NOT NULL, "+
					" C_TABLENAME VARCHAR(50)	NOT NULL, C_COLUMNNAME VARCHAR(50)	NOT NULL, C_COLUMNDATATYPE VARCHAR(50)	NOT NULL, "+
					" C_OPERATOR VARCHAR(10)	NOT NULL, C_DIMCODE VARCHAR(700)	NOT NULL,  C_COMMENT VARCHAR(MAX)			NULL, "+
					" C_TOOLTIP VARCHAR(900)	NULL, M_APPLIED_PATH VARCHAR(700)	NOT NULL, UPDATE_DATE DATETIME		NOT NULL, "+
					" DOWNLOAD_DATE DATETIME	NULL,  IMPORT_DATE DATETIME	NULL, SOURCESYSTEM_CD VARCHAR(50)	NULL, "+
					" VALUETYPE_CD VARCHAR(50)	NULL, M_EXCLUSION_CD	VARCHAR(25) NULL, C_PATH	VARCHAR(700)   NULL, "+
					" C_SYMBOL	VARCHAR(50)	NULL )  ";


			if(dbInfo.getDb_serverType().equals("POSTGRESQL"))	{
				createSql = "CREATE TABLE " + metadataSchema + tableName +
						"  (	C_HLEVEL INT			NOT NULL, C_FULLNAME VARCHAR(700)	NOT NULL, C_NAME VARCHAR(2000)		NOT NULL, "+
						" C_SYNONYM_CD CHAR(1)		NOT NULL, C_VISUALATTRIBUTES CHAR(3)	NOT NULL,  C_TOTALNUM INT			NULL, " +
						" C_BASECODE VARCHAR(50)	NULL, C_METADATAXML TEXT		NULL,   C_FACTTABLECOLUMN VARCHAR(50)	NOT NULL, "+
						" C_TABLENAME VARCHAR(50)	NOT NULL, C_COLUMNNAME VARCHAR(50)	NOT NULL, C_COLUMNDATATYPE VARCHAR(50)	NOT NULL, "+
						" C_OPERATOR VARCHAR(10)	NOT NULL, C_DIMCODE VARCHAR(700)	NOT NULL,  C_COMMENT TEXT			NULL, "+
						" C_TOOLTIP VARCHAR(900)	NULL, M_APPLIED_PATH VARCHAR(700)	NOT NULL, UPDATE_DATE TIMESTAMP		NOT NULL, "+
						" DOWNLOAD_DATE TIMESTAMP	NULL,  IMPORT_DATE TIMESTAMP	NULL, SOURCESYSTEM_CD VARCHAR(50)	NULL, "+
						" VALUETYPE_CD VARCHAR(50)	NULL, M_EXCLUSION_CD	VARCHAR(25) NULL, C_PATH	VARCHAR(700)   NULL, "+
						" C_SYMBOL	VARCHAR(50)	NULL ) ";
			}
			else if(dbInfo.getDb_serverType().equals("SNOWFLAKE"))	{
				createSql = "CREATE TABLE " + metadataSchema + tableName +
						"  (	C_HLEVEL INT			NOT NULL, C_FULLNAME VARCHAR(700)	NOT NULL, C_NAME VARCHAR(2000)		NOT NULL, "+
						" C_SYNONYM_CD CHAR(1)		NOT NULL, C_VISUALATTRIBUTES CHAR(3)	NOT NULL,  C_TOTALNUM INT			NULL, " +
						" C_BASECODE VARCHAR(50)	NULL, C_METADATAXML TEXT		NULL,   C_FACTTABLECOLUMN VARCHAR(50)	NOT NULL, "+
						" C_TABLENAME VARCHAR(50)	NOT NULL, C_COLUMNNAME VARCHAR(50)	NOT NULL, C_COLUMNDATATYPE VARCHAR(50)	NOT NULL, "+
						" C_OPERATOR VARCHAR(10)	NOT NULL, C_DIMCODE VARCHAR(700)	NOT NULL,  C_COMMENT TEXT			NULL, "+
						" C_TOOLTIP VARCHAR(900)	NULL, M_APPLIED_PATH VARCHAR(700)	NOT NULL, UPDATE_DATE TIMESTAMP		NOT NULL, "+
						" DOWNLOAD_DATE TIMESTAMP	NULL,  IMPORT_DATE TIMESTAMP	NULL, SOURCESYSTEM_CD VARCHAR(50)	NULL, "+
						" VALUETYPE_CD VARCHAR(50)	NULL, M_EXCLUSION_CD	VARCHAR(25) NULL, C_PATH	VARCHAR(700)   NULL, "+
						" C_SYMBOL	VARCHAR(50)	NULL ) ";
			}
			else if(dbInfo.getDb_serverType().equals("ORACLE"))	{
				createSql = "CREATE TABLE " + metadataSchema + tableName +
						"  (	C_HLEVEL NUMBER(22,0)			NOT NULL, C_FULLNAME VARCHAR2(700)	NOT NULL, C_NAME VARCHAR2(2000)		NOT NULL, "+
						" C_SYNONYM_CD CHAR(1)		NOT NULL, C_VISUALATTRIBUTES CHAR(3)	NOT NULL,  C_TOTALNUM NUMBER(22,0)			NULL, " +
						" C_BASECODE VARCHAR2(50)	NULL, C_METADATAXML CLOB		NULL,   C_FACTTABLECOLUMN VARCHAR2(50)	NOT NULL, "+
						" C_TABLENAME VARCHAR2(50)	NOT NULL, C_COLUMNNAME VARCHAR2(50)	NOT NULL, C_COLUMNDATATYPE VARCHAR2(50)	NOT NULL, "+
						" C_OPERATOR VARCHAR2(10)	NOT NULL, C_DIMCODE VARCHAR2(700)	NOT NULL,  C_COMMENT CLOB			NULL, "+
						" C_TOOLTIP VARCHAR2(900)	NULL, M_APPLIED_PATH VARCHAR2(700)	NOT NULL, UPDATE_DATE DATE		NOT NULL, "+
						" DOWNLOAD_DATE DATE	NULL,  IMPORT_DATE DATE	NULL, SOURCESYSTEM_CD VARCHAR2(50)	NULL, "+
						" VALUETYPE_CD VARCHAR2(50)	NULL, M_EXCLUSION_CD	VARCHAR2(25) NULL, C_PATH	VARCHAR2(700)   NULL, "+
						" C_SYMBOL	VARCHAR2(50)	NULL )  ";
			}



			try {
				jt.execute(createSql);

				String indexTableName = tableName;
				if (tableName.length() > 8)
				{
					SecureRandom random = new SecureRandom();

					// generate a random integer from 0 to 899, then add 100
					int x = random.nextInt(900) + 100;
					
					indexTableName = tableName.substring(0,7) + x;
					
					
				}

				String index1Sql = " CREATE INDEX META_FULLNAME_" + indexTableName + "_IDX ON " + metadataSchema+tableName +"(C_FULLNAME)";
				jt.execute(index1Sql);
				String index2Sql = " CREATE INDEX META_APPLIED_PATH_" + indexTableName + "_IDX ON "+  metadataSchema+tableName +"(M_APPLIED_PATH)";
				jt.execute(index2Sql);
				String index3Sql = " CREATE INDEX META_EXCLUSION_" + indexTableName + "_IDX ON " +  metadataSchema+tableName + "(M_EXCLUSION_CD)";
				jt.execute(index3Sql);
				String index4Sql = " CREATE INDEX META_HLEVEL_" + indexTableName + "_IDX ON " +  metadataSchema+tableName + "(C_HLEVEL)";
				jt.execute(index4Sql);
				String index5Sql = " CREATE INDEX META_SYNONYM_" + indexTableName + "_IDX ON " +  metadataSchema+tableName +"(C_SYNONYM_CD)";
				jt.execute(index5Sql);
			} catch (Exception ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
				throw(new I2B2Exception("metadata table or index creation failed"));
			}

		}
		//else
		//	throw new Exception("Metadata Table already exists");

	}


	public void loadTableAccess(DBInfoType dbInfo, final List<OntologyDataType> categories) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());



		String startSql = "insert into " + metadataSchema + "table_access" + 
				"(c_table_cd, c_table_name, c_protected_access, c_ontology_protection, c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_facttablecolumn," +
				"c_totalnum, c_metadataxml, c_dimtablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_comment,c_tooltip," +
				"c_entry_date,c_change_date, c_status_cd, valuetype_cd) "+
				"VALUES (?, ?, ?, ?, ?, ?, ?,?, ?,?, ?,  ?, ?, ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?)";


		log.info(startSql);
		List<Object[]> parameters = new ArrayList<Object[]>();

		for (OntologyDataType concept : categories) {
			// convert XMLValueType to string
			String xml;
			// convert XMLGregorianCalendar to Date
			Date changeDate;
			Date entryDate;
			int totalnum = 0;
			try {
				xml = null;
				if(concept.getMetadataxml() == null){
					//			log.info("metadata xml is null");
				}
				if(concept.getMetadataxml() != null){
					List ele = concept.getMetadataxml().getAny();
					if(ele != null && ele.size() > 0){
						Element element = concept.getMetadataxml().getAny().get(0);
						if(element != null){
							//				log.info("trying element to string");
							xml = XMLUtil.convertDOMElementToString(element);
							xml = xml.replaceAll("\n", "");
						}
					}
				}
				changeDate = null;
				if(concept.getChangeDate() != null) {
					changeDate = concept.getChangeDate().toGregorianCalendar().getTime();
					//			log.info(changeDate.toString());
				}

				entryDate = null;
				if(concept.getEntryDate() != null) {
					entryDate = concept.getEntryDate().toGregorianCalendar().getTime();
					//			log.info(entryDate.toString());
				}

				if(concept.getTotalnum() != null)
					totalnum = concept.getTotalnum().intValue();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("table_access load data conversion error");
				throw e;
			}


			if (checkForTableAccessExistence( dbInfo,  concept.getTableName(), concept.getFullname()) > 0)
			{
				String deleteSql = " delete from " + metadataSchema + "table_access  where c_table_name = ? and c_fullname = ?";

				try {
					jt.update(deleteSql, concept.getTableName(), concept.getFullname())	;
					//			log.info(checkForTableSql + " count " + count);
				} catch (Exception e) {

					//throw e;
				}
			}


			parameters.add(new Object[] { concept.getTableCd(), concept.getTableName(), concept.getProtectedAccess(), concept.getOntologyProtection(), concept.getLevel(), concept.getFullname(), concept.getName(), concept.getSynonymCd(),
					concept.getVisualattributes(), concept.getBasecode(), concept.getFacttablecolumn(), concept.getTotalnum(), xml, concept.getDimtablename(),
					concept.getColumnname(),concept.getColumndatatype(), concept.getOperator(), concept.getDimcode(),  concept.getComment(),
					concept.getTooltip(),  entryDate, changeDate, concept.getStatusCd(), concept.getValuetypeCd()}
					);


		}
		//		log.info("built parameters");

		int[] inserted = {0}; 

		try {
			inserted = jt.batchUpdate(startSql, parameters);
		} catch (DataAccessException e1) {
			log.error(e1.getMessage());
			throw new Exception("Database Error");

		}

	}

	public void loadSchemes(DBInfoType dbInfo, final List<OntologyDataType> schemes) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());


		String startSql = "insert into " + metadataSchema + "schemes" + 
				"(c_key, c_name, c_description) " +
				"VALUES (?, ?, ? )";

		log.info(startSql);

		List<Object[]> parameters = new ArrayList<Object[]>();

		for (OntologyDataType scheme : schemes) {
			parameters.add(new Object[] { scheme.getKey(), scheme.getName(), scheme.getDescription()}
					);
		}	 

		int[] inserted = {0}; 
		try {
			inserted = jt.batchUpdate(startSql, parameters);
		} catch (DataAccessException e1) {
			log.error(e1.getMessage());

			throw new Exception("Database Error");

		}

	}


	public void loadMetadata(DBInfoType dbInfo, String table, final List<OntologyDataType> concepts) throws Exception {

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());


		String startSql = "insert into " + metadataSchema + table + 
				"(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_facttablecolumn," +
				"c_totalnum, c_metadataxml, c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_comment,c_tooltip," +
				"import_date, download_date, update_date, sourcesystem_cd, valuetype_cd, m_applied_path, m_exclusion_cd, c_path, c_symbol) "+
				"VALUES (?, ?, ?, ?, ?, ?, ?, 	?, ?,?, ?, ?, ?,?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?,?)";

		log.info(startSql);


		List<Object[]> parameters = new ArrayList<Object[]>();


		for (OntologyDataType concept : concepts) {
			// Remove existing if they exist


			String xml;
			// convert XMLGregorianCalendar to Date
			Date importDate;
			Date downloadDate;
			Date updateDate;
			int totalnum = 0;
			try {
				xml = null;
				if(concept.getMetadataxml() != null){
					List ele = concept.getMetadataxml().getAny();
					if(ele != null && ele.size() > 0){
						Element element = concept.getMetadataxml().getAny().get(0);
						if(element != null){
							log.info("trying element to string");
							xml = XMLUtil.convertDOMElementToString(element);
							xml = xml.replaceAll("\n", "");
						}
					}
				}
				importDate = null;
				if(concept.getImportDate() != null) {
					importDate = concept.getImportDate().toGregorianCalendar().getTime();
					//			log.info(importDate.toString());
				}

				downloadDate = null;
				if(concept.getDownloadDate() != null) {
					downloadDate = concept.getDownloadDate().toGregorianCalendar().getTime();
					//			log.info(downloadDate.toString());
				}
				updateDate = null;
				if(concept.getUpdateDate() != null) {
					updateDate = concept.getUpdateDate().toGregorianCalendar().getTime();
					//			log.info(updateDate.toString());
				}

				if(concept.getTotalnum() != null)
					totalnum = concept.getTotalnum().intValue();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("data conversion error");
				throw e;
			}

			if(concept.getTotalnum() != null)
				parameters.add(new Object[] {  concept.getLevel(), concept.getFullname(), concept.getName(), concept.getSynonymCd(),
						concept.getVisualattributes(), concept.getBasecode(), concept.getFacttablecolumn(), concept.getTotalnum().intValue(), xml, concept.getDimtablename(),
						concept.getColumnname(),concept.getColumndatatype(), concept.getOperator(), concept.getDimcode(),  concept.getComment(),
						concept.getTooltip(),  importDate, downloadDate,  updateDate, concept.getSourcesystemCd(), 
						concept.getValuetypeCd(), concept.getAppliedPath(), concept.getExclusionCd(), concept.getPath(), concept.getSymbol()}
						);
			else
				parameters.add(new Object[] {  concept.getLevel(), concept.getFullname(), concept.getName(), concept.getSynonymCd(),
						concept.getVisualattributes(), concept.getBasecode(), concept.getFacttablecolumn(), null, xml, concept.getDimtablename(),
						concept.getColumnname(),concept.getColumndatatype(), concept.getOperator(), concept.getDimcode(),  concept.getComment(),
						concept.getTooltip(),  importDate, downloadDate,  updateDate, concept.getSourcesystemCd(), 
						concept.getValuetypeCd(), concept.getAppliedPath(), concept.getExclusionCd(), concept.getPath(), concept.getSymbol()}
						);
		}	 

		int[] inserted = {0}; 
		try {
			inserted = jt.batchUpdate(startSql, parameters);
		} catch (DataAccessException e1) {
			log.error(e1.getMessage());
			throw new Exception("Database Error");

		}

	}
}
