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
