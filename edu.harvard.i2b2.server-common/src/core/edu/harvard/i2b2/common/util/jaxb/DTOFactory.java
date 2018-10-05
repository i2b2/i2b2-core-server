/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util.jaxb;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class DTOFactory {
	
    public XMLGregorianCalendar getXMLGregorianCalendar(long timeInMilliSec) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeInMilliSec);

        return getXMLGregorianCalendar(cal);
    }

    public XMLGregorianCalendar getXMLGregorianCalendar(GregorianCalendar cal) {
        DatatypeFactory dataTypeFactory;
        XMLGregorianCalendar xmlCalendar = null;

        try {
            dataTypeFactory = DatatypeFactory.newInstance();
            xmlCalendar = dataTypeFactory.newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return xmlCalendar;
    }

    public XMLGregorianCalendar getXMLGregorianCalendarDate(int year,
        int month, int day) {
        DatatypeFactory dataTypeFactory;
        XMLGregorianCalendar xmlCalendar = null;

        try {
            dataTypeFactory = DatatypeFactory.newInstance();

            xmlCalendar = dataTypeFactory.newXMLGregorianCalendarDate(year,
                    month, day, 0);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return xmlCalendar;
    }
}
