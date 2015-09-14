/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */


package edu.harvard.i2b2.pm.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.pm.delegate.RequestHandler;


/**
 * Implements thread runnable interface, to do PM
 * notes processing using thread.
 */
public class ExecutorRunnable implements Runnable {
    private String inputString = null;
    protected final Log log = LogFactory.getLog(getClass());

    private String outputString = null;
    private Exception ex = null;
    private boolean jobCompleteFlag = false;
    private RequestHandler reqHandler = null;
    public Exception getJobException() {
        return ex;
    }

    public boolean isJobCompleteFlag() {
        return jobCompleteFlag;
    }

    public void setJobCompleteFlag(boolean jobCompleteFlag) {
        this.jobCompleteFlag = jobCompleteFlag;
    }

    public void setJobException(Exception ex) {
        this.ex = ex;
    }


    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public String getOutputString() {
        return outputString;
    }
    
    public void setRequestHandler(RequestHandler handler) {
    	log.debug("Setting the requesthandler");
        this.reqHandler = handler;
    }

    public RequestHandler getRequestHandler() {
        return this.reqHandler;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public void run() {
        try {
            outputString = reqHandler.execute();
            setJobCompleteFlag(true);
        } catch (Exception e) {
        	setJobCompleteFlag(true);
            setJobException(e);
        }

        //notify();
    }
}
