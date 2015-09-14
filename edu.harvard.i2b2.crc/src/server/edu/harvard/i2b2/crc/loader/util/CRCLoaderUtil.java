/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.crc.loader.ejb.DataMartLoaderAsyncBeanLocal;
import edu.harvard.i2b2.crc.loader.ejb.LoaderStatusBeanLocal;
import edu.harvard.i2b2.crc.loader.ejb.MissingTermReportBeanLocal;
//import edu.harvard.i2b2.crc.loader.ejb.fr.FRLocalHome;

/**
 * This is the CRC application's main utility class This utility class provides
 * support for fetching resources like datasouce, to read application
 * properties, to get ejb home,etc. $Id: CRCLoaderUtil.java,v 1.7 2007/04/25
 * 15:05:11 rk903 Exp $
 * 
 * @author rkuttan
 */
public class CRCLoaderUtil {

	/** log **/
	protected final static Log log = LogFactory.getLog(CRCLoaderUtil.class);

	/** property file name which holds application directory name **/
	public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "crc_application_directory.properties";

	/** application directory property name **/
	public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.crc.applicationdir";

	/** application property filename* */
	public static final String APPLICATION_PROPERTIES_FILENAME = "edu.harvard.i2b2.crc.loader.properties";

	/** property name for datasource present in app property file* */
	private static final String DATASOURCE_JNDI_PROPERTIES = "queryprocessor.jndi.datasource_name";

	/** property name for metadata schema name* */
	private static final String PMCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.pm.url";

	/** property name for metadata schema name* */
	private static final String PMCELL_BYPASS_FLAG_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.pm.bypass";

	/** property name for metadata schema name* */
	private static final String PMCELL_BYPASS_ROLE_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.pm.bypass.role";

	/** property name for pm bypass project name **/
	private static final String PMCELL_BYPASS_PROJECT_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.pm.bypass.project";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_DATASOURCE_PROPERTIES = "edu.harvard.i2b2.crc.loader.ds.lookup.datasource";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_SCHEMANAME_PROPERTIES = "edu.harvard.i2b2.crc.loader.ds.lookup.schemaname";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_SERVERTYPE_PROPERTIES = "edu.harvard.i2b2.crc.loader.ds.lookup.servertype";

	/** property name for metadata schema name* */
	private static final String FRCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.fr.url";

	/** property name for metadata schema name* */
	private static final String PROCESS_FOLDER_PROPERTIES = "edu.harvard.i2b2.crc.loader.process.foldername";

	private static final String PROCESS_TRANSACTIONTIMEOUT_PROPERTIES = "edu.harvard.i2b2.crc.loader.process.transactiontimeout";

	private static final String FRCELL_WS_TEMPSPACE_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.fr.tempspace";

	private static final String FRCELL_WS_TIMEOUT_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.fr.timeout";

	private static final String FRCELL_WS_FILETHRESHOLD_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.fr.filethreshold";

	private static final String FRCELL_WS_ATTACHMENTNAME_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.fr.attachmentname";

	private static final String FRCELL_WS_OPERATIONNAME_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.fr.operation";

	/** class instance field* */
	private static CRCLoaderUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static Properties appProperties = null;

	private static Properties loadProperties = null;

	/** field to store app datasource* */
	private DataSource dataSource = null;

	/** single instance of spring bean factory* */
	private BeanFactory beanFactory = null;

	/**
	 * Private constructor to make the class singleton
	 */
	private CRCLoaderUtil() {
	}

	/**
	 * Return this class instance
	 * 
	 * @return QueryProcessorUtil
	 */
	public static CRCLoaderUtil getInstance() {
		if (thisInstance == null) {
			thisInstance = new CRCLoaderUtil();
			serviceLocator = ServiceLocator.getInstance();
		}
		return thisInstance;
	}

	/**
	 * Function to get ejb local home for query manager
	 * 
	 * @return QueryManagerLocalHome
	 * @throws I2B2Exception
	 * @throws ServiceLocatorException
	 */
	/* //mm removed EJB
	public FRLocalHome getFRBeanLocalHome() throws I2B2Exception,
			ServiceLocatorException {
		return (FRLocalHome) serviceLocator
				.getLocalHome("ejb.crc.loader.FRBeanLocal");
	}
	*/

