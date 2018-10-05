/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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

	@Override
	public String getMessage() {
		return message;
	}

}
