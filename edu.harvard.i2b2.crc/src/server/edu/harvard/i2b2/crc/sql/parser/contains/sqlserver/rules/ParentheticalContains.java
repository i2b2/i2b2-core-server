package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class ParentheticalContains extends AbstractProductionRule {

	@Override
	public String toString() 
	{
		return "( Contains)";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		if ( statement.getInvalidTokens().size() > 0 )
		{
			Token invalid = statement.getInvalidTokens().get(0);
			return new ParseResult("Invalid token '" + invalid.getString() + "' at position " + invalid.getIndex() );
		}
		
		int saveIndex = statement.getIndex();
		ParenOpen  o = new ParenOpen();
		ParseResult pr = o.parse(statement);
		if ( pr.isSuccess() )
		{
			ParentheticalContains pc = new ParentheticalContains();
			pr = pc.parse(statement);
			if (pr.isSuccess())
			{
				ParenClose c = new ParenClose();
				pr = c.parse(statement);
				return pr; // return failure involved with parsing ParenClose
			}
			else
				return pr; // return failure involved with parsing a ParentheticalContains
		}
		else
		{
			statement.setIndex( saveIndex );
			Contains c = new Contains();
			return c.parse(statement); // return ParseReulst parsing a Contains
		}
	}	
}
