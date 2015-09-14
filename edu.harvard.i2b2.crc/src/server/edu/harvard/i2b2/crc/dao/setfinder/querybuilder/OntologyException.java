package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public class OntologyException extends I2B2DAOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public OntologyException() {
	}
	
	 /**
     * Constructor that takes message and the exception as inputs.
     * @param message
     * @param e
     */
    public OntologyException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public OntologyException(String message) {
        super(message);
    }
	
}
