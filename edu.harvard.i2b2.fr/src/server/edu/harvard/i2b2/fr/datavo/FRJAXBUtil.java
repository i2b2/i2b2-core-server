/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.fr.datavo;

/**
 * Factory class to create jaxb context
 * Since jaxb context is tread safe, only one instance is created for this cell.
 * The package used for jaxb context is read from spring config file
 * $Id: FRJAXBUtil.java,v 1.2 2008/09/10 16:18:10 mem61 Exp $
 * @author rkuttan
 */
public class FRJAXBUtil {
    private static edu.harvard.i2b2.common.util.jaxb.JAXBUtil jaxbUtil = null;
   
    

    private FRJAXBUtil() {
    }

	public static edu.harvard.i2b2.common.util.jaxb.JAXBUtil getJAXBUtil() {
        if (jaxbUtil == null) {
           jaxbUtil = new edu.harvard.i2b2.common.util.jaxb.JAXBUtil(new String[]{"edu.harvard.i2b2.fr.datavo.fr.query"
        		   ,"edu.harvard.i2b2.fr.datavo.i2b2message",
        		   "edu.harvard.i2b2.fr.datavo.pm"});
        }
        return jaxbUtil;
    }
    
    
    
}
