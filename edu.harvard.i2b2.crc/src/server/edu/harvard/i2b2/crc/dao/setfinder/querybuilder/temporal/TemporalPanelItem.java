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
 
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.DateConstrainUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.TotalItemOccurrenceHandler;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.UnitConverstionUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ValueConstrainsHandler;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.ontology.ModifierType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.query.TotOccuranceOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType.ConstrainByDate;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType.ConstrainByValue;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;
import edu.harvard.i2b2.crc.util.StringUtil;


/**
 **
 * Temporal Panel Item
 * 
 * <P>
 * Panel Item query object that wraps the item tag found in the query definition
 * xml for panel items. It roughly corresponds to an item dropped into a panel in the query UI. PanelItem is responsible
 * for generating the sql for an individual panel item. It implements the logic for the sql query
 * and takes into account item value constraints, modifier constraints, item and panel based date constraints, 
 * and panel occurrences. This class is an abstract class and must be implemented by class dedicated to 
 * specific panel item types.
 * 
 * @author Christopher Herrick
 *  
 */
public abstract class TemporalPanelItem {

	protected final Log log = LogFactory.getLog(getClass());

	protected TemporalPanel parent = null;

	protected ItemType baseItem = null;
	protected ConceptType conceptType = null;
	protected ModifierType modifierType = null;
	protected Integer conceptTotal;
	protected String noLockSqlServer = " ";

	//Concept Type fields
	protected String factTableColumn = null;
	protected String tableName = null;
	protected String dimCode = null;
	protected String operator = null;
	protected String columnName = null;
	protected XmlValueType metaDataXml = null;
	protected String factTable = "observation_fact";
	
	/**
	 * Constructor
	 * 
	 * @param parent - TemporalPanel object to which this panle item belongs
	 * @param item - object representation of the xml from panel item contained in query definition xml 
	 * @throws I2B2Exception - thrown when an i2b2 specific error is found
	 */
	public TemporalPanelItem(TemporalPanel parent, ItemType item)
			throws I2B2Exception {
		this.parent = parent;
		this.baseItem = item;
		parseItem();
	}

	/**
	 * Constructor
	 * 
	 * @param parent - TemporalPanel object to which this panle item belongs
	 * @param item - object representation of the xml from panel item contained in query definition xml 
	 * @param conceptType - object representation of the item obtained from the Ontology cell
	 * @throws I2B2Exception - thrown when an i2b2 specific error is found
	 */
	public TemporalPanelItem(TemporalPanel parent, ItemType item, ConceptType conceptType)
			throws I2B2Exception {
		this.parent = parent;
		this.baseItem = item;
		this.conceptType = conceptType;
		parseItem();
	}

	/**
	 * Parse Item
	 * 
	 * Parses through the item and/or concept type object and assigns key fields
	 * to the corresponding properties of the class. ParseItem is called from the constructor
	 * and must be executed before trying to build sql on the item.
	 * 
	 * @throws I2B2Exception - thrown when an i2b2 specific error is found
	 */
	protected void parseItem() throws I2B2Exception {
		if (conceptType==null){
			conceptType = getConceptType();
		}
		if (conceptType != null) {
			if (conceptType.getTotalnum() != null) {
				conceptTotal = conceptType.getTotalnum();
			}
			factTableColumn = conceptType.getFacttablecolumn();
			tableName = conceptType.getTablename();
			dimCode = conceptType.getDimcode();
			operator = conceptType.getOperator();
			columnName = conceptType.getColumnname();
			metaDataXml = conceptType.getMetadataxml();
			//OMOP addition
			parseFactColumn(factTableColumn);
		}
	}

