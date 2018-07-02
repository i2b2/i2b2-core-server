package edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class Contains extends AbstractProductionRule
{
	

	
	@Override
	public String toString() 
	{
		return "Contains";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		SimpleTerm st = new SimpleTerm();
		ParseResult r = st.parse(statement);

		if (r.isSuccess()) // try parse to see if simple term is followed by a keywordphrase
		{
			int saveIndex = statement.getIndex();
			KeywordPhrase k = new KeywordPhrase();
			ParseResult pr = k.parse(statement);
			if (pr.isSuccess())
				return pr;
			else
			{
				statement.setIndex( saveIndex );
				return r;
			}
		}

		return r;
	}
	
	public static void main(String [] args)
	{
		Contains c = new Contains();
		// 
		// 
		String[] strs = {"wrist", "rheumatoid arthritis", "\"rheumatoid arthritis\"", " \" rheumatoid arthritis \"",  "\"rheumatoid arthritis \"", "\" rheumatoid arthritis\"", "rheumatoid arthritis\"\"", "rheumatoid arthritis \"\"",
						 "rheumatoid and arthritis", "rheumatoid aNd arthritis", "\"rheumatoid\" and arthritis", "rheumatoid and \"arthritis\"", "\"rheumatoid arthritis\" and wrist", "wrist and \" rheumatoid and arthritis \"",
						 "rheumatoid and arthritis and wrist", 	"wrist and \" rheumatoid and arthritis \" and bones",
						 "rheumatoid and arthritis or RA", "Reumatoid or Arthritis or RA", "\"Reumatoid arthritis\" or RA", "A or B or C or D", "A and B or C and D", "A and B oror C", "A or B aand C",
						 " \"rheumatoid arthritis \" or \" broken wrist\"", "broken and wrist or \"rheumatoid arthritis\"",
						 " rheumatoid and not arthritis", "\"rheumatoid \" anD NOT \"arthritis\"", "\"rheumatoid\" and not", "rhematoid and not \"rheumatoid arthritis\"", "rheumatoid and"
						};
		
		for (int i = 0; i < strs.length; i++ )
		{
			ParseResult r = c.parse( new TokenizedStatement( strs[i] ) );
			System.err.println( "["+i + "] Parsing: '" + strs[i] + "'");
			System.err.println( "\tResult: " + r.toString() );
			System.err.println( "" );
		}
		
	}
}
