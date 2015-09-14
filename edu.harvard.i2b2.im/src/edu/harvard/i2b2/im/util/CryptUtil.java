/*
 * Copyright (c) 2006-2009 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *  
 *     
 */
package edu.harvard.i2b2.im.util;


import edu.harvard.i2b2.common.exception.I2B2Exception;


/**
 * This is convenient class to do encryption and decryption of empi, encounter
 * and notes fields.
 * 
 * It uses same key for empi and encounter field and for notes it uses seperate
 * key.
 * 
 * 
 */
public class CryptUtil {
	private HighEncryption empiHighEnc = null;
	// private HighEncryption notesHighEnc = null;

	/**
	 * Default note key.
	 */
	private String notesKey = " ";

	/**
	 * Default constructor Uses default keys for empi, encounter and notes.
	 */
	public CryptUtil() {
		initHighEncrypt();
	}

	/**
	 * Constructor to accept only notes key. Default key is used for empi and
	 * encounter encryption.
	 * 
	 * @param notesKey
	 */
	public CryptUtil(String notesKey) {
		this.notesKey = notesKey;
		initHighEncrypt();
	}

	/**
	 * Initialize HighEncryption variable for empi and notes.
	 */
	private void initHighEncrypt() {
		try {
			//Hashtable<String, String> hashNotestemp = new Hashtable<String, String>();
			//hashNotestemp.put("A:\\I401.txt", notesKey);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return encrypted encounter ide
	 * 
	 * @param encounterIde
	 * @return
	 */
	public String encryptEncounterIde(String encounterIde) {
		return null;// empiHighEnc.generic_encrypt(encounterIde);
	}

	/**
	 * Return encrypted patient ide.
	 * 
	 * @param patientIde
	 * @return
	 */
	public String encryptPatientIde(String patientIde) {
		String encryptPatientIde = empiHighEnc.mrn_encrypt(patientIde, true,
				"EMPI");
		if (encryptPatientIde != null && encryptPatientIde.trim().length() > 0) {
			encryptPatientIde = '(' + encryptPatientIde;
		}
		return encryptPatientIde;
	}

	/**
	 * Decrypt encounter ide.
	 * 
	 * @param encryptEncounterIde
	 * @return
	 * @throws I2B2Exception 
	 */
	public String decryptEncounterIde(String encryptEncounterIde) throws I2B2Exception {
		return empiHighEnc.generic_decrypt(encryptEncounterIde);
	}

	/**
	 * Decrypt patient ide.
	 * 
	 * @param encryptPatientIde
	 * @return
	 */
	public String decryptPatientIde(String encryptPatientIde) {
		return empiHighEnc.mrn_decrypt(encryptPatientIde, true);
	}

	/**
	 * Encrypt notes using notes key
	 * 
	 * @param notes
	 * @return
	 * @throws I2B2Exception 
	 */
	public String encryptNotes(String notes) throws I2B2Exception {
		return empiHighEnc.generic_encrypt(notes);
	}

	/**
	 * Decrypt notes with notes key.
	 * 
	 * @param encrypted
	 *            notes
	 * @return
	 * @throws I2B2Exception 
	 */
	public String decryptNotes(String encryptedNotes) throws I2B2Exception {
		return empiHighEnc.generic_decrypt(encryptedNotes);
	}

}