	/**
	 * Build Sql 
	 * 
	 * Main method for generating the sql string that will be run on the
	 * database for this item. Constructs sql based on logic for main item, value constraints,
	 * modifier constraints, date constraints, and occurrence constraints
	 * 
	 * @return String - sql logic for querying based on the constraints represented in this item
	 * @throws I2B2DAOException - thrown when an i2b2 specific data or database error occurs
	 */
	protected String buildSql() throws I2B2DAOException {

		checkLargeTextConstrainPermission();

		String sqlHintClause = buildSqlHintClause();
		String selectSql = buildSelectSql();
		String fromSql = buildFromSql();			
		String dimensionJoinSql = buildDimensionJoinSql();
		String modifierConstraintSql = buildModifierConstraintSql();
		String[] modifierValueConstrainSql = buildModifierValueConstraintSql();
		String dateConstraintSql = buildItemDateConstraintSql();
		String[] valueConstraintSql = buildValueConstraintSql();
		String panelDateConstraintSql = buildPanelDateConstraintSql();
		String groupbyClause = buildGroupBySql();
		String havingClause = buildHavingSql();

		if (parent.getAccuracyScale()>0&&
				valueConstraintSql[1]!=null&&
				valueConstraintSql[1].trim().length()>0) { 
			fromSql += valueConstraintSql[1];
		}

		if (parent.getAccuracyScale() >0&
				modifierValueConstrainSql[1]!=null&&
				modifierValueConstrainSql[1].trim().length()>0) { 
			fromSql += modifierValueConstrainSql[1];
		}

		String derivedTableSql = " select " + sqlHintClause 
				+ selectSql
				+ " \nfrom " + fromSql 
				+ " \nwhere  " 
				+ formatSql(dimensionJoinSql) + "  "
				+ formatSql(modifierConstraintSql) 
				+ formatSql(modifierValueConstrainSql[0])
				+ formatSql(valueConstraintSql[0])
				+ formatSql(dateConstraintSql) 
				+ formatSql(panelDateConstraintSql)
				+ " \ngroup by " + groupbyClause
				+ formatSql(havingClause);
		log.debug("Derived table sql [" + derivedTableSql + "]");

		return derivedTableSql;
	}

	/**
	 * Format Sql
	 * 
	 * Method for adding carriage returns into sql so statements are more
	 * readable when output to error and logs
	 * 
	 * @param sql String to be formatted
	 * @return String formatted sql string
	 */
	private String formatSql(String sql){
		if (sql!=null&&sql.trim().length()>0)
			return "\n" + sql;
		else
			return "";
	}


	/**
	 * Build Select Sql
	 * 
	 * Construct the sql SELECT clause for this item. Determines which columns should be
	 * returned by the query
	 * 
	 * @return String sql SELECT clause for use in larger statement
	 */
	protected String buildSelectSql(){
		return buildSelectSql(getPrimaryTableAlias());
	}

	/**
	 * Build Select Sql
	 * 
	 * Construct the sql SELECT clause for this item. Determines which columns should be
	 * returned by the query
	 * 
	 * @param tableAlias String used to alias all columns contained in the select statement
	 * @return String sql SELECT clause for use in larger statement
	 */
	protected String buildSelectSql(String tableAlias){

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";

		String selectClause = tableAlias + "patient_num ";

		if (this.returnInstanceNum()) {
			selectClause = " " + tableAlias + "provider_id, " 
					+ tableAlias + "start_date, " 
					+ tableAlias + "concept_cd, " 
					+ tableAlias + "instance_num, " 
					+ tableAlias + "encounter_num, "
					+ selectClause;
		} else if (this.returnEncounterNum()) {
			selectClause = " " + tableAlias + "encounter_num, " 
					+ selectClause;
		}

		if (parent.hasPanelOccurrenceConstraint() &&
				parent.applyOccurrenceToPanelLevel() &&
				(parent.getItemList().size()>1 ||
						!parent.isPatientOnlyQuery())){
			selectClause += ", " + buildFactCountSql(tableAlias) + " ";
		}

		return selectClause;
	}

	/**
	 * Build Sql Hint Clause
	 * 
	 * Returns any database specific sql syntax used to guid the database optimizer in 
	 * choosing an execution plan or index to use
	 * 
	 * @return String sql clause that contains database specific parameters for passing sql hints to optimizer
	 */
	protected String buildSqlHintClause(){
		return " ";
	}

	/**
	 * Build From Sql
	 * 
	 * Construct the sql FROM clause for this item. Determines which tables should be
	 * referenced and joined to in the item query
	 *   
	 * @return String sql FROM clause that contains tables used in this item query 
	 */
	protected String buildFromSql(){
		return buildFromSql(getPrimaryTableAlias());
	}

