/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
