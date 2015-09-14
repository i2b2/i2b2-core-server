package edu.harvard.i2b2.crc.loader.util;

import java.util.Hashtable;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.util.security.HighEncryption;

/**
 * This is convenient class to do encryption and decryption of empi, encounter
 * and notes fields.
 * 
 * It uses single key for empi and encounter field. 
 * And for notes it uses seperate key.
 * 
 * 
 * @author rk903
 */
public class CryptUtil {
	
	private HighEncryption empiHighEnc = null;

	private HighEncryption notesHighEnc = null;

	
	private String empiKey = null;

	private String notesKey = null;


	/**
	 * Constructor to accept empi and  notes key. 
	 * 
	 * @param notesKey
	 */
	public CryptUtil(String empiKey, String notesKey) {
		this.empiKey = empiKey;
		this.notesKey = notesKey;
		initHighEncrypt();
	}

	
	/**
	 * Initialize HighEncryption variable for empi and notes.
	 */
	private void initHighEncrypt() {
		try {
			// init high encryption with empikey
			Hashtable<String, String> hashEmpiTemp = new Hashtable<String, String>();
			hashEmpiTemp.put("A:\\I401.txt", empiKey);
			empiHighEnc = new HighEncryption("A:\\I401.txt", hashEmpiTemp);

			// init high encryption with notes key
			Hashtable<String, String> hashNotestemp = new Hashtable<String, String>();
			hashNotestemp.put("A:\\I401.txt", notesKey);
			notesHighEnc = new HighEncryption("A:\\I401.txt", hashNotestemp);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return encrypted encounter ide
	 * 
	 * @param encounterIde
	 * @return
	 * @throws I2B2Exception 
	 */
	public String encryptEncounterIde(String encounterIde) throws I2B2Exception {
		return empiHighEnc.generic_encrypt(encounterIde);
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
		return notesHighEnc.generic_encrypt(notes);
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
		return notesHighEnc.generic_decrypt(encryptedNotes);
	}

}
