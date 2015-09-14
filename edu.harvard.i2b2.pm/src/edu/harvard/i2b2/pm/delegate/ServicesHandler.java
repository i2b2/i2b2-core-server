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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.pm.services.HiveParamData;
import edu.harvard.i2b2.pm.services.SessionData;
import edu.harvard.i2b2.pm.services.UserParamData;
import edu.harvard.i2b2.pm.util.*;
//import edu.harvard.i2b2.pm.util.SessionKey;
import edu.harvard.i2b2.pm.ws.MessageFactory;
import edu.harvard.i2b2.pm.ws.ServicesMessage;
import edu.harvard.i2b2.pm.dao.PMDbDao;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pm.datavo.pm.PasswordType;
import edu.harvard.i2b2.pm.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.pm.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.pm.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.pm.datavo.pm.ApprovalType;
import edu.harvard.i2b2.pm.datavo.pm.ApprovalsType;
import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.CellDatasType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.datavo.pm.ConfiguresType;
import edu.harvard.i2b2.pm.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDataType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDatasType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ParamsType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectRequestType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectRequestsType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectsType;
import edu.harvard.i2b2.pm.datavo.pm.RoleType;
import edu.harvard.i2b2.pm.datavo.pm.RolesType;
import edu.harvard.i2b2.pm.datavo.pm.UserType;
import edu.harvard.i2b2.pm.datavo.pm.UsersType;
import edu.harvard.i2b2.pm.ejb.DBInfoType;


public class ServicesHandler extends RequestHandler {
	private ProjectType projectInfo = null;
	private ServicesMessage getServicesMsg = null;

	public ServicesHandler(ServicesMessage servicesMsg) throws I2B2Exception{
		log.debug("Setting the servicesMsg");	

		getServicesMsg = servicesMsg;
		//setDbInfo(servicesMsg.getRequestMessageType().getMessageHeader());
	}

	private void saveLoginAttempt(PMDbDao pmDb, String username, String attempt)
	{
		// Add new timeout to it
		pmDb.setLoginAttempt(username, attempt );

	}


	private UserType validateSuppliedPassword (String username, String password, Hashtable param) throws Exception
	{
		PMDbDao pmDb = new PMDbDao();

		if (pmDb.verifyNotLockedOut(username))
			throw new Exception ("To many invalid attempts, user locked out");

		//if (method.equalsIgnoreCase("NTLM"))
		if ((param.get("authentication_method") != null) && (!param.get("authentication_method").equals("")))
		{
			//String SQL_QUERY ="from UserData where oid='" + username + "'";
			List response = null;	
			try {
				response = pmDb.getUser(username, null);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for " + param.get("authentication_method"));
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for " + param.get("authentication_method"));
			}

			Iterator it = response.iterator();
			UserType user = null;
			while (it.hasNext())
			{
				user = (UserType)it.next();
			}

			if (user == null)
			{
				log.debug("Did not find user: " + username);
				saveLoginAttempt(pmDb, username, "NONEXIST");

				throw new Exception ("Username does not exist");
			}

			// Handle all internal classnames.  Also for backward compatibility need to call it NTLM.
			String classname = "";
			if (param.get("authentication_method").equals("NTLM"))
				classname = "edu.harvard.i2b2.pm.util.SecurityAuthenticationNTLM";
			else if (param.get("authentication_method").equals("LDAP"))
				classname = "edu.harvard.i2b2.pm.util.SecurityAuthenticationLDAP";

			ClassLoader classLoader = ServicesHandler.class.getClassLoader();

			try {
				Class securityClass = classLoader.loadClass(classname);


				SecurityAuthentication security =  (SecurityAuthentication) securityClass.newInstance();

				security.validateUser(username, password, param);

			} catch (ClassNotFoundException e) {
				log.equals("Did not find class: " + e.getMessage());
				throw new Exception ("Error loading class: " + e.getMessage());
			}

			return user;

		} else
		{
			//Check to see if user has authentication set


			List response = null;

			try {
				response = pmDb.getUser(username, null, null, true); //PMUtil.getInstance().getHashedPassword(password));
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data");
			}

			Iterator it = response.iterator();
			UserType user = null;
			while (it.hasNext())

			{
				user = (UserType)it.next();

				//Check the password
				if (user.getPassword().getValue().startsWith("@"))
				{
					if	(!(user.getPassword().getValue().substring(1)).equals(password))
					{
						saveLoginAttempt(pmDb, username, "BADPASSWORD");
						throw new Exception ("Current password is incorrect");
					}
				}				
				else if (!user.getPassword().getValue().equals(PMUtil.getInstance().getHashedPassword(password)))
				{
					saveLoginAttempt(pmDb, username, "BADPASSWORD");
					throw new Exception ("Current password is incorrect");

				}
			}
			if (user == null)
			{
				saveLoginAttempt(pmDb, username, "NONEXIST");

				log .debug("Did not find user: " + username + " with password: " + PMUtil.getInstance().getHashedPassword(password) );
				throw new Exception ("Username does not exist");
			}

			//passwordManagerService.validateSuppliedPassword(user, rmt.getPassword());
			return user;
		}
	}

