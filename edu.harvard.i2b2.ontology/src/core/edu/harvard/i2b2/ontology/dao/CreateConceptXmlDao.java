package edu.harvard.i2b2.ontology.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.util.ConceptXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.ModifierXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.ObserverXMLWriterUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.PatientDataXMLWriterUtil;

public class CreateConceptXmlDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(CreateConceptXmlDao.class);

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

	public void buildConceptUpdateXml(ProjectType projectInfo,
			DBInfoType dbInfo, String pdoFileName, boolean synchronizeAllFlag,
			boolean hiddenConceptFlag)
			throws I2B2Exception {
		File tempFile = createTempFile(pdoFileName);

		log.info("Temp file name " + tempFile.getAbsolutePath() + "]");

		try {
			FileWriter fileWriter = new FileWriter(tempFile);
			XMLEventWriter xmlWriter = PatientDataXMLWriterUtil
					.createXMLEventWriter(fileWriter);
			ConceptXMLWriterUtil conceptWriter = new ConceptXMLWriterUtil(
					xmlWriter);

			conceptWriter.startDocument();
			buildDimensionUpdateXml(projectInfo, dbInfo, pdoFileName,
					synchronizeAllFlag, hiddenConceptFlag,"concept_dimension", conceptWriter);
			ObserverXMLWriterUtil observerWriter = new ObserverXMLWriterUtil(
					xmlWriter);

			buildDimensionUpdateXml(projectInfo, dbInfo, pdoFileName,
					synchronizeAllFlag, hiddenConceptFlag,"provider_dimension", observerWriter);
			ModifierXMLWriterUtil modifierWriter = new ModifierXMLWriterUtil(
					xmlWriter);

			buildDimensionUpdateXml(projectInfo, dbInfo, pdoFileName,
					synchronizeAllFlag, hiddenConceptFlag,"modifier_dimension", modifierWriter);
			modifierWriter.endDocument();
			
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("Error while writing concept xml", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new I2B2Exception("Error while writing concept xml", e);
		}

	}

	private void buildDimensionUpdateXml(ProjectType projectInfo,
			DBInfoType dbInfo, String pdoFileName, boolean synchronizeAllFlag,
			boolean hiddenConceptFlag,String dimensionTableName, PatientDataXMLWriterUtil xmlWriterUtil)
			throws I2B2Exception {
		String metadataSchema = dbInfo.getDb_fullSchema();
		TableAccessDao tableAccessDao = new TableAccessDao();
		if (this.dataSource == null) {
			setDataSource(dbInfo.getDb_dataSource());
		} else {
			tableAccessDao.setDataSourceObject(this.dataSource);
		}
		String emptyStringClause = " ";
		if (dbInfo.getDb_serverType().equals("ORACLE")) {
			emptyStringClause = "trim(c_basecode) is not null ";
		} else {
			emptyStringClause = "rtrim(ltrim(c_basecode)) <> ''";
		}
		String hiddenConceptSql = " ";
		if (hiddenConceptFlag) { 
			hiddenConceptSql = " and c_visualattributes not like '_H%' ";
		}
		
		String updateOnlyClause = " ";
		if (synchronizeAllFlag == false) {
			updateOnlyClause = " and c_visualattributes like '%E' "+ hiddenConceptSql + " and c_synonym_cd = 'N' and m_exclusion_cd is null";
		} else {
			updateOnlyClause = "  and c_synonym_cd = 'N' " + hiddenConceptSql + " and m_exclusion_cd is null";
		}
		// call table access
		List<String> tableNameList = tableAccessDao.getEditorTableName(
				projectInfo, dbInfo, synchronizeAllFlag);
		// iterate table and
		String selectSql = "";

		Connection conn = null;
		ResultSet resultSet = null;
		PreparedStatement query = null;
		// ConceptXMLWriterUtil xmlWriterUtil = null;

		try {
			conn = dataSource.getConnection();
			// xmlWriterUtil = new ConceptXMLWriterUtil(
			// new FileWriter(pdoFileName));
			xmlWriterUtil.startSet();
			for (String singleTableName : tableNameList) {
				selectSql = " select * from " + metadataSchema
						+ singleTableName
						+ " where c_basecode is not null and "
						+ emptyStringClause + updateOnlyClause
						+ "   and lower(c_tablename) = '" + dimensionTableName.toLowerCase() + "'";
				log.debug("Executing sql [" + selectSql + "]");
				query = conn.prepareStatement(selectSql);

				resultSet = query.executeQuery();

				while (resultSet.next()) {
					xmlWriterUtil.buildConcept(resultSet);
				}

			}
			xmlWriterUtil.endSet();

		} catch (SQLException sqlEx) {
			throw new I2B2Exception("Error while writing concept xml", sqlEx);
		} catch (Exception e) {
			throw new I2B2Exception("Error while writing concept xml ", e);
		} finally {
			closeAll(resultSet, query, conn);
		}

	}

	private void closeAll(ResultSet resultSet, PreparedStatement query,
			Connection conn) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (query != null) {
				query.close();
			}

			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			;
		}
	}

	private File createTempFile(String fileName) throws I2B2Exception {
		File tempFile = null;
		try {

			// Create temp file.
			tempFile = new File(fileName);

		} catch (Exception e) {
			throw new I2B2Exception("Unable to create temp file for ", e);
		}
		return tempFile;
	}

}
