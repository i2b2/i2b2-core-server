/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util;

import edu.harvard.i2b2.common.exception.I2B2Exception;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;


/**
 * ServiceLocator class to get reference to resource like EJB, JMS, Datasource etc.
 * This is singleton class and caches resources.
 * @author rk903
 */
public class ServiceLocator {
    //datasource bean name
    public static String DATASOURCE_BEAN_NAME = "dataSource";

    //default client property file name
    public static final String CLIENT_PROPERTY_CONFIG_LOCATION = "client.properties";
 //   private static BeanFactory beanFactory = null;
    private static Properties clientProperties = null;

    //to make this class singleton
    private static ServiceLocator thisInstance;

    static {
        try {
            thisInstance = new ServiceLocator();
        } catch (ServiceLocatorException se) {
            System.err.println(se);
            se.printStackTrace(System.err);
        }
    }

    private InitialContext ic;

    //used to hold references to EJBHomes/JMS Resources for re-use
    private Map cache;

    private ServiceLocator() throws ServiceLocatorException {
        try {
            ic = new InitialContext();
            cache = Collections.synchronizedMap(new HashMap());
        } catch (NamingException ne) {
            throw new ServiceLocatorException(ne);
        } catch (Exception e) {
            throw new ServiceLocatorException(e);
        }
    }

    public static ServiceLocator getInstance() {
        return thisInstance;
    }

    /* TODO MM Removed
    public  BeanFactory getSpringFactory() {
        if (beanFactory == null) {
            BeanFactoryLocator beanFactoryLoc = SingletonBeanFactoryLocator.getInstance();
            BeanFactoryReference beanFactoryRef = beanFactoryLoc.useBeanFactory(
                    "edu.harvard.i2b2Core2");
            beanFactory = beanFactoryRef.getFactory();
        }

        return beanFactory;
    }
	*/
    

    
    /**
      * Get the database datasource.
      * @param dataSourceName
      * @return The data source
      * @throws MPGException
      */
    public DataSource getAppServerDataSource(String dataSourceName)
        throws I2B2Exception {
        javax.sql.DataSource dataSource = null;

        try {
            Context context = new InitialContext();
            dataSource = (javax.sql.DataSource) context.lookup(dataSourceName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new I2B2Exception("Servicelocator.getDBDataSource", ex);
        }

        return dataSource;
    }

    /**
     * Function to get client's property context.
     * @return PropertyUtil
    
    public static Properties getClientProperty() throws I2B2Exception {
        if (clientProperties == null) {
            clientProperties = getProperties(CLIENT_PROPERTY_CONFIG_LOCATION);
        }

        return clientProperties;
    }
 */
    /**
     * Return Properties object, for input property file.
     * @param propFileName
     * @return java.util.Properties
     * @throws I2B2Exception
    
    public static Properties getProperties(String propFileName)
        throws I2B2Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(new ClassPathResource(propFileName));

        Properties props = null;

        try {
            pfb.afterPropertiesSet();
            props = (Properties) pfb.getObject();
        } catch (IOException ioEx) {
            throw new I2B2Exception("IOException " + ioEx.getMessage(), ioEx);
        }

        return props;
    }
 */
}
