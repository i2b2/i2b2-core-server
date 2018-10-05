/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.quartz;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzFactory {

	private static QuartzFactory instance = null;
	SchedulerFactory sf = null;
	Scheduler sched = null;

	protected QuartzFactory() {
		sf = new StdSchedulerFactory();

		try {
			sched = sf.getScheduler();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static QuartzFactory getInstance() {
		if (instance == null) {
			instance = new QuartzFactory();
		}
		return instance;
	}

	public Scheduler getScheduler() {
		return sched;
	}

}
