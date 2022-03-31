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
 *                 Raj Kuttan
 *                 Lori Phillips
 */
package edu.harvard.i2b2.ontology.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.pm.ParamType;

/**
 * This is the Ontology service's main utility class This utility class provides
 * support for fetching resources like datasouce, to read application
 * properties, to get ejb home,etc. $Id: OntologyUtil.java,v 1.15 2009/01/08
 * 19:27:01 lcp5 Exp $
 * 
 * @author rkuttan
 */
public class OntologyUtil {


	private static List<ParamType> appProperties = null;


	/** property name for PM endpoint reference **/
	private static final String PM_WS_EPR = "ontology.ws.pm.url";

	/** property name for ONT_TERM_DELIMITER **/
	private static final String ONT_TERM_DELIMITER = "ontology.terminal.delimiter";

	private static final String FRCELL_WS_TEMPSPACE_PROPERTIES = "edu.harvard.i2b2.ontology.ws.fr.tempspace";

	private static final String FRCELL_WS_TIMEOUT_PROPERTIES = "edu.harvard.i2b2.ontology.ws.fr.timeout";

	private static final String FRCELL_WS_FILETHRESHOLD_PROPERTIES = "edu.harvard.i2b2.ontology.ws.fr.filethreshold";

	private static final String FRCELL_WS_ATTACHMENTNAME_PROPERTIES = "edu.harvard.i2b2.ontology.ws.fr.attachmentname";

	private static final String FRCELL_WS_OPERATIONNAME_PROPERTIES = "edu.harvard.i2b2.ontology.ws.fr.operation";

	/** property name for metadata schema name* */
	private static final String FRCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.ontology.ws.fr.url";
	private static final String STOPWORD_PROPERTIES = "edu.harvard.i2b2.ontology.stopword";

	
	private static final String CRCCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.ontology.ws.crc.url";

	private static final String SERVICE_ACCOUNT_USER = "edu.harvard.i2b2.ontology.pm.serviceaccount.user";
	private static final String SERVICE_ACCOUNT_PASSWORD =  "edu.harvard.i2b2.ontology.pm.serviceaccount.password";

	/** class instance field **/
	private static OntologyUtil thisInstance = null;

	/** service locator field **/
	private static ServiceLocator serviceLocator = null;

	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/** field to store app datasource **/
	private DataSource dataSource = null;

	/** single instance of spring bean factory **/
	private BeanFactory beanFactory = null;

	/**
	 * Private constructor to make the class singleton
	 */
	private OntologyUtil() {
	}

	/**
	 * Return this class instance
	 * 
	 * @return OntologyUtil
	 */
	public static OntologyUtil getInstance() {
		if (thisInstance == null) {
			thisInstance = new OntologyUtil();
		}

		serviceLocator = ServiceLocator.getInstance();

		return thisInstance;
	}

	/**
	 * Return the ontology spring config
	 * 
	 * @return
	 */

