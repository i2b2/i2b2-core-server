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

import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;

public class TemporalQueryBuilder extends CRCDAO {

	private StringBuffer sqlBuffer = new StringBuffer();
	private StringBuffer ignoredItemBuffer = new StringBuffer();
	private DataSourceLookup dataSourceLookup = null;
	private String queryXML = null;
	private StringBuffer processTimingMessageBuffer = new StringBuffer();
	private Map projectParamMap = null;
	private String processTimingFlag = "";
	private boolean allowLargeTextValueConstrainFlag = true;
	private boolean queryWithoutTempTableFlag = false;
	private boolean isTemporalQuery = false;

	public TemporalQueryBuilder(DataSourceLookup dataSourceLookup,
			String queryXML) {
		this.dataSourceLookup = dataSourceLookup;
		this.queryXML = queryXML;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
	}

	public String getProcessTimingMessage() {
		return this.processTimingMessageBuffer.toString();
	}

	public Map getProjectParamMap() {
		return this.projectParamMap;
	}

	public void setProjectParamMap(Map projectParamMap) {
		this.projectParamMap = projectParamMap;
		if (projectParamMap != null
				&& projectParamMap.get(ParamUtil.PM_ENABLE_PROCESS_TIMING) != null) {
			this.processTimingFlag = (String) projectParamMap
					.get(ParamUtil.PM_ENABLE_PROCESS_TIMING);
		}
	}

	public void setAllowLargeTextValueConstrainFlag(
			boolean allowLargeTextValueConstrainFlag) {
		this.allowLargeTextValueConstrainFlag = allowLargeTextValueConstrainFlag;
	}

	public String getSql() {
		return sqlBuffer.toString();
	}

	public String getIgnoredItemMessage() {
		return ignoredItemBuffer.toString();
	}

	public void startSqlBuild() throws JAXBUtilException, I2B2Exception {
		LogTimingUtil logTimingUtil = new LogTimingUtil();
		logTimingUtil.setStartTime();

		execQuery(this.queryXML, null, 0);

		logTimingUtil.setEndTime();
		if (processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.INFO)
				|| processTimingFlag
						.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) {
			ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(
					this.dataSourceLookup);
			this.processTimingMessageBuffer.append(ptrUtil.buildProcessTiming(
					logTimingUtil, "BUILD SQL", null));
		}
	}

	private String[] execQuery(String requestXML, String itemName, int level)
			throws JAXBUtilException, I2B2Exception {

		TemporalQueryHandler queryTool = null;
		queryTool = new TemporalQueryHandler(dataSourceLookup, requestXML, false);
		queryTool.setProjectParamMap(this.projectParamMap);

		if (this.processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) {
			queryTool.setProcessTimingFlag(this.processTimingFlag);
		}
		
		queryTool.setAllowLargeTextValueConstrainFlag(allowLargeTextValueConstrainFlag);
		queryTool.setQueryWithoutTempTableFlag(queryWithoutTempTableFlag);

		String sql = queryTool.buildSql();
		String maxPanelNum = String.valueOf(queryTool.getMaxPanelNumber());
		this.isTemporalQuery = queryTool.isTemporalQuery();

		ignoredItemBuffer.append(queryTool.getIgnoredItemMessage());
		if (this.processTimingFlag
				.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) {
			processTimingMessageBuffer.append(queryTool
					.getProcessTimingMessage());
		}

		log.debug("generated sql " + sql);
		sqlBuffer.append(sql);

		return new String[] { sql, maxPanelNum };
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
