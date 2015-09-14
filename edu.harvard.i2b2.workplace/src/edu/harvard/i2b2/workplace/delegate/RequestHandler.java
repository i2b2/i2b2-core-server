/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.workplace.delegate;

import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.workplace.datavo.pm.CellDataType;
import edu.harvard.i2b2.workplace.datavo.pm.ConfigureType;
import edu.harvard.i2b2.workplace.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.pm.ws.PMResponseMessage;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;
import edu.harvard.i2b2.workplace.ejb.DBInfoType;
import edu.harvard.i2b2.workplace.util.WorkplaceUtil;

public abstract class RequestHandler {
    protected final Log log = LogFactory.getLog(getClass());
    public abstract String execute() throws I2B2Exception;
    private DBInfoType dbInfo;
    private SecurityType securityType = null;
    
    public SecurityType getSecurityType() {
		return securityType;
	}


	public ProjectType getRoleInfo(MessageHeaderType header) 
    {
    	ProjectType projectType = null;
    	
		// Are we bypassing the PM cell?  Look in properties file.
		Boolean pmBypass = false;
		String pmBypassRole = null;
		String pmBypassProject = null;
		try {
			pmBypass = WorkplaceUtil.getInstance().isPmBypass();
			pmBypassRole = WorkplaceUtil.getInstance().getPmBypassRole();
			pmBypassProject = WorkplaceUtil.getInstance().getPmBypassProject();
			log.debug(pmBypass + pmBypassRole + pmBypassProject);
		} catch (I2B2Exception e1) {
			pmBypass = false;
			log.error(e1.getMessage());
		}
    	
		if(pmBypass == true){
			projectType = new ProjectType();
			projectType.getRole().add(pmBypassRole);
			projectType.setId(pmBypassProject);
		}
		else {
			try {
				GetUserConfigurationType userConfigType = new GetUserConfigurationType();

				PMResponseMessage msg = new PMResponseMessage();
				StatusType procStatus = null;	
				String response = PMServiceDriver.getRoles(userConfigType,header);		
				log.debug(response);
				procStatus = msg.processResult(response);
				if(procStatus.getType().equals("ERROR"))
					return null;
				// check that user has access to this project.
				ConfigureType pmConfigure = msg.readUserInfo();
				Iterator it = pmConfigure.getUser().getProject().iterator();
				
				//Set CRC Cell URL
				for (CellDataType cell : pmConfigure.getCellDatas().getCellData())
				{
					if (cell.getId().equals("CRC"))
					{
						WorkplaceUtil.getInstance().setCRCEndpointReference(cell.getUrl());
						break;
					}
					
				}
				
				//Set Security Type
				log.debug("Setting security type needed for CRC");
				securityType = new SecurityType();
				securityType.setDomain(pmConfigure.getUser().getDomain());
				securityType.setUsername(pmConfigure.getUser().getUserName());
				edu.harvard.i2b2.workplace.datavo.i2b2message.PasswordType ptype = new edu.harvard.i2b2.workplace.datavo.i2b2message.PasswordType();
				ptype.setIsToken(pmConfigure.getUser().getPassword().isIsToken());
				ptype.setTokenMsTimeout(pmConfigure.getUser().getPassword().getTokenMsTimeout());
				ptype.setValue(pmConfigure.getUser().getPassword().getValue());
				securityType.setPassword(ptype);
				
				while (it.hasNext())
				{
					projectType = (ProjectType)it.next();
					if (projectType.getId().equals(header.getProjectId())) {
				//		log.info(header.getProjectId());
				//		log.info(projectType.getId());
						break;	
					}

				}

				//	projectType = pmConfigure.getUser().getProject().get(0);
			} catch (AxisFault e) {
				log.error("Cant connect to PM service");
			} catch (I2B2Exception e) {
				log.error("Problem processing PM service address");
			} catch (Exception e) {
				log.error("General PM processing problem:  "+ e.getMessage());
			}
		}
		return projectType;
    }
    
    public void setDbInfo(MessageHeaderType requestMessageHeader) throws I2B2Exception{

    	DataSourceLookupHelper dsHelper = new DataSourceLookupHelper();
    	this.dbInfo =
    		dsHelper.matchDataSource(requestMessageHeader.getSecurity().getDomain(),  
    				requestMessageHeader.getProjectId(),
    				requestMessageHeader.getSecurity().getUsername());
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
