/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class TempConceptInsertHandler extends JdbcDaoSupport {
	private ConceptDAO.TempConceptInsert tempConceptInsert = null;

	public TempConceptInsertHandler(DataSource ds) {
		setDataSource(ds);
	}

	public TempConceptInsertHandler(
			ConceptDAO.TempConceptInsert tempConceptInsert) {
		this.tempConceptInsert = tempConceptInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertConcept(ConceptType concept) {
		tempConceptInsert.insert(concept);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		tempConceptInsert.flush();
		tempConceptInsert.reset();
	}

}
