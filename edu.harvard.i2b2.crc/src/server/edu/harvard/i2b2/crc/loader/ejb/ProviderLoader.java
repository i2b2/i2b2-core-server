package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IProviderDAO;
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
public class ProviderLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(ProviderLoader.class);
	private IUploaderDAOFactory uploaderDaoFactory = null;
	IProviderDAO providerDAO = null;
	boolean deleteOldDataFlag = false;

	public ProviderLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String sourceSystemCd,
			boolean deleteOldDataFlag, int uploadId) {
		this.uploaderDaoFactory = uploaderDaoFactory;
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setEncounterSource(encounterSource);
		setUploadId(uploadId);
		setSourceSystemCd(sourceSystemCd);
		providerDAO = uploaderDaoFactory.getProviderDAO();
		this.deleteOldDataFlag = deleteOldDataFlag;
	}

	public void load() throws I2B2Exception {
		try {

			// update the status table
			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			;
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.OBSERVER_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);

			// set pdo xml file
			setOutputXmlFileName(getInputLoadFile());

			String tempPatientMappingTableName = "TEMP_PROVIDER_"
					+ getUploadId();
			setStagingTableName(tempPatientMappingTableName);

			IProviderDAO providerDAO = uploaderDaoFactory.getProviderDAO();
			providerDAO.createTempTable(getStagingTableName());
			log.info("Created Provider staging table" + getStagingTableName());

			ProviderXmlDbLoader providerDbLoader = new ProviderXmlDbLoader(
					uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			providerDbLoader.doUpload();
			log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());

			providerDAO.createProviderFromTempTable(
					tempPatientMappingTableName, getUploadId());
			log.info("Completed Provider insert operation for staging table"
					+ getStagingTableName());

			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.OBSERVER_SET);
			setStatus.setLoadStatus("COMPLETED");
			setStatus.setLoadedRecord(providerDAO
					.getRecordCountByUploadId(getUploadId()));
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setNoOfRecord(providerDbLoader.getRowCount());
			uploadStatusDao.updateUploadSetStatus(setStatus);

		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.OBSERVER_SET);
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

		String tempPatientMappingTableName = "TEMP_PROVIDER_" + getUploadId();
		setStagingTableName(tempPatientMappingTableName);

		providerDAO.createTempTable(getStagingTableName());
		log.info("Created Provider staging table" + getStagingTableName());

	}

	@Override
	public int loadTempTable() throws I2B2Exception {

		ProviderXmlDbLoader providerDbLoader = new ProviderXmlDbLoader(
				uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		providerDbLoader.doUpload();
		log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return providerDbLoader.getRowCount();

	}

	@Override
	public int mergeTempTable() throws I2B2Exception {
		String backupProviderDimensionTableName = "pd_bkup_" + getUploadId();
		if (deleteOldDataFlag) {
			providerDAO.backupAndSyncProviderDimensionTable(
					getStagingTableName(), backupProviderDimensionTableName,
					getUploadId());
		} else {
			providerDAO.createProviderFromTempTable(this.getStagingTableName(),
					getUploadId());
		}
		log.info("Completed Provider insert operation for staging table"
				+ getStagingTableName());
		return providerDAO.getRecordCountByUploadId(this.getUploadId());
	}

	@Override
	public void setStartStatus() {
		// TODO Auto-generated method stub

	}

}
