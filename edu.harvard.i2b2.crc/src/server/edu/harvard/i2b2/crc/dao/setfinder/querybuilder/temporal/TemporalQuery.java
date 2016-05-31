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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.DxTableHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryDefinitionUnWrapUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.TempTableNameMap;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.InvertedConstraintStrategy;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.QueryConstraintStrategy;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryConstraintType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Temporal Query Object
 * 
 * <P> General query object that reads in CRC query requests and converts it into sql that can be run against the database
 * This class and the subsequent classes it depends on duplicate much of functionality already found in querybuilder package, but reorganize the 
 * code in a way that makes it easier to identify sql syntax issues and find the corresponding code snippet that generates it. In addition,
 * temporal comparison functionality was added to this set of classes to enable temporal based queries to run in the CRC.
 * 
 * @author Christopher Herrick
 *
 */
public class TemporalQuery {

	protected final Log log = LogFactory.getLog(getClass());
	private static QueryTimingHandler timingHandler = null;
	
	private TempTableNameMap tempTableNameMap = null; 
	private String queryTiming = "ANY";  
	private DataSourceLookup dsLookup = null;
	private List<TemporalSubQuery> subQueryList = null;
	private HashMap<String, Integer> subQueryMap = null;
	private QueryDefinitionType queryDef = null;
	private String queryId = null;
	private String projectId = null;
	private SecurityType securityType = null;
	private SecurityType userSecurityType = null;
	private Map projectParamMap  = null;
	private boolean allowLargeTextValueConstrainFlag = false;
	private TemporalQueryConstraintMapping constraintMapping = null;
	private int maxPanelIndex = 0;
	private int processingLevel = 1;
	private StringBuffer ignoredItemMessageBuffer = new StringBuffer();
	private TemporalQueryOptions options = null;
	private String lastSubQueryId = null;
	private List<String> preProcessingSql = null;
	private List<String> postProcessingSql = null;
	

	/**
	 * Constructor
	 * 
	 * @param dataSourceLookup 						data source lookup object that contains information about the database and connection used to query against
	 * @param projectParameterMap 					map object that contains project parameters used to configure CRC and query logic 
	 * @param queryXml  							string that contains the query definition xml object received from the query request
	 * @param allowLargeTextValueConstrainFlag		boolean flag that, when set to true, allows text searching against text fields in the database
	 * @throws JAXBUtilException					exception thrown when errors arise from converting string to xml and vice versa
	 * @throws I2B2Exception						exception thrown when i2b2 specific error arises
	 */
	public TemporalQuery(DataSourceLookup dataSourceLookup, Map projectParameterMap, String queryXml, boolean allowLargeTextValueConstrainFlag) throws JAXBUtilException, I2B2Exception{
		this(dataSourceLookup, projectParameterMap, queryXml, allowLargeTextValueConstrainFlag, 1);
	}
	
