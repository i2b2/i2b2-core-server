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
import edu.harvard.i2b2.crc.loader.dao.IEidDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.TempEidInsertHandler;
import edu.harvard.i2b2.crc.datavo.pdo.EidType;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

/**
 * This program loads visit dimension xml data into visit staging table. It uses
 * VisitXmlParser to fetch visit data and then loads into staging table.
 * 
 * @author rk903
 * 
 */
public class EidXmlDbLoader implements StartElementListener {

	private String stagingPidTableName = null;

	private int uploadId = 0;

	private String inputXmlFile = null;

	private TempEidInsertHandler eidInsertHandler = null;

	private IUploaderDAOFactory uploaderDaoFactory = null;

	private int rowCount = 0;

	// log
	private static Log log = LogFactory.getLog(EidXmlDbLoader.class);

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
	public EidXmlDbLoader(IUploaderDAOFactory uploaderDaoFactory,
			String inputXmlFile, String stagingPidTableName, int uploadId)
			throws I2B2Exception {
		this.uploaderDaoFactory = uploaderDaoFactory;
		this.inputXmlFile = inputXmlFile;
		this.stagingPidTableName = stagingPidTableName;
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
		IEidDAO eidDAO = this.uploaderDaoFactory.getEidDAO();
		eidInsertHandler = eidDAO.createTempEidInsert(stagingPidTableName);

		TypePullParser parser = new TypePullParser(this, "eid", EidType.class,
				inputXmlFile);
		parser.doParsing();
		eidInsertHandler.flush();
	}

	@Override
	public void process(Object eid) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading EID [" + rowCount + "]");
			eidInsertHandler.flush();
		}
		EidType eidType = (EidType) eid;

		eidInsertHandler.insertEid(eidType);
	}

	public int getRowCount() {
		return rowCount;
	}

}
