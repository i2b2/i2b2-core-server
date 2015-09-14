/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.delegate.fr;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;

public class FileRepositoryServiceClient {
	private static ServiceClient sender = null;

	private FileRepositoryServiceClient() {
	}

	public static ServiceClient getServiceClient() {
		if (sender == null) {
			try {
				sender = new ServiceClient();
			} catch (AxisFault e) {
				e.printStackTrace();
			}
		}
		return sender;
	}

}
