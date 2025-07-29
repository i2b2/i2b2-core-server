/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.util;

import java.util.Hashtable;

public class CacheUtil {

	static Hashtable rootNode = new Hashtable();
	
	public static Object get(Object key)
	{
		return rootNode.get(key);
	}

	public static Object remove(Object key)
	{
		return rootNode.remove(key);
	}
	public static void put(Object key, Object value)
	{
		try {
			rootNode.put(key, value);
		} catch (Exception e)
		{
			e.printStackTrace();
			
		}
	}

	
	/*
	 TODO mm: Removed JBOSS Cache
	public static Cache getCache() throws MalformedObjectNameException,
			NullPointerException {
		MBeanServer server = MBeanServerLocator.locateJBoss();

		ObjectName on = new ObjectName("jboss.cache:service=Cache");

		CacheJmxWrapperMBean cacheWrapper =

		(CacheJmxWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(
				server, on,

				CacheJmxWrapperMBean.class, false);

		Cache cache = cacheWrapper.getCache();
		return cache;
	}
	*/
}
