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
package edu.harvard.i2b2.ontology.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;



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
    
    public static String getTableCd(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);
    	return fullPath.substring(2, end).trim();
    }
    
    public static String getPath(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);

    	fullPath = fullPath.substring(end).trim();
    	
    	Boolean addDelimiter = true;
    	
    	try {
			addDelimiter = OntologyUtil.getInstance().getOntTerminalDelimiter();
		} catch (I2B2Exception e) {
			log.debug(e.getMessage() + " property set to false");
			addDelimiter = false;  //if delimiter property is missing; assume false.
			
		}
    	
		// add trailing backslash if ont.terminal.delimiter property set to true.
		if ((addDelimiter) && (fullPath.lastIndexOf('\\') != fullPath.length()-1)) {
				fullPath = fullPath + "\\";
		}
    	return fullPath;
    	
    	
    }
    
    public static String getLiteralPath(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);

    	fullPath = fullPath.substring(end).trim();
    	
    	return fullPath;
    	
    	
    }

    public static String escapePOSTGRESQL(String sql){
        sql=sql.replaceAll("\\\\", "\\\\\\\\");
//        sql=sql.replaceAll("'", "''"); <-- this should only be escaped when the string is going in the query, not if it is a parameter
//    	sql=sql.replaceAll("\\?", "??");
 //   	sql=sql.replaceAll("_", "?_");    
  //  	sql=sql.replaceAll("%", "?%");
   // 	sql=sql.replaceAll("\\[", "?["); 
    //	sql += "%";
 
    	return sql;
	}

	public static String escapeSNOWFLAKE(String sql){
		sql=sql.replaceAll("\\\\", "\\\\\\\\");
		return sql;
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
    
    public static String getCpath(String key){
    	int beginning = key.indexOf("\\", 3);
    	if(key.endsWith("\\")){
    		key = key.substring(0, key.length() - 1).trim();
    	}
    		int end = key.lastIndexOf("\\");    	
    		String cpath = key.substring(beginning, end+1 ).trim();
    		
    		return cpath;

    }
    
    public static String getSymbol(String key){
    	if(key.endsWith("\\")){
    		key = key.substring(0, key.length() - 1).trim();
    	}
    		int end = key.lastIndexOf("\\");    	
    		String symbol = key.substring(end + 1 , key.length()).trim();
    		
    		return symbol;
    }
    
    public static String getParentPath(String key){
    	//int beginning = key.indexOf("\\", 3);
    	if(key.endsWith("\\")){
    		key = key.substring(0, key.length() - 1).trim();
    	}
    		int end = key.lastIndexOf("\\");    	
    		String cpath = key.substring(0, end+1 ).trim();
    		
    		return cpath;

    }
    
}
