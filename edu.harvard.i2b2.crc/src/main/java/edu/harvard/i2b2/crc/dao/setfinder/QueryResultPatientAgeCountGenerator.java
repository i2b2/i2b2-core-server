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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
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

	@Override
	public String getResults() {
		return xmlResult;
	}

	private String xmlResult = null;
	
	public static final String RESULT_NAME = "PATIENT_AGE_COUNT_XML";

	@Override
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
				+ " <from> "
				+ "patient_dimension pd ,"
				+ " <TEMP_DX_TABLE> "
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 0 and 20";

		String age2CountSql = "select count(dx.patient_num) patient_count, '21-40' patient_range"
				+ " from "
				+ " <from> "
				+ "patient_dimension pd ,"
				+ " <TEMP_DX_TABLE> "
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 21 and 40";

		String age3CountSql = "select count(dx.patient_num) patient_count, '41-60' patient_range"
				+ " from "
				+ " <from> "
				+ "patient_dimension pd ,"
				+ " <TEMP_DX_TABLE> "
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 41 and 60";

		String age4CountSql = "select count(dx.patient_num) patient_count, '61-80' patient_range"
				+ " from "
				+ " <from> "
				+ "patient_dimension pd ,"
				+ " <TEMP_DX_TABLE> "
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num between 61 and 80";

		String age5CountSql = "select count(dx.patient_num) patient_count, '>80' patient_range"
				+ " from "
				+ " <from> "
				+ "patient_dimension pd ,"
				+ " <TEMP_DX_TABLE> "
				+ " dx where pd.patient_num = dx.patient_num"
				+ " and pd.age_in_years_num > 80";

		String ageFinalCountSql = age1CountSql + "\n union all " + age2CountSql
				+ "\n union all " + age3CountSql + "\n union all "
				+ age4CountSql + "\n union all " + age5CountSql;

		boolean errorFlag = false;
		int totalCount = 0;
		try {

			String sqlFinal =  ageFinalCountSql.replace("<from>",   this.getDbSchemaName()  );
			sqlFinal = ageFinalCountSql.replace("<TEMP_DX_TABLE>", TEMP_DX_TABLE);

			log.debug("Executing[ " + ageFinalCountSql + " ]");
			PreparedStatement stmt = sfConn.prepareStatement(sqlFinal);
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
			 xmlResult = strWriter.toString();
			xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
					.toString());

		} catch (Exception sqlEx) {
			log.error("QueryResultPatientAgeCountGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientAgeCountGenerator.generateResult:"
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
