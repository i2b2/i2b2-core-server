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
 * 		Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal;


import java.util.List;
import java.util.Map;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.QueryConstraintStrategy;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;


public class TemporalQueryHandler extends CRCDAO {

	protected final Logger logesapi = ESAPI.getLogger(getClass());

	private DataSourceLookup dataSourceLookup = null;
	private String queryXML = null;
	private String noLockSqlServer = " ";
	private String tempTableName = " ";
	private String tempDxTableName = " ";
	private StringBuffer ignoredItemMessageBuffer = new StringBuffer();
	private boolean encounterSetOutputFlag = false;
	private int maxPanelNum = 0;
	private ProcessTimingReportUtil processTimingUtil = null;
	private String processTimingFlag = ProcessTimingReportUtil.NONE;
	private Map projectParamMap = null;
	private StringBuffer processTimingStr = new StringBuffer();
	private boolean allowLargeTextValueConstrainFlag = true;
	private List<String> userRoles = null;
	private boolean queryWithoutTempTableFlag = false;
	private boolean isTemporalQuery = false;
	private boolean isProtectedQuery = false;

	public TemporalQueryHandler(DataSourceLookup dataSourceLookup, String queryXML,
			boolean encounterSetOutputFlag) {
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.queryXML = queryXML;
		this.encounterSetOutputFlag = encounterSetOutputFlag;
		try {
			this.processTimingUtil = new ProcessTimingReportUtil(dataSourceLookup);
		} catch (I2B2DAOException e) {
			log.error("Error creating ProcessTimingReportUtil [" + e.getMessage() + "]");
		}

		if (this.dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			noLockSqlServer = " WITH(NOLOCK) ";
			tempTableName = "#global_temp_table";
			tempDxTableName = "#dx";
		} else if (this.dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || this.dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL) || this.dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SNOWFLAKE)) {
			tempTableName = "QUERY_GLOBAL_TEMP";
			tempDxTableName = "DX";
		}
	}

	public int getMaxPanelNumber() {
		return maxPanelNum;
	}

	public void setProcessTimingFlag(String level) { 
		this.processTimingFlag = level;
	}

	public void setProjectParamMap(Map projectParamMap) { 
		this.projectParamMap = projectParamMap;
	}

	public String getProcessTimingMessage() {
		return this.processTimingStr.toString();
	}

	public void setAllowLargeTextValueConstrainFlag(boolean allowLargeTextValueConstrainFlag)  { 
		this.allowLargeTextValueConstrainFlag = allowLargeTextValueConstrainFlag;
	}

	public void setUserRoles(List<String> userRoles)  { 
		this.userRoles = userRoles;
	}
	
	public String buildSql() throws JAXBUtilException, I2B2Exception {
		TemporalQuery tQuery = new TemporalQuery(this.dataSourceLookup, this.projectParamMap, this.queryXML, this.allowLargeTextValueConstrainFlag, this.userRoles);
		if (this.queryWithoutTempTableFlag)
			tQuery.getQueryOptions().setQueryConstraintLogic(QueryConstraintStrategy.DERIVED_TABLES);
		String tQuerySql = tQuery.buildSql();
		this.ignoredItemMessageBuffer = tQuery.getIgnoredItemMessageBuffer();
		this.maxPanelNum = tQuery.getMaxPanelIndex();
		this.isTemporalQuery = (tQuery.getSubQueryCount()>1?true:false);
		this.isProtectedQuery = tQuery.isProtectedQuery();
		logesapi.debug(null,tQuerySql);
		
		return tQuerySql;

	}
	
	public String getIgnoredItemMessage() {
		if (this.ignoredItemMessageBuffer != null
				&& this.ignoredItemMessageBuffer.length() > 0) {
			return "Missing Concept in Ontology Cell : \n"
					+ this.ignoredItemMessageBuffer.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * @return the queryWithoutTempTableFlag
	 */
	public boolean getQueryWithoutTempTableFlag() {
		return queryWithoutTempTableFlag;
	}

	/**
	 * @param queryWithoutTempTableFlag the queryWithoutTempTableFlag to set
	 */
	public void setQueryWithoutTempTableFlag(boolean queryWithoutTempTableFlag) {
		this.queryWithoutTempTableFlag = queryWithoutTempTableFlag;
	}

	public boolean isTemporalQuery(){
		return this.isTemporalQuery;
	}

	public boolean isProtectedQuery() {
		return isProtectedQuery;
	}
}
