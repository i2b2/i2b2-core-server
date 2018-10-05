/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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

	@Override
	public void process(Object observation) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading Observation [" + rowCount + "]");
			observationFactInsertHandle.flush();
		}
		ObservationType observationType = (ObservationType) observation;
		observationType.setUploadId(String.valueOf(uploadId));

		observationFactInsertHandle
				.insertObservationFact(observationType);
	}

	public int getRowCount() {
		return rowCount;
	}

}
