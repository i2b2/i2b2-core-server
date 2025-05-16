package edu.harvard.i2b2.crc.datavo.pdo.query;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class CDATAAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String v) throws Exception {
    	if (v.contains("<") || v.contains("&"))
    		return "<![CDATA[" + v + "]]>";
    	else 
    		return v;
    }

    @Override
    public String unmarshal(String v) throws Exception {
        return v;
    }
}