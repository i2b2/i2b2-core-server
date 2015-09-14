package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IVisitDAO;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAO;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataFormatType;
import edu.harvard.i2b2.crc.loader.util.csv.VisitCSV2XmlBuilder;

/**
 * This specific loader class for Visit dimension. This class performs three
 * operations, namely. 1.Build PatientData xml file based, if input file is csv
 * 2.Load PatientData xml information into staging area in database. 3.Run
 * procedure which will merge staging area into Visit dimension.
 * 
 * @author rk903
 * 
 */
public class VisitLoader extends AbstractDimensionLoader {

	private static Log log = LogFactory.getLog(VisitLoader.class);
	private IUploaderDAOFactory uploaderDaoFactory = null;
	IVisitDAO visitDAO = null;

	public VisitLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputLoadFile, String inputLoadFileFormat,
			String encounterSource, String sourceSystemCd, int uploadId) {
		this.uploaderDaoFactory = uploaderDaoFactory;
		setUploaderDaoFactory(uploaderDaoFactory);
		setInputLoadFile(inputLoadFile);
		setInputLoadFileFormat(inputLoadFileFormat);
		setEncounterSource(encounterSource);
		setUploadId(uploadId);
		setSourceSystemCd(sourceSystemCd);
		visitDAO = uploaderDaoFactory.getVisitDAO();
	}

	public void load() throws I2B2Exception {
		try {

			// update the status table
			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(getUploadId());
			setStatus.setLoadStatus("STARTED");
			setStatus.setSetTypeId(UploadStatusDAO.EVENT_SET);
			setStatus.setInputFileName(this.getInputLoadFile());
			setStatus.setSourceCd(getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);

			if (this.getInputLoadFileFormat().equalsIgnoreCase(
					DataFormatType.CSV.value())) {
				String visitOutputXmlFileName = DataMartLoader.loaderWorkingDirectory
						+ "VISIT_" + getUploadId();
				setOutputXmlFileName(visitOutputXmlFileName);
				log.info("Building Visit xml file " + getOutputXmlFileName()
						+ " for " + getInputLoadFile());
				VisitCSV2XmlBuilder visitBuilder = new VisitCSV2XmlBuilder(
						getInputLoadFile(), getOutputXmlFileName());
				visitBuilder.buildXml();
			} else {
				setOutputXmlFileName(getInputLoadFile());
			}

			String tempVisitTableName = "TEMP_VISIT_" + getUploadId();
			setStagingTableName(tempVisitTableName);

			IVisitDAO visitDAO = uploaderDaoFactory.getVisitDAO();
			visitDAO.createTempTable(getStagingTableName());
			log.info("Created Visit staging table" + getStagingTableName());

			VisitXmlDbLoader visitDbLoader = new VisitXmlDbLoader(
					uploaderDaoFactory, getOutputXmlFileName(),
					getStagingTableName(), getUploadId());
			visitDbLoader.doUpload();
			log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
					+ getStagingTableName());

			visitDAO.createVisitFromTempTable(getStagingTableName(),
					getUploadId());
			log.info("Completed Visit insert operation for staging table"
					+ getStagingTableName());

			setStatus = uploadStatusDao.getUploadSetStatus(getUploadId(),
					UploadStatusDAO.EVENT_SET);
			setStatus.setLoadStatus("COMPLETED");
			setStatus.setLoadedRecord(visitDAO
					.getRecordCountByUploadId(getUploadId()));
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setNoOfRecord(visitDbLoader.getRowCount());
			uploadStatusDao.updateUploadSetStatus(setStatus);

		} catch (I2B2Exception i2b2Ex) {
			// try to write error to set status table, if that fails then simply
			// pass the exception to top level

			UploadStatusDAOI uploadStatusDao = uploaderDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = uploadStatusDao.getUploadSetStatus(
					getUploadId(), UploadStatusDAO.EVENT_SET);
			setStatus.setLoadStatus("ERROR");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			i2b2Ex.printStackTrace(pw);
			setStatus.setMessage(sw.toString().substring(0, 400));
			uploadStatusDao.updateUploadSetStatus(setStatus);

			throw i2b2Ex;
		}

	}

	@Override
	public void createTempTable() throws I2B2Exception {
		String tempVisitTableName = "TEMP_VISIT_" + getUploadId();
		setStagingTableName(tempVisitTableName);

		visitDAO.createTempTable(getStagingTableName());
		log.info("Created Visit staging table" + getStagingTableName());

	}

	@Override
	public int loadTempTable() throws I2B2Exception {
		setOutputXmlFileName(getInputLoadFile());
		VisitXmlDbLoader visitDbLoader = new VisitXmlDbLoader(
				uploaderDaoFactory, getOutputXmlFileName(),
				getStagingTableName(), getUploadId());
		visitDbLoader.doUpload();
		log.info("Uploaded " + getOutputXmlFileName() + "to staging table"
				+ getStagingTableName());
		return visitDbLoader.getRowCount();
	}

	@Override
	public int mergeTempTable() throws I2B2Exception {
		visitDAO.createVisitFromTempTable(getStagingTableName(), getUploadId());
		log.info("Completed Visit insert operation for staging table"
				+ getStagingTableName());
		return visitDAO.getRecordCountByUploadId(getUploadId());
	}

	@Override
	public void setStartStatus() {
		// TODO Auto-generated method stub

	}

}
