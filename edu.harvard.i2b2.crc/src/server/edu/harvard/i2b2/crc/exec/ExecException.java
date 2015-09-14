package edu.harvard.i2b2.crc.exec;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public class ExecException extends I2B2Exception {

	private String exitStatus = null;
	private String message = null;

	public final static String TIMEOUT_STATUS = "TIMEOUT_STATUS";
	public final static String ERROR_STATUS = "ERROR_STATUS";

	public ExecException(int exitValue, String message) {

		if (exitValue == 143) {
			exitStatus = TIMEOUT_STATUS;
			this.message = "Process Timeout Error : " + message;
		} else {
			exitStatus = ERROR_STATUS;
			this.message = message;
		}
	}

	public ExecException(String message) {
		super(message);
	}

	public String getExitStatus() {
		return exitStatus;
	}

	public String getMessage() {
		return message;
	}

}
