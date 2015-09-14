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
