package edu.harvard.i2b2.crc.loader.util;

/**
 * Exception class specific for key fetch failure
 * @author rk903
 *
 */
public class NoKeyException extends Exception {

	public NoKeyException() { 
		super();
	}
	
	public NoKeyException(String msg) { 
		super(msg);
	}
	
}
