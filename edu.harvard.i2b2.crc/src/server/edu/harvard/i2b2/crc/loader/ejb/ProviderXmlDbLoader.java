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
import edu.harvard.i2b2.crc.loader.dao.IProviderDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.TempProviderInsertHandler;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

/**
 * This program loads visit dimension xml data into visit staging table. It uses
 * VisitXmlParser to fetch visit data and then loads into staging table.
 * 
 * @author rk903
 * 
 */
public class ProviderXmlDbLoader implements StartElementListener {

	private String stagingPidTableName = null;

	private int uploadId = 0;

	private String inputXmlFile = null;

	private TempProviderInsertHandler providerInsertHandler = null;

	private int rowCount = 0;

	// log
	private static Log log = LogFactory.getLog(ProviderXmlDbLoader.class);

	private IUploaderDAOFactory uploaderDaoFactory = null;

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
	public ProviderXmlDbLoader(IUploaderDAOFactory uploaderDaoFactory,
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

		// call temp table batch inserter
		IProviderDAO providerDAO = uploaderDaoFactory.getProviderDAO();
		;
		providerInsertHandler = providerDAO
				.createTempProviderInsert(stagingPidTableName);

		TypePullParser parser = new TypePullParser(this, "observer",
				ObserverType.class, inputXmlFile);
		parser.doParsing();
		providerInsertHandler.flush();
	}

	@Override
	public void process(Object provider) {
		rowCount++;
		if (rowCount % 1000 == 0) {
			log.debug("Loading observer [" + rowCount + "]");
			providerInsertHandler.flush();
		}
		providerInsertHandler.insertProvider((ObserverType) provider);
	}

	public int getRowCount() {
		return rowCount;
	}

}
