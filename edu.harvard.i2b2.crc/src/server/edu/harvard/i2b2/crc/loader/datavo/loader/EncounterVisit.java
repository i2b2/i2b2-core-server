package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.util.Date;

/**
 * Object to hold Encounter and Visit values.
 * @author rk903
 *
 */
public class EncounterVisit {

	private String encounterIde = null;
	private String source = null;
	private int encounterNum = 0 ;
	private int patientNum = 0;
	private String inOutCd = null;
	private String locationPath = null;
	private String visitBlob = null;
	
	private Date startDate = null;
	private Date endDate = null;
	private Date updateDate = null;
	private Date downloadDate = null;
	private Date importDate = null;
	
	public Date getDownloadDate() {
		return downloadDate;
	}
	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}
	public String getEncounterIde() {
		return encounterIde;
	}
	public void setEncounterIde(String encounterIde) {
		this.encounterIde = encounterIde;
	}
	public int getEncounterNum() {
		return encounterNum;
	}
	public void setEncounterNum(int encounterNum) {
		this.encounterNum = encounterNum;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getImportDate() {
		return importDate;
	}
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}
	public String getInOutCd() {
		return inOutCd;
	}
	public void setInOutCd(String inOutCd) {
		this.inOutCd = inOutCd;
	}
	public String getLocationPath() {
		return locationPath;
	}
	public void setLocationPath(String locationPath) {
		this.locationPath = locationPath;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getVisitBlob() {
		return visitBlob;
	}
	public void setVisitBlob(String visitBlob) {
		this.visitBlob = visitBlob;
	}
	public int getPatientNum() {
		return patientNum;
	}
	public void setPatientNum(int patientNum) {
		this.patientNum = patientNum;
	} 

}
