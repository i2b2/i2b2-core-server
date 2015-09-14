/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.fr.ws;

import static org.junit.Assert.*;


import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.fr.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.fr.util.FRUtil;

public class FRServiceRESTTest extends FRAxisAbstract{
	private static String testFileDir = "";

	private static String ontologyTargetEPR = 
			"http://localhost:9090/i2b2/rest/OntologyService/getSchemes";			
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(FRServiceRESTTest.class);
	}


	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "test"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}


}





