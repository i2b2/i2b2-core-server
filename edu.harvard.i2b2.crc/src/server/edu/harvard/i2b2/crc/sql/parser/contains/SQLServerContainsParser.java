package edu.harvard.i2b2.crc.sql.parser.contains;

public class SQLServerContainsParser extends AbstractSQLContainsParser 
{

	@Override
	public boolean parse(String input) 
	{
		TokenizedStatement statement = new TokenizedStatement(input);
		
		return false;
	}

}
