package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class PostgreSQLContainsTranslator extends AbstractContainsTranslator
{
	private static PostgreSQLContainsTranslator myInstance;
	
	private PostgreSQLContainsTranslator()
	{}
	
	public static PostgreSQLContainsTranslator getInstance()
	{
		if (myInstance == null)
			myInstance = new PostgreSQLContainsTranslator();
		return myInstance;
	}
	
	public String formatValue(String containsValue, TokenizedStatement ts)
	{
		return containsValue;
	}

}