/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
