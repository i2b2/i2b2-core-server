/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.fr.util;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the CRC application's main utility class This utility class provides
 * support for fetching resources like datasouce, to read application
 * properties, to get ejb home,etc. $Id: CRCLoaderUtil.java,v 1.7
 * 2007/04/25 15:05:11 rk903 Exp $
 * 
 * @author rkuttan
 */
public class FRUtil {
	
	  /** log **/
    protected final static Log log = LogFactory.getLog(FRUtil.class);

	/** property file name which holds application directory name **/
	public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "fr_application_directory.properties";

	/** application directory property name **/
	public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.fr.applicationdir";
	
	/** application property filename* */
	public static final String APPLICATION_PROPERTIES_FILENAME = "edu.harvard.i2b2.fr.properties";

	/** property name for datasource present in app property file* */
	//private static final String DATASOURCE_JNDI_PROPERTIES = "queryprocessor.jndi.datasource_name";
		
	/** property name for metadata schema name* */
	private static final String PMCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.fr.ws.pm.url";

	/** property name for metadata schema name* */
	private static final String PMCELL_BYPASS_FLAG_PROPERTIES = "edu.harvard.i2b2.fr.ws.pm.bypass";

	/** property name for metadata schema name* */
	private static final String PMCELL_BYPASS_ROLE_PROPERTIES = "edu.harvard.i2b2.fr.ws.pm.bypass.role";
	
	private static final String PM_WS_EPR = "fr.ws.pm.url";

	/** property name for PM webservice method **/
	private static final String PM_WS_METHOD = "edu.harvard.i2b2.fr.ws.pm.webServiceMethod";

	/** class instance field* */
	private static FRUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static Properties appProperties = null;
	
	private static Properties loadProperties = null;

	/** field to store app datasource* */
	private DataSource dataSource = null;

	/**
	 * Private constructor to make the class singleton
	 */
	private FRUtil() {
	}


	
	/**
	 * Return this class instance
	 * @return QueryProcessorUtil
	 */
	public static FRUtil getInstance() {
		if (thisInstance == null) {
			thisInstance = new FRUtil();
			serviceLocator = ServiceLocator.getInstance();
		}
		return thisInstance;
	}
	
	
	
	/**
	 * Get Project managment cell's service url
	 * @return
	 * @throws I2B2Exception
	 */
	public String getProjectManagementCellUrl() throws I2B2Exception {
		return getPropertyValue(PMCELL_WS_URL_PROPERTIES);
	}

	/**
	 * Get Project management bypass flag
	 * @return
	 * @throws I2B2Exception
	 */
	public boolean getProjectManagementByPassFlag() throws I2B2Exception {
		String  pmByPassFlag = getPropertyValue(PMCELL_BYPASS_FLAG_PROPERTIES);
		if (pmByPassFlag == null) { 
			return false;
		}
		else if (pmByPassFlag.trim().equalsIgnoreCase("true")) {
			return true;
		}
		else { 
			return false;
		}
	}
	
	/**
	 * Get Project management bypass flag
	 * @return
	 * @throws I2B2Exception
	 */
	public String getProjectManagementByPassRole() throws I2B2Exception {
		return getPropertyValue(PMCELL_BYPASS_ROLE_PROPERTIES );
	}


	// ---------------------
	// private methods here
	// ---------------------

	/**
	 * Load application property file into memory
	 */
	private String getPropertyValue(String propertyName) throws I2B2Exception {
		if (appProperties == null) {
			//read application directory property file
			loadProperties = ServiceLocator.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			//read application directory property
			String appDir = loadProperties.getProperty(APPLICATIONDIR_PROPERTIES);
			if (appDir == null) { 
				throw new I2B2Exception("Could not find " + APPLICATIONDIR_PROPERTIES + "from " + APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			}
			String appPropertyFile = appDir+"/"+APPLICATION_PROPERTIES_FILENAME;
			try { 
				FileSystemResource fileSystemResource = new FileSystemResource(appPropertyFile);
				PropertiesFactoryBean pfb = new PropertiesFactoryBean();
				pfb.setLocation(fileSystemResource);
				pfb.afterPropertiesSet();
				appProperties = (Properties) pfb.getObject();
			} catch (IOException e) {
				throw new I2B2Exception(
						"Application property file("+appPropertyFile+") missing entries or not loaded properly");
			}
			if (appProperties == null) {
				throw new I2B2Exception(
						"Application property file("+appPropertyFile+") missing entries or not loaded properly");
			}
		}

		String propertyValue = appProperties.getProperty(propertyName);

		if ((propertyValue != null) && (propertyValue.trim().length() > 0)) {
			;
		} else {
			throw new I2B2Exception(
					"Application property file("+APPLICATION_PROPERTIES_FILENAME+") missing "
							+ propertyName + " entry");
		}

		return propertyValue;
	}



	public String getPmEndpointReference() throws I2B2Exception {
		return getPropertyValue(PMCELL_WS_URL_PROPERTIES).trim();
	}



	public String getPmWebServiceMethod() throws I2B2Exception {
		return getPropertyValue(PM_WS_METHOD).trim();
	}

}
