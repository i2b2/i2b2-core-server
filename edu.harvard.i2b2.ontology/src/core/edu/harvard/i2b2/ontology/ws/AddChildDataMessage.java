/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
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
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifierType;



/**
 * The AddChildDataMessage class is a helper class to build Ontology messages in the
 * i2b2 format
 */
public class AddChildDataMessage extends RequestDataMessage{
    
	public AddChildDataMessage(String requestVdo) throws I2B2Exception {
		super(requestVdo);
	}
    
    /**
     * Function to get FolderType object from i2b2 request message type
     * @return
     * @throws I2B2Exception
     */
    public ConceptType getChild() throws I2B2Exception {
    	ConceptType addChildType;
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			addChildType = (ConceptType) helper.getObjectByClass(bodyType.getAny(),
					ConceptType.class);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}        
        return addChildType;
    }
    
    /**
     * Function to get ModifierType object from i2b2 request message type
     * @return
     * @throws I2B2Exception
     */
    public ModifierType getModifier() throws I2B2Exception {
    	ModifierType addChildType;
		try {
			BodyType bodyType = reqMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			addChildType = (ModifierType) helper.getObjectByClass(bodyType.getAny(),
					ModifierType.class);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Unwrap error: " + e.getMessage(), e);
		}        
        return addChildType;
    }
    

}
