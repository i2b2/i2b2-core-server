/*
 * Copyright (c) 2006-2009 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *     
 */
package edu.harvard.i2b2.im.util;

import javax.crypto.*;
import javax.crypto.spec.*;

public class RijndaelAlgorithm {

	private byte[] masterKey = null;

	private int keySize = 128;
	private Cipher cipherEnc = null;
	private Cipher cipherDec = null;


	public RijndaelAlgorithm(String password, int ksize) throws Exception {
		this(password, ksize, "AES", "AES/ECB/NoPadding");
	}

	public RijndaelAlgorithm(String password, int ksize, String encryptionType,
			String emethod) throws Exception {

		SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes("UTF-8"),
				encryptionType);

		cipherEnc = Cipher.getInstance(emethod);
		cipherDec = Cipher.getInstance(emethod);

		keySize = ksize;

		// setKey( pword );
		cipherEnc.init(Cipher.ENCRYPT_MODE, skeySpec);
		cipherDec.init(Cipher.DECRYPT_MODE, skeySpec);

	}

	// / Set the key.
	public void setKey(byte[] key) {
		if (key.length == 0) {
			System.out.println("the key passed to setKey was zero length");
			return;
		}
		// copy the byte key
		if (this.masterKey == null) {
			this.masterKey = key;
		}
	}

	public byte[] bencrypt(String clear) throws Exception {
		byte[] sValue = new byte[((clear.length() / ((keySize / 8) - 1)) + (clear
				.length()
				% ((keySize / 8) - 1) == 0 ? 0 : 1))
				* (keySize / 8)];
		int count = 0;
		int realCount = clear.getBytes().length;
		byte[] clearB = new byte[sValue.length];
		// Array.Copy(System.Text.Encoding.UTF8.GetBytes(clear),clearB,realCount);

		System.arraycopy(clear.getBytes(), 0, clearB, 0, realCount);
		for (int s = 0; s < clear.length(); s = s + ((keySize / 8) - 1)) {
			byte[] text = new byte[(keySize / 8)];// {'0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0'};
			// text[0] = (byte)'0';
			// Buffer.BlockCopy(

			// Array.Copy(clearB,s,text,0,this.BlockSize - 1);
			System.arraycopy(clearB, s, text, 0, (keySize / 8) - 1);
			if (s + ((keySize / 8) - 1) < realCount)
				text[(keySize / 8) - 1] = (byte) 15;
			else
				text[(keySize / 8) - 1] = (byte) (realCount - s);
			byte[] ct = encrypt(text);
			// Array.Copy(ct,0,sValue,count*BlockSize,BlockSize);
			System.arraycopy(ct, 0, sValue, count * (keySize / 8),
					(keySize / 8));
			count++;
		}
		return sValue;
	}

	public byte[] encrypt(byte[] source) throws Exception {
		// Return a String representation of the cipher text
		return cipherEnc.doFinal(source);
	}

	// / Encrypt a string
	public String encrypt(String source) throws Exception {
		return new String(encrypt(source.getBytes()));
	}


	public byte[] decrypt(byte[] source) throws Exception {
		// Return the clear text
		return cipherDec.doFinal(source);
	}

	public String decrypt(String source) throws Exception {
		return new String(decrypt(source.getBytes()));
	}

}