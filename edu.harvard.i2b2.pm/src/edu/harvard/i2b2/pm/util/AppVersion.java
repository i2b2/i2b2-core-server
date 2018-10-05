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
package edu.harvard.i2b2.pm.util;

import java.io.StringWriter;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

/**
 * StringUtil class to perform string parsing tasks
 * This is singleton class.
 * @author lcp5
 */
public class AppVersion {

    //to make this class singleton
    private static AppVersion thisInstance;
    
    static {
            thisInstance = new AppVersion();
    }
    
    public static AppVersion getInstance() {
        return thisInstance;
    }
    

    public static String appServerRunningVersion() throws Exception {  
		StringWriter strWriter = new StringWriter();
        try (ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9990)) {  
            final ModelNode op = Operations.createReadResourceOperation(new ModelNode().setEmptyList());  
            final ModelNode result = client.execute(op);  
            if (Operations.isSuccessfulOutcome(result)) {  
                final ModelNode model = Operations.readResult(result);  
                final String productName;  
                if (model.hasDefined("product-name")) {  
                    productName = model.get("product-name").asString();  
                } else {  
                    productName = "WildFly";  
                }  
      
      
                String productVersion = null;  
                if (model.hasDefined("product-version")) {  
                    productVersion = model.get("product-version").asString();  
                }  
      
      
                String releaseCodename = null;  
                if (model.hasDefined("release-codename")) {  
                    releaseCodename = model.get("release-codename").asString();  
                }  
      
      
                String releaseVersion = null;  
                if (model.hasDefined("release-version")) {  
                    releaseVersion = model.get("release-version").asString();  
                }  
      
      
                strWriter.append((productName != null ? productName : "WildFly"));  
                strWriter.append(" " + productVersion);  
                //strWriter.append("\nCodename: " + releaseCodename);  
                //strWriter.append("\nRelease Version: " +  releaseVersion);  
            } else {  
            	strWriter.append(result.toString());  
            }  
        }  
        return strWriter.toString();
    }  
}
