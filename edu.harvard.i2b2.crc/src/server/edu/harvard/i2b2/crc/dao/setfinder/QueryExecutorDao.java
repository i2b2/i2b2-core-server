package edu.harvard.i2b2.crc.dao.setfinder;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
import org.springframework.beans.factory.BeanFactory;

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
import edu.harvard.i2b2.crc.datavo.pm.RoleType;
import edu.harvard.i2b2.crc.datavo.pm.RolesType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.delegate.ejbpm.EJBPMUtil;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryExecutorDao extends CRCDAO implements IQueryExecutorDao {

	private DataSourceLookup dataSourceLookup = null,
			originalDataSourceLookup = null;
	private static Map generatorMap = null;
	private static String defaultResultType = null;
	private Map projectParamMap = new HashMap();
	private boolean queryWithoutTempTableFlag = false;

	static {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		BeanFactory bf = qpUtil.getSpringBeanFactory();
		generatorMap = (Map) bf.getBean("setFinderResultGeneratorMap");
		defaultResultType = (String) bf.getBean("defaultSetfinderResultType");
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
	
	
	/**
	 * This function executes the given sql and create query result instance and
	 * its collection
	 * 
	 * @param conn
	 *            db connection
	 * @param sqlString
	 * @param queryInstanceId
	 * @return query result instance id
	 * @throws JAXBUtilException 
	 * @throws I2B2Exception 
	 */
	public String executeSQL(
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList, boolean allowLargeTextValueConstrainFlag, String pmXml)
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
						DAOFactoryHelper.POSTGRESQL)) {
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
				requestDao.setQueryWithoutTempTableFlag(queryWithoutTempTableFlag);
				
				String[] sqlResult = null;
				if (this.queryWithoutTempTableFlag == false) { 
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

				queryMasterDao.updateQueryAfterRun(masterId, generatedSql, queryType);
				
				if (missingItemMessage != null
						&& missingItemMessage.trim().length() > 1) {
					log.debug("Setfinder query missing item message not null" + missingItemMessage);
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
