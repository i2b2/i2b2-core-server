package edu.harvard.i2b2.crc.dao.setfinder;


public interface IEncounterSetCollectionDao {

	/**
	 * function to add encounter to encounter set without out creating new db
	 * session
	 * 
	 * @param patientId
	 */
	public void addEncounter(long encounterId, long patientId);

	/**
	 * Set resultInstance before addEncounter
	 * 
	 * @param resultInstanceId
	 */
	public void createPatientEncCollection(String resultInstanceId);

	public String getResultInstanceId();

	/**
	 * Call this function at the end. i.e. after loading all patient with
	 * addPatient function, finally call this function to clear session
	 */
	public void flush();

}