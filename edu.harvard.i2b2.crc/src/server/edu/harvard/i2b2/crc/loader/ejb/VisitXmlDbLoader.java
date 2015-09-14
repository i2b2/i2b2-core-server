package edu.harvard.i2b2.crc.loader.ejb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IVisitDAO;
import edu.harvard.i2b2.crc.loader.dao.TempVisitDimensionInsertHandler;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

/**
 * This program loads visit dimension xml data into visit staging table. It uses
 * VisitXmlParser to fetch visit data and then loads into staging table.
 * 
 * @author rk903
 * 
 */
public class VisitXmlDbLoader implements StartElementListener {

	private String stagingVisitTableName = null;

	private int uploadId = 0;

	private String inputXmlFile = null;

	private TempVisitDimensionInsertHandler visitInsertHandler = null;

	private IUploaderDAOFactory uploaderDaoFactory = null;

	private int rowCount = 0;

	// log
	private static Log log = LogFactory.getLog(VisitXmlDbLoader.class);

	/**
	 * Constructor accepts 3 parameter.
	 * 
	 * @param inputXmlFile
	 *            input xml file with just visit information.
	 * @param stagingVisitTableName
	 *            staging visit_dimension table name
	 * @param uploadId
	 *            upload id
	 */
	public VisitXmlDbLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputXmlFile, String stagingVisitTableName, int uploadId)
			throws I2B2Exception {
		this.uploaderDaoFactory = uploaderDaoFactory;
		this.inputXmlFile = inputXmlFile;
		this.stagingVisitTableName = stagingVisitTableName;
		this.uploadId = uploadId;

	}

	/**
	 * Start parsing and upload process.
	 * 
	 * @throws I2B2Exception
	 */
	public void doUpload() throws I2B2Exception {

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(inputXmlFile);
		} catch (FileNotFoundException fileEx) {
			throw new I2B2Exception("File not found" + inputXmlFile, fileEx);
		}
		// call temp table batch inserter
		IVisitDAO visitDAO = uploaderDaoFactory.getVisitDAO();
		visitInsertHandler = visitDAO
				.createTempVisitDimensionInsert(stagingVisitTableName);

		TypePullParser parser = new TypePullParser(this, "event",
				EventType.class, inputXmlFile);
		parser.doParsing();
		visitInsertHandler.flush();
	}

	public void process(Object event) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading event [" + rowCount + "]");
			visitInsertHandler.flush();
		}
		String patientIde = null;
		String encounterIde = null;
		EventType eventType = (EventType) event;
		eventType.setUploadId(String.valueOf(uploadId));

		visitInsertHandler.insertVisitDimension((EventType) eventType);
	}

	public int getRowCount() {
		return rowCount;
	}

}
