package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;

public class RecursiveBuild extends CRCDAO {

	private static String testFileDir = null;
	private StringBuffer sqlBuffer =  new StringBuffer();
	private StringBuffer ignoredItemBuffer =  new StringBuffer();
	private ArrayList<String> masterIdLIFO = new ArrayList<String>();
	private DataSourceLookup dataSourceLookup = null; 
	private String queryXML = null; 
	private boolean encounterSetOutputFlag = false;
	private TempTableNameMap tempTableNameMap = null;
	private StringBuffer processTimingMessageBuffer = new StringBuffer();
	private Map projectParamMap = null;
	private String processTimingFlag = "";
	private boolean allowLargeTextValueConstrainFlag = true;
	
	
	
	
	public RecursiveBuild(DataSourceLookup dataSourceLookup, String queryXML,
			boolean encounterSetOutputFlag) {
		this.dataSourceLookup = dataSourceLookup;
		this.queryXML = queryXML;
		this.encounterSetOutputFlag = encounterSetOutputFlag;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		tempTableNameMap = new TempTableNameMap(dataSourceLookup.getServerType());
	}

	
	
	
	public String getProcessTimingMessage() { 
		return this.processTimingMessageBuffer.toString();
	}
	
	public void setProjectParamMap(Map projectParamMap) { 
		this.projectParamMap = projectParamMap;
		if (projectParamMap != null && projectParamMap.get(ParamUtil.PM_ENABLE_PROCESS_TIMING) != null) {
			this.processTimingFlag = (String)projectParamMap.get(ParamUtil.PM_ENABLE_PROCESS_TIMING);
		}
	}
	
	public void setAllowLargeTextValueConstrainFlag(boolean allowLargeTextValueConstrainFlag)  { 
		this.allowLargeTextValueConstrainFlag = allowLargeTextValueConstrainFlag;
	}
	
	
	public Map getProjectParamMap() { 
		return this.projectParamMap;
	}
	
	
	
	private QueryDefinitionType  getQueryDefinitionType(String requestString) throws I2B2DAOException { 
		QueryDefinitionRequestType queryDefReqType = null;
		JAXBElement responseJaxb;
		try {
			responseJaxb = CRCJAXBUtil.getJAXBUtil()
			.unMashallFromString(requestString);
		
		RequestMessageType r = (RequestMessageType) responseJaxb.getValue();
		BodyType bodyType = r.getMessageBody();
		// 	get body and search for analysis definition
		JAXBUnWrapHelper unWraphHelper = new JAXBUnWrapHelper();
		
		
			queryDefReqType = (QueryDefinitionRequestType) unWraphHelper
			.getObjectByClass(bodyType.getAny(),
					QueryDefinitionRequestType.class);
		} catch (JAXBUtilException e) {
			throw new I2B2DAOException(e.getMessage());
		}
		
		return queryDefReqType.getQueryDefinition();
	}
	
	public String getSql() { 
		return sqlBuffer.toString();
	}
	
	public String getIgnoredItemMessage() { 
		return ignoredItemBuffer.toString();
	}
	
