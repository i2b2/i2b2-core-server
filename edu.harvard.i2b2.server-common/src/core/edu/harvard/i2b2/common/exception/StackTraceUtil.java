/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * This class has the functions to convert an exception StackTrace into a string.
 * @author rk903
 */
public class StackTraceUtil {
    /**
     * Takes an Exception argument and returns the stacktrace.
     *
     * @param exception
     * @return The stacktrace string
     */
    public static String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        return sw.toString();
    }
}
