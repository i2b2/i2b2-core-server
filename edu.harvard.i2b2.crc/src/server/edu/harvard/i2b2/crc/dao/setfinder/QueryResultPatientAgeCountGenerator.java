package edu.harvard.i2b2.crc.dao.setfinder;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;

public class QueryResultPatientAgeCountGenerator extends CRCDAO implements
		IResultGenerator {

	public static final String RESULT_NAME = "PATIENT_AGE_COUNT_XML";

	public void generateResult(Map param) throws I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");
		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		this
				.setDbSchemaName(sfDAOFactory.getDataSourceLookup()
						.getFullSchema());

		String age1CountSql = "select count(distinct dx.patient_num) patient_count, '0-20' patient_range"
				+ " from "
				+ this.getDbSchemaName()
				+ "patient_dimension pd ,"
				+ TEMP_DX_TABLE
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 0 and 20";

		String age2CountSql = "select count(dx.patient_num) patient_count, '21-40' patient_range"
				+ " from "
				+ this.getDbSchemaName()
				+ "patient_dimension pd ,"
				+ TEMP_DX_TABLE
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 21 and 40";

		String age3CountSql = "select count(dx.patient_num) patient_count, '41-60' patient_range"
				+ " from "
				+ this.getDbSchemaName()
				+ "patient_dimension pd ,"
				+ TEMP_DX_TABLE
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 41 and 60";

		String age4CountSql = "select count(dx.patient_num) patient_count, '61-80' patient_range"
				+ " from "
				+ this.getDbSchemaName()
				+ "patient_dimension pd ,"
				+ TEMP_DX_TABLE
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 61 and 80";

		String age5CountSql = "select count(dx.patient_num) patient_count, '>80' patient_range"
				+ " from "
				+ this.getDbSchemaName()
				+ "patient_dimension pd ,"
				+ TEMP_DX_TABLE
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num > 80";

		String ageFinalCountSql = age1CountSql + "\n union all " + age2CountSql
				+ "\n union all " + age3CountSql + "\n union all "
				+ age4CountSql + "\n union all " + age5CountSql;

		boolean errorFlag = false;
		int totalCount = 0;
		try {

			log.debug("Executing[ " + ageFinalCountSql + " ]");
			PreparedStatement stmt = sfConn.prepareStatement(ageFinalCountSql);
			ResultSet resultSet = stmt.executeQuery();
			ResultType resultType = new ResultType();
			resultType.setName(RESULT_NAME);
			while (resultSet.next()) {
				String demoCount = resultSet.getString("patient_count");
				String raceCd = resultSet.getString("patient_range");
				DataType mdataType = new DataType();
				mdataType.setValue(demoCount);
				mdataType.setColumn(raceCd);
				mdataType.setType("int");
				resultType.getData().add(mdataType);
			}
			resultSet.close();
			stmt.close();
			edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createResult(resultType));
			ResultEnvelopeType resultEnvelop = new ResultEnvelopeType();
			resultEnvelop.setBody(bodyType);

			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

			StringWriter strWriter = new StringWriter();

			jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop),
					strWriter);

			IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
			xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
					.toString());

		} catch (Exception sqlEx) {
			log.error("QueryResultPatientSetGenerator.generateResult:"
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
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, totalCount);
			}
		}
	}
}
