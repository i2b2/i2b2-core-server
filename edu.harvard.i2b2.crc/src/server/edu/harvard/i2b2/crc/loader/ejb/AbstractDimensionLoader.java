package edu.harvard.i2b2.crc.loader.ejb;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;

/**
 * Abstract loader classs. Individual dimension loader class should implement
 * load method.
 * 
 * @author rk903
 * 
 */
public abstract class AbstractDimensionLoader {
	protected String inputLoadFile = null;
	protected String inputLoadFileFormat = null;
	protected String outputXmlFileName = null;
	protected String stagingTableName = null;
	protected String sourceSystemCd = null;
	protected String encounterSource = null;
	protected IUploaderDAOFactory uploaderDaoFactory = null;

	private int uploadId = 0;

	public void setStagingTableName(String stagingTableName) {
		this.stagingTableName = stagingTableName;
	}

	public String getStagingTableName() {
		return stagingTableName;
	}

	public String getOutputXmlFileName() {
		return outputXmlFileName;
	}

	public void setOutputXmlFileName(String outputXmlFileName) {
		this.outputXmlFileName = outputXmlFileName;
	}

	public int getUploadId() {
		return uploadId;
	}

	public void setUploadId(int uploadId) {
		this.uploadId = uploadId;
	}

	public String getInputLoadFile() {
		return inputLoadFile;
	}

	public void setInputLoadFile(String inputLoadFile) {
		this.inputLoadFile = inputLoadFile;
	}

	public String getInputLoadFileFormat() {
		return inputLoadFileFormat;
	}

	public void setInputLoadFileFormat(String inputLoadFileFormat) {
		this.inputLoadFileFormat = inputLoadFileFormat;
	}

	public String getSourceSystemCd() {
		return sourceSystemCd;
	}

	public void setSourceSystemCd(String sourceSystemCd) {
		this.sourceSystemCd = sourceSystemCd;
	}

	public String getEncounterSource() {
		return encounterSource;
	}

	public void setEncounterSource(String encounterSource) {
		this.encounterSource = encounterSource;
	}

	/**
	 * Implement load method. The values needed for load are set thru instance
	 * variable.
	 * 
	 * @throws I2B2Exception
	 */
	public abstract void load() throws I2B2Exception;

	public abstract void setStartStatus();

	public abstract void createTempTable() throws I2B2Exception;

	public abstract int loadTempTable() throws I2B2Exception;

	public abstract int mergeTempTable() throws I2B2Exception;

	// public abstract void backupAndClearTable() throws I2B2Exception;

	// public abstract int updateSetStatus(int totalRecords, int updatedRecords,
	// Exception exception) throws I2B2Exception;

	public IUploaderDAOFactory getUploaderDaoFactory() {
		return uploaderDaoFactory;
	}

	public void setUploaderDaoFactory(IUploaderDAOFactory uploaderDaoFactory) {
		this.uploaderDaoFactory = uploaderDaoFactory;
	}

}
