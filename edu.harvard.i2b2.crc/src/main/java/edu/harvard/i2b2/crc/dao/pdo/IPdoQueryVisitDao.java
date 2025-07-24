/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

public interface IPdoQueryVisitDao {
	
	
	
	public void setMetaDataParamList(List<ParamType> metaDataParamList);

	/**
	 * Get Visit set based on the fact's filter
	 * 
	 * @param panelSqlList
	 * @param sqlParamCountList
	 * @param inputOptionListHandler
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;

	/**
	 * Function to return list of eventset for given encounter number list
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @exception I2B2DAOException
	 */
	public EventSet getVisitsByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * Get visit dimension data base on visit list
	 * (InputOptionList.getVisitListType())
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return I2B2DAOException
	 * @throws Exception
	 */
	public EventSet getVisitDimensionSetFromVisitList(
			EventListType visitListType, boolean detailFlag, boolean blobFlag,
			boolean statusFlag) throws I2B2DAOException;

	/**
	 * Get visit dimension from patientlist (InputOptionList.getPatientList())
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.VisitDimensionSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromPatientList(
			PatientListType patientListType, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;

}
