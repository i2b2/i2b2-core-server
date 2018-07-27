package edu.harvard.i2b2.crc.sql.parser.contains;

public class Token 
{
	public enum POS 
	{
		DOUBLEQUOTE, OPEN_PARENTHESIS, CLOSE_PARENTHESIS, TERM, AND, OR, NOT, OPEN_BRACE, CLOSE_BRACE;
	}
	
	private String 	myString;
	private int		myIndex; // index of myString in the original unparsed statement
	private POS		myPOS	= null;	 // part of speech as tagged by parser

	public Token(String string, int index)
	{
		myString 	= string;
		myIndex = index;
	}

	public String getString()
	{ return this.myString; }

	public void setString( String str )
	{ this.myString = str; }
	
	public int getIndex()
	{ return this.myIndex; }

	public void setIndex( int index )
	{ this.myIndex = index; }
	
	public void setPOS( Token.POS pos)
	{ this.myPOS = pos; }
	
	public POS getPOS()
	{ return this.myPOS;}

	public String toString()
	{
		return "[" + myString + " @ " + this.myIndex + "]";
	}

}
