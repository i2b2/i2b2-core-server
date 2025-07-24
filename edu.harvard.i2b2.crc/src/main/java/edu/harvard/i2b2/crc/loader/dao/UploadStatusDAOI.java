/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.dao;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;

public interface UploadStatusDAOI {
	public int insertUploadStatus(UploadStatus uploadStatus);

	public void updateUploadStatus(UploadStatus uploadStatus);

	public List getAllUploadStatus();

	public void dropTempTable(String tempTable);

	public void calculateUploadStatus(int uploadId) throws I2B2Exception;

	public void deleteUploadData(int uploadId) throws I2B2Exception;

	public void insertUploadSetStatus(UploadSetStatus uploadSetStatus);

	public UploadStatus findById(int uploadStatusId) throws UniqueKeyException;

	public List<UploadSetStatus> getUploadSetStatusByLoadId(int uploadId);

	public List<UploadStatus> getUpoadStatusByUser(String userId);

	public void updateUploadSetStatus(UploadSetStatus uploadSetStatus);

	public UploadSetStatus getUploadSetStatus(int uploadId, int setId);
}
