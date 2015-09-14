package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public class CRCTimeOutException extends I2B2DAOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public CRCTimeOutException() {
	}
	
	 /**
     * Constructor that takes message and the exception as inputs.
     * @param message
     * @param e
     */
    public CRCTimeOutException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public CRCTimeOutException(String message) {
        super(message);
    }
	
}
