package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public class OracleLoaderDAOFactory implements ILoaderDAOFactory {
	
	private DataSourceLookup dataSourceLookup = null;
	
	public OracleLoaderDAOFactory(DataSourceLookup dataSourceLookup) { 
		this.dataSourceLookup = dataSourceLookup;
	}
	
	public  IUploaderDAOFactory getUpLoaderDAOFactory() throws I2B2Exception { 
		return new OracleUploaderDAOFactory(dataSourceLookup);
	}
}
