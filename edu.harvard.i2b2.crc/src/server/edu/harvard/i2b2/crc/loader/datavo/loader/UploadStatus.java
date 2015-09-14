package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.util.Date;

/**
 * Object to hold Upload_Status values.
 * 
 * @author rk903
 * 
 */
public class UploadStatus {

	public static final String SUCCESS_STATUS = "SUCCESS" ; 
	public static final String ERROR_STATUS = "ERROR" ;
	
	private int uploadId = 0;

	private String uploadLabel = null;

	private String userId = null;

	private String sourceCd = null;

	private int noOfRecord = 0;

	private int deletedRecord = 0;
	
	private int loadedRecord = 0;

	private Date loadDate = null;
	
	private Date endDate = null;

	private String loadStatus = null;

	private String inputFileName = null;

	private String logFileName = null;

	private String transformName = null;
	
	private String message = null;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public Date getLoadDate() {
		return loadDate;
	}

	public void setLoadDate(Date loadDate) {
		this.loadDate = loadDate;
	}

	public int getLoadedRecord() {
		return loadedRecord;
	}

	public void setLoadedRecord(int loadedRecord) {
		this.loadedRecord = loadedRecord;
	}
	

	public int getDeletedRecord() {
		return deletedRecord;
	}

	public void setDeletedRecord(int deletedRecord) {
		this.deletedRecord = deletedRecord;
	}

	public String getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(String loadStatus) {
		this.loadStatus = loadStatus;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public int getNoOfRecord() {
		return noOfRecord;
	}

	public void setNoOfRecord(int noOfRecord) {
		this.noOfRecord = noOfRecord;
	}

	public String getSourceCd() {
		return sourceCd;
	}

	public void setSourceCd(String sourceCd) {
		this.sourceCd = sourceCd;
	}

	public String getTransformName() {
		return transformName;
	}

	public void setTransformName(String transformName) {
		this.transformName = transformName;
	}

	public int getUploadId() {
		return uploadId;
	}

	public void setUploadId(int uploadId) {
		this.uploadId = uploadId;
	}

	public String getUploadLabel() {
		return uploadLabel;
	}

	public void setUploadLabel(String uploadLabel) {
		this.uploadLabel = uploadLabel;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}



}