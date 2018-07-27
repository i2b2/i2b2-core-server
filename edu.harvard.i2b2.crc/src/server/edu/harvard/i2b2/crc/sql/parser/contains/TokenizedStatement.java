package edu.harvard.i2b2.crc.sql.parser.contains;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TokenizedStatement 
{
	private   boolean			isDEBUG				= false;
	
	protected String 			myContainsStatement = null;
	protected ArrayList<Token> 	myTokens 			= new ArrayList<Token>();
	protected ArrayList<Token>	myInvalidTokens		= new ArrayList<Token>();
	private   int 				myCurrentIndex 		= 0;
	
	public TokenizedStatement( String containsStatement )
	{
		myContainsStatement = containsStatement;
		StringTokenizer tokenizer = new StringTokenizer( myContainsStatement, " \t\n\r\f\"()", true );
		// build tokens:
		// 1. Each space-delimited word is a token
		// 2. Each space, (, ), " is a token
		// 3. Each token remembers the position it is in this.myContainsStatement
		ArrayList<Token> tempTokens = new ArrayList<Token>();
		int index = 0;
		while (tokenizer.hasMoreTokens())
		{
			String str = tokenizer.nextToken();
			int startingIndex = myContainsStatement.indexOf(str, index);
			int strLength = str.length();
			
			tempTokens.add( new Token( str, index));
			index = startingIndex + strLength;
		}
		
		if (isDEBUG)
			printTokens( tempTokens );
			
		// put tokens that belong to the same quoted string together
		boolean isInQuotes = false;
		ArrayList<Token> tokenBuffer = new ArrayList<Token>();
		int		bufferStartIndex = -1;
		for (int i = 0; i < tempTokens.size(); i++ )
		{
			Token t = tempTokens.get(i);
			if (t.getString().equals("\""))
			{
				isInQuotes = !isInQuotes;
				if (!isInQuotes) // finishing quote reached, collect tokenBuffer
				{
					String buff = "";
					for (int j = 0; j < tokenBuffer.size(); j++)
						 buff = buff + tokenBuffer.get(j).getString();
					if (buff.length() > 0) // only add token if string has length > 0
					{
						Token combinedToken = new Token( buff, bufferStartIndex );
						this.myTokens.add(combinedToken);
					}
				}
				else
				{
					tokenBuffer = new ArrayList<Token>(); // reset tokenBuffer
					bufferStartIndex = -1;
				}
				this.myTokens.add(t); // add the " token
			}
			else
			{
				if (isInQuotes)
				{
					tokenBuffer.add(t);
					if (bufferStartIndex == -1)			// save the index of the first term in the quote
						bufferStartIndex = t.getIndex();
				}
				else
					this.myTokens.add(t);
			}
		}
		
		// eliminate blank tokens
		tempTokens = this.myTokens;
		this.myTokens = new ArrayList<Token>();
		for (int i= 0; i < tempTokens.size(); i++)
		{
			Token t = tempTokens.get(i);
			if (!(t.getString().equals(" ") || t.getString().equals("\t") || t.getString().equals("\n") || t.getString().equals("\r") || t.getString().equals("\f")))
				this.myTokens.add(t);
		}

	}

	public int getTokenCount()
	{ return this.myTokens.size(); }


	public void resetIndex()
	{ this.myCurrentIndex = 0; }

	public void setIndex(int newIndex)
	{ this.myCurrentIndex = newIndex;}
	
	public int getIndex()
	{ return this.myCurrentIndex; }
	
	public Token getTokenAt( int index )
	{ return myTokens.get(index); }

	public Token nextToken()
	{ return myTokens.get(myCurrentIndex++); }

	public Token peekNext()
	{ return myTokens.get(myCurrentIndex); }

	public boolean hasMoreTokens()
	{ return this.myCurrentIndex < this.myTokens.size(); }
	
	public List<Token> nextNTokens( int n )
	{		
		List<Token> v = this.myTokens.subList(myCurrentIndex, myCurrentIndex+n);
		myCurrentIndex += n;
		return v;
	}

	public ArrayList<Token> getInvalidTokens()
	{ return this.myInvalidTokens; }
	
	/**
	 *  TO PRINT
	 */ 
	private void printTokens( ArrayList<Token> toPrint )
	{
		System.err.println("Sentence = " + this.myContainsStatement);
		for (int i=0; i<toPrint.size(); i++)
			System.err.println("\t [" + i + "] = " + toPrint.get(i) + "");
	}	
	private void printTokens()
	{ 
		printTokens( this.myTokens );
	}
	/**
	 *  /TO PRINT
	 */
	

	
	public static void main(String [] args)
	{
		String [] sentences = {
								/* "abcd", "abc d", "abc d ", " a b cd", "beef", "\"beef\"", "\"beef noodles\"",
								"\"beef\" and \"noodles\"", "\"beef\"\"noodles\"", "(\"beef(\") and (\"noo()dles\")",
								"(abcd)", "((abcd efg))", */
								"\"asfsd\"\"\"", "\"\"\"\"\"\")_ad"
							  };
		
		for ( int i = 0; i < sentences.length; i++ )
		{
			TokenizedStatement statement = new TokenizedStatement(sentences[i]);
			System.err.println( i +"\t Sentence = " + sentences[i]);
			for (int j=0; j<statement.getTokenCount(); j++)
				System.err.println("\t [" + j + "] = " + statement.getTokenAt(j));
			System.err.println("----------------------------------------------------");
		}
		
		

		
		/*
		String sentence = "(\"abc)d\")";
		TokenizedStatement statement = new TokenizedStatement(sentence);
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		System.err.println("----------------------------------------------------");
		
		sentence = "(\"beef\")";
		statement = new TokenizedStatement(sentence);
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		System.err.println("----------------------------------------------------");
		
		sentence = "(\"beef noodles\")";
		statement = new TokenizedStatement(sentence);
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		System.err.println("----------------------------------------------------");
		
		sentence = "(\"beef\" \"noodles\")";
		statement = new TokenizedStatement(sentence);
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		System.err.println("----------------------------------------------------");
		*/
		
		
		/*
		String sentence = "This is a test of tokenized statement";
		TokenizedStatement statement = new TokenizedStatement(sentence);
		System.err.println("Test code for TokenizedStatement - Token Access and Iteration");
		System.err.println("-------------------------------------------------------------");
		System.err.println("Sentence = " + sentence);		
		System.err.println("statement[0] = " + statement.getTokenAt(0));
		System.err.println("statement[5] = " + statement.getTokenAt(5));
		
		System.err.println("statement.index = " + statement.getIndex());
		System.err.println("statement[" + statement.getIndex() + "] = " + statement.nextToken() + " | now index = " + statement.getIndex());
		int n = 3;
		int start = statement.getIndex();
		List<Token> tokens = statement.nextNTokens(n);
		int end   = statement.getIndex()-1;
		System.err.println("statement[" + start + "-" + end + "]  = " + tokens.get(0)+ " " + tokens.get(1) + " " + tokens.get(2) );
		
		System.err.println();
		System.err.println();
		System.err.println("Test code for TokenizedStatement - Tokenizing with \"\" ");
		System.err.println("------------------------------------------------------");
		sentence = "This is a \"test of\" tok\"\"\"enized \"\"statement\"\"";
		statement = new TokenizedStatement( sentence);
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");

		
		System.err.println();
		System.err.println();
		System.err.println("Test code for TokenizedStatement - Tokenizing with \"\" ");
		System.err.println("------------------------------------------------------");
		sentence = "\" rheumatoid arthritis \"";
		statement = new TokenizedStatement( sentence);
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		*/
		/*
		sentence = "(rheumatoid (arthritis)) and( rheumatoid) and (arthritis ) and \"(arthritis )\"";
		System.err.println();
		System.err.println();
		System.err.println("Test code for TokenizedStatement - Tokenizing with () 1");
		System.err.println("------------------------------------------------------");
		System.err.println("Sentence = " + sentence);
		statement = new TokenizedStatement( sentence);
		
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
		{
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		}
		for (Token t: statement.getInvalidTokens())
		{
			System.err.println("\t [invalid] = " + t + "");
		}
		
		sentence = "\"(rheumatoid \" and ( rheumatoid )and \"arthritis(\"";
		System.err.println();
		System.err.println();
		System.err.println("Test code for TokenizedStatement - Tokenizing with () 2");
		System.err.println("------------------------------------------------------");
		System.err.println("Sentence = " + sentence);
		statement = new TokenizedStatement( sentence);
		
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
		{
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		}
		for (Token t: statement.getInvalidTokens())
		{
			System.err.println("\t [invalid] = " + t + "");
		}
		
		sentence = "\"(rheumatoid \" and ( rheum(atoid )and \"arthritis(\"";
		System.err.println();
		System.err.println();
		System.err.println("Test code for TokenizedStatement - Tokenizing with () and invalids");
		System.err.println("------------------------------------------------------");
		System.err.println("Sentence = " + sentence);
		statement = new TokenizedStatement( sentence);
		
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
		{
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		}
		for (Token t: statement.getInvalidTokens())
		{
			System.err.println("\t [invalid] = " + t + "");
		}
		
		sentence = "((wrist))";
		System.err.println();
		System.err.println();
		System.err.println("Test code for TokenizedStatement - Tokenizing with () ");
		System.err.println("------------------------------------------------------");
		System.err.println("Sentence = " + sentence);
		statement = new TokenizedStatement( sentence);
		
		System.err.println("Sentence = " + sentence);
		for (int i=0; i<statement.getTokenCount(); i++)
		{
			System.err.println("\t [" + i + "] = " + statement.getTokenAt(i) + "");
		}
		for (Token t: statement.getInvalidTokens())
		{
			System.err.println("\t [invalid] = " + t + "");
		}
		*/
	}
	
}
