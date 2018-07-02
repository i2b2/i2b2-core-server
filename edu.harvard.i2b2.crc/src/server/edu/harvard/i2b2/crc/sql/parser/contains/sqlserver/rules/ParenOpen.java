package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class ParenOpen extends AbstractProductionRule 
{	

	@Override
	public String toString() 
	{
		return "(";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		if (statement.hasMoreTokens())
		{
			Token t = statement.nextToken();
			if (t.getString().equals("("))
				return new ParseResult();
			else
				return new ParseResult( "'" + this.toString() + "' is expected at position " + t.getIndex());
		}
		return new ParseResult( "'" + this.toString() + "' is expected, but end of statement is reached.");
	}

}
