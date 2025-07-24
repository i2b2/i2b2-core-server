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
package edu.harvard.i2b2.fr.delegate.pm;

import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.fr.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.fr.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.fr.datavo.pm.ConfigureType;
import edu.harvard.i2b2.fr.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.fr.util.FRUtil;


public class PMServiceDriver {
	private static Log log = LogFactory.getLog(PMServiceDriver.class);

	/**
	 * Function to send getRoles request to PM web service
	 * 
	 * @param GetUserConfigurationType  userConfig we wish to get data for
	 * @return A String containing the PM web service response 
	 */
	public  String getRoles1( SecurityType userSec) throws AxisFault,I2B2Exception {
		String response = null;	
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
			String getRolesRequestString = reqMsg.doBuildXML(new GetUserConfigurationType(), userSec);
			OMElement getPm = edu.harvard.i2b2.common.util.axis2.ServiceClient.getPayLoad(getRolesRequestString);
			String pmEPR = "";
			try {
				pmEPR = FRUtil.getInstance().getProjectManagementCellUrl();
				log.debug("project management cell URL " + pmEPR);
			} catch (I2B2Exception e1) {
				e1.printStackTrace();
				log.error("Error getting project management cell URL " + e1.getMessage());
			}
			Options options = new Options();
			options.setTo( new EndpointReference(pmEPR));
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setTimeOutInMilliSeconds(50000);
			ServiceClient sender = PMServiceClient.getServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(getPm);
			if (result != null) {
				response = result.toString();
				//log.debug(response);

			}
			sender.cleanup();
		} catch (AxisFault e) {
			log.error(e);
			throw new I2B2Exception (e.getMessage(), e);
			//	} catch (Exception e) {
			//		log.error(e);
			//		throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e);
			throw new I2B2Exception (e.getMessage(), e);
		}
		return response;
	}



	public static ConfigureType checkValidUser(SecurityType security) throws I2B2Exception,AxisFault  
	{
		ConfigureType pmResponseUserInfo  = null;

		// Are we bypassing the PM cell?  Look in properties file.
		Boolean pmBypass = false;
		String pmBypassRole = null;
		String pmBypassProject = null;
		String response = null;

			try {
				GetUserConfigurationType userConfigType = new GetUserConfigurationType();
				PMResponseMessage msg = new PMResponseMessage();
				log.debug("calling PM service!!!!!!!!!!!!!!");
				response = new PMServiceDriver().getRoles1( security);
				//log.debug("PM response" + response);

				StatusType procStatus = msg.processResult(response);
				if (!procStatus.getType().equalsIgnoreCase("ERROR")) { 
					pmResponseUserInfo = msg.readUserInfo();
					//projectType = pmResponseUserInfo.getUser().getProject().get(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.debug("Error in checkValidUser: " + e.getMessage());
				throw new I2B2Exception(e.getMessage());
				//pmResponseUserInfo = null;
			}
		
		return pmResponseUserInfo;
	}


}
