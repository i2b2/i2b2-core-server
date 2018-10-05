/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util.jaxb;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;


class NamespacePrefixMapperImpl extends NamespacePrefixMapper {
    public String getPreferredPrefix(String namespaceUri, String suggestion,
        boolean requirePrefix) {
        if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
            return "xsi";
        } else if ("http://i2b2.mgh.harvard.edu/message".equals(namespaceUri)) {
            return "i2b2";
        } else {
            return suggestion;
        }
    }

    public String[] getPreDeclaredNamespaceUris() {
        return new String[] {  };
    }
}
