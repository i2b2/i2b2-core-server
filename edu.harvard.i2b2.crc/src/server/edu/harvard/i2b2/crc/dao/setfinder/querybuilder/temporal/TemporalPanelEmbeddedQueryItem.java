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
 
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.TempTableNameMap;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;

public class TemporalPanelEmbeddedQueryItem extends TemporalPanelItem {

	private String masterQueryTiming = null;

	public TemporalPanelEmbeddedQueryItem(TemporalPanel parent, ItemType item)
			throws I2B2Exception {
		super(parent, item);
		parseEmbeddedItem();
	}

	protected void parseEmbeddedItem() throws I2B2Exception {
		// TODO Auto-generated method stub
		super.parseItem();
		
		TemporalQuery masterQuery = null;

		//first look at query defintion to see if it was sent in the request
		QueryDefinitionType queryDef = parent.searchForQueryInRequestDefinition(baseItem.getItemKey());
		if (queryDef!=null){
			try {
				masterQuery = new TemporalQuery(parent.getDataSourceLookup(), parent.getProjectParameterMap(), queryDef, parent.allowLargeTextValueConstrainFlag(), parent.getProcessingLevel() + 1,
						parent.getProjectId(), parent.getRequestorSecurityType(), parent.getSecurityType());
			} catch (JAXBUtilException e) {
				e.printStackTrace();
				throw new I2B2Exception("Error processing embedded query: " + e.getMessage());
			}
		}
		else {
			String requestXml = getQueryDefinitionRequestXml(baseItem.getItemKey());
			try {
				masterQuery = new TemporalQuery(parent.getDataSourceLookup(), parent.getProjectParameterMap(), requestXml, parent.allowLargeTextValueConstrainFlag(), parent.getProcessingLevel() + 1);
			} catch (JAXBUtilException e) {
				e.printStackTrace();
				throw new I2B2Exception("Error processing embedded query: " + e.getMessage());
			}
		}

		parent.addPreProcessingSql(masterQuery.buildSql());
		masterQueryTiming = masterQuery.getQueryTiming();

		parent.addPreProcessingSql(copyDxTempToMaster(baseItem.getItemKey(), masterQueryTiming, String.valueOf(masterQuery.getMaxPanelIndex()), 
				masterQuery.getDxTempTableName(), masterQuery.getMasterTempTableName()));

		parent.addPreProcessingSql(deleteDxTempTable(masterQuery.getDxTempTableName()));
		parent.addPreProcessingSql(deleteTempTable(masterQuery.getTempTableName()));
		
		parent.addPostProcessingSql(deleteMasterTempTable(baseItem.getItemKey(), parent.getProcessingLevel(), masterQuery.getMasterTempTableName()));
	}

	
	public String getQueryDefinitionRequestXml(String itemKey)
			throws I2B2DAOException {

		DAOFactoryHelper helper = new DAOFactoryHelper(
				parent.getDataSourceLookup().getDomainId(),
				parent.getDataSourceLookup().getProjectPath(),
				parent.getDataSourceLookup().getProjectPath());

		SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory()
				.getSetFinderDAOFactory();

		String masterId = itemKey.substring(9);
		QtQueryMaster queryMaster = sfDaoFactory.getQueryMasterDAO().getQueryDefinition(masterId);
		return queryMaster.getI2b2RequestXml();
	}


