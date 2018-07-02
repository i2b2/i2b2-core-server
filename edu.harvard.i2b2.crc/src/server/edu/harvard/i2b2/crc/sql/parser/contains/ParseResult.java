package edu.harvard.i2b2.crc.sql.parser.contains;

public class ParseResult 
{
	private boolean 		   myIsSuccess   = false;
	private SQLParserException myException = null;
	private String			   myErrorMsg  = null;

	public ParseResult( )
	{
		myIsSuccess = true;
	}

	public ParseResult( String errorMsg )
	{
		myIsSuccess = false;
		myErrorMsg  = errorMsg;
		
		if (myErrorMsg!= null)
			myException = new SQLParserException( errorMsg );
	}
	
	public boolean isSuccess()
	{
		return myIsSuccess;
	}
	
	public String getErrorMsg()
	{
		return myErrorMsg;
	}
	
	public SQLParserException getException()
	{ 
		return myException;
	}
	
	public String toString()
	{
		if ( this.myIsSuccess )
			return "Success!";
		else
			return "Failure: " + this.myErrorMsg;
	}
}
