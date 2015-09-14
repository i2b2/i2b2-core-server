package edu.harvard.i2b2.crc.dao.setfinder;

public interface IPatientSetCollectionDao {

	/**
	 * function to add patient to patient set without out creating new db
	 * session
	 * 
	 * @param patientId
	 */
	public void addPatient(long patientId);


	/**
	 * Set resultInstance before addPatient
	 * @param resultInstanceId
	 */
	public void createPatientSetCollection(String resultInstanceId) ;
	
	public String getResultInstanceId() ;
	
	/**
	 * Call this function at the end. i.e. after loading all patient with
	 * addPatient function, finally call this function to clear session
	 */
	public void flush();

}