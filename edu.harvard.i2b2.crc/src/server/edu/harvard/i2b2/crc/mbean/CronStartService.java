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
