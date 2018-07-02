package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class KeywordPhrase extends AbstractProductionRule
{

	@Override
	public String toString() 
	{
		return "KeywordPhrase";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		if (!statement.hasMoreTokens())
			return new ParseResult("Failed: Expecting more token(s) but end of statement is reached.");

		Token t = statement.peekNext();
		int index = statement.getIndex();
		if ( t.getString().equalsIgnoreCase("and"))
		{
			AndNot an = new AndNot();
			ParseResult r = an.parse(statement);
			if (r.isSuccess())
				return r;
			else
			{
				statement.setIndex(index);
				And a = new And();
				return a.parse(statement);
			}
		}
		else if (t.getString().equalsIgnoreCase("or"))
		{
			statement.setIndex(index);
			Or o = new Or();
			return o.parse(statement);
		}
		return new ParseResult("Failed to parse:  'and', 'and not', or 'or' expected at position " + t.getIndex() );
	}
}
