/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.datavo;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * This class customizes the namespace prefix generated
 * by jaxb while marshalling. It overides default namespace like ns2,ns3..
 * $Id: NamespacePrefixMapperImpl.java,v 1.3 2007/08/31 14:47:05 rk903 Exp $
 * @author rkuttan
 */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {
    public String getPreferredPrefix(String namespaceUri, String suggestion,
        boolean requirePrefix) {
        if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
            return "xsi";
        } else if ("http://i2b2.mgh.harvard.edu/message".equals(namespaceUri)) {
            return "i2b2";
        } else if ("http://i2b2.mgh.harvard.edu/querytool".equals(namespaceUri)) {
            return "querytool";
        } else if ("http://i2b2.mgh.harvard.edu/repository_cell".equals(
                    namespaceUri)) {
            return "patientdata";
        } else {
            return suggestion;
        }
    }

    public String[] getPreDeclaredNamespaceUris() {
        return new String[] {  };
    }
}
