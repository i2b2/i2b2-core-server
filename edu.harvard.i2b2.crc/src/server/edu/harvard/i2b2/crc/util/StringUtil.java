/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.util;


/*

 * 
 * Contributors:
 * 		Lori Phillips
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * StringUtil class to perform string parsing tasks
 * This is singleton class.
 * @author lcp5
 */
public class StringUtil {
	private static Log log = LogFactory.getLog(StringUtil.class.getName());
    //to make this class singleton
    private static StringUtil thisInstance;
    
    static {
            thisInstance = new StringUtil();
    }
    
    public static StringUtil getInstance() {
        return thisInstance;
    }
    
    public static String escapeSQLSERVER(String sql){
    
    	sql=sql.replaceAll("\\?", "??");
    	sql=sql.replaceAll("_", "?_");    
    	sql=sql.replaceAll("%", "?%");
    	sql=sql.replaceAll("\\[", "?["); 
    //	sql += "%";
 
    	return sql;
	}
    
    public static String escapeORACLE(String sql){
        
    	sql=sql.replaceAll("\\?", "??");
    	sql=sql.replaceAll("_", "?_");
    	sql=sql.replaceAll("%", "?%");
  //  	sql += "%";
    	return sql;
	}
    
}
