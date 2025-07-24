/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;

public class PMServiceAccountUtil {
	private static Log log = LogFactory.getLog(PMServiceAccountUtil.class);

	public static SecurityType getServiceSecurityType(String domainId)
			throws I2B2Exception {
		SecurityType securityType = new SecurityType();

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String serviceAccountUser = qpUtil
				.getCRCPropertyValue("edu.harvard.i2b2.crc.pm.serviceaccount.user");
		String serviceAccountPassword = qpUtil
				.getCRCPropertyValue("edu.harvard.i2b2.crc.pm.serviceaccount.password");

		securityType.setUsername(serviceAccountUser);
		PasswordType passwordType = new PasswordType();
		passwordType.setValue(serviceAccountPassword);
		securityType.setPassword(passwordType);
		securityType.setDomain(domainId);
		log.debug("CRC using service account from property file ["
				+ serviceAccountUser + "]");

		return securityType;
	}
}
