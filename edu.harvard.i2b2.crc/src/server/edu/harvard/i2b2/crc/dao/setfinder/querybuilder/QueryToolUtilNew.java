package edu.harvard.i2b2.crc. dao.setfinder.querybuilder;
 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;

public class QueryToolUtilNew extends CRCDAO {

	DataSourceLookup dataSourceLookup = null;
	String queryXML = null;
	String noLockSqlServer = " ";
	String tempTableName = " ", tempDxTableName = " ";
	StringBuffer ignoredItemMessageBuffer = new StringBuffer();
	boolean encounterSetOutputFlag = false;
	int maxPanelNum = 0;
	ProcessTimingReportUtil processTimingUtil = null;
	String processTimingFlag = ProcessTimingReportUtil.NONE;
	Map projectParamMap = null;
	StringBuffer processTimingStr = new StringBuffer();
	boolean allowLargeTextValueConstrainFlag = true;


	public QueryToolUtilNew(DataSourceLookup dataSourceLookup, String queryXML,
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



	public String getSetfinderSqlForQueryDefinition() throws JAXBUtilException, I2B2Exception {
		StringBuffer setfinderSql = new StringBuffer();
		QueryDefinitionUnWrapUtil queryDefUnWrapHelper = new QueryDefinitionUnWrapUtil();

		QueryDefinitionType queryDefType = queryDefUnWrapHelper
				.getQueryDefinitionType(this.queryXML);



		boolean invertFlag = isInvert(queryDefType);
		boolean onlyInvertFlag = isOnlyInvert(queryDefType);
		if (onlyInvertFlag) {
			BuildInvertSql buildInvertSql = new BuildInvertSql(dataSourceLookup,  this
					.getTempTableName());
			setfinderSql.append(buildInvertSql.buildInsertInvertSql(queryDefType.getQueryTiming()));
		}
		// sort the panel
		/*
		CallOntologyUtil ontologyUtil = null;
		try {
			ontologyUtil = new CallOntologyUtil(queryXML);
		} catch (JAXBUtilException e) {
			throw new I2B2DAOException("Error in reading the request xml "
					+ queryXML + StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			throw new I2B2DAOException("Error in reading the request xml "
					+ queryXML + StackTraceUtil.getStackTrace(e));
		}
		 */

		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(queryXML);
		RequestMessageType request = (RequestMessageType) responseJaxb
				.getValue();
		String projectId = request.getMessageHeader().getProjectId();
		SecurityType tempSecurityType = request.getMessageHeader()
				.getSecurity();
		SecurityType securityType = PMServiceAccountUtil
				.getServiceSecurityType(tempSecurityType.getDomain());

		//if query timing is null, then take the timing from panel timing 
		String queryTimingVal = queryDefType.getQueryTiming();
		if (queryDefType.getQueryTiming() == null) {
			CalulateQueryTiming calculateTiming = new CalulateQueryTiming(); 
			String calcQueryTiming = calculateTiming.getQueryTiming(queryDefType.getPanel());
			queryDefType.setQueryTiming(calcQueryTiming);
			queryTimingVal = calcQueryTiming;
		}

		// group panels by the timing(query_timing & panel_timing)
		GroupPanelByTiming grpPanelTiming = new GroupPanelByTiming();
		Map<String, List<PanelType>> grpPanelByTimingMap = grpPanelTiming
				.groupPanelByTiming(queryDefType.getPanel(), queryDefType
						.getQueryTiming());



		Map<String, String> possibleMap  = new HashMap<String,String>();
		possibleMap.put("ANY", "SAME:SAMEVISIT:SAMEINSTANCENUM");
		possibleMap.put("SAMEVISIT", "SAMEINSTANCENUM");
		possibleMap.put("SAME", "SAMEINSTANCENUM");
		possibleMap.put("SAMEINSTANCENUM", "");

		String panelTiming = "";
		String validPanelTiming = possibleMap.get(queryTimingVal);
		String[] valueList = validPanelTiming.split(":");
		List<String> valList = Arrays.asList(valueList); 
		for (Iterator<String> i = grpPanelByTimingMap.keySet().iterator();i.hasNext();) { 
			panelTiming = i.next();
			List<PanelType> pl = (List<PanelType>)grpPanelByTimingMap.get(panelTiming);
			if (pl.size() == 0) { 
				continue;
			}

			log.debug("Checking for valid query timing [" + queryTimingVal + "] with panel timing [" + panelTiming + "]");


			if (valList.indexOf(panelTiming)>-1) { 
				throw new I2B2DAOException("Query timing ["+ queryTimingVal +"] and panel timing [" + panelTiming +"] is not valid");
			}
		}


		QueryTimingHandler queryTiming = new QueryTimingHandler();
		boolean encounterFlag = queryTiming.isSameVisit(queryDefType);
		boolean instanceNumFlag = queryTiming.isSameInstanceNum(queryDefType);

		String[] timingOrder = new String[] {
				QueryTimingHandler.SAMEINSTANCENUM,
				QueryTimingHandler.SAMEVISIT, QueryTimingHandler.ANY };
		List<PanelType> groupedPanelList = null;


		boolean firstPanelFlag = true;

		Map<String, List<PanelType>>  invertGroupPanelByTimingMap =   grpPanelTiming.filterByExcludeFlag(grpPanelByTimingMap,true);
		Map<String, List<PanelType>>  nonInvertGroupPanelByTimingMap = grpPanelTiming.filterByExcludeFlag(grpPanelByTimingMap,false);
		ArrayList<Map<String,List<PanelType>>> t = new ArrayList<Map<String, List<PanelType>>>();
		if (nonInvertGroupPanelByTimingMap.size() > 0) { 
			t.add(nonInvertGroupPanelByTimingMap);
		}
		t.add(invertGroupPanelByTimingMap);

		String firstItemJoinTiming = calculateFirstItemJoin(timingOrder,invertGroupPanelByTimingMap, nonInvertGroupPanelByTimingMap);

		for (Map<String, List<PanelType>> panelGroupByInvertList  : t) { 
			for (int k = 0; k < timingOrder.length; k++) {
				groupedPanelList = panelGroupByInvertList.get(timingOrder[k]);


				if (groupedPanelList != null && groupedPanelList.size()>0) {
					if (timingOrder[k]
							.equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)) {
						instanceNumFlag = true;
						encounterFlag = false;
					} else if (timingOrder[k]
							.equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)) {
						encounterFlag = true;
						instanceNumFlag = false;
					} else if (timingOrder[k]
							.equalsIgnoreCase(QueryTimingHandler.ANY)) {
						encounterFlag = false;
						instanceNumFlag = false;
					}
					log.debug("setfinder panel size  for ["+  timingOrder[k] + "] timins is " + groupedPanelList.size());
					Map<Integer, String> panelSql = generateSqlForGroupedPanelList(
							groupedPanelList, queryDefType, securityType, projectId,
							encounterFlag, instanceNumFlag, maxPanelNum,
							firstPanelFlag, invertFlag, onlyInvertFlag,firstItemJoinTiming);
					firstPanelFlag = false;
					for (Integer key : panelSql.keySet()) {
						setfinderSql.append(panelSql.get(key));
						// setfinderSql.append(getSqlDelimitor());
						// panelSql.remove(key);
						// maxPanelNum = key;
					}
					maxPanelNum = maxPanelNum + groupedPanelList.size();
					panelSql.clear();

				}
			}
		}


