package edu.harvard.i2b2.crc.dao;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class DataSourceLookupDAOFactory {

	/** log **/
	protected final static Log log = LogFactory
			.getLog(DataSourceLookupDAOFactory.class);

	public static final String ORACLE = "ORACLE";
	public static final String SQLSERVER = "SQLSERVER";
	public static final String POSTGRESQL = "POSTGRESQL";

	private static String dataSourceName = null;
	private static String serverType = null;
	private static String schemaName = null;
	private static DataSource lookupDataSource = null;

	// private static ServiceLocator serviceLocator =
	// ServiceLocator.getInstance();
	// private static QueryProcessorUtil crcUtil = QueryProcessorUtil
	// .getInstance();

	public static DataSourceLookupDAO getDataSourceLookupDAO()
			throws I2B2DAOException {
		if (serverType == null) {
			getLookupDataSourceFromPropertyFile();
		}
		if (serverType.equalsIgnoreCase(ORACLE)) {
			return new OracleDataSourceLookupDAO(lookupDataSource, schemaName);
		} else if (serverType.equalsIgnoreCase(SQLSERVER)) {
			return new OracleDataSourceLookupDAO(lookupDataSource, schemaName);
		} else if (serverType.equalsIgnoreCase(POSTGRESQL)) {
			return new OracleDataSourceLookupDAO(lookupDataSource, schemaName);
		} else {
			throw new I2B2DAOException(
					"DataSourceLookupDAOFactory.getDataSourceLookupDAO: serverType="
							+ serverType + " not valid");
		}
	}

	public static CRCQueueDAO getCRCQueueDAO() throws I2B2DAOException {
		if (serverType == null) {
			getLookupDataSourceFromPropertyFile();
		}
		CRCQueueDAO queueDAO = new CRCQueueDAO(lookupDataSource, schemaName);
		return queueDAO;
	}

	private static void getLookupDataSourceFromPropertyFile()
			throws I2B2DAOException {
		QueryProcessorUtil crcUtil = QueryProcessorUtil.getInstance();
		try {
			dataSourceName = crcUtil.getCRCDBLookupDataSource();
			serverType = crcUtil.getCRCDBLookupServerType();
			schemaName = crcUtil.getCRCDBLookupSchemaName();
			lookupDataSource = (DataSource) crcUtil
					.getDataSource("java:/CRCBootStrapDS");
		} catch (I2B2Exception i2b2Ex) {
			log.error(
					"DataSourceLookupDAOFactory.getLookupDataSourceFromPropertyFile"
							+ i2b2Ex.getMessage(), i2b2Ex);
			throw new I2B2DAOException(
					"DataSourceLookupDAOFactory.getLookupDataSourceFromPropertyFile"
							+ i2b2Ex.getMessage(), i2b2Ex);
		}
	}
}
