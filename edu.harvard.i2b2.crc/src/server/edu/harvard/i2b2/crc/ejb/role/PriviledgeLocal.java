package edu.harvard.i2b2.crc.ejb.role;

import java.util.List;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.QtPriviledge;

public interface PriviledgeLocal {

	public QtPriviledge checkPriviledge(IDAOFactory daoFactory,
			String protectionLabel, List<String> roles)
			throws MissingRoleException, I2B2Exception;

	public QtPriviledge checkPluginPrivilege(IDAOFactory daoFactory,
			String pluginId, List<String> roles) throws MissingRoleException,
			I2B2Exception;

}