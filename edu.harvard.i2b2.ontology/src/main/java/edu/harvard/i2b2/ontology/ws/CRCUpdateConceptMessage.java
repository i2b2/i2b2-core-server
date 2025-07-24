/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.vdo.UpdateCrcConceptType;

/**
 * The AddChildDataMessage class is a helper class to build Ontology messages in
 * the i2b2 format
 */
public class CRCUpdateConceptMessage extends RequestDataMessage {

	public CRCUpdateConceptMessage(String requestVdo) throws I2B2Exception {
		super(requestVdo);
	}

	/**
	 * Function to get FolderType object from i2b2 request message type
	 * 
	 * @return
	 * @throws I2B2Exception
	 */
	public UpdateCrcConceptType getChild() throws I2B2Exception {
		UpdateCrcConceptType updateCrcConceptType = null;
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			updateCrcConceptType = (UpdateCrcConceptType) helper
					.getObjectByClass(bodyType.getAny(),
							UpdateCrcConceptType.class);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}
		return updateCrcConceptType;
	}

}
