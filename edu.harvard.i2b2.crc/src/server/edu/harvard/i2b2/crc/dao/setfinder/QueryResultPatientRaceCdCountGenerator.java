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

public class QueryResultPatientRaceCdCountGenerator extends CRCDAO implements
		IResultGenerator {

	public static final String RESULT_NAME = "PATIENT_RACE_COUNT_XML";

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

		String demographics_count_sql = "select count(distinct dx.patient_num) as race_count, case when cl.name_char IS NULL then pd.race_cd else cl.name_char end as pd_race_cd from "
				+ this.getDbSchemaName()
				+ "patient_dimension pd left join code_lookup  cl on pd.race_cd = cl.code_cd"
				+ " and lower(cl.table_cd) = 'patient_dimension' and upper(cl.column_cd) = 'RACE_CD', "
				+ TEMP_DX_TABLE
				+ " dx where pd.patient_num = dx.patient_num"
				+ " group by case when cl.name_char IS NULL then pd.race_cd else cl.name_char end";

		boolean errorFlag = false;
		int totalCount = 0;
		try {

			log.debug("Executing[ " + demographics_count_sql + " ]");
			PreparedStatement stmt = sfConn
					.prepareStatement(demographics_count_sql);
			ResultSet resultSet = stmt.executeQuery();
			ResultType resultType = new ResultType();
			resultType.setName(RESULT_NAME);
			while (resultSet.next()) {
				String demoCount = resultSet.getString("race_count");
				String raceCd = resultSet.getString("pd_race_cd");
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
