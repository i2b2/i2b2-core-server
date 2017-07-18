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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.InvertedConstraintStrategy;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryOptions.QueryConstraintStrategy;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.datavo.setfinder.query.TotOccuranceOperatorType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Temporal Panel Object
 * 
 * <P>
 * Panel query object that wraps the panel tag found in the query definition
 * xml. It roughly corresponds to a panel in the query UI. Panel is responsible
 * for organizing the sql that comes back from individual panel items - sql from
 * items should be logically or'd together. It is also the container that holds
 * the panel constraint types - occurrence, dates, and exclude.
 * 
 * @author Christopher Herrick
 * 
 */
public class TemporalPanel implements Comparable<Object> {

	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * Max Timing Score - max score a panel can get when ordering based on items
	 * and timing
	 */
	private final int MAXTIMINGSCORE = 5;

	private TemporalSubQuery parent;
	private PanelType basePanel;
	private int estimatedPanelSize = 0;
	private List<TemporalPanelItem> panelItemList = null;
	private int missingItemTotals = 0;

	protected class TemporalPanelItemSql{
		protected String itemSql = null;
		protected String factTable = null;
		protected String joinTable = null;
		protected boolean valueConstraint = false;
		protected boolean textConstraint = false;
	}
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            reference to the temporal panel group to which this panel
	 *            belongs
	 * @param panel
	 *            i2b2 instantiation of the panel xml object found in the query
	 *            definition
	 * @throws I2B2Exception
	 *             thrown when an i2b2 specific error is found
	 */
	public TemporalPanel(TemporalSubQuery parent, PanelType panel)
			throws I2B2Exception {
		this.parent = parent;
		this.basePanel = panel;

		parsePanel();
	}

	/**
	 * Parse Panel
	 * 
	 * Parses through the paneltype object passed in through the constructor.
	 * Evaluates each panel item and casts them to the appropriate type.
	 * Calculates the estimated return size of the times in the panel which will
	 * then be used when sorting panel items
	 * 
	 * @throws I2B2Exception
	 *             thrown when an i2b2 specific error is found
	 */
	private void parsePanel() throws I2B2Exception {
		panelItemList = new ArrayList<TemporalPanelItem>();

		List<ItemType> itemList = basePanel.getItem();

		for (ItemType itemType : itemList) {
			TemporalPanelItem panelItem = null;
			try{
				if (itemType.getItemKey().toLowerCase()
						.startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_SET)) {
					panelItem = new TemporalPanelPatientSetItem(this, itemType);
				} else if (itemType.getItemKey().toLowerCase()
						.startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_ENCOUNTER_SET)) {
					panelItem = new TemporalPanelPatientEncounterSetItem(this,
							itemType);
				} else if (itemType.getItemKey().toLowerCase()
						.startsWith(ItemKeyUtil.ITEM_KEY_MASTERID)) {
					panelItem = new TemporalPanelEmbeddedQueryItem(this, itemType);
				} else if (itemType.getItemKey().toLowerCase()
						.startsWith(ItemKeyUtil.ITEM_KEY_PATIENT)) {
					panelItem = new TemporalPanelPatientItem(this, itemType);
				} else if (itemType.getItemKey().toLowerCase()
						.startsWith(ItemKeyUtil.ITEM_KEY_ENCOUNTER)) {
					panelItem = new TemporalPanelEncounterItem(this, itemType);
				} else {
					panelItem = new TemporalPanelConceptItem(this, itemType);
					if(panelItem.getConceptType() == null)
						panelItem.getConceptType();
					if (panelItem != null
							&& panelItem.getConceptType() != null
							&& panelItem.getConceptType().getDimcode() != null
							&& panelItem.getConceptType().getDimcode()
									.toLowerCase().trim()
									.startsWith(ItemKeyUtil.ITEM_KEY_CELLID)) {
						panelItem = new TemporalPanelCellQueryItem(this, itemType,
								panelItem.getConceptType());
							
					}
					/*
					 * check for derived table parameter and look for other views for this item.
					 * ...  i.e. item is found in multiple views.
					 * ... if others found, then add them to the panel item list individually.
					 */		
					if (this.parent.getQueryOptions()!=null&&this.parent.getQueryOptions().useDerivedFactTable()) 
					 {
						 if(panelItem.getConceptType().getFacttablecolumn().contains("."))
						 {
							 String baseItemFactColumn = panelItem.getConceptType().getFacttablecolumn();
							 DerivedFactColumnsType columns = getFactColumnsFromOntologyCell(itemType.getItemKey());
							 if(columns.getDerivedFactTableColumn().size() > 1) {

								 for (String column : columns.getDerivedFactTableColumn()) {
									 // look for non-null fact table columns that are not equal to the base item's column
									 if((column != null) && !(column.equals(baseItemFactColumn))){
										 if(column.contains(".")){
											 TemporalPanelItem	derivedPanelItem = new TemporalPanelConceptItem(this, itemType);
											 if(derivedPanelItem.getConceptType() == null)
												 derivedPanelItem.getConceptType();

											 if (derivedPanelItem != null
													 && derivedPanelItem.getConceptType() != null
													 && derivedPanelItem.getConceptType().getDimcode() != null
													 && derivedPanelItem.getConceptType().getDimcode()
													 .toLowerCase().trim()
													 .startsWith(ItemKeyUtil.ITEM_KEY_CELLID)) {
												 derivedPanelItem = new TemporalPanelCellQueryItem(this, itemType,
														 derivedPanelItem.getConceptType());
											 }
											 log.debug("setting a new fact column: " + column);
											 derivedPanelItem.parseFactColumn(column);
											 panelItemList.add(derivedPanelItem);
										 }
									 }
								 }
							 }

						 }
					 }
				}
				Integer conceptTotal = panelItem.getConceptTotal();
				if (conceptTotal != null) {
					estimatedPanelSize += conceptTotal;
				} else
					missingItemTotals++;
				panelItemList.add(panelItem);

				
			}
			catch (ConceptNotFoundException ce){
				log.debug("Concept not found error: " + ce.getMessage());
				parent.addIgnoredMessage(ce.getMessage() + " panel#" + parent.getPanelIndex(this));
			}
		}
	}

	protected DerivedFactColumnsType getFactColumnsFromOntologyCell(String itemKey)
			throws ConceptNotFoundException, OntologyException {
		DerivedFactColumnsType factColumns = new DerivedFactColumnsType();
		try {
			
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);

			factColumns = CallOntologyUtil.callGetFactColumns(itemKey,
					parent.getSecurityType(), parent.getProjectId(),
					ontologyUrl +"/getDerivedFactColumns");
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

//		if (factColumns.isEmpty()) {
//			throw new ConceptNotFoundException("[" + itemKey + "] ");

//		} 

		return factColumns;
	}

	/**
	 * Build Sql
	 * 
	 * Main method for generating the sql string that will be run on the
	 * database for this panel item. Iterates through each item contained in the
	 * panel and appends sql together to form sql string for all items in the
	 * panel
	 * 
	 * @param currentIndex
	 *            int value that represents the panel position in the query
	 * @return String sql representation of the items in the query
	 * @throws I2B2DAOException
	 *             thrown when a data related i2b2 issue is encountered
	 */
	public String buildSql(int currentIndex) throws I2B2DAOException {
		boolean firstPanel = this.isFirstPanelInQuery();
		StringBuilder panelSqlBuffer = new StringBuilder();

		boolean addDelimiter = false;
		// OMOP WAS...
		//		List<String> itemSqlList = getItemSql();
		List<TemporalPanelItemSql> itemSqlList = getItemSql();
		if (itemSqlList!=null&&itemSqlList.size()>0){
			if (this.hasPanelOccurrenceConstraint()
					&& this.applyOccurrenceToPanelLevel()
					&& (this.getItemList().size() > 1 || !this.isPatientOnlyQuery())) {
				if (firstPanel) {
					panelSqlBuffer
							.append(firstPanelItemSqlWithOccurrence(itemSqlList));
				} else {
					panelSqlBuffer.append(nonFirstPanelItemSqlWithOccurrence(
							currentIndex, itemSqlList));
				}
			} else if (firstPanel && this.isPanelInverted()) {
				panelSqlBuffer.append(buildFirstPanelInvertSql(itemSqlList));
			} else {
				if (firstPanel) {
					panelSqlBuffer.append(firstPanelItemSql(itemSqlList));
				} else {
					panelSqlBuffer.append(nonFirstPanelItemSql(itemSqlList,
							currentIndex));
				}
			}
		}
		else if (this.isPanelInverted()){
			//no items and inverted panel means this is a get everyone query
			
			String schema = getDatabaseSchema();
			if (schema == null)
				schema = "";
			else if (!schema.endsWith("."))
				schema += ".";
			
			panelSqlBuffer.append("insert into " + parent.getTempTableName() + " (patient_num, panel_count ) " + 
					"select distinct patient_num, 0 from " + schema + "patient_dimension pat ");
			
		}

		return panelSqlBuffer.toString();
	}

	/**
	 * Get Item Sql
	 * 
	 * Gets the sql for each of the items in the panel and returns the results
	 * in an List
	 * 
	 * @return List<String> list of sql statements for all the times contained in the panel
	 * @throws I2B2DAOException
	 */
	//OMOP WAS..
