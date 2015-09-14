/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.datavo;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Factory class to create jaxb context Since jaxb context is tread safe, only
 * one instance is created for this cell. The package used for jaxb context is
 * read from spring config file $Id: CRCJAXBUtil.java,v 1.6 2007/09/11 20:05:40
 * rk903 Exp $
 * 
 * @author rkuttan
 */
public class CRCJAXBUtil {
	private static JAXBUtil jaxbUtil = null;
	private static JAXBUtil queryDefjaxbUtil = null;
	private static JAXBUtil analysisDefjaxbUtil = null;

	//private CRCJAXBUtil() {
	//}

	//@SuppressWarnings("unchecked")
	public static JAXBUtil getJAXBUtil() {
		if (jaxbUtil == null) {
			BeanFactory springBean = QueryProcessorUtil.getInstance()
					.getSpringBeanFactory();
			List jaxbPackageName = (List) springBean.getBean("jaxbPackage");
			String[] jaxbPackageNameArray = (String[]) jaxbPackageName
					.toArray(new String[] {

					});
			jaxbUtil = new JAXBUtil(
					jaxbPackageNameArray);
		}

		return jaxbUtil;
	}

	//@SuppressWarnings("unchecked")
	public static JAXBUtil getQueryDefJAXBUtil() {
		if (queryDefjaxbUtil == null) {
			queryDefjaxbUtil = new JAXBUtil(
					edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType.class);
		}
		return queryDefjaxbUtil;
	}

	//@SuppressWarnings("unchecked")
	public static JAXBUtil getAnalysisDefJAXBUtil() {
		if (analysisDefjaxbUtil == null) {
			analysisDefjaxbUtil = new JAXBUtil(
					edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionRequestType.class);
		}
		return analysisDefjaxbUtil;
	}
}
