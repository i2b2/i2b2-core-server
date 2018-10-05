/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.util;
import java.text.DateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class SessionKey {
	private String username;
	private String password;
	private String domain;
	private Date expirationDate;
	private static Cipher desCipher;
	private static SecretKey desKey;
	static
	{
		try
		{
			//Build the cipher for storage in the static class and use by encrypt and decrypt
			KeyGenerator keygen = KeyGenerator.getInstance("DES");
			desKey = keygen.generateKey();
			// Create the cipher 
			desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		}
		catch(Exception ee)
		{
			//do something because couldnt find algorithm
		}	  
	}

	public SessionKey(String username, String password, String domain, Date expirationDate)
	{
		this.username=username;
		this.password=password;
		this.domain=domain;
		this.expirationDate=expirationDate;
	}
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public static SessionKey Decrypt(String encryptedSessionKey)
	{
		SessionKey decryptedKey=null;
		try{
			// Initialize the same cipher for decryption
			desCipher.init(Cipher.DECRYPT_MODE, desKey);
			byte[] ciphertext=hexStringToByteArray(encryptedSessionKey);
			// Decrypt the ciphertext
			byte[] cleartext = desCipher.doFinal(ciphertext);
			String cleartextstring=new String(cleartext);
			String[] temps=null;
			temps=cleartextstring.split("\t");
			decryptedKey=new SessionKey(temps[0],temps[1], temps[2], DateFormat.getInstance().parse(temps[3]));
		}
		catch (Exception ee)
		{
			System.out.println(ee.toString());
		}
		return decryptedKey;
	}
	public static String Encrypt(SessionKey sessionKey)
	{ 
		String encrypted=null;
		try
		{
			//concatenate the key elements
			String concat=sessionKey.getUsername()+"\t"+sessionKey.getPassword()+"\t"+sessionKey.getDomain()+"\t"+DateFormat.getInstance().format(sessionKey.getExpirationDate());
			//convert it to bytes
			byte[] cleartext=concat.getBytes();
			// Initialize the cipher for encryption
			desCipher.init(Cipher.ENCRYPT_MODE, desKey);
			//do the encryption
			byte[] ciphertext = desCipher.doFinal(cleartext);
			encrypted=byteArrayToHexString(ciphertext);
		}
		catch(Exception ee)
		{
			//failed to encrypt
		}
		return encrypted;
	}	
	/**
	* Convert a byte[] array to readable string format. This makes the "hex"
	readable!
	* @return result String buffer in String format 
	* @param in byte[] buffer to convert to string format
	*/
	static String byteArrayToHexString(byte in[]) {
	    byte ch = 0x00;
	    int i = 0; 
	    if (in == null || in.length <= 0)
	        return null;
	        
	    String pseudo[] = {"0", "1", "2",
	"3", "4", "5", "6", "7", "8",
	"9", "A", "B", "C", "D", "E",
	"F"};
	    StringBuffer out = new StringBuffer(in.length * 2);
	    
	    while (i < in.length) {
	        ch = (byte) (in[i] & 0xF0); // Strip off high nibble
	        ch = (byte) (ch >>> 4);
	        // shift the bits down
	        ch = (byte) (ch & 0x0F);    
	      //must do this is high order bit is on!
	        out.append(pseudo[ (int) ch]); // convert the nibble to a String Character
	        
	        ch = (byte) (in[i] & 0x0F); // Strip off low nibble 
	        out.append(pseudo[ (int) ch]); // convert the nibble to a String Character
	        i++;
	    }
	    String rslt = new String(out);
	    return rslt;
	}  
	static byte[] hexStringToByteArray(String hexstring)
	{
		
	    int i = 0; 
	    if (hexstring == null || hexstring.length()<= 0)
	        return null;
	        
	   String stringvector="0123456789ABCDEF";
	   byte[] bytevector={0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
	   byte[] out=new byte[hexstring.length()/2];
	   while(i <hexstring.length()-1) 
	   {
		   byte ch = 0x00;
		   //Convert high nibble charater to a hex byte
		   ch=(byte) (ch | bytevector[stringvector.indexOf(hexstring.charAt(i))]);
		   ch= (byte) (ch << 4); //move this to the high bit
		   
		   //Convert the low nibble to a hexbyte
		   ch=(byte)(ch | bytevector[stringvector.indexOf(hexstring.charAt(i+1))]); //next hex value
		   out[i/2]=ch;
		   i++;
		   i++;	   
	   }
	   return out;
	}
}
