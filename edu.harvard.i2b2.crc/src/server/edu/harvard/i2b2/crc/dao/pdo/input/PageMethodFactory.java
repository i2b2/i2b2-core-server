package edu.harvard.i2b2.crc.dao.pdo.input;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

/**
 * Factory class to return
 * 
 * @author rkuttan
 */
public class PageMethodFactory {

	public final static String AVERAGE_OBSERVATION_METHOD = "AVERAGE_OBSERVATION_METHOD";
	public final static String SUBDIVIDE_INPUT_METHOD = "SUBDIVIDE_INPUT_METHOD";

	public static PageMethod buildPageMethod(String pageMethodName)
			throws I2B2DAOException {
		PageMethod pageMethod = null;
		if (pageMethodName.trim().equalsIgnoreCase(AVERAGE_OBSERVATION_METHOD)) {
			pageMethod = new AverageObservationPageMethod();
		} else if (pageMethodName.trim().equalsIgnoreCase(
				SUBDIVIDE_INPUT_METHOD)) {
			pageMethod = new SubDividePageMethod();
		} else {
			throw new I2B2DAOException("Could not find page method for ["
					+ pageMethodName + "]");
		}
		return pageMethod;
	}
}
