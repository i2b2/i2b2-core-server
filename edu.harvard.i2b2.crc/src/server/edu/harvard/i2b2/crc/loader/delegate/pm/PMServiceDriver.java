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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.delegate.pm;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.AxisFault;
//import org.apache.axis2.Constants;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axis2.client.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

public class PMServiceDriver {
	private static Log log = LogFactory.getLog(PMServiceDriver.class);
	protected static Log logesapi = LogFactory.getLog(PMServiceDriver.class);


	
	/**
	 * Function to send getRoles request to PM web service
	 * 
	 * @param GetUserConfigurationType  userConfig we wish to get data for
	 * 		  MessageHeaderType
	 * @return A String containing the PM web service response 
	 */

	public String getRoles(GetUserConfigurationType userConfig, MessageHeaderType header) throws I2B2Exception, AxisFault, Exception {
		String response = null;	
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
			String getRolesRequestString = reqMsg.buildXML(userConfig, header);
//			OMElement getPm = getPmPayLoad(getRolesRequestString);
			String pmEPR = CRCLoaderUtil.getInstance().getProjectManagementCellUrl();
			log.debug("project management cell URL " + pmEPR);
//			response = ServiceClient.sendREST(pmEPR, getPm);
			response = ServiceClient.sendREST(pmEPR, getRolesRequestString);
		} catch (I2B2Exception e1) {
			e1.printStackTrace();
			log.error("Error reading project management cell URL " + e1.getMessage());
			throw new I2B2Exception("Error reading project management cell URL " + e1.getMessage());
		} catch (AxisFault e2) {
			log.error(e2);
			throw e2;
		} catch (Exception e3) {
			log.error(e3.getMessage());
			e3.printStackTrace();
			throw e3;
		}
		return response;
	}
	

	/**
	 * Function to send getRoles request to PM web service
	 * 
	 * @param GetUserConfigurationType
	 *            userConfig we wish to get data for
	 * @return A String containing the PM web service response
	 */
	public String getRoles(SecurityType userSec) throws AxisFault,
			I2B2Exception {
		String response = null;
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
			String getRolesRequestString = reqMsg.doBuildXML(
					new GetUserConfigurationType(), userSec);
			OMElement getPm = ServiceClient.getPayLoad(getRolesRequestString);
			String pmEPR = "";
			try {
				pmEPR = CRCLoaderUtil.getInstance()
						.getProjectManagementCellUrl();
				log.debug("project management cell URL " + pmEPR);
			} catch (I2B2Exception e1) {
				e1.printStackTrace();
				log.error("Error reading project management cell URL "
						+ e1.getMessage());
				throw new I2B2Exception(
						"Error reading project management cell URL "
								+ e1.getMessage());
			}
			/*
			Options options = new Options();
			options.setTo(new EndpointReference(pmEPR));
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setTimeOutInMilliSeconds(50000);
			ServiceClient sender = PMServiceClient.getServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(getPm);
			if (result != null) {
				response = result.toString();
				log.debug("PM response message [" + response + "]");
			}
			sender.cleanup();
			*/
			
			 response = ServiceClient.sendREST(pmEPR, getPm);
		} catch (AxisFault e) {
			log.error(e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public ProjectType checkValidUser(SecurityType security, String projectId)
			throws I2B2Exception, AxisFault, JAXBUtilException {
		ProjectType projectType = null;

		// Are we bypassing the PM cell? Look in properties file.

		String response = null;

			PMResponseMessage msg = new PMResponseMessage();
			response = getRoles(security);
			StatusType procStatus = msg.processResult(response);
			if (procStatus.getType().equalsIgnoreCase("ERROR")) {
				//logesapi.debug("PM response error [" + procStatus.getValue() + "]");
				projectType = null;
			} else {
				ConfigureType pmResponseUserInfo = msg.readUserInfo();
				List<ProjectType> projectList = pmResponseUserInfo.getUser()
						.getProject();
				for (ProjectType pType : projectList) {
					if (pType.getId().equalsIgnoreCase(projectId)) {
						projectType = pType;
						break;
					}
				}
				if (projectType == null) {
					throw new I2B2Exception(
							"User not registered to the project[" + projectId
									+ "]");
				}

			}
		
		return projectType;
	}
}
