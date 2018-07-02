package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class SimpleTerm extends AbstractProductionRule
{

	@Override
	public String toString() 
	{
		return "Simple Term";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		if (!statement.hasMoreTokens())
			return new ParseResult("Failed to parse: Expecting more token(s) but end of statement is reached.");
		Token t = statement.peekNext();
		// looks like a phrase
		if (t.getString().startsWith("\""))
		{
			Phrase p = new Phrase();
			return p.parse( statement );
		}
		
		// not a phrase
		Word w = new Word();
		return  w.parse( statement );
	}

}
