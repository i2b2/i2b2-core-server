/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 *     Wayne Chan
 */
package edu.harvard.i2b2.crc.axis2;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
//import edu.harvard.i2b2.crc.delegate.loader.LoaderQueryRequestDelegate;
//import edu.harvard.i2b2.crc.delegate.loader.PublishDataRequestHandler;
import edu.harvard.i2b2.crc.delegate.getnameinfo.GetNameInfoRequestDelegate;
import edu.harvard.i2b2.crc.delegate.pdo.PdoQueryRequestDelegate;
import edu.harvard.i2b2.crc.delegate.setfinder.QueryRequestDelegate;
import edu.harvard.i2b2.crc.loader.delegate.BulkLoadRequestHandler;
import edu.harvard.i2b2.crc.loader.delegate.GetLoadStatusRequestHandler;
import edu.harvard.i2b2.crc.loader.delegate.LoaderQueryRequestDelegate;
import edu.harvard.i2b2.crc.loader.delegate.PublishDataRequestHandler;
//import edu.harvard.i2b2.crc.loader.ws.ProviderRestService;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.delegate.DbLookupReqHandler;
//import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.delegate.DeleteDblookupHandler;
import edu.harvard.i2b2.crc.delegate.GetAllDblookupsHandler;
import edu.harvard.i2b2.crc.delegate.GetDblookupHandler;
import edu.harvard.i2b2.crc.delegate.SetDblookupHandler;
import edu.harvard.i2b2.crc.axis2.DeleteDblookupDataMessage;
import edu.harvard.i2b2.crc.axis2.GetAllDblookupsDataMessage;
import edu.harvard.i2b2.crc.axis2.GetDblookupDataMessage;
import edu.harvard.i2b2.crc.axis2.MessageFactory;
import edu.harvard.i2b2.crc.axis2.SetDblookupDataMessage;

/**
 * <b>Axis2's service class<b>
 * 
 * <p>
 * This class implements methods related to webservice operation.
 * <li>For example http://localhost:8080/axis2/services/crc/serfinderrequest
 * http://localhost:8080/axis2/services/crc/pdorequest
 * 
 * $Id: QueryService.java,v 1.14 2009/09/10 19:32:06 rk903 Exp $
 * 
 * @author rkuttan
 * @see QueryRequestDelegate
 * @see PdoQueryRequestDelegate
 */
public class QueryService {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/** set pdo request constant used only inside this class **/
	private final String PDO_REQUEST = "PDO_REQUEST";

	/** set finder request constant used only inside this class **/
	private final String SETFINDER_REQUEST = "SETFINDER_REQUEST";

	/** get name info request constant used only inside this class **/
	private final String GETNAMEINFO_REQUEST = "GETNAMEINFO_REQUEST";

	/**
	 * Webservice function to handle setfinder request
	 * 
	 * @param omElement
	 *            request message wrapped in OMElement
	 * @return response message in wrapped inside OMElement
	 */
	public OMElement request(OMElement omElement) {
		Assert.notNull(omElement,
				"Setfinder request OMElement must not be null");
		log.debug("Inside setfinder request " + omElement);
		return handleRequest(SETFINDER_REQUEST, omElement);
	}

	/**
	 * Webservice function to handle pdo request
	 * 
	 * @param omElement
	 *            request message wrapped in OMElement
	 * @return response message in wrapped inside OMElement
	 */
	public OMElement pdorequest(OMElement omElement) {
		Assert.notNull(omElement, "PDO request OMElement must not be null");
		log.debug("Inside pdo request " + omElement);
		return handleRequest(PDO_REQUEST, omElement);
	}
	
	/**
	 * Webservice function to handle find request
	 * 
	 * @param omElement
	 *            request message wrapped in OMElement
	 * @return response message in wrapped inside OMElement
	 */
	public OMElement getNameInfo(OMElement omElement) {
		Assert.notNull(omElement, "getNameInfo  OMElement must not be null");
		log.debug("Inside getNameInfo request " + omElement);
		return handleRequest(GETNAMEINFO_REQUEST, omElement);
	}

