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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.delegate.RequestHandler;
import edu.harvard.i2b2.pm.delegate.ServicesHandler;
import edu.harvard.i2b2.pm.ws.ExecutorRunnable;
import edu.harvard.i2b2.pm.ws.MessageFactory;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is webservice skeleton class. It passes incoming report to PFT parser
 * and collects parsed pft concepts. Then these parsed concepts returned back to
 * webservice client in Patient Data Object XML format.
 *
 */
public class PMService {
	private static Log log = LogFactory.getLog(PMService.class);

	private static String msgVersion = "1.1";
	
	public OMElement getVersion(OMElement getPMDataElement)
	throws I2B2Exception, JAXBUtilException {
		
		Pattern p = Pattern.compile("<password>.+</password>");
		Matcher m = p.matcher(getPMDataElement.toString());
		String outString = m.replaceAll("<password>*********</password>");
	
		p = Pattern.compile(">.+</ns9:set_password>");
		m = p.matcher(outString);
		outString = m.replaceAll(">*********</ns9:set_password>");
		log.debug("Received Request PM Element " + outString);

		OMElement returnElement = null;


		if (getPMDataElement == null) {
			log.error("Incoming Version request is null");
			throw new I2B2Exception("Incoming Version request is null");
		}


		VersionMessage servicesMsg = new VersionMessage(getPMDataElement.toString());

        String version = servicesMsg.getRequestMessageType().getMessageBody().getGetMessageVersion().toString();
        if (version.equals(""))
        {
			edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType pmDataResponse = new edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType();

			edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType.MessageBody mb = new edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType.MessageBody();
			mb.setI2B2MessageVersion(msgVersion);
			pmDataResponse.setMessageBody(mb);
        	
			String xmlMsg = MessageFactory.convertToXMLString(pmDataResponse);

	        try {
	            returnElement = MessageFactory.createResponseOMElementFromString(xmlMsg);
	            log.debug("my pm repsonse is: " + pmDataResponse);
	            log.debug("my return is: " + returnElement);
	        } catch (XMLStreamException e) {
	            log.error("Error creating OMElement from response string " +
	            		pmDataResponse, e);
	        }

        }
		
		return returnElement;

	}
	
	/**
	 * This function is main webservice interface to get pulmonary data from
	 * pulmonary report. It uses AXIOM elements(OMElement) to conveniently parse
	 * xml messages.
	 *
	 * It excepts incoming request in i2b2 message format, which wraps PFT
	 * report inside patientdata object. The response is also will be in i2b2
	 * message, which will wrap patientdata object. Patient data object will
	 * have all the extracted pft concepts from the report.
	 *
	 *
	 * @param getServices
	 * @return OMElement in i2b2message format
	 * @throws PortletServiceNotFoundException 
	 * @throws PortletServiceUnavailableException 
	 * @throws Exception
	 */
	public OMElement getServices(OMElement getPMDataElement)
	throws I2B2Exception {
		
		/*

    	OMElement returnElement = null;
    	String pmDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";

    	if (getPMDataElement == null) {
    		log.error("Incoming PM request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			pmDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(pmDataResponse);
    	}
    	
		ServicesMessage servicesMsg = new ServicesMessage(getPMDataElement.toString());
        
       // String requestElementString = getPMDataElement.toString();
       // childrenDataMsg.setRequestMessageType(requestElementString);
   
        long waitTime = 0;
        if (servicesMsg.getRequestMessageType() != null) {
            if (servicesMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = servicesMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }
        
        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new ServicesHandler(servicesMsg), waitTime);
        */
		
		OMElement returnElement = null;


		if (getPMDataElement == null) {
			log.error("Incoming PM request is null");
			throw new I2B2Exception("Incoming PM request is null");
		}

		Pattern p = Pattern.compile("<password>.+</password>");
		Matcher m = p.matcher(getPMDataElement.toString());
		String outString = m.replaceAll("<password>*********</password>");
	
		p = Pattern.compile(">.+</ns9:set_password>");
		m = p.matcher(outString);
		outString = m.replaceAll(">*********</ns9:set_password>");
		log.debug("Received Request PM Element " + outString);

		
		log.debug("Begin getting servicesMsg");
		ServicesMessage servicesMsg = new ServicesMessage(getPMDataElement.toString());
		long waitTime = 0;

		if (servicesMsg.getRequestMessageType() != null) {
			if (servicesMsg.getRequestMessageType().getRequestHeader() != null) {
				waitTime = servicesMsg.getRequestMessageType()
				.getRequestHeader()
				.getResultWaittimeMs();
			}
		}

		log.debug("Completed getting servicesMsg, waittime is: " + waitTime);

		//do PM processing inside thread, so that  
		// service could sends back message with timeout error.

		String pmDataResponse = null;
		try {
		ExecutorRunnable er = new ExecutorRunnable();
		//er.setInputString(requestElementString);
		log.debug("begin setRequestHandler, my servicesMsg: " + servicesMsg);

		er.setRequestHandler(new ServicesHandler(servicesMsg));
		log.debug("middle setRequestHandler");
		
		
		log.debug("end setRequestHandler");

		
		Thread t = new Thread(er);
		
		ResponseMessageType responseMsgType = null;
		
		synchronized (t) {
			t.start();

			try {
				//if (waitTime > 0) {
				//	t.wait(waitTime);
				//} else {
				//	t.wait();
				//}
				
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
				pmDataResponse = er.getOutputString();

				if (pmDataResponse == null) {
					if (er.getJobException() != null) {
						pmDataResponse = "";
						throw new I2B2Exception("Portal is not property configured.");
					} 
					else if (er.isJobCompleteFlag() == false) {
						String timeOuterror = "Result waittime millisecond <result_waittime_ms> :" +
						waitTime +
						" elapsed, try again with increased value";
						log.debug(timeOuterror);

						responseMsgType = MessageFactory.doBuildErrorResponse(null,
								timeOuterror);
						pmDataResponse = MessageFactory.convertToXMLString(responseMsgType);
					} 
				}
			} catch (InterruptedException e) {
	        	log.error("Error in thread: " + e.getMessage());

				e.printStackTrace();
				throw new I2B2Exception("Thread error while running PM job " +
						getPMDataElement, e);
			} finally {
				t.interrupt();
				er = null;
				t = null;
			}
		}
		
        } catch (Exception e) {
        	log.error("Error: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            returnElement = MessageFactory.createResponseOMElementFromString(pmDataResponse);
            log.debug("my pm repsonse is: " + pmDataResponse);
            log.debug("my return is: " + returnElement);
        } catch (XMLStreamException e) {
            log.error("Error creating OMElement from response string " +
            		pmDataResponse, e);
        }
        
		return returnElement;
		
	}
}







