package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.datavo.pdo.EidSet;
import edu.harvard.i2b2.crc.datavo.pdo.query.EidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

public interface IPdoQueryEidDao {

	/**
	 * Get Eid set based on the fact's filter
	 * 
	 * @param panelSqlList
	 * @param sqlParamCountList
	 * @param inputOptionListHandler
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public EidSet getEidByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;

	/**
	 * Function to return patient dimension data for given list of patient num
	 * 
	 * @param patientNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws Exception
	 */
	public EidSet getEidByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * Get Patient dimension data based on patientlist present in input option
	 * list
	 * 
	 * @param patientListType
	 *            {@link PatientListType}
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws I2B2DAOException
	 */
	public EidSet getEidFromPatientSet(PatientListType patientListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * Function to return patient dimension data for given list of pid list
	 * 
	 * @param pidList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws Exception
	 */
	public EidSet getEidByEidList(EidListType eidList, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;

}