package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public class LockedoutException extends I2B2DAOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public LockedoutException() {
	}
	
	 /**
     * Constructor that takes message and the exception as inputs.
     * @param message
     * @param e
     */
    public LockedoutException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public LockedoutException(String message) {
        super(message);
    }
	
}
