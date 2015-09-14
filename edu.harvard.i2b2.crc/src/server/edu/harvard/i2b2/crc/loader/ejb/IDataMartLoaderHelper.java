package edu.harvard.i2b2.crc.loader.ejb;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;

public interface IDataMartLoaderHelper {

	public void load(IUploaderDAOFactory uploaderDaoFactory, String userId,
			int uploadId, String localUploadFile, String publishMessage)
			throws I2B2Exception;

	public void deleteUploadData(IUploaderDAOFactory uploaderDaoFactory,
			int uploadId) throws I2B2Exception;

}