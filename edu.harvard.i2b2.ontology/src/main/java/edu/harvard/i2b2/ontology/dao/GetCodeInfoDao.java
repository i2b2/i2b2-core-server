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
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.util.StringUtil;
import edu.harvard.i2b2.ontology.ejb.ExpandedConceptType;;

public class GetCodeInfoDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(GetCodeInfoDao.class);
	final static String CORE = "c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip";
	final static String ALL = CORE + ", update_date, download_date, import_date, sourcesystem_cd, valuetype_cd";
	final static String DEFAULT = " c_name ";
	final static String BLOB = ", c_metadataxml, c_comment ";	

	public List findCodeInfo(final VocabRequestType vocabType, final List categories, ProjectType projectInfo) throws DataAccessException, I2B2Exception{
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource("java:OntologyLocalDS");
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
			throw e2;
		} 

		 JdbcTemplate jt = new JdbcTemplate(ds);

		// find return parameters
		String parameters = DEFAULT;		
		if (vocabType.getType().equals("core")){
			parameters = CORE;
		}
		else if (vocabType.getType().equals("all")){
			parameters = ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;

		//extract table code
		String tableCd = vocabType.getCategory();
		//		log.info(tableCd);


		// table code to table name conversion
		// Get metadata schema name from properties file.
		String metadataSchema = "";
		try {
			metadataSchema = OntologyUtil.getInstance().getMetaDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
			throw e1;
		}			

		List tableNames = null;
		if(tableCd != null) {
			// table code to table name conversion
			String tableSql = "select distinct(c_table_name) from "+ metadataSchema + "table_access where c_table_cd = ? ";

			tableNames = jt.queryForList(tableSql, String.class, tableCd);
		}
		else {  // tableCd is null, so query all tables user has access to
			String whereClause = "where ";
			Iterator it = categories.iterator();
			while (it.hasNext()){
				ConceptType entry = null;
				if (whereClause.equals("where ")) {
					entry = (ConceptType) it.next();
					whereClause = whereClause + " c_table_cd = '" + StringUtil.getTableCd(entry.getKey()) + "' ";
				}
				else {
					entry = (ConceptType) it.next();
					whereClause = whereClause + " or " + " c_table_cd = '" + StringUtil.getTableCd(entry.getKey())+ "' ";
				}
			}
			String tableSql = "select distinct(c_table_name) from "+ metadataSchema + "table_access " + whereClause;



			try {
				tableNames = jt.queryForList(tableSql, String.class);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		String codeInfoSql = null;
		if(tableNames != null){
			Iterator it = tableNames.iterator();
			String table = (String)it.next();
			String tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
			String basecode = " '" + vocabType.getMatchStr().getValue() + "' ";
			codeInfoSql = "select " + parameters + tableCdSql + " from " + metadataSchema + table + " where upper(c_basecode) =  " + basecode.toUpperCase() 	+ hidden + synonym;;
			while(it.hasNext()){		
				table = (String)it.next();
				tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table + "') as tableCd"; 
				codeInfoSql = codeInfoSql +  " union select "+ parameters + tableCdSql + " from " + metadataSchema + table + " where upper(c_basecode) =  " + basecode.toUpperCase() 
				+ hidden + synonym;
			}
			codeInfoSql = codeInfoSql + " order by c_name ";
		}
		else
			return null;

		log.debug(codeInfoSql);
		final  boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated( projectInfo );
		


		List queryResult = null;
		try {
			if(tableNames != null)
				queryResult = jt.query(codeInfoSql, getExpandedConcept(obfuscatedUserFlag,vocabType));
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());


		if (queryResult != null) {			
			if(parameters != DEFAULT){
//				 fix the key so it equals "\\tableCd\fullname"
				//  in all cases but default
				Iterator it = queryResult.iterator();
				while (it.hasNext()){
					ExpandedConceptType entry = (ExpandedConceptType) it.next();
					entry.setKey("\\\\" + entry.getTableCd() + entry.getKey());
				}
			}

			// Cant gather clobs when you perform unions....
			//  So you have to loop through all the results and gather clobs			
			if (vocabType.isBlob() == true){
				Iterator itr = queryResult.iterator();
				while (itr.hasNext()){
					ConceptType child = (ConceptType) itr.next();
					Iterator it = tableNames.iterator();
					while(it.hasNext()){
						String clobSql = "select c_metadataxml, c_comment from "+  metadataSchema+(String)it.next() +  " where c_name = ? and " + synonym;
						//TODO MM is this being used?   7/5/2019
						/*
						ParameterizedRowMapper<ConceptType> map = new ParameterizedRowMapper<ConceptType>() {
							public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
								ConceptType concept = new ConceptType();
								try {
									if(rs.getClob("c_metadataxml") == null){
										concept.setMetadataxml(null);
									}else {
										String c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
										if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
										{
											Element rootElement = null;
											try {
												Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
						        				rootElement = doc.getDocumentElement();
											} catch (Exception e) {
												log.error(e.getMessage());	
												concept.setMetadataxml(null);
											}
											if (rootElement != null) {
												XmlValueType xml = new XmlValueType();									
												xml.getAny().add(rootElement);								
												concept.setMetadataxml(xml);
											}
										}else {
											concept.setMetadataxml(null);
										}
									}	
								} catch (IOException e) {
									log.error(e.getMessage());
									concept.setMetadataxml(null);
								}
								try {
									if(rs.getClob("c_comment") == null){
										concept.setComment(null);
									}else {
										concept.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
									}	

								} catch (IOException e) {
									log.error(e.getMessage());
									concept.setComment(null);
								}
								return concept;
							}

						};
						List clobResult = null;
						try {
							clobResult = jt.query(clobSql, map, child.getName(), child.getTooltip());
						} catch (DataAccessException e) {
							log.error(e.getMessage());
							throw e;
						}
						if(clobResult != null) {
							if( (((ConceptType)(clobResult.get(0))).getMetadataxml() != null) ||       
									(((ConceptType)(clobResult.get(0))).getComment() != null) ) {

								child.setMetadataxml(((ConceptType)(clobResult.get(0))).getMetadataxml());
								child.setComment(((ConceptType)(clobResult.get(0))).getComment());
								break;
							}
						}
						else {
							child.setMetadataxml(null);
							child.setComment(null);
						}
						*/
					}
				}
			}
		}
		return queryResult;

	}


	private GetExpandedConcept getExpandedConcept(boolean obfuscatedUserFlag, VocabRequestType vocabType) {
		// TODO Auto-generated method stub
		GetExpandedConcept mapper = new GetExpandedConcept();
		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);

		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);
		return mapper;
	}
}


