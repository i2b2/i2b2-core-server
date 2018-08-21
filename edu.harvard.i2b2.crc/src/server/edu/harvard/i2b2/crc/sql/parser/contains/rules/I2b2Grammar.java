package edu.harvard.i2b2.crc.sql.parser.contains.rules;

import edu.harvard.i2b2.crc.sql.parser.contains.AbstractProductionRule;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class I2b2Grammar extends AbstractProductionRule {

	@Override
	public String toString() 
	{
		return "Start";
	}

	@Override
	public ParseResult parse(TokenizedStatement statement) 
	{
		ParentheticalContains pc = new ParentheticalContains();
		ParseResult pr = pc.parse( statement );
		if (!pr.isSuccess() )
			return pr;

		//	return new ParseResult("Parsing ended, but there are tokens left unparsed at position " + statement.getTokenAt(statement.getIndex()).getIndex() );
		while ( statement.hasMoreTokens() )
		{
			KeywordPhrase k = new KeywordPhrase();
			pr = k.parse(statement);
			if (!pr.isSuccess())
				return pr;
		}		
		return pr;
	}
	
	public static void main(String [] args)
	{
		I2b2Grammar s = new I2b2Grammar();
		// 
		// 
		/*
		String[] strs = {								
				 "(wrist)", "rheumatoid arthritis", "rheumatoid and arthritis", "rheumatoid and arthritis and heart or \"some other phrase\" or cat",
				 
				 "\"rheumatoid arthritis\"", " \" rheumatoid arthritis \"",  "\"rheumatoid arthritis \"", "\" rheumatoid arthritis\"", "rheumatoid arthritis\"\"", "rheumatoid arthritis \"\"",
				 "rheumatoid and arthritis", "rheumatoid aNd arthritis", "\"rheumatoid\" and arthritis", "rheumatoid and \"arthritis\"", "\"rheumatoid arthritis\" and wrist", "wrist and \" rheumatoid and arthritis \"",
				 "rheumatoid and arthritis and wrist", 	"wrist and \" rheumatoid and arthritis \" and bones",
				 "rheumatoid and arthritis or RA", "Reumatoid or Arthritis or RA", "\"Reumatoid arthritis\" or RA", "A or B or C or D", "A and B or C and D", "A and B oror C", "A or B aand C",
				 " \"rheumatoid arthritis \" or \" broken wrist\"", "broken and wrist or \"rheumatoid arthritis\"",
				 // 27  
				 " rheumatoid  not arthritis", "\"rheumatoid \" NOT \"arthritis\"", "\"rheumatoid\" and not", "rhematoid not \"rheumatoid arthritis\"", "rheumatoid and",
				 "(wrist)", "((wrist))", 
				 "(((wrist)))", "(wrist", "wrist)", 
				 "(\"BEEF\")", "(\"Beef noodles\")",
				 // 39
				 "((\"i r b\" not RA))",
				 "\"(\" and \"wrist \" or ((\"i r b\" not RA))",
				 "\"\" and \"(wrist)\" or \"\"", "\"\"(wrist)\"\"", "\"\"((wrist))\"\"", "()", "wris(t",  
				 "broken and (wrist or \"rheumatoid arthritis\")", "broken and (wrist or \"rheumatoid arthritis\")",
				 "(\"aa\" or \"bb\") and knees ", "(\"aa\" or \"bb\" and \"cc\") and knees ", "(\"aa\" or \"bb\" ) and (knees) ", "(\"aa\" or \"bb\" and \"cc\") and (knees or bones) ", 
				 "(\"aa\" or \"bb\") and \"xx\" or (\"yy\" and (\"\"))"
				 "(\"aa\" or \"bb\") and knees ", "(\"aa\" or \"bb\") and \"xx\" or (\"yy\" and (\"zz\"))", "(\"aa\" or \"bb\") and \"xx\" or (\"yy\" and (something and \"zz\"))",
				 "rheumatoid and arthritis or (a and \"adddc\")", "arthritis not (hand or foot)", "(hand or foot) not arthritis"
				};
		*/
		
		String [] strs = {"(hand or foot) not arthritis"};
		
		for (int i = 0; i < strs.length; i++ )
		{
			TokenizedStatement ts = new TokenizedStatement( strs[i] );
			System.err.println( strs[i] );
			ParseResult r = s.parse( ts );
			for (int j=0; j < ts.getTokenCount(); j++)
				System.err.println("\t [" + j + "] = " + ts.getTokenAt(j) + ": (" + ts.getTokenAt(j).getPOS() + ")");

			System.err.println( "["+i + "] Parsing: '" + strs[i] + "'");
			System.err.println( "\tResult: " + r.toString() );
			System.err.println( "" );
		}
		
	}


}
