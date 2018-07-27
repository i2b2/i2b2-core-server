package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class ContainsTranslatorFactory 
{
	private static ContainsTranslatorFactory myInstance = null;
	
	public static ContainsTranslatorFactory getInstance()
	{
		if (myInstance == null )
			myInstance = new ContainsTranslatorFactory();
		return myInstance;
	}
	
	private ContainsTranslatorFactory()
	{}
	
	public AbstractContainsTranslator getTranslator( String dbServerType )
	{
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE))				return OracleContainsTranslator.getInstance();
		else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER))		return SQLServerContainsTranslator.getInstance();
		else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))	return PostgreSQLContainsTranslator.getInstance();
		return null;
	}
}