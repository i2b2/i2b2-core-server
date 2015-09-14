package edu.harvard.i2b2.crc.dao.setfinder;


import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;

public interface IQueryExecutorDao {
	public String executeSQL(
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList, boolean allowLargeTextValueConstrainFlag, String pmXMl)
			throws CRCTimeOutException, I2B2DAOException, I2B2Exception, JAXBUtilException;
}