class GetExpandedConcept implements RowMapper<ExpandedConceptType> {

	boolean obfuscatedUserFlag;
	VocabRequestType vocabType;
	
	public void setObfuscatedUserFlag(boolean obfuscatedUserFlag) {
		this.obfuscatedUserFlag = obfuscatedUserFlag;
	}

	public void setVocabType(VocabRequestType vocabType) {
		this.vocabType = vocabType;
	}


	@Override
	public ExpandedConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ExpandedConceptType entry = new ExpandedConceptType();
		//TODO fix this for all/+blob

		entry.setName(rs.getString("c_name"));
		entry.setTableCd(rs.getString("tablecd"));
		if(vocabType.getType().equals("core")) {
			entry.setKey(rs.getString("c_fullname")); 
			entry.setBasecode(rs.getString("c_basecode"));
			entry.setLevel(rs.getInt("c_hlevel"));
			entry.setSynonymCd(rs.getString("c_synonym_cd"));
			entry.setVisualattributes(rs.getString("c_visualattributes"));
			Integer totalNum = rs.getInt("c_totalnum");
			if (obfuscatedUserFlag == false) { 
				entry.setTotalnum(totalNum);
			}
			entry.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
			entry.setTablename(rs.getString("c_tablename")); 
			entry.setColumnname(rs.getString("c_columnname")); 
			entry.setColumndatatype(rs.getString("c_columndatatype")); 
			entry.setOperator(rs.getString("c_operator")); 
			entry.setDimcode(rs.getString("c_dimcode"));
			entry.setTooltip(rs.getString("c_tooltip"));

		}

		if((vocabType.getType().equals("all"))){
			DTOFactory factory = new DTOFactory();
			// make sure date isnt null before converting to XMLGregorianCalendar
			Date date = rs.getDate("update_date");
			if (date == null)
				entry.setUpdateDate(null);
			else 
				entry.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("download_date");
			if (date == null)
				entry.setDownloadDate(null);
			else 
				entry.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 

			date = rs.getDate("import_date");
			if (date == null)
				entry.setImportDate(null);
			else 
				entry.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 

			entry.setSourcesystemCd(rs.getString("sourcesystem_cd"));
			entry.setValuetypeCd(rs.getString("valuetype_cd"));
		}

		return entry;
	}
}
