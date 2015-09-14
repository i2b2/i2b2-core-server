package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IEidDAO;
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
public class EidLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(EidLoader.class);
	private IUploaderDAOFactory uploaderDaoFactory = null;
	IEidDAO eidDAO = null;

	public EidLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String sourceSystemCd, int uploadId) {
		this.uploaderDaoFactory = uploaderDaoFactory;
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setEncounterSource(encounterSource);
		setUploadId(uploadId);
		setSourceSystemCd(sourceSystemCd);
		eidDAO = uploaderDaoFactory.getEidDAO();
	}

	public void load() throws I2B2Exception {
		UploadStatusDAOI uploadStatusDao = null;
		try {

			// update the status table
			uploadStatusDao = uploaderDaoFactory.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.EID_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);

			// set pdo xml file
			setOutputXmlFileName(getInputLoadFile());

			String tempPatientMappingTableName = "TEMP_EID_MAP_"
					+ getUploadId();
			setStagingTableName(tempPatientMappingTableName);

			IEidDAO eidDAO = uploaderDaoFactory.getEidDAO();
			eidDAO.createTempTable(getStagingTableName());
			log.info("Created EID staging table" + getStagingTableName());

			EidXmlDbLoader eidDbLoader = new EidXmlDbLoader(
					this.uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			eidDbLoader.doUpload();
			log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());

			eidDAO.createEidFromTempTable(tempPatientMappingTableName,
					getUploadId());
			log.info("Completed EID insert operation for staging table"
					+ getStagingTableName());

			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.EID_SET);
			setStatus.setLoadStatus("COMPLETED");
			setStatus.setLoadedRecord(eidDAO
					.getRecordCountByUploadId(getUploadId()));
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setNoOfRecord(eidDbLoader.getRowCount());
			uploadStatusDao.updateUploadSetStatus(setStatus);

		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.EID_SET);
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

		String tempPatientMappingTableName = "TEMP_EID_MAP_" + getUploadId();
		setStagingTableName(tempPatientMappingTableName);

		eidDAO.createTempTable(getStagingTableName());
		log.info("Created EID staging table" + getStagingTableName());

	}

	@Override
	public int loadTempTable() throws I2B2Exception {
		EidXmlDbLoader eidDbLoader = new EidXmlDbLoader(
				this.uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		eidDbLoader.doUpload();
		log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return eidDbLoader.getRowCount();

	}

	@Override
	public int mergeTempTable() throws I2B2Exception {
		eidDAO.createEidFromTempTable(getStagingTableName(), getUploadId());
		log.info("Completed EID insert operation for staging table"
				+ getStagingTableName());
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
				getUploadId(), UploadStatusDAO.EID_SET);
		setStatus.setLoadStatus("COMPLETED");
		setStatus.setLoadedRecord(eidDAO
				.getRecordCountByUploadId(getUploadId()));
		setStatus.setEndDate(new Date(System.currentTimeMillis()));
		// setStatus.setNoOfRecord(eidDbLoader.getRowCount());
		uploadStatusDao.updateUploadSetStatus(setStatus);
		return eidDAO.getRecordCountByUploadId(getUploadId());

	}

	@Override
	public void setStartStatus() {
		// update the status table
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = new UploadSetStatus();
		setStatus.setUploadId(getUploadId());
		setStatus.setLoadStatus("STARTED");
		setStatus.setSetTypeId(UploadStatusDAO.EID_SET);
		setStatus.setInputFileName(this.getInputLoadFile());
		setStatus.setSourceCd(getSourceSystemCd());
		setStatus.setLoadDate(new Date(System.currentTimeMillis()));
		uploadStatusDao.insertUploadSetStatus(setStatus);
	}

}
