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
package edu.harvard.i2b2.fr.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.fr.datavo.pm.ParamType;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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

	/** property name for metadata schema name* */
	private static final String PMCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.fr.ws.pm.url";

	
	private static final String PM_WS_EPR = "fr.ws.pm.url";

	/** property name for PM webservice method **/
	private static final String PM_WS_METHOD = "edu.harvard.i2b2.fr.ws.pm.webServiceMethod";

	/** class instance field* */
	private static FRUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static List<ParamType> appProperties = null;

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
	
	
	public String getMetaDataSchemaName() throws I2B2Exception {
		try {
			Connection conn = dataSource.getConnection();
			
			String metadataSchema = conn.getSchema() + ".";
			conn.close();
			return metadataSchema + "." ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //getPropertyValue(METADATA_SCHEMA_NAME_PROPERTIES).trim() + ".";
		return null;
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
				DataSource   ds = this.getDataSource("java:/FRBootStrapDS");

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



	public String getPmEndpointReference() throws I2B2Exception {
		return getPropertyValue(PMCELL_WS_URL_PROPERTIES).trim();
	}



	public String getPmWebServiceMethod() throws I2B2Exception {
		return getPropertyValue(PM_WS_METHOD).trim();
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

