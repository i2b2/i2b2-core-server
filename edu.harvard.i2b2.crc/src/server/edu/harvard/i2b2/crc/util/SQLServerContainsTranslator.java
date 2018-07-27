package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;


public class SQLServerContainsTranslator extends AbstractContainsTranslator
{
	
	private static SQLServerContainsTranslator myInstance;
	
	private SQLServerContainsTranslator()
	{}
	
	public static SQLServerContainsTranslator getInstance()
	{
		if (myInstance == null)
			myInstance = new SQLServerContainsTranslator();
		return myInstance;
	}
	
	public String formatValue(String containsValue, TokenizedStatement ts)
	{
		return containsValue;
	}
}