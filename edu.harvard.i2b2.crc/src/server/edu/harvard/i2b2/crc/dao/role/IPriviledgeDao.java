package edu.harvard.i2b2.crc.dao.role;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.db.QtPriviledge;

public interface IPriviledgeDao {
	public QtPriviledge getPriviledgeByProtectionLabel(String protectionLabel)
			throws I2B2DAOException;

	public List<QtPriviledge> getPriviledgeByPluginId(String pluginId)
			throws I2B2DAOException;
}