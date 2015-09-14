package edu.harvard.i2b2.ontology.ejb;

public class TableAccessType {

	private String tableCd; 
	private String tableName;
	private String fullName;
	private String synonymCd;
	private String visualAttributes;
	private String dimCode;
	
	
	public String getTableCd() {
		return tableCd;
	}
	public void setTableCd(String tableCd) {
		this.tableCd = tableCd;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getSynonymCd() {
		return synonymCd;
	}
	public void setSynonymCd(String synonymCd) {
		this.synonymCd = synonymCd;
	}
	public String getVisualAttributes() {
		return visualAttributes;
	}
	public void setVisualAttributes(String visualAttributes) {
		this.visualAttributes = visualAttributes;
	}
	public String getDimCode() {
		return dimCode;
	}
	public void setDimCode(String dimCode) {
		this.dimCode = dimCode;
	}
	
	
	
	
}
