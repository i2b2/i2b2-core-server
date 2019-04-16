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
package edu.harvard.i2b2.ontology.delegate;

import java.util.Iterator;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontology.datavo.pm.ConfigureType;
import edu.harvard.i2b2.ontology.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.pm.ws.PMResponseMessage;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;



public abstract class RequestHandler {
    protected static Log log = LogFactory.getLog(RequestHandler.class);
    public abstract String  execute() throws I2B2Exception;
    
    private DBInfoType dbInfo;
    
    public boolean isAdmin(MessageHeaderType header) {
		try {
			GetUserConfigurationType userConfigType = new GetUserConfigurationType();
			String response = PMServiceDriver.getRoles(userConfigType, header);		
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
    
    public ProjectType getRoleInfo(MessageHeaderType header) 
    {
    	ProjectType projectType = null;
    	

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
					// IF THERE IS NO MATCH (header.projectId is null) 
					//    this will return projectType == null.
					ConfigureType pmConfigure = msg.readUserInfo();
					Iterator it = pmConfigure.getUser().getProject().iterator();
					while (it.hasNext())
					{
		/* BUG				projectType = (ProjectType)it.next();
						log.debug("Matching PM response's project name [" + projectType.getName() + "] with the request  project name [" + header.getProjectId() + "]");
						if (projectType.getName().equals(header.getProjectId())) { 
							break;
						}
			*/			
						ProjectType project = (ProjectType)it.next();
						log.debug("Matching PM response's project  [" + project.getId() + "] with the request  project [" + header.getProjectId() + "]");
						if (project.getId().equals(header.getProjectId())) { 
							projectType = project;
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
