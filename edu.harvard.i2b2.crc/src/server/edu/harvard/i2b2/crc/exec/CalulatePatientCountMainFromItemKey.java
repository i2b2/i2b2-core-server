package edu.harvard.i2b2.crc.exec;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryStatusTypeId;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisParamType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.ejb.analysis.QueryMaster;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * 
 * Sample analysis plugin.
 * 
 * Shows how to fetch datasource and uses existing DAOs for following
 * operations.
 * 
 * 
 * a)using instance id to fetch analysis requests xml from the master table
 * 
 * b)write generated result xml to the QT_XML_RESULT table
 * 
 */
public class CalulatePatientCountMainFromItemKey extends CRCDAO {

	private static final String RESULT_NAME = "CALCULATE_PATIENTCOUNT_FROM_CONCEPTPATH";

	public static void main(String args[]) throws Exception {
		CalulatePatientCountMainFromItemKey main1 = new CalulatePatientCountMainFromItemKey();

		// read command line params[domain, project, user and analysis
		// instance id
		String arg = "", domainId = "", projectId = "", userId = "", patientSetId = "", instanceId = "", conceptPath = "";
		int i = 0;
		while (i < args.length) {
			arg = args[i++];
			if (arg.startsWith("-domain_id")) {
				domainId = arg.substring(arg.indexOf('=') + 1);
			} else if (arg.startsWith("-project_id")) {
				projectId = arg.substring(arg.indexOf('=') + 1);
			} else if (arg.startsWith("-user_id")) {
				userId = arg.substring(arg.indexOf('=') + 1);
			} else if (arg.startsWith("-instance_id")) {
				instanceId = arg.substring(arg.indexOf('=') + 1);
			}
		}
		System.out.println("domainId = " + domainId + " project " + projectId
				+ " userid" + userId + " instanceId " + instanceId);

		// call the calculation function
		main1.calculateAndWriteResultXml(projectId, userId, domainId,
				patientSetId, instanceId, conceptPath);
	}

