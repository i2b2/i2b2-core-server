package edu.harvard.i2b2.crc.sql.parser.contains.translators;

import java.util.StringTokenizer;

import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;
import edu.harvard.i2b2.crc.sql.parser.contains.rules.I2b2Grammar;

public class PosgreSQLTranslator extends Translator
{
	public String getName()
	{ return "PostgreSQL"; }
	
	// assumes st has already been syntax-checked and therefore POS-tagged
	public String translate( TokenizedStatement ts )
	{
		String translatedStr = "";
		boolean isOpenQuote = false;
		
		// produce translated text by using POS tags
		for (int i = 0; i < ts.getTokenCount(); i++)
		{			
			Token t = ts.getTokenAt(i);
			if (t.getPOS() == Token.POS.DOUBLEQUOTE)
			{
				isOpenQuote = !isOpenQuote;
				continue;
			}
			
			if (isOpenQuote) // t is a phrase
			{
				StringTokenizer st = new StringTokenizer(t.getString());
				int j =0;
				while ( st.hasMoreTokens() )
				{
					String part = st.nextToken();
					if (!translatedStr.endsWith(" "))
						translatedStr = translatedStr + " ";
					if (j == 0 )
						translatedStr = translatedStr + part;
					else
						translatedStr = translatedStr + "<-> " + part;
					j++;
				}
				continue;
			}

			if (t.getPOS() == Token.POS.AND)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "& ";
			}
			else if (t.getPOS() == Token.POS.OR)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "| ";
			}
			else if (t.getPOS() == Token.POS.NOT)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "&!";
			}
			else if (t.getPOS() == Token.POS.OPEN_PARENTHESIS)
			{
				if (!translatedStr.endsWith(" "))
					if ((i != 0) && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_PARENTHESIS)
						translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "(";
			}
			else if (t.getPOS() == Token.POS.CLOSE_PARENTHESIS)
			{
				translatedStr = translatedStr + ")";
			}
			else if (t.getPOS() == Token.POS.OPEN_BRACE)
				translatedStr = translatedStr + "{";
			else if (t.getPOS() == Token.POS.CLOSE_BRACE)
				translatedStr = translatedStr + "}";
			else
			{
				if (!translatedStr.endsWith(" "))
				{
					if ((i != 0) && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_PARENTHESIS )
						translatedStr = translatedStr + " ";
				}
				translatedStr = translatedStr + t.getString();
			}
		}
		return translatedStr;
	}
	
	public static void main( String [] args )
	{
		I2b2Grammar s = new I2b2Grammar();
		// 
		// 
		String[] strs = {
						 "(wrist)", "rheumatoid arthritis", "rheumatoid and arthritis", "rheumatoid and arthritis and heart or \"some other phrase\" or cat",
						 "\"rheumatoid arthritis\"", " \" rheumatoid arthritis \"",  "\"rheumatoid arthritis \"", "\" rheumatoid arthritis\"", "rheumatoid arthritis\"\"", "rheumatoid arthritis \"\"",
						 "rheumatoid and arthritis", "rheumatoid aNd arthritis", "\"rheumatoid\" and arthritis", "rheumatoid and \"arthritis\"", "\"rheumatoid arthritis\" and wrist", "wrist and \" rheumatoid and arthritis \"",
						 "rheumatoid and arthritis and wrist", 	"wrist and \" rheumatoid and arthritis \" and bones",
						 "rheumatoid and arthritis or RA", "Reumatoid or Arthritis or RA", "\"Reumatoid arthritis\" or RA", "A or B or C or D", "A and B or C and D", "A and B oror C", "A or B aand C",
						 " \"rheumatoid arthritis \" or \" broken wrist\"", "broken and wrist or \"rheumatoid arthritis\"",
				/* 27 */ " rheumatoid  not arthritis", "\"rheumatoid \" NOT \"arthritis\"", "\"rheumatoid\" and not", "rhematoid not \"rheumatoid arthritis\"", "rheumatoid and",
						 "(wrist)", "((wrist))", 
						 "(((wrist)))", "(wrist", "wrist)", 
						 "(\"BEEF\")", "(\"Beef noodles\")",
				/* 39*/  "((\"i r b\" not RA))",
						 "\"(\" and \"wrist \" or ((\"i r b\" not RA))",
						 "\"\" and \"(wrist)\" or \"\"", "\"\"(wrist)\"\"", "\"\"((wrist))\"\"", "()", "wris(t",  
						 "broken and (wrist or \"rheumatoid arthritis\")", "broken and (wrist or \"rheumatoid arthritis\")"
						};
		
		PosgreSQLTranslator t = new PosgreSQLTranslator();
		for (int i = 0; i < strs.length; i++ )
		{
			TokenizedStatement ts = new TokenizedStatement( strs[i] );
			System.err.println( strs[i] );
			ParseResult r = s.parse( ts );
			for (int j=0; j < ts.getTokenCount(); j++)
				System.err.println("\t [" + j + "] = " + ts.getTokenAt(j) + ": (" + ts.getTokenAt(j).getPOS() + ")");			
			System.err.println( "\tResult: " + r.toString() );
			
			System.err.println( "["+i + "] Parsing : '" + strs[i] + "'");
			System.err.println( "["+i + "] Postgres: '" + t.translate(ts) + "'");
			System.err.println( "" );
		}
	}

}