	public void startSqlBuild() throws JAXBUtilException, I2B2Exception  { 
		LogTimingUtil logTimingUtil = new LogTimingUtil();
		logTimingUtil.setStartTime();
		execQuery(this.queryXML,null,0);
		
		logTimingUtil.setEndTime();
		if (processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.INFO) || processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG) ) {
			ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(this.dataSourceLookup);
			this.processTimingMessageBuffer.append(ptrUtil.buildProcessTiming(logTimingUtil, "BUILD SQL", null));
		 }
	}
	
	private String[] execQuery(String requestXML, String itemName, int level) throws JAXBUtilException, I2B2Exception {
		String returnTempTableName = "";
		//ExecSql execSql = new ExecSql(conn);
		QueryToolUtilNew queryTool = null;
		
		QueryDefinitionType queryDef = getQueryDefinitionType(requestXML); 
		String queryTiming = queryDef.getQueryTiming();
		List<PanelType> panelList = queryDef.getPanel();
		for (PanelType singlePanel : panelList) { 
			List<ItemType> itemList = singlePanel.getItem();
			for (ItemType singleItem : itemList) { 
				if (singleItem.getItemKey().toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_MASTERID)) {
					
					if ((singlePanel.getPanelDateTo() != null) || (singlePanel.getPanelDateFrom() != null))
						throw new I2B2DAOException("Date Contraints is not supported with Query in Query");
					log.debug("In RecursiveBuild, total occurrences: " + singlePanel.getTotalItemOccurrences().getValue());
					if (singlePanel.getTotalItemOccurrences().getValue() > 1)
						throw new I2B2DAOException("Total Occurances greater than 0 is not supported with Query in Query.");
				
					String requestXml = getQueryDefinitionRequestXml(singleItem.getItemKey());
					String[] sql = execQuery(requestXml,singleItem.getItemKey(), level+1);
					QueryDefinitionType masterQueryDef = getQueryDefinitionType(requestXml);
					String masterQueryTiming = masterQueryDef.getQueryTiming();
					//copy the dx data to master temp table
					//execSql.copyDxToMaster(singleItem.getItemKey(), false, false);
					//System.out.println("insert into master_temp(m");
					if (queryTiming == null) { 
						CalulateQueryTiming calculateTiming = new CalulateQueryTiming(); 
						queryTiming = calculateTiming.getQueryTiming(queryDef.getPanel());
					}
					sqlBuffer.append("\n<*>\n");
					sqlBuffer.append(copyDxToMaster(singleItem.getItemKey(), queryTiming, masterQueryTiming, false, false,sql[1],level) ); 
					sqlBuffer.append("\n<*>\n");
					//delete the dx temp table
					//execSql.deleteDx();
					sqlBuffer.append(deleteTempTable());
					sqlBuffer.append("\n<*>\n");
					masterIdLIFO.add(singleItem.getItemKey());
				}
			}
		}
		
		
		//build sql
		queryTool = new QueryToolUtilNew(dataSourceLookup,  requestXML,
				 false); 
		queryTool.setProjectParamMap(this.projectParamMap);
		
		if (this.processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) {
			queryTool.setProcessTimingFlag(this.processTimingFlag);
		}
		queryTool.setAllowLargeTextValueConstrainFlag(allowLargeTextValueConstrainFlag);
		
		
		String sql = "";
		String maxPanelNum = "";
		//if (itemName != null) {
		 sql = queryTool.getSetfinderSqlForQueryDefinition();
		 maxPanelNum = String.valueOf(queryTool.getMaxPanelNumber());
		
		ignoredItemBuffer.append(queryTool.getIgnoredItemMessage());
		if (this.processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) {
			processTimingMessageBuffer.append(queryTool.getProcessTimingMessage()) ; 
		}
		
		//execute sql
		log.debug("generated sql " + sql);
		sqlBuffer.append(sql);
	//	}
		
		
		if (itemName != null && itemName.startsWith("masterid")) {
			
			for(Iterator<String> i = masterIdLIFO.listIterator();i.hasNext();) { 
				log.debug("master id to delete " + i.next());
				
			}
			
			int j=0;
			for(Iterator<String> i = masterIdLIFO.listIterator();i.hasNext();j++) {
				
				sqlBuffer.append(deleteTempMaster( masterIdLIFO.get(j),level)); 
				masterIdLIFO.remove(j);
			}
			
		}
		return new String[] { sql, maxPanelNum};
	}
	
	public String getQueryDefinitionRequestXml(String itemKey) throws I2B2DAOException { 

		
			DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup.getDomainId(), dataSourceLookup.getProjectPath(),
					dataSourceLookup.getProjectPath());
			SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory()
					.getSetFinderDAOFactory();
			
		//get master id
		String masterId = itemKey.substring(9);	
		//get query definition	
		QtQueryMaster queryMaster = sfDaoFactory.getQueryMasterDAO().getQueryDefinition(masterId);
		return queryMaster.getI2b2RequestXml();
	}
	
	
	
	public String copyDxToMaster(String masterId, String queryTiming, String masterQueryTiming, boolean selectEncounterFlag, boolean selectInstanceFlag,
			   String maxPanelNum, int level)   {
		String selectFields = " ";
		String joinTableName = tempTableNameMap.getTempTableName();
		String joinColumnName = "patient_num";
		if (selectInstanceFlag) {
			selectFields = " encounter_num, instance_num, patient_num, concept_cd, start_date, provider_id, ";
		} else if (selectEncounterFlag) { 
			selectFields = "encounter_num, patient_num, ";
		} else { 
			selectFields = " patient_num, ";
		}
		
		if (queryTiming != null && masterQueryTiming != null) { 
			if (!queryTiming.equalsIgnoreCase(masterQueryTiming)) {
				QueryTimingHandler queryTimingHandler = new QueryTimingHandler();
				if (queryTimingHandler.isSameVisit(queryTiming) && queryTimingHandler.isAny(masterQueryTiming)) { 
					joinTableName = "visit_dimension";
					joinColumnName = "patient_num";
					selectFields = "encounter_num, patient_num, ";
				} else if (queryTimingHandler.isSameInstanceNum(queryTiming) && queryTimingHandler.isAny(masterQueryTiming)) { 
					joinTableName = "observation_fact";
					joinColumnName = "patient_num";
					selectFields = " encounter_num, instance_num, patient_num, concept_cd, start_date, provider_id,  ";
				}  else if (queryTimingHandler.isSameInstanceNum(queryTiming) && queryTimingHandler.isSameVisit(masterQueryTiming)) { 
					joinTableName = "observation_fact";
					joinColumnName = "encounter_num";
					selectFields = " encounter_num, instance_num, patient_num, concept_cd, start_date, provider_id, ";
				}
			}
		}
		//return " insert into " + this.getDbSchemaName() + tempTableNameMap.getTempMasterTable() + "(master_id, " + selectFields + " level_no) " +
		//		"select '" + masterId + "', " + selectFields +   level + "  from " + this.getDbSchemaName() + tempTableNameMap.getTempTableName() +" where panel_count = " + maxPanelNum;
		
		return " insert into " + this.getDbSchemaName() + tempTableNameMap.getTempMasterTable() + "(master_id, " + selectFields + " level_no) " +
				"select '" + masterId + "', " + selectFields +   level + "  from " + this.getDbSchemaName() + joinTableName + " where " + joinColumnName + " IN ( " + 
				"select " + joinColumnName  + " from " + this.getDbSchemaName() + tempTableNameMap.getTempTableName() +" where panel_count = " + maxPanelNum + " ) ";	
		
		
	}
	
	public String  deleteTempTable()  { 
	
		return " delete  "+ this.getDbSchemaName() + tempTableNameMap.getTempDxTableName() + " \n<*>\n delete  " + this.getDbSchemaName()+ tempTableNameMap.getTempTableName();
		
	}
	

	public String deleteTempMaster(String masterId,int level) {
		return "\n <*> \n delete " + this.getDbSchemaName() + tempTableNameMap.getTempMasterTable() +" where master_id = '" + masterId + "' and level_no >= " + level + "\n<*>";
	}
	
	

}
