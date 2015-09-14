package edu.harvard.i2b2.crc.exec;

import java.io.StringWriter;
import java.util.List;

import javax.sql.DataSource;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.ejb.analysis.QueryMaster;
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
public class CalulatePatientCountMain {

	private static final String RESULT_NAME = "CALCULATE_PATIENTCOUNT_BY_PATIENTSET_ID";

	public static void main(String args[]) throws Exception {
		CalulatePatientCountMain main1 = new CalulatePatientCountMain();

		// read command line params[domain, project, user and analysis
		// instance id
		String arg = "", domainId = "", projectId = "", userId = "", patientSetId = "", instanceId = "";
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
				+ " userid" + userId + " instanceId " + instanceId + "*");

		// call the calculation function
		main1.calculateAndWriteResultXml(projectId, userId, domainId,
				patientSetId, instanceId);
	}

	public void calculateAndWriteResultXml(String projectId, String userId,
			String domainId, String patientSetId, String instanceId)
			throws Exception {
		try {

			// find out datasource for the matching domain,project and user id
			DataSourceLookupHelper dataSourceLookupHelper = new DataSourceLookupHelper();
			DataSourceLookup dataSourceLookup = dataSourceLookupHelper
					.matchDataSource(domainId, projectId, userId);

			// inside analysis plugin always instanciate datasource using
			// spring, the
			// jboss container datasource will not work
			DataSource dataSource = QueryProcessorUtil.getInstance()
					.getDataSource(dataSourceLookup.getDataSource());
			DAOFactoryHelper daoHelper = new DAOFactoryHelper(dataSourceLookup,
					dataSource);

			// from the dao helper, get the setfinder dao factory
			SetFinderDAOFactory setfinderDaoFactory = daoHelper.getDAOFactory()
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
			// //step2:get analysis definition from the master id
			QueryMaster queryMasterHelper = new QueryMaster(setfinderDaoFactory);
			AnalysisDefinitionType analysisDefinition = queryMasterHelper
					.getAnalysisDefinitionByMasterId(masterId);

			// just generating random number, not calculating patitient set size
			// using analysis definition info
			java.util.Random r = new java.util.Random();
			int setSize = r.nextInt();

			// build result xml
			String resultXml = buildXmlResult(RESULT_NAME, setSize);

			// to write the result xml get the result instance id by instance id
			List<QtQueryResultInstance> resultInstanceList = setfinderDaoFactory
					.getPatientSetResultDAO().getResultInstanceList(instanceId);

			String resultInstanceId = resultInstanceList.get(0)
					.getResultInstanceId();

			// writing back the result xml in the result table
			IXmlResultDao xmlResultDao = setfinderDaoFactory.getXmlResultDao();
			xmlResultDao.createQueryXmlResult(resultInstanceId, resultXml);
		} catch (Exception e) {
			// write exception stack trace to the output file
			e.printStackTrace();
			throw e;
		}

	}

	private String buildXmlResult(String resultName, int setSize)
			throws JAXBUtilException {
		DataType dataType = new DataType();
		dataType.setValue(String.valueOf(setSize));
		dataType.setColumn("count");
		dataType.setType("int");

		ResultType resultType = new ResultType();
		resultType.setName(resultName);
		resultType.getData().add(dataType);
		edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();

		edu.harvard.i2b2.crc.datavo.i2b2result.BodyType bodyType = new edu.harvard.i2b2.crc.datavo.i2b2result.BodyType();
		bodyType.getAny().add(of.createResult(resultType));
		ResultEnvelopeType resultEnvelopeType = new ResultEnvelopeType();
		resultEnvelopeType.setBody(bodyType);

		StringWriter strWriter = new StringWriter();
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelopeType),
				strWriter);

		System.out.println("Results marshalled" + strWriter.toString());
		return strWriter.toString();
	}
}
