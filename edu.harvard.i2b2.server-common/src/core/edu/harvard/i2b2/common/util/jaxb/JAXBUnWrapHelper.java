/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util.jaxb;

import java.util.List;

import javax.xml.bind.JAXBElement;


public class JAXBUnWrapHelper {
    public JAXBUnWrapHelper() {
    }

    public Object getObjectByClass(List<Object> listType, Class requestClass)
        throws JAXBUtilException {
        Object returnObject = null;

        if (listType == null) {
            throw new JAXBUtilException("Input list is null");
        }

        for (Object so : listType) {
            Object object = null;

            if (so instanceof JAXBElement) {
                object = ((JAXBElement) so).getValue();
            } else {
                object = so;
            }

            Class objClass = object.getClass();

            if (objClass.equals(requestClass)) {
                returnObject = object;
            }
        }

        return returnObject;
    }
}
