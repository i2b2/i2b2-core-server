package edu.harvard.i2b2.crc.loader.util.security;

import java.util.Hashtable;

public class Cipher {

	
	public static String encryptMRN(String key, String mrn, String company) throws Exception
	{
		
		Hashtable htemp = new Hashtable();
		htemp.put("a", key);
		HighEncryption highEnc =new HighEncryption("a", htemp);		
		return  highEnc.mrn_encrypt(mrn, true, company);		
	}
	
	public static String decryptMRN(String key, String mrn) throws Exception
	{
		Hashtable htemp = new Hashtable();
		htemp.put("a", key);
		HighEncryption highEnc =new HighEncryption("a", htemp);		
		return  highEnc.mrn_decrypt(mrn, true);		
	}

	public static String encrypt(String key, String clear) throws Exception
	{
		Hashtable htemp = new Hashtable();
		htemp.put("a", key);
		HighEncryption highEnc =new HighEncryption("a", htemp);		
		return  highEnc.generic_encrypt(clear);		
	}

	public static String decrypt(String key, String cipher) throws Exception
	{
		Hashtable htemp = new Hashtable();
		htemp.put("a", key);
		HighEncryption highEnc =new HighEncryption("a", htemp);		
		return  highEnc.generic_decrypt(cipher);		
	}

}
