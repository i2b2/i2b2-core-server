package edu.harvard.i2b2.pm.services;


///import edu.harvard.i2b2.pm.datavo.pm.ParamType;

public class GroupData {
    // every persistent object needs an identifier
    
	//private String oid = null;
    private String wiki = new String();
    private String oid = new String();
    private String name = new String();
    private String key = new String();
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getWiki() {
		return wiki;
	}
	public void setWiki(String wiki) {
		this.wiki = wiki;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}

}
