/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
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
import edu.harvard.i2b2.crc.loader.ws.ProviderRestService;

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
