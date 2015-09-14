/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
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
    
    public ProjectType getRoleInfo(MessageHeaderType header) 
    {
    	ProjectType projectType = null;
    	
		// Are we bypassing the PM cell?  Look in properties file.
		Boolean pmBypass = false;
		String pmBypassRole = null;
		String pmBypassProject = null;
		try {
			pmBypass = OntologyUtil.getInstance().isPmBypass();
			pmBypassRole = OntologyUtil.getInstance().getPmBypassRole();
			pmBypassProject = OntologyUtil.getInstance().getPmBypassProject();
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
