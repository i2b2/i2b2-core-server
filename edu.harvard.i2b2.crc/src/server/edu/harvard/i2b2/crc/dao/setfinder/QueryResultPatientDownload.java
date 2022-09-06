/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;


import java.io.File;

/**
 * To add Length of Stay (This is for Oracle and Postgresl)  For Sql Server change the sql statement from (DX to #DX)
 * 
 * Add a entry to QT_BREAKDOWN_PATH
 *     NAME = LENGTH_OF_STAY
 *     VALUE = select length_of_stay as patient_range, count(distinct a.PATIENT_num) as patient_count  from visit_dimension a, DX b where a.patient_num = b.patient_num group by a.length_of_stay order by 1
 * 
 * Add a entry to QT_QUERY_RESULT_TYPE
 *     RESULT_TYPE_ID = 13 (Or any unused number)
 *     NAME = LENGTH_OF_STAY
 *     DESCRIPTION = Length of Dtay Brealdown
 *     DISPLAY_TYPE_ID = CATNUM
 *     VISUAL_ATTRIBUTE_TYPE_ID = LA
 *         
 * Add a new <entry> in CRCApplicationContext.xml
 * 	<entry>
 *           <key>
 *             <value>LENGTH_OF_STAY</value>
 *           </key>
 *           <value>edu.harvard.i2b2.crc.dao.setfinder.QueryResultPatientSQLCountGenerator</value>
 *         </entry>
 * 
 */


import java.io.StringWriter;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.opencsv.CSVWriter;
import edu.harvard.i2b2.crc.opencsv.ResultSetHelperService;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Setfinder's result genertor class. This class calculates patient break down
 * for the result type.
 * 
 * Calls the ontology to get the children for the result type and then
 * calculates the patient count for each child of the result type.
 */
public class QueryResultPatientDownload extends CRCDAO implements IResultGenerator {

	protected final Logger logesapi = ESAPI.getLogger(getClass());

