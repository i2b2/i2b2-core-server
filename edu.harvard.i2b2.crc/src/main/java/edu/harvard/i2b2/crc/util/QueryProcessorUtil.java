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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.dao.mapper.HiveCellParam;
import edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ParamsType;
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


	/** property name for metadata schema name* */
	private static final String PMCELL_WS_URL_PROPERTIES = "queryprocessor.ws.pm.url";  //http://localhost:9090/i2b2/services/PMService/getServices


	/** property name for ontology url schema name **/
	private static final String ONTOLOGYCELL_WS_URL_PROPERTIES = "queryprocessor.ws.ontology.url"; //http://localhost:9090/i2b2/services/OntologyService/getTermInfo

	public static final String ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.url"; //http://localhost:9090/i2b2/services/OntologyService

	public static final String ONTOLOGYCELL_GETTERMINFO_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.operation.getterminfo";  //   /getTermInfo

	public static final String ONTOLOGYCELL_GETCHILDREN_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.operation.getchildren"; //   /getChildren

	public static final String ONTOLOGYCELL_GETMODIFIERINFO_URL_PROPERTIES = "edu.harvard.i2b2.crc.delegate.ontology.operation.getmodifierinfo";  //   /getModifierInfo

	//	public static final String SINGLEPANEL_SKIPTEMPTABLE_PROPERTIES = "edu.harvard.i2b2.crc.setfinderquery.singlepanel.skiptemptable";

	public static final String SINGLEPANEL_SKIPTEMPTABLE_MAXCONCEPT_PROPERTIES = "edu.harvard.i2b2.crc.setfinderquery.skiptemptable.maxconcept";   // 40

	public static final String PAGING_OBSERVATION_SIZE = "edu.harvard.i2b2.crc.pdo.paging.observation.size";	//7500

	public static final String PAGING_MINPERCENT = "edu.harvard.i2b2.crc.pdo.paging.inputlist.minpercent";	// 20

	public static final String PAGING_MINSIZE = "edu.harvard.i2b2.crc.pdo.paging.inputlist.minsize";		// 1

	public static final String PAGING_METHOD = "edu.harvard.i2b2.crc.pdo.paging.method";  //# Paging method can be SUBDIVIDE_INPUT_METHOD / AVERAGE_OBSERVATION_METHOD
	// SUBDIVIDE_INPUT_METHOD
	public static final String PAGING_ITERATION = "edu.harvard.i2b2.crc.pdo.paging.iteration";				//100

	public static final	String MULTI_FACT_TABLE = "queryprocessor.multifacttable";					// false

	/** class instance field* */
	private static volatile QueryProcessorUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static List<ParamType> appProperties = null;

	private static Properties loadProperties = null;

	/** field to store app datasource* */
	private DataSource dataSource = null;


	private static volatile ProcessQueue pqMedium = null;
	private static volatile ProcessQueue pqLarge = null;

	private static final Object lock = new Object();

	/**
	 * Private constructor to make the class singleton
	 */
	private QueryProcessorUtil() {


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


	public StartAnalysisLocal getStartAnalysisLocal() throws I2B2Exception {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (StartAnalysisLocal) ctx.lookup("QP1/StartAnalysis/local");
		} catch (NamingException e) {
			throw new I2B2Exception("Bean lookup error Analysis ", e);
		}
	}
	
    public Scheduler getQuartzScheduler() throws I2B2Exception {
        return QuartzFactory.getInstance().getScheduler();
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

	}

	/**
	 * Function returns database connection from app server
	 * 
	 * @return
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	/*
	public Connection getConnection() throws I2B2Exception, SQLException {
		String dataSourceName = getPropertyValue(DATASOURCE_JNDI_PROPERTIES);
		dataSource = serviceLocator
				.getAppServerDataSource(dataSourceName);

		Connection conn = dataSource.getConnection();
		return conn;
	}
	 */
	/**
	 * Function returns database connection from app server
	 * 
	 * @return
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	/*	public Connection getManualConnection() throws I2B2Exception, SQLException {
		String dbConnectionString = getPropertyValue(DATABASE_CONNECTION_STRING_PROPERTIES);
		String dbUser = getPropertyValue(DATABASE_CONNECTION_USER_PROPERTIES);
		String dbPassword = getPropertyValue(DATABASE_CONNECTION_PASSWORD_PROPERTIES);
		return DriverManager.getConnection(dbConnectionString, dbUser,
				dbPassword);
	}
	 */
	/**
	 * Function to return metadata schema name, which is specified in property
	 * file
	 * 
	 * @return String
	 * @throws I2B2Exception
	 */
	//	public String getMetaDataSchemaName() throws I2B2Exception {
	//		return getPropertyValue(METADATA_SCHEMA_NAME_PROPERTIES);
	//	}

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
	/*
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
	 */

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

		dataSource = serviceLocator
				.getAppServerDataSource(dataSourceName);


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




	/**
	 * Load application property file into memory
	 */
	private String getPropertyValue(String propertyName) throws I2B2Exception {
		if (appProperties == null) {



			//		log.info(sql + domainId + projectId + ownerId);
			//	List<ParamType> queryResult = null;
			try {
				DataSource   ds = this.getDataSource("java:/CRCBootStrapDS");


				Connection conn = ds.getConnection();
				
				String metadataSchema = conn.getSchema();
				conn.close();
				JdbcTemplate jt =  new JdbcTemplate(ds);
				String sql =  "select * from " + metadataSchema + ".hive_cell_params where status_cd <> 'D' and cell_id = 'CRC'";

				log.debug("Start query");
				appProperties = jt.query(sql, new getHiveCellParam());
				log.debug("End query");

			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
				throw new I2B2DAOException("Database error reading hive_cell_params");
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