/*	private List<String> getItemSql() throws I2B2DAOException {
		List<String> itemList = null;
		if (panelItemList.size() > 0) {
			itemList = new ArrayList<String>(panelItemList.size());
			for (TemporalPanelItem item : panelItemList) {
				try {
					itemList.add(item.buildSql());
				}
				catch (ConceptNotFoundException ce){
					parent.addIgnoredMessage(ce.getMessage() + " panel#" + parent.getPanelIndex(this));
				}
			}
		}
		return itemList;
	}
	*/
	private List<TemporalPanelItemSql> getItemSql() throws I2B2DAOException {
		List<TemporalPanelItemSql> itemList = null;
		if (panelItemList.size() > 0) {
			itemList = new ArrayList<TemporalPanelItemSql>(panelItemList.size());
			for (TemporalPanelItem item : panelItemList) {
				try {
					TemporalPanelItemSql itemSql = new TemporalPanelItemSql();
					itemSql.itemSql = item.buildSql();
					itemSql.factTable = item.factTable;
					itemSql.joinTable = item.tableName;
					itemList.add(itemSql);
				}
				catch (ConceptNotFoundException ce){
					parent.addIgnoredMessage(ce.getMessage() + " panel#" + parent.getPanelIndex(this));
				}
			}
		}
		return itemList;
	}

	/**
	 * Union Item Sql
	 * 
	 * Takes the list of individual sql statements and constructs one sql statement by unioning
	 * individual statements together. Submitted statements are assumed to have the same select list
	 * 
	 * @param itemSqlList List of individual sql statements
	 * @return String one sql statement that unions individual statement together
	 */
	//OMOP WAS..
	//	private String unionItemSql(List<String> itemSqlList) {
	private String unionItemSql(List<TemporalPanelItemSql> itemSqlList) {
		StringBuilder unionSql = new StringBuilder();

		boolean first = true;
		// OMOP WAS...
		//for (String itemSql : itemSqlList) {
		for (TemporalPanelItemSql itemSqlItem : itemSqlList) {
			String itemSql = itemSqlItem.itemSql;
			if (!first)
				unionSql.append("\n union all \n");
			else
				first = false;

			unionSql.append(itemSql);
		}

		return unionSql.toString();
	}

	/**
	 * Consolidate Item Sql
	 * 
	 * Takes in list of individual sql statements and combines them by consolidating statements with the same from clause
	 * into one statement with multiple constraints. Statements with different from clauses are unioned together
	 * 
	 * @param itemSql List of individual sql statements
	 * @return String one sql statement that combines all statements from the individual list
	 */
	//OMOP WAS..
