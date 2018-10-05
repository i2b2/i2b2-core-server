/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.pm.delegate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pm.ejb.DBInfoType;

public abstract class RequestHandler {
    protected final Log log = LogFactory.getLog(getClass());
    public abstract String execute() throws I2B2Exception;
    private DBInfoType dbInfo;
    
    
    public void setDbInfo(MessageHeaderType requestMessageHeader) throws I2B2Exception{
/*
    	DataSourceLookupHelper dsHelper = new DataSourceLookupHelper();
    	this.dbInfo =
    		dsHelper.matchDataSource(requestMessageHeader.getSecurity().getDomain(),  
    				requestMessageHeader.getProjectId(),
    				requestMessageHeader.getSecurity().getUsername());

    */
    	}     

		
	public DBInfoType getDbInfo() {
		return this.dbInfo;
	}
    
	public String getMetadata_dataSource() {
		return dbInfo.getDb_dataSource();
	}


	public String getMetadata_fullSchema() {
		return dbInfo.getDb_fullSchema();
	}

	public String getMetadata_serverType() {
		return dbInfo.getDb_serverType();
	}
}
