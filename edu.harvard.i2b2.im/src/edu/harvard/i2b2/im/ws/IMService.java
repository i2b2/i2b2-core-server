/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.im.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.im.delegate.GetAuditHandlerHandler;
import edu.harvard.i2b2.im.delegate.IsKeySetHandlerHandler;
import edu.harvard.i2b2.im.delegate.RequestHandler;
import edu.harvard.i2b2.im.delegate.PdoHandlerHandler;
import edu.harvard.i2b2.im.delegate.SetKeyHandlerHandler;
import edu.harvard.i2b2.im.delegate.ValidationHandlerHandler;
import edu.harvard.i2b2.im.ws.ExecutorRunnable;
import edu.harvard.i2b2.im.ws.MessageFactory;
import edu.harvard.i2b2.im.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;

import org.apache.axiom.om.OMElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import javax.xml.stream.XMLStreamException;


/**
 * This is webservice skeleton class. It parses incoming Workplace service requests
 * and  generates responses in the Work Data Object XML format.
 *
 */
public class IMService {
    private static Log log = LogFactory.getLog(IMService.class);

    public OMElement validateSiteId(OMElement requestElement) throws Exception {
    	log.debug("In Validate Site ID");

    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (requestElement == null) {
    		log.error("Incoming IM request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	PDORequestMessage protectedDataMsg = new PDORequestMessage();
    	String requestElementString = requestElement.toString();

    	protectedDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (protectedDataMsg.getRequestMessageType() != null) {
    		if (protectedDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = protectedDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}
  	
        return execute(new ValidationHandlerHandler(protectedDataMsg), waitTime);  
    }    
    
    
    public OMElement pdorequest(OMElement requestElement) throws Exception {
    	log.debug("In pdorequest");

    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (requestElement == null) {
    		log.error("Incoming IM request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	PDORequestMessage protectedDataMsg = new PDORequestMessage();
    	String requestElementString = requestElement.toString();

    	protectedDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (protectedDataMsg.getRequestMessageType() != null) {
    		if (protectedDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = protectedDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}
  	
        return execute(new PdoHandlerHandler(protectedDataMsg), waitTime);  
    }    
    

    public OMElement setKey(OMElement requestElement) throws Exception {
    	log.debug("In setKey");

    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (requestElement == null) {
    		log.error("Incoming IM request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	SetKeyRequestMessage setKey = new SetKeyRequestMessage();
    	String requestElementString = requestElement.toString();

    	setKey.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (setKey.getRequestMessageType() != null) {
    		if (setKey.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = setKey.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}
  	
        return execute(new SetKeyHandlerHandler(setKey), waitTime);  
    }    
    
    public OMElement isKeySet(OMElement requestElement) throws Exception {
    	log.debug("In Is Key Set");

    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (requestElement == null) {
    		log.error("Incoming IM request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	IsKeySetRequestMessage setKey = new IsKeySetRequestMessage();
    	String requestElementString = requestElement.toString();

    	setKey.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (setKey.getRequestMessageType() != null) {
    		if (setKey.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = setKey.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}
  	
        return execute(new IsKeySetHandlerHandler(setKey), waitTime);  
    }    


    public OMElement getAudit(OMElement requestElement) throws Exception {
    	log.debug("In Get Audit");

    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (requestElement == null) {
    		log.error("Incoming IM request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	GetAuditRequestMessage getAudit = new GetAuditRequestMessage();
    	String requestElementString = requestElement.toString();

    	getAudit.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (getAudit.getRequestMessageType() != null) {
    		if (getAudit.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = getAudit.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}
  	
        return execute(new GetAuditHandlerHandler(getAudit), waitTime);  
    }    

    
    
    private OMElement execute(RequestHandler handler, long waitTime)throws I2B2Exception{
        //do workplace processing inside thread, so that  
        // service could send back message with timeout error.  
    	log.debug("In execute");

    	OMElement returnElement = null;
        	
        	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    		"You may wish to retry your last action";
        	
        	
        	ExecutorRunnable er = new ExecutorRunnable();        

        	er.setRequestHandler(handler);

        	Thread t = new Thread(er);
        	String workplaceDataResponse = null;

        	synchronized (t) {
        		t.start();

//        		try {
//        			if (waitTime > 0) {
//        				t.wait(waitTime);
//        			} else {
//        				t.wait();
//        			}
        			
        		try {
        			long startTime = System.currentTimeMillis();
        			long deltaTime = -1;
        			while((er.isJobCompleteFlag() == false)&& (deltaTime < waitTime)){
        				if (waitTime > 0) {
        					t.wait(waitTime - deltaTime);
        					deltaTime = System.currentTimeMillis() - startTime;
        				} else {
        					t.wait();
        				}
        			}

            		workplaceDataResponse = er.getOutputString();

            		if (workplaceDataResponse == null) {
            			if (er.getJobException() != null) {
            				log.error("er.jobException is " + er.getJobException().getMessage());
            		    	
            		    	log.info("waitTime is " + waitTime);
            				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
            						unknownErrorMessage);
            				workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);

            			} else if (er.isJobCompleteFlag() == false) {
            				//<result_waittime_ms>5000</result_waittime_ms>
            				String timeOuterror = "Remote server timed out \n" +    		
            				"Result waittime = " +
            				waitTime +
            				" ms elapsed,\nPlease try again";
            				log.error(timeOuterror);
            				log.debug("im waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName());
            				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
            						timeOuterror);
            				workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);

            			} else {
            				log.error("im  data response is null");
            			   	log.info("waitTime is " + waitTime);
            				log.debug("im waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName());
            			   	ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
            						unknownErrorMessage);
            				workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
            			}
            		}
        		} catch (InterruptedException e) {
        			log.error(e.getMessage());
        			throw new I2B2Exception("Thread error while running IM job ");
        		} finally {
        			t.interrupt();
        			er = null;
        			t = null;
        		}
        	}
        	returnElement = MessageFactory.createResponseOMElementFromString(workplaceDataResponse);

        	return returnElement;
        }
    
}
