/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.pm.delegate;

import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pm.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.pm.ejb.DBInfoType;
import edu.harvard.i2b2.pm.util.PMUtil;

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
