/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.ejb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.IPatientDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.TempPatientDimensionInsertHandler;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

/**
 * This program loads visit dimension xml data into visit staging table. It uses
 * VisitXmlParser to fetch visit data and then loads into staging table.
 * 
 * @author rk903
 * 
 */
public class PatientXmlDbLoader implements StartElementListener {

	private String stagingPatientTableName = null;

	private int uploadId = 0;

	private String inputXmlFile = null;

	private TempPatientDimensionInsertHandler patientInsertHandler = null;

	private IUploaderDAOFactory uploaderDaoFactory = null;

	private int rowCount = 0;

	// log
	private static Log log = LogFactory.getLog(PatientXmlDbLoader.class);

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
	public PatientXmlDbLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputXmlFile, String stagingPatientTableName, int uploadId)
			throws I2B2Exception {
		this.uploaderDaoFactory = uploaderDaoFactory;
		this.inputXmlFile = inputXmlFile;
		this.stagingPatientTableName = stagingPatientTableName;
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
		IPatientDAO patientDAO = uploaderDaoFactory.getPatientDAO();
		patientInsertHandler = patientDAO
				.createTempPatientDimensionInsert(stagingPatientTableName);

		TypePullParser parser = new TypePullParser(this, "patient",
				PatientType.class, inputXmlFile);
		parser.doParsing();
		patientInsertHandler.flush();
	}

	@Override
	public void process(Object patient) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading Patient [" + rowCount + "]");
			patientInsertHandler.flush();
		}
		PatientType patientType = (PatientType) patient;
		patientType.setUploadId(String.valueOf(uploadId));

		patientInsertHandler.insertPatientDimension(patientType);
	}

	public int getRowCount() {
		return rowCount;
	}

}
