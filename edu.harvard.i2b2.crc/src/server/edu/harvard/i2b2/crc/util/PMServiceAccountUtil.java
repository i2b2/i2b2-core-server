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
