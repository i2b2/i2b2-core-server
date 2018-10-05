/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.datavo.db;

import java.util.Date;

public class QtAnalysisPlugin {

	String pluginId;
	String pluginName;
	String description;
	String version;
	String commandOptionCd;
	String parameterInfo;
	String parameterInfoXsd;
	String commandLine;
	String workingFolder;
	String pluginIcon;
	String statusId;
	String userId;
	String groupId;
	Date createDate;
	Date updateDate;

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getParameterInfo() {
		return parameterInfo;
	}

	public void setParameterInfo(String parameterInfo) {
		this.parameterInfo = parameterInfo;
	}

	public String getParameterInfoXsd() {
		return parameterInfoXsd;
	}

	public void setParameterInfoXsd(String parameterInfoXsd) {
		this.parameterInfoXsd = parameterInfoXsd;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public String getWorkingFolder() {
		return workingFolder;
	}

	public void setWorkingFolder(String workingFolder) {
		this.workingFolder = workingFolder;
	}

	public String getPluginIcon() {
		return pluginIcon;
	}

	public void setPluginIcon(String pluginIcon) {
		this.pluginIcon = pluginIcon;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getCommandOptionCd() {
		return commandOptionCd;
	}

	public void setCommandOptionCd(String commandOptionCd) {
		this.commandOptionCd = commandOptionCd;
	}

}
