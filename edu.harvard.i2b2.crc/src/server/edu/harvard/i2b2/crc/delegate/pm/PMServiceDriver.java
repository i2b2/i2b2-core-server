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
package edu.harvard.i2b2.crc.delegate.pm;

import java.util.Arrays;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class PMServiceDriver {
	private static Log log = LogFactory.getLog(PMServiceDriver.class);

	public ProjectType checkValidUser(SecurityType security, String projectId)
			throws I2B2Exception, AxisFault, JAXBUtilException {
		ProjectType projectType = null;


		//	CallPMUtil callPMUtil = new CallPMUtil(security, projectId);
			projectType = CallPMUtil.callUserProject(security, projectId);
		
		return projectType;
	}

	public boolean isAdmin(SecurityType securityType, String projectId) 
		throws I2B2Exception, AxisFault, JAXBUtilException {
		boolean isAdmin = false;
		//projectType = null;


		
		return CallPMUtil.callIsAdmin(securityType, projectId);
	}
}
