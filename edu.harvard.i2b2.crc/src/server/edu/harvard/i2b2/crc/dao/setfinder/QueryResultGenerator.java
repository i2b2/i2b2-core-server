/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalPanelCellQueryItem;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalPanelConceptItem;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalPanelItem;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptsType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;

/**
 * Setfinder's result genertor class. This class calculates patient break down
 * for the result type.
 * 
 * Calls the ontology to get the children for the result type and then
 * calculates the patient count for each child of the result type.
 */
public class QueryResultGenerator extends CRCDAO implements IResultGenerator {


	@Override
	public String getResults() {
		return xmlResult;
	}


	protected DerivedFactColumnsType getFactColumnsFromOntologyCell(String itemKey,
			SecurityType securityType, String projectId)
					throws ConceptNotFoundException, OntologyException {
		DerivedFactColumnsType factColumns = new DerivedFactColumnsType();
		try {

			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);

			factColumns = CallOntologyUtil.callGetFactColumns(itemKey,
					securityType, projectId,
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
	private String xmlResult = null;
	/**
	 * Function accepts parameter in Map. The patient count will be obfuscated
	 * if the user is OBFUS
	 */
	@Override
	public void generateResult(Map param) throws CRCTimeOutException,
	I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");

		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		//String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		// String itemKey = (String) param.get("ItemKey");
		String resultTypeName = (String) param.get("ResultOptionName");
		String processTimingFlag = (String) param.get("ProcessTimingFlag");
		int obfuscatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		int recordCount = (Integer) param.get("RecordCount");
		int transactionTimeout = (Integer) param.get("TransactionTimeout");
		boolean obfscDataRoleFlag = (Boolean)param.get("ObfuscatedRoleFlag");

		String TEMP_DX_TABLE = "#DX";
		if (sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			TEMP_DX_TABLE = getDbSchemaName() + "#DX";

		} else if (sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) {
			TEMP_DX_TABLE = getDbSchemaName() + "DX";
		}

		this
		.setDbSchemaName(sfDAOFactory.getDataSourceLookup()
				.getFullSchema());
		Map ontologyKeyMap = (Map) param.get("setFinderResultOntologyKeyMap");
		String serverType = (String) param.get("ServerType");
		//		CallOntologyUtil ontologyUtil = (CallOntologyUtil) param
		//				.get("CallOntologyUtil");
		List<String> roles = (List<String>) param.get("Roles");
		String tempTableName = "";
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		boolean errorFlag = false, timeoutFlag = false;
		String itemKey = "";


		try {
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			logTimingUtil.setStartTime();
			itemKey = getItemKeyFromResultType(sfDAOFactory, resultTypeName, roles);

			//log.debug("Result type's " + resultTypeName + " item key value "
			//		+ itemKey);

			LogTimingUtil subLogTimingUtil = new LogTimingUtil();
			subLogTimingUtil.setStartTime();
			ConceptsType conceptsType = CallOntologyUtil.callGetChildren(itemKey, (SecurityType) param.get("securityType"), (String) param.get("projectId"),  (String) param.get("ontologyGetChildrenUrl"));
			if (conceptsType != null && conceptsType.getConcept().size()<1) { 
				throw new I2B2DAOException("Could not fetch children result type " + resultTypeName + " item key [ " + itemKey + " ]" );
			}
			subLogTimingUtil.setEndTime();
			if (processTimingFlag != null) {
				if (processTimingFlag.trim().equalsIgnoreCase(ProcessTimingReportUtil.DEBUG) ) {
					ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
					ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(subLogTimingUtil, "BUILD - " + resultTypeName + " : Ontology Call(GetChildren) ", ""));
				}
			}

			String itemCountSql = " select count(distinct PATIENT_NUM) as item_count  from "
					+ this.getDbSchemaName() 
					+ "observation_fact obs_fact  "
					+ " where obs_fact.patient_num in (select patient_num from "
					+ TEMP_DX_TABLE
					+ "    ) "
					+ " and obs_fact.concept_cd in (select concept_cd from "
					+ this.getDbSchemaName()
					+ "concept_dimension  where concept_path like ?)";

			//get break down count sigma from property file 

			double breakdownCountSigma = GaussianBoxMuller.getBreakdownCountSigma();
			double obfuscatedMinimumValue = GaussianBoxMuller.getObfuscatedMinimumVal();

			ResultType resultType = new ResultType();
			resultType.setName(resultTypeName);
			/*
			stmt = sfConn.prepareStatement(itemCountSql);

			CancelStatementRunner csr = new CancelStatementRunner(stmt,
					transactionTimeout);
			Thread csrThread = new Thread(csr);
			csrThread.start();
			 */

			for (ConceptType conceptType : conceptsType.getConcept()) {

				// OMOP WAS...	
				// String joinTableName = "observation_fact";
				String factTable = "observation_fact";
				DerivedFactColumnsType columns = new DerivedFactColumnsType();
				columns.getDerivedFactTableColumn().add(conceptType.getFacttablecolumn());
				if( QueryProcessorUtil.getInstance().getDerivedFactTable() == true) {

					String baseItemFactColumn = conceptType.getFacttablecolumn();
					columns = getFactColumnsFromOntologyCell(conceptType.getKey(), (SecurityType) param.get("securityType"), (String) param.get("projectId"));

					if(columns.getDerivedFactTableColumn().size() > 1) {
						for (String column : columns.getDerivedFactTableColumn()) {
							// look for non-null fact table columns that are not equal to the base item's column
							if((column != null) && !(column.equals(baseItemFactColumn))){
								if(!column.contains(".")){
									columns.getDerivedFactTableColumn().remove(column);
									/*
									 TemporalPanelItem	derivedPanelItem = new TemporalPanelConceptItem(this, itemType);
									 //if(conceptType == null)
									//	 derivedPanelItem.getConceptType();

									 if (derivedPanelItem != null
											 && conceptType != null
											 && conceptType.getDimcode() != null
											 && conceptType.getDimcode()
											 .toLowerCase().trim()
											 .startsWith(ItemKeyUtil.ITEM_KEY_CELLID)) {
										 derivedPanelItem = new TemporalPanelCellQueryItem(this, itemType,
												 conceptType);
									 }
									 */
									log.debug("setting a new fact column: " + column);
									///									 derivedPanelItem.parseFactColumn(column);
									//								 panelItemList.add(derivedPanelItem);
								}
							}
						}
					}



				}
				int conceptWithCount = 0;
				int actualTotal = 0; //, obsfcTotal = 0;
				itemCountSql = " select  count (distinct item_count) as item_count from ( ";
				for (String column : columns.getDerivedFactTableColumn()) {

					String factColumnName = column; //conceptType.getFacttablecolumn();
					String factTableColumn = factColumnName;

					if (factColumnName!=null&&factColumnName.contains(".")){
						int lastIndex = factColumnName.lastIndexOf(".");
						factTable= factColumnName.substring(0, lastIndex);
						if ((lastIndex+1)<factColumnName.length()){
							factTableColumn = factColumnName.substring(lastIndex+1);
						}
					}

					String joinTableName = factTable;

					if (conceptType.getTablename().equalsIgnoreCase(
							"patient_dimension")) { 
						joinTableName = "patient_dimension";
					} else if (conceptType.getTablename().equalsIgnoreCase(
							"visit_dimension")) { 
						joinTableName = "visit_dimension"; 
					} else if (!conceptType.getTablename().equalsIgnoreCase(
							"concept_dimension")) { 
						joinTableName = conceptType.getTablename(); 
					}

					String dimCode = this.getDimCodeInSqlFormat(conceptType);

					if (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) 
						dimCode = dimCode.replaceAll("\\\\", "\\\\\\\\");
					itemCountSql += " ( select distinct PATIENT_NUM as item_count from " +  this.getDbSchemaName() + joinTableName +  
							" where " + " patient_num in (select patient_num from "
							+ TEMP_DX_TABLE

							//OMOP WAS...
							//+ " )  and "+ conceptType.getFacttablecolumn() + " IN (select "
							//+ conceptType.getFacttablecolumn() + " from "
							+ " )  and "+ factTableColumn + " IN (select "
							+ factTableColumn + " from "
							+ getDbSchemaName() + conceptType.getTablename() + "  "
							+  " where " + conceptType.getColumnname()
							+ " " + conceptType.getOperator() + " "
							+ dimCode + ")) union";

				}
				itemCountSql = itemCountSql.substring(0,itemCountSql.length()-5) + "  ) a ";
				stmt = sfConn.prepareStatement(itemCountSql);
				stmt.setQueryTimeout(transactionTimeout);
				//log.debug("Executing count sql [" + itemCountSql + "]");

				//
				subLogTimingUtil.setStartTime();
				resultSet = stmt.executeQuery();
				/*
					if (csr.getSqlFinishedFlag()) {
						timeoutFlag = true;
						throw new CRCTimeOutException("The query was canceled.");
					}
				 */
				resultSet.next();
				int demoCount = resultSet.getInt("item_count");
				subLogTimingUtil.setEndTime();
				if (processTimingFlag != null) {
					if (processTimingFlag.trim().equalsIgnoreCase(ProcessTimingReportUtil.DEBUG) ) {
						ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
						ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(subLogTimingUtil, "BUILD - " + resultTypeName + " : COUNT SQL for " + conceptType.getDimcode() + " ", "sql="+itemCountSql));
					}
				}
				//

				actualTotal = demoCount;
				//if (demoCount > 0)
				//	conceptWithCount++;



				//if (conceptWithCount > 0)
				//	actualTotal = actualTotal/conceptWithCount;
				if (obfscDataRoleFlag) {
					GaussianBoxMuller gaussianBoxMuller = new GaussianBoxMuller();
					actualTotal = (int) gaussianBoxMuller
							.getNormalizedValueForCount(actualTotal,breakdownCountSigma,obfuscatedMinimumValue);
					//obsfcTotal += actualTotal;
				}
				DataType mdataType = new DataType();
				mdataType.setValue(String.valueOf(actualTotal));
				mdataType.setColumn(conceptType.getName());
				mdataType.setType("int");
				resultType.getData().add(mdataType);
			}
			//csr.setSqlFinishedFlag();
			//csrThread.interrupt();
			resultSet.close();
			stmt.close();

			edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createResult(resultType));
			ResultEnvelopeType resultEnvelop = new ResultEnvelopeType();
			resultEnvelop.setBody(bodyType);

			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

