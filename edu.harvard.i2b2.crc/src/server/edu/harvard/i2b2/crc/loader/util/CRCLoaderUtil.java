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
package edu.harvard.i2b2.crc.loader.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
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

	/** property name for datasource present in app property file* */
	private static final String DATASOURCE_JNDI_PROPERTIES = "queryprocessor.jndi.datasource_name";

	/** property name for metadata schema name* */
	private static final String PMCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.crc.loader.ws.pm.url";


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
	private static List<ParamType> appProperties = null;

	private static Properties loadProperties = null;

	/** field to store app datasource* */
	private DataSource dataSource = null;


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
		//		DataSource dataSource = (DataSource) getSpringBeanFactory().getBean(
		//				dataSourceName);

		return dataSource;

	}
	/**
	 * Function to create spring bean factory
	 * 
	 * @return BeanFactory
	 */
	/*
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

			if (appDir != null && !appDir.equals("")) {
				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						"file:" + appDir + "/"
								+ "CRCLoaderApplicationContext.xml");
				beanFactory = ctx.getBeanFactory();
			} else {

				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						  "standalone/configuration/crcapp/CRCLoaderApplicationContext.xml");
				beanFactory = ctx.getBeanFactory();
			}

		}
		return beanFactory;
	}
	 */

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
	/*
	public DataSource getSpringDataSource(String dataSourceName)
			throws I2B2Exception {
		DataSource dataSource = (DataSource) getSpringBeanFactory().getBean(
				dataSourceName);

		return dataSource;

	}
	 */

	// ---------------------
	// private methods here
	// ---------------------

	/**
	 * Load application property file into memory
	 */


	/**
	 * Load application property file into memory
	 */
	private String getPropertyValue(String propertyName) throws I2B2Exception {
		if (appProperties == null) {



			//		log.info(sql + domainId + projectId + ownerId);
			//	List<ParamType> queryResult = null;
			try {
				DataSource   ds = this.getDataSource("java:/CRCBootStrapDS");

				JdbcTemplate jt =  new JdbcTemplate(ds);
				Connection conn = ds.getConnection();
				
				String metadataSchema = conn.getSchema();
				conn.close();
				String sql =  "select * from " + metadataSchema + ".hive_cell_params where status_cd <> 'D' and cell_id = 'CRC'";

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

		if ((propertyValue == null) && (propertyValue.trim().length() == 0)) {
			throw new I2B2Exception("Application property file("
					//	+ APPLICATION_PROPERTIES_FILENAME + ") missing "
					+ propertyName + " entry");
		}

		return propertyValue;
	}

/*

	public class  HiveCellParamMapper void getHiveCellParam() implements RowMapper {  

		public ParamType mapRow(ResultSet rs, int rowNum) throws SQLException {  
			ParamType param = new ParamType();
			param.setId(rs.getInt("id"));
			param.setName(rs.getString("param_name_cd"));
			param.setValue(rs.getString("value"));
			param.setDatatype(rs.getString("datatype_cd"));
			return param;
		}
	}
*/
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


