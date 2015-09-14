package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

public interface ITablePdoQueryVisitDao {

	/**
	 * Function to return EventSet from visit information
	 * 
	 * @param encounterNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return EventSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitsByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * Function to return EventSet from visit information
	 * 
	 * @param visitListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return EventSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromVisitList(
			EventListType visitListType, boolean detailFlag, boolean blobFlag,
			boolean statusFlag) throws I2B2DAOException;

	/**
	 * Function to return EventSet for given patient set
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromPatientList(
			PatientListType patientListType, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;

	public EventSet getVisitByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;
	
	public void setMetaDataParamList(List<ParamType> metaDataParamList);

}