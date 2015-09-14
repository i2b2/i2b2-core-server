package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.io.Serializable;
import java.util.Date;


/**
 * User profile data object
 * @author rk903
 *
 */
public class DataMartReport implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private long totalObservationFact = 0;
	private long totalPatient = 0;
	private long totalVisit = 0;
	private Date reportDate;
	
	public DataMartReport() { 
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public long getTotalObservationFact() {
		return totalObservationFact;
	}

	public void setTotalObservationFact(long totalObservationFact) {
		this.totalObservationFact = totalObservationFact;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public long getTotalPatient() {
		return totalPatient;
	}

	public void setTotalPatient(long totalPatient) {
		this.totalPatient = totalPatient;
	}

	public long getTotalVisit() {
		return totalVisit;
	}

	public void setTotalVisit(long totalVisit) {
		this.totalVisit = totalVisit;
	}


	
}
