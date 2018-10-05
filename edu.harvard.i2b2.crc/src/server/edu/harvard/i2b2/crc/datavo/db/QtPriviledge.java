/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.datavo.db;

public class QtPriviledge {

	String protectionLabelCd;
	String dataProtCd;
	String hivemgmtCd;
	String pluginId;

	public String getProtectionLabelCd() {
		return protectionLabelCd;
	}

	public void setProtectionLabelCd(String protectionLabelCd) {
		this.protectionLabelCd = protectionLabelCd;
	}

	public String getDataProtCd() {
		return dataProtCd;
	}

	public void setDataProtCd(String dataProtCd) {
		this.dataProtCd = dataProtCd;
	}

	public String getHivemgmtCd() {
		return hivemgmtCd;
	}

	public void setHivemgmtCd(String hivemgmtCd) {
		this.hivemgmtCd = hivemgmtCd;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

}
