package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IObservationFactDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAO;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataFormatType;
import edu.harvard.i2b2.crc.loader.util.csv.ObservationFactCSV2XmlBuilder;

/**
 * This specific loader class for ObservationFact dimension. This class performs
 * three operations, namely. 1.Build PatientData xml file based, if input file
 * is csv 2.Load PatientData xml information into staging area in database.
 * 3.Run procedure which will merge staging area into ObservationFact dimension.
 * 
 * @author rk903
 * 
 */
public class ObservationFactLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(ObservationFactLoader.class);

	private boolean appendFlag = false;
	private String conceptCodePrefix = null;
	private IUploaderDAOFactory uploaderDaoFactory = null;
	IObservationFactDAO obsFactDAO = null;

	public ObservationFactLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String conceptCodePrefix,
			String sourceSystemCd, boolean appendFlag, int uploadId) {
		this.uploaderDaoFactory = uploaderDaoFactory;
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setUploadId(uploadId);
		setEncounterSource(encounterSource);
		setSourceSystemCd(sourceSystemCd);
		setConceptCodePrefix(conceptCodePrefix);
		setAppendFlag(appendFlag);
	}

	public void load() throws I2B2Exception {

		try {
			// update the status table
			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.OBSERVATION_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);

			if (getInputLoadFileFormat().equalsIgnoreCase(
					DataFormatType.CSV.value())) {
				String obsFactOutputXmlFileName = DataMartLoader.loaderWorkingDirectory
						+ "OBSERVATIONFACT_" + getUploadId();
				setOutputXmlFileName(obsFactOutputXmlFileName);
				log
						.debug("Building observation fact xml file "
								+ getOutputXmlFileName() + " for "
								+ getInputLoadFile());
				ObservationFactCSV2XmlBuilder obsFactXmlBuilder = new ObservationFactCSV2XmlBuilder(
						getInputLoadFile(), getOutputXmlFileName());
				obsFactXmlBuilder.buildXml();
			} else {
				setOutputXmlFileName(getInputLoadFile());
			}

			String stagingObsFactTableName = "TEMP_OBSFACT_" + getUploadId();
			setStagingTableName(stagingObsFactTableName);

			IObservationFactDAO obsFactDAO = uploaderDaoFactory
					.getObservationDAO();
			obsFactDAO.createTempTable(getStagingTableName());
			log.debug("Created Observation fact staging table"
					+ getStagingTableName());

			ObservationFactXmlDbLoader obsFactDbLoader = new ObservationFactXmlDbLoader(
					this.uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			obsFactDbLoader.doUpload();
			log.debug("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());

			obsFactDAO.doTempTableMerge(getStagingTableName(), getUploadId(),
					getAppendFlag());
			log
					.debug("Completed ObservationFact merge operation for staging table"
							+ getStagingTableName());

			log
					.debug("ObservationFactLoader.load: updating the upload set status");

			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.OBSERVATION_SET);
			setStatus.setLoadStatus("COMPLETED");
			setStatus.setLoadedRecord(obsFactDAO
					.getRecordCountByUploadId(getUploadId()));
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setNoOfRecord(obsFactDbLoader.getRowCount());
			uploadStatusDao.updateUploadSetStatus(setStatus);
		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.OBSERVATION_SET);
			setStatus.setLoadStatus("ERROR");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			i2b2Ex.printStackTrace(pw);
			setStatus.setMessage(sw.toString().substring(0, 400));
			uploadStatusDao.updateUploadSetStatus(setStatus);

			throw i2b2Ex;
		}
	}

	public String getConceptCodePrefix() {
		return conceptCodePrefix;
	}

	public void setConceptCodePrefix(String conceptCodePrefix) {
		this.conceptCodePrefix = conceptCodePrefix;
	}

	public boolean getAppendFlag() {
		return appendFlag;
	}

	public void setAppendFlag(boolean appendFlag) {
		this.appendFlag = appendFlag;
	}

	@Override
	public void createTempTable() throws I2B2Exception {
		String stagingObsFactTableName = "TEMP_OBSFACT_" + getUploadId();
		setStagingTableName(stagingObsFactTableName);

		obsFactDAO = uploaderDaoFactory.getObservationDAO();
		obsFactDAO.createTempTable(getStagingTableName());
		log.debug("Created Observation fact staging table"
				+ getStagingTableName());

	}

	@Override
	public int loadTempTable() throws I2B2Exception {
		setOutputXmlFileName(getInputLoadFile());
		ObservationFactXmlDbLoader obsFactDbLoader = new ObservationFactXmlDbLoader(
				this.uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		obsFactDbLoader.doUpload();
		log.debug("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return obsFactDbLoader.getRowCount();
	}

	@Override
	public int mergeTempTable() throws I2B2Exception {
		obsFactDAO.doTempTableMerge(getStagingTableName(), getUploadId(),
				getAppendFlag());
		log.debug("Completed ObservationFact merge operation for staging table"
				+ getStagingTableName());
		return obsFactDAO.getRecordCountByUploadId(getUploadId());
	}

	@Override
	public void setStartStatus() {
		// TODO Auto-generated method stub

	}

}
