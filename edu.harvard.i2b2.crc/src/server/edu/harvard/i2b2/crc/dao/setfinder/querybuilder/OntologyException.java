/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
