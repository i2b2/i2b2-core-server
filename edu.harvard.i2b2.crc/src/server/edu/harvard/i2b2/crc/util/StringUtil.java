package edu.harvard.i2b2.crc.util;


/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
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
