/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.util;
 
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.impl.SchedulerRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.crc.ejb.ProcessQueue;
import edu.harvard.i2b2.crc.ejb.QueryManagerBeanUtil;
import edu.harvard.i2b2.crc.ejb.analysis.AnalysisPluginInfoLocal;
import edu.harvard.i2b2.crc.ejb.analysis.CronEjbLocal;
import edu.harvard.i2b2.crc.ejb.analysis.StartAnalysisLocal;
import edu.harvard.i2b2.crc.ejb.role.PriviledgeBean;
import edu.harvard.i2b2.crc.ejb.role.PriviledgeLocal;
import edu.harvard.i2b2.crc.quartz.QuartzFactory;

/**
 * This is the CRC application's main utility class This utility class provides
 * support for fetching resources like datasouce, to read application
 * properties, to get ejb home,etc. $Id: QueryProcessorUtil.java,v 1.7
 * 2007/04/25 15:05:11 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryProcessorUtil {

	/** log **/
	protected final static Log log = LogFactory
			.getLog(QueryProcessorUtil.class);

	/** property file name which holds application directory name **/
	public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "crc_application_directory.properties";

	/** application directory property name **/
	public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.crc.applicationdir";

	/** application property filename* */
	public static final String APPLICATION_PROPERTIES_FILENAME = "crc.properties";

	/** property name for query manager ejb present in app property file* */
	private static final String EJB_LOCAL_JNDI_QUERYMANAGER_PROPERTIES = "queryprocessor.jndi.querymanagerlocal";

	/** property name for query info ejb present in app property file* */
	private static final String EJB_LOCAL_JNDI_QUERYINFO_PROPERTIES = "queryprocessor.jndi.queryinfolocal";

	/** property name for query run ejb present in app property file* */
	private static final String EJB_LOCAL_JNDI_QUERYRUN_PROPERTIES = "queryprocessor.jndi.queryrunlocal";

	/** property name for query result ejb present in app property file* */
	private static final String EJB_LOCAL_JNDI_QUERYRESULT_PROPERTIES = "queryprocessor.jndi.queryresultlocal";

	/** property name for pdo query ejb present in app property file* */
	private static final String EJB_LOCAL_JNDI_PDOQUERY_PROPERTIES = "queryprocessor.jndi.pdoquerylocal";

	/** property name for datasource present in app property file* */
	private static final String DATASOURCE_JNDI_PROPERTIES = "queryprocessor.jndi.datasource_name";

	/** property name for database connection string in app property file* */
	private static final String DATABASE_CONNECTION_STRING_PROPERTIES = "queryprocessor.database.connection_string";
	/** property name for database user in app property file* */
	private static final String DATABASE_CONNECTION_USER_PROPERTIES = "queryprocessor.database.user";
	/** property name for database password in app property file* */
	private static final String DATABASE_CONNECTION_PASSWORD_PROPERTIES = "queryprocessor.database.password";

	/** property name for metadata schema name* */
	private static final String METADATA_SCHEMA_NAME_PROPERTIES = "queryprocessor.db.metadataschema";

	/** property name for metadata schema name* */
	private static final String PMCELL_WS_URL_PROPERTIES = "queryprocessor.ws.pm.url";

	/** property name for PM bypass flag **/
	private static final String PMCELL_BYPASS_FLAG_PROPERTIES = "queryprocessor.ws.pm.bypass";

	/** property name for PM bypass project role name* */
	private static final String PMCELL_BYPASS_ROLE_PROPERTIES = "queryprocessor.ws.pm.bypass.role";

	/** property name for pm bypass project name **/
	private static final String PMCELL_BYPASS_PROJECT_PROPERTIES = "queryprocessor.ws.pm.bypass.project";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_DATASOURCE_PROPERTIES = "queryprocessor.ds.lookup.datasource";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_SCHEMANAME_PROPERTIES = "queryprocessor.ds.lookup.schemaname";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_SERVERTYPE_PROPERTIES = "queryprocessor.ds.lookup.servertype";

	/** property name for ontology url schema name **/
	private static final String ONTOLOGYCELL_WS_URL_PROPERTIES = "queryprocessor.ws.ontology.url";

	public static final String ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.url";

	public static final String ONTOLOGYCELL_GETTERMINFO_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.operation.getterminfo";

	public static final String ONTOLOGYCELL_GETCHILDREN_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.operation.getchildren";

	public static final String ONTOLOGYCELL_GETMODIFIERINFO_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.operation.getmodifierinfo";

	public static final String SINGLEPANEL_SKIPTEMPTABLE_PROPERTIES = "edu.harvard.i2b2.crc.setfinderquery.singlepanel.skiptemptable";

	public static final String SINGLEPANEL_SKIPTEMPTABLE_MAXCONCEPT_PROPERTIES = "edu.harvard.i2b2.crc.setfinderquery.skiptemptable.maxconcept";

	/** spring bean name for datasource **/
	private static final String DATASOURCE_BEAN_NAME = "dataSource";

	public static final String DEFAULT_SETFINDER_RESULT_BEANNAME = "defaultSetfinderResultType";

	public static final String PAGING_OBSERVATION_SIZE = "edu.harvard.i2b2.crc.pdo.paging.observation.size";

	public static final String PAGING_MINPERCENT = "edu.harvard.i2b2.crc.pdo.paging.inputlist.minpercent";

	public static final String PAGING_MINSIZE = "edu.harvard.i2b2.crc.pdo.paging.inputlist.minsize";

	public static final String PAGING_METHOD = "edu.harvard.i2b2.crc.pdo.paging.method";

	public static final String PAGING_ITERATION = "edu.harvard.i2b2.crc.pdo.paging.iteration";
	
	public static final	String MULTI_FACT_TABLE = "queryprocessor.multifacttable";

	/** class instance field* */
	private static volatile QueryProcessorUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static Properties appProperties = null;

	private static Properties loadProperties = null;

	/** field to store app datasource* */
	private DataSource dataSource = null;

	/** single instance of spring bean factory* */
	private BeanFactory beanFactory = null;

	private static volatile ProcessQueue pqMedium = null;
	private static volatile ProcessQueue pqLarge = null;
		
	private static final Object lock = new Object();
	
	/**
	 * Private constructor to make the class singleton
	 */
	private QueryProcessorUtil() {


	}

	static {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			log.error(e);

		}
	}

	/**
	 * Return this class instance
	 * 
	 * @return QueryProcessorUtil
	 */
	public static QueryProcessorUtil getInstance() {
		
		QueryProcessorUtil i = thisInstance;
		if (i == null) {
			synchronized (lock){
				i = thisInstance;
				if (i==null){
					i = new QueryProcessorUtil();
					thisInstance = i;
					serviceLocator = ServiceLocator.getInstance();

					pqMedium = new ProcessQueue(QueryManagerBeanUtil.MEDIUM_QUEUE);
					pqLarge = new ProcessQueue( QueryManagerBeanUtil.LARGE_QUEUE);


					Thread m1 = new Thread(pqMedium);
					m1.start();
					log.info("started MEDIUM");

					Thread m2 = new Thread(pqLarge);
					m2.start();
					log.info("started LARGE");

				}
			}

		}

		return i;
	}

	private static void startCronJob() {
		CronEjbLocal cronLocal;
		try {
			cronLocal = thisInstance.getCronLocal();
			cronLocal.start();
		} catch (I2B2Exception e) {

			e.printStackTrace();
		}

	}


	public StartAnalysisLocal getStartAnalysisLocal() throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (StartAnalysisLocal) ctx.lookup("QP1/StartAnalysis/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error Analysis ", e);
		}
	}

	public AnalysisPluginInfoLocal getAnalysisPluginInfoLocal()
			throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (AnalysisPluginInfoLocal) ctx
					.lookup("QP1/AnalysisPluginInfo/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error Anaylysis Plugin", e);
		}
	}

	public CronEjbLocal getCronLocal() throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (CronEjbLocal) ctx.lookup("QP1/CronEjb/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error Cron ", e);
		}
	}

	public PriviledgeLocal getPriviledgeLocal() throws I2B2Exception {
		return new PriviledgeBean();
		/* removed ejb
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (PriviledgeLocal) ctx.lookup("QP1/PriviledgeBean/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error Priviledge", e);
		}
		 */
	}

	public Scheduler getQuartzScheduler() throws I2B2Exception {
		return QuartzFactory.getInstance().getScheduler();
	}

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
						"file:" + appDir + "/" + "CRCApplicationContext.xml");
				beanFactory = ctx.getBeanFactory();
			} else {
				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						"classpath:" + "CRCApplicationContext.xml");
				beanFactory = ctx.getBeanFactory();
			}

		}
		return beanFactory;
	}

	/**
	 * Function returns database connection from app server
	 * 
	 * @return
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	public Connection getConnection() throws I2B2Exception, SQLException {
		String dataSourceName = getPropertyValue(DATASOURCE_JNDI_PROPERTIES);
		dataSource = (DataSource) serviceLocator
				.getAppServerDataSource(dataSourceName);

		Connection conn = dataSource.getConnection();
		return conn;
	}

	/**
	 * Function returns database connection from app server
	 * 
	 * @return
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	public Connection getManualConnection() throws I2B2Exception, SQLException {
		String dbConnectionString = getPropertyValue(DATABASE_CONNECTION_STRING_PROPERTIES);
		String dbUser = getPropertyValue(DATABASE_CONNECTION_USER_PROPERTIES);
		String dbPassword = getPropertyValue(DATABASE_CONNECTION_PASSWORD_PROPERTIES);
		return DriverManager.getConnection(dbConnectionString, dbUser,
				dbPassword);
	}

	/**
	 * Function to return metadata schema name, which is specified in property
	 * file
	 * 
	 * @return String
	 * @throws I2B2Exception
	 */
	public String getMetaDataSchemaName() throws I2B2Exception {
		return getPropertyValue(METADATA_SCHEMA_NAME_PROPERTIES);
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

	public long getPagingObservationSize() throws I2B2Exception {
		String obsPageSizeStr = getPropertyValue(PAGING_OBSERVATION_SIZE);
		return Long.parseLong(obsPageSizeStr);
	}

	public int getPagingInputListMinPercent() throws I2B2Exception {
		String pagingMinPercent = getPropertyValue(PAGING_MINPERCENT);
		return Integer.parseInt(pagingMinPercent);
	}

	public int getPagingInputListMinSize() throws I2B2Exception {
		String pagingMinSize = getPropertyValue(PAGING_MINSIZE);
		return Integer.parseInt(pagingMinSize);
	}

	public String getPagingMethod() throws I2B2Exception {
		String pagingMethod = getPropertyValue(PAGING_METHOD);
		return pagingMethod;
	}

	public int getPagingIterationCount() throws I2B2Exception {
		String pagingIteration = getPropertyValue(PAGING_ITERATION);
		return Integer.parseInt(pagingIteration);

	}



	/**
	 * Get Project management bypass project role
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

	public String getOntologyUrl() throws I2B2Exception {
		return getPropertyValue(ONTOLOGYCELL_WS_URL_PROPERTIES);
	}
	
	public boolean getDerivedFactTable()  {
		String setting = "false";
		try {
			setting = (getPropertyValue(MULTI_FACT_TABLE));

		} catch (I2B2Exception e) {
			log.info(e.getMessage());
			return false;
		}
		if (setting == null){
			return false;
		}
		else{
			if(setting.equals("true"))
				return true;
			else if(setting.equals("TRUE"))
				return true;
			else
				return false;
		}
	}
	

	/**
	 * Return app server datasource
	 * 
	 * @return datasource
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	public DataSource getDataSource(String dataSourceName)
			throws I2B2Exception {

		dataSource = (DataSource) serviceLocator
				.getAppServerDataSource(dataSourceName);
		//		DataSource dataSource = (DataSource) getSpringBeanFactory().getBean(
		//				dataSourceName);

		return dataSource;

	}
	public DataSource getSpringDataSource(String dataSourceName)
			throws I2B2Exception {
		return getDataSource( dataSourceName);
	}

	public String getCRCPropertyValue(String propertyName) throws I2B2Exception {
		return getPropertyValue(propertyName);
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
