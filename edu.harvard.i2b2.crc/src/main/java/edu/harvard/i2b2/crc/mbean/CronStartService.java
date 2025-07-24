/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.mbean;

import javax.naming.InitialContext;

//import org.jboss.system.ServiceMBeanSupport;

import edu.harvard.i2b2.crc.ejb.analysis.CronEjbLocal;
import edu.harvard.i2b2.crc.ejb.analysis.LargeCronEjbLocal;

public class CronStartService // extends ServiceMBeanSupport 
		{

	public void startService() throws Exception {
		InitialContext ic = new InitialContext();
		CronEjbLocal cronLocal = (CronEjbLocal) ic.lookup("QP1/CronEjb/local");
		cronLocal.start();

		LargeCronEjbLocal largeCronLocal = (LargeCronEjbLocal) ic
				.lookup("QP1/LargeCronEjb/local");

		largeCronLocal.start();

	}

	public void stopService() {
		// unbind(jndiName);
	}
}
