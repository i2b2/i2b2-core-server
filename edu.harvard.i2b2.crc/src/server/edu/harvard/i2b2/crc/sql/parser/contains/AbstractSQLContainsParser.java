package edu.harvard.i2b2.crc.sql.parser.contains;

public abstract class AbstractSQLContainsParser 
{
	private String myName;
	
	public String getName()
	{ return myName; }

	public abstract boolean parse( String input );
	
	
}