	@Override
	public String getResults() {
		return xmlResult;
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
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		// String itemKey = (String) param.get("ItemKey");
		String resultTypeName = (String) param.get("ResultOptionName");
		String processTimingFlag = (String) param.get("ProcessTimingFlag");
		String projectId = (String) param.get("projectId");
		int obfuscatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		int recordCount = (Integer) param.get("RecordCount");
		int transactionTimeout = (Integer) param.get("TransactionTimeout");
		boolean obfscDataRoleFlag = (Boolean)param.get("ObfuscatedRoleFlag");
		QueryDefinitionType queryDef = (QueryDefinitionType)param.get("queryDef");
		List<PanelType> panelList = (List<PanelType>)param.get("panelList");
		boolean skipCSV = false;
		if ((Boolean)param.get("SkipCSV") != null)
			skipCSV = (Boolean)param.get("SkipCSV");


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
		boolean errorFlag = false, timeoutFlag = false;
		//String itemKey = "";

		int actualTotal = 0, obsfcTotal = 0;

		try {
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			logTimingUtil.setStartTime();

			LogTimingUtil subLogTimingUtil = new LogTimingUtil();
			subLogTimingUtil.setStartTime();

			String itemCountSql = getItemKeyFromResultType(sfDAOFactory, resultTypeName);

			//get break down count sigma from property file 

			ResultType resultType = new ResultType();
			resultType.setName(resultTypeName);
			//stmt = sfConn.prepareStatement(itemCountSql);

			CancelStatementRunner csr = new CancelStatementRunner(stmt,
					transactionTimeout);
			Thread csrThread = new Thread(csr);
			csrThread.start();

			//String sqlFinal = "";

			if (itemCountSql.contains("{{{DX}}}"))
				itemCountSql = itemCountSql.replaceAll("\\{\\{\\{DX\\}\\}\\}", TEMP_DX_TABLE);
			if (itemCountSql.contains("{{{DATABASE_NAME}}}"))
				itemCountSql = itemCountSql.replaceAll("\\{\\{\\{DATABASE_NAME\\}\\}\\}", this.getDbSchemaName());


			//	String[] sqls = itemCountSql.split("<\\*>");
			//	int count = 0;
			//	while (count < sqls.length - 1)
			//	{



			stmt = sfConn.prepareStatement(itemCountSql);
			stmt.setQueryTimeout(transactionTimeout);
			logesapi.debug(null,"Executing count sql [" + itemCountSql + "]");

			//
			subLogTimingUtil.setStartTime();
			ResultSet resultSet = stmt.executeQuery();

			//String fileName = "/tmp/"+ projectId +"output"+resultInstanceId + ".csv";
			//automatically compression if destination file extension is ".zip" or ".gz"

			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			char separator = ',';
			char escapechar = '"';
			char lineEnd = '\n';
			String userName = "";
			char quotechar = '"';
			String fileName = "";
			int fetchSize = 50000;
			int maxFetchRows = -1;
			String separatorStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.defaultseperator");
			if (separatorStr != null) {
				separator = getChar(separatorStr);
			}
			String quotecharStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.defaultquotechar");
			if (quotecharStr != null) {
				quotechar = getChar(quotecharStr);
			}
			String escapecharStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.defaultescapecharacter");
			if (escapecharStr != null) {
				escapechar = getChar(escapecharStr);
			}
			String lineEndStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.defaultlineend");
			if (lineEndStr != null) {
				lineEnd = getChar(lineEndStr);
			}
			String fileNameStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.filename");
			if (fileNameStr != null) {
				fileName = fileNameStr;
			}
			String fetchSizeStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.resultfetchsize");
			if (fetchSizeStr != null) {
				fetchSize = Integer.parseInt(fetchSizeStr);
			}	
			String maxFetchRowsStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.maxfetchrows");
			if (maxFetchRowsStr != null) {
				maxFetchRows =  Integer.parseInt(maxFetchRowsStr);
			}
			fileName = fileName.replaceAll("\\{\\{\\{USER_NAME\\}\\}\\}", userName);
			fileName = fileName.replaceAll("\\{\\{\\{PROJECT_ID\\}\\}\\}", projectId.substring(1, projectId.length()-1));
			fileName = fileName.replaceAll("\\{\\{\\{RESULT_INSTANCE_ID\\}\\}\\}", resultInstanceId);
			fileName = fileName.replaceAll("\\{\\{\\{QUERY_NAME\\}\\}\\}", sanitizeFilename(queryDef.getQueryName()));

			while (fileName.contains("{{{RANDOM_"))
			{
				try {
					int start = fileName.indexOf("{{{RANDOM_");
					int end = fileName.indexOf("}}}", start);
					int size = Integer.parseInt(fileName.substring(start+10, end));

					SecureRandom random = new SecureRandom();
					fileName = fileName.replaceAll("\\{\\{\\{RANDOM_"+size+"\\}\\}\\}", String.valueOf(random.nextInt(size)));

				} catch (Exception e) {}

			}
			// Deal with dates
			while (fileName.contains("{{{DATE_"))
			{

				try {
					int start = fileName.indexOf("{{{DATE_");
					int end = fileName.indexOf("}}}", start);
					String date = fileName.substring(start+8, end);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(date);
					fileName = fileName.replaceAll("\\{\\{\\{DATE_"+date+"\\}\\}\\}", LocalDate.now().format(formatter));
				} catch (Exception e) {}

			}



			if (skipCSV == false) {
				boolean async = true;

				File file = new File(fileName);
				file.getParentFile().mkdirs();
				try (CSVWriter writer = new  CSVWriter(fileName, separator,  quotechar,  escapechar,  Character.toString(lineEnd), recordCount,sfDAOFactory)) {
					//CSVWriter(fileName)) {


					//Define fetch size(default as 30000 rows), higher to be faster performance but takes more memory
					ResultSetHelperService.RESULT_FETCH_SIZE=fetchSize;
					//Define MAX extract rows, -1 means unlimited.
					ResultSetHelperService.MAX_FETCH_ROWS=maxFetchRows;



					//String lockoutQueryCountStr = qpUtil
					//		.getCRCPropertyValue("edu.harvard.i2b2.crc.lockout.setfinderquery.count");
					writer.setAsyncMode(async);
					int result = writer.writeAll(resultSet, true);

				}
				if (csr.getSqlFinishedFlag()) {
					timeoutFlag = true;
					throw new CRCTimeOutException("The query was canceled.");
				}

			}


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
			if (resultInstanceId != null)
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
			errorFlag = true;
			log.error("Error while executing sql", sqlEx);
			throw new I2B2DAOException("Error while executing sql", sqlEx);
		} catch (Exception sqlEx) {

			errorFlag = true;
			log.error("QueryResultPatientSetGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);
		} finally {

			if (resultInstanceId != null) {
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

	}

	private String sanitizeFilename(String filename)
	{
		
		return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
	}
	/*
	private static final Pattern TAG_REGEX = Pattern.compile("{{{(.+?)}}}", Pattern.DOTALL);


	private static List<String> getTagValues(final String str) {
		final List<String> tagValues = new ArrayList<String>();
		final Matcher matcher = TAG_REGEX.matcher(str);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}
		return tagValues;
	}
	 */
	private char getChar(String sTerminatedBy)
	{
		if ("\\t".equals(sTerminatedBy)) {
			return  ( '\t');
		} else if ("\\n".equals(sTerminatedBy)) {
			return  ( '\n');
		} else if ("\\r".equals(sTerminatedBy)) {
			return  ( '\r');
		} else if ("\\f".equals(sTerminatedBy)) {
			return  ( '\f');			
		} else if (null == sTerminatedBy || "".equals(sTerminatedBy) || "\\0".equals(sTerminatedBy)) {
			return  ('\0');
		} else {
			return  ( sTerminatedBy.charAt(0));
		}

	}

	private String getItemKeyFromResultType(SetFinderDAOFactory sfDAOFactory,
			String resultTypeKey) {
		//
		IQueryBreakdownTypeDao queryBreakdownTypeDao = sfDAOFactory
				.getQueryBreakdownTypeDao();
		QtQueryBreakdownType queryBreakdownType = queryBreakdownTypeDao
				.getBreakdownTypeByName(resultTypeKey);
		String itemKey = queryBreakdownType.getValue();
		return itemKey;
	}


}
