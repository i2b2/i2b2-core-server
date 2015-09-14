/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;
//import edu.harvard.i2b2.crc.util.HibernateUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * DAO abstract class to provide common dao functions $Id: CRCDAO.java,v 1.8
 * 2008/03/27 23:38:50 rk903 Exp $
 * 
 * @author rkuttan
 * @see Connection
 * @see Session
 */
public abstract class CRCDAO {
	/** log * */
	protected final Log log = LogFactory.getLog(CRCDAO.class);

	protected DataSource dataSource = null;

	protected String dbSchemaName = null;

	/**
	 * Returns connection from appserver datasource
	 * 
	 * @return Appserver database Connection
	 * @throws SQLException
	 * @throws I2B2DAOException
	 * @throws SQLException
	 */
	protected Connection getConnection() throws SQLException, I2B2DAOException {
		try {
			return QueryProcessorUtil.getInstance().getConnection();
		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2DAOException("Could not get connection", i2b2Ex);
		}
	}

	protected DataSource getApplicationDataSource(String dataSourceName)
			throws I2B2DAOException {
		try {
			// dataSource = (DataSource)
			// crcUtil.getSpringDataSource(dataSourceName);
			DataSource dataSource = ServiceLocator.getInstance()
					.getAppServerDataSource(dataSourceName);
			return dataSource;
		} catch (I2B2Exception i2b2Ex) {
			log.error(i2b2Ex);
			throw new I2B2DAOException(
					"Error getting appliation/spring datasource "
							+ dataSourceName + " : " + i2b2Ex.getMessage(),
					i2b2Ex);
		}
	}

	/**
	 * Helper function to construct {@link OutputOptionType} from given boolean
	 * flags
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return OutputOptionType
	 */
	protected OutputOptionType buildOutputOptionType(boolean detailFlag,
			boolean blobFlag, boolean statusFlag) {
		OutputOptionType outputOptionType = new OutputOptionType();
		outputOptionType.setOnlykeys((detailFlag) ? false : true);
		outputOptionType.setBlob(blobFlag);
		outputOptionType.setTechdata(statusFlag);

		return outputOptionType;
	}

	/**
	 * Get hibernate session
	 * 
	 * @return Session
	 */
//	protected Session getSession() {
//		return HibernateUtil.getSession();
//	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getDbSchemaName() {
		return this.dbSchemaName;
	}

	public void setDbSchemaName(String dbSchemaName) {
		if (dbSchemaName != null && dbSchemaName.endsWith(".")) { 
			this.dbSchemaName = dbSchemaName.trim();
		}
		else if (dbSchemaName != null) { 
			this.dbSchemaName = dbSchemaName.trim() + ".";
		}
		
	}

}
