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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;

public class GetCategoriesDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(GetCategoriesDao.class);
    final static String CORE = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip ";
	final static String DEFAULT = " c_fullname, c_name ";
	
	public List findRootCategories(final GetReturnType returnType, final ProjectType projectInfo) throws DataAccessException, I2B2DAOException{
		
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource("java:OntologyLocalDS");
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 

		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(ds);
		
		// find return parameters
		String parameters = DEFAULT;		
		if (returnType.getType().equals("core")){
			parameters = CORE;
		}
/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
*/
			
		// First step is get metadata schema name from properties file.
		String metadataSchema = "";
		try {
			metadataSchema = OntologyUtil.getInstance().getMetaDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}
//		 First step is to call PM to see what roles/project user belongs to.
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}
		
		String roles = "( '";
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role =   (String) it.next();
			 roles = roles + role;
			 if (it.hasNext()){
				 roles = roles + "', '";
			 }
			 else {
				 roles = roles + "' )";
			 }
		}
		log.debug(roles);
		log.debug(projectInfo.getId().toLowerCase());
		
		String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "table_access where c_project = ? and c_role in " + roles;
		final boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            ConceptType child = new ConceptType();
	            //TODO fix this for all/+blob

		        child.setKey("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_fullname")); 
	            child.setName(rs.getString("c_name"));
	            if(returnType.getType().equals("core")) {
	            	child.setBasecode(rs.getString("c_basecode"));
	            	child.setLevel(rs.getInt("c_hlevel"));
	            	child.setSynonymCd(rs.getString("c_synonym_cd"));
	            	child.setVisualattributes(rs.getString("c_visualattributes"));
	            	Integer totalNum = rs.getInt("c_totalnum");
	            	if (obfuscatedUserFlag == false) { 
	            		child.setTotalnum(totalNum);
	            	}
	            	child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
	            	child.setTablename(rs.getString("c_tablename")); 
	            	child.setColumnname(rs.getString("c_columnname")); 
	            	child.setColumndatatype(rs.getString("c_columndatatype")); 
	            	child.setOperator(rs.getString("c_operator")); 
	            	child.setDimcode(rs.getString("c_dimcode")); 
	            	child.setTooltip(rs.getString("c_tooltip"));
	            }
	            return child;
	        }
	    };
	    
		List queryResult = null;
		try {
			queryResult = jt.query(tablesSql, mapper, projectInfo.getId().toLowerCase());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		if (returnType.isBlob() == true && queryResult != null){
			Iterator itr = queryResult.iterator();
			while (itr.hasNext()){
				ConceptType child = (ConceptType) itr.next();
				String clobSql = "select c_metadataxml, c_comment from "+  metadataSchema +  "table_access where c_name = ? and c_tooltip = ?";
				ParameterizedRowMapper<ConceptType> map = new ParameterizedRowMapper<ConceptType>() {
			        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
			        	ConceptType concept = new ConceptType();
//			        	ResultSetMetaData rsmd = rs.getMetaData();
//			        	rsmd.get
			        	if(rs.getClob("c_metadataxml") == null){
			        		concept.setMetadataxml(null);
			        	}else {
			        		String c_xml = null;
			        		try {
			        			c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
			        		} catch (IOException e1) {
			        			// TODO Auto-generated catch block
			        			log.error(e1.getMessage());
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
			        				concept.setMetadataxml(null);
			        			} catch (IOException e) {
			        				log.error(e.getMessage());
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

			        	if(rs.getClob("c_comment") == null){
			        		concept.setComment(null);
			        	}else {
			        		try {
								concept.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
							} catch (IOException e) {
								log.error(e.getMessage());
								concept.setComment(null);
							}
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
	


}
