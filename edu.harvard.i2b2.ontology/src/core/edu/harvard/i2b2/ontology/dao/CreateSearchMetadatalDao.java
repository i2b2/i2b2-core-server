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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.ontology.dao.lucene.parser.LuceneIndexer;
import edu.harvard.i2b2.ontology.dao.lucene.parser.LuceneIndexer.SuggestionIndexInfo;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ParamType;
import edu.harvard.i2b2.ontology.datavo.pm.ParamsType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.TableAccessType;
import edu.harvard.i2b2.ontology.util.ConceptXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.ModifierXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.ObserverXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.PatientDataXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.StringUtil;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;

public class CreateSearchMetadatalDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(CreateSearchMetadatalDao.class);

	private DataSource dataSource = null;

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

	public void buildCreateSearchMetadata(ProjectType projectInfo,
			DBInfoType dbInfo, SecurityType securityType)
					throws I2B2Exception {
		//Connection conn = null;
		//ResultSet resultSet = null;
		//PreparedStatement query = null;
		boolean isAlreadyRunning = false;

		TableAccessDao tableAccessDao = new TableAccessDao();
		ParamType param  = null;
		if (this.dataSource == null) {
			setDataSource(dbInfo.getDb_dataSource());
		} else {
			tableAccessDao.setDataSourceObject(this.dataSource);
		}
		try {
			param = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX",  securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			if (param == null)
			{
				PMServiceDriver.setProjectParam("A",
						"AUTOSUGGEST_INDEX", "RUNNING", securityType, projectInfo.getId(),
						OntologyUtil.getInstance()
						.getPmEndpointReference());
			}
			else				 
			{
				if (param.getValue().equals("RUNNING"))
				{
					isAlreadyRunning = true;
					throw new Exception("Auto Correct is already running");
				}
				else 
				{
					PMServiceDriver.setProjectParam(param.getId() ,"A",
							"AUTOSUGGEST_INDEX", "RUNNING", securityType, projectInfo.getId(),
							OntologyUtil.getInstance()
							.getPmEndpointReference());
				}
			}
			param = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX",  securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());



			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_STARTED_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			LuceneIndexer lucene = new LuceneIndexer();
			String suggestIndexDirName =  System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "autosuggest_index" + File.separatorChar + projectInfo.getId(); //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));
			//String suggestIndexDirName = ".." + File.separatorChar + "standalone" + File.separatorChar + "lucene_index" + File.separatorChar + projectInfo.getId(); //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));


			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_DIRECTORY_INDEX", suggestIndexDirName, securityType, projectInfo.getId(),
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

				//	throw new I2B2Exception("Error while creating lucene, folder already exists " + suggestIndexDirName);
				PMServiceDriver.setProjectParam("S",
						"AUTOSUGGEST_OLD_DIRECTORY_INDEX", suggestIndexDirName + formattedString, securityType, projectInfo.getId(),
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
			log.info("Creating auto-suggest index in directory " + suggestIndexDir);

			Map<String, File> suggestIndexDumpFilesMap = new LinkedHashMap<>();
			for (Map.Entry<String, String> n : suggestIndexDumpNames.entrySet()) {
				log.info(String.format("Creating CSV dump file for prefix '%s': %s", n.getKey(), n.getValue()));
				suggestIndexDumpFilesMap.put(n.getKey(), new File(n.getValue()));
			}

			suggestIndexDir.mkdir();
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


			//	List<String> tableNameList = tableAccessDao.getEditorTableName(
			//			projectInfo, dbInfo, true);

			List<TableAccessType> tableAccessType = tableAccessDao.getAllTableAccess(projectInfo, dbInfo);

			for (int i = 0; i < tableAccessType.size(); i++) {

				TableAccessType tableName = tableAccessType.get(i);
				PMServiceDriver.setProjectParam("S",
						"AUTOSUGGEST_WORKING_ON", i+1 + " of " + tableAccessType.size() + " : " + tableName.getTableName() + " - " + tableName.getName() , securityType, projectInfo.getId(),
						OntologyUtil.getInstance()
						.getPmEndpointReference());

				lucene.indexFromDB(suggestIndexInfo,
						tableName,  dataSource,  dbInfo);
			}
			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_BUILD_SUGGESION_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());		

			suggestIndexInfo.suggestionIndexer.buildSuggestionIndex();

			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_FINISHED_INDEX", new Date(System.currentTimeMillis()).toString(), securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			// Close the directory
			log.debug("Finished creating the ontology auto-suggest indices");
			suggestIndexDirectory.close();
			PMServiceDriver.setProjectParam(param.getId() ,"A",
					"AUTOSUGGEST_INDEX", "FINISHED", securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());

		} catch (SQLException sqlEx) {
			PMServiceDriver.setProjectParam(param.getId() ,"A",
					"AUTOSUGGEST_INDEX", "ERROR", securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_ERROR", sqlEx.getMessage(), securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());

			throw new I2B2Exception("Error while writing concept xml", sqlEx);
		} catch (Exception e) {
			if (isAlreadyRunning == false) {
			PMServiceDriver.setProjectParam(param.getId() ,"A",
					"AUTOSUGGEST_INDEX", "ERROR", securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			PMServiceDriver.setProjectParam("S",
					"AUTOSUGGEST_ERROR", e.getMessage(), securityType, projectInfo.getId(),
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			}
			throw new I2B2Exception("Error: ", e);
		} finally {
			//	closeAll(resultSet, query, conn);
		}

	}



}
