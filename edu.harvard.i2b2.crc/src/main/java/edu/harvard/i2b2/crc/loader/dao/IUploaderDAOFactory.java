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

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public interface IUploaderDAOFactory {
	public DataSourceLookup getDataSourceLookup();

	public IConceptDAO getConceptDAO();
	
	public IModifierDAO getModifierDAO();

	public IPatientDAO getPatientDAO();

	public IPidDAO getPidDAO();

	public IEidDAO getEidDAO();

	public IObservationFactDAO getObservationDAO();

	public UploadStatusDAOI getUploadStatusDAO();

	public IProviderDAO getProviderDAO();

	public IVisitDAO getVisitDAO();
	
	public IMissingTermDAO getMissingTermDAO();

	public DataSource getDataSource();

	public void setDataSource(DataSource dataSource);

}
