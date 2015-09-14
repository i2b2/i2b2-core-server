package edu.harvard.i2b2.crc.datavo.db;


import java.util.Date;

/**
 * QtQueryBreakdownType 
 */
public class QtQueryBreakdownType implements java.io.Serializable {

	// Fields

	private String name;
	private String value;
	private Date createDate;
	private Date updateDate;
	private String userId;

	// Constructors

	/** default constructor */
	public QtQueryBreakdownType() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
