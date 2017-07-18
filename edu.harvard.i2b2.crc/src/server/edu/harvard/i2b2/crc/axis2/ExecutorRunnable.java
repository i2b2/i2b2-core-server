/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 * 		Wayne Chan
 */
package edu.harvard.i2b2.crc.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.harvard.i2b2.crc.delegate.DbLookupReqHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandler;

/**
 * Implements thread runnable interface, to do CRC Dblookup 
 * processing using thread.
 */
public class ExecutorRunnable implements Runnable {
    private static Log log = LogFactory.getLog(ExecutorRunnable.class);
    private String inputString = null;
    private String outputString = null;
    private RequestHandler reqHandler = null;
    private DbLookupReqHandler dbluHdlr = null;
    private Exception ex = null;
    private boolean jobCompleteFlag = false;

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
    
    public void setRequestHandler(RequestHandler handler) {
        this.reqHandler = handler;
    }

    public RequestHandler getRequestHandler() {
        return this.reqHandler;
    }
    
    public void setDbLookupReqHandler(DbLookupReqHandler handler) {
        this.dbluHdlr = handler;
    }

    public DbLookupReqHandler getDbLookupReqHandler() {
        return this.dbluHdlr;
    }
    
    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public void run() {
        try {
        	if (null != dbluHdlr) {
        		log.debug("about to run DbLookupReqHandler");
                outputString = dbluHdlr.execute().toString();
        	} else if (null != reqHandler) {
        		log.debug("about to run RequestHandler");
                outputString = reqHandler.execute().toString();
        	}
            setJobCompleteFlag(true);
        }catch (Exception e) {
            setJobException(e);
        }
    }
    
 }
