/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import java.io.File;
import java.io.FileOutputStream;
import edu.harvard.i2b2.crc.dao.xml.ValueExporter;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.EmailUtil;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.delegate.pm.CallPMUtil;
import edu.harvard.i2b2.crc.opencsv.CSVWriter;
import edu.harvard.i2b2.crc.opencsv.ResultSetHelperService;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.dao.xml.Item_orig;
import edu.harvard.i2b2.crc.dao.xml.Items;
import edu.harvard.i2b2.crc.dao.xml.ValueExport;
import net.lingala.zip4j.ZipFile;
//mport net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import java.io.StringReader;

/**
 * Setfinder's result genertor class. This class calculates patient break down
 * for the result type.
 * 
 * Calls the ontology to get the children for the result type and then
 * calculates the patient count for each child of the result type.
 */
public class QueryResultPatientDownload extends CRCDAO implements IResultGenerator {

	protected final Log logesapi = LogFactory.getLog(getClass());

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


		// mm create a csv in a folder


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

		ResultType resultXmlType = (ResultType) param.get("resultType");

		//ZipOutputStream zipStream;

		//List<File> filesToAdd = new List<File>;
		//(String) param.get("ResultFullName");

		String processTimingFlag = (String) param.get("ProcessTimingFlag");
		//String projectId = (String) param.get("projectId");
		int obfuscatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		int recordCount = (Integer) param.get("RecordCount");
		int resultPriority = (Integer) param.get("ResultPriority");
		int transactionTimeout = (Integer) param.get("TransactionTimeout");
		boolean obfscDataRoleFlag = (Boolean)param.get("ObfuscatedRoleFlag");
		QueryDefinitionType queryDef = (QueryDefinitionType)param.get("queryDef");
		//List<PanelType> panelList = (List<PanelType>)param.get("panelList");
		List<ResultOutputOptionType>  resultOptionList = (List<ResultOutputOptionType>) param.get("resultOptionList");

		boolean skipCSV = false;
		if ((Boolean)param.get("SkipCSV") != null)
			skipCSV = (Boolean)param.get("SkipCSV");
		ValueExporter valueExport ;
		this
		.setDbSchemaName(sfDAOFactory.getDataSourceLookup()
				.getFullSchema());
		//Map ontologyKeyMap = (Map) param.get("setFinderResultOntologyKeyMap");
		String serverType = (String) param.get("ServerType");
		//		CallOntologyUtil ontologyUtil = (CallOntologyUtil) param
		//				.get("CallOntologyUtil");
		List<String> roles = (List<String>) param.get("Roles");
		String tempTableName = "";
		PreparedStatement stmt = null;
		boolean errorFlag = false, timeoutFlag = false;
		//String itemKey = "";

		String letter = "";
		int actualTotal = 0, obsfcTotal = 0;

