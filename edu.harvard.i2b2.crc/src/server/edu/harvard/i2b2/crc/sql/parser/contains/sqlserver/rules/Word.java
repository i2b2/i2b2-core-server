package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class Word extends AbstractProductionRule
{
	
	@Override
	public ParseResult parse( TokenizedStatement statement ) 
	{
		if ( !statement.hasMoreTokens() )
			return new ParseResult("Expecting a Word, but end of statement is reached instead. " );
		Token t = statement.nextToken();
		if (t.getString().equals("\""))
			return new ParseResult("Unexpected \" near position " + t.getIndex() );
		return new ParseResult();
	}
	
	@Override
	public String toString() 
	{
		return "Word -> .* ";
	}
	
}
