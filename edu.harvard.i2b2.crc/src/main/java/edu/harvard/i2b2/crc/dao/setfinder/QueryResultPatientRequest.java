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
import edu.harvard.i2b2.crc.dao.EmailUtil;

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
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.mail.MessagingException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
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
public class QueryResultPatientRequest extends CRCDAO implements IResultGenerator {

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

		String requestedData = "\n";
		String finalResultOutput = "";

		ValueExporter valueExport = null;
		//ZipOutputStream zipStream;

		//List<File> filesToAdd = new List<File>;
		//(String) param.get("ResultFullName");

		String processTimingFlag = (String) param.get("ProcessTimingFlag");
		String projectId = (String) param.get("projectId");
		int obfuscatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		int recordCount = (Integer) param.get("RecordCount");
		int resultPriority = (Integer) param.get("ResultPriority");
		int transactionTimeout = (Integer) param.get("TransactionTimeout");
		boolean obfscDataRoleFlag = (Boolean)param.get("ObfuscatedRoleFlag");
		QueryDefinitionType queryDef = (QueryDefinitionType)param.get("queryDef");
		List<PanelType> panelList = (List<PanelType>)param.get("panelList");
		List<ResultOutputOptionType>  resultOptionList = (List<ResultOutputOptionType>) param.get("resultOptionList");

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


		QtQueryMaster queryMaster = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId());
		UserType user = null;
		sfDAOFactory.getQueryMasterDAO().renameQuery(queryMaster.getQueryMasterId(), "(" + queryMaster.getQueryMasterId() + ") " + queryDef.getQueryName());

		try {

			if (recordCount==0) return;

			LogTimingUtil logTimingUtil = new LogTimingUtil();
			logTimingUtil.setStartTime();

			LogTimingUtil subLogTimingUtil = new LogTimingUtil();
			subLogTimingUtil.setStartTime();

			String exportItemXml = getItemKeyFromResultType(sfDAOFactory, resultTypeName);



			user = CallPMUtil.getUserFromResponse(queryMaster.getPmXml());

			param.put("QueryStartDate", queryMaster.getCreateDate());
			param.put("FullName", user.getFullName());
			param.put("UserName", user.getUserName());
			param.put("Email", user.getEmail());
			//get break down count sigma from property file 


			//stmt = sfConn.prepareStatement(itemCountSql);

			CancelStatementRunner csr = new CancelStatementRunner(stmt,
					transactionTimeout);
			Thread csrThread = new Thread(csr);
			csrThread.start();

			//String sqlFinal = "";
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();

			valueExport = new ValueExporter();



			valueExport = JaxbXmlToObj(exportItemXml);


			//JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			//ValueExport valueExport = (ValueExport) jaxbUtil
			//		.unMashallFromString(exportItemXml).getValue();

			if (csr.getSqlFinishedFlag()) {
				timeoutFlag = true;
				throw new CRCTimeOutException("The query was canceled.");
			}

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
									.getQueryResultTypeByName(resultTypeName, null);

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

							if (recordCount == 0)
								description = "0 patients, no email sent";
							else
								description = resultTypeList.get(0)
								.getDescription() + " for \"" + queryName +"\"";

							// set the result instance description
							resultInstanceDao.updateResultInstanceDescription(
									resultInstanceId, description);
							//	tm.commit();



							if ((valueExport != null) && (recordCount != 0)) {
								QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
								String letter = valueExport.getDataManagerEmailMessage();
								String requesterMessage = valueExport.getRequesterEmailMessage();
								if (letter != null) {

									letter = processFilename(letter, param);
			
									requesterMessage = processFilename(requesterMessage, param);
									for (ResultOutputOptionType resultOutputOption : resultOptionList) {
										String resultName = resultOutputOption.getName()
												.toUpperCase();
										resultTypeList = resultTypeDao
												.getQueryResultTypeByName(resultName, null);
										if (resultTypeList.size() > 0) {
											requestedData += resultTypeList.get(0).getDescription() + ", ";
											finalResultOutput = resultTypeList.get(0).getName().toUpperCase();
										}
									}
									letter = letter.replaceAll("\\{\\{\\{REQUESTED_DATA_TYPE\\}\\}\\}",requestedData);
									requesterMessage  = requesterMessage.replaceAll("\\{\\{\\{REQUESTED_DATA_TYPE\\}\\}\\}",requestedData);


									ResultType resultType = new ResultType();
									resultType.setName(resultTypeName);
									DataType mdataType = new DataType();
									mdataType.setValue(String.valueOf(recordCount));
									mdataType.setColumn("patientCount");
									mdataType.setType("int");
									resultType.getData().add(mdataType);	
									mdataType = new DataType();
									mdataType.setValue(letter);
									mdataType.setColumn("RequestEmail");
									mdataType.setType("string");
									resultType.getData().add(mdataType);
									mdataType = new DataType();
									mdataType.setValue(qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail"));
									mdataType.setColumn("DataManagerEmail");
									mdataType.setType("string");
									resultType.getData().add(mdataType);

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
									//subLogTimingUtil.setEndTime();
									//tm.begin();
									IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
									xmlResult = strWriter.toString();
									if (resultInstanceId != null)
										xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
												.toString());
//qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.subject")

									if (qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.enabled").equalsIgnoreCase("true")) {
										EmailUtil email = new EmailUtil();
										try {
											
											if (resultTypeName.equals(finalResultOutput)) {
												email.email(qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail"), qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail"),  "i2b2 Data Request", letter);
												if (user.getEmail()!= null &&  !user.getEmail().equals("") && requesterMessage != null)
													email.email(user.getEmail(), qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.exportcsv.datamanageremail"),  "i2b2 Data Request", requesterMessage);
											}
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (I2B2Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (MessagingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}


							}
						} catch (SecurityException e) {
							throw new I2B2DAOException(
									"Failed to write obfuscated description "
											+ e.getMessage(), e);
						} catch (IllegalStateException e) {
							throw new I2B2DAOException(
									"Failed to write obfuscated description "
											+ e.getMessage(), e);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}




	public static ValueExporter JaxbXmlToObj(String xmlString) {

		ValueExporter val = new ValueExporter();
		ValueExporter resultDataSet = null ;
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(val.getClass());

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			resultDataSet = (ValueExporter) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));

			//System.out.println(resultDataSet);
		}
		catch (JAXBException e) {
			e.printStackTrace();
		}
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
		//fileName = fileName.replaceAll("\\{\\{\\{RESULT_INSTANCE_ID\\}\\}\\}", resultInstanceId);
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_NAME\\}\\}\\}", sanitizeFilename(resultFullName));
		fileName = fileName.replaceAll("\\{\\{\\{QUERY_MASTER_ID\\}\\}\\}", sanitizeFilename(queryInstanceId)); //qtMaster.getQueryMasterId()));

		while (fileName.contains("{{{RANDOM_"))
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
		while (fileName.contains("{{{DATE_"))
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
