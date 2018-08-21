package edu.harvard.i2b2.crc.sql.parser.contains.translators;

import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;

public abstract class Translator 
{
	public abstract String translate( TokenizedStatement ts );
	public abstract String getName();
}
