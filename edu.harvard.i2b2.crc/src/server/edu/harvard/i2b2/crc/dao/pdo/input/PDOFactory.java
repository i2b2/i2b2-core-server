package edu.harvard.i2b2.crc.dao.pdo.input;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RangeType;

public class PDOFactory {

	public static IInputOptionListHandler buildInputListHandler(
			InputOptionListType inputOptionList,
			DataSourceLookup dataSourceLookup) throws I2B2DAOException {

		IInputOptionListHandler inputOptionListHandler = null;
		if (inputOptionList.getPatientList() != null) {
			inputOptionListHandler = new PatientListTypeHandler(
					dataSourceLookup, inputOptionList.getPatientList());

		} else if (inputOptionList.getEventList() != null) {
			inputOptionListHandler = new VisitListTypeHandler(dataSourceLookup,
					inputOptionList.getEventList());

		} else if (inputOptionList.getPidList() != null) {
			inputOptionListHandler = new PidListTypeHandler(dataSourceLookup,
					inputOptionList.getPidList());
		} else if (inputOptionList.getEidList() != null) {
			inputOptionListHandler = new EidListTypeHandler(dataSourceLookup,
					inputOptionList.getEidList());
		} else {
			throw new I2B2DAOException(
					"Input option list does not contain visit or patient list");
		}
		return inputOptionListHandler;

	}

	public static RangeType getRangeType(InputOptionListType inputOptionList)
			throws I2B2DAOException {
		if (inputOptionList.getPatientList() != null) {
			return inputOptionList.getPatientList();

		} else if (inputOptionList.getEventList() != null) {
			return inputOptionList.getEventList();

		} else if (inputOptionList.getPidList() != null) {
			return inputOptionList.getPidList();

		} else if (inputOptionList.getEidList() != null) {
			return inputOptionList.getEidList();

		} else {
			throw new I2B2DAOException(
					"Input option list does not contain visit or patient list");
		}
	}

}
