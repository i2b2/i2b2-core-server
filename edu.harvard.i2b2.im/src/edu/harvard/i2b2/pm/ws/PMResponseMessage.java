/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Mike Mendis
 * 		Raj Kuttan
 */
package edu.harvard.i2b2.pm.ws;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.im.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.im.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.im.datavo.pm.ConfigureType;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;
import edu.harvard.i2b2.im.datavo.pm.ProjectsType;
import edu.harvard.i2b2.im.util.IMJAXBUtil;


public class PMResponseMessage {

	public static final String THIS_CLASS_NAME = PMResponseMessage.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);	
    private ResponseMessageType pmRespMessageType = null;
    
	public PMResponseMessage() {}
	
	public StatusType processResult(String response) {	
		StatusType status = null;
		
		JAXBElement jaxbElement;
		try {
			jaxbElement = IMJAXBUtil.getJAXBUtil().unMashallFromString(response);
		} catch (JAXBUtilException e) {
			return status;
		}

		pmRespMessageType  = (ResponseMessageType) jaxbElement.getValue();

		// Get response message status 
		ResponseHeaderType responseHeader = pmRespMessageType.getResponseHeader();
		status = responseHeader.getResultStatus().getStatus();

		return status;
	}
	
	public ProjectsType readProjectsInfo() throws Exception {
		ProjectsType pmResponse = null;
			
			BodyType bodyType = pmRespMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper(); 
			pmResponse = (ProjectsType)helper.getObjectByClass(bodyType.getAny(), ProjectsType.class);
		
		return pmResponse;
	}
	
	public ConfigureType readUserInfo() throws Exception {
		ConfigureType pmResponse = null;
			
			BodyType bodyType = pmRespMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper(); 
			pmResponse = (ConfigureType)helper.getObjectByClass(bodyType.getAny(), ConfigureType.class);
		
		return pmResponse;
	}
	
}
