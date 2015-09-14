package edu.harvard.i2b2.crc.dao;

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class DAOFactoryHelper {

	public static final String ORACLE = "ORACLE";
	public static final String SQLSERVER = "SQLSERVER";
	public static final String POSTGRESQL = "POSTGRESQL";
	DataSourceLookup dataSourceLookup = null;
	DataSource dataSource = null;
	DataSourceLookup originalDataSourceLookup = null;

	public DAOFactoryHelper(String hiveId, String projectId, String ownerId)
			throws I2B2DAOException {
		originalDataSourceLookup = new DataSourceLookup();
		originalDataSourceLookup.setProjectPath(projectId);
		originalDataSourceLookup.setOwnerId(ownerId);
		originalDataSourceLookup.setDomainId(hiveId);
		try {
			DataSourceLookupHelper dsHelper = new DataSourceLookupHelper();
			dataSourceLookup = dsHelper.matchDataSource(hiveId, projectId,
					ownerId);
		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2DAOException("DataSource lookup error"
					+ i2b2Ex.getMessage(), i2b2Ex);
		}
	}

	public DAOFactoryHelper(DataSourceLookup dataSourceLookup)
			throws I2B2DAOException {
		this(dataSourceLookup.getDomainId(), dataSourceLookup.getProjectPath(),
				dataSourceLookup.getOwnerId());
	}

	public DAOFactoryHelper(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		this.dataSource = dataSource;
	}

	public DataSourceLookup getDataSourceLookup() {
		return dataSourceLookup;
	}

	public DataSourceLookup getOriginalDataSource() {
		return originalDataSourceLookup;
	}

	public IDAOFactory getDAOFactory() throws I2B2DAOException {
		String dataSourceName = dataSourceLookup.getServerType();
		if (dataSourceName.equalsIgnoreCase(ORACLE)) {
			if (dataSource != null) {
				return new OracleDAOFactory(dataSourceLookup, dataSource);
			} else {
				return new OracleDAOFactory(dataSourceLookup,
						originalDataSourceLookup);
			}
		} else if (dataSourceName.equalsIgnoreCase(SQLSERVER)) {
			if (dataSource != null) {
				return new OracleDAOFactory(dataSourceLookup, dataSource);
			} else {
				return new OracleDAOFactory(dataSourceLookup,
						originalDataSourceLookup);
			}
		} else if (dataSourceName.equalsIgnoreCase(POSTGRESQL)) {
			if (dataSource != null) {
				return new OracleDAOFactory(dataSourceLookup, dataSource);
			} else {
				return new OracleDAOFactory(dataSourceLookup,
						originalDataSourceLookup);
			}
		} else {
			return null;
		}
	}

}
