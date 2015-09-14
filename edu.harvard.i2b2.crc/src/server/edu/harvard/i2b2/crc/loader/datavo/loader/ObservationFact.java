package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.util.Date;


/**
 * Object to hold observation fact values.
 * @author rk903
 * 
 */
public class ObservationFact {
	private int encounterNum = 0; 
	private String conceptCd = null;
	private int patientNum = 0;
	private String providerId = null;
	private Date startDate = null;
	private String modifierCd = null;
	private String valtypeCd = null;
	private String observationBlob = null;
	private String tvalChar = null;
	private Float nvalNum = null;
	private String valueflagCd = null;
	private Float quantityNum = null;
	private Integer confidenceNum = null;
	private String unitsCd = null;
	private Date endDate = null;
	private String locationCd = null;
	private Date updateDate = null;
	private Date downloadDate = null;
	private Date importDate = null;
	private String sourcesystemCd = null;
	private String patientIde = null;
	private String encounterIde = null;
	private int uploadId = 0;
	
	
	
	public int getUploadId() {
		return uploadId;
	}
	public void setUploadId(int uploadId) {
		this.uploadId = uploadId;
	}
	public String getEncounterIde() {
		return encounterIde;
	}
	public void setEncounterIde(String encounterIde) {
		this.encounterIde = encounterIde;
	}
	public String getPatientIde() {
		return patientIde;
	}
	public void setPatientIde(String patientIde) {
		this.patientIde = patientIde;
	}
	public String getConceptCd() {
		return conceptCd;
	}
	public void setConceptCd(String conceptCd) {
		this.conceptCd = conceptCd;
	}
	public Integer getConfidenceNum() {
		return confidenceNum;
	}
	public void setConfidenceNum(Integer confidenceNum) {
		this.confidenceNum = confidenceNum;
	}
	public Date getDownloadDate() {
		return downloadDate;
	}
	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
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
	public String getLocationCd() {
		return locationCd;
	}
	public void setLocationCd(String locationCd) {
		this.locationCd = locationCd;
	}
	public Float getNvalNum() {
		return nvalNum;
	}
	public void setNvalNum(Float nvalNum) {
		this.nvalNum = nvalNum;
	}
	public int getPatientNum() {
		return patientNum;
	}
	public void setPatientNum(int patientNum) {
		this.patientNum = patientNum;
	}
	
	public String getProviderId() {
		return providerId;
	}
	
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public Float getQuantityNum() {
		return quantityNum;
	}
	public void setQuantityNum(Float quantityNum) {
		this.quantityNum = quantityNum;
	}
	public String getSourcesystemCd() {
		return sourcesystemCd;
	}
	public void setSourcesystemCd(String sourcesystemCd) {
		this.sourcesystemCd = sourcesystemCd;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public String getTvalChar() {
		return tvalChar;
	}
	public void setTvalChar(String tvalChar) {
		this.tvalChar = tvalChar;
	}
	public String getUnitsCd() {
		return unitsCd;
	}
	public void setUnitsCd(String unitsCd) {
		this.unitsCd = unitsCd;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getValtypeCd() {
		return valtypeCd;
	}
	public void setValtypeCd(String valtypeCd) {
		this.valtypeCd = valtypeCd;
	}
	public String getValueflagCd() {
		return valueflagCd;
	}
	public void setValueflagCd(String valueflagCd) {
		this.valueflagCd = valueflagCd;
	}
	public String getObservationBlob() {
		return observationBlob;
	}
	public void setObservationBlob(String observationBlob) {
		this.observationBlob = observationBlob;
	}
	
	public String toString() { 
		return this.getEncounterNum() + 
		" "  +
		this.getConceptCd() + 
		" " + 
		this.getPatientNum() + 
		" " + 
		this.getProviderId() +
		" " +
		this.getStartDate();
	}
	
	public String getModifierCd() {
		return modifierCd;
	}
	public void setModifierCd(String modifierCd) {
		this.modifierCd = modifierCd;
	}
}
