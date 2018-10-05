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


/**
 * StringUtil class to perform string parsing tasks
 * This is singleton class.
 * @author lcp5
 */
public class StringUtil {

    //to make this class singleton
    private static StringUtil thisInstance;
    
    static {
            thisInstance = new StringUtil();
    }
    
    public static StringUtil getInstance() {
        return thisInstance;
    }
    
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public static String getTableCd(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);
    	return fullPath.substring(2, end).trim();
    }
    
    public static String getPath(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);
    	return fullPath.substring(end).trim();
    }
    
    // convert //key/index   to index
    public static String getParentIndex(String fullIndex) {
    	int end = fullIndex.indexOf("\\", 3) + 1;
    	return fullIndex.substring(end).trim();
    }
    
    // convert //key/index   to index
    public static String getIndex(String fullIndex) {
    	int end = fullIndex.indexOf("\\", 3) + 1;
    	return fullIndex.substring(end).trim();
    }
    
    public static String replaceEnd(String path, String oldEnding, String newEnding) {
    	if(path == null)
    		return null;
    	else {
    		int end;
			try {
				end = path.length() - oldEnding.length();
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				return null;
			}
			String newPath = path.substring(0,end).trim() + newEnding;
    		return newPath.trim();
    	}
    }
    
	public static String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<20; i++) {
			int num = getValidAcsiiValue();
			strWriter.append((char)num);
		}
		return strWriter.toString();
	}

	private static int getValidAcsiiValue() {
		int number = 48;
		while(true) {
			number = 48+(int) Math.round(Math.random() * 74);
			if((number > 47 && number < 58) || (number > 64 && number < 91) 
				|| (number > 96 && number < 123)) {
					break;
				}
		}
		return number;
	}
	
}
