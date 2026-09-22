/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.ontology.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.lucene.parser.LuceneIndexer;
import edu.harvard.i2b2.ontology.dao.lucene.parser.LuceneIndexer.SearchIndexInfo;
import edu.harvard.i2b2.ontology.dao.lucene.parser.LuceneIndexer.SuggestionIndexInfo;
import edu.harvard.i2b2.ontology.dao.lucene.parser.SearchIndexer;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ParamType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.DblookupType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.TableAccessType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;

import oracle.jdbc.OracleTypes;


public class CreateSearchMetadatalDao extends JdbcDaoSupport  { // extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(CreateSearchMetadatalDao.class);

	public static final String ORACLE = "ORACLE";
	public static final String SQLSERVER = "SQLSERVER";
	public static final String POSTGRESQL = "POSTGRESQL";

	CallableStatement callStmt = null;

	private DataSource dataSource = null;
	private static JdbcTemplate jt;
	PreparedStatement stmt = null;
	ResultSet resultSet = null;

	Connection conn = null;

	public void setDataSourceObject(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private void setDataSource(String dataSourceName) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSourceName);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
			;
		}
		dataSource = ds;
	}

	public void buildCreateSearchMetadata(ProjectType projetcType, String projectInfo,
			DBInfoType dbInfo, SecurityType securityType)
					throws I2B2Exception {
		//Connection conn = null;
		//ResultSet resultSet = null;
		//PreparedStatement query = null;
		boolean isAlreadyRunning = false;

		TableAccessDao tableAccessDao = new TableAccessDao();
		ParamType paramIndexStatus  = null;
		ParamType param  = null;
		if (this.dataSource == null) {
			setDataSource(dbInfo.getDb_dataSource());
		} else {
			tableAccessDao.setDataSourceObject(this.dataSource);
		}
		try {
			paramIndexStatus = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX_STATUS",  securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			if (paramIndexStatus == null)
			{
				PMServiceDriver.setProjectParam("A",
						"AUTOSUGGEST_INDEX_STATUS", "RUNNING", securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());
			}
			else				 
			{
				if (paramIndexStatus.getValue().equals("RUNNING") && OntologyUtil.getInstance().isAutoSuggectStarted() )
				{
					isAlreadyRunning = true;
					throw new Exception("Auto Correct is already running");
				}
				else 
				{
					OntologyUtil.getInstance().setAutoSuggectStarted(true);
					PMServiceDriver.setProjectParam(paramIndexStatus.getId() ,"A",
							"AUTOSUGGEST_INDEX_STATUS", "RUNNING", securityType, projectInfo,
							OntologyUtil.getInstance()
							.getPmEndpointReference());

					//Clear out finished time
					PMServiceDriver.setProjectParam(true, "D",
							"AUTOSUGGEST_FINISHED_INDEX", "", securityType, projectInfo,
							OntologyUtil.getInstance()
							.getPmEndpointReference());

				}
			}
			paramIndexStatus = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX_STATUS",  securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());



			PMServiceDriver.setProjectParam(true, "S",
					"AUTOSUGGEST_STARTED_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			LuceneIndexer lucene = new LuceneIndexer();
			//MM
			//String suggestIndexDirName =  System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "autosuggest_index" + File.separatorChar + projectInfo; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));
			//String searchIndexDirName =  System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "search_index" + File.separatorChar + projectInfo; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));
			String suggestIndexDirName =  System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "autosuggest_index" + File.separatorChar + projectInfo ; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));


			if (OntologyUtil.getInstance().getAutosuggestIndexDirectory() != null && OntologyUtil.getInstance().getAutosuggestIndexDirectory() != "")
				suggestIndexDirName =  OntologyUtil.getInstance().getAutosuggestIndexDirectory();

			String searchIndexDirName2 =  System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "search_index" + File.separatorChar + projectInfo; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));


			//String suggestIndexDirName = ".." + File.separatorChar + "standalone" + File.separatorChar + "lucene_index" + File.separatorChar + projectInfo; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));


			PMServiceDriver.setProjectParam(true, "S",
					"AUTOSUGGEST_DIRECTORY_INDEX", suggestIndexDirName, securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());



			Path folderPath = Paths.get(suggestIndexDirName);

			// Check if the path exists AND if it is a directory
			if (Files.exists(folderPath) && Files.isDirectory(folderPath)) 
			{
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("_MMddyyyy_HHmmss");
				String formattedString = now.format(formatter); // e.g., "11-03-2026 15:15:00"
				Path target = Paths.get(suggestIndexDirName + formattedString);


				Files.move(folderPath, target, StandardCopyOption.REPLACE_EXISTING);


				PMServiceDriver.setProjectParam(true, "S",
						"AUTOSUGGEST_PREVIOUS_DIRECTORY_INDEX", suggestIndexDirName + formattedString, securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());


			}
			List<String> suggestIndexDumpPrefixes = Collections.emptyList();

			Map<String, String> suggestIndexDumpNames = new LinkedHashMap<>();
			for (String p : suggestIndexDumpPrefixes) {
				if (p != null && !p.trim().isEmpty()) {
					suggestIndexDumpNames.put(p, String.format("%s.%s.csv", suggestIndexDirName, p.trim()));
				}
			}

			File suggestIndexDir = new File(suggestIndexDirName);
			//MM todo	File searchIndexDir = new File(searchIndexDirName);
			log.info("Creating auto-suggest index in directory " + suggestIndexDir);

			Map<String, File> suggestIndexDumpFilesMap = new LinkedHashMap<>();
			for (Map.Entry<String, String> n : suggestIndexDumpNames.entrySet()) {
				log.info(String.format("Creating CSV dump file for prefix '%s': %s", n.getKey(), n.getValue()));
				suggestIndexDumpFilesMap.put(n.getKey(), new File(n.getValue()));
			}

			suggestIndexDir.mkdir();
			//searchIndexDir.mkdir();
			FSDirectory suggestIndexDirectory = FSDirectory.open(suggestIndexDir.toPath());

			SuggestionIndexer suggestionIndexer = new SuggestionIndexer(
					3,
					suggestIndexDirectory,
					suggestIndexDirName,
					null,
					false,
					suggestIndexDumpFilesMap
					);



			SuggestionIndexInfo suggestIndexInfo = new SuggestionIndexInfo(suggestionIndexer, suggestIndexDirName, null);


			//MM todo FSDirectory searchIndexDirectory = FSDirectory.open(searchIndexDir.toPath());
			//IndexWriter searchIndexWriter = createSearchIndexWriter(searchIndexDirectory); 
			/*
			SearchIndexer searchIndexer = new SearchIndexer(
					lucene.createSearchIndexWriter(searchIndexDirectory), //lucene.createSearchIndexWriter(searchIndexDirectory), // searchIndexWriter,
					"", //, //codeCategoryFilename,
					"", //filename,
					',', //fileDelimiter,
					',', //categoryFileDelimiter,
					true //includeTooltips
					);

			SearchIndexInfo searchIndexInfo = new SearchIndexInfo(searchIndexer, searchIndexDirName, null ); //searchIndexZipFileName);
			 */

			//	List<String> tableNameList = tableAccessDao.getEditorTableName(
			//			projectInfo, dbInfo, true);

			List<TableAccessType> tableAccessType = tableAccessDao.getAllTableAccess(projetcType, dbInfo);

			for (int i = 0; i < tableAccessType.size(); i++) {

				TableAccessType tableName = tableAccessType.get(i);

				PMServiceDriver.setProjectParam(true, "S",
						"AUTOSUGGEST_WORKING_ON", i+1 + " of " + tableAccessType.size() + " : " + tableName.getTableName() + " - " + tableName.getName() , securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());

				lucene.indexFromDB(suggestIndexInfo, null,//searchIndexInfo,
						tableName,  dataSource,  dbInfo);
			}
			/*
			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_BUILD_SUGGESION_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());		
			 */
			suggestIndexInfo.suggestionIndexer.buildSuggestionIndex();

			PMServiceDriver.setProjectParam(true, "S",
					"AUTOSUGGEST_FINISHED_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			// Close the directory
			log.debug("Finished creating the ontology auto-suggest indices");
			suggestIndexDirectory.close();
			PMServiceDriver.setProjectParam(paramIndexStatus.getId() ,"A",
					"AUTOSUGGEST_INDEX_STATUS", "FINISHED", securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

		} catch (SQLException sqlEx) {
			PMServiceDriver.setProjectParam(paramIndexStatus.getId() ,"A",
					"AUTOSUGGEST_INDEX_STATUS", "ERROR", securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			PMServiceDriver.setProjectParam(true, "S",
					"AUTOSUGGEST_ERROR", sqlEx.getMessage(), securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			throw new I2B2Exception("Error while writing concept xml", sqlEx);
		} catch (Exception e) {
			if (isAlreadyRunning == false) {
				PMServiceDriver.setProjectParam(paramIndexStatus.getId() ,"A",
						"AUTOSUGGEST_INDEX_STATUS", "ERROR", securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());
				PMServiceDriver.setProjectParam(true, "S",
						"AUTOSUGGEST_ERROR", e.getMessage(), securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());
			}
			throw new I2B2Exception("Error: ", e);
		} finally {
			//	closeAll(resultSet, query, conn);
		}

	}



	public void buildUpdateTotalNum(String projectInfo,
			DBInfoType dbInfo, SecurityType securityType, String operationType, String cdm)
					throws I2B2Exception {
		boolean isAlreadyRunning = false;

		TableAccessDao tableAccessDao = new TableAccessDao();
		ParamType param  = null;
		if (this.dataSource == null) {
			setDataSource(dbInfo.getDb_dataSource());
		} else {
			tableAccessDao.setDataSourceObject(this.dataSource);
		}

		jt = new JdbcTemplate(dataSource);

		String spName = "RunTotalNum";
		try {
			spName =  OntologyUtil.getInstance().getAutosuggestIndexStoredProcedure();

		} catch (Exception e)
		{
			
		}

		try {
			//if (OntologyUtil.getInstance().getAutosuggestIndexStoredProcedure() != null && OntologyUtil.getInstance().getAutosuggestIndexStoredProcedure() != "")

			param = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX_STATUS",  securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			if (param == null)
			{
				PMServiceDriver.setProjectParam("A",
						"AUTOSUGGEST_INDEX_STATUS", "RUNNING", securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());
			}
			else				 
			{
				if (param.getValue().equals("RUNNING") && OntologyUtil.getInstance().isAutoSuggectStarted() )
				{
					isAlreadyRunning = true;
					throw new Exception("Total Num is already running");
				}
				else 
				{
					OntologyUtil.getInstance().setAutoSuggectStarted(true);
					PMServiceDriver.setProjectParam(param.getId() ,"A",
							"AUTOSUGGEST_INDEX_STATUS", "RUNNING", securityType, projectInfo,
							OntologyUtil.getInstance()
							.getPmEndpointReference());
				}
			}
			param = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX_STATUS",  securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());


			// Run the stored procedure
			String value = "";
			String serverType = dbInfo.getDb_serverType();
			String dataSchema = dbInfo.getDb_fullSchema();
			DblookupDao dsLookupDao = new DblookupDao();
			List<DblookupType> dsLookup = dsLookupDao.getDblookup("project_path",projectInfo, securityType,  "crc_db_lookup");

			log.error("A");
			conn = dataSource.getConnection();
			if (dataSchema.equals(""))
				try {

					//Connection conn = dataSource.getConnection();

					dataSchema = conn.getSchema();
					//conn.close();
				} catch (SQLException e1) {
					log.error(e1.getMessage());
				} 
			log.error("B");

			log.error( spName + " for " + projectInfo  + " in database " + serverType);
			log.error(securityType);
			log.error(projectInfo);
			log.error(OntologyUtil.getInstance()
					.getPmEndpointReference());

			//if (operationType.equals("synchronize_all")) {
			PMServiceDriver.setProjectParam(true,"S",
					"TOTALNUM_WORKING_ON", spName + " for " + projectInfo  + " in database " + serverType, securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			if (serverType.equalsIgnoreCase(SQLSERVER))
			{

				log.error("D");

				value = "exec " + dataSchema + "." + spName + " 'observation_fact','" + dataSchema +"','@','N','" + cdm + "'";				
				stmt = conn.prepareStatement(value);
				log.error("E");

				resultSet = stmt.executeQuery();
				//}
				log.error("F");


			} else if (serverType.equalsIgnoreCase(ORACLE))
			{


				value =  "{ call  " + //dataSchema +
						  spName + "  ('observation_fact','" + dataSchema.replaceAll(".", "") +"','@','" + cdm + "')"
						+ "  }";
				callStmt = dataSource.getConnection().prepareCall(value);
				callStmt.execute();


			} else if (serverType.equalsIgnoreCase(POSTGRESQL))
			{


				value = "SELECT  " + spName + " ('observation_fact','" + dataSchema +"','@','N','" + cdm + "')";

				// Step 1: start transaction
				callStmt = conn.prepareCall(value);
				callStmt.execute(); // Step 2: call procedure
				//}


			}



			PMServiceDriver.setProjectParam(true, "S",
					"AUTOSUGGEST_FINISHED_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			PMServiceDriver.setProjectParam(param.getId() ,"A",
					"AUTOSUGGEST_INDEX_STATUS", "FINISHED", securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

		} catch (SQLException sqlEx) {
			PMServiceDriver.setProjectParam(param.getId() ,"A",
					"AUTOSUGGEST_INDEX_STATUS", "ERROR", securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_ERROR", sqlEx.getMessage(), securityType, projectInfo,
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			throw new I2B2Exception("Error while writing concept xml", sqlEx);
		} catch (Exception e) {
			if (isAlreadyRunning == false) {
				PMServiceDriver.setProjectParam(param.getId() ,"A",
						"AUTOSUGGEST_INDEX_STATUS", "ERROR", securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());
				PMServiceDriver.setProjectParam("S",
						"AUTOSUGGEST_ERROR", e.getMessage(), securityType, projectInfo,
						OntologyUtil.getInstance()
						.getPmEndpointReference());
			}
			throw new I2B2Exception("Error: ", e);
		} finally {
			//	closeAll(resultSet, query, conn);
		}

	}

}
