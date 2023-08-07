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

public class UploadDAOFactory implements IUploaderDAOFactory {

	@Override
	public ConceptDAO getConceptDAO() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ModifierDAO getModifierDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservationFactDAO getObservationDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PatientDAO getPatientDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PidDAO getPidDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProviderDAO getProviderDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UploadStatusDAO getUploadStatusDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VisitDAO getVisitDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSourceLookup getDataSourceLookup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		// TODO Auto-generated method stub

	}

	@Override
	public IEidDAO getEidDAO() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IMissingTermDAO getMissingTermDAO() {
		// TODO Auto-generated method stub
		return null;
	}

}
