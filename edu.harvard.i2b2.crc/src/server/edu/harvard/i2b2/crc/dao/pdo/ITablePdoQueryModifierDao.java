package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierSet;

public interface ITablePdoQueryModifierDao {

	/**
	 * Get Concept set based on the fact's filter
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

	/**
	 * Function returns modifiers based on list of modifier codes
	 * 
	 * @param modifierCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return ModifierSet
	 * @throws I2B2DAOException
	 */
	public ModifierSet getModifierByModifierCd(List<String> modifierCdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

}