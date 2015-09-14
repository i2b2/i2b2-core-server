package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public class OracleUploaderDAOFactory implements IUploaderDAOFactory {

	private DataSourceLookup dataSourceLookup = null;
	private DataSource dataSource = null;

	/** log **/
	protected final static Log log = LogFactory
			.getLog(OracleUploaderDAOFactory.class);

	public OracleUploaderDAOFactory(DataSourceLookup dataSourceLookup)
			throws I2B2Exception {
		this.dataSourceLookup = dataSourceLookup;
		String dataSourceName = dataSourceLookup.getDataSource();
		log.info("Using datasource " + dataSourceName);
		try {
			// dataSource = (DataSource)
			// crcUtil.getSpringDataSource(dataSourceName);
			dataSource = ServiceLocator.getInstance().getAppServerDataSource(
					dataSourceName);
		} catch (I2B2Exception i2b2Ex) {
			log.error(i2b2Ex);
			throw new I2B2DAOException(
					"Error getting application/spring datasource "
							+ dataSourceName + " : " + i2b2Ex.getMessage(),
					i2b2Ex);
		}
	}

	/**
	 * Constructor for testing, datasource can be passed as parameter.
	 * 
	 * @param dataSourceLookup
	 * @param dataSource
	 */
	public OracleUploaderDAOFactory(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		this.dataSource = dataSource;
	}

	public IConceptDAO getConceptDAO() {
		return new ConceptDAO(dataSourceLookup, dataSource);
	}
	
	public IModifierDAO getModifierDAO() {
		return new ModifierDAO(dataSourceLookup, dataSource);
	}

	public IObservationFactDAO getObservationDAO() {
		return new ObservationFactDAO(dataSourceLookup, dataSource);
	}

	public IPatientDAO getPatientDAO() {
		return new PatientDAO(dataSourceLookup, dataSource);
	}

	public IPidDAO getPidDAO() {
		return new PidDAO(dataSourceLookup, dataSource);
	}

	public IEidDAO getEidDAO() {
		return new EidDAO(dataSourceLookup, dataSource);
	}

	public IProviderDAO getProviderDAO() {
		return new ProviderDAO(dataSourceLookup, dataSource);
	}

	public UploadStatusDAOI getUploadStatusDAO() {
		return new UploadStatusDAO(dataSourceLookup, dataSource);
	}

	public IVisitDAO getVisitDAO() {
		return new VisitDAO(dataSourceLookup, dataSource);
	}


	public IMissingTermDAO getMissingTermDAO() {
		return new MissingTermDAO(dataSourceLookup, dataSource);
	}


	public DataSourceLookup getDataSourceLookup() {
		// TODO Auto-generated method stub
		return dataSourceLookup;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;

	}

}
