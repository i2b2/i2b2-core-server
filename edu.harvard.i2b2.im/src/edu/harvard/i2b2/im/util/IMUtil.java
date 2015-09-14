/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *                 Raj Kuttan
 *                 Lori Phillips
 */
package edu.harvard.i2b2.im.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;


/**
 * This is the Workplace service's main utility class
 * This utility class provides support for
 * fetching resources like datasouce, to read application
 * properties, to get ejb home,etc.
 * $Id: WorkplaceUtil.java,v 1.5 2008/03/13 14:32:32 lcp5 Exp $
 * @author rkuttan
 */
public class IMUtil {
    /** property file name which holds application directory name **/
    public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "im_application_directory.properties";

    /** application directory property name **/
    public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.im.applicationdir";

    /** application property filename**/
    public static final String APPLICATION_PROPERTIES_FILENAME = "im.properties";

    /** property name for im schema name**/
    private static final String IM_SCHEMA_NAME_PROPERTIES = "im.bootstrapdb.imschema";

    /** property name for PM endpoint reference **/
    private static final String PM_WS_EPR = "im.ws.pm.url";
    
    private static  String CRC_WS_EPR = "";
    
    /** property name for PM webserver method **/
    private static final String PM_WS_METHOD = "im.ws.pm.webServiceMethod";

    /** property name for PM bypass **/
    private static final String PM_BYPASS = "im.ws.pm.bypass";

    /** property name for PM bypass project **/
    private static final String PM_BYPASS_PROJECT = "im.ws.pm.bypass.project";

    /** property name for PM bypass role **/
    private static final String PM_BYPASS_ROLE = "im.ws.pm.bypass.role";

    /** property name for EMPI Service **/
    private static final String EMPI_SERVICE = "im.empi.service";

    /** property name for EMPI endpoint reference **/
    private static final String OPENEMPI_WS_EPR = "im.empi.openempi.url";

    /** property name for EMPI username **/
    private static final String OPENEMPI_USERNAME = "im.empi.openempi.username";

    /** property name for EMPI password **/
    private static final String OPENEMPI_PASSWORD = "im.empi.openempi.password";


    /** property name to check if patient is in project  **/
    private static final String CHECK_PATIENT_IN_PROJECT = "im.checkPatientInProject";

    /** class instance field**/
    private static IMUtil thisInstance = null;

    /** service locator field**/
    private static ServiceLocator serviceLocator = null;

    /** field to store application properties **/
    private static Properties appProperties = null;

    /** log **/
    protected final Log log = LogFactory.getLog(getClass());

    /** field to store app datasource**/
    private DataSource dataSource = null;

    /** single instance of spring bean factory**/
    private BeanFactory beanFactory = null;

    /**
     * Private constructor to make the class singleton
     */
    private IMUtil() {
    }

    /**
     * Return this class instance
     * @return OntologyUtil
     */
    public static IMUtil getInstance() {
        if (thisInstance == null) {
            thisInstance = new IMUtil();
        }

        serviceLocator = ServiceLocator.getInstance();

        return thisInstance;
    }

