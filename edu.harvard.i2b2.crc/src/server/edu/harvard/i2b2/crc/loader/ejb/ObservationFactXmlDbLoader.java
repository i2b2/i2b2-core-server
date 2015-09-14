package edu.harvard.i2b2.crc.loader.ejb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IObservationFactDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.ObservationFactInsertHandle;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

/**
 * This program loads observation fact's data from xml file to staging temp
 * table.
 * 
 * @author rk903
 * 
 */
public class ObservationFactXmlDbLoader implements StartElementListener {

	private ObservationFactInsertHandle observationFactInsertHandle = null;

	private int uploadId = 0;

	private String inputXmlFile = null;

	private String stagingObservationFactTableName = null;

	private int rowCount = 0;

	private IUploaderDAOFactory uploaderDaoFactory = null;

	// log
	private static Log log = LogFactory
			.getLog(ObservationFactXmlDbLoader.class);

	/**
	 * Constuctor
	 * 
	 * @param inputXmlFile
	 * @param stagingObservationFactTableName
	 * @param uploadId
	 */
	public ObservationFactXmlDbLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputXmlFile, String stagingObservationFactTableName,
			int uploadId) throws I2B2Exception {
		this.uploaderDaoFactory = uploaderDaoFactory;
		this.inputXmlFile = inputXmlFile;
		this.stagingObservationFactTableName = stagingObservationFactTableName;
		this.uploadId = uploadId;

	}

	/**
	 * Start upload process.
	 * 
	 * @throws I2B2Exception
	 */
	public void doUpload() throws I2B2Exception {

		// call temp table batch inserter
		IObservationFactDAO observationFactDAO = uploaderDaoFactory
				.getObservationDAO();
		observationFactInsertHandle = observationFactDAO
				.createObservationFactInserter(stagingObservationFactTableName);

		TypePullParser parser = new TypePullParser(this, "observation",
				ObservationType.class, inputXmlFile);
		parser.doParsing();
		observationFactInsertHandle.flush();
	}

	public void process(Object observation) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading Observation [" + rowCount + "]");
			observationFactInsertHandle.flush();
		}
		ObservationType observationType = (ObservationType) observation;
		observationType.setUploadId(String.valueOf(uploadId));

		observationFactInsertHandle
				.insertObservationFact((ObservationType) observationType);
	}

	public int getRowCount() {
		return rowCount;
	}

}
