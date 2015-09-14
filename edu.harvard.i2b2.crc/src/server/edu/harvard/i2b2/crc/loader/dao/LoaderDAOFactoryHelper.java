package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public  class LoaderDAOFactoryHelper {

	public static final String ORACLE = "ORACLE";
	public static final String SQLSERVER = "SQLSERVER";
	public static final String POSTGRESQL = "POSTGRESQL";
	DataSourceLookup dataSourceLookup = null;

	public LoaderDAOFactoryHelper(String hiveId, String projectId, String ownerId) throws I2B2DAOException {
	    try { 
			DataSourceLookupHelper dsHelper = new DataSourceLookupHelper();
			dataSourceLookup = dsHelper.matchDataSource(hiveId, projectId, ownerId);
		} catch(I2B2Exception i2b2Ex) { 
	    	throw new I2B2DAOException("DataSource lookup error" +i2b2Ex.getMessage(),i2b2Ex);
	    }
	}

	public LoaderDAOFactoryHelper(DataSourceLookup dataSourceLookup) throws I2B2DAOException {
		
		if (dataSourceLookup.getDataSource() == null ) {
			throw new I2B2DAOException("DataSource value is missing in DataSourceLookup parameter");
		}
		if (dataSourceLookup.getServerType() == null ) {
			throw new I2B2DAOException("Server type value is missing in DataSourceLookup parameter");
		}
		if (dataSourceLookup.getFullSchema() == null) {
			throw new I2B2DAOException("Full schema name is missing in DataSourceLookup parameter");
		}
		this.dataSourceLookup = dataSourceLookup;
	}

	public ILoaderDAOFactory getDAOFactory() throws I2B2DAOException {
		String dataSourceName = dataSourceLookup.getServerType();
		if (dataSourceName.equalsIgnoreCase(ORACLE)) {
			return new OracleLoaderDAOFactory(dataSourceLookup);
		} else if (dataSourceName.equalsIgnoreCase(SQLSERVER)) {
			return new OracleLoaderDAOFactory(dataSourceLookup);
		} else if (dataSourceName.equalsIgnoreCase(POSTGRESQL)) {
			return new OracleLoaderDAOFactory(dataSourceLookup);
		} else {
			return null;
		}
	}

}
