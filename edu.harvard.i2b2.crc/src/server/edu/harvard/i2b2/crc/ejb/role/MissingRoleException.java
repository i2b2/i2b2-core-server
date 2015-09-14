package edu.harvard.i2b2.crc.ejb.role;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public class MissingRoleException extends I2B2DAOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public MissingRoleException() {
	}
	
	 /**
     * Constructor that takes message and the exception as inputs.
     * @param message
     * @param e
     */
    public MissingRoleException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public MissingRoleException(String message) {
        super(message);
    }
	
}
