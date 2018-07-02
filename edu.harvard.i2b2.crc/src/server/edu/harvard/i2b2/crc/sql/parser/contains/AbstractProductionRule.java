package edu.harvard.i2b2.crc.sql.parser.contains;

public abstract class AbstractProductionRule 
{
	
	public abstract String toString();
	
	public abstract ParseResult parse( TokenizedStatement statement );
	
}
