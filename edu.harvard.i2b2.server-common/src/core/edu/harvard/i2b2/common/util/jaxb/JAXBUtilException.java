package edu.harvard.i2b2.common.util.jaxb;

public class JAXBUtilException extends Exception {
    /**
     */
    private static final long serialVersionUID = 1L;

    /**
    * Constructor that takes message and the exception as inputs.
    * @param message
    * @param e
    */
    public JAXBUtilException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public JAXBUtilException(String message) {
        super(message);
    }
}