	/**
	 * Return metadata schema name
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public String getMetaDataSchemaName() throws I2B2Exception {
		try {
			Connection conn = dataSource.getConnection();
			
			String metadataSchema = conn.getSchema() + ".";
			conn.close();
			
			return metadataSchema ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //getPropertyValue(METADATA_SCHEMA_NAME_PROPERTIES).trim() + ".";
		return null;
	}

	/**
	 * Return PM cell endpoint reference URL
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public String getPmEndpointReference() throws I2B2Exception {
		return getPropertyValue(PM_WS_EPR).trim();
	}



	/**
	 * Return Ontology terminal delimiter
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public Boolean getOntTerminalDelimiter() throws I2B2Exception {
		return Boolean.valueOf(getPropertyValue(ONT_TERM_DELIMITER).trim());
	}

	public String getFileRepositoryTempSpace() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_TEMPSPACE_PROPERTIES);
	}
	
	public String getStopWord() throws I2B2Exception {
		return getPropertyValue(STOPWORD_PROPERTIES);
	}

	public String getFileRepositoryTimeout() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_TIMEOUT_PROPERTIES);
	}

	public String getFileRepositoryThreshold() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_FILETHRESHOLD_PROPERTIES);
	}

	public String getFileRepositoryAttachmentName() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_ATTACHMENTNAME_PROPERTIES);
	}

	public String getFileRepositoryOperationName() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_OPERATIONNAME_PROPERTIES);
	}

	public String getFileManagentCellUrl() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_URL_PROPERTIES);
	}

	public String getCRCUrl() throws I2B2Exception {
		return getPropertyValue(CRCCELL_WS_URL_PROPERTIES);
	}

	public String getServiceAccountUser() throws I2B2Exception {
		return getPropertyValue(SERVICE_ACCOUNT_USER);
	}

	public String getServiceAccountPassword() throws I2B2Exception {
		return getPropertyValue(SERVICE_ACCOUNT_PASSWORD);
	}


	/**
	 * Return app server datasource
	 * 
	 * @return datasource
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	public DataSource getDataSource(String dataSourceName) throws I2B2Exception {
		// DataSource dataSource = (DataSource) getSpringBeanFactory()
		// .getBean(DATASOURCE_BEAN_NAME);

		dataSource = (DataSource) serviceLocator
				.getAppServerDataSource(dataSourceName);
		return dataSource;

	}

	// ---------------------
	// private methods here
	// ---------------------



	/**
	 * Load application property file into memory
	 */
	private String getPropertyValue(String propertyName) throws I2B2Exception {

		if (appProperties == null) {



			//		log.info(sql + domainId + projectId + ownerId);
			//	List<ParamType> queryResult = null;
			try {
				DataSource   ds = this.getDataSource("java:/OntologyBootStrapDS");

				JdbcTemplate jt =  new JdbcTemplate(ds);
				Connection conn = ds.getConnection();
				
				String metadataSchema = conn.getSchema();
				conn.close();
				String sql =  "select * from " + metadataSchema + ".hive_cell_params where status_cd <> 'D' and cell_id = 'ONT'";

				log.debug("Start query");
				appProperties = jt.query(sql, new getHiveCellParam());
				log.debug("End query");


			} catch (DataAccessException e) {
				log.error(e.getMessage());
				e.printStackTrace();
				throw new I2B2DAOException("Database error");
			}
			//return queryResult;	
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		String propertyValue = null;//appProperties.getProperty(propertyName);
		for (int i=0; i < appProperties.size(); i++)
		{
			if (appProperties.get(i).getName() != null)
			{
				if (appProperties.get(i).getName().equalsIgnoreCase(propertyName))
					if (appProperties.get(i).getDatatype().equalsIgnoreCase("U"))
						try {
							 propertyValue = ServiceClient.getContextRoot() + appProperties.get(i).getValue();

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					else 
						propertyValue = appProperties.get(i).getValue();
			}
		}

		if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
			throw new I2B2Exception("Application property file("
					//	+ APPLICATION_PROPERTIES_FILENAME + ") missing "
					+ propertyName + " entry");
		}

		return propertyValue;
	}

	public MessageHeaderType getMessageHeader()  {
		MessageHeaderType messageHeader = new MessageHeaderType();
		ApplicationType appType = new ApplicationType();
		try {
			appType.setApplicationName(getPropertyValue("applicationName"));
			appType.setApplicationVersion(getPropertyValue("applicationVersion"));
		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messageHeader.setSendingApplication(appType);
		return messageHeader;
	}
}

class getHiveCellParam implements RowMapper<ParamType> {
	@Override
	public ParamType mapRow(ResultSet rs, int rowNum) throws SQLException {

			ParamType param = new ParamType();
			param.setId(rs.getInt("id"));
			param.setName(rs.getString("param_name_cd"));
			param.setValue(rs.getString("value"));
			param.setDatatype(rs.getString("datatype_cd"));
			return param;
		} 
}

