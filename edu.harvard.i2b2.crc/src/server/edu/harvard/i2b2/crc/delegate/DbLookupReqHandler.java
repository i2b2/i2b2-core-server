/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wayne Chan
 */
package edu.harvard.i2b2.crc.delegate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.loader.delegate.pm.PMResponseMessage;
import edu.harvard.i2b2.crc.loader.delegate.pm.PMServiceDriver;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Top level class to process the request. There will be separate request handler
 * class for each request type. The main processing of for the request will be
 * done inside execute function 
 * 
 */
public abstract class DbLookupReqHandler {
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Function to perform operation on the given request
	 * 
	 * @return response xml message
	 */
	public abstract String execute() throws I2B2Exception;
    
    public boolean isAdmin(MessageHeaderType header) {
		try {
			GetUserConfigurationType userConfigType = new GetUserConfigurationType();
			PMServiceDriver pmSrvDrvr = new PMServiceDriver();
			String response = pmSrvDrvr.getRoles(userConfigType, header);		
			log.debug(response);
			PMResponseMessage msg = new PMResponseMessage();
			StatusType procStatus = msg.processResult(response);
			if(procStatus.getType().equals("ERROR")) return false;
			ConfigureType pmConfigure = msg.readUserInfo();
			if (pmConfigure.getUser().isIsAdmin()) return true;
		} catch (AxisFault e) {
				log.error("Can't connect to PM service");
		} catch (I2B2Exception e) {
				log.error("Problem processing PM service address");
		} catch (Exception e) {
				log.error("General PM processing problem: "+ e.getMessage());
		}
		return false;
    }        
    
}
