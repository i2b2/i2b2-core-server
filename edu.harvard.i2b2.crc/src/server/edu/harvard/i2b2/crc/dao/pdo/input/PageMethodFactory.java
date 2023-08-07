/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