		// call build dx temp table insert sql
		DxTableHandler dxTableHandler = new DxTableHandler(this
				.getDbSchemaName(), this.getTempDxTableName(), this
				.getTempTableName());
		String dxTempTableSql = dxTableHandler.buildDxInsertSql(
				encounterFlag, instanceNumFlag, maxPanelNum);

		setfinderSql.append(dxTempTableSql);
		log.debug(setfinderSql.toString());
		return setfinderSql.toString();

	}

	private Map<Integer, String> generateSqlForGroupedPanelList(
			List<PanelType> panelList, QueryDefinitionType queryDefType,
			SecurityType securityType, String projectId, boolean encounterFlag,
			boolean instanceNumFlag, int panelCount, boolean firstPanelFlag, boolean invertQueryFlag, boolean invertOnlyQueryFlag, 
			String firstItemJoinTiming)
					throws I2B2Exception {
		SortPanel sortPanel = new SortPanel();
		LogTimingUtil timingUtil = new LogTimingUtil();
		timingUtil.setStartTime();
		List<PanelType> sortedPanelList = sortPanel.sortedPanelList(panelList,
				securityType, projectId);
		timingUtil.setEndTime();
		//build the log xml and add it to the  string variable. 
		this.processTimingStr.append(this.processTimingUtil.buildProcessTiming(timingUtil, "SORT PANEL", null));

		BuildTempTableSql tempTableSql = new BuildTempTableSql(
				dataSourceLookup, queryXML);
		tempTableSql.setProjectParamMap(projectParamMap);
		tempTableSql.setAllowLargeTextValueConstrainFlag(this.allowLargeTextValueConstrainFlag);

		// build sql for each panel
		Map<Integer, String> panelSql = tempTableSql.buildTempTableSql(
				sortedPanelList, encounterFlag, instanceNumFlag, queryDefType
				.getQueryTiming(), panelCount, firstPanelFlag, invertQueryFlag, invertOnlyQueryFlag,firstItemJoinTiming);
		this.processTimingStr.append("\n");
		this.processTimingStr.append(tempTableSql.getProcessTimingXml());

		// store ignored item message to buffer
		this.ignoredItemMessageBuffer.append(tempTableSql
				.getIgnoredItemMessage());

		return panelSql;

	}

	private boolean isInvert(QueryDefinitionType queryDefType) {
		List<PanelType> panelList = queryDefType.getPanel();

		for (PanelType panelType : panelList) {
			if (panelType.getInvert() == 1) {
				return true;
			}
		}
		return false;
	}

	private boolean isOnlyInvert(QueryDefinitionType queryDefType) {
		List<PanelType> panelList = queryDefType.getPanel();

		for (PanelType panelType : panelList) {
			if (panelType.getInvert() != 1) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Return the ignored item list
	 * 
	 * @return
	 */
	public String getIgnoredItemMessage() {
		if (this.ignoredItemMessageBuffer != null
				&& this.ignoredItemMessageBuffer.length() > 0) {
			return "Missing Concept in Ontology Cell : \n"
					+ this.ignoredItemMessageBuffer.toString();
		} else {
			return "";
		}

	}

	private String getTempTableName() {

		return this.tempTableName;
	}

	private String getTempDxTableName() {
		return this.tempDxTableName;
	}

	private String calculateFirstItemJoin(String[] timingOrder,Map<String, List<PanelType>> invertGroupPanelByTimingMap,Map<String, List<PanelType>> nonInvertGroupPanelByTimingMap) {
		String invertTiming = "ANY", nonInvertTiming = "ANY", firstItemJoinTiming = "ANY"; 
		for (int i=0;i<timingOrder.length;i++) { 
			if (invertGroupPanelByTimingMap.get(timingOrder[i]) != null) { 
				invertTiming = timingOrder[i];
				break;
			}
		}
		for (int i=0;i<timingOrder.length;i++) { 
			if (nonInvertGroupPanelByTimingMap.get(timingOrder[i]) != null) { 
				nonInvertTiming = timingOrder[i];
				break;
			}
		}
		if (invertTiming.equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)) { 
			if (nonInvertTiming.equalsIgnoreCase(QueryTimingHandler.ANY) ||nonInvertTiming.equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)) { 
				firstItemJoinTiming = QueryTimingHandler.SAMEINSTANCENUM;
			} 
		}
		if (invertTiming.equalsIgnoreCase(QueryTimingHandler.SAMEVISIT)) { 
			if (nonInvertTiming.equalsIgnoreCase(QueryTimingHandler.ANY)) {
				firstItemJoinTiming = QueryTimingHandler.SAMEVISIT;
			}
		}

		return firstItemJoinTiming;
	}

}
