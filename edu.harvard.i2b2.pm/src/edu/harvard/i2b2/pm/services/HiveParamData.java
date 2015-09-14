package edu.harvard.i2b2.pm.services;

public class HiveParamData {

    private String value = new String();
    public HiveParamDataPK getHiveParamDataPK() {
		return hiveParamDataPK;
	}
	public void setHiveParamDataPK(HiveParamDataPK hiveParamDataPK) {
		this.hiveParamDataPK = hiveParamDataPK;
	}
	private HiveParamDataPK hiveParamDataPK = new HiveParamDataPK();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return hiveParamDataPK.getName();
	}
	public void setName(String name) {
		hiveParamDataPK.setName(name);
	}
	public String getDomain() {
		return hiveParamDataPK.getDomain();
	}
	public void setDomain(String domain) {
		hiveParamDataPK.setDomain(domain);
	}



}
