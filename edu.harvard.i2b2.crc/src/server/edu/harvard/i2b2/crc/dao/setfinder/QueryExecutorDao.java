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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
//import org.springframework.beans.factory.BeanFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.DirectQueryForSinglePanel;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.pm.RoleType;
import edu.harvard.i2b2.crc.datavo.pm.RolesType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.delegate.ejbpm.EJBPMUtil;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.ejb.ExecRunnable;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

public class QueryExecutorDao extends CRCDAO implements IQueryExecutorDao {


	protected static Logger logesapi = ESAPI.getLogger(ExecRunnable.class);


	private DataSourceLookup dataSourceLookup = null,
			originalDataSourceLookup = null;
	//	private static Map generatorMap = null;
	//	private static String defaultResultType = null;
	private Map projectParamMap = new HashMap();
	private boolean queryWithoutTempTableFlag = false;
	private boolean queryCountMinSketchFlag = false;
	static {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		//	qpUtil.getManualConnection()

		//		BeanFactory bf = qpUtil.getSpringBeanFactory();
		//	generatorMap = (Map) bf.getBean("setFinderResultGeneratorMap");
		//	defaultResultType = (String) bf.getBean("defaultSetfinderResultType");
	}

	public QueryExecutorDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup,
			DataSourceLookup originalDataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.originalDataSourceLookup = originalDataSourceLookup;
	}

	//
	public void setQueryWithoutTempTableFlag(boolean queryWithoutTempTableFlag) { 
		this.queryWithoutTempTableFlag = queryWithoutTempTableFlag;
	}

	public void setQueryCountMinSketchFlag(boolean queryCountMinSketchFlag) {
		this.queryCountMinSketchFlag = queryCountMinSketchFlag;
	}
	/**
	 * This function executes the given sql and create query result instance and
	 * its collection
	 * 
	 * @param conn
	 *            db connection
	 * @param sqlString
	 * @param queryInstanceId
	 * @param userRoles 
	 * @return query result instance id
	 * @throws JAXBUtilException 
	 * @throws I2B2Exception 
	 */
	@Override
	public String executeSQL(
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList, boolean allowLargeTextValueConstrainFlag,   String pmXml, List<String> userRoles)
					throws I2B2Exception, JAXBUtilException {

		String singleSql = null;
		int recordCount = 0;

		boolean errorFlag = false, timeOutErrorFlag = false;
		Statement stmt = null;
		ResultSet resultSet = null;
		Connection manualConnection = null;
		/** Global temp table to store intermediate setfinder results* */
		String TEMP_TABLE = "#GLOBAL_TEMP_TABLE";

		/** Global temp table to store intermediate patient list * */
		String TEMP_DX_TABLE = "#DX";
		if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			TEMP_TABLE = getDbSchemaName() + "#GLOBAL_TEMP_TABLE";
			TEMP_DX_TABLE = getDbSchemaName() + "#DX";

		} else if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL) || dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SNOWFLAKE)) {
			TEMP_TABLE = getDbSchemaName() + "QUERY_GLOBAL_TEMP";
			TEMP_DX_TABLE = getDbSchemaName() + "DX";
		}
		Exception exception = null;

		InitialContext context;
		try {
			context = new InitialContext();

			String processTimingFlag = LogTimingUtil.getPocessTiming(originalDataSourceLookup.getProjectPath(), originalDataSourceLookup.getOwnerId(), 
					originalDataSourceLookup.getDomainId());
			if (processTimingFlag == null) { 
				processTimingFlag = ProcessTimingReportUtil.NONE;
			}

			projectParamMap.put(ParamUtil.PM_ENABLE_PROCESS_TIMING, processTimingFlag);
			ParamUtil projectParamUtil = new ParamUtil(); 
			String unitConversionFlag = projectParamUtil.getParam(originalDataSourceLookup.getProjectPath(), originalDataSourceLookup.getOwnerId(), 
					originalDataSourceLookup.getDomainId(), ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
			if (unitConversionFlag != null) { 
				projectParamMap.put(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION, unitConversionFlag.trim());
			}


			// change status of result instance to running
			IQueryResultInstanceDao psResultDao = sfDAOFactory
					.getPatientSetResultDAO();
			psResultDao.updatePatientSet(patientSetId, 2, 0);


			// check if the sql is stored, else generate and store
			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			IQueryInstanceDao queryInstaneDao = sfDAOFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstaneDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			String masterId = queryInstance.getQtQueryMaster()
					.getQueryMasterId();
			QtQueryMaster queryMaster = queryMasterDao
					.getQueryDefinition(masterId);
			String generatedSql = queryMaster.getGeneratedSql();
			if (generatedSql == null) {
				generatedSql = "";
			}
			String missingItemMessage = "", processTimingMessage = "";
			boolean missingItemFlag = false;
			String queryType = null;

			if (generatedSql.trim().length() == 0) {
				// check if the sql is for patient set or encounter set
				boolean encounterSetFlag = this
						.getEncounterSetFlag(resultOutputList);

				// generate sql and store
				IQueryRequestDao requestDao = sfDAOFactory.getQueryRequestDAO();
				requestDao.setProjectParam(projectParamMap) ;
				requestDao.setAllowLargeTextValueConstrainFlag(allowLargeTextValueConstrainFlag);
				//requestDao.setAllowProtectedQueryFlag(allowProtectedQueryFlag);
				requestDao.setQueryWithoutTempTableFlag(queryWithoutTempTableFlag);
				requestDao.setUserRoles(userRoles);

				String[] sqlResult = null;
				if (this.queryCountMinSketchFlag) {
					//generate sql for each panel
					try { 
						RequestMessageType reqMsgType = this.getRequestMessageType(requestXml);
						QueryDefinitionRequestType queryDefRequestType = this.getQueryDefinitionRequestType(reqMsgType);
						PanelType[] panelList = queryDefRequestType.getQueryDefinition().getPanel().toArray(new PanelType[]{});
						String newRequestMsg = "";

						/*
						 * create table  qt_est_observation_fact 
	as

	select  lower(c.concept_path) as concept_path, o.patient_num as patient_num, min(o.nval_num) as min_nval_num, max(o.nval_num) as max_nval_num, 
	count(patient_num) as instance_num, avg(o.nval_num) as avg_nval_num, 
	min(o.start_date) as first_start_date, max(o.start_date) as last_start_date
	from observation_fact o, concept_dimension c
	where
	o.concept_cd = c.concept_cd
	group by o.patient_num, c.concept_path 


	select concat('<breakDown><patientCount>', count(nval_num),'</patientCount><valueData>', cast(nval_num as integer), '</valueData></breakDown>')
	from observation_fact 
	where concept_cd = 'LOINC:14815-5'
	and nval_num is not null
	group by cast(nval_num as integer)

	SELECT cast(nval_num as integer) as a,
	        COUNT(*) AS Count
	FROM    
	        (SELECT
	             nval_num,
	             NTILE(70) OVER (ORDER BY m.nval_num) AS Buckets
	         FROM
	             observation_fact m
	                where concept_cd = 'LOINC:9830-1'

	        ) m
	WHERE   
	      Buckets BETWEEN 2 AND 69
	GROUP BY cast(nval_num as integer)



	truncate table calc_qt_breakdown
	GO
	insert into calc_qt_breakdown
	select 'sex_cd', sex_cd as patient_range, count(distinct patient_num) as patient_count 
	from patient_dimension  group by sex_cd order by 1
	GO
	insert into calc_qt_breakdown
	select 'race_cd', b.c_name as patient_range, count(distinct a.patient_num) as patient_count 
	from patient_dimension a, i2b2 b where a.race_cd = b.c_dimcode group by a.race_cd, b.c_name order by 1
	GO
	insert into calc_qt_breakdown
	select 'total_counts', 'patients', count(distinct patient_num) from observation_fact 
	GO
	insert into calc_qt_breakdown
	select 'total_counts', 'encounters', count(distinct encounter_num) from observation_fact 
	GO
	insert into calc_qt_breakdown
	select 'total_counts', 'diagnosis', count( patient_num) from observation_fact where concept_cd IN (select concept_cd from  public.concept_dimension   where concept_path LIKE '\\i2b2\\Diagnoses\\%' )
	GO
	insert into calc_qt_breakdown
	select 'total_counts', 'Labtests', count( patient_num) from observation_fact where concept_cd IN (select concept_cd from  public.concept_dimension   where concept_path LIKE '\\i2b2\\Labtests\\%' )
	GO
	insert into calc_qt_breakdown
	select 'total_counts', 'medications', count( patient_num) from observation_fact where concept_cd IN (select concept_cd from  public.concept_dimension   where concept_path LIKE '\\i2b2\\Medications\\%' )
	GO
	insert into calc_qt_breakdown
	select 'top10diag', b.name_char as patient_range, count(distinct a.patient_num) as patient_count from observation_fact a, concept_dimension b  where a.concept_cd = b.concept_cd and concept_path like '\\i2b2\\Diagnoses\\%'   group by name_char order by patient_count desc limit 10
	GO
	insert into calc_qt_breakdown
	select 'age_cd', '>= 85 years old',  count(distinct patient_num) from patient_dimension where birth_date 	<=	(CURRENT_DATE - INTERVAL '31047.25 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '35-44 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '16437.25 day') AND (CURRENT_DATE - INTERVAL '12784.75 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd', '>= 65 years old',  count(distinct patient_num) from patient_dimension where birth_date 	<=	(CURRENT_DATE - INTERVAL '23741.25 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '75-84 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '31047.25 day') AND (CURRENT_DATE - INTERVAL '27394.75 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '55-64 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '23742.25 day') AND (CURRENT_DATE - INTERVAL '20089.75 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '45-54 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '20089.75 day') AND (CURRENT_DATE - INTERVAL '16437.25 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '0-9 years old',  count(distinct patient_num) from patient_dimension where birth_date 	>	(CURRENT_DATE - INTERVAL '3653.5 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '18-34 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '12784.75 day') AND (CURRENT_DATE - INTERVAL '6575.5 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '10-17 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '6575.5 day') AND (CURRENT_DATE - INTERVAL '3653.5 day')
	GO
	insert into calc_qt_breakdown
	select 'age_cd',   '65-74 years old',  count(distinct patient_num) from patient_dimension where birth_date 	BETWEEN	(CURRENT_DATE - INTERVAL '27394.75 day') AND (CURRENT_DATE - INTERVAL '23742.25 day')
	GO
	select * from calc_qt_breakdown

						 */

						generatedSql = "";
						for (int i =0; i < panelList.length; i++) { 
							//PanelType panelType = panelList[i];
							//buildRequestXml(panelType);
							//queryDefRequestType.getQueryDefinition().getPanel().clear(); 
							log.debug("Setfinder query panel count " + panelList.length);
							//queryDefRequestType.getQueryDefinition().getPanel().add(panelType);
							//newRequestMsg = this.buildRequestMessage(reqMsgType, queryDefRequestType); 

							log.debug("Single panel request message [" + newRequestMsg + "]");
							//send request xml for each panel


							
							
							generatedSql += "select distinct patient_num from "+ this.getDbSchemaName() +"qt_est_observation_fact where " ;

							for (int j=0; j< panelList[i].getItem().size(); j++)
							{
								ItemType item = panelList[i].getItem().get(j);
								String key = item.getItemKey();


								ConceptType conceptType = null;
								try {
									


									SecurityType securityType = PMServiceAccountUtil
											.getServiceSecurityType(dataSourceLookup.getDomainId());
									
		
										conceptType = CallOntologyUtil.callOntology(key,
												securityType, dataSourceLookup.getProjectPath(),
												QueryProcessorUtil.getInstance().getOntologyUrl());
									
								} catch (Exception e) {

									log.error("Error while fetching metadata [" + key
											+ "] from ontology ", e);
									throw new OntologyException("Error while fetching metadata ["
											+ key + "] from ontology "
											+ StackTraceUtil.getStackTrace(e));
								}
								
								key = key.substring(key.indexOf('\\', 3)).toLowerCase();
								if (dsLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL) ||
										dsLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.SNOWFLAKE))
									key = key.replace("\\", "\\\\");
								
								generatedSql += " ( concept_path LIKE '" + key + "%'";
								if ( item.getConstrainByValue() != null)
								{
									for (ItemType.ConstrainByValue valueConstrain : item.getConstrainByValue()) {

										ConstrainValueType valueType = valueConstrain.getValueType();
										ConstrainOperatorType operatorType = valueConstrain
												.getValueOperator();
										String value = valueConstrain.getValueConstraint();

										if (valueType.equals(ConstrainValueType.NUMBER)) {
											// check if operator and value not null
											if (operatorType == null || value == null) {
												continue;
											}


											if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.GT.value())) {
												generatedSql +=
														" and min_nval_num > " + value;
											} else if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.GE.value())) {
												generatedSql += " and min_nval_num >= " + value;
											} else if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.LT.value())) {
												generatedSql += " and min_nval_num <  " + value;
											} else if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.LE.value())) {
												generatedSql += " and min_nval_num <=  " + value;
											} else if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.EQ.value())) {
												generatedSql += " and min_nval_num = " + value
														+ " and max_nval_num = " + value;
											} else if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.BETWEEN.value())) {
												generatedSql += " and min_nval_num >  " + value
														+ " and max_nval_num < " + value;
											} else if (operatorType.value().equalsIgnoreCase(
													ConstrainOperatorType.NE.value())) {
												generatedSql += " and min_nval_num <  " + value;
											} else {
												throw new I2B2DAOException(
														"Error NUMBER value constrain because operator("
																+ operatorType.toString() + ")is invalid");
											}											

										}
									}
								}

								if (panelList[i].getItem().size()  != (j + 1))
									generatedSql += " ) or ";
								else
									generatedSql += " ) ";

							}
							if (panelList.length != (i + 1))
								generatedSql += " union all ";

						}

						generatedSql = "select count(*) as patient_num_count from (select count( *) as mycount,  patient_num from  (" +
								generatedSql +
								" ) as a group by patient_num ) as b where mycount = " + panelList.length; 

						log.debug("Setfinder converted sql without temp table " + generatedSql);

					} catch (JAXBUtilException e) { 
						e.printStackTrace();
					} catch (I2B2Exception e) { 
						e.printStackTrace();
					}
					log.debug("Setfinder skip temp table generated sql " + generatedSql);
					log.debug("Setfinder skip temp table missing item message " +  missingItemMessage);
					log.debug("Setfinder skip temp table process timing message " + processTimingMessage);
				}
				else if (this.queryWithoutTempTableFlag == false) { 
					sqlResult = requestDao.buildSql(requestXml,
							encounterSetFlag);
					generatedSql = sqlResult[0];
					missingItemMessage = sqlResult[1];
					processTimingMessage = sqlResult[2];
					if (sqlResult.length>3)
						queryType = sqlResult[3];
				} else {
					//generate sql for each panel
					try { 
						RequestMessageType reqMsgType = this.getRequestMessageType(requestXml);
						QueryDefinitionRequestType queryDefRequestType = this.getQueryDefinitionRequestType(reqMsgType);
						PanelType[] panelList = queryDefRequestType.getQueryDefinition().getPanel().toArray(new PanelType[]{});
						String newRequestMsg = "";
						boolean buildSqlWithOR = true;

						boolean fullSqlGenerated = false;
						while (!fullSqlGenerated) { 
							generatedSql = "";
							for (int i =0; i < panelList.length; i++) { 
								PanelType panelType = panelList[i];
								//buildRequestXml(panelType);
								queryDefRequestType.getQueryDefinition().getPanel().clear(); 
								log.debug("Setfinder query panel count " + panelList.length);
								queryDefRequestType.getQueryDefinition().getPanel().add(panelType);
								newRequestMsg = this.buildRequestMessage(reqMsgType, queryDefRequestType); 

								log.debug("Single panel request message [" + newRequestMsg + "]");
								//send request xml for each panel
								sqlResult = requestDao.buildSql(newRequestMsg,
										encounterSetFlag);
								DirectQueryForSinglePanel directQuerySql = new DirectQueryForSinglePanel(); 
								if (buildSqlWithOR == false) { 
									generatedSql += "\n(" + directQuerySql.buildSqlWithUnion(sqlResult[0]) + ")\n";
									if (i+1 < panelList.length) { 
										generatedSql += " INTERSECT  \n";
									}
								} else if (buildSqlWithOR == true && (sqlResult[0].indexOf("patient_dimension where")>0 ||
										sqlResult[0].indexOf("visit_dimension where")>0)) { 
									buildSqlWithOR = false;
									fullSqlGenerated = false;
									break;
								} else { 
									generatedSql += "\n(" + directQuerySql.buildSqlWithOR(sqlResult[0]) + ")\n";
									if (i+1 < panelList.length) { 
										generatedSql += " INTERSECT \n";
										generatedSql += "select patient_num from " + this.getDbSchemaName()  +"observation_fact f where \n";
									}
								}

								if (sqlResult[1] != null && sqlResult[1].trim().length()>0) { 
									missingItemMessage += sqlResult[1];	
								}
								if (sqlResult[2] != null && sqlResult[2].trim().length()>0) { 
									processTimingMessage += sqlResult[2];
								}
								if (sqlResult[3] != null && sqlResult[3].trim().length()>0) { 
									queryType = sqlResult[3];
								}
								fullSqlGenerated = true;
							}
						}

						if (buildSqlWithOR)  { 
							generatedSql = "select patient_num from " + this.getDbSchemaName()  +"observation_fact f where " + generatedSql;
						}
						generatedSql = "select count(distinct patient_num) as patient_num_count from ( \n" + generatedSql + " \n ) allitem ";

						logesapi.debug(null,"Setfinder converted sql without temp table " + generatedSql);

					} catch (JAXBUtilException e) { 
						e.printStackTrace();
					} catch (I2B2Exception e) { 
						e.printStackTrace();
					}
					logesapi.debug(null,"Setfinder skip temp table generated sql " + generatedSql);
					logesapi.debug(null,"Setfinder skip temp table missing item message " +  missingItemMessage);
					logesapi.debug(null,"Setfinder skip temp table process timing message " + processTimingMessage);
				}
				queryMasterDao.updateQueryAfterRun(masterId, generatedSql, queryType);

				if (missingItemMessage != null
						&& missingItemMessage.trim().length() > 1) {
					logesapi.debug(null,"Setfinder query missing item message not null" + missingItemMessage);
					missingItemFlag = true;

					queryInstance.setEndDate(new Date(System
							.currentTimeMillis()));
					// queryInstance.setMessage(missingItemMessage);
					setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 4,
							missingItemMessage);
					// update the error status to result instance
					setQueryResultInstanceStatus(sfDAOFactory, queryInstanceId,
							4, missingItemMessage);
					throw new I2B2DAOException("Concept missing");

				}

				if (processTimingMessage != null && processTimingMessage.trim().length()>0) {

					setQueryInstanceProcessTimingXml(sfDAOFactory,
							queryInstanceId,   processTimingMessage);

				}

			}
			log.debug("Setfinder before executor helper dao missingItemFlag " + missingItemFlag);
			if (missingItemFlag == false) {
				QueryExecutorHelperDao helperDao = new QueryExecutorHelperDao(
						dataSource, dataSourceLookup, originalDataSourceLookup);
				helperDao.setProcessTimingFlag(processTimingFlag);
				helperDao.setQueryWithoutTempTableFlag(this.queryWithoutTempTableFlag);
				if (this.queryCountMinSketchFlag)
					helperDao.setQueryWithoutTempTableFlag(this.queryCountMinSketchFlag);
				helperDao.executeQuery( transactionTimeout,
						dsLookup, sfDAOFactory, requestXml, sqlString,
						queryInstanceId, patientSetId, resultOutputList,
						generatedSql, pmXml);

			}
		} catch (NamingException e) {
			exception = e;
			errorFlag = true;
		} catch (SecurityException e) {
			exception = e;
			errorFlag = true;
		} catch (IllegalStateException e) {
			exception = e;
			errorFlag = true;
		} catch (CRCTimeOutException e) {
			throw e;
		} catch (I2B2DAOException e) {

			setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 4,
					e.getMessage());
			// update the error status to result instance
			setQueryResultInstanceStatus(sfDAOFactory, queryInstanceId,
					4, e.getMessage());

			log.debug("Error in QueryExecutorDAO Throwing: " + e.getMessage());
			exception = e;
			errorFlag = true;
			throw e;
		} finally {
			// close resultset and statement
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				resultSet = null;
				if (stmt != null) {
					stmt.close();
				}
				stmt = null;
				if (manualConnection != null) {
					manualConnection.close();
				}
				manualConnection = null;

			} catch (SQLException sqle) {
				log.error("Error closing statement/resultset ", sqle);
			}
		}
		return patientSetId;
	}

	private void setQueryInstanceStatus(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, int statusTypeId, String message) throws I2B2DAOException {
		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(queryInstanceId);

		QtQueryStatusType queryStatusType = new QtQueryStatusType();
		queryStatusType.setStatusTypeId(statusTypeId);
		queryInstance.setQtQueryStatusType(queryStatusType);
		queryInstance.setEndDate(new Date(System.currentTimeMillis()));
		queryInstance.setMessage(message);
		queryInstanceDao.update(queryInstance, true);
	}

	private void setQueryInstanceProcessTimingXml(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId,  String message) throws I2B2DAOException {
		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		queryInstanceDao.updateMessage(queryInstanceId, message, true); 

	}

	private void setQueryResultInstanceStatus(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, int statusTypeId, String message) {
		IQueryResultInstanceDao queryResultInstanceDao = sfDAOFactory
				.getPatientSetResultDAO();
		List<QtQueryResultInstance> resultInstanceList = queryResultInstanceDao
				.getResultInstanceList(queryInstanceId);
		for (QtQueryResultInstance queryResultInstance : resultInstanceList) {
			queryResultInstanceDao.updatePatientSet(queryResultInstance
					.getResultInstanceId(), statusTypeId, message, -1, -1, "");
		}

	}



	public boolean getEncounterSetFlag(
			ResultOutputOptionListType resultOutputList) {
		boolean encounterFoundFlag = false;
		for (ResultOutputOptionType resultOutputOption : resultOutputList
				.getResultOutput()) {
			if (resultOutputOption.getName().equalsIgnoreCase(
					"PATIENT_ENCOUNTER_SET")) {
				encounterFoundFlag = true;
				break;
			}
		}
		return encounterFoundFlag;
	}

	/**
	 * Call PM to get user roles. The security info is taken from the request
	 * xml
	 * 
	 * @param requestXml
	 * @return
	 * @throws I2B2Exception
	 */
	public List<String> getRoleFromPM(String requestXml) throws I2B2Exception {

		I2B2RequestMessageHelper reqMsgHelper = new I2B2RequestMessageHelper(
				requestXml);
		SecurityType origSecurityType = reqMsgHelper.getSecurityType();
		String projectId = reqMsgHelper.getProjectId();

		SecurityType serviceSecurityType = PMServiceAccountUtil
				.getServiceSecurityType(origSecurityType.getDomain());
		//EJBPMUtil callPMUtil = new EJBPMUtil(serviceSecurityType, projectId);
		List<String> roleList = new ArrayList<String>();
		try {
			//RolesType rolesType = callPMUtil.callGetRole(origSecurityType
			//		.getUsername(), projectId);
			RolesType rolesType = EJBPMUtil.callGetRole(origSecurityType
					.getUsername(), origSecurityType, projectId, QueryProcessorUtil.getInstance()
					.getProjectManagementCellUrl());


			RoleType roleType = null;
			for (java.util.Iterator<RoleType> iterator = rolesType.getRole()
					.iterator(); iterator.hasNext();) {
				roleType = iterator.next();
				roleList.add(roleType.getRole());
			}

		} catch (AxisFault e) {
			throw new I2B2Exception(" Failed to get user role from PM "
					+ StackTraceUtil.getStackTrace(e));
		}
		return roleList;
		/*
		 * I2B2RequestMessageHelper reqMsgHelper = new I2B2RequestMessageHelper(
		 * requestXml); SecurityType securityType =
		 * reqMsgHelper.getSecurityType(); String projectId =
		 * reqMsgHelper.getProjectId(); // get roles from pm driver
		 * PMServiceDriver serviceDriver = new PMServiceDriver(); ProjectType
		 * projectType = null;
		 * 
		 * try { projectType = serviceDriver.checkValidUser(securityType,
		 * projectId); } catch (AxisFault e) { e.printStackTrace(); throw new
		 * I2B2Exception(" Failed to get user role from PM " +
		 * StackTraceUtil.getStackTrace(e)); } catch (JAXBUtilException e) {
		 * e.printStackTrace(); throw new
		 * I2B2Exception(" Failed to get user role from PM " +
		 * StackTraceUtil.getStackTrace(e)); } return projectType.getRole();
		 */
	}

	private RequestMessageType  getRequestMessageType (String xmlRequest) throws I2B2Exception, JAXBUtilException  {
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

		if (jaxbElement == null) {
			throw new I2B2Exception(
					"null value in after unmarshalling request string ");
		}

		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		return requestMessageType;
	}


	public QueryDefinitionRequestType  getQueryDefinitionRequestType(RequestMessageType requestMessageType) throws JAXBUtilException { 
		BodyType bodyType = requestMessageType.getMessageBody();
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		QueryDefinitionRequestType queryDefReqType = (QueryDefinitionRequestType) unWrapHelper
				.getObjectByClass(bodyType.getAny(),
						QueryDefinitionRequestType.class);
		return queryDefReqType;


	}


	private String buildRequestMessage(RequestMessageType requestMessageType , QueryDefinitionRequestType queryDefRequestType) throws JAXBUtilException{
		edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory setfinderOf = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
		requestMessageType.getMessageBody().getAny().add(setfinderOf.createRequest(queryDefRequestType));
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		ObjectFactory ob = new ObjectFactory();
		jaxbUtil.marshaller(ob.createRequest(requestMessageType), strWriter);
		return strWriter.toString();
	}

}
