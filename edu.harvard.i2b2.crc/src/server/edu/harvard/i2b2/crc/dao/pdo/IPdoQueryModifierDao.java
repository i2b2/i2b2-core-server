package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;

public interface IPdoQueryModifierDao {

	/**
	 * Get modifiers detail from modifier code list
	 * 
	 * @param modifierCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return {@link PatientDataType.ModifierDimensionSet}
	 * @throws I2B2DAOException
	 */
	public ModifierSet getModifierByModifierCd(List<String> modifierCdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	
	/**
	 * Get Modifier set based on the fact's filter
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
	public ModifierSet getModifierByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;
}