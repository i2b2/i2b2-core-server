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
package edu.harvard.i2b2.im.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.im.datavo.pm.ParamType;


/**
 * This is the Workplace service's main utility class
 * This utility class provides support for
 * fetching resources like datasouce, to read application
 * properties, to get ejb home,etc.
 * $Id: WorkplaceUtil.java,v 1.5 2008/03/13 14:32:32 lcp5 Exp $
 * @author rkuttan
 */
public class IMUtil {

    /** property name for PM endpoint reference **/
    private static final String PM_WS_EPR = "im.ws.pm.url";
    
    private static  String CRC_WS_EPR = "";
    
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
     * Return im schema name
     * @return
     * @throws I2B2Exception
     */
    public String getIMDataSchemaName() throws I2B2Exception {
		try {
			Connection conn = dataSource.getConnection();
			
			String metadataSchema = conn.getSchema() + ".";
			conn.close();
			return metadataSchema;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //getPropertyValue(METADATA_SCHEMA_NAME_PROPERTIES).trim() + ".";
		return null;

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
     * Return app server datasource
     * @return datasource
     * @throws I2B2Exception
     */
    public DataSource getDataSource(String dataSourceName) throws I2B2Exception {    	
    	dataSource = (DataSource) serviceLocator
		.getAppServerDataSource(dataSourceName);
    	return dataSource;
  
    }
    

    /**
     * Load application property file into memory
     */
	private String getPropertyValue(String propertyName) throws I2B2Exception {

		if (appProperties == null) {



			//		log.info(sql + domainId + projectId + ownerId);
			//	List<ParamType> queryResult = null;
			try {
				DataSource   ds = this.getDataSource("java:/IMBootStrapDS");

				JdbcTemplate jt =  new JdbcTemplate(ds);
				Connection conn = ds.getConnection();
				
				String metadataSchema = conn.getSchema();
				conn.close();
				String sql =  "select * from " + metadataSchema + ".hive_cell_params where status_cd <> 'D' and cell_id = 'IM'";

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

