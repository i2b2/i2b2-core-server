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
