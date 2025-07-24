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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.role.IPriviledgeDao;
import edu.harvard.i2b2.crc.datavo.db.QtPriviledge;

public class PriviledgeBean implements PriviledgeLocal {

	// public static ApplicationContext ac;
	// log
	private static Log log = LogFactory.getLog(PriviledgeBean.class);

	@Override
	public QtPriviledge checkPriviledge(IDAOFactory daoFactory,
			String protectionLabel, List<String> roles)
			throws MissingRoleException, I2B2Exception {
		IPriviledgeDao priviledgeDao = daoFactory.getSetFinderDAOFactory()
				.getPriviledgeDao();
		QtPriviledge priviledge = priviledgeDao
				.getPriviledgeByProtectionLabel(protectionLabel);
		String dataTrck = priviledge.getDataProtCd();
		String mgmtTrck = priviledge.getHivemgmtCd();
		// check if dataTrck in roles
		boolean dataTrckPassFlag = roles.contains(dataTrck);
		// check for mgmtTrck in roles
		boolean mgmtTrckPassFlag = roles.contains(mgmtTrck);
		if (dataTrckPassFlag && mgmtTrckPassFlag) {
			return priviledge;
		} else {
			String errorMsg = "This operation required privilege : ";

			String andMsg = " ";
			if (dataTrckPassFlag == false) {
				errorMsg += dataTrck;
				andMsg = " and ";
			}
			if (mgmtTrckPassFlag == false) {
				errorMsg += andMsg + mgmtTrck;
			}

			throw new MissingRoleException(errorMsg);
		}
	}

	@Override
	public QtPriviledge checkPluginPrivilege(IDAOFactory daoFactory,
			String pluginId, List<String> roles) throws MissingRoleException,
			I2B2Exception {
		QtPriviledge privilege = null;
		IPriviledgeDao priviledgeDao = daoFactory.getSetFinderDAOFactory()
				.getPriviledgeDao();
		List<QtPriviledge> priviledgeList = priviledgeDao
				.getPriviledgeByPluginId(pluginId);
		if (priviledgeList.size() < 1) {
			return privilege;
		}
		privilege = priviledgeList.get(0);
		String dataTrck = privilege.getDataProtCd();
		String mgmtTrck = privilege.getHivemgmtCd();
		// check if dataTrck in roles
		boolean dataTrckPassFlag = roles.contains(dataTrck);
		// check for mgmtTrck in roles
		boolean mgmtTrckPassFlag = roles.contains(mgmtTrck);
		if (dataTrckPassFlag && mgmtTrckPassFlag) {
			return privilege;
		} else {
			String errorMsg = "This operation required privilege : ";

			String andMsg = " ";
			if (dataTrckPassFlag == false) {
				errorMsg += dataTrck;
				andMsg = " and ";
			}
			if (mgmtTrckPassFlag == false) {
				errorMsg += andMsg + mgmtTrck;
			}

			throw new MissingRoleException(errorMsg);
		}
	}
}
