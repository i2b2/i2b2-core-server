/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
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

		// Are we bypassing the PM cell? Look in properties file.
		Boolean pmBypass = false;
		String pmBypassRole = null, pmBypassProject = null, response = null;
		try {
			pmBypass = QueryProcessorUtil.getInstance()
					.getProjectManagementByPassFlag();
			pmBypassRole = QueryProcessorUtil.getInstance()
					.getProjectManagementByPassRole();
			pmBypassProject = QueryProcessorUtil.getInstance()
					.getProjectManagementByPassProject();
			log.debug("Project Management bypass flag  from property file :["
					+ pmBypass + "] bypass role [" + pmBypassRole
					+ "] project [" + pmBypassProject + "]");
		} catch (I2B2Exception e1) {
			e1.printStackTrace();
			log
					.info("Could not read Project Management bypass setting, trying PM without bypass option");
		}

		if (pmBypass == true) {
			log.info("Using Project Management by pass option ");
			log
					.info("Using project Management bypass flag  from property file :["
							+ pmBypass
							+ "] bypass role ["
							+ pmBypassRole
							+ "] project [" + pmBypassProject + "]");
			projectType = new ProjectType();
			if (pmBypassRole != null) {
				String[] roles = pmBypassRole.split(",");
				projectType.getRole().addAll(Arrays.asList(roles));

			}
			projectType.setId(pmBypassProject);

		} else {
		//	CallPMUtil callPMUtil = new CallPMUtil(security, projectId);
			projectType = CallPMUtil.callUserProject(security, projectId);
		}
		return projectType;
	}
}