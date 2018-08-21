package edu.harvard.i2b2.crc.sql.parser.contains.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class DoubleQuote extends AbstractProductionRule
{

	@Override
	public String toString() 
	{
		return "\"";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		Token t = statement.nextToken();
		if (t.getString().equals("\""))			// success
		{
			t.setPOS( Token.POS.DOUBLEQUOTE );
			return new ParseResult();
		}
		return new ParseResult( "Expecting a '\"' near position " + t.getIndex() ); // failure
	}

}
