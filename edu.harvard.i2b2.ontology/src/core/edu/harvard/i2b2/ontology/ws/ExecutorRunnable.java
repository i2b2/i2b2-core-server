/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 */
package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.ontology.delegate.RequestHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements thread runnable interface, to do Ontology
 * processing using thread.
 */
public class ExecutorRunnable implements Runnable {
    private static Log log = LogFactory.getLog(ExecutorRunnable.class);
    private String inputString = null;
    private String outputString = null;
    private RequestHandler reqHandler = null;
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
    
    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public void run() {
        try {
            outputString = reqHandler.execute();
            setJobCompleteFlag(true);
        } catch (Exception e) {
            setJobException(e);
        }

        //notify();
    }
    
 /*  public OMElement execute(RequestHandler handler, long waitTime)throws I2B2Exception{
    	
    	OMElement returnElement = null;
    	String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	this.setRequestHandler(handler);

    	// timeout test    waitTime=10;   Passed 1/29/08  lcp
    	
    	Thread t = new Thread(this);
        synchronized (t) {
        	t.start();

        	try {
        		int count = 0;
        		while((ontologyDataResponse == null)&&(count<2)){
        			if (waitTime > 0) {
        				t.wait(waitTime);
        			} else {
        				t.wait();
        			}
        			count++;
        			log.debug("ontology ER loop" + count);
        		}

        		ontologyDataResponse = this.getOutputString();

        		if (ontologyDataResponse == null) {
        			if (this.getJobException() != null) {
        				log.error("er.jobException is not null");
        				log.error(this.getJobException().getMessage());
        				log.error(this.getJobException().getStackTrace());
        		    	
        		    	log.info("waitTime is " + waitTime);
        				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        						unknownErrorMessage);
        				ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);

        			} else if (this.isJobCompleteFlag() == false) {
        				//<result_waittime_ms>5000</result_waittime_ms>
        				String timeOuterror = "Remote server timed out \n" +    		
        				"Result waittime = " +
        				waitTime +
        				" ms elapsed,\nPlease try again";
        				log.error(timeOuterror);

        				log.error("for " + this.getRequestHandler().getClass().getName() +
        						" and loop count = " + count);
     
        				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        						timeOuterror);
        				ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);

        			} else {
        				log.error("ontology data response is null");
        			   	log.info("waitTime is " + waitTime);
        			   	ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        						unknownErrorMessage);
        				ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
        			}
        		}
        	} catch (InterruptedException e) {
        		log.error("interrupt exception " + e.getMessage());
        		ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        				unknownErrorMessage);
        		ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
        	} finally {
        		t.interrupt();
        		t = null;
        	}
        }

        returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        return returnElement;
    }*/
}
