/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao;

import javax.sql.DataSource;

import edu.harvard.i2b2.crc.dao.pdo.IMetadataDao;
import edu.harvard.i2b2.crc.dao.pdo.IObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.IPageDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryEidDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryModifierDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryPidDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryVisitDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryModifierDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryVisitDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public interface PatientDataDAOFactory {
	public IObservationFactDao getObservationFactDAO();

	public IPdoQueryConceptDao getPdoQueryConceptDAO();
	
	public IPdoQueryModifierDao getPdoQueryModifierDAO();
	
	public IPdoQueryPatientDao getPdoQueryPatientDAO();

	public IPdoQueryProviderDao getPdoQueryProviderDAO();

	public IPdoQueryVisitDao getPdoQueryVisitDAO();

	public ITablePdoQueryConceptDao getTablePdoQueryConceptDAO();
	
	public ITablePdoQueryModifierDao getTablePdoQueryModifierDAO();
	
	public ITablePdoQueryPatientDao getTablePdoQueryPatientDAO();

	public ITablePdoQueryProviderDao getTablePdoQueryProviderDAO();

	public ITablePdoQueryVisitDao getTablePdoQueryVisitDAO();

	public IPageDao getPageDAO();

	public IPdoQueryPidDao getPdoQueryPidDAO();

	public IPdoQueryEidDao getPdoQueryEidDAO();
	
	public IMetadataDao getMetadataDAO();

	public DataSourceLookup getDataSourceLookup();

	public DataSourceLookup getOriginalDataSourceLookup();
	
	public DataSource getDataSource();
}
