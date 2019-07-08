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
 *                 Raj Kuttan
 *                 Lori Phillips
 */
package edu.harvard.i2b2.pm.util;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;


/**
 * This is the PM service's main utility class
 * This utility class provides support for
 * fetching resources like datasouce, to read application
 * properties, to get ejb home,etc.
 * $Id: PMUtil.java,v 1.3 2009/07/10 18:40:07 mem61 Exp $
 * @author rkuttan
 */
public class PMUtil {
    /** property file name which holds application directory name **/
    public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "pm_application_directory.properties";

    /** application directory property name **/
    public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.pm.applicationdir";

    /** application property filename**/
    public static final String APPLICATION_PROPERTIES_FILENAME = "pm.properties";

    /** property name for datasource present in app property file**/
    private static final String DATASOURCE_JNDI_PROPERTIES = "pm.jndi.datasource_name";

    /** property name for metadata schema name**/
    private static final String METADATA_SCHEMA_NAME_PROPERTIES = "pm.bootstrapdb.metadataschema";

    /** spring bean name for datasource **/
    private static final String DATASOURCE_BEAN_NAME = "dataSource";

    /** class instance field**/
    private static PMUtil thisInstance = null;

    /** service locator field**/
    private static ServiceLocator serviceLocator = null;

    /** field to store application properties **/
    private static Properties appProperties = null;

    /** log **/
    protected final Log log = LogFactory.getLog(getClass());

    /** field to store app datasource**/
    private DataSource dataSource = null;

    /**
     * Private constructor to make the class singleton
     */
    private PMUtil() {
    }

    /**
     * Return this class instance
     * @return OntologyUtil
     */
    public static PMUtil getInstance() {
        if (thisInstance == null) {
            thisInstance = new PMUtil();
        }

        serviceLocator = ServiceLocator.getInstance();

        return thisInstance;
    }



    
    /**
     * Return app server datasource
     * @return datasource
     * @throws I2B2Exception
     */
    public DataSource getDataSource(String dataSourceName) throws I2B2Exception {    	
    	dataSource = (DataSource) serviceLocator
		.getAppServerDataSource(dataSourceName);
    	return dataSource;
  
    }
    

	public void convertToUppercaseStrings( List< String > list )
	{
		ListIterator< String > iterator = list.listIterator();

		while ( iterator.hasNext() ) 
		{
			String color = iterator.next();  // get item                 
			iterator.set( color.toUpperCase() ); // convert to upper case
		} // end while
	}
	public  String toHex(byte[] digest) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			buf.append(Integer.toHexString((int) digest[i] & 0x00FF));
		}
		return buf.toString();
	}

	public String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<20; i++) {
			int num = getValidAcsiiValue();
			//System.out.println("Generated number: " + num + " char: "+(char)num);
			strWriter.append((char)num);
		}
		return strWriter.toString();
	}
	
	private int getValidAcsiiValue() {
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

	    public boolean passwordValidation(String passwd, String pattern) {
	    	/*
	    	 Explanations:
	    	 
    			(?=.*[0-9]) a digit must occur at least once
    			(?=.*[a-z]) a lower case letter must occur at least once
    			(?=.*[A-Z]) an upper case letter must occur at least once
    			(?=.*[!@#$%^&+=]) a special character must occur at least once
    			(?=\\S+$) no whitespace allowed in the entire string
    			.{8,} at least 8 characters
	    	 */
	      //String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[)(;:}{,.><!@#$%^&+=])(?=\\S+$).{8,}";
	      return(passwd.matches(pattern));
	   }
	
	public String getHashedPassword(String pass) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(pass.getBytes());
			return toHex(md5.digest());
		} catch (NoSuchAlgorithmException e) {
			log.error("NoSuchAlgorithm MD5!", e);    
		}
		return null;
	}
    
    //---------------------
    // private methods here
    //---------------------

 
}
