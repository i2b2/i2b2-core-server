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
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadDataResponseType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;

public class UploadStatusRunner implements Runnable {

	private CallCRCUtil callCRCUtil = null;
	private String uploadId = null;
	private LoadDataResponseType loadDataResponseType = null;
	private String statusType = null;
	private String exceptionMsg = null;
	boolean exitFlag = false;
	private SecurityType securityType = null;
	private String projectId = null;

	public void setCRCUtil(CallCRCUtil callCRCUtil) {
		this.callCRCUtil = callCRCUtil;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

	public LoadDataResponseType getLodDataResponseType() {
		return loadDataResponseType;
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
				loadDataResponseType = callCRCUtil
						.callCRCUploadStatus(uploadId, securityType, projectId);

				// check if the response is completed or error
				statusType = loadDataResponseType.getLoadStatus();
				if (statusType.equalsIgnoreCase("COMPLETED")
						|| statusType.equalsIgnoreCase("DONE")
						|| statusType.equalsIgnoreCase("ERROR")) {

					exitFlag = true;
				}
				if (statusType.equalsIgnoreCase("ERROR")) {
					exceptionMsg = loadDataResponseType.getMessage();
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