	public OMElement publishDataRequest(OMElement request) {
		Assert.notNull(request,
				"publish data request OMElement must not be null");
		log.debug("Inside publish data request " + request);
		//TODO removed loader
		// Added back
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			PublishDataRequestHandler handler = new PublishDataRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);
			responseElement = buildOMElementFromString(response);

		} catch (Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;

	}

	public OMElement bulkLoadRequest(OMElement request) {
		Assert.notNull(request,
				"bulk load request OMElement must not be null");
		log.debug("Inside bulk load request " + request);
		
		//LoaderQueryReqDel handles permissions...
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			BulkLoadRequestHandler handler = new BulkLoadRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);
			responseElement = buildOMElementFromString(response);

		} catch (Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;

	}	
	
	public OMElement getLoadDataStatusRequest(OMElement request) {
		Assert.notNull(request,
				"get load Data status request OMElement must not be null");
		log.debug("Inside load status request " + request);
		
		//LoaderQueryReqDel handles permissions...
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try {
			String requestXml = request.toString();
			GetLoadStatusRequestHandler handler = new GetLoadStatusRequestHandler(
					requestXml);
			String response = queryDelegate.handleRequest(requestXml, handler);
			responseElement = buildOMElementFromString(response);

		} catch (Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return responseElement;

	}	
	
    
	//swc20160523 copied from edu.harvard.i2b2.im/src/edu/harvard/i2b2/im/ws/IMService.java
    private OMElement execute(DbLookupReqHandler handler, long waitTime) throws I2B2Exception {
        //do processing inside thread, so that service could send back message with timeout error.  
    	OMElement returnElement = null;   	
        String unknownErrorMessage = "Error message delivered from the remote server \nYou may wish to retry your last action";  
        ExecutorRunnable er = new ExecutorRunnable();        
        er.setDbLookupReqHandler(handler);
        Thread t = new Thread(er);
        String dataResponse = null;
        log.info("waiting " + waitTime + "ms for response from remote server processing " + handler.getClass().getName());
        synchronized (t) {
        	t.start();
        	try {
        		long startTime = System.currentTimeMillis();
        		long deltaTime = -1;
        		while((er.isJobCompleteFlag() == false) && (deltaTime < waitTime)) {
        			if (waitTime > 0) {
        				t.wait(waitTime - deltaTime);
        				deltaTime = System.currentTimeMillis() - startTime;
        			} else {
       					t.wait();
       				}
       			}
            	dataResponse = er.getOutputString();
           		if (dataResponse == null) {
           			ResponseMessageType responseMsgType = null;
             		if (!er.isJobCompleteFlag()) {
            			String timeOuterror = "Remote server timed out after waittime of " + waitTime + "ms.";            				
            			log.error(timeOuterror);
            		    responseMsgType = MessageFactory.doBuildErrorResponse(null, timeOuterror);
             		} else {
             			if (null != er.getJobException()) {
            			log.error("jobException: " + er.getJobException().getMessage());            		    	
            			dataResponse = MessageFactory.convertToXMLString(responseMsgType);
                 		} else {
                			log.error("CRC data response is null!");
               				log.info("CRC waited " + deltaTime + "ms for " + handler.getClass().getName());
                 		}
            		    log.info("waitTime is " + waitTime);
            			responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrorMessage);
           			   	dataResponse = MessageFactory.convertToXMLString(responseMsgType);
            		}
        			dataResponse = MessageFactory.convertToXMLString(responseMsgType);
            	}
        	} catch (InterruptedException e) {
        		log.error(e.getMessage());
       			throw new I2B2Exception("Thread error while running CRC job!");
       		} finally {
       			t.interrupt();
       			er = null;
       			t = null;
       		}
       	}
        returnElement = MessageFactory.createResponseOMElementFromString(dataResponse);
        return returnElement;
    }

	
	/** swc20160523
	 * This function is main webservice interface to get the I2B2HIVE.CRC_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            getAllDblookupsElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getAllDblookups(OMElement getAllDblookupsElement) throws I2B2Exception {
		String crcDataResponse = null;
		String unknownErrMsg = null;
		if (null == getAllDblookupsElement) {
			log.error("Incoming CRC request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			crcDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(crcDataResponse);
		}
		String requestElementString = getAllDblookupsElement.toString();
		GetAllDblookupsDataMessage dblookupsDataMsg = new GetAllDblookupsDataMessage(requestElementString);
		long waitTime = 0;
		if (null != dblookupsDataMsg.getRequestMessageType()) {
			if (null != dblookupsDataMsg.getRequestMessageType().getRequestHeader()) {
				waitTime = dblookupsDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
			}
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new GetAllDblookupsHandler(dblookupsDataMsg), waitTime);
	}
	
	/** swc20160523
	 * This function is main webservice interface to get specific I2B2HIVE.CRC_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            getDblookupElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getDblookup(OMElement getDblookupElement) throws I2B2Exception {
		String crcDataResponse = null;
		String unknownErrMsg = null;
		if (null == getDblookupElement) {
			log.error("Incoming CRC request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			crcDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(crcDataResponse);
		}
		String requestElementString = getDblookupElement.toString();
		GetDblookupDataMessage dblookupDataMsg = new GetDblookupDataMessage(requestElementString);
		long waitTime = 0;
		if (null != dblookupDataMsg.getRequestMessageType()) {
			if (null != dblookupDataMsg.getRequestMessageType().getRequestHeader()) {
				waitTime = dblookupDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
			}
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new GetDblookupHandler(dblookupDataMsg), waitTime);
	}
	
	/** swc20160523
	 * This function is main webservice interface to add a new entry to the I2B2HIVE.CRC_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            getAllDblookupsElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement setDblookup(OMElement setDblookupElement) throws I2B2Exception {
		String crcDataResponse = null;
		String unknownErrMsg = null;
		if (null == setDblookupElement) {
			log.error("Incoming CRC request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			crcDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(crcDataResponse);
		}
		String requestElementString = setDblookupElement.toString();
		SetDblookupDataMessage dblookupDataMsg = new SetDblookupDataMessage(requestElementString);
		long waitTime = 0;
		if (null != dblookupDataMsg.getRequestMessageType()) {
			if (null != dblookupDataMsg.getRequestMessageType().getRequestHeader()) {
				waitTime = dblookupDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
			}
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new SetDblookupHandler(dblookupDataMsg), waitTime);
	}
	
	/** swc20160523
	 * This function is main webservice interface to delete specific I2B2HIVE.CRC_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            deleteDblookupElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement deleteDblookup(OMElement deleteDblookupElement) throws I2B2Exception {
		String crcDataResponse = null;
		String unknownErrMsg = null;
		if (null == deleteDblookupElement) {
			log.error("Incoming CRC request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			crcDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(crcDataResponse);
		}
		String requestElementString = deleteDblookupElement.toString();
		DeleteDblookupDataMessage dblookupDataMsg = new DeleteDblookupDataMessage(requestElementString);
		long waitTime = 0;
		if (null != dblookupDataMsg.getRequestMessageType()) {
			if (null != dblookupDataMsg.getRequestMessageType().getRequestHeader()) {
				waitTime = dblookupDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
			}
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new DeleteDblookupHandler(dblookupDataMsg), waitTime);
	}
	

	
	//TODO removed loader   
	// added back above (lcp5)
	/*
	public OMElement getLoadDataStatusRequest(OMElement request) {
		Assert.notNull(request,
				"Data load status request OMElement must not be null");
		log.debug("Inside data load status request " + request);
		ProviderRestService rs = new ProviderRestService();
		return rs.getLoadDataStatusRequest(request);
	}

	public OMElement getMissingTermRequest(OMElement request) {
		Assert.notNull(request,
				"Missing term request OMElement must not be null");
		log.debug("Inside missing term request " + request);
		ProviderRestService rs = new ProviderRestService();
		return rs.getMissingTermRequest(request);
	}
	 */
	// --------------------------------------------
	// Creates delegate based on the request type
	// --------------------------------------------
	private OMElement handleRequest(String requestType, OMElement request) {
		RequestHandlerDelegate requestHandlerDelegate = null;

		if (requestType.equals(PDO_REQUEST)) {
			requestHandlerDelegate = new PdoQueryRequestDelegate();
		} else if (requestType.equals(SETFINDER_REQUEST)) {
			requestHandlerDelegate = new QueryRequestDelegate();
		} else if (requestType.equals(GETNAMEINFO_REQUEST)) {
			requestHandlerDelegate = new GetNameInfoRequestDelegate();			
		}
		OMElement returnElement = null;
		try {
			// call delegate's handleRequest function
			String response = requestHandlerDelegate.handleRequest(request
					.toString());
			log.debug("Response in service" + response);
			returnElement = buildOMElementFromString(response);
		} catch (XMLStreamException e) {
			log.error("xml stream exception", e);
		} catch (I2B2Exception e) {
			log.error("i2b2 exception", e);
		} catch (Throwable e) {
			log.error("Throwable", e);
		}
		return returnElement;
	}

	/**
	 * Function constructs OMElement for the given String
	 * 
	 * @param xmlString
	 * @return OMElement
	 * @throws XMLStreamException
	 */
	private OMElement buildOMElementFromString(String xmlString)
			throws XMLStreamException {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		StringReader strReader = new StringReader(xmlString);
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement element = builder.getDocumentElement();
		return element;
	}
}