	/**
	 * Build From Sql
	 * 
	 * Construct the sql FROM clause for this item. Determines which tables should be
	 * referenced and joined to in the item query
	 *   
	 * @param tableAlias String used to alias the table in the from clause
	 * @return String sql FROM clause that contains tables used in this item query 
	 */
	protected String buildFromSql(String tableAlias){
		String joinTableName = parent.getDatabaseSchema() + getJoinTable();

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			joinTableName += " " + tableAlias;

		return joinTableName;
	}


	/**
	 * Build Dimension Join Sql
	 * 
	 * Construct the sql FROM clause for this item. Determines which tables should be
	 * referenced and joined to in the item query.  Use the default table alias.
	 *   
	 * @return String sql FROM clause that contains tables used in this item query 
	 */
	protected String buildDimensionJoinSql(){
		return buildDimensionJoinSql(getPrimaryTableAlias());
	}

	/**
	 * Build Dimension Join Sql
	 * 
	 * Construct the sql FROM clause for this item. Determines which tables should be
	 * referenced and joined to in the item query
	 *   
	 * @param tableAlias String used to alias the table in the from clause
	 * @return String sql FROM clause that contains tables used in this item query 
	 */
	protected String buildDimensionJoinSql(String tableAlias) {
		String dimensionSql = "";

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";

		if ((this.operator!=null)&& 
				(this.operator.toUpperCase().equals("LIKE"))&&
				(this.dimCode!=null)  && (parent.getServerType().equalsIgnoreCase("POSTGRESQL")))
		{
			this.dimCode = this.dimCode.replaceAll("\\\\", "\\\\\\\\");

		}
		dimensionSql = tableAlias + this.factTableColumn + " IN (select "
				+ this.factTableColumn + " from " + noLockSqlServer
				+ parent.getDatabaseSchema() + this.tableName
				+ "  " + " where " + this.columnName + " "
				+ this.operator + " " + this.dimCode;

		if ((this.operator!=null)&& 
				(this.operator.toUpperCase().equals("LIKE"))&&
				(this.dimCode!=null)&&
				(this.dimCode.contains("?")))
		{			
			dimensionSql +=  (!parent.getDataSourceLookup().getServerType().toUpperCase().equals("POSTGRESQL") ? " {ESCAPE '?'} " : "" ) ;
		}
		dimensionSql += ")";
		return dimensionSql;
	}

	/**
	 * Build Item Date Constraint Sql
	 * 
	 * Construct sql WHERE constraint statements that constrain this item by date. These date constraints orignate
	 * at the item level and not at the panel level.
	 * 
	 * @return String sql constraint clause to be appended to the WHERE clause generated by this item
	 */
	protected String buildItemDateConstraintSql() {
		return buildItemDateConstraintSql(getPrimaryTableAlias());
	}


	/**
	 * Build Item Date Constraint Sql
	 * 
	 * Construct sql WHERE constraint statements that constrain this item by date. These date constraints orignate
	 * at the item level and not at the panel level.
	 * 
	 * @param tableAlias String used to alias the table that is being constrained
	 * @return String sql constraint clause to be appended to the WHERE clause generated by this item
	 */
	protected String buildItemDateConstraintSql(String tableAlias) {
		// generate sql for item date constrain
		DateConstrainUtil dateConstrainUtil = new DateConstrainUtil(
				parent.getDataSourceLookup());
		String itemDateConstrainSql = dateConstrainUtil
				.buildItemDateSql(baseItem, tableAlias);
		if (itemDateConstrainSql != null
				&& itemDateConstrainSql.trim().length() > 0) {
			log.info("Item date constrain sql" + itemDateConstrainSql);
			itemDateConstrainSql = "  AND ( " + itemDateConstrainSql + " ) ";
		} else {
			itemDateConstrainSql = "";
		}
		return itemDateConstrainSql;
	}

