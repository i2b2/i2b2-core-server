package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class ContainsTranslatorFactory 
{
	public static ContainsTranslatorFactory myInstance = null;
	
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
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE))
		{
			return new OracleContainsTranslator();
		}
		else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER))
		{
			return new SQLServerContainsTranslator();
		}
		else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))
		{
			return new PostgreSQLContainsTranslator();
		}
		return null;
	}
}