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
import edu.harvard.i2b2.crc.loader.dao.IConceptDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.TempConceptInsertHandler;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.loader.util.CryptUtil;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

/**
 * This program loads visit dimension xml data into visit staging table. It uses
 * VisitXmlParser to fetch visit data and then loads into staging table.
 * 
 * @author rk903
 * 
 */
public class ConceptXmlDbLoader implements StartElementListener {

	private String stagingConceptTableName = null;

	private int uploadId = 0;

	private String inputXmlFile = null;

	private TempConceptInsertHandler conceptInsertHandler = null;

	private CryptUtil cryptUtil = null;

	private int rowCount = 0;

	private IUploaderDAOFactory uploaderDaoFactory = null;

	// log
	private static Log log = LogFactory.getLog(ConceptXmlDbLoader.class);

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
	public ConceptXmlDbLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputXmlFile, String stagingConceptTableName, int uploadId)
			throws I2B2Exception {
		this.uploaderDaoFactory = uploaderDaoFactory;
		this.inputXmlFile = inputXmlFile;
		this.stagingConceptTableName = stagingConceptTableName;
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
		IConceptDAO conceptDAO = uploaderDaoFactory.getConceptDAO();
		conceptInsertHandler = conceptDAO
				.createTempConceptInsert(stagingConceptTableName);

		TypePullParser parser = new TypePullParser(this, "concept",
				ConceptType.class, inputXmlFile);
		parser.doParsing();
		conceptInsertHandler.flush();
	}

	@Override
	public void process(Object pid) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading Concept [" + rowCount + "]");
			conceptInsertHandler.flush();
		}
		ConceptType conceptType = (ConceptType) pid;
		conceptInsertHandler.insertConcept(conceptType);
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

}
