package edu.harvard.i2b2.crc.dao.pdo;

import java.util.HashMap;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.pdo.input.IFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;

public interface IPageDao {

	public long getTotalForAllPanel(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler)
			throws I2B2DAOException;

	public String buildTotalSql(IFactRelatedQueryHandler factHandler,
			PanelType panel) throws I2B2DAOException;

	public HashMap getMinIndexAndCountAllPanel(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler)
			throws I2B2DAOException;

}