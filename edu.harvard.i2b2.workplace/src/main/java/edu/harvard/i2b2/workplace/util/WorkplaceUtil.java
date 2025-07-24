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
package edu.harvard.i2b2.workplace.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

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
import edu.harvard.i2b2.workplace.datavo.pm.ParamType;

/**
 * This is the Workplace service's main utility class
 * This utility class provides support for
 * fetching resources like datasouce, to read application
 * properties, to get ejb home,etc.
 * $Id: WorkplaceUtil.java,v 1.5 2008/03/13 14:32:32 lcp5 Exp $
 * @author rkuttan
 */
public class WorkplaceUtil {
    /** property file name which holds application directory name **/
 //   public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "workplace_application_directory.properties";

    /** application directory property name **/
 //   public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.workplace.applicationdir";

    /** application property filename**/
 //   public static final String APPLICATION_PROPERTIES_FILENAME = "workplace.properties";

    /** property name for datasource present in app property file**/
//   private static final String DATASOURCE_JNDI_PROPERTIES = "workplace.jndi.datasource_name";

    /** property name for metadata schema name**/
 //   private static final String METADATA_SCHEMA_NAME_PROPERTIES = "workplace.bootstrapdb.metadataschema";

    /** spring bean name for datasource **/
 //   private static final String DATASOURCE_BEAN_NAME = "dataSource";

    /** property name for PM endpoint reference **/
    private static final String PM_WS_EPR = "workplace.ws.pm.url";
    
    private static  String CRC_WS_EPR = "";
    
    /** property name for PM webserver method **/
    private static final String PM_WS_METHOD = "workplace.ws.pm.webServiceMethod";

    /** property name for PM bypass **/
//    private static final String PM_BYPASS = "workplace.ws.pm.bypass";

    /** property name for PM bypass project **/
//    private static final String PM_BYPASS_PROJECT = "workplace.ws.pm.bypass.project";

    /** property name for PM bypass role **/
//    private static final String PM_BYPASS_ROLE = "workplace.ws.pm.bypass.role";

    /** class instance field**/
    private static WorkplaceUtil thisInstance = null;

    /** service locator field**/
    private static ServiceLocator serviceLocator = null;

    /** field to store application properties **/
    private static List<ParamType> appProperties = null;
    
    /** log **/
    protected final Log log = LogFactory.getLog(getClass());

    /** field to store app datasource**/
    private DataSource dataSource = null;

    /** single instance of spring bean factory**/
    private BeanFactory beanFactory = null;

    /**
     * Private constructor to make the class singleton
     */
    private WorkplaceUtil() {
    }

    /**
     * Return this class instance
     * @return OntologyUtil
     */
    public static WorkplaceUtil getInstance() {
        if (thisInstance == null) {
            thisInstance = new WorkplaceUtil();
        }

        serviceLocator = ServiceLocator.getInstance();

        return thisInstance;
    }

    /**
     * Return the ontology spring config
     * @return
     */
    /*
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

            if (appDir != null && !appDir.trim().equals("")) {
                FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
                        "file:" + appDir + "/" +
                        "WorkplaceApplicationContext.xml");
                beanFactory = ctx.getBeanFactory();
            } else {
				 String path = WorkplaceUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				 path = path.substring(0, path.indexOf("deployments"));
                FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
                        path + "configuration/workplaceapp/WorkplaceApplicationContext.xml");
                beanFactory = ctx.getBeanFactory();
            }
        }

        return beanFactory;
    }
*/
    /**
     * Return metadata schema name
     * @return
     * @throws I2B2Exception
     */
 //   public String getMetaDataSchemaName() throws I2B2Exception {
 //       return getPropertyValue(METADATA_SCHEMA_NAME_PROPERTIES).trim()+ ".";
 //   }

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
     * Return app server datasource
     * @return datasource
     * @throws I2B2Exception
     */
    public DataSource getDataSource(String dataSourceName) throws I2B2Exception {    	
    	dataSource = serviceLocator
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



			//		log.info(sql + domainId + projectId + ownerId);
			//	List<ParamType> queryResult = null;
			try {
				DataSource   ds = this.getDataSource("java:/WorkplaceBootStrapDS");

				JdbcTemplate jt = new JdbcTemplate(ds);
				Connection conn = ds.getConnection();
				
				String metadataSchema = conn.getSchema();
				conn.close();
				String sql =  "select * from " + metadataSchema + ".hive_cell_params where status_cd <> 'D' and cell_id = 'WORK'";

				log.debug("Start query");
				appProperties =  jt.query(sql, new getHiveCellParam());
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

		if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
			throw new I2B2Exception("Application property file("
					//	+ APPLICATION_PROPERTIES_FILENAME + ") missing "
					+ propertyName + " entry");
		}

		return propertyValue;
    	/*
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
			if (appDir.trim().equals(""))
			{
				
				appDir =  "standalone/configuration/workplaceapp";
			
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
        */
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
