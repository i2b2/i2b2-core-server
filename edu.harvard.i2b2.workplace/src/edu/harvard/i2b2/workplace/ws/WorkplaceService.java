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
package edu.harvard.i2b2.workplace.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.delegate.RequestHandler;
import edu.harvard.i2b2.workplace.ws.ExecutorRunnable;
import edu.harvard.i2b2.workplace.ws.MessageFactory;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.delegate.AddChildHandler;
import edu.harvard.i2b2.workplace.delegate.AnnotateChildHandler;
import edu.harvard.i2b2.workplace.delegate.DeleteChildHandler;
import edu.harvard.i2b2.workplace.delegate.ExportChildHandler;
import edu.harvard.i2b2.workplace.delegate.GetFoldersByProjectHandler;
import edu.harvard.i2b2.workplace.delegate.GetFoldersByUserIdHandler;
import edu.harvard.i2b2.workplace.delegate.GetChildrenHandler;
import edu.harvard.i2b2.workplace.delegate.RenameChildHandler;
import edu.harvard.i2b2.workplace.delegate.MoveChildHandler;
import edu.harvard.i2b2.workplace.delegate.GetNameInfoHandler;
import edu.harvard.i2b2.workplace.delegate.SetProtectedAcessHandler;

import org.apache.axiom.om.OMElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import javax.xml.stream.XMLStreamException;


/**
 * This is webservice skeleton class. It parses incoming Workplace service requests
 * and  generates responses in the Work Data Object XML format.
 *
 */
