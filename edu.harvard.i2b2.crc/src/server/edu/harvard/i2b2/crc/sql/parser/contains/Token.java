package edu.harvard.i2b2.crc.sql.parser.contains;

public class Token 
{
	private String 	myString;
	private int		myIndex; // index of myString in the original unparsed statement
	
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
	
	public String toString()
	{
		return "[" + myString + " @ " + this.myIndex + "]";
	}

}
