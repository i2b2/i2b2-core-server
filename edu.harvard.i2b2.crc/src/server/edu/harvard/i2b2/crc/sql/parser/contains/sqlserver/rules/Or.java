package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class Or extends AbstractProductionRule
{

	@Override
	public String toString() 
	{
		return "Or";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		Token t = statement.nextToken();
		if ( t.getString().equalsIgnoreCase("or"))
		{
			ParentheticalContains c = new ParentheticalContains();
			return c.parse(statement);
		}
		return new ParseResult("Syntax error at position " + t.getIndex() + ". A Keyword may be expected.");

	}
	
}
