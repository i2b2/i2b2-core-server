package edu.harvard.i2b2.crc.dao;


public interface IDAOFactory {

	// List of DAO types supported by the factory
	public PatientDataDAOFactory getPatientDataDAOFactory();

	public SetFinderDAOFactory getSetFinderDAOFactory();

}
