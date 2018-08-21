package edu.harvard.i2b2.crc.sql.parser.contains.translators;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class TranslatorFactory 
{
	private static TranslatorFactory myInstance = null;
	
	public static TranslatorFactory getInstance()
	{
		if (myInstance == null )
			myInstance = new TranslatorFactory();
		return myInstance;
	}
	
	private TranslatorFactory()
	{}

	
	public Translator getTranslator(String dbServerType)
	{
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE))				return new OracleTranslator();
		else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER))		return new SQLServerTranslator();
		else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))	return new PosgreSQLTranslator();

		return null;
	}
	
}
