/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.ejb;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.loader.dao.ILoaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IMissingTermDAO;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.GetMissingTermRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesConceptSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesModifierSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesObserverSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermReportResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermReportResponseType.MissingCodes;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermReportResponseType.MissingTermReport;


public class MissingTermReportBean implements MissingTermReportBeanLocal,
		MissingTermReportBeanRemote {

	private DTOFactory dtoFactory = new DTOFactory();

	@Override
	public MissingTermReportResponseType getMissingTermReport(DataSourceLookup dataSourceLookup, GetMissingTermRequestType getMissingTermMessage) throws I2B2Exception {

		MissingTermReportResponseType missingTermReportResp = new MissingTermReportResponseType();
		LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
				dataSourceLookup.getDomainId(),
				dataSourceLookup.getProjectPath(),
				dataSourceLookup.getOwnerId());
		ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
		IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
				.getUpLoaderDAOFactory();
		IMissingTermDAO missingTermDao = uploaderDaoFactory.getMissingTermDAO();
		
		String uploadId = getMissingTermMessage.getUploadId();
		int uploadIdInt = 0;
		if (uploadId != null) { 
			uploadIdInt = Integer.parseInt(uploadId);
		}
		getMissingTermMessage.getConceptSet().isDetail();
		
		MissingCodes missingCodes = new MissingCodes();
		// call for concept
		if (getMissingTermMessage.getConceptSet() != null) { 
			MissingCodesConceptSetType missingConceptSet= missingTermDao
				.getMissingConceptSet(uploadIdInt, getMissingTermMessage.getConceptSet().getStart(), 
						getMissingTermMessage.getConceptSet().getStop(), getMissingTermMessage.getConceptSet().isDetail());
			missingCodes.setConceptSet(missingConceptSet);
			
		}
		
		// call for provider
		if (getMissingTermMessage.getObserverSet() != null) { 
			MissingCodesObserverSetType missingObserverSet= missingTermDao
			.getMissingObserverSet(uploadIdInt, getMissingTermMessage.getObserverSet().getStart(), getMissingTermMessage.getObserverSet().getStop(), getMissingTermMessage.getObserverSet().isDetail());
			missingCodes.setObserverSet(missingObserverSet);
			
		}
		
		// call for modifier
		if (getMissingTermMessage.getObserverSet() != null) { 
			MissingCodesModifierSetType missingModifierSet = missingTermDao
			.getMissingModifierSet(uploadIdInt, getMissingTermMessage.getModifierSet().getStart(), getMissingTermMessage.getModifierSet().getStop(), getMissingTermMessage.getModifierSet().isDetail());
			missingCodes.setModifierSet(missingModifierSet);
		}
		
		 // build response and return
		 missingTermReportResp.setMissingCodes(missingCodes);
	 
		 MissingTermReport missingTermReportType = new MissingTermReport();
		 missingTermReportType.setConceptSet(missingTermDao.getMissingTermReport(uploadIdInt, "concept_set"));
		 missingTermReportType.setModifierSet(missingTermDao.getMissingTermReport(uploadIdInt, "modifier_set"));
		 missingTermReportType.setObserverSet(missingTermDao.getMissingTermReport(uploadIdInt, "observer_set"));
		 
		 
		 missingTermReportResp.setMissingTermReport(missingTermReportType); 
		 
		return missingTermReportResp;
	}

}
