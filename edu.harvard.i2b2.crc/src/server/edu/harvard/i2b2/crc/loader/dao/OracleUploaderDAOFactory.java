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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public class OracleUploaderDAOFactory implements IUploaderDAOFactory {

	private DataSourceLookup dataSourceLookup = null;
	private DataSource dataSource = null;

	/** log **/
	protected final static Log log = LogFactory
			.getLog(OracleUploaderDAOFactory.class);

	public OracleUploaderDAOFactory(DataSourceLookup dataSourceLookup)
			throws I2B2Exception {
		this.dataSourceLookup = dataSourceLookup;
		String dataSourceName = dataSourceLookup.getDataSource();
		log.info("Using datasource " + dataSourceName);
		try {
			// dataSource = (DataSource)
			// crcUtil.getSpringDataSource(dataSourceName);
			dataSource = ServiceLocator.getInstance().getAppServerDataSource(
					dataSourceName);
		} catch (I2B2Exception i2b2Ex) {
			log.error(i2b2Ex);
			throw new I2B2DAOException(
					"Error getting application/spring datasource "
							+ dataSourceName + " : " + i2b2Ex.getMessage(),
					i2b2Ex);
		}
	}

	/**
	 * Constructor for testing, datasource can be passed as parameter.
	 * 
	 * @param dataSourceLookup
	 * @param dataSource
	 */
	public OracleUploaderDAOFactory(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		this.dataSource = dataSource;
	}

	@Override
	public IConceptDAO getConceptDAO() {
		return new ConceptDAO(dataSourceLookup, dataSource);
	}
	
	@Override
	public IModifierDAO getModifierDAO() {
		return new ModifierDAO(dataSourceLookup, dataSource);
	}

	@Override
	public IObservationFactDAO getObservationDAO() {
		return new ObservationFactDAO(dataSourceLookup, dataSource);
	}

	@Override
	public IPatientDAO getPatientDAO() {
		return new PatientDAO(dataSourceLookup, dataSource);
	}

	@Override
	public IPidDAO getPidDAO() {
		return new PidDAO(dataSourceLookup, dataSource);
	}

	@Override
	public IEidDAO getEidDAO() {
		return new EidDAO(dataSourceLookup, dataSource);
	}

	@Override
	public IProviderDAO getProviderDAO() {
		return new ProviderDAO(dataSourceLookup, dataSource);
	}

	@Override
	public UploadStatusDAOI getUploadStatusDAO() {
		return new UploadStatusDAO(dataSourceLookup, dataSource);
	}

	@Override
	public IVisitDAO getVisitDAO() {
		return new VisitDAO(dataSourceLookup, dataSource);
	}


	@Override
	public IMissingTermDAO getMissingTermDAO() {
		return new MissingTermDAO(dataSourceLookup, dataSource);
	}


	@Override
	public DataSourceLookup getDataSourceLookup() {
		// TODO Auto-generated method stub
		return dataSourceLookup;
	}

	@Override
	public DataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;

	}

}
