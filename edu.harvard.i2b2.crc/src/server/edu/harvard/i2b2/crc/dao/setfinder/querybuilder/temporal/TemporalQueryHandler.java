/*
 * Copyright (c) 2006-2013 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal;


import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.QueryConstraintStrategy;

import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryJoinColumnType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryJoinType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QuerySpanConstraintType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryAggregateOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryConstraintType;


public class TemporalQueryHandler extends CRCDAO {

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
	private boolean queryWithoutTempTableFlag = false;
	private boolean isTemporalQuery = false;

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
						DAOFactoryHelper.POSTGRESQL)) {
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

	
	public String buildSql() throws JAXBUtilException, I2B2Exception {
		TemporalQuery tQuery = new TemporalQuery(this.dataSourceLookup, this.projectParamMap, this.queryXML, this.allowLargeTextValueConstrainFlag);
		if (this.queryWithoutTempTableFlag)
			tQuery.getQueryOptions().setQueryConstraintLogic(QueryConstraintStrategy.DERIVED_TABLES);
		String tQuerySql = tQuery.buildSql();
		this.ignoredItemMessageBuffer = tQuery.getIgnoredItemMessageBuffer();
		this.maxPanelNum = tQuery.getMaxPanelIndex();
		this.isTemporalQuery = (tQuery.getSubQueryCount()>1?true:false);
		System.out.println(tQuerySql);
		
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
}