	/**
	 * Function to create spring bean factory
	 * 
	 * @return BeanFactory
	 */
	public BeanFactory getSpringBeanFactory() {
		if (beanFactory == null) {
			String appDir = null;
			try {
				// read application directory property file via classpath
				loadProperties = ServiceLocator
						.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
				// read directory property
				appDir = loadProperties.getProperty(APPLICATIONDIR_PROPERTIES);

			} catch (I2B2Exception e) {
				log.error(APPLICATION_DIRECTORY_PROPERTIES_FILENAME
						+ "could not be located from classpath ");
			}

			if (appDir != null) {
				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						"file:" + appDir + "/"
								+ "CRCLoaderApplicationContext.xml");
				beanFactory = ctx.getBeanFactory();
			} else {
				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						"classpath:" + "CRCLoaderApplicationContext.xml");
				beanFactory = ctx.getBeanFactory();
			}

		}
		return beanFactory;
	}

	public DataMartLoaderAsyncBeanLocal getDataMartLoaderBean()
			throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (DataMartLoaderAsyncBeanLocal) ctx
					.lookup("DataMartLoaderAsyncBean/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error ", e);
		}

	}

	/**
	 * Get Project managment cell's service url
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public String getProjectManagementCellUrl() throws I2B2Exception {
		return getPropertyValue(PMCELL_WS_URL_PROPERTIES);
	}

	/**
	 * Get Project managment cell's service url
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public String getFileManagentCellUrl() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_URL_PROPERTIES);
	}

	/**
	 * Get Project management bypass flag
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public boolean getProjectManagementByPassFlag() throws I2B2Exception {
		String pmByPassFlag = getPropertyValue(PMCELL_BYPASS_FLAG_PROPERTIES);
		if (pmByPassFlag == null) {
			return false;
		} else if (pmByPassFlag.trim().equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get Project management bypass flag
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public String getProjectManagementByPassRole() throws I2B2Exception {
		return getPropertyValue(PMCELL_BYPASS_ROLE_PROPERTIES);
	}

	/**
	 * Get Project management bypass project
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public String getProjectManagementByPassProject() throws I2B2Exception {
		return getPropertyValue(PMCELL_BYPASS_PROJECT_PROPERTIES);
	}

	public String getCRCDBLookupDataSource() throws I2B2Exception {
		return getPropertyValue(DS_LOOKUP_DATASOURCE_PROPERTIES);
	}

	public String getCRCDBLookupServerType() throws I2B2Exception {
		return getPropertyValue(DS_LOOKUP_SERVERTYPE_PROPERTIES);
	}

	public String getCRCDBLookupSchemaName() throws I2B2Exception {
		return getPropertyValue(DS_LOOKUP_SCHEMANAME_PROPERTIES);
	}

	public String getProcessFolderName() throws I2B2Exception {
		return getPropertyValue(PROCESS_FOLDER_PROPERTIES);
	}

	public String getProcessTransactionTimeout() throws I2B2Exception {
		return getPropertyValue(PROCESS_TRANSACTIONTIMEOUT_PROPERTIES);
	}

	public String getFileRepositoryTempSpace() throws I2B2Exception {
		return getPropertyValue(FRCELL_WS_TEMPSPACE_PROPERTIES);
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

	public LoaderStatusBeanLocal getLoaderStatusBean() throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (LoaderStatusBeanLocal) ctx.lookup("LoaderStatusBean/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error ", e);
		}

	}
	
	public MissingTermReportBeanLocal getMissingTermReportBean() throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (MissingTermReportBeanLocal) ctx.lookup("MissingTermReportBean/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error ", e);
		}

	}

	/**
	 * Return app server datasource
	 * 
	 * @return datasource
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	public DataSource getSpringDataSource(String dataSourceName)
			throws I2B2Exception {
		DataSource dataSource = (DataSource) getSpringBeanFactory().getBean(
				dataSourceName);

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
			// read application directory property file
			loadProperties = ServiceLocator
					.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			// read application directory property
			String appDir = loadProperties
					.getProperty(APPLICATIONDIR_PROPERTIES);
			if (appDir == null) {
				throw new I2B2Exception("Could not find "
						+ APPLICATIONDIR_PROPERTIES + "from "
						+ APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			}
			String appPropertyFile = appDir + "/"
					+ APPLICATION_PROPERTIES_FILENAME;
			try {
				FileSystemResource fileSystemResource = new FileSystemResource(
						appPropertyFile);
				PropertiesFactoryBean pfb = new PropertiesFactoryBean();
				pfb.setLocation(fileSystemResource);
				pfb.afterPropertiesSet();
				appProperties = (Properties) pfb.getObject();
			} catch (IOException e) {
				throw new I2B2Exception("Application property file("
						+ appPropertyFile
						+ ") missing entries or not loaded properly");
			}
			if (appProperties == null) {
				throw new I2B2Exception("Application property file("
						+ appPropertyFile
						+ ") missing entries or not loaded properly");
			}
		}

		String propertyValue = appProperties.getProperty(propertyName);

		if ((propertyValue != null) && (propertyValue.trim().length() > 0)) {
			;
		} else {
			throw new I2B2Exception("Application property file("
					+ APPLICATION_PROPERTIES_FILENAME + ") missing "
					+ propertyName + " entry");
		}

		return propertyValue;
	}

}
