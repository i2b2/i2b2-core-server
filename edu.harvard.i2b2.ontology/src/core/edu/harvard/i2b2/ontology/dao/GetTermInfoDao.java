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
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.util.StringUtil;

public class GetTermInfoDao extends JdbcDaoSupport {
	private static Log log = LogFactory.getLog(GetChildrenDao.class);
	final static String DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip";
	final static String ALL = DEFAULT + ", update_date, download_date, import_date, sourcesystem_cd, valuetype_cd";
	final static String CORE = DEFAULT;
	final static String BLOB = ", c_metadataxml, c_comment ";

	public List findByFullname(final GetTermInfoType termInfoType, List categories,  ProjectType projectInfo, final String dbType) throws DataAccessException{
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource("java:OntologyLocalDS");
		} catch (I2B2Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} 
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(ds);

		// find return parameters
		String parameters = DEFAULT;		
		if (termInfoType.getType().equals("core")){
			parameters = CORE;
		}
		else if (termInfoType.getType().equals("all")){
			parameters = ALL;
		}
		if(termInfoType.isBlob() == true)
			parameters = parameters + BLOB;

		//extract table code
		String tableCd = StringUtil.getTableCd(termInfoType.getSelf());

		// table code to table name conversion
		// Get metadata schema name from properties file.
		String metadataSchema = "";
		try {
			metadataSchema = OntologyUtil.getInstance().getMetaDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}

		// table code to table name conversion
		String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? ";
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};

		String tableName = jt.queryForObject(tableSql, map, tableCd);

		String path = StringUtil.getPath(termInfoType.getSelf());

		//			if(path.equals("\\Providers"))
		//				path="\\RPDR\\Providers";

		String searchPath = path;

		String hidden = "";
		if(termInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";

		String synonym = "";
		if(termInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ? "; 
		sql = sql + hidden + synonym + " order by c_name ";

		//	log.info(sql + path + level);
		final  boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated( projectInfo );

		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
			public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConceptType self = new ConceptType();	            
				self.setName(rs.getString("c_name"));
				self.setBasecode(rs.getString("c_basecode"));
				self.setLevel(rs.getInt("c_hlevel"));
				self.setKey(rs.getString("c_fullname")); 
				self.setSynonymCd(rs.getString("c_synonym_cd"));
				self.setVisualattributes(rs.getString("c_visualattributes"));

				Integer totalNum = rs.getInt("c_totalnum");
				if ( obfuscatedUserFlag == false) { 
					self.setTotalnum(totalNum);
				}
				self.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
				self.setTablename(rs.getString("c_tablename")); 
				self.setColumnname(rs.getString("c_columnname")); 
				self.setColumndatatype(rs.getString("c_columndatatype")); 
				self.setOperator(rs.getString("c_operator")); 
				self.setDimcode(rs.getString("c_dimcode")); 
				self.setTooltip(rs.getString("c_tooltip"));
				if(termInfoType.isBlob() == true) {
					if(rs.getClob("c_comment") == null)
						self.setComment(null);
					else {
						try {
							if (dbType.equals("POSTGRESQL"))
								self.setComment(rs.getString("c_comment"));
							else
								self.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
						} catch (IOException e1) {
							log.error(e1.getMessage());
							self.setComment(null);
						}
					}
					if(rs.getClob("c_metadataxml") == null){
						self.setMetadataxml(null);
					}else {
						String c_xml = null;
						try {
							if (dbType.equals("POSTGRESQL"))
								c_xml = rs.getString("c_metadataxml");
							else
								c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
						} catch (IOException e1) {
							log.error(e1.getMessage());
							self.setMetadataxml(null);
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
								self.setMetadataxml(null);
							} catch (IOException e1) {
								log.error(e1.getMessage());
								self.setMetadataxml(null);
							}
							if (rootElement != null) {
								XmlValueType xml = new XmlValueType();									
								xml.getAny().add(rootElement);								
								self.setMetadataxml(xml);
							}

						}else {
							self.setMetadataxml(null);
						}
					}	
				}	
				if((termInfoType.getType().equals("all"))){
					DTOFactory factory = new DTOFactory();
					// make sure date isnt null before converting to XMLGregorianCalendar
					Date date = rs.getDate("update_date");
					if (date == null)
						self.setUpdateDate(null);
					else 
						self.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 

					date = rs.getDate("download_date");
					if (date == null)
						self.setDownloadDate(null);
					else 
						self.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 

					date = rs.getDate("import_date");
					if (date == null)
						self.setImportDate(null);
					else 
						self.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 

					self.setSourcesystemCd(rs.getString("sourcesystem_cd"));
					self.setValuetypeCd(rs.getString("valuetype_cd"));
				}
				return self;
			}
		};

		//	log.info(sql + " " + path + " " + level);

		List queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, searchPath );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());

		//Fix the key so it equals "\\tableCd\fullname"
		if(queryResult != null) {
			Iterator itr = queryResult.iterator();
			while (itr.hasNext()){
				ConceptType self = (ConceptType) itr.next();
				self.setKey("\\\\" + tableCd + self.getKey());
			}
		}
		return queryResult;
	}
}
