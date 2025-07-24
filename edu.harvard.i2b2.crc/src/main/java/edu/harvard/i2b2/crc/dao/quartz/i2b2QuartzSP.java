package edu.harvard.i2b2.crc.dao.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class i2b2QuartzSP implements Job {

	    /**
	     * <p>
	     * Empty constructor for job initilization
	     * </p>
	     * <p>
	     * Quartz requires a public empty constructor so that the
	     * scheduler can instantiate the class whenever it needs.
	     * </p>
	     */
	
	

	protected final Log logger = LogFactory.getLog(getClass());
	
	    public i2b2QuartzSP() {
	    }

	    /**
	     * <p>
	     * Called by the <code>{@link org.quartz.Scheduler}</code> when a
	     * <code>{@link org.quartz.Trigger}</code> fires that is associated with
	     * the <code>Job</code>.
	     * </p>
	     * 
	     * @throws JobExecutionException
	     *             if there is an exception while executing the job.
	     */
	    public void execute(JobExecutionContext context)
	        throws JobExecutionException {

	    	logger.info("test");
	    }



}
