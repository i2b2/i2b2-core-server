package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.util.Date;

public class Patient {

	private String patientIde = null;
	private String patientIdeStatus = null;
	private int patientNum = 0;
	private String sexCd = null;
	private int    ageInYearsNum = 0; 
	private String languageCd = null;
	private String raceCd = null;
	private String maritalStatusCd = null; 
	private String religionCd = null;
	private String zipCd = null;
	private String vitalStatusCd = null;
	private Date   birthDate = null;
	private Date   deathDate = null;
	private String stateCityZipPath = null;
//	skiping patientBlob
	private Date   updateDate = null;
	private Date   downloadDate = null;
	private Date   importDate = null;
	private String sourceSystemCd = null;
	private String source = null;
	
	
	public Patient() { 
		
	}
	
	public int getAgeInYearsNum() {
		return ageInYearsNum;
	}
	public void setAgeInYearsNum(int ageInYearsNum) {
		this.ageInYearsNum = ageInYearsNum;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public Date getDeathDate() {
		return deathDate;
	}
	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}
	public Date getDownloadDate() {
		return downloadDate;
	}
	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}
	public Date getImportDate() {
		return importDate;
	}
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}
	public String getLanguageCd() {
		return languageCd;
	}
	public void setLanguageCd(String languageCd) {
		this.languageCd = languageCd;
	}
	public String getMaritalStatusCd() {
		return maritalStatusCd;
	}
	public void setMaritalStatusCd(String maritalStatusCd) {
		this.maritalStatusCd = maritalStatusCd;
	}
	public String getPatientIde() {
		return patientIde;
	}
	public void setPatientIde(String patientIde) {
		this.patientIde = patientIde;
	}
	public String getPatientIdeStatus() {
		return patientIdeStatus;
	}
	public void setPatientIdeStatus(String patientIdeStatus) {
		this.patientIdeStatus = patientIdeStatus;
	}
	public int getPatientNum() {
		return patientNum;
	}
	public void setPatientNum(int patientNum) {
		this.patientNum = patientNum;
	}
	public String getRaceCd() {
		return raceCd;
	}
	public void setRaceCd(String raceCd) {
		this.raceCd = raceCd;
	}
	public String getReligionCd() {
		return religionCd;
	}
	public void setReligionCd(String religionCd) {
		this.religionCd = religionCd;
	}
	public String getSexCd() {
		return sexCd;
	}
	public void setSexCd(String sexCd) {
		this.sexCd = sexCd;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSourceSystemCd() {
		return sourceSystemCd;
	}
	public void setSourceSystemCd(String sourceSystemCd) {
		this.sourceSystemCd = sourceSystemCd;
	}
	public String getStateCityZipPath() {
		return stateCityZipPath;
	}
	public void setStateCityZipPath(String stateCityZipPath) {
		this.stateCityZipPath = stateCityZipPath;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getVitalStatusCd() {
		return vitalStatusCd;
	}
	public void setVitalStatusCd(String vitalStatusCd) {
		this.vitalStatusCd = vitalStatusCd;
	}
	public String getZipCd() {
		return zipCd;
	}
	public void setZipCd(String zipCd) {
		this.zipCd = zipCd;
	}

	


}
