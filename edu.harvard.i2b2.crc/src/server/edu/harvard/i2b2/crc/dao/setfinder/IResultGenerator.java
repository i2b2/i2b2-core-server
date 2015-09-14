package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface IResultGenerator {

	public void generateResult(Map param) throws CRCTimeOutException,
			I2B2DAOException;

}