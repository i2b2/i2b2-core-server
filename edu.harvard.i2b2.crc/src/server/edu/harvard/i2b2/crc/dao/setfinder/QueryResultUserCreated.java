/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;


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
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.EmailUtil;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.dao.xml.ValueExporter;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.delegate.pm.CallPMUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import jakarta.mail.MessagingException;

/**
 * Request that is executed when a user creates a RPDO request from the Run Query.
 */
public class QueryResultUserCreated extends CRCDAO implements IResultGenerator {

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
		SecurityType securityType = (SecurityType) param.get("securityType");
		//	String projectId= null;// = (String) param.get("projectId");



		//int transactionTimeout = (Integer) param.get("TransactionTimeout");
		//long dxCreateTime = (Long) param.get("DXCreateTime");
		//transactionTimeout = transactionTimeout ;//- Math.toIntExact(dxCreateTime);
		boolean obfscDataRoleFlag = (Boolean)param.get("ObfuscatedRoleFlag");

		QueryDefinitionType queryDef = (QueryDefinitionType) param.get("queryDef");
		List<ResultOutputOptionType>  resultOptionList = (List<ResultOutputOptionType>) param.get("resultOptionList");

		String requestedData = "\n";
		String finalResultOutput = "";
		UserType user = null;
		//ProjectType project = null;

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

		int actualTotal = 0, obsfcTotal = 0;

		try {
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			logTimingUtil.setStartTime();

			LogTimingUtil subLogTimingUtil = new LogTimingUtil();
			subLogTimingUtil.setStartTime();

			String itemCountSql = getItemKeyFromResultType(sfDAOFactory, resultTypeName);

			QtQueryMaster queryMaster = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId());

			user = CallPMUtil.getUserFromResponse(queryMaster.getPmXml());
			//project = CallPMUtil.getUserProjectFromResponse(queryMaster.getPmXml(), securityType, queryMaster.getGroupId());


			param.put("QueryStartDate", queryMaster.getCreateDate());
			param.put("FullName", user.getFullName());
			param.put("UserName", user.getUserName());
			param.put("resultInstanceId", resultInstanceId);
			param.put("QueryMasterId", queryMaster.getQueryMasterId());
			//param.put("ResultNameDescription", qpUtil.sanitizeFilename(getResultTypeDescrption(sfDAOFactory, resultTypeName)));

			/*
			ResultType resultType = new ResultType();
			resultType.setName(resultTypeName);

			DataType mdataType = new DataType();

			mdataType.setValue( new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
			mdataType.setColumn("SUBMITTED");
			mdataType.setType("string");
			resultType.getData().add(mdataType);


			mdataType = new DataType();

			mdataType.setValue( queryDef.getEmail());
			mdataType.setColumn("EMAIL");
			mdataType.setType("string");
			resultType.getData().add(mdataType);


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
				xmlResultDao.createQueryXmlResult(resultInstanceId, xmlResult);
			 */

			if (queryInstanceId != null) {
				IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
						.getPatientSetResultDAO();
				resultInstanceDao.updateInstanceMessage(queryInstanceId, queryDef.getMessage());

				QtQueryMaster qtQueryMaster = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId());

				qtQueryMaster.setMasterTypeCd("EXPORT");

				sfDAOFactory.getQueryMasterDAO().updateMasterTypeAfterRun(
						sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId(), 
						"EXPORT");

			}



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

							//Send out email



							if (recordCount == 0)
								description = "0 patients, no email sent";
							else
								description = resultTypeList.get(0)
								.getDescription() + " for \"" + queryName +"\"";

							// set the result instance description
							resultInstanceDao.updateResultInstanceDescription(
									resultInstanceId, description);
							//	tm.commit();


							//ValueExporter valueExport = null;
							// Send out email



							List<ParamType> projectParam = null;
							for (ProjectType project : user.getProject())
							{
								if (project.getPath().replace("/", "").equals(((String) param.get("projectId")).replace("/", "")) )
									projectParam = project.getParam();
							}


							QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
							String reuqestTemplate = getProjectParam(projectParam, "Data Request Template");
							//String requesterMessage = valueExport.getRequesterEmailMessage();
							//if (reuqestTemplate != null) {
							if (reuqestTemplate != null) 
								reuqestTemplate = qpUtil.processFilename(reuqestTemplate, param);

							//requesterMessage = qpUtil.processFilename(requesterMessage, param);
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
							reuqestTemplate = reuqestTemplate.replaceAll("\\{\\{\\{REQUESTED_DATA_TYPE\\}\\}\\}",requestedData);
							//requesterMessage  = requesterMessage.replaceAll("\\{\\{\\{REQUESTED_DATA_TYPE\\}\\}\\}",requestedData);


							ResultType resultType = new ResultType();
							resultType.setName(resultTypeName);
							DataType mdataType = new DataType();
							mdataType.setValue(String.valueOf(recordCount));
							mdataType.setColumn("patientCount");
							mdataType.setType("int");
							resultType.getData().add(mdataType);	
							if (reuqestTemplate != null) {
								mdataType = new DataType();
								mdataType.setValue(reuqestTemplate);
								mdataType.setColumn("RequestEmail");
								mdataType.setType("string");
								resultType.getData().add(mdataType);
							}
							mdataType = new DataType();
							mdataType.setValue(getProjectParam(projectParam, "Data Request Email Address"));
							mdataType.setColumn("DataManagerEmail");
							mdataType.setType("string");
							resultType.getData().add(mdataType);
							mdataType = new DataType();
							mdataType.setValue( new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
							mdataType.setColumn("SUBMITTED");
							mdataType.setType("string");
							resultType.getData().add(mdataType);
							mdataType = new DataType();
							mdataType.setValue( queryDef.getEmail());
							mdataType.setColumn("EMAIL");
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




							if (qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.enabled").equalsIgnoreCase("true") && recordCount != 0) {
								EmailUtil email = new EmailUtil();
								try {


									if ((resultTypeName.equals(finalResultOutput))
											|| (resultTypeName.startsWith("RPDO")) )

										email.email(getProjectParam(projectParam, "Data Request Email Address"), getProjectParam(projectParam, "Data Request Email Address"), getProjectParam(projectParam, "Data Request Subject"), reuqestTemplate);

							
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
								//}
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


	private String getProjectParam(List<ParamType> projectParam, String string) {
		if (projectParam != null)
			for (ParamType param: projectParam)
				if (param.getName().equals(string))
					return param.getValue();
		return null;
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