	protected String[] buildValueConstraintSql()
			throws I2B2DAOException {

		// generate sql for unit_cd conversion
		String unitCdSwitchClause = "", unitCdInClause = "";

		if (parent.getProjectParameterMap() != null
				&& parent.getProjectParameterMap().get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION) != null) {
			String unitCdConversionFlag = (String) parent.getProjectParameterMap().get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
			if (unitCdConversionFlag != null
					&& unitCdConversionFlag.equalsIgnoreCase("ON")) {
				if (metaDataXml != null
						&& metaDataXml.getAny().get(0) != null) {
					Element valueMetadataElement = (Element) metaDataXml.getAny().get(0);
					UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
					unitCdSwitchClause = unitConverstionUtil
							.buildUnitCdSwitchClause(valueMetadataElement,
									false, "");
					log.debug("concept unit Conversion sql "
							+ unitCdSwitchClause);
					unitCdInClause = unitConverstionUtil.buildUnitCdInClause(
							valueMetadataElement, "");

				}
			}
		}

		ValueConstrainsHandler valueConstrainHandler = new ValueConstrainsHandler();
		if (unitCdSwitchClause.length() > 0) {
			valueConstrainHandler.setUnitCdConversionFlag(true, unitCdInClause,
					unitCdSwitchClause);
		}

		String[] itemValueConstrainSql = valueConstrainHandler
				.constructValueConstainClause(baseItem.getConstrainByValue(),
						parent.getServerType(), parent.getDatabaseSchema(),
						parent.getAccuracyScale(), true);
		log.info("Item value constrain sql " + itemValueConstrainSql);

