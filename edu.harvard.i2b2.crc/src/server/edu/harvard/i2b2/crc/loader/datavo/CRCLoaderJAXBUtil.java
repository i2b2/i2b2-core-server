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
package edu.harvard.i2b2.crc.loader.datavo;

import java.util.List;


import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

/**
 * Factory class to create jaxb context Since jaxb context is tread safe, only
 * one instance is created for this cell. The package used for jaxb context is
 * read from spring config file $Id: CRCLoaderJAXBUtil.java,v 1.1 2008/01/21
 * 16:09:02 rk903 Exp $
 * 
 * @author rkuttan
 */
public class CRCLoaderJAXBUtil {
	private static edu.harvard.i2b2.common.util.jaxb.JAXBUtil jaxbUtil = null;

	private CRCLoaderJAXBUtil() {
	}

	@SuppressWarnings("unchecked")
	public static edu.harvard.i2b2.common.util.jaxb.JAXBUtil getJAXBUtil() {
		if (jaxbUtil == null) {
			jaxbUtil = new edu.harvard.i2b2.common.util.jaxb.JAXBUtil(edu.harvard.i2b2.crc.util.JAXBConstant.DEFAULT_PACKAGE_NAME);
		}
		return jaxbUtil;
	}

}
