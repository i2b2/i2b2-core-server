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
