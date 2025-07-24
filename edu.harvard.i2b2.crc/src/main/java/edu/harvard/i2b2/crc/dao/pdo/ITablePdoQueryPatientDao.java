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
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

public interface ITablePdoQueryPatientDao {

	/**
	 * Function returns Patient information for given list of patient number in
	 * TablePDO format
	 * 
	 * @param patientNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientByPatientNum(List<String> patientNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromPatientSet(PatientListType patientListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * Function returns patient information for given list of encounters
	 * 
	 * @param visitListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientSet
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromVisitSet(EventListType visitListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

	/**
	 * Get Patient set based on the fact's filter
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
	public PatientSet getPatientByFact(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException;
	
	
	public void setMetaDataParamList(List<ParamType> metaDataParamList);

}