	/**
	 * Constructor
	 * 
	 * @param dataSourceLookup 						data source lookup object that contains information about the database and connection used to query against
	 * @param projectParameterMap 					map object that contains project parameters used to configure CRC and query logic 
	 * @param queryXml  							string that contains the query definition xml object received from the query request
	 * @param allowLargeTextValueConstrainFlag		boolean flag that, when set to true, allows text searching against text fields in the database
	 * @throws JAXBUtilException					exception thrown when errors arise from converting string to xml and vice versa
	 * @throws I2B2Exception						exception thrown when i2b2 specific error arises
	 */
	public TemporalQuery(DataSourceLookup dataSourceLookup, Map projectParameterMap, String queryXml, boolean allowLargeTextValueConstrainFlag, int processingLevel) throws JAXBUtilException, I2B2Exception{
		dsLookup = dataSourceLookup;
		projectParamMap = projectParameterMap;
		
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String queryConstraintLogic= "";
		try{
			queryConstraintLogic = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinderquery.constraintlogic");
		}catch (I2B2Exception e) {
			// ignore this default will be WITH
		}
	
				
		this.allowLargeTextValueConstrainFlag = allowLargeTextValueConstrainFlag;
		this.processingLevel = processingLevel;
		options = new TemporalQueryOptions();
		if (getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)){
			if (queryConstraintLogic.equalsIgnoreCase("TEMP")||queryConstraintLogic.equalsIgnoreCase("TEMPTABLES"))
				options.setQueryConstraintLogic(QueryConstraintStrategy.TEMP_TABLES);
			else 
				options.setQueryConstraintLogic(QueryConstraintStrategy.WITH_STATEMENT);
		}
		else if (getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)){
			options.setQueryConstraintLogic(QueryConstraintStrategy.WITH_STATEMENT);
		}
		else if (getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)){
			options.setInvertedConstraintLogic(InvertedConstraintStrategy.MINUS_CLAUSE);
		}
		this.preProcessingSql = new ArrayList<String>();
		this.postProcessingSql = new ArrayList<String>();

		parseQueryDefinitionXml(queryXml);
	}
	
	
	public TemporalQuery(DataSourceLookup dataSourceLookup, Map projectParameterMap, QueryDefinitionType queryDefinition, boolean allowLargeTextValueConstrainFlag,
			String queryProjectId, SecurityType userSecurityType, SecurityType querySecurityType) throws JAXBUtilException, I2B2Exception{
		this(dataSourceLookup, projectParameterMap, queryDefinition, allowLargeTextValueConstrainFlag, 1, queryProjectId, userSecurityType, querySecurityType);
	}
	
	public TemporalQuery(DataSourceLookup dataSourceLookup, Map projectParameterMap, QueryDefinitionType queryDefinition, boolean allowLargeTextValueConstrainFlag, int processingLevel, 
			String queryProjectId, SecurityType userSecurityType, SecurityType querySecurityType) throws JAXBUtilException, I2B2Exception{
		dsLookup = dataSourceLookup;
		projectParamMap = projectParameterMap;
		this.allowLargeTextValueConstrainFlag = allowLargeTextValueConstrainFlag;
		this.processingLevel = processingLevel;
		options = new TemporalQueryOptions();
		
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String queryConstraintLogic= "";
		try{
			queryConstraintLogic = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinderquery.constraintlogic");
		}catch (I2B2Exception e) {
			// ignore this default will be WITH
		}
	
		if (getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)){
			//no default options at this time
			if (queryConstraintLogic.equalsIgnoreCase("TEMP")||queryConstraintLogic.equalsIgnoreCase("TEMPTABLES"))
				options.setQueryConstraintLogic(QueryConstraintStrategy.TEMP_TABLES);
			else 
				options.setQueryConstraintLogic(QueryConstraintStrategy.WITH_STATEMENT);
		}
		else if (getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)){
				//no default options at this time
				
			options.setQueryConstraintLogic(QueryConstraintStrategy.WITH_STATEMENT);
		}
		else if (getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)){
			options.setInvertedConstraintLogic(InvertedConstraintStrategy.MINUS_CLAUSE);
		}
		this.queryDef = queryDefinition;
		this.projectId = queryProjectId;
		this.userSecurityType = userSecurityType;
		this.securityType = querySecurityType;
		this.preProcessingSql = new ArrayList<String>();
		this.postProcessingSql = new ArrayList<String>();
		parseQuery();
	}

	
	public void parseQueryDefinitionXml(String queryXml) throws JAXBUtilException, I2B2Exception{
		if (queryXml==null)
			return;
		
		//start by converting string object to query definition object
		QueryDefinitionUnWrapUtil queryDefUnWrapHelper = new QueryDefinitionUnWrapUtil();
		queryDef = queryDefUnWrapHelper
				.getQueryDefinitionType(queryXml);
		
		//get query timing out - this will be used as the default timing for all groups 
		//and panels. Panel timing values will override this value
		queryTiming = queryDef.getQueryTiming();
		
		//extract out project and security information. this will be reused when validating
		//panel items with ontology cell
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(queryXml);
		RequestMessageType request = (RequestMessageType) responseJaxb
				.getValue();
		projectId = request.getMessageHeader().getProjectId();
		userSecurityType = request.getMessageHeader()
				.getSecurity();
		securityType = PMServiceAccountUtil
				.getServiceSecurityType(userSecurityType.getDomain());
		
		parseQuery();
	}
	
	/**
	 * Parse Query
	 * 
	 * <P> Parses out string representation of query xml object into building blocks that will be used to generate sql statement.
	 * In particular, this method establishes the processing order of groups, panels, and items when building the query
	 * 
	 * @param queryXml				string that contains the query definition xml object received from the query request
	 * @throws JAXBUtilException	exception thrown when errors arise from converting string to xml and vice versa
	 * @throws I2B2Exception		exception thrown when i2b2 specific error arises
	 */
	public void parseQuery() throws I2B2Exception {
		//get query timing out - this will be used as the default timing for all groups 
		//and panels. Panel timing values will override this value

		queryTiming = queryDef.getQueryTiming();
		//first step, get the constraints parsed out by groups they reference
		//also parse through the columns that need to be returned by each panel group

		constraintMapping = new TemporalQueryConstraintMapping(queryDef);
		
		HashMap<String, TemporalSubQuery> subQuerySet = new HashMap<String, TemporalSubQuery>();
		
		//second step, treat panel object on the query level as panel group with no temporal component

		if (queryDef.getPanel()!=null&&queryDef.getPanel().size()>0){
			TemporalSubQuery mainQuery = new TemporalSubQuery(this, queryDef.getPanel());
			
			//check to see if the main query is an "everyone" query
			boolean getEveryoneQuery = false;
			if (mainQuery!=null&&mainQuery.getPanelCount()==1){
				TemporalPanel mainPanel = mainQuery.getPanelList().get(0);
				if (mainPanel!=null &&
						mainPanel.isPanelInverted() &&
						(mainPanel.getItemList()==null||mainPanel.getItemList().size()==0)){
					getEveryoneQuery = true;
				}
			}
			

			//if there are no subqueries or the main query is not an "everyone" query, include
			//the main query as a special subquery
			if (queryDef.getSubquery()==null ||
					queryDef.getSubquery().size()==0 ||
					!getEveryoneQuery){
				subQuerySet.put("**default**", mainQuery);						
			}
		}

		//third step, check for query objects and create corresponding temporal query panel groups
		if (queryDef.getSubquery()!=null&&queryDef.getSubquery().size()>0){
			for (QueryDefinitionType query : queryDef.getSubquery()){
				HashMap<String, List<QueryConstraintType>> constraints = constraintMapping.getConstraintsForQuery(query.getQueryId());
				EnumSet<TemporalSubQuery.TemporalQueryReturnColumns> returnColumns = constraintMapping.getReturnColumnsForQuery(query.getQueryId());
				TemporalSubQuery subQuery = new TemporalSubQuery(this, query, constraints, returnColumns);
				subQuerySet.put(query.getQueryId(), subQuery);						
			}
		}
		
		//last step, group set object established the order of the groups.  now copy objects
		//into straight list for easier processing
		subQueryList = new ArrayList<TemporalSubQuery>();
		subQueryMap = new HashMap<String, Integer>();
		int index = 0;
		for(String eventId : constraintMapping.getOrderedQueryList()){
			TemporalSubQuery subQuery = subQuerySet.get(eventId);
			if (subQuery!=null){
				subQueryList.add(subQuery);
				subQueryMap.put(eventId, index);
				index++;
				subQuerySet.remove(eventId);
			}
		}
		
		if (subQuerySet.size()>0){
			for (TemporalSubQuery grp : subQuerySet.values()){
				subQueryList.add(grp);
				subQueryMap.put(grp.getQueryId(), index);
				index++;
			}
		}
		
	}
	
	protected QueryDefinitionType searchForSubQuery(String subQueryId){
		if (subQueryId==null||subQueryId.trim().length()==0)
			return null;
		
		String strippedId = "";
		if (subQueryId.startsWith("masterid:")){
			strippedId = subQueryId.replace("masterid:", "");			
		}
		if (this.queryDef!=null&&this.queryDef.getSubquery()!=null){
			for (QueryDefinitionType query : queryDef.getSubquery()){
				if (query.getQueryId()!=null&&query.getQueryId().trim().length()>0&&query.getQueryId().equalsIgnoreCase(subQueryId)){
					return query;
				}
				else if (strippedId!=null&&strippedId.trim().length()>0&&strippedId.equalsIgnoreCase(subQueryId))
					return query;
				
			}
		}
		return null;
	}
	
	/**
	 * Build Sql
	 * 
	 * <P> Main call that converts the query object into a sql statement that can be run against the database
	 * 
	 * @return String sql statement generated from query object
	 * @throws I2B2DAOException exception generated when encountering an i2b2 specific database error
	 */
	public String buildSql() throws I2B2DAOException {
		StringBuffer querySqlBuffer = new StringBuffer();		
		
		// iterator through the ordered panel groups, generating a sql
		// statement for each

		for (TemporalSubQuery subQuery : subQueryList){
			if (subQuery!=null){
				querySqlBuffer.append(subQuery.buildSql());	
				lastSubQueryId = subQuery.getSubQueryId();
				maxPanelIndex = subQuery.getEndPanelIndex();				
			}
		}
		
		// finally, add in insert into dx table that will get passed back to 
		// calling class
			
		String dxTempTableSql = "";
		if (subQueryList.size()>1){
			dxTempTableSql = buildDxInsertSqlFromMaster(returnEncounterNum(), returnInstanceNum(), lastSubQueryId, 1);			
		}
		else {
			dxTempTableSql = buildDxInsertSqlFromTemp(returnEncounterNum(), returnInstanceNum(), maxPanelIndex);
		}
		querySqlBuffer.append(dxTempTableSql);
		
		
		StringBuffer sqlBuffer = new StringBuffer();
		if (this.preProcessingSql!=null&&this.preProcessingSql.size()>0){
			for(String sql : this.preProcessingSql){
				sqlBuffer.append(sql);
				sqlBuffer.append(getSqlDelimiter());
			}
		}
		sqlBuffer.append(querySqlBuffer.toString());
		if (this.postProcessingSql!=null&&this.postProcessingSql.size()>0){
			for(String sql : this.postProcessingSql){
				sqlBuffer.append(getSqlDelimiter());
				sqlBuffer.append(sql);
			}
		}
		
		return sqlBuffer.toString();		
	}
	
	
	public String buildDxInsertSqlFromTemp(boolean encounterNumFlag, boolean instanceNumFlag, int maxPanelNum) {
		StringBuilder dxInsertSql = new StringBuilder();

		
			String selectEncounterNum = " ", selectPatientNum = " patient_num ";
			if (encounterNumFlag) {
				selectEncounterNum = " , encounter_num ";
			}

			dxInsertSql.append(" insert into " + this.getDxTempTableName() + " ( " + selectPatientNum
					+ selectEncounterNum + " ) select * from ("
					+ " select distinct " + selectPatientNum
					+ selectEncounterNum + "from " + this.getTempTableName()
					+ " where panel_count = " + maxPanelNum
					+ " ) q");
		
		return dxInsertSql.toString();
	}
	
	public String buildDxInsertSqlFromMaster(boolean encounterNumFlag, boolean instanceNumFlag, String subQueryId, int levelNo) {
		StringBuilder dxInsertSql = new StringBuilder();

		
			String selectEncounterNum = " ", selectPatientNum = " patient_num ";
			if (encounterNumFlag) {
				selectEncounterNum = " , encounter_num ";
			}

			if (useSqlServerTempTables()){
				dxInsertSql.append(" insert into " + this.getDxTempTableName() + " ( " + selectPatientNum
						+ selectEncounterNum + " ) select * from ("
						+ " select distinct " + selectPatientNum
						+ selectEncounterNum + "from #m" + subQueryId
						+ " where level_no = " + String.valueOf(levelNo));				
			}
			else {
				dxInsertSql.append(" insert into " + this.getDxTempTableName() + " ( " + selectPatientNum
						+ selectEncounterNum + " ) select * from ("
						+ " select distinct " + selectPatientNum
						+ selectEncounterNum + "from " + this.getMasterTempTableName()
						+ " where level_no = " + String.valueOf(levelNo)
						+ " and master_id = '" + subQueryId + "'");
			}
			
			dxInsertSql.append(" ) q");
		
		return dxInsertSql.toString();
	}
	

	/**
	 * Get Project Id
	 * 
	 * @return string that represents the project id for the query that was passed in
	 */
	protected String getProjectId() {
		return projectId;
	}

	/**
	 * Get Security Type
	 * 
	 * @return security type for the query that was passed in
	 */
	protected SecurityType getSecurityType() {
		return securityType;
	}
	
	/**
	 * Get Security Type
	 * 
	 * @return security type for the query that was passed in
	 */
	protected SecurityType getRequestorSecurityType() {
		return userSecurityType;
	}
	
	/**
	 * Get Panel Group Index
	 * 
	 * @param grp reference to temporal panel group to test
	 * @return int that corresponds to the index of this panel group occurs the group list
	 */
	public int getSubQueryIndex(TemporalSubQuery grp){
		return subQueryList.indexOf(grp);
	}
	
	public int getSubQueryIndex(String subQueryId){
		return subQueryMap.get(subQueryId);
	}
	
	/**
	 * Is First Group
	 * 
	 * Test to check if passed in temporal panel group is the first panel group for this query
	 * 
	 * @param subQuery reference to a temporal panel group to test
	 * @return true or false depending on if passed in panel group is the first group for this query
	 */
	public boolean isFirstSubQuery(TemporalSubQuery subQuery){
		return (getSubQueryIndex(subQuery)==0);
	}

	/**
	 * Is Last Group
	 * 
	 * Test to check if passed in temporal panel group is the last panel group for this query
	 * 
	 * @param subQuery reference to a temporal panel group to test
	 * @return true or false depending on if passed in panel group is the last group for this query
	 */
	public boolean isLastSubQuery(TemporalSubQuery subQuery){
		return (getSubQueryIndex(subQuery)==(subQueryList.size()-1));
	}

	/**
	 * Get Query Timing
	 * 
	 * Get the default timing passed in by the query object
	 * 
	 * @return string containing the query timing constant
	 */
	public String getQueryTiming() {
		return queryTiming;
	}

	/**
	 * Get Data Source Lookup
	 * 
	 * Returns the data source lookup object passed in in the constructor
	 * 
	 * @return data source lookup object passed in the constructor
	 */
	protected DataSourceLookup getDataSourceLookup() {
		return dsLookup;
	}
	
	
	/**
	 * Get Database Schema
	 * 
	 * Returns the name of the schema used to reference the correct table names in the sql syntax
	 * 
	 * @return string that contains the name of the schema this query should run under
	 */
	public String getDatabaseSchema() {
		String dbSchemaName = dsLookup.getFullSchema();
		if (dbSchemaName != null && dbSchemaName.endsWith(".")) { 
			return dbSchemaName.trim();
		}
		else if (dbSchemaName != null) { 
			return dbSchemaName.trim() + ".";
		}
		return dbSchemaName;		
	}
	
	/**
	 * Get Server Type
	 * 
	 * Returns the type of database server CRC is running against
	 * 
	 * @return string containing the type of database CRC is running against (Sql Server or Oracle)
	 */
	protected String getServerType(){
		return dsLookup.getServerType();
	}

	/**
	 * Get Project Parameter Map
	 * 
	 * Returns the map that was passed in in the constructor that contains the parameters used for this project
	 * 
	 * @return map object that contains the project parameters
	 */
	protected Map getProjectParameterMap(){
		return this.projectParamMap;
	}
	
	/**
	 * Get Timing Handler
	 * 
	 * @return query timing handler object that tests timing text from query for various states
	 */
	protected QueryTimingHandler getTimingHandler(){
		if (timingHandler==null)
			timingHandler = new QueryTimingHandler();
		return timingHandler;
	}
	
	/**
	 * Get Temp Table Name
	 * 
	 * Return the default name of the temproary table in the database. The temporary table is used to store the results of individual panel items and the joins
	 * of panel to panel items
	 * 
	 * @return string containing the name of the default temporary table name in the database
	 */
	protected String getTempTableName(){
		if (this.tempTableNameMap==null)
			this.tempTableNameMap = new TempTableNameMap(this.getServerType());
		String tableName = "";
		if (getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)){
			tableName = getDatabaseSchema();
		}
		tableName += tempTableNameMap.getTempTableName();

		return tableName;
	}
	
	/**
	 * Get DX Temp Table Name
	 * 
	 * Return the default name of the dx temporary table in the database.  The dx table is used to return the final results of the query from the database to the
	 * CRC cell
	 * 
	 * @return string containing the default name of the dx return table in database
	 */
	protected String getDxTempTableName(){
		if (this.tempTableNameMap==null)
			this.tempTableNameMap = new TempTableNameMap(this.getServerType());
		String tableName = "";
		if (getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)){
			tableName = getDatabaseSchema();
		}
		tableName += tempTableNameMap.getTempDxTableName();

		return tableName;
	}
	
	/**
	 * Get Master Temp Table Name
	 * 
	 * Return default name of the master table in the database. The master table is used to temporarily store results from embedded query items 
	 * and panel group constraints
	 * 
	 * @return string containing the default name used for the "master" table in the database
	 */
	protected String getMasterTempTableName(){
		if (this.tempTableNameMap==null)
			this.tempTableNameMap = new TempTableNameMap(this.getServerType());
		String tableName = "";
		if (getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)){
			tableName = getDatabaseSchema();
		}
		tableName += tempTableNameMap.getTempMasterTable();

		return tableName;
	}
	
	/**
	 * Allow Large Text Value Constrian Flag
	 * 
	 * @return true if text querying against large text fields is allowed, else false
	 */
	protected boolean allowLargeTextValueConstrainFlag(){
		return this.allowLargeTextValueConstrainFlag;
	}
	
	/**
	 * Return Encounter Num
	 * 
	 * @return true if query needs to return encounter information in final results, else false
	 */
	protected boolean returnEncounterNum(){
		if (getTimingHandler().isSameVisit(queryTiming))
			return true;
		else
			return false;
	}
	
	/**
	 * Return Instance Num
	 * 
	 * @return true if query needs to return instance information is the final results, else false
	 */
	protected boolean returnInstanceNum(){
		if (getTimingHandler().isSameInstanceNum(queryTiming))
			return true;
		else
			return false;
	}
	
	/**
	 * Get Panel Group Count
	 * 
	 * @return int - the number of panel groups contained in this query
	 */
	protected int getSubQueryCount(){
		return subQueryList.size();
	}

	/**
	 * Generate Unique Id
	 * 
	 * Returns a string with a guaranteed unique value. This method is used specifically to uniquely identify queries and 
	 * panel groups within the context of the query
	 * 
	 * @return string with a guaranteed unique value
	 */
	protected String generateUniqueId(){
		UUID uniqueKey = UUID.randomUUID();   
		String hexNum =  uniqueKey.toString().replace("-", "");
		BigInteger big = new BigInteger(hexNum, 16);
		return big.toString(36);
	}
	
	/**
	 * Get Query Id
	 * 
	 * @return string containing the unique id of the query that is being run
	 */
	protected String getQueryId(){
		if (queryId==null)
			queryId = generateUniqueId();
		return queryId;
	}
	
	public int getMaxPanelIndex(){
		return this.maxPanelIndex;
	}

	/**
	 * @return the processingLevel
	 */
	public int getProcessingLevel() {
		return processingLevel;
	}
	
	public TemporalQueryOptions getQueryOptions(){
		return options;
	}
	
	/**
	 * @return the ignoredItemMessageBuffer
	 */
	public StringBuffer getIgnoredItemMessageBuffer() {
		return ignoredItemMessageBuffer;
	}

	public void addIgnoredMessage(String errorMessage) {
		this.ignoredItemMessageBuffer.append(errorMessage + "\n");
	}

	/**
	 * @return the lastSubQueryId
	 */
	protected String getLastProcessedSubQueryId() {
		return lastSubQueryId;
	}

	protected void addPreProcessingSql(String sql){
		if (sql!=null&&sql.trim().length()>0)
			this.preProcessingSql.add(sql);
	}

	protected void addPostProcessingSql(String sql){
		if (sql!=null&&sql.trim().length()>0)
			this.postProcessingSql.add(sql);
	}
	
	public String getSqlDelimiter() {
		return "\n<*>\n";
	}
	

	/**
	 * Build Temp Table Check Drop
	 * 
	 * Create a Sql Server specific statement to check for the existence of a temporary table and, 
	 * if the table exists, drop it
	 * 
	 * @param tempTableName String name of the temporary table
	 * @return String sql statement that checks for temporary table and drops it
	 */
	protected String buildTempTableCheckDrop(String tempTableName) {
		if (tempTableName == null)
			return "";
		else if (!tempTableName.startsWith("#"))
			tempTableName = "#" + tempTableName;

		return "if (object_id('tempdb.." + tempTableName + "') is not null) \n"
				+ "begin \n" + "drop table " + tempTableName + " \n" + "end";
	}
	
	protected boolean useSqlServerTempTables(){
		return ((this.getQueryOptions().getQueryConstraintLogic()==QueryConstraintStrategy.TEMP_TABLES) &&
				(this.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)));
	}

}


