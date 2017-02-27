package edu.harvard.i2b2.crc.dao.pdo.input;

import java.util.List;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;

public interface IFactRelatedQueryHandler {

	/**
	 * Function to build and execute pdo sql and build plain pdo's observation
	 * fact
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ObservationSet> getPdoObservationFact() throws I2B2DAOException;

	/**
	 * Function to build and execute pdo sql and build table pdo's observation
	 * fact
	 * 
	 * @return ObservationSet list
	 * @throws I2B2DAOException
	 */
	public List<ObservationSet> getTablePdoObservationFact()
			throws I2B2DAOException;

	/**
	 * Returns provider id, belong to the facts
	 * 
	 * @return list of provider/observer id
	 */
	public List<String> getProviderFactList();

	/**
	 * Returns concept code belong to the facts
	 * 
	 * @return
	 */
	public List<String> getConceptFactList();
	
	/**
	 * Returns modifier code belong to the facts
	 * 
	 * @return
	 */
	public List<String> getModifierFactList();

	/**
	 * Returns patient number belong to the facts
	 * 
	 * @return
	 */
	public List<String> getPatientFactList();

	/**
	 * Returns encounter number belong to the facts
	 * 
	 * @return list of encounter number
	 */
	public List<String> getVisitFactList();
	

	/**
	 * This is the main function to build query for plain and table pdo request
	 * 
	 * @param pdoType
	 * @return String
	 * @throws I2B2DAOException
	 */
	public String buildQuery(PanelType panel, String pdoType)
			throws I2B2DAOException;

	public String buildTotalQuery(PanelType panel, String pdoType)
			throws I2B2DAOException;

	public List<String> getPanelSqlList();

	
	public void setProjectParamMap(Map projectParamMap);
	
	public void setModifierMetadataXmlMap(Map<String,XmlValueType> modifierMetadataXmlMap) ;
	
	public void setRequestVersion(String requestVersion);
	
	
}