package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class SQLServerContainsTranslator extends AbstractContainsTranslator
{
	public String formatValue(String containsValue, String dbServerType)
	{
		return defaultFormatValue(containsValue, dbServerType);
	}
}