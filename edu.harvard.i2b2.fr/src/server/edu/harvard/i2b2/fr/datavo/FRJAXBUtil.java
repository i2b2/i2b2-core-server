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
