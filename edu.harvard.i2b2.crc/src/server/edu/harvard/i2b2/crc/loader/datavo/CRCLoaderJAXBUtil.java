/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.datavo;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;

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
			BeanFactory springBean = CRCLoaderUtil.getInstance()
					.getSpringBeanFactory();
			List jaxbPackageName = (List) springBean.getBean("jaxbPackage");
			String[] jaxbPackageNameArray = (String[]) jaxbPackageName
					.toArray(new String[] {

					});
			jaxbUtil = new edu.harvard.i2b2.common.util.jaxb.JAXBUtil(
					jaxbPackageNameArray);
		}
		return jaxbUtil;
	}

}
