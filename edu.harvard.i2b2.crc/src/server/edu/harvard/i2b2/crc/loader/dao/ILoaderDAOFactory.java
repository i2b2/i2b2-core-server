package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface ILoaderDAOFactory {
	public  IUploaderDAOFactory getUpLoaderDAOFactory() throws I2B2Exception ;
}
