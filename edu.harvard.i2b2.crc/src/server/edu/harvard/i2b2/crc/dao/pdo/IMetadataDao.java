package edu.harvard.i2b2.crc.dao.pdo;

import java.sql.SQLException;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface IMetadataDao {

	public Map  getMetadataForOptionalField(String tableName) throws I2B2DAOException;

}