			StringWriter strWriter = new StringWriter();
			subLogTimingUtil.setStartTime();
			jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop),
					strWriter);
			subLogTimingUtil.setEndTime();
			//tm.begin();
			IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
			xmlResult = strWriter.toString();
			xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
					.toString());
			//
			if (processTimingFlag != null) {
				if (!processTimingFlag.trim().equalsIgnoreCase(ProcessTimingReportUtil.NONE) ) {
					ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
					if (processTimingFlag.trim().equalsIgnoreCase(ProcessTimingReportUtil.DEBUG) ) {
						ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(subLogTimingUtil, "JAXB - " + resultTypeName , ""));
					}
					logTimingUtil.setEndTime();
					ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(logTimingUtil, "BUILD - " + resultTypeName , ""));
				}
			}
			//tm.commit();

		} catch (SQLException sqlEx) {
			// catch oracle query timeout error ORA-01013
			if (sqlEx.toString().indexOf("ORA-01013") > -1) {
				timeoutFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(), sqlEx);
			}
			if (sqlEx.getMessage().indexOf("The query was canceled.") > -1) {
				timeoutFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(), sqlEx);
			}
			if (sqlEx.getMessage().indexOf("timed out") > -1) {
				timeoutFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(), sqlEx);
			}
			errorFlag = true;
			log.error("Error while executing sql", sqlEx);
			throw new I2B2DAOException("Error while executing sql", sqlEx);
		} catch (Exception sqlEx) {

			errorFlag = true;
			log.error("QueryResultGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);
		} finally {

			IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				// set the setsize and the description of the result instance if
				// the user role is obfuscated
				if (timeoutFlag == false) { // check if the query completed
					try {
						//	tm.begin();

						String obfusMethod = "", description = null;
						if (obfscDataRoleFlag) {
							obfusMethod = IQueryResultInstanceDao.OBSUBTOTAL;
							// add () to the result type description
							// read the description from result type

						} else { 
							obfuscatedRecordCount = recordCount;
						}
						IQueryResultTypeDao resultTypeDao = sfDAOFactory.getQueryResultTypeDao();
						List<QtQueryResultType> resultTypeList = resultTypeDao
								.getQueryResultTypeByName(resultTypeName, roles);

						// add "(Obfuscated)" in the description
						//description = resultTypeList.get(0)
						//		.getDescription()
						//		+ " (Obfuscated) ";
						String queryName = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(
								sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId()).getName();



						resultInstanceDao.updatePatientSet(resultInstanceId,
								QueryStatusTypeId.STATUSTYPE_ID_FINISHED, null,
								//obsfcTotal, 
								obfuscatedRecordCount, recordCount, obfusMethod);

						description = resultTypeList.get(0)
								.getDescription() + " for \"" + queryName +"\"";

						// set the result instance description
						resultInstanceDao.updateResultInstanceDescription(
								resultInstanceId, description);
						//	tm.commit();
					} catch (SecurityException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (IllegalStateException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					}
				}
			}
		}

	}

	private String getItemKeyFromResultType(SetFinderDAOFactory sfDAOFactory,
			String resultTypeKey, List<String> roles) throws I2B2Exception {
		//
		IQueryBreakdownTypeDao queryBreakdownTypeDao = sfDAOFactory
				.getQueryBreakdownTypeDao();
		QtQueryBreakdownType queryBreakdownType = queryBreakdownTypeDao
				.getBreakdownTypeByName(resultTypeKey);
		String itemKey = queryBreakdownType.getValue();
		if (queryBreakdownType.getUserRoleCd() != null && !roles.contains(queryBreakdownType.getUserRoleCd()))
			throw new I2B2Exception("User does not have permission to run this breakdown.");
		return itemKey;
	}



	private String getDimCodeInSqlFormat(ConceptType conceptType)  {
		String theData = null;
		if (conceptType.getColumndatatype() != null
				&& conceptType.getColumndatatype().equalsIgnoreCase("T")) {
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
		return theData;
	}
}