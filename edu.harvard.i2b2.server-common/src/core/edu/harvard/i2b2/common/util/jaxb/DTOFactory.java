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