	@Override
	protected String buildSql() throws I2B2DAOException {
		if (this.returnEncounterNum()||
				this.returnInstanceNum()||
				this.hasItemDateConstraint()||
				this.hasModiferConstraint()||
				this.hasPanelDateConstraint()||
				this.hasPanelOccurrenceConstraint()||
				this.hasValueConstraint()
				//||parent.isTimingQuery()
				){
			return super.buildSql();
		}
		else{
			return "select "
					+ this.factTableColumn + " from " + noLockSqlServer
					+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : parent.getDatabaseSchema() )
					+ this.tableName
					+ "  " + " where " + this.columnName + " "
					+ this.operator + " " + this.dimCode
					+ "";
		}
	}


	@Override
	protected ConceptType getConceptType() throws ConceptNotFoundException,
			OntologyException {
		if (conceptType==null){
			
			TempTableNameMap tempTableNameMap = new TempTableNameMap(
					parent.getServerType());
			String masterTableName = tempTableNameMap.getTempMasterTable();
			
			String itemKey = baseItem.getItemKey();
	
			if (itemKey != null) {
				conceptType = new ConceptType();
				conceptType.setColumnname(" master_id ");
				conceptType.setOperator(" = ");
				conceptType.setColumndatatype("T");
				conceptType.setFacttablecolumn(" patient_num ");
				conceptType.setTablename(masterTableName);
				conceptType.setDimcode("'" + itemKey + "'");
			}
		}

		return conceptType;
	}


	@Override
	protected String getJoinTable() {
		if (this.returnInstanceNum()||
				hasItemDateConstraint()||
				hasPanelDateConstraint()||
				hasValueConstraint()||
				hasPanelOccurrenceConstraint()) {
			return "observation_fact";
		} else if (this.returnEncounterNum()
				//||parent.isTimingQuery()
				) {
			return "visit_dimension";
		} else {

			TempTableNameMap tempTableNameMap = new TempTableNameMap(
					parent.getServerType());
			String masterTableName = tempTableNameMap.getTempMasterTable();
			return masterTableName;
		}
	}

	@Override
	protected String buildDimensionJoinSql(String tableAlias) {
		String dimensionSql = "";

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";
		
		if (masterQueryTiming==null||masterQueryTiming.trim().length()==0)
			masterQueryTiming="ANY";
		
		String dbSchema = parent.getDatabaseSchema();
		if (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)){
			dbSchema = "";
		}
		
		if (masterQueryTiming.equals(QueryTimingHandler.SAMEINSTANCENUM)){
			//we have all columns in the master table to join to 
			if (parent.getPanelTiming().equals(QueryTimingHandler.SAMEINSTANCENUM)||parent.hasPanelOccurrenceConstraint()){
				dimensionSql = "exists (select 1 "
					+ "from " + noLockSqlServer
					+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : dbSchema )					
					+ this.tableName + " mqt "
					+ "where mqt.patient_num = " + tableAlias + "patient_num "
					+ "and mqt.encounter_num = " + tableAlias + "encounter_num "
					+ "and mqt.provider_id = " + tableAlias + "provider_id "
					+ "and mqt.start_date = " + tableAlias + "start_date "
					+ "and mqt.concept_cd = " + tableAlias + "concept_cd "
					+ "and mqt.instance_num = " + tableAlias + "instance_num "
					+ "and mqt.master_id = " + this.dimCode
					+ ")";
			}
			else if (parent.getPanelTiming().equals(QueryTimingHandler.SAME)||parent.getPanelTiming().equals(QueryTimingHandler.SAMEVISIT)||parent.hasPanelDateConstraint()||this.hasItemDateConstraint()){
				dimensionSql = "exists (select 1 "
						+ "from " + noLockSqlServer
						+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : dbSchema )					
						+ this.tableName + " mqt "
						+ "where mqt.patient_num = " + tableAlias + "patient_num "
						+ "and mqt.encounter_num = " + tableAlias + "encounter_num "
						+ "and mqt.master_id = " + this.dimCode
						+ ")";
			}
			else {
				dimensionSql = "exists (select 1 "
						+ "from " + noLockSqlServer
						+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : dbSchema )					
						+ this.tableName + " mqt "
						+ "where mqt.patient_num = " + tableAlias + "patient_num "
						+ "and mqt.master_id = " + this.dimCode
						+ ")";
			}
		}
		else if (masterQueryTiming.equals(QueryTimingHandler.SAME)||masterQueryTiming.equals(QueryTimingHandler.SAMEVISIT)){
			if (parent.getPanelTiming().equals(QueryTimingHandler.SAMEINSTANCENUM)||parent.hasPanelOccurrenceConstraint()||
					parent.getPanelTiming().equals(QueryTimingHandler.SAME)||parent.getPanelTiming().equals(QueryTimingHandler.SAMEVISIT)||
					parent.hasPanelDateConstraint()||this.hasItemDateConstraint()){
				dimensionSql = "exists (select 1 "
						+ "from " + noLockSqlServer
						+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : dbSchema )					
						+ this.tableName + " mqt "
						+ "where mqt.patient_num = " + tableAlias + "patient_num "
						+ "and mqt.encounter_num = " + tableAlias + "encounter_num "
						+ "and mqt.master_id = " + this.dimCode
						+ ")";
			}
			else {
				dimensionSql = "exists (select 1 "
						+ "from " + noLockSqlServer
						+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : dbSchema )					
						+ this.tableName + " mqt "
						+ "where mqt.patient_num = " + tableAlias + "patient_num "
						+ "and mqt.master_id = " + this.dimCode
						+ ")";
			}
		}
		else {
			dimensionSql = "exists (select 1 "
					+ "from " + noLockSqlServer
					+ (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ? "" : dbSchema )					
					+ this.tableName + " mqt "
					+ "where mqt.patient_num = " + tableAlias + "patient_num "
					+ "and mqt.master_id = " + this.dimCode
					+ ")";			
		}
						
		return dimensionSql;
	}

	
	public String copyDxTempToMaster(String masterId, String masterQueryTiming, String maxPanelNum, String dxTempTableName, String masterTempTableName) {
		String selectFields = " ";
		String joinTableName = dxTempTableName;
		String joinColumnName = "patient_num";
		if (this.returnInstanceNum()) {
			selectFields = " encounter_num, instance_num, patient_num, concept_cd, start_date, provider_id, ";
		} else if (this.returnEncounterNum()) {
			selectFields = "encounter_num, patient_num, ";
		} else {
			selectFields = " patient_num, ";
		}

		if (masterQueryTiming != null) {
			QueryTimingHandler queryTimingHandler = new QueryTimingHandler();

			if (queryTimingHandler.isSameInstanceNum(masterQueryTiming)) {
				selectFields = " encounter_num, instance_num, patient_num, concept_cd, start_date, provider_id, ";
			} else if (queryTimingHandler.isSameVisit(masterQueryTiming)) {
				selectFields = "encounter_num, patient_num, ";
			} else {
				selectFields = " patient_num, ";
			}
		}

		if (joinTableName.equals(dxTempTableName)){
			return " insert into " + masterTempTableName + "(master_id, "
					+ selectFields + " level_no) " + "select '" + masterId + "', "
					+ selectFields + parent.getProcessingLevel() + "  from " 
					+ dxTempTableName;			
		}
		else {
			return " insert into " + masterTempTableName + "(master_id, "
					+ selectFields + " level_no) " + "select '" + masterId + "', "
					+ selectFields + parent.getProcessingLevel() + "  from " 
					+ joinTableName + " where " + joinColumnName + " IN ( "
					+ "select " + joinColumnName + " from "
					+ dxTempTableName + " ) ";
		}
	}
	
	public String deleteDxTempTable(String dxTempTableName){
		return " delete  " + 
			(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)? " from " : "") +
				dxTempTableName;		
	}

	public String deleteTempTable(String tempTableName) {
		return " delete  " +
				(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)? " from " : "") +
				tempTableName;		
	}

	public String deleteMasterTempTable(String masterId, int level, String masterTempTableName) {
		return "delete " + 
				(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)? " from " : "") +
				masterTempTableName
				+ " where master_id = '" + masterId + "' and level_no >= "
				+ level;
	}

}
