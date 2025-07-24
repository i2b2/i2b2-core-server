/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.im.util;

import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import edu.harvard.i2b2.im.datavo.pdo.PatientType;
import edu.harvard.i2b2.im.datavo.pdo.PidType;

public interface EMPI {

		String findPerson(String username, String source, String value ) throws Exception;
		void parse(PatientType ptype) throws Exception;
		void getIds(PidType newPidType) throws Exception;
	
}