//	private String consolidateItemSql(List<String> itemSql) {
	private String consolidateItemSql(List<TemporalPanelItemSql> itemSqlList) {	
		StringBuilder itemSqlBuffer = new StringBuilder();

		HashMap<String, List<TemporalQuerySimpleSqlParser>> tableMatch = new HashMap<String, List<TemporalQuerySimpleSqlParser>>();
		int index = 0;
		//OMOP WAS..
		//		for (String sql : itemSql) {
		for (TemporalPanelItemSql itemSql : itemSqlList) {
			String sql = itemSql.itemSql;
			TemporalQuerySimpleSqlParser simpleSql = new TemporalQuerySimpleSqlParser(
					sql);

			String selectClause = simpleSql.getSelectClause();
			String fromClause = simpleSql.getFromClause();
			String groupByClause = simpleSql.getGroupByClause();
			String havingClause = simpleSql.getHavingClause();

			String consolidatedKey = selectClause
					+ "|"
					+ fromClause
					+ "|"
					+ (groupByClause != null
							&& groupByClause.trim().length() > 0 ? "|"
							+ groupByClause : "");

			if (havingClause != null && havingClause.trim().length() > 0) {
				consolidatedKey += "|" + String.valueOf(index);
			}

			List<TemporalQuerySimpleSqlParser> sqlList = null;
			if (tableMatch.containsKey(consolidatedKey)) {
				sqlList = tableMatch.get(consolidatedKey);
			} else {
				sqlList = new ArrayList<TemporalQuerySimpleSqlParser>();
			}

			sqlList.add(simpleSql);
			tableMatch.put(consolidatedKey, sqlList);
			index++;
		}

		boolean firstKey = true;
		for (String consolidatedKey : tableMatch.keySet()) {
			List<TemporalQuerySimpleSqlParser> sqlList = tableMatch
					.get(consolidatedKey);

			boolean firstItem = true;

			String selectClause = "";
			String fromClause = "";
			String whereClause = "";
			String groupByClause = "";
			String havingClause = "";
			for (TemporalQuerySimpleSqlParser simpleSql : sqlList) {
				if (firstItem) {
					selectClause = "select " + simpleSql.getSelectClause()
							+ " ";
					fromClause = "from " + simpleSql.getFromClause() + " ";
					if (simpleSql.getGroupByClause() != null
							&& simpleSql.getGroupByClause().trim().length() > 0)
						groupByClause = "group by "
								+ simpleSql.getGroupByClause() + " ";
					if (simpleSql.getHavingClause() != null
							&& simpleSql.getHavingClause().trim().length() > 0)
						havingClause = "having " + simpleSql.getHavingClause()
								+ " ";
				}

				if (simpleSql.getWhereClause() != null
						&& simpleSql.getWhereClause().trim().length() > 0) {
					if (whereClause.length() == 0) {
						whereClause = "where (" + simpleSql.getWhereClause()
								+ ") ";
					} else {
						whereClause += "or (" + simpleSql.getWhereClause()
								+ ") ";
					}
				}

				firstItem = false;
			}

			if (!firstKey) {
				itemSqlBuffer.append("\nunion all \n");
			}

			itemSqlBuffer.append(selectClause);
			itemSqlBuffer.append(fromClause);
			if (whereClause != null && whereClause.trim().length() > 0)
				itemSqlBuffer.append(whereClause);
			if (groupByClause != null && groupByClause.trim().length() > 0)
				itemSqlBuffer.append(groupByClause);
			if (havingClause != null && havingClause.trim().length() > 0)
				itemSqlBuffer.append(havingClause);

			firstKey = false;
		}

		return itemSqlBuffer.toString();
	}

	/**
	 * Build Item Union Sql
	 * 
	 * Used to return the proper sql syntax for unioning two sql statements
	 * together
	 * 
	 * @return String with sql building block used to union to sql statements
	 *         together
	 * @throws I2B2DAOException
	 *             thrown when an i2b2 related data error is encountered
	 */
	private String buildItemUnionSql() throws I2B2DAOException {
		StringBuilder itemSqlBuffer = new StringBuilder();

		int currentIndex = 0;
		for (TemporalPanelItem item : panelItemList) {
			String itemSql = item.buildSql();
			if (currentIndex > 0) {
				itemSqlBuffer.append("\n union all \n");
			}

			itemSqlBuffer.append(itemSql);
			currentIndex++;
		}

		return itemSqlBuffer.toString();
	}

	/**
	 * First Panel Item Sql
	 * 
	 * Processes an item from the first panel of a panel group. First panel
	 * items are processed as an insert statement into a temporary table rather
	 * than an update
	 * 
	 * @param itemSqlList
	 *           List of individual sql statements found in this panel
	 * @return String sql representation that joins the item sql to the panel
	 *         sql
	 */
	//OMOP WAS...
	//	private String firstPanelItemSql(List<String> itemSqlList) {
	private String firstPanelItemSql(List<TemporalPanelItemSql> itemSqlList) {
		StringBuilder panelSql = new StringBuilder();
		boolean addDelimiter = false;

		//OMOP WAS..
		//	for (String itemSql : itemSqlList) {
		for (TemporalPanelItemSql itemSqlItem : itemSqlList) {
			String itemSql = itemSqlItem.itemSql;
			String insertValuesClause = buildInsertValuesClause();

			StringBuilder withItemSql = new StringBuilder();

			String firstPanelItemSql = "";

			String innerSelectClause = buildInnerSelectClause();
			String innerGroupByClause = buildInnerGroupByClause();

			String itemStatement = "";
			StringBuilder tableStatement = new StringBuilder(itemSql);

			boolean useTempTables = false;
			if (parent.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)
					&& parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.TEMP_TABLES) {
				useTempTables = true;
			}

			// String suffix = getPanelId();
			String suffix = "";

			String tSelect = "select " + innerSelectClause + " " + "from "
					+ (useTempTables ? "#t" + suffix + " " : "") + "t ";

			String schema = getDatabaseSchema();
			if (schema == null)
				schema = "";
			else if (!schema.endsWith("."))
				schema += ".";

			if (getPanelTiming().equals(QueryTimingHandler.ANY)) {
				// this means there are negation panels behind this one
				if (returnInstanceToParent()) {
					itemStatement = itemSql;
					innerSelectClause = buildInnerSelectClause("f");
					innerGroupByClause = buildInnerGroupByClause("f");
					tableStatement = new StringBuilder();
					tableStatement.append("select " + innerSelectClause + " "
							+ "from " + schema + "observation_fact f, ");
					if (useTempTables) {
						tableStatement.append("#i" + suffix + " ");
					} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
						tableStatement.append("(" + itemSql + ") ");
					}
					tableStatement
					.append(" i where i.patient_num = f.patient_num ");
					if (innerGroupByClause != null
							&& innerGroupByClause.trim().length() > 0
							&& parent.getQueryOptions().useItemGroupByStatement()) {
						tableStatement.append("group by " + innerGroupByClause);
					}
				} else if (returnEncounterToParent()) {
					itemStatement = itemSql;
					innerSelectClause = buildInnerSelectClause("v");
					innerGroupByClause = buildInnerGroupByClause("v");
					tableStatement = new StringBuilder();
					tableStatement.append("select " + innerSelectClause + " "
							+ "from " + schema + "visit_dimension v, ");
					if (useTempTables) {
						tableStatement.append("#i" + suffix + " ");
					} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
						tableStatement.append("(" + itemSql + ") ");
					}
					tableStatement
					.append(" i where i.patient_num = v.patient_num ");
					if (innerGroupByClause != null
							&& innerGroupByClause.trim().length() > 0
							&& parent.getQueryOptions().useItemGroupByStatement()) {
						tableStatement.append("group by " + innerGroupByClause);
					}
				}
			} else if (getPanelTiming().equals(QueryTimingHandler.SAME)
					|| getPanelTiming().equals(QueryTimingHandler.SAMEVISIT)) {
				if (returnInstanceToParent()) {
					itemStatement = itemSql;
					innerSelectClause = buildInnerSelectClause("f");
					innerGroupByClause = buildInnerGroupByClause("f");
					tableStatement = new StringBuilder();
					tableStatement.append("select " + innerSelectClause + " "
							+ "from " + schema + "observation_fact f, ");
					if (useTempTables) {
						tableStatement.append("#i" + suffix + " ");
					} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
						tableStatement.append("(" + itemSql + ") ");
					}
					tableStatement.append(" i "
							+ "where i.patient_num = f.patient_num "
							+ "and i.encounter_num = f.encounter_num ");
					if (innerGroupByClause != null
							&& innerGroupByClause.trim().length() > 0
							&& parent.getQueryOptions().useItemGroupByStatement()) {
						tableStatement.append("group by " + innerGroupByClause);
					}
				}
			}

			if (useTempTables) {
				if (itemStatement != null
						&& itemStatement.trim().length() > 0) {
					withItemSql
							.append(parent.buildTempTableCheckDrop("i" + suffix));
					withItemSql.append(parent.getSqlDelimiter());
					withItemSql.append(buildSelectIntoStatement(
							itemStatement, "i" + suffix));
					withItemSql.append(parent.getSqlDelimiter());
					withItemSql
							.append(parent.buildTempTableCheckDrop("t" + suffix));
					withItemSql.append(parent.getSqlDelimiter());
					withItemSql.append(buildSelectIntoStatement(
							tableStatement.toString(), "t" + suffix));
					withItemSql.append(parent.getSqlDelimiter());
					withItemSql.append("drop table #i" + suffix);
					withItemSql.append(parent.getSqlDelimiter());
				} else {
					withItemSql
							.append(parent.buildTempTableCheckDrop("t" + suffix));
					withItemSql.append(parent.getSqlDelimiter());
					withItemSql.append(buildSelectIntoStatement(
							tableStatement.toString(), "t" + suffix));
					withItemSql.append(parent.getSqlDelimiter());
				}
			} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
				withItemSql = new StringBuilder();
				innerSelectClause = buildInnerSelectClause();
				tSelect = "select " + innerSelectClause + " " + "from ("
						+ tableStatement.toString() + ") t ";
			} else {
				if (itemStatement != null
						&& itemStatement.trim().length() > 0) {
					withItemSql.append("with i as ( " + "\n"
							+ itemStatement + "\n" + " ) " + "\n");
					withItemSql.append(", t as ( " + "\n"
							+ tableStatement.toString() + "\n" + " ) "
							+ "\n");
				} else {
					withItemSql.append("with t as ( " + "\n"
							+ tableStatement.toString() + "\n" + " ) "
							+ "\n");
				}
			}

			if (parent.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE) || parent.getServerType()
					.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				firstPanelItemSql = "insert into "
						+ parent.getTempTableName() + " ("
						+ insertValuesClause + ")" + "\n"
						+ withItemSql.toString() + tSelect;
			} else {
				firstPanelItemSql = withItemSql.toString() + "insert into "
						+ parent.getTempTableName() + " ("
						+ insertValuesClause + ")" + "\n" + tSelect;
			}

			if (useTempTables) {
				firstPanelItemSql += parent.getSqlDelimiter();
				firstPanelItemSql += "drop table #t" + suffix + "";
			}

			if (addDelimiter) {
				panelSql.append(parent.getSqlDelimiter());
			}

			panelSql.append(firstPanelItemSql);

			addDelimiter = true;
		}

		return panelSql.toString();
	}

	/**
	 * Build Select Into Statement
	 * 
	 * Takes a sql statement and creates sql statement that selects the results into a temporary table. This method should only be used on
	 * Sql Server or other database that supports the select into sybntax
	 * 
	 * @param sqlStatement String that contains the sql statements to be saved into a temporary table
	 * @param tempTableName String that contains the name of the temporary table
	 * @return String properly formated sql statement that stores the result of the select statement into a temporary table
	 */
	private String buildSelectIntoStatement(String sqlStatement,
			String tempTableName) {
		TemporalQuerySimpleSqlParser sqlParser = new TemporalQuerySimpleSqlParser(
				sqlStatement);
		String select = sqlParser.getSelectClause();
		String into = " into #" + tempTableName;
		String from = sqlParser.getFromClause();
		String where = sqlParser.getWhereClause();
		String groupBy = sqlParser.getGroupByClause();
		String having = sqlParser.getHavingClause();
		String orderBy = sqlParser.getOrderByClause();
		String selectIntoSql = "select "
				+ select
				+ " \n"
				+ into
				+ " \n"
				+ "from "
				+ from
				+ " \n"
				+ (where != null && where.trim().length() > 0 ? "where "
						+ where + " \n" : "")
				+ (groupBy != null && groupBy.trim().length() > 0 ? "group by "
						+ groupBy + " \n" : "")
				+ (having != null && having.trim().length() > 0 ? "having "
						+ having + " \n" : "")
				+ (orderBy != null && orderBy.trim().length() > 0 ? "order by "
						+ orderBy + " \n" : "");
		return selectIntoSql;
	}

	/**
	 * Build First Panel Invert Sql
	 * 
	 * Take in the list of individual sql statements from this panel and creates a sql statement that is inverted. Current invert options are 
	 * limited to either using a minus/except syntax or a not exists/not in syntax.  
	 * 
	 * @param itemSqlList List of individual sql statements for all items in this panel
	 * @return String sql statement that applies invert clause to all items in first panel
	 */
		//OMOP WAS...
	//	private String buildFirstPanelInvertSql(List<String> itemSqlList) {
		private String buildFirstPanelInvertSql(List<TemporalPanelItemSql> itemSqlList) {

		String insertValuesClause = buildInsertValuesClause();
		StringBuilder withItemSql = new StringBuilder();
		String invertSql = "";
		String replaceString = "<!***PLACEHOLDER****!>";

		String innerSelectClause = buildInnerSelectClause();
		String innerGroupByClause = buildInnerGroupByClause();
		String itemSql = unionItemSql(itemSqlList);
		String itemStatement = "";
		StringBuilder tableStatement = new StringBuilder(itemSql);
		
		List<String> factTables = null;
		if (this.parent.getQueryOptions()!=null&&this.parent.getQueryOptions().useDerivedFactTable()) 
		 {
			factTables = buildFactTableList(itemSqlList);
		 }
		

		boolean useTempTables = false;
		if (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)
				&& parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.TEMP_TABLES) {
			useTempTables = true;
		}

		String schema = getDatabaseSchema();
		if (schema == null)
			schema = "";
		else if (!schema.endsWith("."))
			schema += ".";

		// String suffix = getPanelId();
		String suffix = "";

		String tSelect = "select " + innerSelectClause + " " + "from "
				+ (useTempTables ? "#t" + suffix + " " : "") + " t";

		if (getPanelTiming().equals(QueryTimingHandler.ANY)) {
			// this means there are negation panels behind this one
			if (returnInstanceToParent()) {
				itemStatement = itemSql;
				innerSelectClause = buildInnerSelectClause("f");
				innerGroupByClause = buildInnerGroupByClause("f");
				tableStatement = new StringBuilder();
				if(factTables == null){
					tableStatement.append("select " + innerSelectClause + " "
						+ "from " + schema + "observation_fact f, ");
				}
				else{
					if(factTables.size() == 1) {
						tableStatement.append("select " + innerSelectClause + " "
								+ "from " + schema + factTables.get(0) +" f, ");
					}
					else {
						Iterator i = factTables.iterator();
						while (i.hasNext()){
							tableStatement.append("select " + innerSelectClause + " "
									+ "from " + schema + i.next() +" f, ");
							if(i.hasNext()){
								tableStatement.append(" union all ");
							}
						}
					}
				}
				
				if (useTempTables) {
					tableStatement.append("#i" + suffix + " ");
				} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
					tableStatement.append("(" + replaceString + ") ");
				}
				tableStatement.append(" i "
						+ "where i.patient_num = f.patient_num ");
				if (innerGroupByClause != null
						&& innerGroupByClause.trim().length() > 0
						&& parent.getQueryOptions().useItemGroupByStatement()) {
					tableStatement.append("group by " + innerGroupByClause);
				}
			} else if (returnEncounterToParent()) {
				itemStatement = itemSql;
				innerSelectClause = buildInnerSelectClause("v");
				innerGroupByClause = buildInnerGroupByClause("v");
				tableStatement = new StringBuilder();
				tableStatement.append("select " + innerSelectClause + " "
						+ "from " + schema + "visit_dimension v, ");
				if (useTempTables) {
					tableStatement.append("#i" + suffix + " ");
				} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
					tableStatement.append("(" + replaceString + ") ");
				}
				tableStatement.append(" i "
						+ "where i.patient_num = v.patient_num ");
				if (innerGroupByClause != null
						&& innerGroupByClause.trim().length() > 0
						&& parent.getQueryOptions().useItemGroupByStatement()) {
					tableStatement.append("group by " + innerGroupByClause);
				}
			}
		} else if (getPanelTiming().equals(QueryTimingHandler.SAME)
				|| getPanelTiming().equals(QueryTimingHandler.SAMEVISIT)) {
			if (returnInstanceToParent()) {
				itemStatement = itemSql;
				innerSelectClause = buildInnerSelectClause("f");
				innerGroupByClause = buildInnerGroupByClause("f");
				tableStatement = new StringBuilder();
				if(factTables == null){
					tableStatement.append("select " + innerSelectClause + " "
						+ "from " + schema + "observation_fact f, ");
				}
				else{
					if(factTables.size() == 1) {
						tableStatement.append("select " + innerSelectClause + " "
								+ "from " + schema + factTables.get(0) +" f, ");
					}
					else {
						Iterator i = factTables.iterator();
						while (i.hasNext()){
							tableStatement.append("select " + innerSelectClause + " "
									+ "from " + schema + i.next() +" f, ");
							if(i.hasNext()){
								tableStatement.append(" union all ");
							}
						}
					}
				}

				if (useTempTables) {
					tableStatement.append("#i" + suffix + " ");
				} else if (parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.DERIVED_TABLES) {
					tableStatement.append("(" + replaceString + ") ");
				}
				tableStatement.append(" i "
						+ "where i.patient_num = f.patient_num "
						+ "and i.encounter_num = f.encounter_num ");
				if (innerGroupByClause != null
						&& innerGroupByClause.trim().length() > 0
						&& parent.getQueryOptions().useItemGroupByStatement()) {
					tableStatement.append("group by " + innerGroupByClause);
				}
			}
		}

		if (useTempTables) {
			if (itemStatement != null && itemStatement.trim().length() > 0) {
				withItemSql.append(parent.buildTempTableCheckDrop("y" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append(buildSelectIntoStatement(itemStatement, "y"
						+ suffix));
				withItemSql.append(parent.getSqlDelimiter());

				String invertClause = "";
				if (parent.getQueryOptions().getInvertedConstraintLogic()==InvertedConstraintStrategy.MINUS_CLAUSE){
					invertClause = buildInvertExceptSql("#y" + suffix);
				}
				else {
					invertClause = buildInvertNotExistsSql("#y" + suffix);					
				}
				withItemSql.append(parent.buildTempTableCheckDrop("i" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append(buildSelectIntoStatement(invertClause, "i"
						+ suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append("drop table #y" + suffix + "");
				withItemSql.append(parent.getSqlDelimiter());

				withItemSql.append(parent.buildTempTableCheckDrop("t" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append(buildSelectIntoStatement(
						tableStatement.toString(), "t" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append("drop table #i" + suffix);
				withItemSql.append(parent.getSqlDelimiter());
			} else {

				withItemSql.append(parent.buildTempTableCheckDrop("y" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append(buildSelectIntoStatement(itemSql, "y"
						+ suffix));
				withItemSql.append(parent.getSqlDelimiter());

				String invertClause = "";
				if (parent.getQueryOptions().getInvertedConstraintLogic()==InvertedConstraintStrategy.MINUS_CLAUSE){
					invertClause = buildInvertExceptSql("#y" + suffix);
				}
				else {
					invertClause = buildInvertNotExistsSql("#y" + suffix);
				}
				withItemSql.append(parent.buildTempTableCheckDrop("i" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append(buildSelectIntoStatement(invertClause, "i"
						+ suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append("drop table #y" + suffix);
				withItemSql.append(parent.getSqlDelimiter());

				String invertInsertSql = buildInvertInsertSelectSql("i");
				withItemSql.append(parent.buildTempTableCheckDrop("t" + suffix));
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql
						.append("select " + invertInsertSql + " " + "into #t"
								+ suffix + " " + "from #i" + suffix + " i ");
				withItemSql.append(parent.getSqlDelimiter());
				withItemSql.append("drop table #i" + suffix);
				withItemSql.append(parent.getSqlDelimiter());
			}

			invertSql = withItemSql.toString() + "insert into "
					+ parent.getTempTableName() + " (" + insertValuesClause
					+ ")" + "\n" + tSelect + parent.getSqlDelimiter()
					+ "drop table #t" + suffix;
		} else if (parent.getQueryOptions().getQueryConstraintLogic()==QueryConstraintStrategy.DERIVED_TABLES){
			if (itemStatement != null && itemStatement.trim().length() > 0) {
				String invertClause = "";
				if (parent.getQueryOptions().getInvertedConstraintLogic()==InvertedConstraintStrategy.MINUS_CLAUSE){
					invertClause = buildInvertExceptSql("y");
				}
				else {
					invertClause = buildInvertNotExistsSql(itemStatement, "y");
				}
				String derivedSql = tableStatement.toString().replace(replaceString, invertClause);
				withItemSql.append(derivedSql);
			} else {
				String invertClause = "";
				if (parent.getQueryOptions().getInvertedConstraintLogic()==InvertedConstraintStrategy.MINUS_CLAUSE){
					invertClause = buildInvertExceptSql("y");
				}
				else {
					invertClause = buildInvertNotExistsSql(itemSql, "y");
				}
				String invertInsertSql = buildInvertInsertSelectSql("i");
				withItemSql.append(" select " +
						invertInsertSql + " from (" + 
						invertClause + 
						") i \n");
			}
			
			innerSelectClause = buildInnerSelectClause();
			tSelect = "select " + innerSelectClause + " " + "from ("
					+ withItemSql.toString() + ") t ";

			if (parent.getServerType()
					.equalsIgnoreCase(DAOFactoryHelper.ORACLE) || parent.getServerType()
					.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				invertSql = "insert into " + parent.getTempTableName() + " ("
						+ insertValuesClause + ")" + "\n"
						+ tSelect;
			} else {
				invertSql = "insert into "
						+ parent.getTempTableName() + " (" + insertValuesClause
						+ ")" + "\n" + tSelect;
			}
		} else {
			if (itemStatement != null && itemStatement.trim().length() > 0) {
				withItemSql.append("with y as ( " + "\n" + itemStatement + "\n"
						+ " ) " + "\n");
	
				String invertClause = "";
				if (parent.getQueryOptions().getInvertedConstraintLogic()==InvertedConstraintStrategy.MINUS_CLAUSE){
					invertClause = buildInvertExceptSql("y");
				}
				else {
					invertClause = buildInvertNotExistsSql("y");
				}
				withItemSql.append(", i as ( " + "\n" + invertClause + "\n"
						+ " ) " + "\n");
				withItemSql.append(", t as ( " + "\n"
						+ tableStatement.toString() + "\n" + " ) " + "\n");
			} else {
				withItemSql.append("with y as ( " + "\n" + itemSql + "\n"
						+ " ) " + "\n");
				String invertClause = "";
				if (parent.getQueryOptions().getInvertedConstraintLogic()==InvertedConstraintStrategy.MINUS_CLAUSE){
					invertClause = buildInvertExceptSql("y");
				}
				else {
					invertClause = buildInvertNotExistsSql("y");
				}
				withItemSql.append(", i as ( " + "\n" + invertClause + "\n"
						+ " ) " + "\n");
				String invertInsertSql = buildInvertInsertSelectSql("i");
				withItemSql.append(", t as ( " + "\n" + " select "
						+ invertInsertSql + " from i " + "\n" + " ) " + "\n");
			}
	
			if (parent.getServerType()
					.equalsIgnoreCase(DAOFactoryHelper.ORACLE) || parent.getServerType()
					.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
				invertSql = "insert into " + parent.getTempTableName() + " ("
						+ insertValuesClause + ")" + "\n"
						+ withItemSql.toString() + tSelect;
			} else {
				invertSql = withItemSql.toString() + "insert into "
						+ parent.getTempTableName() + " (" + insertValuesClause
						+ ")" + "\n" + tSelect;
			}
	
		}

		return invertSql;
	}


	/**
	 * First Panel Item Sql With Occurrence
	 * 
	 * This method returns the sql representation of all items in a panel when
	 * the panel is the first panel in a query. This method is only accessed
	 * when usePanelLevelOccurrence flag is set to true - in which case the
	 * occurrence is processed on the panel level instead of in an item by item
	 * basis
	 * 
	 * @param itemSqlList List of individual sql statements for all items in this panel
	 * @return String sql representation for the first panel in the query
	 * @throws I2B2DAOException
	 *             thrown when an i2b2 data related error is encountered
	 */
		//OMOP WAS..
		//	private String firstPanelItemSqlWithOccurrence(List<String> itemSqlList)
	private String firstPanelItemSqlWithOccurrence(List<TemporalPanelItemSql> itemSqlList)
			throws I2B2DAOException {

		String itemSql = consolidateItemSql(itemSqlList);

		String insertValuesClause = buildInsertValuesClause();

		String innerSelectClause = buildInnerSelectClause("");
		String innerGroupByClause = buildInnerGroupByClause();

		TotalItemOccurrenceHandler totalItemOccurrencHandler = new TotalItemOccurrenceHandler();
		String totalItemOccurrenceClause = totalItemOccurrencHandler
				.buildTotalItemOccurrenceClause(this.getTotalOccurrences());

		StringBuilder withItemSql = new StringBuilder();

		String firstPanelItemSql = "";

		String schema = getDatabaseSchema();
		if (schema == null)
			schema = "";
		else if (!schema.endsWith("."))
			schema += ".";

		String tSelect = "select " + innerSelectClause + " " + "from t";

		if (getPanelTiming().equals(QueryTimingHandler.ANY)) {
			// this means there are negation panels behind this one
			if (returnInstanceToParent()) {
				withItemSql
						.append("with sub_t as ( "
								+ "\n"
								+ itemSql
								+ "\n"
								+ " ), "
								+ "\n"
								+ "y as ("
								+ "select "
								+ innerSelectClause
								+ " "
								+ "from sub_t "
								+ "where patient_num in (select patient_num from sub_t group by patient_num having sum(fact_count) "
								+ totalItemOccurrenceClause + ") " + ") \n");

				innerSelectClause = buildInnerSelectClause("f");
				innerGroupByClause = buildInnerGroupByClause("f");
				withItemSql
						.append(", t as ("
								+ "\n"
								+ "select "
								+ innerSelectClause
								+ " "
								+ "from "
								+ schema
								+ "observation_fact f "
								+ "where exists ("
								+ "select 1 "
								+ "from y "
								+ "where y.patient_num = f.patient_num) "
								+ (innerGroupByClause != null
										&& innerGroupByClause.trim().length() > 0
										&& parent.getQueryOptions().useItemGroupByStatement() ? "group by "
										+ innerGroupByClause
										: "") + ") " + "\n");
			} else if (returnEncounterToParent()) {

				withItemSql
						.append("with sub_t as ( "
								+ "\n"
								+ itemSql
								+ "\n"
								+ " ), "
								+ "\n"
								+ "y as ("
								+ "select "
								+ innerSelectClause
								+ " "
								+ "from sub_t "
								+ "where patient_num in (select patient_num from sub_t group by patient_num having sum(fact_count) "
								+ totalItemOccurrenceClause + ") " + ") \n");

				innerSelectClause = buildInnerSelectClause("v");
				innerGroupByClause = buildInnerGroupByClause("v");
				withItemSql
						.append(", t as ("
								+ "\n"
								+ "select "
								+ innerSelectClause
								+ " "
								+ "from "
								+ schema
								+ "visit_dimension v "
								+ "where exists ("
								+ "select 1 "
								+ "from y "
								+ "where y.patient_num = v.patient_num) "
								+ (innerGroupByClause != null
										&& innerGroupByClause.trim().length() > 0
										&& parent.getQueryOptions().useItemGroupByStatement() ? "group by "
										+ innerGroupByClause
										: "") + ") " + "\n");
			}
		} else if (getPanelTiming().equals(QueryTimingHandler.SAME)
				|| getPanelTiming().equals(QueryTimingHandler.SAMEVISIT)) {
			if (returnInstanceToParent()) {
				withItemSql
						.append("with sub_t as ( "
								+ "\n"
								+ itemSql
								+ "\n"
								+ " ), "
								+ "\n"
								+ "y as ("
								+ "select "
								+ innerSelectClause
								+ " "
								+ "from sub_t "
								+ "where patient_num in (select patient_num from sub_t group by patient_num having sum(fact_count) "
								+ totalItemOccurrenceClause + ") " + ") \n");

				innerSelectClause = buildInnerSelectClause("f");
				innerGroupByClause = buildInnerGroupByClause("f");
				withItemSql
						.append(", t as ("
								+ "\n"
								+ "select "
								+ innerSelectClause
								+ " "
								+ "from "
								+ schema
								+ "observation_fact f "
								+ "where exists ("
								+ "select 1 "
								+ "from y "
								+ "where y.patient_num = f.patient_num "
								+ "and y.encounter_num = f.encounter_num) "
								+ (innerGroupByClause != null
										&& innerGroupByClause.trim().length() > 0
										&& parent.getQueryOptions().useItemGroupByStatement() ? "group by "
										+ innerGroupByClause
										: "") + ") " + "\n");
			}
		} else {
			withItemSql
					.append("with sub_t as ( "
							+ "\n"
							+ itemSql
							+ "\n"
							+ " ), "
							+ "\n"
							+ "t as ("
							+ "select "
							+ innerSelectClause
							+ " "
							+ "from sub_t "
							+ "where patient_num in (select patient_num from sub_t group by patient_num having sum(fact_count) "
							+ totalItemOccurrenceClause + ") " + ") \n");
		}

		if (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE) || parent.getServerType()
				.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
			firstPanelItemSql = "insert into " + parent.getTempTableName()
					+ " (" + insertValuesClause + ")" + "\n" + withItemSql
					+ tSelect;
		} else {
			firstPanelItemSql = withItemSql + "insert into "
					+ parent.getTempTableName() + " (" + insertValuesClause
					+ ")" + "\n" + tSelect;
		}

		return firstPanelItemSql;
	}

	/**
	 * Build Insert Values Clause
	 * 
	 * Returns a list of columns from the temporary table that will be populated
	 * in an insert
	 * 
	 * @return String sql list of columns from temporary table that will be
	 *         inserted into
	 */
	public String buildInsertValuesClause() {
		String insertValuesClause = "";

		if (returnInstanceToParent()
				|| getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEINSTANCENUM)) {
			insertValuesClause = "provider_id, start_date, concept_cd, instance_num, encounter_num,  patient_num";
		} else if (returnEncounterToParent()
				|| getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME)
				|| getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEVISIT)) {
			insertValuesClause = "encounter_num, patient_num";
		} else {
			insertValuesClause = "patient_num";
		}

		insertValuesClause += ", panel_count";

		return insertValuesClause;
	}

	/**
	 * Build Inner Select Clause
	 * 
	 * Returns a list of columns that will be inserted into the temporary table
	 * for each item
	 * 
	 * @return String sql statement that specifies the columns that should be
	 *         returned from the item sql
	 */
	public String buildInnerSelectClause() {
		return buildInnerSelectClause("t");
	}

	/**
	 * Build Inner Select Clause
	 * 
	 * Returns a list of columns that will be inserted into the temporary table
	 * for each item
	 * 
	 * @param tableAlias
	 *            String that specifies the table alias to use when building sql
	 *            string
	 * @return String sql statement that specifies the columns that should be
	 *         returned from the item sql
	 */
	public String buildInnerSelectClause(String tableAlias) {

		if (tableAlias != null && tableAlias.trim().length() > 0
				&& !tableAlias.trim().endsWith("."))
			tableAlias += ".";

		String innerSelectClause = " ";
		if (!isFirstPanelInQuery()) {
			innerSelectClause = " 1 as panel_count ";
		} else {
			if (returnInstanceToParent()
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAMEINSTANCENUM)) {
				innerSelectClause = "" + tableAlias + "provider_id, "
						+ tableAlias + "start_date, " + tableAlias
						+ "concept_cd, " + tableAlias + "instance_num, "
						+ tableAlias + "encounter_num, ";
			} else if (returnEncounterToParent()
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAME)
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAMEVISIT)) {
				innerSelectClause = "" + tableAlias + "encounter_num, ";
			}
			innerSelectClause += "" + tableAlias
					+ "patient_num, 0 as panel_count ";
		}
		return innerSelectClause;
	}

	/**
	 * Build Inner Group By Clause
	 * 
	 * Returns a list of columns that will be used to group the results of the panel sql statement
	 * 
	 * @return String sql statement that specifies the columns that should be used to group the results
	 */
	public String buildInnerGroupByClause() {
		return buildInnerGroupByClause("t");
	}

	/**
	 * Build Inner Group By Clause
	 * 
	 * Returns a list of columns that will be used to group the results of the panel sql statement
	 * 
	 * @param tableAlias
	 *            String that specifies the table alias to use when building sql
	 *            string
	 * @return String sql statement that specifies the columns that should be used to group the results
	 */
	public String buildInnerGroupByClause(String tableAlias) {

		if (tableAlias != null && tableAlias.trim().length() > 0
				&& !tableAlias.trim().endsWith("."))
			tableAlias += ".";

		String innerSelectClause = " ";
		if (!isFirstPanelInQuery()) {
			innerSelectClause = " 1 as panel_count ";
		} else {
			if (returnInstanceToParent()
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAMEINSTANCENUM)) {
				innerSelectClause = "" + tableAlias + "provider_id, "
						+ tableAlias + "start_date, " + tableAlias
						+ "concept_cd, " + tableAlias + "instance_num, "
						+ tableAlias + "encounter_num, ";
			} else if (returnEncounterToParent()
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAME)
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAMEVISIT)) {
				innerSelectClause = "" + tableAlias + "encounter_num, ";
			}
			innerSelectClause += "" + tableAlias + "patient_num ";
		}
		return innerSelectClause;
	}

	/**
	 * Non First Panel Item Sql With Occurrence
	 * 
	 * This method returns the sql representation of all items in a panel when
	 * the panel is not the first panel in a query. This method is only accessed
	 * when usePanelLevelOccurrence flag is set to true - in which case the
	 * occurrence is processed on the panel level instead of in an item by item
	 * basis
	 * 
	 * @param panelIndex
	 *            int update index for the given panel
	* @param itemSqlList 
	* 			  List of individual sql statements for all items in this panel
	 * @return String sql representation for updating temporary table with panel
	 *         information
	 * @throws I2B2DAOException
	 *             thrown when an i2b2 data related error is encountered
	 */
	private String nonFirstPanelItemSqlWithOccurrence(int panelIndex,
			List<TemporalPanelItemSql> itemSqlList) throws I2B2DAOException {	
		String encounterNumClause = " ", instanceNumClause = " ";
		String tempTableName = parent.getTempTableName();
		int oldPanelIndex = panelIndex - 1;

		if (getPanelTiming().equalsIgnoreCase(
				QueryTimingHandler.SAMEINSTANCENUM)) {
			instanceNumClause = " and  " // + parent.getDatabaseSchema()
					+ tempTableName
					+ ".encounter_num = t.encounter_num and "
					+ tempTableName
					+ ".instance_num = t.instance_num  and "
					+ tempTableName
					+ ".start_date = t.start_date  and "
					+ tempTableName
					+ ".concept_cd = t.concept_cd  and "
					+ tempTableName + ".provider_id = t.provider_id ";
		} else if (getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME)
				|| getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEVISIT)) {
			encounterNumClause = " and " + tempTableName
					+ ".encounter_num = t.encounter_num ";
		}

		String itemSql = consolidateItemSql(itemSqlList);

		String nonFirstPanelItemSql = "";

		if (this.isPanelInverted()) {
			oldPanelIndex = 0;
			panelIndex = -1;
		}

		TotalItemOccurrenceHandler totalItemOccurrencHandler = new TotalItemOccurrenceHandler();
		String totalItemOccurrenceClause = totalItemOccurrencHandler
				.buildTotalItemOccurrenceClause(this.getTotalOccurrences());

		String withItemSql = "with sub_t as ( " + "\n" + itemSql + "\n"
				+ " ) \n";

		if (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE) || parent.getServerType()
				.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
			nonFirstPanelItemSql += "update "
					+ tempTableName
					+ " set panel_count ="
					+ panelIndex
					+ " where "
					+ tempTableName
					+ ".panel_count =  "
					+ oldPanelIndex
					+ " and exists ( "
					+ withItemSql
					+ "select 1 "
					+ "from ("
					+ "select * "
					+ "from sub_t "
					+ "where patient_num in (select patient_num from sub_t group by patient_num having sum(fact_count) "
					+ totalItemOccurrenceClause + ") " + ") t " + "where "
					+ tempTableName + ".patient_num = t.patient_num "
					+ encounterNumClause + instanceNumClause + " ) ";
		} else {
			nonFirstPanelItemSql += withItemSql
					+ "update "
					+ tempTableName
					+ " set panel_count ="
					+ panelIndex
					+ " where "
					+ tempTableName
					+ ".panel_count =  "
					+ oldPanelIndex
					+ " and exists ( "
					+ "select 1 "
					+ "from ("
					+ "select * "
					+ "from sub_t "
					+ "where patient_num in (select patient_num from sub_t group by patient_num having sum(fact_count) "
					+ totalItemOccurrenceClause + ") " + ") t " + "where "
					+ tempTableName + ".patient_num = t.patient_num "
					+ encounterNumClause + instanceNumClause + " ) ";
		}

		return nonFirstPanelItemSql;

	}

	/**
	 * Non First Panel Item Sql
	 * 
	 * Processes item sql from all items in panel that is not the first panel in a subquery. 
	 * Non first panel items are processed as an update statement to the
	 * temporary table rather than an insert
	 * 
	 * @param itemSqlList List of individual sql statements for all items in this panel
	 * @param panelIndex
	 *            int update index for the given panel
	 * @return String sql representation that joins the item sql to the panel
	 *         sql
	 */
	private String nonFirstPanelItemSql(List<TemporalPanelItemSql> itemSqlList, int panelIndex) {

		StringBuilder panelSql = new StringBuilder();
		boolean addDelimiter = false;
		StringBuilder tempItemSql = new StringBuilder();
		
		boolean useTempTables = false;
		if (parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)
				&& parent.getQueryOptions().getQueryConstraintLogic() == QueryConstraintStrategy.TEMP_TABLES) {
			useTempTables = true;
		}

		// OMOP WAS..
		//for (String itemSql : itemSqlList) {
		for (TemporalPanelItemSql itemSqlItem : itemSqlList) {
			String itemSql = itemSqlItem.itemSql;

			String encounterNumClause = " ", instanceNumClause = " ";
			String tempTableName = parent.getTempTableName();
			int oldPanelIndex = panelIndex - 1;

			if (getPanelTiming().equalsIgnoreCase(
					QueryTimingHandler.SAMEINSTANCENUM)) {
				instanceNumClause = " and  " // + parent.getDatabaseSchema()
						+ tempTableName
						+ ".encounter_num = t.encounter_num and "
						+ tempTableName
						+ ".instance_num = t.instance_num  and "
						+ tempTableName
						+ ".start_date = t.start_date  and "
						+ tempTableName
						+ ".concept_cd = t.concept_cd  and "
						+ tempTableName + ".provider_id = t.provider_id ";
			} else if (getPanelTiming().equalsIgnoreCase(
					QueryTimingHandler.SAME)
					|| getPanelTiming().equalsIgnoreCase(
							QueryTimingHandler.SAMEVISIT)) {
				encounterNumClause = " and " + tempTableName
						+ ".encounter_num = t.encounter_num ";
			}

			String nonFirstPanelItemSql = "";
			/*
			 * "with t as ( " + "\n" + itemSql + "\n" + " ) " + "\n" + "\n";
			 */

			if (this.isPanelInverted()) {
				if (oldPanelIndex<0)
					oldPanelIndex = 0;

				if (useTempTables){
					String suffix = "";
					
					nonFirstPanelItemSql += parent.buildTempTableCheckDrop("t" + suffix);
					nonFirstPanelItemSql += parent.getSqlDelimiter();
			
					nonFirstPanelItemSql += buildSelectIntoStatement(itemSql, "t");
					nonFirstPanelItemSql += parent.getSqlDelimiter();
					
					nonFirstPanelItemSql += " update " + tempTableName
							+ " set panel_count = -1 " + " where " + tempTableName
							+ ".panel_count =  " + oldPanelIndex + " and exists ( "
							+ "select 1 " + "from #t t " + "where "
							+ tempTableName + ".patient_num = t.patient_num "
							+ encounterNumClause + instanceNumClause + " )  ";
					
					nonFirstPanelItemSql += parent.getSqlDelimiter();
					nonFirstPanelItemSql += parent.buildTempTableCheckDrop("t" + suffix);
			
				}
				else {
					nonFirstPanelItemSql += " update " + tempTableName
						+ " set panel_count = -1 " + " where " + tempTableName
						+ ".panel_count =  " + oldPanelIndex + " and exists ( "
						+ "select 1 " + "from (" + itemSql + ") t " + "where "
						+ tempTableName + ".patient_num = t.patient_num "
						+ encounterNumClause + instanceNumClause + " )  ";
				}

			} else {
				
				if (useTempTables){
					String suffix = "";
					
					nonFirstPanelItemSql += parent.buildTempTableCheckDrop("t" + suffix);
					nonFirstPanelItemSql += parent.getSqlDelimiter();
			
					nonFirstPanelItemSql += buildSelectIntoStatement(itemSql, "t");
					nonFirstPanelItemSql += parent.getSqlDelimiter();
					
					nonFirstPanelItemSql += "update " + tempTableName
							+ " set panel_count =" + panelIndex + " where "
							+ tempTableName + ".panel_count =  " + oldPanelIndex
							+ " and exists ( " + "select 1 " + "from #t"
							+ " t " + "where " + tempTableName
							+ ".patient_num = t.patient_num " + encounterNumClause
							+ instanceNumClause + " ) ";
					
					nonFirstPanelItemSql += parent.getSqlDelimiter();
					nonFirstPanelItemSql += parent.buildTempTableCheckDrop("t" + suffix);
			
				}
				else {
					nonFirstPanelItemSql += "update " + tempTableName
							+ " set panel_count =" + panelIndex + " where "
							+ tempTableName + ".panel_count =  " + oldPanelIndex
							+ " and exists ( " + "select 1 " + "from (" + itemSql
							+ ") t " + "where " + tempTableName
							+ ".patient_num = t.patient_num " + encounterNumClause
							+ instanceNumClause + " ) ";
				}
			}
			if (addDelimiter) {
				panelSql.append(parent.getSqlDelimiter());
			}

			panelSql.append(nonFirstPanelItemSql);

			addDelimiter = true;
		}

		return panelSql.toString();
	}

	/**
	 * Build Invert Main Table Sql
	 * 
	 * Constructs sql statement that selects the correct columns from the superset of items from which
	 * an invert can be applied - through either a minus/except or not in/not exists clause
	 * 
	 * @return String sql statement that contains sql for accessing main table sql
	 */
	public String buildInvertMainTableSql() {
		String selectClause = "";
		String whereClause = "";
		String groupByClause = "";

		String patientTable = "patient_dimension p ";
		String instanceTable = "observation_fact f ";
		String visitTable = "visit_dimension v ";

		List<String> factTables = null;
		List<TemporalPanelItemSql> itemSqlList = null;
		try {
			itemSqlList = getItemSql();
		} catch (I2B2DAOException e) {
			return null;
		}
		if (this.parent.getQueryOptions()!=null&&this.parent.getQueryOptions().useDerivedFactTable()) 
		 {
			factTables = buildFactTableList(itemSqlList);
		 }
		
		
		String invertTableName = patientTable;

		if (getPanelTiming().equalsIgnoreCase(
				QueryTimingHandler.SAMEINSTANCENUM)) {
			selectClause = "f.provider_id, f.start_date, f.concept_cd, f.instance_num, f.encounter_num,  f.patient_num";
			invertTableName = instanceTable;
		} else if (getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME)
				|| getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEVISIT)) {
			invertTableName = visitTable;
			selectClause = "v.encounter_num, v.patient_num";
		} else {
			selectClause = "p.patient_num";
		}

		String invertSql = "select " + selectClause + ", 0 panel_count"
				+ " from " + parent.getDatabaseSchema() + invertTableName
				+ whereClause + groupByClause;
		
		if(factTables != null){
			if((factTables.size() == 1)) {

				invertSql = "select " + selectClause + ", 0 panel_count"
						+ " from " + parent.getDatabaseSchema() +  factTables.get(0) + " f "
						+ whereClause + groupByClause;
			}
			else {
				invertSql = "";
				Iterator i = factTables.iterator();
				while (i.hasNext()){

					invertSql += "select " + selectClause + ", 0 panel_count"
							+ " from " + parent.getDatabaseSchema() +  i.next() + " f "
							+ whereClause + groupByClause;
					if(i.hasNext()){
						invertSql += "\n union all \n";
					}
				}
			}
		}


		return invertSql;

	}

	/**
	 * Build Invert Insert Select Sql
	 * 
	 * Constructs select clause for use in a larger statement. Clause contains the correct columns
	 * based on panel and query timing models
	 * 
	 * @param tableAlias String alias for table referenced in from clause
	 * @return String select clause for inclusion in larger statement
	 */
	protected String buildInvertInsertSelectSql(String tableAlias) {
		if (tableAlias != null && tableAlias.trim().length() > 0)
			tableAlias = tableAlias + ".";
		else
			tableAlias = "";

		String insertSelectClause = "";

		if (getPanelTiming().equalsIgnoreCase(
				QueryTimingHandler.SAMEINSTANCENUM)) {
			insertSelectClause = tableAlias + "provider_id, " + tableAlias
					+ "start_date, " + tableAlias + "concept_cd, " + tableAlias
					+ "instance_num, " + tableAlias + "encounter_num,  "
					+ tableAlias + "patient_num";
		} else if (getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME)
				|| getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEVISIT)) {
			insertSelectClause = " " + tableAlias + "encounter_num, "
					+ tableAlias + "patient_num";
		} else {
			insertSelectClause = " " + tableAlias + "patient_num";
		}

		insertSelectClause += ", 0 as panel_count";

		return insertSelectClause;

	}

	/**
	 * Build Invert Not Except Sql
	 * 
	 * Constructs with clause for use in larger invert statement. Clause contains proper minus/except
	 * constraints based on the panel timing
	 * 
	 * @param withAlias String alias of table or with clause for use in referencing column names
	 * @return String not exists clause for use in larger sql invert statement
	 */
	protected String buildInvertExceptSql(String withAlias) {
		if (withAlias == null)
			withAlias = "y";

		String minusOperator = "minus";
		if (!parent.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
			minusOperator = "except";
		}

		StringBuilder exceptClause = new StringBuilder();

		exceptClause.append(buildInvertMainTableSql() + "\n");

		exceptClause.append(" " + minusOperator + "\n");
		String invertInsertSql = buildInvertInsertSelectSql(withAlias);
		exceptClause.append("select " + invertInsertSql + " from " + withAlias
				+ " ");
		return exceptClause.toString();
	}
	
	/**
	 * Build Invert Not Exists Sql
	 * 
	 * Constructs with clause for use in larger invert statement. Clause contains proper
	 * constraints based on the panel timing
	 * 
	 * @param withAlias String alias of table or with clause for use in referencing column names
	 * @return String not exists clause for use in larger sql invert statement
	 */
	protected String buildInvertNotExistsSql(String withAlias){
		return buildInvertNotExistsSql("", withAlias);
	}

	/**
	 * Build Invert Not Exists Sql
	 * 
	 * Constructs with clause for use in larger invert statement. Clause contains proper not in/not exists
	 * constraints based on the panel timing
	 * 
	 * @param withTable String name of table or with clause name
	 * @param withAlias String alias of table or with clause for use in referencing column names
	 * @return String not exists clause for use in larger sql invert statement
	 */
	protected String buildInvertNotExistsSql(String withTable, String withAlias) {
		if (withAlias == null)
			withAlias = "y";

		StringBuilder notExistsClause = new StringBuilder();
		notExistsClause.append(buildInvertMainTableSql() + "\n");
		notExistsClause.append("where not exists (\n");
		notExistsClause.append("select 1 from " +
				(withTable!=null&&withTable.trim().length()>0 ? withTable + " " : "") +
				withAlias + " \n");
		if (getPanelTiming().equalsIgnoreCase(
				QueryTimingHandler.SAMEINSTANCENUM)) {
			notExistsClause.append("where " + withAlias
					+ ".patient_num = f.patient_num ");
			notExistsClause.append("and " + withAlias
					+ ".encounter_num = f.encounter_num ");
			notExistsClause.append("and " + withAlias
					+ ".start_date = f.start_date ");
			notExistsClause.append("and " + withAlias
					+ ".concept_cd = f.concept_cd ");
			notExistsClause.append("and " + withAlias
					+ ".instance_num = f.instance_num ");
			notExistsClause.append("and " + withAlias
					+ ".provider_id = f.provider_id) ");
		} else if (getPanelTiming().equalsIgnoreCase(QueryTimingHandler.SAME)
				|| getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEVISIT)) {
			notExistsClause.append("where " + withAlias
					+ ".patient_num = v.patient_num ");
			notExistsClause.append("and " + withAlias
					+ ".encounter_num = v.encounter_num) ");
		} else {
			notExistsClause.append("where " + withAlias
					+ ".patient_num = p.patient_num) ");
		}

		return notExistsClause.toString();
	}

	@Override
	public int compareTo(Object element) {
		if (element.getClass().equals((TemporalPanel.class))) {
			TemporalPanel tp2 = (TemporalPanel) element;

			Integer tp1Score = this.getTimingScore();
			Integer tp2Score = tp2.getTimingScore();

			int compare = tp1Score.compareTo(tp2Score);
			if (compare == 0) {
				tp1Score = this.getEstimatedTotal();
				tp2Score = tp2.getEstimatedTotal();
				compare = tp1Score.compareTo(tp2Score);
				if (compare == 0) {
					tp1Score = this.basePanel.getPanelNumber();
					tp2Score = tp2.basePanel.getPanelNumber();
					compare = tp1Score.compareTo(tp2Score);
					if (compare == 0) {
						return this.toString().compareTo(tp2.toString());
					} else
						return compare;

				} else
					return compare;
			} else
				return compare;

		} else {
			return this.toString().compareTo(element.toString());
		}
	}

	/**
	 * Get Timing Score
	 * 
	 * Calculates the timing score of the panel. Timing scores are used to
	 * properly sort panels so query processing can be optimized
	 * 
	 * @return int represents the timing score of the panel
	 */
	private int getTimingScore() {
		String timing = getPanelTiming();
		int score = 0;
		if (timing == null || timing.trim().length() == 0)
			return MAXTIMINGSCORE;
		else if (timing.equalsIgnoreCase(QueryTimingHandler.ANY))
			score = MAXTIMINGSCORE - 1;
		else if (timing.equalsIgnoreCase(QueryTimingHandler.SAME))
			score = MAXTIMINGSCORE - 2;
		else if (timing.equalsIgnoreCase(QueryTimingHandler.SAMEVISIT))
			score = MAXTIMINGSCORE - 2;
		else if (timing.equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM))
			score = MAXTIMINGSCORE - 3;
		else
			score = MAXTIMINGSCORE - 4;

		if (isPanelInverted())
			return (MAXTIMINGSCORE * 2) - (MAXTIMINGSCORE - score);
		else
			return score;
	}

	/**
	 * Get Estimated Total
	 * 
	 * Calculate the estimated number of patients that will be returned by this
	 * panel. This method accounts for missing values and inverted panels when
	 * estimated the number of patients returned. Estimated counts are used to
	 * help sort panels in a way that optimizes query performance
	 * 
	 * @return int estimated count of patients returned by this panel
	 */
	private int getEstimatedTotal() {
		int totalCount = getSumOfPanelItemCounts();
		double missingCount = getMissingItemTotals();

		if (missingCount > 0) {
			double size = getItemList().size();
			double knownSize = size - missingCount;
			if (knownSize == 0)
				return Integer.MAX_VALUE;

			// estimate total size by averaging known items and applying it to
			// all items

			double dblTotal = totalCount;
			totalCount = (int) ((dblTotal / knownSize) * size);
		}

		if (this.isPanelInverted())
			totalCount = Integer.MAX_VALUE - totalCount;

		return totalCount;
	}

	/**
	 * Is Panel Inverted
	 * 
	 * @return boolean true if the panel has the inverted flag set, otherwise
	 *         false
	 */
	public boolean isPanelInverted() {
		return basePanel.getInvert() == 1;
	}

	/**
	 * Get Sum Of Patient Item Counts
	 * 
	 * Sum the estimated patient counts for all items in the panel
	 * 
	 * @return int total of estimated counts from items within the panel
	 */
	public int getSumOfPanelItemCounts() {
		return estimatedPanelSize;
	}

	/**
	 * Get Panel Timing
	 * 
	 * Return timing to use for this panel. If no timing is specified on the
	 * panel level, than the timing for the query is used.
	 * 
	 * @return String representation of the timing used for this panel
	 */
	public String getPanelTiming() {
		if (basePanel.getPanelTiming() == null
				|| basePanel.getPanelTiming().trim().length() == 0)
			return parent.getQueryTiming();
		else
			return basePanel.getPanelTiming();
	}

	/**
	 * Return Encounter To Parent
	 * 
	 * @return boolean true if panel is needs to return encounter num
	 *  results to parent query
	 */
	private boolean returnEncounterToParent() {
		return parent.returnEncounterNum();
	}

	/**
	 * Return Instance to Parent
	 * 
	 * @return boolean true if panel is needs to return instance based columns
	 *  results to parent query
	 */
	private boolean returnInstanceToParent() {
		return parent.returnInstanceNum();
	}

	/**
	 * Get Accuracy Scale
	 * 
	 * @return int accuracy as included in the original query xml
	 */
	public int getAccuracyScale() {
		return basePanel.getPanelAccuracyScale();
	}

	/**
	 * Get Item List
	 * 
	 * @return List<TemporalPanelItem> list of items in this panel
	 */
	public List<TemporalPanelItem> getItemList() {
		return panelItemList;
	}

	/**
	 * Get Security Type
	 * 
	 * @return SecurityType returns security to use for this query
	 */
	protected SecurityType getSecurityType() {
		return parent.getSecurityType();
	}

	/**
	 * Get Requestor Security Type
	 * 
	 * @return SecurityType returns security used by requestor when submitting
	 *         this query
	 */
	protected SecurityType getRequestorSecurityType() {
		return parent.getRequestorSecurityType();
	}

	/**
	 * Get Project Id
	 * 
	 * @return String project id used for this query
	 */
	protected String getProjectId() {
		return parent.getProjectId();
	}

	/**
	 * Get Data Source Lookup
	 * 
	 * @return DataSourceLookup data source information used for submitting
	 *         query to database
	 */
	protected DataSourceLookup getDataSourceLookup() {
		return parent.getDataSourceLookup();
	}

	/**
	 * Get Database Schema
	 * 
	 * @return String name of schema to use when referencing tables
	 */
	protected String getDatabaseSchema() {
		return parent.getDatabaseSchema();
	}

	/**
	 * Build Date Constraint Sql
	 * 
	 * Builds string sql statement that contains the date constraint from the panel leve
	 * 
	 * @return String sql statement that contains the date constraint from the panel level
	 */
	public String buildDateConstraintSql() {
		return buildDateConstraintSql("");
	}

	/**
	 * Build Date Constraint Sql
	 * 
	 * Builds string sql statement that contains the date constraint from the panel leve
	 * 
	 * @param tableAlias String alias of the table to use when reference columns
	 * @return String sql statement that contains the date constraint from the panel level
	 */
	public String buildDateConstraintSql(String tableAlias) {
		DateConstrainUtil dateConstrainUtil = new DateConstrainUtil(
				parent.getDataSourceLookup());

		return dateConstrainUtil.buildPanelDateSql(basePanel, tableAlias);
	}

	/**
	 * Get Server Type
	 * 
	 * Returns name of the underlying database type. Currently, only Oracle and SqlServer are supported
	 * 
	 * @return String name of the database server type
	 */
	public String getServerType() {
		return parent.getServerType();
	}

	/**
	 * Get Project Parameter Map
	 * 
	 * Returns the Map of project parameter values passed in when the query was created
	 * 
	 * @return Map the project parameter map that was passed into the query
	 */
	protected Map getProjectParameterMap() {
		return parent.getProjectParameterMap();
	}

	public int getMissingItemTotals() {
		return missingItemTotals;
	}


	/**
	 * Has Panel Date Constraint
	 * 
	 * Returns whether or not this panel has a panel level date constraint: date from, date to, or both
	 * 
	 * @return Boolean true if this panel has a panel level date constraint
	 */
	public boolean hasPanelDateConstraint() {
		if (basePanel.getPanelDateFrom() != null
				|| basePanel.getPanelDateTo() != null)
			return true;
		else
			return false;
	}

	/**
	 * Has Panel Occurrence Constraint
	 * 
	 * Returns whether or not this panel has an occurrence constraint other than the default
	 * 
	 * @return Boolean true if the panel has an occurrence constraint > 1, else false
	 */
	public boolean hasPanelOccurrenceConstraint() {
		if (this.basePanel.getTotalItemOccurrences() != null
				&& this.basePanel.getTotalItemOccurrences().getOperator() != null) {
			if ((this.basePanel.getTotalItemOccurrences().getOperator() == TotOccuranceOperatorType.GE)
					&& (this.basePanel.getTotalItemOccurrences().getValue() == 1))
				return false;
			else
				return true;
		} else
			return false;
	}

	/**
	 * Is First Panel In Query
	 * 
	 * Determines whether this panel is the first panel in the query
	 * 
	 * @return Boolean true if panel is first panel in query, otherwise false
	 */
	public boolean isFirstPanelInQuery() {
		return parent.getPanelIndex(this) == 0;
	}

	/**
	 * Get Total Occurrences
	 * 
	 * Gets the total occurrences constraint for this panel
	 * 
	 * @return TotalItemOccurrences occurrence constraint from main query
	 */
	public TotalItemOccurrences getTotalOccurrences() {
		return this.basePanel.getTotalItemOccurrences();
	}

	/**
	 * Get Total Occurrences Operator
	 * 
	 * Get the operator for the total occurrences constraint for this panel
	 * 
	 * @return String operator for total occurrences constraint
	 */
	public String getTotalOccurrenceOperator() {
		if (this.basePanel.getTotalItemOccurrences() != null)
			return this.basePanel.getTotalItemOccurrences().getOperator()
					.toString();
		else
			return "";
	}

	/**
	 * Allow Large Text Value Constrain Flag
	 * 
	 * Return whether or not constraints on large text values are allowed
	 * 
	 * @return Boolean true if large text constraints are allowed, otherwise false
	 */
	protected boolean allowLargeTextValueConstrainFlag() {
		return parent.allowLargeTextValueConstrainFlag();
	}

	/**
	 * Appply Occurrence to Panel Level
	 * 
	 * Return whether or not occurrences should be applied on the panel level instead of on each item **NOTE: This is experimental funcationality and not current supported**
	 * 
	 * @return Boolean true if occurrence should be applied over all items in a query instead of on an item by item basis, otherwise false
	 */
	protected boolean applyOccurrenceToPanelLevel() {
		return parent.getQueryOptions().usePanelLevelOccurrence();
	}

	/**
	 * Get Processing Level
	 * 
	 * Returns the processing level of the current query. Default value is 1, when processing an embedded query, processing level is incremented
	 * 
	 * @return int Processing level of current query
	 */
	protected int getProcessingLevel() {
		return parent.getProcessingLevel();
	}

	/**
	 * Add Pre Processing Sql
	 * 
	 * Add sql statement to run before main sql statement is run
	 * 
	 * @param sql String sql statement to be processed before main sql statement has been run
	 */
	protected void addPreProcessingSql(String sql) {
		parent.addPreProcessingSql(sql);
	}

	/**
	 * Add Post Processing Sql
	 * 
	 * Add sql statement to run after main sql statement has run
	 * 
	 * @param sql String sql statement to processed after main sql statement has been run
	 */
	protected void addPostProcessingSql(String sql) {
		parent.addPostProcessingSql(sql);
	}
	
	/**
	 * Search For Query In Request Definition
	 * 
	 * Looks for query defintion with the specified query id in the parent query xml definition
	 * 
	 * @param subQueryId String specified the query id for the query to look for
	 * @return QueryDefinitionType if query is found, otherwise returns null
	 */
	protected QueryDefinitionType searchForQueryInRequestDefinition(String subQueryId){
		return parent.searchForQueryInRequestDefinition(subQueryId);
	}


	/**
	 * Is Patient Only Query
	 * 
	 * @return true if only patient num is required to be returned by this panel
	 */
	protected boolean isPatientOnlyQuery() {
		if (this.returnEncounterToParent()
				|| this.returnInstanceToParent()
				|| this.getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAME)
				|| this.getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEVISIT)
				|| this.getPanelTiming().equalsIgnoreCase(
						QueryTimingHandler.SAMEINSTANCENUM))
			return false;
		else
			return true;
	}
	
	/**
	 * Add Ignored Message
	 * 
	 * @param errorMessage String error m
	 */
	public void addIgnoredMessage(String errorMessage) {
		parent.addIgnoredMessage(errorMessage);
	}
	
	/**
	 * Get Query Options
	 * 
	 * @return TemporalQueryOptions that are valid for this query
	 */
	protected TemporalQueryOptions getQueryOptions() {
		return parent.getQueryOptions();
	}
	
	private List<String> buildFactTableList(List<TemporalPanelItemSql> itemSqlList) {
		List <String> factTableList = new ArrayList<String>();

		for (TemporalPanelItemSql itemSqlItem : itemSqlList) {
			String itemSql = itemSqlItem.itemSql;
			factTableList.add(itemSqlItem.factTable);
		}
		return factTableList;
		
	}

}
