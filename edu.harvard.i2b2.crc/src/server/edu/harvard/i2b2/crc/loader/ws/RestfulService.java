package edu.harvard.i2b2.crc.loader.ws;

/*
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 * You may not modify, use, reproduce, or distribute this software except in compliance with the terms
 * of the License at: http://developer.sun.com/berkeley_license.html
 * Author : Sameer Tyagi, Sun Microsystems s.t@sun.com
 * "Realizing Strategies for Document-Based Web Services With JAX-WS 2.0"
 *  http://java.sun.com/developer/technicalArticles/xml/jaxrpcpatterns
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

// import generated types
/**
 * 
 * @author rkuttan
 */
@javax.xml.ws.WebServiceProvider
@javax.xml.ws.ServiceMode(value = javax.xml.ws.Service.Mode.PAYLOAD)
public class RestfulService implements Provider<Source> {

	private JAXBContext jc;

	@javax.annotation.Resource(type = Object.class)
	protected WebServiceContext wsContext;

	public RestfulService() {

	}

	public Source invoke(Source source) {
		try {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			StreamResult sr = new StreamResult(bos);
			Transformer trans = TransformerFactory.newInstance()
					.newTransformer();
			Properties oprops = new Properties();
			oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperties(oprops);
			trans.transform(source, sr);
			System.out.println("**** Response ******" + bos.toString());

			MessageContext mc = wsContext.getMessageContext();
			String path = (String) mc.get(MessageContext.PATH_INFO);
			String method = (String) mc.get(MessageContext.HTTP_REQUEST_METHOD);
			System.out.println("Got HTTP " + method + " request for " + path);
			if (method.equals("GET"))
				return get(mc);
			if (method.equals("POST"))
				return post(source, mc);
			if (method.equals("PUT"))
				return put(source, mc);
			if (method.equals("DELETE"))
				return delete(source, mc);
			throw new WebServiceException("Unsupported method:" + method);
		} catch (Exception je) {
			throw new WebServiceException(je);
		}
	}

	/**
	 * Handles HTTP GET.
	 */
	private Source get(MessageContext mc) throws JAXBException {
		String path = (String) mc.get(MessageContext.PATH_INFO);
		if ((path.indexOf("/errortest") != -1) || path.equals("")
				|| path.equals("/")) {
			mc.put(MessageContext.HTTP_RESPONSE_CODE, 400);

		}

		// demonstrates verb in path strategy
		if (path != null && path.lastIndexOf("/acceptPO") != -1) {
			// this.acceptPO();
		} else {

		}
		throw new WebServiceException(
				"Webservice does not understand the operation you invoked="
						+ path);
	}

	/**
	 * Handles HTTP POST.
	 */
	private Source post(Source source, MessageContext mc) throws JAXBException {
		return null;
	}

	/**
	 * Handles HTTP PUT.
	 */
	private Source put(Source source, MessageContext mc) throws JAXBException {
		return null;
	}

	/**
	 * Handles HTTP DELETE.
	 */
	private Source delete(Source source, MessageContext mc)
			throws JAXBException {
		String path = (String) mc.get(MessageContext.PATH_INFO);
		path.replace("/", "");
		cancelPO(path);
		return new StreamSource((InputStream) null);
	}

	public String acceptPO(String order) throws WebServiceException {
		return "Accept PO" + order;

	}

	public String updatePO(String order) throws WebServiceException {
		return order + "Update PO";
	}

	public void cancelPO(String orderID) throws WebServiceException {
	}

	public String retreivePO(String orderID) throws WebServiceException {
		return orderID + "retreive PO ";
	}
}
