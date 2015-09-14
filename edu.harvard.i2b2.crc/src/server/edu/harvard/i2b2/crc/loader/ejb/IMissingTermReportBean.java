package edu.harvard.i2b2.crc.loader.ejb;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.GetMissingTermRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermReportResponseType;

public interface IMissingTermReportBean  {
	public MissingTermReportResponseType getMissingTermReport(DataSourceLookup dataSourceLookup, GetMissingTermRequestType getMissingTermMessage) throws I2B2Exception ;

}