package edu.harvard.i2b2.crc.dao.setfinder;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;

public class QueryResultPatientCountGenerator extends CRCDAO implements
		IResultGenerator {

	public void generateResult(Map param) throws I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");
		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		List<String> roles = (List<String>) param.get("Roles");
		int obfucatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		int recordCount = (Integer) param.get("RecordCount");
		String resultTypeName = (String) param.get("ResultOptionName");
		Boolean queryWithoutTempTableFlag = (Boolean) param
				.get("QueryWithoutTempTableFlag");
		boolean dataObfusRole = (Boolean)param.get("ObfuscatedRoleFlag");
		
		this.setDbSchemaName(sfDAOFactory.getDataSourceLookup().getFullSchema());

		boolean errorFlag = false;
		int patientCount = 0, realPatientCount = 0;
		
		try {

				if (dataObfusRole) {
					patientCount = obfucatedRecordCount;
					realPatientCount = recordCount;
				} else {
					patientCount = recordCount;
					realPatientCount = recordCount;
					obfucatedRecordCount = recordCount;
				}
			
			int i = 0;
			ResultType resultType = new ResultType();
			resultType.setName("PATIENT_COUNT_XML");
			DataType mdataType = new DataType();
			mdataType.setValue(String.valueOf(patientCount));
			mdataType.setColumn("patient_count");
			mdataType.setType("int");
			resultType.getData().add(mdataType);

			edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createResult(resultType));
			ResultEnvelopeType resultEnvelope = new ResultEnvelopeType();
			resultEnvelope.setBody(bodyType);

			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

			StringWriter strWriter = new StringWriter();

			jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelope),
					strWriter);

			IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
			xmlResultDao.createQueryXmlResult(resultInstanceId,
					strWriter.toString());
		} catch (Exception sqlEx) {
			log.error(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);
		} finally {
			IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				String obfusMethod = "", description = null;
				if (dataObfusRole) {
					obfusMethod = IQueryResultInstanceDao.OBSUBTOTAL;
					// add () to the result type description
					// read the description from result type
				}

				IQueryResultTypeDao resultTypeDao = sfDAOFactory
						.getQueryResultTypeDao();
				List<QtQueryResultType> resultTypeList = resultTypeDao
						.getQueryResultTypeByName(resultTypeName);

				String queryName = sfDAOFactory
						.getQueryMasterDAO()
						.getQueryDefinition(
								sfDAOFactory
										.getQueryInstanceDAO()
										.getQueryInstanceByInstanceId(
												queryInstanceId)
										.getQtQueryMaster().getQueryMasterId())
						.getName();
				description = resultTypeList.get(0).getDescription()
						+ " for \"" + queryName + "\"";

				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, "",
						obfucatedRecordCount, realPatientCount, obfusMethod);

				resultInstanceDao.updateResultInstanceDescription(
						resultInstanceId, description);
			}
		}
	}

	
}
