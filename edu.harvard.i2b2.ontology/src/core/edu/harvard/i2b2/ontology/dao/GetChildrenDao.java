/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
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
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.util.StringUtil;

public class GetChildrenDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(GetChildrenDao.class);
	final static String DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip";
	final static String ALL = DEFAULT + ", update_date, download_date, import_date, sourcesystem_cd, valuetype_cd";
	final static String CORE = DEFAULT;
	final static String BLOB = ", c_metadataxml, c_comment ";
	
	public List findChildrenByParent(final GetChildrenType childrenType, final List categories, final ProjectType projectInfo) throws DataAccessException{

		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource("java:OntologyLocalDS");
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
		} 

		
		
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(ds);
		
		// find return parameters
		String parameters = DEFAULT;		
		if (childrenType.getType().equals("core")){
			parameters = CORE;
		}
		else if (childrenType.getType().equals("all")){
			parameters = ALL;
		}
		if(childrenType.isBlob() == true)
			parameters = parameters + BLOB;
				
		//extract table code
		String tableCd = StringUtil.getTableCd(childrenType.getParent());
				
		// table code to table name conversion
		// Get metadata schema name from properties file.
		String metadataSchema = "";
		try {
			metadataSchema = OntologyUtil.getInstance().getMetaDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}
		
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? ";
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		String tableName = jt.queryForObject(tableSql, map, tableCd);	    
		String path = StringUtil.getPath(childrenType.getParent());
		String searchPath = path + "%";
		
		
// Lookup to get chlevel + 1 ---  dont allow synonyms so we only get one result back
				
		String levelSql = "select c_hlevel from " + metadataSchema+tableName  + " where c_fullname like ?  and c_synonym_cd = 'N'";

	    int level = 0;
		try {
			level = jt.queryForInt(levelSql, path);
		} catch (DataAccessException e1) {
			// should only get 1 result back  (path == c_fullname which should be unique)
			log.error(e1.getMessage());
			throw e1;
		}

		String hidden = "";
		if(childrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(childrenType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ? and c_hlevel = ? "; 
		sql = sql + hidden + synonym + " order by c_name ";
 
		//	log.info(sql + path + level);
		final  boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated( projectInfo );
		
		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            ConceptType child = new ConceptType();	          
	            child.setName(rs.getString("c_name"));
	            child.setBasecode(rs.getString("c_basecode"));
	            child.setLevel(rs.getInt("c_hlevel"));
	            child.setKey(rs.getString("c_fullname")); 
	            child.setSynonymCd(rs.getString("c_synonym_cd"));
	            child.setVisualattributes(rs.getString("c_visualattributes"));
	            Integer totalNum = rs.getInt("c_totalnum");
	            if (obfuscatedUserFlag == false ) { 
	            	child.setTotalnum(totalNum);
	            }
	            child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
	            child.setTablename(rs.getString("c_tablename")); 
	            child.setColumnname(rs.getString("c_columnname")); 
	            child.setColumndatatype(rs.getString("c_columndatatype")); 
	            child.setOperator(rs.getString("c_operator")); 
	            child.setDimcode(rs.getString("c_dimcode")); 
	            child.setTooltip(rs.getString("c_tooltip"));
	            if(childrenType.isBlob() == true){
					try {
						if(rs.getClob("c_comment") == null)
							child.setComment(null);
						else
							child.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
					} catch (IOException e) {
            			log.error(e.getMessage());
            			child.setComment(null);
					} 

					if(rs.getClob("c_metadataxml") == null){
						child.setMetadataxml(null);
					}else {
						String c_xml = null;
						try {
							c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
						} catch (IOException e) {
							log.error(e.getMessage());
	            			child.setMetadataxml(null);
						}
						if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
						{
							SAXBuilder parser = new SAXBuilder();
							java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
							Element rootElement = null;
							try {
								org.jdom.Document metadataDoc = parser.build(xmlStringReader);
								org.jdom.output.DOMOutputter out = new DOMOutputter(); 
								Document doc = out.output(metadataDoc);
								rootElement = doc.getDocumentElement();
							} catch (JDOMException e) {
								log.error(e.getMessage());
		            			child.setMetadataxml(null);
	            			} catch (IOException e1) {
		            			log.error(e1.getMessage());
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
				if((childrenType.getType().equals("all"))){
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
		            child.setValuetypeCd(rs.getString("valuetype_cd"));
				}
	            return child;
	        }
	    };

		//log.info(sql + " " + path + " " + level);
		
		List queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, searchPath, (level + 1) );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
//		Fix the key so it equals "\\tableCd\fullname"
		if(queryResult != null) {
			Iterator it = queryResult.iterator();
			while (it.hasNext()){
				ConceptType child = (ConceptType) it.next();
				child.setKey("\\\\" + tableCd + child.getKey());
			}
		}
		
		return queryResult;
		// tested statement with aqua data studio   verified output from above against this. 
		// select  c_fullname, c_name, c_synonym_cd, c_visualattributes  from metadata.testrpdr 
		// where c_fullname like '\RPDR\Diagnoses\Circulatory system (390-459)\Arterial vascular disease (440-447)\(446) Polyarteritis nodosa and al%' 
		// and c_hlevel = 5  and c_visualattributes not like '_H%' and c_synonym_cd = 'N'
		
		// verified both with and without hiddens and synonyms.
		
		// clob test   level = 4
		//   <parent>\\testrpdr\RPDR\HealthHistory\PHY\Health Maintenance\Mammogram\Mammogram - Deferred</parent> 
	}
	
}
