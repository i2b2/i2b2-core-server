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