		String logFile = "";
		int rowCount = 0;
		QtQueryMaster queryMaster = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId());
		UserType user = null;
		//zip up if priority is 999
		if (resultPriority == 999)
		{


		} else {

			try {
				LogTimingUtil logTimingUtil = new LogTimingUtil();
				logTimingUtil.setStartTime();

				LogTimingUtil subLogTimingUtil = new LogTimingUtil();
				subLogTimingUtil.setStartTime();

				user = CallPMUtil.getUserFromResponse(queryMaster.getPmXml());

				param.put("QueryStartDate", queryMaster.getCreateDate());
				param.put("FullName", user.getFullName());
				param.put("UserName", user.getUserName());
				param.put("resultInstanceId", resultInstanceId);
				String exportItemXml = getItemKeyFromResultType(sfDAOFactory, resultTypeName);

				//get break down count sigma from property file 

				//ResultType resultType = new ResultType();
				//resultType.setName(resultTypeName);
				//stmt = sfConn.prepareStatement(itemCountSql);

				CancelStatementRunner csr = new CancelStatementRunner(stmt,
						transactionTimeout);
				Thread csrThread = new Thread(csr);
				csrThread.start();

				//String sqlFinal = "";
				QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();

				valueExport = new ValueExporter();


				String workDir = "";

				String workDirStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.workfolder");
				if (workDirStr != null) {
					workDir = workDirStr; // + File.separatorChar + (String) param.get("ResultRandom") + File.separatorChar;
				}

				try {
					valueExport = JaxbXmlToObj(exportItemXml);
				} catch (Exception e)
				{
					edu.harvard.i2b2.crc.dao.xml.File item = new edu.harvard.i2b2.crc.dao.xml.File();
					if (qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.filename").endsWith(".zip")) {
						item.setFilename(workDir+qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.filename").replace(".zip", ".csv"));
						item.setFilename(processFilename(item.getFilename(), param));

						valueExport.setZipFilename(processFilename(workDir+qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.filename"), param));
					}else {
						item.setFilename(workDir+qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.filename"));
						item.setFilename(processFilename(item.getFilename(), param));
					}
					item.setQuery(exportItemXml);
					item.setSeperatorCharacter(qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.defaultseperator"));
					valueExport.setFile(new edu.harvard.i2b2.crc.dao.xml.File[] {item});
					valueExport.setZipEncryptMethod(qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.zipencryptmethod"));

				}
				JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
				//ValueExport valueExport = (ValueExport) jaxbUtil
				//		.unMashallFromString(exportItemXml).getValue();

				letter = valueExport.getDataManagerEmailMessage();


				String letterFilenameStr = valueExport.getDataManagerEmailMessage();
				String letterFilename = null;

				String zipFileNameStr = valueExport.getZipFilename();//.getZipFilename();
				String zipFileName = null;
				if (zipFileNameStr != null) {
					
					zipFileName = workDirStr + zipFileNameStr;

					zipFileName = processFilename(zipFileName, param);


				}

				for (edu.harvard.i2b2.crc.dao.xml.File item: valueExport.getFile()) {
					/*
					 * 		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		RequestMessageType reqMsgType = (RequestMessageType) jaxbUtil
				.unMashallFromString(xml).getValue();
		System.out.println(reqMsgType.getMessageHeader().getMessageControlId());
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		QueryDefinitionRequestType queryDefinitionType = (QueryDefinitionRequestType) unWrapHelper
				.getObjectByClass(
						reqMsgType.getMessageBody().getAny(),
						edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType.class);
		System.out.println("query namef"
				+ queryDefinitionType.getQueryDefinition().getQueryName());
					 */

					Path p = Paths.get(item.getFilename());
					letterFilename = workDirStr + p.getParent() + File.separator + "Letter.txt";

					letterFilename = processFilename(letterFilename, param);



					if (item.getQuery().contains("{{{DX}}}"))
						item.setQuery(item.getQuery().replaceAll("\\{\\{\\{DX\\}\\}\\}", TEMP_DX_TABLE));
					if (item.getQuery().contains("{{{FULL_SCHEMA}}}"))
						item.setQuery(item.getQuery().replaceAll("\\{\\{\\{FULL_SCHEMA\\}\\}\\}", this.getDbSchemaName()));
					if (item.getQuery().contains("{{{RESULT_INSTANCE_ID}}}"))
						item.setQuery(item.getQuery().replaceAll("\\{\\{\\{RESULT_INSTANCE_ID\\}\\}\\}", resultInstanceId));
					

					stmt = sfConn.prepareStatement(item.getQuery());
					stmt.setQueryTimeout(transactionTimeout);
					logesapi.debug("Executing count sql [" + item.getQuery() + "]");

					//
					subLogTimingUtil.setStartTime();
					ResultSet resultSet = stmt.executeQuery();

					//String fileName = "/tmp/"+ projectId +"output"+resultInstanceId + ".csv";
					//automatically compression if destination file extension is ".zip" or ".gz"

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
					if (item.getSeperatorCharacter() != null)
						separator = getChar(item.getSeperatorCharacter());

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
					if (item.getFilename() != null)
						fileName = workDir + item.getFilename();

					String fetchSizeStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.resultfetchsize");
					if (fetchSizeStr != null) {
						fetchSize = Integer.parseInt(fetchSizeStr);
					}	
					String maxFetchRowsStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.maxfetchrows");
					if (maxFetchRowsStr != null) {
						maxFetchRows =  Integer.parseInt(maxFetchRowsStr);
					}

					fileName = processFilename(fileName, param);


					//Update XML Value
					
					DataType mdataType = new DataType();

					mdataType.setValue( fileName);
					mdataType.setColumn("FILENAME");
					mdataType.setType("string");
					resultXmlType.getData().add(mdataType);


					edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of2 = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
					edu.harvard.i2b2.crc.datavo.i2b2result.BodyType bodyType2 = new edu.harvard.i2b2.crc.datavo.i2b2result.BodyType();
					bodyType2.getAny().add(of2.createResult(resultXmlType));
					ResultEnvelopeType resultEnvelop2 = new ResultEnvelopeType();
					resultEnvelop2.setBody(bodyType2);

					JAXBUtil jaxbUtil2 = CRCJAXBUtil.getJAXBUtil();

					StringWriter strWriter2 = new StringWriter();
					jaxbUtil2.marshaller(of2.createI2B2ResultEnvelope(resultEnvelop2),
							strWriter2);
					//tm.begin();
					IXmlResultDao xmlResultDao2 = sfDAOFactory.getXmlResultDao();
					String xmlResult2 = strWriter2.toString();

					xmlResultDao2.deleteQueryXmlResult(resultInstanceId);
					xmlResultDao2.createQueryXmlResult(resultInstanceId, xmlResult2);

					
					
					if (skipCSV == false) {
						boolean async = true;

						File file = new File(fileName);
						file.getParentFile().mkdirs();
						try (CSVWriter writer = new  CSVWriter(fileName, separator,  quotechar,  escapechar,  Character.toString(lineEnd), recordCount)) {
							//CSVWriter(fileName)) {


							//Define fetch size(default as 30000 rows), higher to be faster performance but takes more memory
							ResultSetHelperService.RESULT_FETCH_SIZE=fetchSize;
							//Define MAX extract rows, -1 means unlimited.
							ResultSetHelperService.MAX_FETCH_ROWS=maxFetchRows;



							//String lockoutQueryCountStr = qpUtil
							//		.getCRCPropertyValue("edu.harvard.i2b2.crc.lockout.setfinderquery.count");
							writer.setAsyncMode(async);
							rowCount = writer.writeAll(resultSet, true);

							logFile += writer.getLog();
							resultSet.close();
						}
						if (csr.getSqlFinishedFlag()) {
							timeoutFlag = true;
							throw new CRCTimeOutException("The query was canceled.");
						}

					}
				}

				edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
				BodyType bodyType = new BodyType();
				bodyType.getAny().add(of.createResult(resultXmlType));
				ResultEnvelopeType resultEnvelop = new ResultEnvelopeType();
				resultEnvelop.setBody(bodyType);

				//JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

				StringWriter strWriter = new StringWriter();
				subLogTimingUtil.setStartTime();
				jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop),
						strWriter);
				subLogTimingUtil.setEndTime();
				//tm.begin();
				/*
				IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
				xmlResult = strWriter.toString();
				if (resultInstanceId != null)
					xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
							.toString());
				*/
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
				//}
				//tm.commit();

				//Process XIP File 
				if (letter != null && letterFilename != null) {

					File path  = new File(letterFilename);
					path.getParentFile().mkdirs();
					// Declaring the print writer with path
					PrintWriter pw = new PrintWriter(path);


					//queryMaster.
					if (zipFileName != null) {
						letter = letter.replaceAll("\\{\\{\\{ZIP_FILENAME_WIN\\}\\}\\}", zipFileName.replaceAll("/", "\\\\"));
						letter = letter.replaceAll("\\{\\{\\{ZIP_FILENAME_MAC\\}\\}\\}", zipFileName.replaceAll("\\\\", "/"));
						letter = letter.replaceAll("\\{\\{\\{ZIP_PASSWORD\\}\\}\\}", valueExport.getZipPassword());	
					}
					// Now calling writer() method with string
					pw.write(processFilename(letter, param  ));

					// Flushing the print writer
					pw.flush();

					// Lastly closing the printwriter
					// using the close() method
					pw.close();
				}


				//String requesterLetter = valueExport.getDataManagerEmailMessage();


				if (letter != null && qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail") != null) {

					letter = processFilename(letter, param);

					//Send out email letter
					EmailUtil email = new EmailUtil();
					IQueryResultTypeDao resultTypeDao = sfDAOFactory.getQueryResultTypeDao();
					String requestedData = "\n";
					String finalResultOutput = "";

					for (ResultOutputOptionType resultOutputOption : resultOptionList) {
						String resultName = resultOutputOption.getName()
								.toUpperCase();
						List<QtQueryResultType> resultTypeList = resultTypeDao
								.getQueryResultTypeByName(resultName, null);
						if (resultTypeList.size() > 0) {
							requestedData += resultTypeList.get(0).getDescription() + ", ";
							finalResultOutput = resultTypeList.get(0).getName().toUpperCase();
						}
					}
					letter = letter.replaceAll("\\{\\{\\{REQUESTED_DATA_TYPE\\}\\}\\}",requestedData);
					if (zipFileName != null && skipCSV == false && resultTypeName.equals(finalResultOutput)) {

						File file = new File(zipFileName);
						file.getParentFile().mkdirs();
						String zipencryptMethod = "NONE";
						if ( valueExport.getZipEncryptMethod() != null) {
							zipencryptMethod = valueExport.getZipEncryptMethod();
						}

						ZipParameters zipParameters = new ZipParameters();
						if (!zipencryptMethod.equals("NONE")) {
							zipParameters.setEncryptFiles(true);
							zipParameters.setCompressionLevel(CompressionLevel.MAXIMUM);
							if (zipencryptMethod.equals("AES"))
								zipParameters.setEncryptionMethod(EncryptionMethod.AES);
							else if (zipencryptMethod.equals("STANARD"))
								zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
							else
								zipParameters.setEncryptionMethod(EncryptionMethod.NONE);

						}
						//zipParameters.setFileNameInZip( workDir + "*.*" );
						//zipParameters.setPassword("password");

						//zipStream = new ZipOutputStream(buff);
						//zip.
						//ZipFile zipfile = new ZipFile(fileName + (extName.equals(null) ? "" : "." + default_ext), "password".toCharArray());


						if (zipParameters.getEncryptionMethod().equals(EncryptionMethod.NONE))
							new ZipFile(zipFileName).addFolder(new File(workDir),zipParameters);
						//zipStream = new ZipOutputStream(new FileOutputStream(new File(zipFileName)));
						else
							new ZipFile(zipFileName,valueExport.getZipPassword().toCharArray()).addFolder(new File(workDir),zipParameters);


						//						zipStream = new ZipOutputStream(new FileOutputStream(new File(zipFileName))
						//								, valueExport.getZip_password().toCharArray());

						//zipStream.putNextEntry(zipParameters);
					}

					if ((resultTypeName.equals(finalResultOutput))
							&& (qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.enabled").equalsIgnoreCase("true")) )

						email.email(qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail"), qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail"), "i2b2 Data Export - " + queryDef.getQueryName(), letter);
				}
				

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


								ResultType resultType = new ResultType();
								resultType.setName(resultTypeName);
								DataType mdataType = new DataType();
								mdataType.setValue(String.valueOf(recordCount));
								mdataType.setColumn("PatientCount");
								mdataType.setType("int");
								resultType.setName(resultTypeName);
								mdataType = new DataType();
								mdataType.setValue(String.valueOf(rowCount));								
								mdataType.setColumn("RowCount");
								mdataType.setType("int");
								resultType.getData().add(mdataType);	
								mdataType = new DataType();
								mdataType.setValue(letter);
								mdataType.setColumn("RequestLetter");
								mdataType.setType("string");
								mdataType = new DataType();
								mdataType.setValue(String.valueOf(logFile));								
								mdataType.setColumn("Log");
								mdataType.setType("string");

								resultType.getData().add(mdataType);
								mdataType = new DataType();
								mdataType.setValue(sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId());
								mdataType.setColumn("QueryMasterID");
								mdataType.setType("string");
								resultType.getData().add(mdataType);
								mdataType = new DataType();

								edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
								BodyType bodyType = new BodyType();
								bodyType.getAny().add(of.createResult(resultType));
								ResultEnvelopeType resultEnvelop = new ResultEnvelopeType();
								resultEnvelop.setBody(bodyType);

								//JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

								StringWriter strWriter = new StringWriter();

								//subLogTimingUtil.setStartTime();
								JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
								jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop),
										strWriter);

								IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
								xmlResult = strWriter.toString();
								if (resultInstanceId != null)
									xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
											.toString());
								
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
							} catch (JAXBUtilException e) {
								throw new I2B2DAOException(
										"Failed to write jaxb  "
												+ e.getMessage(), e);
							}
						}
					}
				}
			}
		}

	}


	public static ValueExporter JaxbXmlToObj(String xmlString) throws JAXBException {

		ValueExporter val = new ValueExporter();
		ValueExporter resultDataSet = null ;
		JAXBContext jaxbContext;
		//try {
			jaxbContext = JAXBContext.newInstance(val.getClass());

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			resultDataSet = (ValueExporter) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));

			//System.out.println(resultDataSet);
		
		return resultDataSet;
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

	private String processFilename(String fileName, Map param)
	{


		//		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
		//				.get("SetFinderDAOFactory");

		String projectId = (String) param.get("projectId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		QueryDefinitionType queryDef = (QueryDefinitionType)param.get("queryDef");
		String resultFullName = queryDef.getQueryName();
		//queryDef.getQueryId()

		//		String resultFullName =  sfDAOFactory.getQueryMasterDAO().getQueryDefinition(
		//				sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId()).getName();
		//		QtQueryMaster qtMaster =  sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster();

		fileName = fileName.replaceAll("\\{\\{\\{USER_NAME\\}\\}\\}",(String) param.get("UserName"));//queryDef.getUserId());
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_STARTDATE\\}\\}\\}", ((java.sql.Timestamp) param.get("QueryStartDate")).toLocaleString());//sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getStartDate().toLocaleString());
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_ENDDATE\\}\\}\\}", new Date().toLocaleString());
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_RUNTIME\\}\\}\\}", Integer.toString(Math.toIntExact(new Date().getTime() - ((java.sql.Timestamp) param.get("QueryStartDate")).getTime())/1000));

		fileName = fileName.replaceAll("\\{\\{\\{PATIENT_COUNT\\}\\}\\}", param.get("RecordCount").toString());		
		fileName = fileName.replaceAll("\\{\\{\\{FULL_NAME\\}\\}\\}", (String) param.get("FullName"));			
		fileName = fileName.replaceAll("\\{\\{\\{PROJECT_ID\\}\\}\\}", projectId.substring(1, projectId.length()-1));
		fileName = fileName.replaceAll("\\{\\{\\{RESULT_INSTANCE_ID\\}\\}\\}", (String) param.get("resultFullName"));
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_NAME\\}\\}\\}", sanitizeFilename(resultFullName));
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_MASTER_ID\\}\\}\\}", sanitizeFilename(queryInstanceId)); //qtMaster.getQueryMasterId()));

		if (fileName.contains("{{{RANDOM_"))
		{

			try {
				int start = fileName.indexOf("{{{RANDOM_");
				int end = fileName.indexOf("}}}", start);
				int size = Integer.parseInt(fileName.substring(start+10, end));

				//SecureRandom random = new SecureRandom();
				//fileName = fileName.replaceAll("\\{\\{\\{RANDOM_"+size+"\\}\\}\\}", String.valueOf(random.nextInt(size)));
				fileName = fileName.replaceAll("\\{\\{\\{RANDOM_"+size+"\\}\\}\\}", (String) param.get("ResultRandom"));

			} catch (Exception e) {}

		}
		// Deal with dates
		if (fileName.contains("{{{DATE_"))
		{

			try {
				int start = fileName.indexOf("{{{DATE_");
				int end = fileName.indexOf("}}}", start);
				String date = fileName.substring(start+8, end);

				//DateTimeFormatter formatter = DateTimeFormatter.ofPattern(date);
				//fileName = fileName.replaceAll("\\{\\{\\{DATE_"+date+"\\}\\}\\}", LocalDate.now().format(formatter));

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(date);
				fileName = fileName.replaceAll("\\{\\{\\{DATE_"+date+"\\}\\}\\}",  ((LocalDate) param.get("ResultDate")).format(formatter));
				//(String) param.get("ResultDate"));

			} catch (Exception e) {}

		}


		return fileName;
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
