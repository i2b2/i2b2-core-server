/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;

/**
 * DAO abstract class to provide common dao functions $Id: CRCLoaderDAO.java,v
 * 1.3 2008/06/03 21:09:03 rk903 Exp $
 * 
 * @author rkuttan
 * @see Connection
 * @see Session
 */
public class CRCLoaderDAO {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	protected DataSource dataSource = null;

	protected String dbSchemaName = null;

	public final static String DATASOUCE_JNDI_NAME = "java:QueryToolDS";

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
		} else if (dbSchemaName != null) {
			this.dbSchemaName = dbSchemaName.trim() + ".";
		}

	}

	public Map<String, ParamType> buildNVParam(List<ParamType> paramTypeList) {
		Map<String, ParamType> paramMap = new HashMap<String, ParamType>();
		for (ParamType paramType : paramTypeList) {
			if (paramType.getColumn() != null) {
				paramMap.put(paramType.getColumn(), paramType);
			}
		}
		return paramMap;
	}

	public void getSQLServerProcedureError(String serverType,
			CallableStatement callStmt, int outParamIndex) throws SQLException,
			I2B2Exception {

		if (serverType.equalsIgnoreCase(DataSourceLookupDAOFactory.SQLSERVER)) {
			String errorMsg = callStmt.getString(outParamIndex);
			if (errorMsg != null) {
				System.out.println("error codde" + errorMsg);
				throw new I2B2Exception("Error from stored procedure ["
						+ errorMsg + "]");
			}
		}
	}

}
