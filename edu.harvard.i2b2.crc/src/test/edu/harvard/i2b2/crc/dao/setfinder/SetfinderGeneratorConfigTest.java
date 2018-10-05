/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class SetfinderGeneratorConfigTest {

	@Test
	public void test() {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		BeanFactory bf = qpUtil.getSpringBeanFactory();
		Map sfMap = (Map)bf.getBean("setFinderResultGeneratorMap");
		String rg = (String)sfMap.get("PATIENTSET");
		try {
			Class generatorClass = Class.forName(rg, true, Thread.currentThread().getContextClassLoader());
			IResultGenerator gi = (IResultGenerator)generatorClass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