	public void calculateAndWriteResultXml(String projectId, String userId,
			String domainId, String patientSetId, String instanceId,
			String conceptPath) throws Exception {
		boolean errorFlag = false;
		String resultInstanceId = "";
		SetFinderDAOFactory setfinderDaoFactory = null;
		Throwable throwable = null;
		try {

			// find out datasource for the matching domain,project and user id
			DataSourceLookupHelper dataSourceLookupHelper = new DataSourceLookupHelper();
			DataSourceLookup dataSourceLookup = dataSourceLookupHelper
					.matchDataSource(domainId, projectId, userId);

			// inside analysis plugin always instanciate datasource using
			// spring, the
			// jboss container datasource will not work
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			DataSource dataSource = qpUtil.getDataSource(dataSourceLookup
					.getDataSource());
			DAOFactoryHelper daoHelper = new DAOFactoryHelper(dataSourceLookup,
					dataSource);

			// from the dao helper, get the setfinder dao factory
			setfinderDaoFactory = daoHelper.getDAOFactory()
					.getSetFinderDAOFactory();

			// read the analysis definition from the master to perform the
			// calculation
			// //step 1:get master id from the instance id
			IQueryInstanceDao queryInstanceDao = setfinderDaoFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(instanceId);
			String masterId = queryInstance.getQtQueryMaster()
					.getQueryMasterId();

			// //step2:get analysis definition from the master id to read the
			// parameters like concept_path,..
			IQueryMasterDao queryMasterDao = setfinderDaoFactory
					.getQueryMasterDAO();
			QtQueryMaster qtQueryMaster = queryMasterDao
					.getQueryDefinition(masterId);
			String requestXml = qtQueryMaster.getRequestXml();
			System.out.println("The request xml " + requestXml);

			String i2b2RequestXml = qtQueryMaster.getI2b2RequestXml();
			I2B2RequestMessageHelper analysisRequestHelper = new I2B2RequestMessageHelper(
					i2b2RequestXml);
			SecurityType securityType = analysisRequestHelper.getSecurityType();

			QueryMaster queryMasterHelper = new QueryMaster(setfinderDaoFactory);
			AnalysisDefinitionType analysisDefinition = queryMasterHelper
					.getAnalysisDefinitionByMasterId(masterId);
			AnalysisParamType conceptPathParam = analysisDefinition
					.getCrcAnalysisInputParam().getParam().get(0);
			conceptPath = conceptPathParam.getValue();

			// call ontology to get children
	//		CallOntologyUtil callOntologyUtil = buildOntologyUtil(requestXml,
	//				projectId, securityType);
	
	//		ConceptsType conceptsType = callOntologyUtil
	//				.callGetChildrenWithHttpClient(conceptPath);
			
			
			ConceptsType conceptsType = CallOntologyUtil.callGetChildrenWithHttpClient(conceptPath, securityType, projectId);
			
			// build result xml
			String resultXml = buildXmlResult(dataSource, conceptsType,
					setfinderDaoFactory);

			// to write the result xml get the result instance id by instance id
			List<QtQueryResultInstance> resultInstanceList = setfinderDaoFactory
					.getPatientSetResultDAO().getResultInstanceList(instanceId);

			resultInstanceId = resultInstanceList.get(0).getResultInstanceId();

			// writing back the result xml in the result table
			IXmlResultDao xmlResultDao = setfinderDaoFactory.getXmlResultDao();
			xmlResultDao.createQueryXmlResult(resultInstanceId, resultXml);
		} catch (Exception e) {
			e.printStackTrace();
			errorFlag = true;
			// write exception stack trace to the output file
			throwable = e.getCause();
			throw e;
		} finally {

			IQueryResultInstanceDao resultInstanceDao = setfinderDaoFactory
					.getPatientSetResultDAO();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, throwable
								.getMessage(), 0, 0, "");
			} else {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, 0);
			}
		}

	}

	public String buildXmlResult(DataSource dataSource,
			ConceptsType conceptsType, SetFinderDAOFactory sfDAOFactory)
			throws I2B2DAOException {
		this
				.setDbSchemaName(sfDAOFactory.getDataSourceLookup()
						.getFullSchema());

		String tempTableName = "";
		PreparedStatement stmt = null;
		boolean errorFlag = false;
		String itemKey = "";
		Connection conn = null;
		try {

			String itemCountSql = " select count(distinct PATIENT_NUM) as item_count  from "
					+ this.getDbSchemaName()
					+ "observation_fact obs_fact "
					+ " where obs_fact.patient_num in (select patient_num from "
					+ this.getDbSchemaName()
					+ "patient_dimension"
					+ "  ) "
					+ " and obs_fact.concept_cd in (select concept_cd from "
					+ this.getDbSchemaName()
					+ "concept_dimension where concept_path like ?)";
			ResultType resultType = new ResultType();
			resultType.setName(RESULT_NAME);
			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(itemCountSql);
			for (ConceptType conceptType : conceptsType.getConcept()) {

				// build results
				stmt.setString(1, conceptType.getDimcode() + "%");
				System.out
						.println("Executing count sql [" + itemCountSql + "]");
				ResultSet resultSet = stmt.executeQuery();
				resultSet.next();
				String demoCount = resultSet.getString("item_count");
				DataType mdataType = new DataType();
				mdataType.setValue(demoCount);
				mdataType.setColumn(conceptType.getName());
				mdataType.setType("int");
				resultType.getData().add(mdataType);
			}

			edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createResult(resultType));
			ResultEnvelopeType resultEnvelop = new ResultEnvelopeType();
			resultEnvelop.setBody(bodyType);

			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

			StringWriter strWriter = new StringWriter();

			jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop),
					strWriter);

			return strWriter.toString();

		} catch (Exception sqlEx) {
			log.error("QueryResultPatientSetGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/*
	private CallOntologyUtil buildOntologyUtils(String requestXml,
			String projectId, SecurityType securityType) {
		//CallOntologyUtil callOntologyUtil = null;
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.delegate.ontology.url");
			String getChildrenOperationName = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.delegate.ontology.operation.getchildren");
			String ontologyGetChildrenUrl = ontologyUrl
					+ getChildrenOperationName;
			log.debug("Ontology getChildren url from property file ["
					+ ontologyGetChildrenUrl + "]");

			SecurityType serviceSecurityType = PMServiceAccountUtil
					.getServiceSecurityType(securityType.getDomain());
			// callOntologyUtil = new CallOntologyUtil(ontologyUrl, requestXml);
			callOntologyUtil = new CallOntologyUtil(ontologyGetChildrenUrl,
					serviceSecurityType, projectId);
		} catch (I2B2Exception e) {
			e.printStackTrace();
		}
		return callOntologyUtil;
	}
	*/
}