    /**
     * Return the ontology spring config
     * @return
     */
    public BeanFactory getSpringBeanFactory() {
        if (beanFactory == null) {
            String appDir = null;

            try {
                //read application directory property file via classpath
                Properties loadProperties = ServiceLocator.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
                //read directory property
                appDir = loadProperties.getProperty(APPLICATIONDIR_PROPERTIES);
            } catch (I2B2Exception e) {
                log.error(APPLICATION_DIRECTORY_PROPERTIES_FILENAME +
                    "could not be located from classpath ");
            }

            if (appDir != null) {
                FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
                        "file:" + appDir + "/" +
                        "IMApplicationContext.xml");
                beanFactory = ctx.getBeanFactory();
            } else {
                FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
                        "classpath:" + "IMApplicationContext.xml");
                beanFactory = ctx.getBeanFactory();
            }
        }

        return beanFactory;
    }
    
    /**
     * Return im schema name
     * @return
     * @throws I2B2Exception
     */
    public String getIMDataSchemaName() throws I2B2Exception {
        return getPropertyValue(IM_SCHEMA_NAME_PROPERTIES).trim()+ ".";
    }

    /**
     * Return PM cell endpoint reference URL
     * @return
     * @throws I2B2Exception
     */
    public String getPmEndpointReference() throws I2B2Exception {
        return getPropertyValue(PM_WS_EPR).trim();
    }

    
    public void setCRCEndpointReference(String s)  {
         CRC_WS_EPR = s;
    }

    
    public String getCRCUrl() throws I2B2Exception {
        return CRC_WS_EPR;
    }

    

    /**
     * Return IM EMPI Service
     * @return
     * @throws I2B2Exception
     */
    public String getEMPIService() throws I2B2Exception {
        return getPropertyValue(EMPI_SERVICE).trim();
    }

    public String getOpenEMPIWebService() throws I2B2Exception {
        return getPropertyValue(OPENEMPI_WS_EPR).trim();
    }
    public String getOpenEMPIUsername() throws I2B2Exception {
        return getPropertyValue(OPENEMPI_USERNAME).trim();
    }
    public String getOpenEMPIPassword() throws I2B2Exception {
        return getPropertyValue(OPENEMPI_PASSWORD).trim();
    }
    
    
    public boolean checkPatientInProject() throws I2B2Exception {
        return Boolean.parseBoolean(getPropertyValue(CHECK_PATIENT_IN_PROJECT).trim());
    }
    
    
    /**
     * Return PM cell web service method
     * @return
     * @throws I2B2Exception
     */
    public String getPmWebServiceMethod() throws I2B2Exception {
        return getPropertyValue(PM_WS_METHOD).trim();
    }
    
    /**
     * Return PM bypass flag
     * @return
     * @throws I2B2Exception
     */
    public Boolean isPmBypass() throws I2B2Exception {
        return Boolean.valueOf(getPropertyValue(PM_BYPASS).trim());
    }

    /**
     * Return PM bypass project name
     * @return
     * @throws I2B2Exception
     */
    public String getPmBypassProject() throws I2B2Exception {
        return getPropertyValue(PM_BYPASS_PROJECT).trim();
    }

    /**
     * Return PM bypass role assignment
     * @return
     * @throws I2B2Exception
     */
    public String getPmBypassRole() throws I2B2Exception {
        return getPropertyValue(PM_BYPASS_ROLE).trim();
    }

    
    /**
     * Return app server datasource
     * @return datasource
     * @throws I2B2Exception
     */
    public DataSource getDataSource(String dataSourceName) throws I2B2Exception {    	
    	dataSource = (DataSource) serviceLocator
		.getAppServerDataSource(dataSourceName);
    	return dataSource;
  
    }
    
    //---------------------
    // private methods here
    //---------------------

    /**
     * Load application property file into memory
     */
    private String getPropertyValue(String propertyName)
        throws I2B2Exception {
        if (appProperties == null) {
            //read application directory property file
            Properties loadProperties = ServiceLocator.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);

            //read application directory property
            String appDir = loadProperties.getProperty(APPLICATIONDIR_PROPERTIES);

            if (appDir == null) {
                throw new I2B2Exception("Could not find " +
                    APPLICATIONDIR_PROPERTIES + "from " +
                    APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
            }

            String appPropertyFile = appDir + "/" +
                APPLICATION_PROPERTIES_FILENAME;

            try {
                FileSystemResource fileSystemResource = new FileSystemResource(appPropertyFile);
                PropertiesFactoryBean pfb = new PropertiesFactoryBean();
                pfb.setLocation(fileSystemResource);
                pfb.afterPropertiesSet();
                appProperties = (Properties) pfb.getObject();
            } catch (IOException e) {
                throw new I2B2Exception("Application property file(" +
                    appPropertyFile +
                    ") missing entries or not loaded properly");
            }

            if (appProperties == null) {
                throw new I2B2Exception("Application property file(" +
                    appPropertyFile +
                    ") missing entries or not loaded properly");
            }
        }

        String propertyValue = appProperties.getProperty(propertyName);

        if ((propertyValue != null) && (propertyValue.trim().length() > 0)) {
            ;
        } else {
            throw new I2B2Exception("Application property file(" +
                APPLICATION_PROPERTIES_FILENAME + ") missing " + propertyName +
                " entry");
        }

        return propertyValue;
    }
}