	private boolean verifySession(PMDbDao pmDb, int timeout, String sessionId, String userId) throws Exception
	{

		List response = pmDb.getSession(userId, sessionId);


		//SessionKey k=SessionKey.Decrypt(sessionKey);
		Date now=new Date();

		Iterator it = response.iterator();
		SessionData session = null;
		while (it.hasNext())
		{
			log.debug("Found: " + response.size());
			session = (SessionData)it.next();
		}

		if (session == null)
			return false;

		//check if the session is still valid	
		log.debug("checking date");
		log.debug("Now Time: "+ now.toString());
		log.debug("Current session: "+ session.getExpiredDate().toString());
		if(now.after(session.getExpiredDate()))
		{
			log.debug("Session Expired");
			return false;
		}
		log.debug("date ok");

		// Add new timeout to it
		pmDb.updateSession(userId, sessionId, timeout);
		return true;
	}

	public String execute() throws I2B2Exception{

		PMDbDao pmDb = new PMDbDao();

		log.debug("I am in the RequestHandler");

		ConfigureType cType = new ConfigureType();
		UserType uType = new UserType();
		CellDatasType aType = new CellDatasType();

		ResponseMessageType responseMessageType = null;

		try {
			String domainId = null;
			SecurityType rmt = getServicesMsg.getRequestMessageType().getMessageHeader().getSecurity();
			String project = getServicesMsg.getRequestMessageType().getMessageHeader().getProjectId();
			log.debug("My username: " + rmt.getUsername());


			String SQL_QUERY = null;

			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();


			//Get Enviornment Data
			List response = null;	
			try {
				response = pmDb.getEnvironment(null);
			} catch (I2B2DAOException e1) {
				e1.printStackTrace();
				throw new Exception ( "Database error in getting environment data");
			} catch (I2B2Exception e1) {
				e1.printStackTrace();
				throw new Exception ("Database error in getting environment data");
			}
			log.debug("Start parsing environment results of: " + response);

			Iterator it = response.iterator();
			while (it.hasNext())
			{
				cType =(ConfigureType)it.next();
				//Make sure domain is set correctly
				if (!cType.getDomainName().equalsIgnoreCase(rmt.getDomain().toUpperCase()))
					throw new Exception (rmt.getDomain() + " is not associated with this domain " + cType.getDomainName() + ". Please check the i2b2workbench.properties file.");

				//Get wiki and environment data
				log.debug("Helpurl: " + cType.getHelpURL());
				log.debug("Environment: " + cType.getEnvironment());
				domainId = cType.getDomainName();;
			}
			if (cType.getEnvironment() == null)
			{
				throw new Exception ("Environment group has not been setup");
			}

			//Determine authentication method
			log.debug("Get authentication method by using domain: " + domainId);
			//String method = "", domainController= "", domain= "";
			String method = null;
			for( it=pmDb.getEnvironmentData(domainId).iterator();it.hasNext();){
				HiveParamData hivedata =(HiveParamData)it.next();

				if (hivedata.getName().equalsIgnoreCase("authentication_method"))
					method  = hivedata.getValue();


			}

			Hashtable params = new Hashtable();
			//First get all the params for the user params
			UserType userType = new UserType();
			userType.setUserName(rmt.getUsername());

			for( it=pmDb.getAllParam(userType,null,null).iterator();it.hasNext();){
				UserParamData userdata =(UserParamData)it.next();			
				params.put(userdata.getName(),  userdata.getValue());
				
				if (userdata.getName().equalsIgnoreCase("authentication_method"))
					method  = userdata.getValue();

			}
			//Get any remaining params from the environment
			for( it=pmDb.getEnvironmentData(domainId).iterator();it.hasNext();){
				HiveParamData hivedata =(HiveParamData)it.next();
				params.put(hivedata.getName(),  hivedata.getValue());

			}	

			String password = rmt.getPassword().getValue();


			//If password begins with "SessionKey:" its a session key and decrypt it and validate it
			if (password.startsWith("SessionKey:"))
			{
				String sessionKey=password.replace("SessionKey:", "");
				log.debug("Encrypted Session key: "+sessionKey+" passed in for validation.");
				if (rmt.getPassword().getTokenMsTimeout() == null)
					rmt.getPassword().setTokenMsTimeout(1800000);
				if (verifySession(pmDb, rmt.getPassword().getTokenMsTimeout(), sessionKey, rmt.getUsername()) == false)
					throw new Exception ("Session invalid");

				//get the user from the service

				UserType user = null;
				for( it=pmDb.getUser(rmt.getUsername(), null).iterator();it.hasNext();){
					user=(UserType)it.next();
				}	

				//user = userManagerService.getUserByUserName(k.getUsername());
				if (user == null)
				{
					log.debug("Did not find user: " + rmt.getUsername());
					throw new Exception ("Username does not exist");
				}
				//check the password


				//everything is good so just return the same session key and the other info
				uType.setFullName(user.getFullName());
				uType.setUserName(rmt.getUsername());
				uType.setDomain(rmt.getDomain());
				PasswordType passType = new PasswordType();
				passType.setIsToken(true);
				passType.setValue(password);
				passType.setTokenMsTimeout(rmt.getPassword().getTokenMsTimeout());
				uType.setPassword(passType); //"SessionKey:"+sessionKey); //return the key instead of password
				//uType.setKey(rmt.getPassword()); //return password so client can reaunthenticate later
			}
			//otherwise check username and password and generate a sessionkey for the password
			else	
			{

				//get the user from the service
				try {
					log.debug("Validating user: " + rmt.getUsername());

					UserType user = validateSuppliedPassword( rmt.getUsername(), rmt.getPassword().getValue(), params);
					uType.setFullName(user.getFullName());
					uType.setIsAdmin(user.isIsAdmin());
					saveLoginAttempt(pmDb, rmt.getUsername(), "SUCCESS");

				} catch (Exception e)
				{
					throw new Exception (e.getMessage());
				}

				//if password was good then set info and generate a new session key
				uType.setUserName(rmt.getUsername());
				uType.setDomain(rmt.getDomain());

				//SessionKey newKey;

				//build the date for the expiration of the key
				//Calendar cal=Calendar.getInstance();
				//cal.add(Calendar.MILLISECOND, rmt.getPassword().getTokenMsTimeout()); //timeout in minutes from now
				//cal.getTime();

				//create the key in unecrypted form
				//newKey=new SessionKey(rmt.getUsername(),PMUtil.getInstance().getHashedPassword(rmt.getPassword().getValue()), rmt.getDomain(), cal.getTime());
				//log.debug("Made new key for: "+rmt.getUsername()+" expires on: "+cal.getTime().toString());
				//encrypt the key
				//				String encryptedKey=SessionKey.Encrypt(newKey);

				log.debug("Creating new session key");
				String encryptedKey = PMUtil.getInstance().generateMessageId();
				log.debug("Saving new session key: " + encryptedKey);
				int timeout = 1800000;
				if (rmt.getPassword().getTokenMsTimeout() != null)
					timeout = rmt.getPassword().getTokenMsTimeout();
				int result = pmDb.setSession(rmt.getUsername(), encryptedKey, timeout);

				if (result < 1)
					throw new Exception("Failed to save session");
				//return the key in the password slot
				PasswordType ptype = new PasswordType();
				ptype.setValue("SessionKey:" + encryptedKey);
				ptype.setIsToken(true);
				ptype.setTokenMsTimeout(timeout);
				//ptype.sett
				uType.setPassword(ptype); //password slot contains password or key
				//uType.setKey(rmt.getPassword().getValue()); //"key" slot contains original password passed back so client can re-authenticate later
				//for testing to undo all the previous security stuff

			}

			log.debug("Working on GetUserConfigure");
			GetUserConfigurationType userConfigurationType  = (GetUserConfigurationType) helper.getObjectByClass(getServicesMsg.getRequestType().getAny(),
					GetUserConfigurationType.class);

			if (userConfigurationType != null)
			{
				PMUtil.getInstance().convertToUppercaseStrings(userConfigurationType.getDataNeeded());
				PMUtil.getInstance().convertToUppercaseStrings(userConfigurationType.getProject());
				return runGetUserConfiguration(pmDb, userConfigurationType, rmt.getUsername(), rmt.getDomain(), cType, uType);
			}


			log.debug("Working on Rest of services: 1");
			BodyType bodyType = getServicesMsg.getRequestType();
			Object obj = bodyType.getAny().get(0);
			log.debug("Working on Rest of services: " + obj);
			if (obj instanceof JAXBElement) {
				String value = null;
				String name = null;
				name  = ((JAXBElement) obj).getName().getLocalPart();
				log.debug("Element name is: " + name );
				if (name.equals("set_user"))
					return runSetUser(pmDb, project, rmt.getUsername(), (UserType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_user"))
					return runGetAllUser(pmDb, project, rmt.getUsername() );
				else if (name.equals("set_project"))
					return runSetProject(pmDb, project, rmt.getUsername(), (ProjectType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_project"))
					return runGetAllProject(pmDb, project, rmt.getUsername() );
				else if (name.equals("set_project_request"))
					return runSetProjectRequest(pmDb, project, rmt.getUsername(), (ProjectRequestType) ((JAXBElement) obj).getValue() );

				else if (name.equals("set_approval"))
					return runSetApproval(pmDb, project, rmt.getUsername(), (ApprovalType) ((JAXBElement) obj).getValue() );
				else if (name.equals("delete_approval"))
					return runDeleteApproval(pmDb, project, rmt.getUsername(), (ApprovalType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_approval"))
					return runGetApproval(pmDb, project, rmt.getUsername(), (ApprovalType) ((JAXBElement) obj).getValue() );				
				else if (name.equals("get_all_approval"))
					return runGetAllApproval(pmDb, project, rmt.getUsername() );

				else if (name.equals("set_cell"))
					return runSetCell(pmDb, project, rmt.getUsername(), (CellDataType) ((JAXBElement) obj).getValue() );
				else if (name.equals("delete_cell"))
					return runDeleteCell(pmDb, project, rmt.getUsername(), (CellDataType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_cell"))
					return runGetCell(pmDb, project, rmt.getUsername(), (CellDataType) ((JAXBElement) obj).getValue() );				
				else if (name.equals("get_all_cell"))
					return runGetAllCell(pmDb, project, rmt.getUsername() );				
				else if (name.equals("set_global"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (GlobalDataType) ((JAXBElement) obj).getValue() );
				else if (name.equals("set_project_param"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (ProjectType) ((JAXBElement) obj).getValue() );
				else if (name.equals("set_project_user_param"))
					return runSetParam(pmDb, ((ProjectType) ((JAXBElement) obj).getValue()).getId(), name, rmt.getUsername(), (ProjectType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_project_user_param"))
					return runGetAllParam(pmDb, project,  rmt.getUsername(), (ProjectType) ((JAXBElement) obj).getValue() );
				else if (name.equals("set_user_param"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (UserType) ((JAXBElement) obj).getValue() );
				else if (name.equals("set_cell_param"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (CellDataType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_cell_param"))
					return runGetAllParam(pmDb, project, rmt.getUsername(),  (CellDataType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_user_param"))
					return runGetAllParam(pmDb, project, rmt.getUsername(),  (UserType) ((JAXBElement) obj).getValue() );
				else if (name.equals("set_hive_param"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (ConfigureType) ((JAXBElement) obj).getValue() );
				else if (name.equals("set_role"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (RoleType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_role"))
					return runGetAllParam(pmDb, project, rmt.getUsername(),  (RoleType) ((JAXBElement) obj).getValue() );
				else if (name.equals("delete_role"))
					return runDeleteParam(pmDb, project, rmt.getUsername(),  (RoleType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_role"))
					return runGetParam(pmDb, project, rmt.getUsername(),  (RoleType) ((JAXBElement) obj).getValue()  );
				else if (name.equals("set_hive"))
					return runSetParam(pmDb, project, name, rmt.getUsername(), (ConfigureType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_all_hive"))
					return runGetAllParam(pmDb, project, rmt.getUsername(),  new ConfigureType() );
				else if (name.equals("delete_hive"))
					return runDeleteParam(pmDb, project, rmt.getUsername(),  (ConfigureType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_hive"))
					return runGetParam(pmDb, project, rmt.getUsername(),  (ConfigureType) ((JAXBElement) obj).getValue()  );
				else if (name.equals("delete_project"))
					return runDeleteProject(pmDb, rmt.getUsername(),  (ProjectType) ((JAXBElement) obj).getValue() );
				else if (name.equals("get_project"))
					return runGetProject(pmDb, rmt.getUsername(), (ProjectType) ((JAXBElement) obj).getValue()  );


				log.debug("working on value");
				value  = ((String) ((JAXBElement) obj).getValue()).trim();
				log.debug("Element is single and is: " + value);
				if (name.equals("delete_user"))
					return runDeleteUser(pmDb, project, rmt.getUsername(), value );
				else if (name.equals("set_password") && method != null)
					throw new Exception(method + " authencation method is used, use that provider to change the password.");
				else if (name.equals("set_password"))
					return runSetPassword(pmDb, rmt.getUsername(), value );
				else if (name.equals("get_user"))
					return runGetUser(pmDb, project, rmt.getUsername(), value );
				else if (name.equals("get_global"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					GlobalDataType global = new GlobalDataType();
					global.getParam().add(param);
					return runGetParam(pmDb, project, rmt.getUsername(), global  );				
				}
				else if (name.equals("get_all_global"))
				{
					GlobalDataType global = new GlobalDataType ();
					global.setProjectPath(value);
					return runGetAllParam(pmDb, project, rmt.getUsername(), global );
				}
				else if (name.equals("delete_global"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					GlobalDataType global = new GlobalDataType();
					global.getParam().add(param);
					return runDeleteParam(pmDb, project, rmt.getUsername(), global);
				}
				else if (name.equals("delete_project_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					ProjectType global = new ProjectType();
					global.getParam().add(param);
					return runDeleteParam(pmDb, project, rmt.getUsername(), global);
				}
				else if (name.equals("delete_project_user_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					ProjectType global = new ProjectType();
					global.setUserName(rmt.getUsername());
					global.getParam().add(param);
					return runDeleteParam(pmDb, project, rmt.getUsername(), global);
				}				
				else if (name.equals("delete_cell_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					CellDataType global = new CellDataType();
					global.getParam().add(param);
					return runDeleteParam(pmDb, project, rmt.getUsername(), global);
				}					
				else if (name.equals("delete_hive_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					ConfigureType global = new ConfigureType();
					global.getParam().add(param);
					return runDeleteParam(pmDb, project, rmt.getUsername(), global);
				}					
				else if (name.equals("delete_user_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					UserType global = new UserType();
					global.setUserName(rmt.getUsername());
					global.getParam().add(param);
					return runDeleteParam(pmDb, project, rmt.getUsername(), global);
				}					

				else if (name.equals("get_all_hive_param"))
				{
					ConfigureType lcType = new ConfigureType();
					lcType.setDomainId(value);
					return runGetAllParam(pmDb, project, rmt.getUsername(),lcType);//(CellDataType) ((JAXBElement) obj).getValue() );
				}
				else if (name.equals("get_all_project_param"))
				{
					ProjectType pType = new ProjectType();
					pType.setId(value);
					return runGetAllParam(pmDb, project, rmt.getUsername(), pType );
				}
				/*				else if (name.equals("get_all_user_param"))
				{
					UserType pType = new UserType();
					pType.setUserName(value);
					return runGetAllParam(pmDb, project, rmt.getUsername(), pType );
				}
				 */
				else if (name.equals("get_project_user_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					ProjectType global = new ProjectType();
					global.setUserName("USER");
					global.getParam().add(param);

					return runGetParam(pmDb, project, rmt.getUsername(), global  );

					//					return runGetParam(pmDb, project, rmt.getUsername(), (ProjectType) ((JAXBElement) obj).getValue() );
				}
				else if (name.equals("get_project_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					ProjectType global = new ProjectType();
					global.getParam().add(param);

					return runGetParam(pmDb, project, rmt.getUsername(), global  );
				}
				else if (name.equals("get_cell_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					CellDataType global = new CellDataType();
					global.getParam().add(param);

					return runGetParam(pmDb, project, rmt.getUsername(), global  );
				}
				else if (name.equals("get_user_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					UserType global = new UserType();
					global.getParam().add(param);

					return runGetParam(pmDb, project, rmt.getUsername(), global  );
				}
				else if (name.equals("get_hive_param"))
				{
					ParamType param = new ParamType();
					param.setId(Integer.valueOf(value));
					ConfigureType global = new ConfigureType();
					global.getParam().add(param);

					return runGetParam(pmDb, project, rmt.getUsername(), global  );
				}
				else if (name.equals("get_all_project_request"))
				{
					return runGetAllProjectRequest(pmDb, project, rmt.getUsername()  );
				}				
				else if (name.equals("get_project_request"))
				{
					log.debug("Got this:" + value);
					return null;
				}
			} 		
		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;		
	}



	//All Param process
	private String runDeleteParam(PMDbDao pmDb, String project, String caller,
			Object utype) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = -1;
			result = pmDb.deleteParam(utype, project, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runSetParam(PMDbDao pmDb, String project, String name, String caller,
			Object utype) {
		ResponseMessageType responseMessageType = null;

		try {

			//String SQL_QUERY ="from UserData where oid='" + username + "'";

			log.debug("Start of setParam");
			int result = -1;
			result = pmDb.setParam(utype,project, name, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}




	private String runGetAllProjectRequest(PMDbDao pmDb, String project, String caller) {
		ResponseMessageType responseMessageType = null;

		try {


			List response = null;	
			try {
				response = pmDb.getAllProjectRequest(project, caller);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			ProjectRequestsType users = new ProjectRequestsType();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				ProjectRequestType user = (ProjectRequestType)it.next();
				users.getProjectRequest().add(user);
			}
			//everything is good so just return the same session key and the other info

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runSetProjectRequest(PMDbDao pmDb, String project, String caller,
			ProjectRequestType value) {
		ResponseMessageType responseMessageType = null;

		try {

			//String SQL_QUERY ="from UserData where oid='" + username + "'";

			log.debug("Start of setProjectRequest");

			String  result = "";


			List response = null;	
			try {
				response = pmDb.setProjectRequest(value,project, caller);
			} catch (I2B2DAOException e1) {
				e1.printStackTrace();
				throw new Exception ( "Database error in setting Projet Request: " + e1.getMessage());
			} catch (I2B2Exception e1) {
				e1.printStackTrace();
				throw new Exception ("Database error in setting Projet Request: " + e1.getMessage());
			}

			Iterator it = response.iterator();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				ProjectRequestType user = (ProjectRequestType)it.next();
				result = user.getId();
				log.debug("added ID: " + user.getId());
			}

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result);
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runGetAllParam(PMDbDao pmDb, String project, String caller, Object utype) {
		ResponseMessageType responseMessageType = null;

		//ParamsType users = new ParamsType();;
		Object users = null;
		try {


			List response = null;	
			try {
				int result = -1;
				//response = pmDb.getAllParam(utype,project, caller);

				//Iterator it = response.iterator();
				// users = new Para();


				if (utype instanceof RoleType)
				{
					response = pmDb.getAllParam(utype,project, caller);

					log.debug("Records returned: " + response.size());
					Iterator it = response.iterator();
					users = new RolesType();
					//ParamsType user = null;


					while (it.hasNext())
					{
						RoleType user = (RoleType)it.next();
						((RolesType) users).getRole().add(user);
					}					

				} else if ((utype instanceof ConfigureType) &&
						((((ConfigureType) utype).getDomainId() == null)))
				{
					response = pmDb.getAllParam(utype,project, caller);

					log.debug("Records returned: " + response.size());
					Iterator it = response.iterator();
					users = new ConfiguresType();
					//ParamsType user = null;
					while (it.hasNext())
					{
						ConfigureType user = (ConfigureType)it.next();
						((ConfiguresType) users).getHive().add(user);
					}
				} else  if (utype instanceof UserType)
				{
					response = pmDb.getAllParam(utype,project, caller);

					log.debug("Records returned: " + response.size());
					Iterator it = response.iterator();

					users = new UsersType();

					UserType userType = new UserType();

					while (it.hasNext())
					{
						UserParamData user = (UserParamData)it.next();

						if (((UserType)userType).getUserName() != null && !user.getUser().equals(((UserType)userType).getUserName()))
						{
							log.debug("adding user: " + ((UserType)userType).getUserName());
							((UsersType)users).getUser().add(userType);
							userType = new UserType();
						}

						ParamType param = new ParamType();
						param.setName(user.getName());
						param.setId(user.getId());
						param.setDatatype(user.getDatatype());
						param.setValue(user.getValue());
						((UserType)userType).setUserName(user.getUser());
						((UserType)userType).getParam().add(param);
						log.debug("adding param: " + param.getName());
					}
					((UsersType)users).getUser().add(userType);
					//	}
				} else 
				{
					response = pmDb.getAllParam(utype,project, caller);

					log.debug("Records returned: " + response.size());
					Iterator it = response.iterator();
					users = new ParamsType();
					//ParamsType user = null;


					while (it.hasNext())
					{
						ParamType user = (ParamType)it.next();
						((ParamsType) users).getParam().add(user);
						//((ParamType) users)..getRole().add(user);
					}
				}

			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}


			//everything is good so just return the same session key and the other info

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runGetParam(PMDbDao pmDb, String project, String caller,
			Object utype) {
		ResponseMessageType responseMessageType = null;

		Object users = null;

		try {

			log.debug("In GetGlobal");
			List response = null;	
			try {
				int result = -1;
				if (utype instanceof RoleType)
				{
					response = pmDb.getParam(utype, true);

					Iterator it = response.iterator();
					//ParamsType user = null;
					users = new  RolesType();
					while (it.hasNext())
					{
						RoleType user = (RoleType)it.next();
						((RolesType) users).getRole().add(user);
					}
				} else if (utype instanceof ConfigureType)
				{
					response = pmDb.getParam(utype, true);

					Iterator it = response.iterator();
					//ParamsType user = null;
					while (it.hasNext())
						users = (ConfigureType)it.next();
				} else if (utype instanceof GlobalDataType)
				{
					response = pmDb.getParam(utype, true);

					Iterator it = response.iterator();
					//ParamsType user = null;
					while (it.hasNext())
						users = (GlobalDataType)it.next();
				} else {
					response = pmDb.getParam(utype, true);

					Iterator it = response.iterator();
					//ParamsType user = null;
					while (it.hasNext())
						users = (ParamType)it.next();

				}
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}



			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}


	//All Users process
	private String runDeleteUser(PMDbDao pmDb, String project, String caller,
			String user) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = pmDb.deleteUser(user, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}


	private String runSetPassword(PMDbDao pmDb,  String caller, String password) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = pmDb.setPassword(PMUtil.getInstance().getHashedPassword(password), caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runSetUser(PMDbDao pmDb, String project, String caller,
			UserType utype) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = pmDb.setUser(utype, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}


	private String runGetAllUser(PMDbDao pmDb, String project, String caller) {
		ResponseMessageType responseMessageType = null;

		try {


			List response = null;	
			try {
				response = pmDb.getAllUser(project, caller);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			UsersType users = new UsersType();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				UserType user = (UserType)it.next();
				users.getUser().add(user);
				log.debug("added: " + user.getUserName());
			}
			//everything is good so just return the same session key and the other info

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runGetUser(PMDbDao pmDb, String project, String caller,
			String username) {
		ResponseMessageType responseMessageType = null;

		try {


			List response = null;	
			try {
				response = pmDb.getUser(username, caller);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			UserType user = null;
			while (it.hasNext())
				user = (UserType)it.next();

			//user = userManagerService.getUserByUserName(k.getUsername());
			if (user == null)
				throw new Exception ("Username does not exist");


			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,user);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	//All Cell process
	private String runDeleteCell(PMDbDao pmDb, String project, String caller, CellDataType utype) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = pmDb.deleteCell(utype.getId(), utype.getProjectPath(), caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runSetCell(PMDbDao pmDb, String project, String caller,
			CellDataType utype) {
		ResponseMessageType responseMessageType = null;

		try {

			//String SQL_QUERY ="from UserData where oid='" + username + "'";

			int result = pmDb.setCell(utype, project,  caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}


	private String runGetAllCell(PMDbDao pmDb, String project, String caller) {
		ResponseMessageType responseMessageType = null;

		try {


			List response = null;	
			try {
				response = pmDb.getAllCell(project, caller);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			CellDatasType users = new CellDatasType();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				CellDataType user = (CellDataType)it.next();
				users.getCellData().add(user);
				log.debug("added: " + user.getName());
			}
			//everything is good so just return the same session key and the other info

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runGetCell(PMDbDao pmDb,  String project,
			String owner, CellDataType utype) {
		ResponseMessageType responseMessageType = null;

		try {

			log.debug("In runGetCell, with cell of: " + utype.getId() + " | " +utype.getProjectPath());

			List response = null;	
			try {
				response = pmDb.getCell(utype.getId(), utype.getProjectPath(), true);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			CellDataType user = null;
			while (it.hasNext())
				user = (CellDataType)it.next();

			//user = userManagerService.getUserByUserName(k.getUsername());
			if (user == null)
				throw new Exception ("Cell does not exist");




			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,user);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}



	//All Approval process
	private String runDeleteApproval(PMDbDao pmDb, String project, String caller, ApprovalType utype) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = pmDb.deleteApproval(utype.getId(), project, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runSetApproval(PMDbDao pmDb, String project, String caller,
			ApprovalType utype) {
		ResponseMessageType responseMessageType = null;

		try {

			//String SQL_QUERY ="from UserData where oid='" + username + "'";

			int result = pmDb.setApproval(utype, project,  caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + "");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}


	private String runGetAllApproval(PMDbDao pmDb, String project, String caller) {
		ResponseMessageType responseMessageType = null;

		try {


			List response = null;	
			try {
				response = pmDb.getAllApproval(project, caller);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			ApprovalsType users = new ApprovalsType();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				ApprovalType user = (ApprovalType)it.next();
				users.getApproval().add(user);
				log.debug("added: " + user.getName());
			}
			//everything is good so just return the same session key and the other info

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runGetApproval(PMDbDao pmDb,  String project,
			String owner, ApprovalType utype) {
		ResponseMessageType responseMessageType = null;

		try {

			List response = null;	
			try {
				response = pmDb.getApproval(utype, true);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}


			Iterator it = response.iterator();
			ApprovalsType users = new ApprovalsType();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				ApprovalType user = (ApprovalType)it.next();
				users.getApproval().add(user);
				log.debug("added: " + user.getName());
			}

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}



	//All Project process
	private String runDeleteProject(PMDbDao pmDb, String caller, Object project) {
		ResponseMessageType responseMessageType = null;

		try {
			int result = pmDb.deleteProject(project, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runSetProject(PMDbDao pmDb, String project, String caller,
			ProjectType utype) {
		ResponseMessageType responseMessageType = null;

		try {

			//String SQL_QUERY ="from UserData where oid='" + username + "'";

			int result = pmDb.setProject(utype, caller);

			ResultStatusType results = new ResultStatusType();
			StatusType status  = new StatusType();
			status.setValue(result + " records");
			results.setStatus(status);
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,results);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}


	private String runGetAllProject(PMDbDao pmDb, String project, String caller) {
		ResponseMessageType responseMessageType = null;

		try {


			List response = null;	
			try {
				response = pmDb.getAllProject(project, caller);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			ProjectsType users = new ProjectsType();
			log.debug("Records returned: " + response.size());
			while (it.hasNext())
			{
				ProjectType user = (ProjectType)it.next();

				users.getProject().add(user);
				log.debug("added: " + user.getName());
			}
			//everything is good so just return the same session key and the other info

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,users);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}

	private String runGetProject(PMDbDao pmDb, String caller, Object utype) {
		ResponseMessageType responseMessageType = null;

		try {

			List response = null;	
			try {
				response = pmDb.getProject(utype, true);
			} catch (I2B2DAOException e1) {
				throw new Exception ( "Database error in getting user data for NTLM");
			} catch (I2B2Exception e1) {
				throw new Exception ("Database error in getting user data for NTLM");
			}

			Iterator it = response.iterator();
			ProjectType user = null;
			while (it.hasNext())
				user = (ProjectType)it.next();

			//user = userManagerService.getUserByUserName(k.getUsername());
			if (user == null)
				throw new Exception ("Project does not exist");


			//everything is good so just return the same session key and the other info
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());    
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,user);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}



	private String runGetUserConfiguration(PMDbDao pmDb, GetUserConfigurationType userType,
			String username, String domain, ConfigureType cType, UserType uType) {

		//ConfigureType cType = new ConfigureType();
		//UserType uType = new UserType();
		CellDatasType aType = new CellDatasType();

		ResponseMessageType responseMessageType = null;

		try {
			//Get user Data
			cType.setUser(uType);

			if ((userType.getDataNeeded() == null) || (userType.getDataNeeded().size() == 0) ||  (userType.getDataNeeded().contains("USER")))
			{


				Iterator itsg = pmDb.getUserProject(username).iterator();
				if (itsg == null)
					throw new Exception (username + " is not associated with any groups");
				while (itsg.hasNext()) {
					ProjectType pType = (ProjectType)itsg.next();


					//Get Variables for the project
					for(Iterator it2=pmDb.getProjectParams(pType.getId()).iterator();it2.hasNext();){
						ParamType vardata =(ParamType)it2.next();
						log.debug("Added Project Param: " + vardata.getName());
						pType.getParam().add(vardata);
					}
					//Get user project params for the project
					ProjectType tempptype = new ProjectType();
					tempptype.setId(pType.getId());
					tempptype.setUserName(uType.getUserName());
					for(Iterator it2=pmDb.getAllParam(tempptype, null, username).iterator();it2.hasNext();){
						ParamType vardata =(ParamType)it2.next();
						log.debug("Added Project Param: " + vardata.getName());
						pType.getParam().add(vardata);
					}

					//Get Roles for the project 
					for(Iterator it2=pmDb.getRole(username, pType.getId()).iterator();it2.hasNext();){
						RoleType g = (RoleType) it2.next();

						log.debug("got Role: " + g.getRole());
						pType.getRole().add(g.getRole());

					}
					//Get ADMIN @ role
					for (Iterator it2=pmDb.getRole(username, "@").iterator();it2.hasNext();){
						RoleType g = (RoleType) it2.next();

						log.debug("got Role: " + g.getRole());
						if (g.getRole().equals("ADMIN"))
							uType.setIsAdmin(true);

					}


					uType.getProject().add(pType);

				}


				//OLD CODE
				/*

				Iterator itsg = pmDb.getRole(username).iterator();
				if (itsg == null)
					throw new Exception (username + " is not associated with any groups");
				boolean found = false;
				String origProject = "";
				while (itsg.hasNext()) {
					found = true;
					ProjectType pType = new ProjectType();

					RoleData g = (RoleData) itsg.next();

					pType.getRole().add(g.getRole());

					log.debug("got Role: " + g.getRole());
					if (g.getProject().equals(origProject))
					{
						// Project is the same, just a new role.  So add it and continue
						continue;
					}


					origProject = g.getProject();

					log.debug("pdata: " + g.getProject());

					//Only get valid projects, 

					//GroupData pData = globaldataservice.getGroupDataByOid(g.getName());

					for(Iterator it=pmDb.getProject(g.getProject()).iterator();it.hasNext();){
						ProjectType pData =(ProjectType)it.next();

						//Get Variables for the group
						for(Iterator it2=pmDb.getProjectParams(g.getProject()).iterator();it2.hasNext();){
							ParamType vardata =(ParamType)it2.next();
							log.debug("Added Project Param: " + vardata.getName());
							pType.getParam().add(vardata);
						}

						pType.setChangeDate(pData.getChangeDate());
						pType.setEntryDate(pData.getEntryDate());
						pType.setKey(pData.getKey());
						pType.setId(pData.getId());
						pType.setName(pData.getName());
						pType.setStatus(pData.getStatus());
						pType.setWiki(pData.getWiki());
						log.debug("Added Project: " + pData.getId());
						//uType.getProject().add(pData);
					}



					//Get user variables for the group
					for(Iterator it=pmDb.getProjectUserParams(g.getProject(), username).iterator();it.hasNext();){

						ParamType vardata =(ParamType)it.next();
						log.debug("Added Project User Param: " + vardata.getName());
						pType.getParam().add(vardata);
					}
					uType.getProject().add(pType);


					// Set project only if null or requested
				//	log.debug("Adding Project: " + userType.getProject());
				//	if ((userType.getProject() == null) || (userType.getProject().size() == 0) ||  (userType.getProject().contains(pType.getId().toUpperCase())))
				//		uType.getProject().add(pType);

					log.debug("Adding usertype");
					cType.setUser(uType);
				}
				 */
				if (cType.getUser() == null)
					throw new Exception ("Group Data has not been filled out");
				//if (found == false)
				//	throw new Exception (username + " is not part of project " + domain);
			}





			//Get Cell Data
			if ((userType.getDataNeeded() == null)  || (userType.getDataNeeded().size() == 0) ||  (userType.getDataNeeded().contains("CELL_DATA")))
			{

				// Get all cells
				for(Iterator it=pmDb.getCell("@", "/", true).iterator();it.hasNext();){
					CellDataType cell =(CellDataType)it.next();			


					if (cell == null)
					{
						throw new Exception ("No cells are be registered yet");
					}

					log.debug("my url is " + cell.getUrl());	


					for(Iterator it2=pmDb.getCellParam(cell.getId(),"/").iterator();it2.hasNext();){
						ParamType cellParam =(ParamType)it2.next();			
						cell.getParam().add(cellParam);		
					}
					aType.getCellData().add(cell);					
				}  

				// Get project specific cells
				for (ProjectType p : uType.getProject()) {
					log.debug("Searching cells for: " + p.getId() + ":" + p.getPath());
					if (p.getPath() != null)
						for(Iterator it=pmDb.getCell("@", p.getPath(), true).iterator();it.hasNext();){
							CellDataType cell =(CellDataType)it.next();			


							if (cell == null)
							{
								throw new Exception ("No cells are be registered yet");
							}

							log.debug("my url is " + cell.getUrl());	


							for(Iterator it2=pmDb.getCellParam(cell.getId(),"/").iterator();it2.hasNext();){
								ParamType cellParam =(ParamType)it2.next();			
								cell.getParam().add(cellParam);		
							}

							aType.getCellData().add(cell);					
						}  
				}

				cType.setCellDatas(aType);
			}

			//Get User Params

			for(Iterator it2=pmDb.getAllParam(uType,null,null).iterator();it2.hasNext();){
				UserParamData p = (UserParamData)it2.next();
				ParamType param = new ParamType();
				param.setName(p.getName());
				param.setValue(p.getValue());
				uType.getParam().add(param);
			}
			//

			// Get Global Data
			GlobalDataType globaldata = new GlobalDataType();

			for(Iterator it=pmDb.getAllParam(new GlobalDataType(), null, null).iterator();it.hasNext();){

				//GlobalDataType globaldata =(GlobalDataType)it.next();			

				//		List gData = globaldataservice.getGlobalData();
				//	GlobalDataType gValue = new GlobalDataType();

				//			if (gData!=null) {
				//			for (int i=0;i<gData.size();i++) {
				//GlobalParamData globaldata = (GlobalParamData)gData.get(i);

				//	ParamType parmType = new ParamType();
				//	parmType.setName(globaldata.getName());
				//	parmType.setValue(globaldata.getValue());

				//	gValue.getParam().add(globaldata);

				ParamType param =(ParamType)it.next();	
				globaldata.getParam().add(param);
			}
			cType.setGlobalData(globaldata);

			//			}			
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,cType);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}




}