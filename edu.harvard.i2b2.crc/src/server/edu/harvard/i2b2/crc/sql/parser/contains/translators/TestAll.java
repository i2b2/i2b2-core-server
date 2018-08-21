package edu.harvard.i2b2.crc.sql.parser.contains.translators;

import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;
import edu.harvard.i2b2.crc.sql.parser.contains.rules.I2b2Grammar;

public class TestAll 
{	
	public static void main(String [] args)
	{
		boolean isPrintAll = false; // control whether to print out translation even if parsing fails
		
		I2b2Grammar s = new I2b2Grammar();
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
					 "(\"aa\" or \"bb\") and \"xx\" or (\"yy\" and (\"\"))",
					 "(\"aa\" or \"bb\") and knees ", "(\"aa\" or \"bb\") and \"xx\" or (\"yy\" and (\"zz\"))", "(\"aa\" or \"bb\") and \"xx\" or (\"yy\" and (something and \"zz\"))",
					 "rheumatoid and arthritis or (a and \"adddc\")", "arthritis not (hand or foot)", "(hand or foot) not arthritis",
					 "(\"interphalangeal joints\" or \"patellofemoral joint\") and knees"
						};
		
		for (int i = 0; i < strs.length; i++ )
		{
			TokenizedStatement ts = new TokenizedStatement( strs[i] );
			System.err.println("["+i + "]" + strs[i] );
			ParseResult r = s.parse( ts );
			for (int j=0; j < ts.getTokenCount(); j++)
				System.err.println("\t [" + j + "] = " + ts.getTokenAt(j) + ": (" + ts.getTokenAt(j).getPOS() + ")");			
			System.err.println( "\tResult: " + r.toString() );
			
			if (r.isSuccess() || isPrintAll)
			{
				Translator t = new SQLServerTranslator();
				System.err.println( "["+i + "] Parsing  : '" + strs[i] + "'");
				System.err.println( "["+i + "] SQLServer: '" + t.translate(ts) + "'");
				
				t = new OracleTranslator();
				ts = new TokenizedStatement( strs[i] ); // re-initialize for Oracle translation
				s.parse( ts );
				System.err.println( "["+i + "] Oracle   : '" + t.translate(ts) + "'");
				
				t = new PosgreSQLTranslator();
				ts = new TokenizedStatement( strs[i] ); // re-initialize for Postgres translation
				s.parse( ts );
				System.err.println( "["+i + "] Postgres : '" + t.translate(ts) + "'");
			}
			
			System.err.println("");
		}
	}
}