public class WorkplaceService {
    private static Log log = LogFactory.getLog(WorkplaceService.class);

    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps a Workplace
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap work data object. Work data object will
     * have all the results returned by the query.
     *
     *
     * @param getChildren
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getChildren(OMElement getChildrenElement) 
        throws I2B2Exception {

    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";

    	if (getChildrenElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
        
        GetChildrenDataMessage childrenDataMsg = new GetChildrenDataMessage();
        String requestElementString = getChildrenElement.toString();
        childrenDataMsg.setRequestMessageType(requestElementString);
   
        long waitTime = 0;
        if (childrenDataMsg.getRequestMessageType() != null) {
            if (childrenDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = childrenDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }
        
        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
   //     ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetChildrenHandler(childrenDataMsg), waitTime);
        
    }
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Workplace
     * query inside a work query request object. The response is also will be in i2b2
     * message format, which will wrap work data object. Work data object will
     * have all the results returned by the query.
     *
     *
     * @param OMElement getFoldersElement
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getFoldersByProject(OMElement getFoldersElement)
        throws Exception {
    	
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";

    	if (getFoldersElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
        
    	GetFoldersDataMessage foldersDataMsg = new GetFoldersDataMessage();
        String requestElementString = getFoldersElement.toString();
        //    log.info(requestElementString);
        foldersDataMsg.setRequestMessageType(requestElementString);

        long waitTime = 0;
        if (foldersDataMsg.getRequestMessageType() != null) {
            if (foldersDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = foldersDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Workplace query processing inside thread, so that  
        // service could sends back message with timeout error.
  
   //    ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetFoldersByProjectHandler(foldersDataMsg), waitTime);
        
    }

    
    public OMElement getFoldersByUserId(OMElement getFoldersElement) 
    throws Exception {
    	
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    	"You may wish to retry your last action";

    	if (getFoldersElement == null) {
    		log.error("Incoming Workplace request is null");

    		ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
    				unknownErrorMessage);
    		workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}

    	GetFoldersDataMessage foldersDataMsg = new GetFoldersDataMessage();
    	String requestElementString = getFoldersElement.toString();
//  	log.info(requestElementString);
    	foldersDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (foldersDataMsg.getRequestMessageType() != null) {
    		if (foldersDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = foldersDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
  //      ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetFoldersByUserIdHandler(foldersDataMsg), waitTime);
        
    }
    
	/**
	 *   
     * This method is for finding the workplace item with the given keyword
     * It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Workplace
     * query inside a work query request object. The response is also will be in i2b2
     * message format, which will wrap work data object. Work data object will
     * have all the results returned by the query.
     *

	 * @param requestElement
	 * @return
	 * @throws Exception
	 * 
	 * @author Neha Patel
	 */
	public OMElement getNameInfo(OMElement requestElement)
	throws Exception	{

			//OMElement requestElement = null;
	    	String workplaceDataResponse = null;
	    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
	    	   	"You may wish to retry your last action";
	    	
	    	if (requestElement == null) {
	    		log.error("Incoming Find Workplace request is null");

	    	    ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
	    	    				unknownErrorMessage);
	    	    workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
	    	    return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
	    	}

	    	GetNameInfoDataMessage foldersDataMsg = new GetNameInfoDataMessage();
	    	String requestElementString = requestElement.toString();
	    	log.debug(requestElementString);
	    	foldersDataMsg.setRequestMessageType(requestElementString);
	    	
	    	long waitTime = 0;
	    	if (foldersDataMsg.getRequestMessageType() != null) {
	    		if (foldersDataMsg.getRequestMessageType().getRequestHeader() != null) {
	    		
	    			waitTime = foldersDataMsg.getRequestMessageType()
	    					.getRequestHeader()
	    					.getResultWaittimeMs();
	    		
	    	 	}
	    	}
     
	    	return execute(new GetNameInfoHandler(foldersDataMsg), waitTime);  	
	}

    
    public OMElement deleteChild(OMElement deleteNodeElement)throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (deleteNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	DeleteChildDataMessage deleteDataMsg = new DeleteChildDataMessage();
    	String requestElementString = deleteNodeElement.toString();
//  	log.info(requestElementString);
    	deleteDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (deleteDataMsg.getRequestMessageType() != null) {
    		if (deleteDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = deleteDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
     //   ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new DeleteChildHandler(deleteDataMsg), waitTime);
        
    }    
    
    
    public OMElement moveChild(OMElement nodeElement)throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (nodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	MoveChildDataMessage moveDataMsg = new MoveChildDataMessage();
    	String requestElementString = nodeElement.toString();
//  	log.info(requestElementString);
    	moveDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (moveDataMsg.getRequestMessageType() != null) {
    		if (moveDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = moveDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
    //    ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new MoveChildHandler(moveDataMsg), waitTime);
    }
    
    
    public OMElement renameChild(OMElement renameNodeElement) throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (renameNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	RenameChildDataMessage renameDataMsg = new RenameChildDataMessage();
    	String requestElementString = renameNodeElement.toString();
//  	log.info(requestElementString);
    	renameDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (renameDataMsg.getRequestMessageType() != null) {
    		if (renameDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = renameDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
   //     ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new RenameChildHandler(renameDataMsg), waitTime);
        
    }    
    
    public OMElement annotateChild(OMElement annotateNodeElement) throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (annotateNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	AnnotateChildDataMessage annotateDataMsg = new AnnotateChildDataMessage();
    	String requestElementString = annotateNodeElement.toString();
//  	log.info(requestElementString);
    	annotateDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (annotateDataMsg.getRequestMessageType() != null) {
    		if (annotateDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = annotateDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
   //    ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new AnnotateChildHandler(annotateDataMsg), waitTime);
        
    }    

    public OMElement exportChild(OMElement aexportNodeElement) throws Exception {
    	log.debug("In export Child");
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (aexportNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	log.debug("Start of request string");
    	ExportChildDataMessage exportDataMsg = new ExportChildDataMessage();
    	String requestElementString = aexportNodeElement.toString();
    	log.debug("created new string");
    	exportDataMsg.setRequestMessageType(requestElementString);
    	log.debug("set request messagetype");
    	long waitTime = 0;
    	if (exportDataMsg.getRequestMessageType() != null) {
    		if (exportDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = exportDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
   //    ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new ExportChildHandler(exportDataMsg), waitTime);
        
    }    

    
    public OMElement addChild(OMElement addNodeElement) throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (addNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	AddChildDataMessage addDataMsg = new AddChildDataMessage();
    	String requestElementString = addNodeElement.toString();
//  	log.info(requestElementString);
    	addDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (addDataMsg.getRequestMessageType() != null) {
    		if (addDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = addDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
    //    ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new AddChildHandler(addDataMsg), waitTime);
        
    }    
    
    public OMElement setProtectedAccess(OMElement requestElement) throws Exception {
    	    	
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (requestElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	ProtectedDataMessage protectedDataMsg = new ProtectedDataMessage();
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
  	
        return execute(new SetProtectedAcessHandler(protectedDataMsg), waitTime);  
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
            				log.debug("workplace waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName());
            				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
            						timeOuterror);
            				workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);

            			} else {
            				log.error("workplace  data response is null");
            			   	log.info("waitTime is " + waitTime);
            				log.debug("workplace waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName());
            			   	ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
            						unknownErrorMessage);
            				workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
            			}
            		}
        		} catch (InterruptedException e) {
        			log.error(e.getMessage());
        			throw new I2B2Exception("Thread error while running Workplace job ");
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
