package edu.harvard.i2b2.pm.services;


///import edu.harvard.i2b2.pm.datavo.pm.ParamType;

public class EnvironmentData {
    // every persistent object needs an identifier
    
	//private String oid = null;
    private String url = new String();
    private String domain = new String();
    private String oid = new String();
    private String environment = new String();
    private Boolean active = null;

	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
        public String getDomain() {
                return domain;
        }
        public void setDomain(String domain) {
                this.domain = domain;
        }
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}


}
