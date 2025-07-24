/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.ejb;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;

public interface ILoaderStatusBean {

	public LoadDataListResponseType getLoadDataResponseByUserId(DataSourceLookup dataSourceLookup,String userId)
			throws I2B2Exception;

	public LoadDataResponseType getLoadDataResponseByUploadId(DataSourceLookup dataSourceLookup,int uploadId)
			throws I2B2Exception;

}
