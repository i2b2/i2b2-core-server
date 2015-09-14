package edu.harvard.i2b2.crc.loader.ejb;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;

public interface IDataMartLoaderBean {
	public LoadDataResponseType load(DataSourceLookup dataSourceLookup,
			String publishMessage, SecurityType i2b2SecurityType, long timeout,
			String fileSystemDefaultStorageResource) throws I2B2Exception;

}