package edu.harvard.i2b2.crc.loader.ejb;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;

public interface ILoaderStatusBean {

	public LoadDataListResponseType getLoadDataResponseByUserId(DataSourceLookup dataSourceLookup,String userId)
			throws I2B2Exception;

	public LoadDataResponseType getLoadDataResponseByUploadId(DataSourceLookup dataSourceLookup,int uploadId)
			throws I2B2Exception;

}