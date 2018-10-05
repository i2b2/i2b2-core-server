/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.ontology.util;

import java.util.ArrayList;

import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

public class Roles {
	private static Roles thisInstance;

	   static {
           thisInstance = new Roles();
   }
   
   public static Roles getInstance() {
       return thisInstance;
   }

   public boolean isRoleValid(ProjectType projectInfo){
 
	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
	   for(String param :roles) {
		   // Bug 728; enable feature for role = editor only
		//   if(param.equalsIgnoreCase("manager")) 
		//	   return true;
		 //  if(param.equalsIgnoreCase("admin")) 
		//	   return true;
		   if(param.equalsIgnoreCase("editor")) 
			   return true;
	   }
	   return false;
   }
   
   public boolean isRoleAdmin(ProjectType projectInfo) { 

	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
	   for(String param :roles) {
		   // Bug 728; enable feature for role = editor only
		//   if(param.equalsIgnoreCase("manager")) 
		//	   return true;
		 
		   if(param.equalsIgnoreCase("admin")) { 
			   return true;
		   }
	   }
	   return false;
   }
   
   public boolean isRoleOfuscated(ProjectType projectInfo) { 
	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
	   for(String param :roles) {
		   if (param.equalsIgnoreCase("DATA_AGG")) { 
			   return false;
		   }
	   }
	   return true;
   }
	
}
