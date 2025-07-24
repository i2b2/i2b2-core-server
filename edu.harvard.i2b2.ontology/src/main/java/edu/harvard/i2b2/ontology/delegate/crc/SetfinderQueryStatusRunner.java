/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.ontology.delegate.crc;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;

public class SetfinderQueryStatusRunner implements Runnable {

	private CallCRCUtil callCRCUtil = null;
	private String queryInstanceId = null;
	private SecurityType securityType = null;
	private String projectId = null;
	public SecurityType getSecurityType() {
		return securityType;
	}

	public void setSecurityType(SecurityType securityType) {
		this.securityType = securityType;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	private ResultResponseType resultResponseType = null;
	private QueryStatusTypeType queryStatusType = null;
	private String statusType = null;
	private String exceptionMsg = null;
	boolean exitFlag = false;

	public void setCRCUtil(CallCRCUtil callCRCUtil) {
		this.callCRCUtil = callCRCUtil;
	}

	public void setQueryInstanceId(String queryInstanceId) {
		this.queryInstanceId = queryInstanceId;
	}

	public ResultResponseType getQueryInstanceStatusResponseType() {
		return resultResponseType;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public boolean isNotDone() {
		if (exitFlag == false) {
			return true;
		} else {
			return false;
		}

	}

	public void run() {
		
		while (exitFlag == false) {
			try {

				// send and get message
				resultResponseType = callCRCUtil
						.callCRCQueryStatus(queryInstanceId, securityType, projectId);
	
				// check if the response is completed or error
				queryStatusType = resultResponseType.getQueryResultInstance().get(0).getQueryStatusType();
				statusType = queryStatusType.getName();
				if (statusType.equalsIgnoreCase("COMPLETED")
						|| statusType.equalsIgnoreCase("DONE")
						|| statusType.equalsIgnoreCase("ERROR")) {

					exitFlag = true;
				}
				if (statusType.equalsIgnoreCase("ERROR")) {
					exceptionMsg = resultResponseType.getQueryResultInstance().get(0).getMessage().getContent().get(0).toString();
				}
			} catch (I2B2Exception i2b2Ex) {
				exceptionMsg = StackTraceUtil.getStackTrace(i2b2Ex);
				exitFlag = true;
			} catch (Throwable t) {
				exceptionMsg = StackTraceUtil.getStackTrace(t);
				exitFlag = true;
			}
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				exitFlag = true;
				e.printStackTrace();
			}
		}
	}
}
