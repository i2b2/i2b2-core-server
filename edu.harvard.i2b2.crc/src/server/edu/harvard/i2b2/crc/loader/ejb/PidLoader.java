package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IPidDAO;
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
public class PidLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(PidLoader.class);
	private IUploaderDAOFactory uploaderDaoFactory = null;

	public PidLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String sourceSystemCd, int uploadId) {
		this.uploaderDaoFactory = uploaderDaoFactory;
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setEncounterSource(encounterSource);
		setUploadId(uploadId);
		setSourceSystemCd(sourceSystemCd);

	}

	public void setStartStatus() {
		// update the status table
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		UploadSetStatus setStatus = new UploadSetStatus();
		setStatus.setUploadId(getUploadId());
		setStatus.setLoadStatus("STARTED");
		setStatus.setSetTypeId(UploadStatusDAO.PID_SET);
		setStatus.setInputFileName(this.getInputLoadFile());
		setStatus.setSourceCd(getSourceSystemCd());
		setStatus.setLoadDate(new Date(System.currentTimeMillis()));
		uploadStatusDao.insertUploadSetStatus(setStatus);
	}

	@Override
	public void createTempTable() throws I2B2Exception {
		setOutputXmlFileName(getInputLoadFile());

		String tempPatientMappingTableName = "TEMP_PID_MAP_" + getUploadId();
		setStagingTableName(tempPatientMappingTableName);

		IPidDAO pidDAO = uploaderDaoFactory.getPidDAO();
		pidDAO.createTempTable(getStagingTableName());
		log.info("Created PID staging table" + getStagingTableName());

	}

	public int loadTempTable() throws I2B2Exception {

		PidXmlDbLoader pidDbLoader = new PidXmlDbLoader(
				this.uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		pidDbLoader.doUpload();
		log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return pidDbLoader.getRowCount();
	}

	public int mergeTempTable() throws I2B2Exception {
		IPidDAO pidDAO = uploaderDaoFactory.getPidDAO();
		pidDAO
				.createPidFromTempTable(this.getStagingTableName(),
						getUploadId());
		log.info("Completed PID insert operation for staging table"
				+ getStagingTableName());

		UploadSetStatus setStatus = new UploadSetStatus();
		setStatus.setUploadId(getUploadId());
		setStatus.setLoadStatus("STARTED");
		setStatus.setSetTypeId(UploadStatusDAO.PID_SET);
		setStatus.setInputFileName(this.getInputLoadFile());
		setStatus.setSourceCd(getSourceSystemCd());
		setStatus.setLoadDate(new Date(System.currentTimeMillis()));
		UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
				.getUploadStatusDAO();
		setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
				UploadStatusDAO.PID_SET);
		setStatus.setLoadStatus("COMPLETED");
		setStatus.setLoadedRecord(pidDAO
				.getRecordCountByUploadId(getUploadId()));
		setStatus.setEndDate(new Date(System.currentTimeMillis()));
		setStatus.setNoOfRecord(10000);
		uploadStatusDao.updateUploadSetStatus(setStatus);
		return pidDAO.getRecordCountByUploadId(getUploadId());

	}

	public void load() throws I2B2Exception {
		UploadStatusDAOI uploadStatusDao = null;
		InitialContext context = null;
		try {


			// update the status table
			uploadStatusDao = uploaderDaoFactory.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.PID_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);

			// set pdo xml file
			setOutputXmlFileName(getInputLoadFile());

			String tempPatientMappingTableName = "TEMP_PID_MAP_"
					+ getUploadId();
			setStagingTableName(tempPatientMappingTableName);
			IPidDAO pidDAO = uploaderDaoFactory.getPidDAO();
			pidDAO.createTempTable(getStagingTableName());
			log.info("Created PID staging table" + getStagingTableName());
			PidXmlDbLoader pidDbLoader = new PidXmlDbLoader(
					this.uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			pidDbLoader.doUpload();
			log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());
			pidDAO.createPidFromTempTable(tempPatientMappingTableName,
					getUploadId());
			log.info("Completed PID insert operation for staging table"
					+ getStagingTableName());


			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.PID_SET);
			setStatus.setLoadStatus("COMPLETED");
			setStatus.setLoadedRecord(pidDAO
					.getRecordCountByUploadId(getUploadId()));
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setNoOfRecord(pidDbLoader.getRowCount());
			uploadStatusDao.updateUploadSetStatus(setStatus);

		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.PID_SET);
			setStatus.setLoadStatus("ERROR");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			i2b2Ex.printStackTrace(pw);
			setStatus.setMessage(sw.toString().substring(0, 500));
			uploadStatusDao.updateUploadSetStatus(setStatus);

			throw i2b2Ex;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
