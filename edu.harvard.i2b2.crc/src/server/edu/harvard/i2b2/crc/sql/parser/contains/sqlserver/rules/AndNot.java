package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class AndNot extends AbstractProductionRule
{

	@Override
	public String toString() 
	{
		return "And Not";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		if (statement.getIndex() < statement.getTokenCount()-1 )
		{
			List<Token> tokens = new ArrayList<Token>(2);
			tokens.add( statement.getTokenAt( statement.getIndex() ));
			tokens.add( statement.getTokenAt( statement.getIndex()+1 ));
			
			if ( tokens.get(0).getString().equalsIgnoreCase("and") && 
				 tokens.get(1).getString().equalsIgnoreCase("not") )
			{
				statement.setIndex( statement.getIndex()+2);
				ParentheticalContains c = new ParentheticalContains();
				return c.parse(statement);
			}
			else
				return new ParseResult("Syntax error near position " + tokens.get(0).getIndex() );
		}
		return new ParseResult("Failed to parse 'and not' -- End of statement reached." );
	}
	
}
