package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class Phrase extends AbstractProductionRule
{

	@Override
	public String toString() 
	{
		return "Phrase -> \".*\"";
	}

	@Override
	public ParseResult parse( TokenizedStatement statement ) 
	{
		DoubleQuote dq = new DoubleQuote();
		ParseResult r = dq.parse(statement);
		if (!r.isSuccess())
			return r;
		
		while ( statement.hasMoreTokens() )
		{
			r = dq.parse(statement);
			if (r.isSuccess())
				return new ParseResult();
		}
		return new ParseResult("Expecting '\"' but reached the end of statement.");
	}

}
