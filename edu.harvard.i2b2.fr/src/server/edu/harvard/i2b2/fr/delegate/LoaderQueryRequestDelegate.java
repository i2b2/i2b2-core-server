/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.fr.delegate;

import java.util.ArrayList;
import java.util.Iterator;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.fr.datavo.FRJAXBUtil;
import edu.harvard.i2b2.fr.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.fr.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.fr.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.fr.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.fr.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.fr.datavo.pm.ConfigureType;
import edu.harvard.i2b2.fr.datavo.pm.ParamType;
import edu.harvard.i2b2.fr.datavo.pm.ProjectType;
import edu.harvard.i2b2.fr.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.fr.delegate.pm.PMServiceDriver;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;


/**
 * PDO query request delegate class
 * $Id: LoaderQueryRequestDelegate.java,v 1.7 2008/10/21 17:56:30 mem61 Exp $
 * @author rkuttan
 */
public class LoaderQueryRequestDelegate extends RequestHandlerDelegate {
    /** log **/
    protected final Log log = LogFactory.getLog(getClass());
	//ConfigureType pmResponseUserInfo  = null;

    @Override
	public String handleRequest(String requestXml,RequestHandler requestHandler) throws I2B2Exception {
        String response = null;
        JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
       
        try {
    		log.debug("LoaderQueryRequestDelegate - RequestXML: " + requestXml);
        	
            JAXBElement<?> jaxbElement = jaxbUtil.unMashallFromString(requestXml);
            RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
            BodyType bodyType = requestMessageType.getMessageBody();

            if (bodyType == null) {
                log.error("null value in body type");
                throw new I2B2Exception("null value in body type");
            }

            log.debug("Calling PM Cell to vaidate user");
            //Call PM cell to validate user
            StatusType procStatus = null;
            ProjectType projectType = null;
            ConfigureType pmResponseUserInfo = null;
            try {
            	SecurityType securityType = null;
            	if (requestMessageType.getMessageHeader() != null ){
            		if (requestMessageType.getMessageHeader().getSecurity() != null) {
            			securityType = requestMessageType.getMessageHeader().getSecurity();
            		}
            	}
            	if (securityType == null) {
            		procStatus = new StatusType();
            		procStatus.setType("ERROR");
            		procStatus.setValue("Request message missing user/password");
            		response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            		return response;
            	}

            	pmResponseUserInfo = PMServiceDriver.checkValidUser(securityType);
            	//projectType = pmResponseUserInfo.getUser().getProject().get(0);
            	
				Iterator<?> it = pmResponseUserInfo.getUser().getProject().iterator();
				
				
				
				while (it.hasNext())
				{
					projectType = (ProjectType)it.next();
					if (projectType.getId().equals(requestMessageType.getMessageHeader().getProjectId())) {
				//		log.info(header.getProjectId());
				//		log.info(projectType.getId());
						break;	
					}

				}
            	
            	if (projectType == null) { 
            		procStatus = new StatusType();
            		procStatus.setType("ERROR");
            		procStatus.setValue("Invalid user/password for the given domain");
            		response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            		return response;
            	}
            	
            	
            	log.debug("project name from PM " + projectType.getName());
            	log.debug("project id from PM " + projectType.getId());
            	log.debug("Project role from PM " + projectType.getRole().get(0));
            	
		
            } catch (AxisFault e) {
            	log.error("AxisFault exception",e);            	
            	procStatus = new StatusType();
            	procStatus.setType("ERROR");
            	procStatus.setValue("Could not connect to server");
            	response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            	return response;
            } catch (I2B2Exception e) {
            	log.error("I2B2Exception exception",e);
            	procStatus = new StatusType();
            	procStatus.setType("ERROR");
            	procStatus.setValue("Message error connecting Project Management cell");
            	response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            	return response;
            } catch (JAXBUtilException e) {
            	log.error("JAXBUtil exception",e);
            	procStatus = new StatusType();
            	procStatus.setType("ERROR");
            	procStatus.setValue("Message error from Project Management cell");
            	response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            	return response;
            }
            
            
            JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
           
            BodyType responseBodyType = null;
            if (requestHandler instanceof SendfileRequestHandler) { 
            	String irodsStorageResource = null;
            	
				// check if user have right permission to access this request
            	log.debug("Number of roles:" + projectType.getRole().size());
            	
				if (projectType != null && projectType.getRole().size() > 0) {
					//for (String a : projectType.getRole())
					//	log.debug("Roles:" + a);
	            	
					if (isRoleValid(projectType) == false) {
						//!projectType.getRole().contains("MANAGER")  && !projectType.getRole().contains("EDITOR")) {
						// Not authorized
						procStatus = new StatusType();
						procStatus.setType("ERROR");
						procStatus
								.setValue("Authorization failure, need MANAGER or EDITOR role");
						response = I2B2MessageResponseFactory
								.buildResponseMessage(requestXml, procStatus,
										bodyType);
						return response;
					}
				} else {
					// Not authorized
					procStatus = new StatusType();
					procStatus.setType("ERROR");
					procStatus
							.setValue("Authorization failure, need MANAGER or EDITOR role");
					response = I2B2MessageResponseFactory.buildResponseMessage(
							requestXml, procStatus, bodyType);
					return response;
				}
            	
            	for (ParamType paramType : projectType.getParam()) { 
            		
            		if (paramType.getName().equalsIgnoreCase("SRBDefaultStorageResource")) { 
            			irodsStorageResource = paramType.getValue();
            			log.debug("param value for SRBDefaultStorageResource" + paramType.getValue());
            		}
            	}
            	((SendfileRequestHandler)requestHandler).setPmResponseUserInfo(pmResponseUserInfo);
            }

            if (requestHandler instanceof RecvfileRequestHandler) { 
            	String irodsStorageResource = null;
            	for (ParamType paramType : projectType.getParam()) { 
            		
            		if (paramType.getName().equalsIgnoreCase("SRBDefaultStorageResource")) { 
            			irodsStorageResource = paramType.getValue();
            			log.debug("param value for SRBDefaultStorageResource" + paramType.getValue());
            		}
            	}
            	((RecvfileRequestHandler)requestHandler).setPmResponseUserInfo(pmResponseUserInfo);
            }            
            
            responseBodyType = requestHandler.execute();
                        
            
            procStatus = new StatusType();
            procStatus.setType("DONE");
            procStatus.setValue("DONE");
            
            response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, responseBodyType,true);
            
        } catch (JAXBUtilException e) {
        	log.error("JAXBUtil exception",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(requestXml + "\n\n" + StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(null, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
        } catch (I2B2Exception e) {
        	log.error("I2B2Exception",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
        } catch (Throwable e) { 
        	log.error("Throwable",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
		}
        return response;
    }

    public boolean isRoleValid(ProjectType projectInfo){
    	 
 	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
 	   for(String param :roles) {
 		   if(param.equalsIgnoreCase("manager")) 
 			   return true;
 		   if(param.equalsIgnoreCase("editor")) 
 			   return true;
 	   }
 	   return false;
    }
}