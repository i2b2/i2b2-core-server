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
import org.springframework.jdbc.core.RowCallbackHandler;
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

public class GetNameInfoDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(GetCodeInfoDao.class);
	final static String CORE = "c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip";
	final static String ALL = CORE + ", update_date, download_date, import_date, sourcesystem_cd, valuetype_cd";
	final static String DEFAULT = " c_name ";
	final static String BLOB = ", c_metadataxml, c_comment ";

	public List findNameInfo(final VocabRequestType vocabType, List categories, ProjectType projectInfo, final String dbType)throws DataAccessException{
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource("java:OntologyLocalDS");
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
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

		// table code to table name conversion
		// Get metadata schema name from properties file.
		String metadataSchema = "";
		try {
			metadataSchema = OntologyUtil.getInstance().getMetaDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}

		// table code to table name conversion
		String tableSql = "select distinct(c_table_name) from " + metadataSchema +"table_access where c_table_cd = ? ";


		String table = jt.queryForObject(tableSql, String.class, tableCd);

		String nameInfoSql = null;
		String compareName = null;

		if(vocabType.getMatchStr().getStrategy().equals("exact")) {
			nameInfoSql = "select " + parameters  + " from " + metadataSchema+table + " where upper(c_name) = ?  ";
			compareName = vocabType.getMatchStr().getValue().toUpperCase();
		}

		else if(vocabType.getMatchStr().getStrategy().equals("left")){
			nameInfoSql = "select " + parameters  + " from " + metadataSchema+table +" where upper(c_name) like ?  ";
			compareName = vocabType.getMatchStr().getValue().toUpperCase() + "%";
		}

		else if(vocabType.getMatchStr().getStrategy().equals("right")) {
			nameInfoSql = "select " + parameters  + " from " + metadataSchema+table +" where upper(c_name) like ?  ";
			compareName =  "%" + vocabType.getMatchStr().getValue().toUpperCase();
		}

		else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
			nameInfoSql = "select " + parameters  + " from " + metadataSchema+table +" where upper(c_name) like ?  ";
			compareName =  "%" + vocabType.getMatchStr().getValue().toUpperCase() + "%";
		}

		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		nameInfoSql = nameInfoSql + hidden + synonym + " order by c_name ";
		final  boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated( projectInfo );


		List queryResult = null;
		try {
			queryResult = jt.query(nameInfoSql, getNamesInfoMapper(obfuscatedUserFlag,vocabType,dbType), compareName);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());

		//		Fix the key so it equals "\\tableCd\fullname"
		if(queryResult != null) {
			Iterator itr = queryResult.iterator();
			while (itr.hasNext()){
				ConceptType entry = (ConceptType) itr.next();
				entry.setKey("\\\\" + tableCd + entry.getKey());
			}		
		}
		return queryResult;
	}

	private GetNamesInfoMapper getNamesInfoMapper(boolean obfuscatedUserFlag, VocabRequestType vocabType,
			String dbType) {
		// TODO Auto-generated method stub
		GetNamesInfoMapper mapper = new GetNamesInfoMapper();
		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);
		mapper.setDbType(dbType);
		mapper.setObfuscatedUserFlag(obfuscatedUserFlag);
		return mapper;
	}

}


class GetNamesInfoMapper implements RowMapper<ConceptType> {

	boolean obfuscatedUserFlag;
	VocabRequestType vocabType;
	String dbType;
	
	
	public void setObfuscatedUserFlag(boolean obfuscatedUserFlag) {
		this.obfuscatedUserFlag = obfuscatedUserFlag;
	}


	public void setVocabType(VocabRequestType vocabType) {
		this.vocabType = vocabType;
	}


	public void setDbType(String dbType) {
		this.dbType = dbType;
	}


	@Override
	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType entry = new ConceptType();
		entry.setName(rs.getString("c_name"));

		if(!(vocabType.getType().equals("default"))) {
			entry.setKey(rs.getString("c_fullname")); 
			entry.setBasecode(rs.getString("c_basecode"));
			entry.setLevel(rs.getInt("c_hlevel"));
			entry.setSynonymCd(rs.getString("c_synonym_cd"));
			entry.setVisualattributes(rs.getString("c_visualattributes"));
			Integer totalNum = rs.getInt("c_totalnum");

			if ( obfuscatedUserFlag == false) { 
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
		if(vocabType.isBlob() == true) {
			if(rs.getClob("c_comment") == null)
				entry.setComment(null);
			else {
				try {
					if (dbType.equals("POSTGRESQL"))
						entry.setComment(rs.getString("c_comment"));
					else if (dbType.equals("SNOWFLAKE"))
						entry.setComment(rs.getString("c_comment"));
					else
						entry.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
				} catch (IOException e1) {
					entry.setComment(null);
				}
			}
			if(rs.getClob("c_metadataxml") == null){
				entry.setMetadataxml(null);
			}else {
				String c_xml = null;
				try {
					if (dbType.equals("POSTGRESQL"))
						c_xml = rs.getString("c_comment");
					else if (dbType.equals("SNOWFLAKE"))
						c_xml = rs.getString("c_comment");
					else
						c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
				} catch (IOException e1) {
					entry.setMetadataxml(null);
				}
				if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
				{
					Element rootElement = null;
					try {
						Document doc = XMLUtil.loadXMLFrom(new java.io.ByteArrayInputStream(c_xml.getBytes()));
						rootElement = doc.getDocumentElement();
					} catch (IOException e) {
						entry.setMetadataxml(null);
					} catch (Exception e1) {
						entry.setMetadataxml(null);
					}
					if (rootElement != null) {
						XmlValueType xml = new XmlValueType();								
						xml.getAny().add(rootElement);							
						entry.setMetadataxml(xml);
					}						
				}else {
					entry.setMetadataxml(null);
				}
			}
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
};
