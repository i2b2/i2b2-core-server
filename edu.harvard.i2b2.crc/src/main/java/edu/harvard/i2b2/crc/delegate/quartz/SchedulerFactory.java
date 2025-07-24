package edu.harvard.i2b2.crc.delegate.quartz;


import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerFactory {
	 static StdSchedulerFactory schedFact = null;
	public SchedulerFactory () throws SchedulerException {
		if (schedFact== null)
			schedFact = new StdSchedulerFactory();

		//Scheduler sched = schedFact.getScheduler();

	}

	public static Scheduler getDefaultScheduler() throws SchedulerException {
		if (schedFact== null)
			schedFact = new StdSchedulerFactory();

		return  schedFact.getScheduler();

	}
}
 