		if (itemValueConstrainSql != null
				&& itemValueConstrainSql[0].trim().length() > 0) {

			itemValueConstrainSql[0] = "  AND  ( " + itemValueConstrainSql[0]
					+ " )";
		} else {
			itemValueConstrainSql[0] = "";
		}
		return itemValueConstrainSql;
	}

	protected String[] buildModifierValueConstraintSql()
			throws I2B2DAOException {

		if (modifierType==null){
			modifierType = getModifierMetadataFromOntology();
		}

		if (modifierType==null)
			return new String[] { "", "" };

		String modifierUnitCdSwitchClause = "", modifierUnitCdInClause = "";

		if (parent.getProjectParameterMap() != null
				&& parent.getProjectParameterMap().get(
						ParamUtil.CRC_ENABLE_UNITCD_CONVERSION) != null) {
			String unitCdConversionFlag = (String) parent
					.getProjectParameterMap().get(
							ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
			if (unitCdConversionFlag != null
					&& unitCdConversionFlag.equalsIgnoreCase("ON")) {
				if (modifierType != null
						&& modifierType.getMetadataxml() != null
						&& modifierType.getMetadataxml().getAny().get(0) != null) {
					Element valueMetadataElement = (Element) modifierType
							.getMetadataxml().getAny().get(0);
					UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
					modifierUnitCdSwitchClause = unitConverstionUtil
							.buildUnitCdSwitchClause(valueMetadataElement,
									false, "");
					log.debug("modifier unit Conversion sql "
							+ modifierUnitCdSwitchClause);
					modifierUnitCdInClause = unitConverstionUtil
							.buildUnitCdInClause(valueMetadataElement, "");

				}
			}
		}

		ValueConstrainsHandler valueConstrainHandler = new ValueConstrainsHandler();
		if (modifierUnitCdSwitchClause.length() > 0) {
			valueConstrainHandler.setUnitCdConversionFlag(true,
					modifierUnitCdInClause, modifierUnitCdSwitchClause);
		}


		String itemModifierValueConstrainSql[] = new String[] { "", "" };

		if (baseItem.getConstrainByModifier() != null
				&& baseItem.getConstrainByModifier().getConstrainByValue() != null) {
			List<ItemType.ConstrainByValue> itemValueConstrainList = getModifierItemValueConstrain(baseItem.getConstrainByModifier().getConstrainByValue());
			itemModifierValueConstrainSql = valueConstrainHandler
					.constructValueConstainClause(itemValueConstrainList,
							parent.getServerType(), parent.getDatabaseSchema(),
							parent.getAccuracyScale(), false);
			if (itemModifierValueConstrainSql != null
					&& itemModifierValueConstrainSql[0].length() > 0) {
				log.info("Modifier constrian value constrain sql "
						+ itemModifierValueConstrainSql);
			}
		}

		if (itemModifierValueConstrainSql[0] != null
				&& itemModifierValueConstrainSql[0].trim().length() > 0) {

			itemModifierValueConstrainSql[0] = "  AND  ( "
					+ itemModifierValueConstrainSql[0] + " )";
		} else {
			itemModifierValueConstrainSql[0] = "";
		}
		return itemModifierValueConstrainSql;
	}

	protected String buildModifierConstraintSql() throws I2B2DAOException {
		return buildModifierConstraintSql(getPrimaryTableAlias());
	}

	protected String buildModifierConstraintSql(String tableAlias) throws I2B2DAOException {
		if (modifierType==null)
			modifierType = getModifierMetadataFromOntology();

		if (modifierType==null)
			return "";			

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";

		String dimPath = "", dimColumnName = "", dimCode = "", dimOperator = "", dimTableName = "", factTableColumn = "";

		String itemModifierConstrainSql = null;
		if (modifierType != null) {
			dimPath = modifierType.getFullname();
			dimColumnName = modifierType.getColumnname();
			dimCode = modifierType.getDimcode();
			dimOperator = modifierType.getOperator();
			dimTableName = modifierType.getTablename();
			factTableColumn = modifierType.getFacttablecolumn();

			if (dimPath == null || dimColumnName == null || dimCode == null
					|| dimOperator == null || dimTableName == null
					|| factTableColumn == null) {
				throw new I2B2DAOException(
						"Error modifier constrain information has null value path ["
								+ dimPath + "] column name [" + dimColumnName
								+ "] dim code [" + dimCode
								+ "] dim operator [ " + dimOperator
								+ "] dim table name [" + dimTableName
								+ "] fact table column [" + factTableColumn
								+ "]");
			}
		}

		dimPath.replaceAll("'", "''");

		if ((dimOperator != null) && (dimOperator.toUpperCase().equals("LIKE") 
				&& (parent.getDataSourceLookup().getServerType().toUpperCase().equals("POSTGRESQL"))))
			dimCode = dimCode.replaceAll("\\\\", "\\\\\\\\");

				

		itemModifierConstrainSql = " (" + tableAlias + factTableColumn + " IN  "
				+ "(select " + factTableColumn 
				+ " from " + parent.getDatabaseSchema() + dimTableName 
				+ " where " + dimColumnName + " " + dimOperator + " " + dimCode;

		if ((dimOperator != null) && (dimOperator.toUpperCase().equals("LIKE"))
				&& (dimCode != null) && (dimCode.contains("?"))) {
			itemModifierConstrainSql +=  (!parent.getDataSourceLookup().getServerType().toUpperCase().equals("POSTGRESQL") ? " {ESCAPE '?'} " : "" ) ;
		}
		itemModifierConstrainSql += ")) ";

		if (itemModifierConstrainSql != null
				&& itemModifierConstrainSql.trim().length() > 0) {
			log.info("Item modifier constrain sql" + itemModifierConstrainSql);
			itemModifierConstrainSql = " AND " + itemModifierConstrainSql;
		} else {
			itemModifierConstrainSql = " ";
		}

		return itemModifierConstrainSql;
	}

	protected String buildPanelDateConstraintSql(){
		return buildPanelDateConstraintSql(getPrimaryTableAlias());
	}

	protected String buildPanelDateConstraintSql(String tableAlias){
		String panelDateConstraintSql = parent.buildDateConstraintSql(tableAlias);
		if (panelDateConstraintSql!=null&&panelDateConstraintSql.trim().length() > 0) {
			panelDateConstraintSql = "  AND  ( " + panelDateConstraintSql + " )";
		}

		return panelDateConstraintSql;
	}

	protected String buildGroupBySql(){
		return buildGroupBySql(getPrimaryTableAlias());
	}

	protected String buildGroupBySql(String tableAlias){
		// check if the dimensionJoinSql is query in query with fact constrains
		String groupbyClause = "";

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";

		if (this.returnInstanceNum()) {
			groupbyClause = " " + tableAlias + "encounter_num ,"
					+ tableAlias + "instance_num, "
					+ tableAlias + "concept_cd," + tableAlias
					+ "start_date," + tableAlias + "provider_id,";
		}
		else if (this.returnEncounterNum()){
			groupbyClause = " " + tableAlias + "encounter_num ,";
		}

		groupbyClause += " " + tableAlias + "patient_num ";

		return groupbyClause;
	}

	protected String buildFactCountSql(String tableAlias){
		TotalItemOccurrences totalOccur = parent.getTotalOccurrences();

		if (tableAlias!=null&&tableAlias.trim().length()>0&&!tableAlias.endsWith("."))
			tableAlias += ".";

		if (totalOccur != null) {
			if ((totalOccur.getOperator() != null
					&& totalOccur.getOperator().value() != null 
					&& totalOccur.getOperator().value().equalsIgnoreCase(TotOccuranceOperatorType.GE.value()))
					&& totalOccur.getValue() == 1) {
			} else {
				String countDistinct = "*";
				if (parent.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER) ) {
					countDistinct = " distinct cast(" + tableAlias + "patient_num as varchar) + '|' +  cast(" + tableAlias + "encounter_num as varchar) + '|' + "
							+ " " + tableAlias + "provider_id + '|' + cast(" + tableAlias + "start_date as varchar) + '|' + cast(" + tableAlias + "instance_num as varchar) + '|' + " + tableAlias + "concept_cd";
				} else if (parent.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.ORACLE) || parent.getServerType().equalsIgnoreCase(
								DAOFactoryHelper.POSTGRESQL)) {
					countDistinct = " distinct " + tableAlias + "patient_num || '|' || " + tableAlias + "encounter_num || '|' || " 
							+ tableAlias + "provider_id || '|' || " + tableAlias + "instance_num || '|' ||" + tableAlias + "concept_cd || '|' ||cast(" + tableAlias + "start_date as varchar(50))";
				}

				return "count(" + countDistinct + ") as fact_count";
			}
		}

		return "";
	}

	protected String buildHavingSql(){
		return buildHavingSql(getPrimaryTableAlias());
	}

	protected String buildHavingSql(String tableAlias){
		TotalItemOccurrences totalOccur = parent.getTotalOccurrences();

		TotalItemOccurrenceHandler totalItemOccurrencHandler = new TotalItemOccurrenceHandler();
		String totalItemOccurrenceClause = totalItemOccurrencHandler.buildTotalItemOccurrenceClause(totalOccur);

		String havingSql = " ";

		if (tableAlias!=null&&tableAlias.trim().length()>0)
			tableAlias += ".";

		if ((!parent.applyOccurrenceToPanelLevel()||parent.getItemList().size()==1) && totalOccur != null) {
			if ((totalOccur.getOperator() != null
					&& totalOccur.getOperator().value() != null 
					&& totalOccur.getOperator().value().equalsIgnoreCase(TotOccuranceOperatorType.GE.value()))
					&& totalOccur.getValue() == 1) {
			} else {
				log.debug("Setfinder query total occurrences operator value ["
						+ totalOccur.getOperator().value() + "]");
				String countDistinct = "*";
				if (parent.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER) ) {
					countDistinct = " distinct cast(" + tableAlias + "patient_num as varchar) + '|' +  cast(" + tableAlias + "encounter_num as varchar) + '|' + "
							+ " " + tableAlias + "provider_id + '|' + cast(" + tableAlias + "start_date as varchar) + '|' + cast(" + tableAlias + "instance_num as varchar) + '|' + " + tableAlias + "concept_cd";
				} else if (parent.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.ORACLE) || parent.getServerType().equalsIgnoreCase(
								DAOFactoryHelper.POSTGRESQL)) {
					countDistinct = " distinct " + tableAlias + "patient_num || '|' || " + tableAlias + "encounter_num || '|' || " 
							+ tableAlias + "provider_id || '|' || " + tableAlias + "instance_num || '|' ||" + tableAlias + "concept_cd || '|' ||cast(" + tableAlias + "start_date as varchar(50))";
				} 
				havingSql = " having count(" + countDistinct + ") "
						+ totalItemOccurrenceClause;
			}
		}

		return havingSql;
	}
	
	public void parseFactColumn(String factColumnName){
		this.factTable= "observation_fact";
		this.factTableColumn = factColumnName;
		if (this.parent.getQueryOptions()!=null&&this.parent.getQueryOptions().useDerivedFactTable())
		{
			if (factColumnName!=null&&factColumnName.contains(".")){
				int lastIndex = factColumnName.lastIndexOf(".");
				this.factTable= factColumnName.substring(0, lastIndex);
				if ((lastIndex+1)<factColumnName.length()){
					this.factTableColumn = factColumnName.substring(lastIndex+1);
				}
			}
	//		log.info("using derived fact table: " + factTable);
		}
	}


	protected String getJoinTable(){
		return factTable;
	}


	protected void checkLargeTextConstrainPermission() throws I2B2DAOException{
		for (ConstrainByValue cvt : baseItem.getConstrainByValue()) {
			if (cvt.getValueType().equals(ConstrainValueType.LARGETEXT)) {
				if (parent.allowLargeTextValueConstrainFlag() == false) {
					throw new I2B2DAOException("Insufficient user role for LARGETEXT constrain. Required minimum role DATA_DEID");
				}
			}
		}
	}

	protected ModifierType getModifierMetadataFromOntology()
			throws ConceptNotFoundException, OntologyException{
		if (modifierType==null){
			ItemType.ConstrainByModifier modifierConstrain = baseItem.getConstrainByModifier();
			if (modifierConstrain == null) {
				return null;
			}
			String modifierKey = modifierConstrain.getModifierKey();
			String modifierAppliedPath = modifierConstrain.getAppliedPath();
			modifierType = getModifierMetadataFromOntology(modifierKey, modifierAppliedPath);
		}
		return modifierType;
	}

	protected List<ItemType.ConstrainByValue> getModifierItemValueConstrain(
			List<ItemType.ConstrainByModifier.ConstrainByValue> modifierConstrainList) {
		List<ItemType.ConstrainByValue> itemValueConstrainList = new ArrayList<ItemType.ConstrainByValue>();
		for (ItemType.ConstrainByModifier.ConstrainByValue modifierValueConstrain : modifierConstrainList) {
			ItemType.ConstrainByValue constrainByValue = new ItemType.ConstrainByValue();
			constrainByValue.setValueConstraint(modifierValueConstrain
					.getValueConstraint());
			constrainByValue.setValueOperator(modifierValueConstrain
					.getValueOperator());
			constrainByValue
			.setValueType(modifierValueConstrain.getValueType());
			constrainByValue.setValueUnitOfMeasure(modifierValueConstrain
					.getValueUnitOfMeasure());
			itemValueConstrainList.add(constrainByValue);
		}
		return itemValueConstrainList;
	}

	protected ConceptType getConceptType() throws ConceptNotFoundException,
	OntologyException {
		if (conceptType==null){
			conceptType = getMetaDataFromOntologyCell(baseItem.getItemKey());
		}

		return conceptType;
	}

	protected ConceptType getMetaDataFromOntologyCell(String itemKey)
			throws ConceptNotFoundException, OntologyException {
		ConceptType conceptType = null;
		try {
			conceptType = CallOntologyUtil.callOntology(itemKey,
					parent.getSecurityType(), parent.getProjectId(),
					QueryProcessorUtil.getInstance().getOntologyUrl());
		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + itemKey
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

		if (conceptType == null) {
			throw new ConceptNotFoundException("[" + itemKey + "] ");

		} else {
			String theData = conceptType.getDimcode();
			if (conceptType.getColumndatatype() != null
					&& conceptType.getColumndatatype().equalsIgnoreCase("T")) {

				if(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)){
					conceptType.setDimcode(StringUtil.escapeSQLSERVER(conceptType.getDimcode()));
				}
				else if(parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)){
					conceptType.setDimcode(StringUtil.escapeORACLE(conceptType.getDimcode()));
				}

				theData = SqlClauseUtil.handleMetaDataTextValue(
						conceptType.getOperator(), conceptType.getDimcode());
			} else if (conceptType.getColumndatatype() != null
					&& conceptType.getColumndatatype().equalsIgnoreCase("N")) {
				theData = SqlClauseUtil.handleMetaDataNumericValue(
						conceptType.getOperator(), conceptType.getDimcode());
			} else if (conceptType.getColumndatatype() != null
					&& conceptType.getColumndatatype().equalsIgnoreCase("D")) {
				theData = SqlClauseUtil.handleMetaDataDateValue(
						conceptType.getOperator(), conceptType.getDimcode());
			}
			conceptType.setDimcode(theData);
		}

		return conceptType;
	}


	protected ModifierType getModifierMetadataFromOntology(String modifierKey,
			String appliedPath) throws ConceptNotFoundException,
			OntologyException {
		ModifierType modifierType = null;
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);
			String getModifierOperationName = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_GETMODIFIERINFO_URL_PROPERTIES);
			String ontologyGetModifierInfoUrl = ontologyUrl
					+ getModifierOperationName;
			log.debug("Ontology getModifierinfo url from property file ["
					+ ontologyGetModifierInfoUrl + "]");

			modifierType = CallOntologyUtil.callGetModifierInfo(modifierKey,
					appliedPath, parent.getSecurityType(),
					parent.getProjectId(), ontologyGetModifierInfoUrl);

		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + modifierKey
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

		if (modifierType == null) {
			throw new ConceptNotFoundException(
					"Error getting modifierinfo for modifier key ["
							+ modifierKey + "] and appliedPath [" + appliedPath
							+ "]");

		} else {
			String theData = modifierType.getDimcode();
			if (modifierType.getColumndatatype() != null
					&& modifierType.getColumndatatype().equalsIgnoreCase("T")) {
				theData = SqlClauseUtil.handleMetaDataTextValue(
						modifierType.getOperator(), modifierType.getDimcode());
			} else if (modifierType.getColumndatatype() != null
					&& modifierType.getColumndatatype().equalsIgnoreCase("N")) {
				theData = SqlClauseUtil.handleMetaDataNumericValue(
						modifierType.getOperator(), modifierType.getDimcode());
			} else if (modifierType.getColumndatatype() != null
					&& modifierType.getColumndatatype().equalsIgnoreCase("D")) {
				theData = SqlClauseUtil.handleMetaDataDateValue(
						modifierType.getOperator(), modifierType.getDimcode());
			}
			modifierType.setDimcode(theData);
		}

		return modifierType;
	}

	protected String getPrimaryTableAlias(){
		String joinTableName = getJoinTable();

		String tableAlias = "f";
		if (joinTableName.equalsIgnoreCase("visit_dimension"))
			tableAlias = "e";
		else if (joinTableName.equalsIgnoreCase("patient_dimension"))
			tableAlias = "p";

		return tableAlias;
	}

	public ItemType getItemType() {
		return baseItem;
	}

	public Integer getConceptTotal() {
		return conceptTotal;
	}

	public boolean hasModiferConstraint(){
		ItemType.ConstrainByModifier modifierConstrain = baseItem.getConstrainByModifier();
		if (modifierConstrain != null) {
			return true;
		}
		else
			return false;
	}

	public boolean hasValueConstraint(){
		List<ConstrainByValue> valueConstrainList = baseItem.getConstrainByValue();
		if (valueConstrainList==null||valueConstrainList.size()==0)
			return false;
		else {
			for (ConstrainByValue valueConstrain : valueConstrainList){
				if (valueConstrain!=null&&valueConstrain.getValueType()!=null)
					return true;
			}
			return false;
		}
	}

	public boolean hasItemDateConstraint(){
		List<ConstrainByDate> constrainByDateList = baseItem.getConstrainByDate();
		if (constrainByDateList!=null){
			for (ConstrainByDate constrainByDate : constrainByDateList) {
				if (constrainByDate!=null){
					if (constrainByDate.getDateTo()!=null||constrainByDate.getDateFrom()!=null)
						return true;
				}
			}
		}
		return false;
	}

	public boolean hasPanelDateConstraint(){
		return parent.hasPanelDateConstraint();
	}

	public boolean hasPanelOccurrenceConstraint(){
		return parent.hasPanelOccurrenceConstraint();
	}

	public boolean returnEncounterNum(){
		if (parent.getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME)||
				parent.getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAMEVISIT))
			return true;
		else
			return false;
	}

	public boolean returnInstanceNum(){
		if (parent.getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM))
			return true;
		else
			return false;		
	}

	public void addIgnoredMessage(String errorMessage) {
		parent.addIgnoredMessage(errorMessage);
	}

}
