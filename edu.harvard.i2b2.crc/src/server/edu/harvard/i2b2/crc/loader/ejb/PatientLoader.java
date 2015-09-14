package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IPatientDAO;
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
public class PatientLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(PatientLoader.class);
	private IUploaderDAOFactory uploaderDaoFactory = null;
	IPatientDAO patientDAO = null;

	public PatientLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String sourceSystemCd, int uploadId) {
		this.uploaderDaoFactory = uploaderDaoFactory;
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setEncounterSource(encounterSource);
		setUploadId(uploadId);
		setSourceSystemCd(sourceSystemCd);
		patientDAO = uploaderDaoFactory.getPatientDAO();
	}

	public void load() throws I2B2Exception {
		try {

			// update the status table
			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.PATIENT_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date());
			uploadStatusDao.insertUploadSetStatus(setStatus);

			// set pdo xml file
			setOutputXmlFileName(getInputLoadFile());

			String tempPatientTableName = "TEMP_PATIENT_" + getUploadId();
			String tempPatientMappingTableName = "TEMP_PATIENT_MAP_"
					+ getUploadId();
			setStagingTableName(tempPatientTableName);

			IPatientDAO patientDAO = uploaderDaoFactory.getPatientDAO();
			patientDAO.createTempTable(getStagingTableName(),
					tempPatientMappingTableName);
			log.info("Created Patient staging table" + getStagingTableName());

			PatientXmlDbLoader patientDbLoader = new PatientXmlDbLoader(
					uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			patientDbLoader.doUpload();
			log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());

			patientDAO.createPatientFromTempTable(getStagingTableName(),
					tempPatientMappingTableName, getUploadId());
			log.info("Completed Patient insert operation for staging table"
					+ getStagingTableName());

			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.PATIENT_SET);
			setStatus.setLoadStatus("COMPLETED");
			uploadStatusDao.updateUploadSetStatus(setStatus);

		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.PATIENT_SET);
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
	public int loadTempTable() throws I2B2Exception {
		// set pdo xml file
		PatientXmlDbLoader patientDbLoader = new PatientXmlDbLoader(
				uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		patientDbLoader.doUpload();
		log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return patientDbLoader.getRowCount();

	}

	@Override
	public int mergeTempTable() throws I2B2Exception {

		patientDAO.createPatientFromTempTable(getStagingTableName(), null,
				getUploadId());
		log.info("Completed Patient insert operation for staging table"
				+ getStagingTableName());
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
				getUploadId(), UploadStatusDAO.PATIENT_SET);
		setStatus.setLoadedRecord(patientDAO
				.getRecordCountByUploadId(getUploadId()));
		setStatus.setLoadStatus("COMPLETED");
		uploadStatusDao.updateUploadSetStatus(setStatus);
		return patientDAO.getRecordCountByUploadId(getUploadId());
	}

	@Override
	public void setStartStatus() {
		// update the status table
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = new UploadSetStatus();
		setStatus.setUploadId(getUploadId());
		setStatus.setLoadStatus("STARTED");
		setStatus.setSetTypeId(UploadStatusDAO.PATIENT_SET);
		setStatus.setInputFileName(this.getInputLoadFile());
		setStatus.setSourceCd(getSourceSystemCd());
		setStatus.setLoadDate(new Date());
		uploadStatusDao.insertUploadSetStatus(setStatus);

	}

	@Override
	public void createTempTable() throws I2B2Exception {
		// set pdo xml file
		setOutputXmlFileName(getInputLoadFile());

		String tempPatientTableName = "TEMP_PATIENT_" + getUploadId();
		String tempPatientMappingTableName = "TEMP_PATIENT_MAP_"
				+ getUploadId();
		setStagingTableName(tempPatientTableName);

		patientDAO.createTempTable(getStagingTableName(),
				tempPatientMappingTableName);
		log.info("Created Patient staging table" + getStagingTableName());

	}

}
