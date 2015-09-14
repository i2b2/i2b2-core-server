package edu.harvard.i2b2.crc.loader.dao;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesConceptSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesModifierSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesObserverSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermSetReportType;

public interface IMissingTermDAO {

	public MissingCodesConceptSetType getMissingConceptSet(int uploadId, int startPos, int endPos, boolean detailFlag)
	throws I2B2DAOException ;
	public MissingCodesObserverSetType getMissingObserverSet(int uploadId, int startPos, int endPos, boolean detailFlag)
	throws I2B2DAOException ;
	public MissingCodesModifierSetType getMissingModifierSet(int uploadId, int startPos, int endPos, boolean detailFlag)
	throws I2B2DAOException ;
	public MissingTermSetReportType getMissingTermReport(int uploadId,
			String setName) throws I2B2DAOException;
	
}