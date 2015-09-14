package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IConceptDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAO;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;

/**
 * This specific loader class for Visit dimension. This class performs three
 * operations, namely. 1.Build PatientData xml file based, if input file is csv
 * 2.Load PatientData xml information into staging area in database. 3.Run
 * procedure which will merge staging area into Visit dimension.
 * 
 * @author rk903
 * 
 */
public class ConceptLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(ConceptLoader.class);
	IConceptDAO conceptDAO = null;
	boolean deleteOldDataFlag = false;

	public ConceptLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String sourceSystemCd,
			boolean deleteOldDataFlag, int uploadId) {
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setEncounterSource(encounterSource);
		setUploadId(uploadId);
		setSourceSystemCd(sourceSystemCd);
		this.deleteOldDataFlag = deleteOldDataFlag;
		conceptDAO = uploaderDaoFactory.getConceptDAO();
	}

	public void load() throws I2B2Exception {
		try {

			// update the status table
			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.CONCEPT_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);

			// set pdo xml file
			setOutputXmlFileName(getInputLoadFile());

			String tempConceptTableName = "TEMP_CONCEPT_" + getUploadId();
			setStagingTableName(tempConceptTableName);

			IConceptDAO conceptDAO = uploaderDaoFactory.getConceptDAO();
			conceptDAO.createTempTable(tempConceptTableName);
			log.info("Created Visit staging table" + getStagingTableName());

			ConceptXmlDbLoader conceptDbLoader = new ConceptXmlDbLoader(
					uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			conceptDbLoader.doUpload();
			log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());

			conceptDAO.createConceptFromTempTable(getStagingTableName(),
					getUploadId());
			log.info("Completed Concept insert operation for staging table"
					+ getStagingTableName());

			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.CONCEPT_SET);
			setStatus.setLoadStatus("COMPLETED");
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setLoadedRecord(conceptDAO
					.getRecordCountByUploadId(getUploadId()));
			// setStatus.setDeletedRecord(deletedRecord);
			setStatus.setNoOfRecord(conceptDbLoader.getRowCount());
			uploadStatusDao.updateUploadSetStatus(setStatus);

		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.CONCEPT_SET);
			setStatus.setLoadStatus("ERROR");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			i2b2Ex.printStackTrace(pw);
			setStatus.setMessage(sw.toString().substring(0, 500));
			uploadStatusDao.updateUploadSetStatus(setStatus);

			throw i2b2Ex;
		}

	}

	@Override
	public void createTempTable() throws I2B2Exception {
		// set pdo xml file
		setOutputXmlFileName(getInputLoadFile());

		String tempConceptTableName = "TEMP_CONCEPT_" + getUploadId();
		setStagingTableName(tempConceptTableName);

		conceptDAO.createTempTable(tempConceptTableName);
		log.info("Created Visit staging table" + getStagingTableName());

	}

	@Override
	public int loadTempTable() throws I2B2Exception {
		ConceptXmlDbLoader conceptDbLoader = new ConceptXmlDbLoader(
				uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		conceptDbLoader.doUpload();
		log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return conceptDbLoader.getRowCount();
	}

	@Override
	public int mergeTempTable() throws I2B2Exception {
		String backupConceptDimensionTableName = "cd_bkup_" + getUploadId();
		if (deleteOldDataFlag) {
			conceptDAO.backupAndSyncConceptDimensionTable(
					getStagingTableName(), backupConceptDimensionTableName,
					getUploadId());
		} else {
			conceptDAO.createConceptFromTempTable(getStagingTableName(),
					getUploadId());
		}
		log.info("Completed Concept insert operation for staging table"
				+ getStagingTableName());
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
				getUploadId(), UploadStatusDAO.CONCEPT_SET);
		setStatus.setLoadStatus("COMPLETED");
		setStatus.setEndDate(new Date(System.currentTimeMillis()));
		setStatus.setLoadedRecord(conceptDAO
				.getRecordCountByUploadId(getUploadId()));
		// setStatus.setDeletedRecord(deletedRecord);
		// ssetStatus.setNoOfRecord(conceptDbLoader.getRowCount());
		uploadStatusDao.updateUploadSetStatus(setStatus);
		return conceptDAO.getRecordCountByUploadId(getUploadId());
	}

	@Override
	public void setStartStatus() {
		// update the status table
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = new UploadSetStatus();
		setStatus.setUploadId(getUploadId());
		setStatus.setLoadStatus("STARTED");
		setStatus.setSetTypeId(UploadStatusDAO.CONCEPT_SET);
		setStatus.setInputFileName(this.getInputLoadFile());
		setStatus.setSourceCd(getSourceSystemCd());
		setStatus.setLoadDate(new Date(System.currentTimeMillis()));
		uploadStatusDao.insertUploadSetStatus(setStatus);

	}

}
