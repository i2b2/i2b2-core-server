/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

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
import edu.harvard.i2b2.crc.delegate.JobReqHandler;
import edu.harvard.i2b2.crc.delegate.RPDOReqHandler;
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
    private JobReqHandler jobHandler = null;
    private DbLookupReqHandler dbluHdlr = null;
    private RPDOReqHandler rpdoHandler = null;
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
    
    public void setJobReqHandler(JobReqHandler handler) {
        this.jobHandler = handler;
    }

    public JobReqHandler getJobReqHandler() {
        return this.jobHandler;
    }
    
    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    @Override
	public void run() {
        try {
        	if (null != dbluHdlr) {
        		log.debug("about to run DbLookupReqHandler");
                outputString = dbluHdlr.execute().toString();
        	} else if (null != reqHandler) {
        		log.debug("about to run RequestHandler");
                outputString = reqHandler.execute().toString();
        	}  else if (null != jobHandler) {
        		log.debug("about to run JobHandler");
                outputString = jobHandler.execute().toString();
        	} else if (null != rpdoHandler) {
        		log.debug("about to run RPDOHandler");
                outputString = rpdoHandler.execute().toString();
        	}
            setJobCompleteFlag(true);
        }catch (Exception e) {
            setJobException(e);
        }
    }

	public RPDOReqHandler getRpdoHandler() {
		return rpdoHandler;
	}

	public void setRpdoHandler(RPDOReqHandler rpdoHandler) {
		this.rpdoHandler = rpdoHandler;
	}
    
 }
