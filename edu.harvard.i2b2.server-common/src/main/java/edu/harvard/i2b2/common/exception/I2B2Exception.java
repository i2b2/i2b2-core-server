/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.exception;

public class I2B2Exception extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -2212839315128709511L;

    /**
    * Default Constructor.
    */
    public I2B2Exception() {
    }

    /**
     * Constructor that takes message and the exception as inputs.
     * @param message
     * @param e
     */
    public I2B2Exception(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public I2B2Exception(String message) {
        super(message);
    }

    /**
     * Returns the known, i.e., not-null, root cause of this exception.
     *
     * @return
     */
    public Throwable getKnownRootCause() {
        Throwable root = this.getCause();

        if (root == null) {
            return this;
        }

        while ((root != null) && (root.getCause() != null)) {
            root = root.getCause();
        }

        return root;
    }
}
