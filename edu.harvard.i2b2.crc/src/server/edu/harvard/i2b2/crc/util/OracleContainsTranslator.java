package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public class OracleContainsTranslator extends AbstractContainsTranslator
{
	private static OracleContainsTranslator myInstance;
	
	private OracleContainsTranslator()
	{}
	
	public static OracleContainsTranslator getInstance()
	{
		if (myInstance == null)
			myInstance = new OracleContainsTranslator();
		return myInstance;
	}

	public String formatValue(String containsValue, TokenizedStatement ts)
	{
		String translatedStr = "";	
		// first mark double quotes as open brace/close brace
		boolean isOpenQuote = true;
		for (int i = 0; i < ts.getTokenCount(); i++)
		{			
			Token t = ts.getTokenAt(i);
			if (t.getPOS() == Token.POS.DOUBLEQUOTE)
			{
				if (isOpenQuote)
					t.setPOS(Token.POS.OPEN_BRACE);
				else
					t.setPOS(Token.POS.CLOSE_BRACE);
				isOpenQuote = !isOpenQuote;
			}
		}
		
		// produce translated text by using POS tags
		for (int i = 0; i < ts.getTokenCount(); i++)
		{			
			Token t = ts.getTokenAt(i);
			if (t.getPOS() == Token.POS.AND)
			{
				if (i+1 >= ts.getTokenCount())
					return null; // error, not expecting end of tokens
				Token t2 = ts.getTokenAt(i+1);
				if (t2.getPOS() != Token.POS.NOT )
				{
					if (!translatedStr.endsWith(" "))
						translatedStr = translatedStr + " ";
					translatedStr = translatedStr + "AND ";
				}
			}
			else if (t.getPOS() == Token.POS.OR)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "OR ";
			}
			else if (t.getPOS() == Token.POS.NOT)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "NOT ";
			}
			else if (t.getPOS() == Token.POS.OPEN_PARENTHESIS)
			{
				if (!translatedStr.endsWith(" "))
					if ((i != 0) && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_BRACE && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_PARENTHESIS)
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
					if ((i != 0) && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_BRACE && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_PARENTHESIS)
						translatedStr = translatedStr + " ";
				}
				translatedStr = translatedStr + t.getString();
			}
		}
		return translatedStr;
	}

}