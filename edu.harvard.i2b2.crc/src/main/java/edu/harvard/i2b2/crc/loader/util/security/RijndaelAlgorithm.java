/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.util.security;

import javax.crypto.spec.*;

public class RijndaelAlgorithm {

	
	      
	
	
	private byte[] masterKey=null;
	
	private String encryptionMethod = "AES";
	private int keySize = 128;
	private javax.crypto.Cipher cipherEnc = null;
	private javax.crypto.Cipher cipherDec = null;
	//private KeyGenerator keygen;
	
	
	//public RijndaelAlgorithm( String password ) throws Exception {
	//	RijndaelAlgorithm(password, keySize, encryptionMethod);
	//}
	

	public RijndaelAlgorithm( String password, int ksize) throws Exception {
		String emethod = "AES/GCM/NoPadding";
		SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes("UTF-8"), encryptionMethod);

		cipherEnc = javax.crypto.Cipher.getInstance(emethod);
		cipherDec = javax.crypto.Cipher.getInstance(emethod);
		
		encryptionMethod = emethod;
		keySize = ksize;
		
		//setKey( pword );
		cipherEnc.init(javax.crypto.Cipher.ENCRYPT_MODE, skeySpec);
		cipherDec.init(javax.crypto.Cipher.DECRYPT_MODE, skeySpec);


	}
	
	/// Set the key.
	public void setKey( byte[] key ) {
		if (key.length == 0) {
			return;
		}
		// copy the byte key
		if (this.masterKey==null) {
			this.masterKey = key;
		}
	}

	public  byte[] bencrypt( String clear ) throws Exception
	{
		byte[] sValue = new byte[((clear.length() / ((keySize/8)-1)) + (clear.length() % ((keySize/8)-1) == 0 ? 0 : 1)) * (keySize/8)];
		int count=0;
		int realCount = clear.getBytes().length; 
		byte[] clearB = new byte[sValue.length];
		//Array.Copy(System.Text.Encoding.UTF8.GetBytes(clear),clearB,realCount);
		
		System.arraycopy(clear.getBytes(),0,clearB,0,realCount);
		for (int s=0; s < clear.length(); s=s+((keySize/8)-1))
		{
			byte[] text = new byte[(keySize/8)];// {'0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0'};
			//text[0] = (byte)'0';
			//Buffer.BlockCopy(

			//Array.Copy(clearB,s,text,0,this.BlockSize - 1);
			System.arraycopy(clearB,s,text,0,(keySize/8) - 1);
			if (s+((keySize/8)-1) < realCount)
				text[(keySize/8) - 1] = (byte) 15;
			else
				text[(keySize/8) - 1] = (byte) (realCount-s);
			byte[] ct =  encrypt(text); 
			//Array.Copy(ct,0,sValue,count*BlockSize,BlockSize);
			System.arraycopy(ct,0,sValue,count*(keySize/8),(keySize/8));
			count++;
		}
		return sValue;
	}
	
	public  byte[] encrypt( byte[] source ) throws Exception
	{
		// Return a String representation of the cipher text
		return cipherEnc.doFinal(source);
	}
	
	/// Encrypt a string
	public String encrypt( String source ) throws Exception
	{
		return new String(edu.harvard.i2b2.crc.util.Base64.getMimeEncoder().encode(source.getBytes("UTF-8")));
	}

	/* OLD
	public String bdecrypt(byte[] text) throws Exception
	{
		StringBuilder sValue = new StringBuilder();
		for (int i=0; i < text.length/(keySize/8); i++)
		{
			byte[] tt = new byte[8];
			System.arraycopy(text, i*(keySize/8), tt, i*(keySize/8), (keySize/8));
			byte[] ct = decrypt(tt); // blockDecrypt(text, i*(keySize/8), this.Key, (keySize/8));
			sValue.append(new String(ct)); //System.Text.Encoding.UTF8.GetString(ct, 0,(int) ct[(keySize/8) - 1]));
			//sValue.Append(ASCIIEncoding.ASCII.GetString(ct).Substring(0,(int) ct[this.BlockSize - 1]));
		}
		return sValue.toString(); 
	}
	*/
	
	public byte[] decrypt( byte[] source )  throws Exception
	{		
		// Return the clear text
		return cipherDec.doFinal(source);
	}

	public String decrypt( String source )  throws Exception
	{
		return new String(decrypt(edu.harvard.i2b2.crc.util.Base64.getMimeDecoder().decode(source.getBytes("UTF-8"))));

	}
	
	//private Key getKey()
	//{
	//	return (SecretKey)keygen.generateKey();
	//}

	
//	public RijndaelAlgorithm(String keyStr, int blocksize) throws Exception
//	{
//		this.BLOCK_SIZE = blocksize;
//		//this.KeySize = keysize;
//		this.Key = makeKey(keyStr.getBytes());
//	}
//
//	      static final String NAME = "Rijndael_Algorithm";
//	      static final boolean IN = true, OUT = false;
//	   
//	      static final boolean DEBUG =  false; //Rijndael_Properties.GLOBAL_DEBUG;
//	      static final int debuglevel = 0; //DEBUG ? Rijndael_Properties.getLevel(NAME) : 0;
//	      static final PrintWriter err = null; //DEBUG ? Rijndael_Properties.getOutput() : null;
//	   
//	      static final boolean TRACE = false; // Rijndael_Properties.isTraceable(NAME);
//	   
//	      static void debug (String s) { 
//	         err.println(">>> "+NAME+": "+s); }
//	      static void trace (boolean in, String s) {
//	         if (TRACE) err.println((in?"==> ":"<== ")+NAME+"."+s);
//	      }
//	      static void trace (String s) { 
//	         if (TRACE) err.println("<=> "+NAME+"."+s); }
//	   
//	   
//	   // Constants and variables
//	   //...........................................................................
//	   
//	      static int BLOCK_SIZE = 16; // default block size in bytes
//	      static Object Key = null; //Key
//	      
//	      static final int[] alog = new int[256];
//	      static final int[] log =  new int[256];
//	   
//	      static final byte[] S =  new byte[256];
//	      static final byte[] Si = new byte[256];
//	      static final int[] T1 = new int[256];
//	      static final int[] T2 = new int[256];
//	      static final int[] T3 = new int[256];
//	      static final int[] T4 = new int[256];
//	      static final int[] T5 = new int[256];
//	      static final int[] T6 = new int[256];
//	      static final int[] T7 = new int[256];
//	      static final int[] T8 = new int[256];
//	      static final int[] U1 = new int[256];
//	      static final int[] U2 = new int[256];
//	      static final int[] U3 = new int[256];
//	      static final int[] U4 = new int[256];
//	      static final byte[] rcon = new byte[30];
//	   
//	      static final int[][][] shifts = new int[][][] {
//	         { {0, 0}, {1, 3}, {2, 2}, {3, 1} },
//	         { {0, 0}, {1, 5}, {2, 4}, {3, 3} },
//	         { {0, 0}, {1, 7}, {3, 5}, {4, 4} }
//	      };
//	   
//	      private static final char[] HEX_DIGITS = {
//	         '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
//	      };
//	   
//	   
//	   // Static code - to intialise S-boxes and T-boxes
//	   //...........................................................................
//	   
//	      static {
//	         long time = System.currentTimeMillis();
//	      
//	         if (DEBUG && debuglevel > 6) {
//	            //System.out.println("Algorithm Name: "+Rijndael_Properties.FULL_NAME);
//	            System.out.println("Electronic Codebook (ECB) Mode");
//	            System.out.println();
//	         }
//	         int ROOT = 0x11B;
//	         int i, j = 0;
//	      
//	        //
//	        // produce log and alog tables, needed for multiplying in the
//	        // field GF(2^m) (generator = 3)
//	        //
//	         alog[0] = 1;
//	         for (i = 1; i < 256; i++) {
//	            j = (alog[i-1] << 1) ^ alog[i-1];
//	            if ((j & 0x100) != 0) j ^= ROOT;
//	            alog[i] = j;
//	         }
//	         for (i = 1; i < 255; i++) log[alog[i]] = i;
//	         byte[][] A = new byte[][] {
//	            {1, 1, 1, 1, 1, 0, 0, 0},
//	            {0, 1, 1, 1, 1, 1, 0, 0},
//	            {0, 0, 1, 1, 1, 1, 1, 0},
//	            {0, 0, 0, 1, 1, 1, 1, 1},
//	            {1, 0, 0, 0, 1, 1, 1, 1},
//	            {1, 1, 0, 0, 0, 1, 1, 1},
//	            {1, 1, 1, 0, 0, 0, 1, 1},
//	            {1, 1, 1, 1, 0, 0, 0, 1}
//	         };
//	         byte[] B = new byte[] { 0, 1, 1, 0, 0, 0, 1, 1};
//	      
//	        //
//	        // substitution box based on F^{-1}(x)
//	        //
//	         int t;
//	         byte[][] box = new byte[256][8];
//	         box[1][7] = 1;
//	         for (i = 2; i < 256; i++) {
//	            j = alog[255 - log[i]];
//	            for (t = 0; t < 8; t++)
//	               box[i][t] = (byte)((j >>> (7 - t)) & 0x01);
//	         }
//	        //
//	        // affine transform:  box[i] <- B + A*box[i]
//	        //
//	         byte[][] cox = new byte[256][8];
//	         for (i = 0; i < 256; i++)
//	            for (t = 0; t < 8; t++) {
//	               cox[i][t] = B[t];
//	               for (j = 0; j < 8; j++)
//	                  cox[i][t] ^= A[t][j] * box[i][j];
//	            }
//	        //
//	        // S-boxes and inverse S-boxes
//	        //
//	         for (i = 0; i < 256; i++) {
//	            S[i] = (byte)(cox[i][0] << 7);
//	            for (t = 1; t < 8; t++)
//	               S[i] ^= cox[i][t] << (7-t);
//	            Si[S[i] & 0xFF] = (byte) i;
//	         }
//	        //
//	        // T-boxes
//	        //
//	         byte[][] G = new byte[][] {
//	            {2, 1, 1, 3},
//	            {3, 2, 1, 1},
//	            {1, 3, 2, 1},
//	            {1, 1, 3, 2}
//	         };
//	         byte[][] AA = new byte[4][8];
//	         for (i = 0; i < 4; i++) {
//	            for (j = 0; j < 4; j++) AA[i][j] = G[i][j];
//	            AA[i][i+4] = 1;
//	         }
//	         byte pivot, tmp;
//	         byte[][] iG = new byte[4][4];
//	         for (i = 0; i < 4; i++) {
//	            pivot = AA[i][i];
//	            if (pivot == 0) {
//	               t = i + 1;
//	               while ((AA[t][i] == 0) && (t < 4))
//	                  t++;
//	               if (t == 4)
//	                  throw new RuntimeException("G matrix is not invertible");
//	               else {
//	                  for (j = 0; j < 8; j++) {
//	                     tmp = AA[i][j];
//	                     AA[i][j] = AA[t][j];
//	                     AA[t][j] = (byte) tmp;
//	                  }
//	                  pivot = AA[i][i];
//	               }
//	            }
//	            for (j = 0; j < 8; j++)
//	               if (AA[i][j] != 0)
//	                  AA[i][j] = (byte)
//	                     alog[(255 + log[AA[i][j] & 0xFF] - log[pivot & 0xFF]) % 255];
//	            for (t = 0; t < 4; t++)
//	               if (i != t) {
//	                  for (j = i+1; j < 8; j++)
//	                     AA[t][j] ^= mul(AA[i][j], AA[t][i]);
//	                  AA[t][i] = 0;
//	               }
//	         }
//	         for (i = 0; i < 4; i++)
//	            for (j = 0; j < 4; j++) iG[i][j] = AA[i][j + 4];
//	      
//	         int s;
//	         for (t = 0; t < 256; t++) {
//	            s = S[t];
//	            T1[t] = mul4(s, G[0]);
//	            T2[t] = mul4(s, G[1]);
//	            T3[t] = mul4(s, G[2]);
//	            T4[t] = mul4(s, G[3]);
//	         
//	            s = Si[t];
//	            T5[t] = mul4(s, iG[0]);
//	            T6[t] = mul4(s, iG[1]);
//	            T7[t] = mul4(s, iG[2]);
//	            T8[t] = mul4(s, iG[3]);
//	         
//	            U1[t] = mul4(t, iG[0]);
//	            U2[t] = mul4(t, iG[1]);
//	            U3[t] = mul4(t, iG[2]);
//	            U4[t] = mul4(t, iG[3]);
//	         }
//	        //
//	        // round constants
//	        //
//	         rcon[0] = 1;
//	         int r = 1;
//	         for (t = 1; t < 30; ) rcon[t++] = (byte)(r = mul(2, r));
//	      
//	         time = System.nanoTime() - time; //.currentTimeMillis() - time;
//	      
//	         if (DEBUG && debuglevel > 8) {
//	            System.out.println("==========");
//	            System.out.println();
//	            System.out.println("Static Data");
//	            System.out.println();
//	            System.out.println("S[]:"); 
//	            for(i=0;i<16;i++) { 
//	               for(j=0;j<16;j++) System.out.print("0x"+byteToString(S[i*16+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("Si[]:"); 
//	            for(i=0;i<16;i++) { 
//	               for(j=0;j<16;j++) System.out.print("0x"+byteToString(Si[i*16+j])+", "); System.out.println();}
//	         
//	            System.out.println();
//	            System.out.println("iG[]:"); 
//	            for(i=0;i<4;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+byteToString(iG[i][j])+", "); System.out.println();}
//	         
//	            System.out.println();
//	            System.out.println("T1[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T1[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T2[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T2[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T3[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T3[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T4[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T4[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T5[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T5[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T6[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T6[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T7[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T7[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("T8[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(T8[i*4+j])+", "); System.out.println();}
//	         
//	            System.out.println();
//	            System.out.println("U1[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(U1[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("U2[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(U2[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("U3[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(U3[i*4+j])+", "); System.out.println();}
//	            System.out.println();
//	            System.out.println("U4[]:"); 
//	            for(i=0;i<64;i++){
//	               for(j=0;j<4;j++) System.out.print("0x"+intToString(U4[i*4+j])+", "); System.out.println();}
//	         
//	            System.out.println();
//	            System.out.println("rcon[]:"); 
//	            for(i=0;i<5;i++){
//	               for(j=0;j<6;j++) System.out.print("0x"+byteToString(rcon[i*6+j])+", "); System.out.println();}
//	         
//	            System.out.println();
//	            System.out.println("Total initialization time: "+time+" ms.");
//	            System.out.println();
//	         }
//	      }
//	   
//	    // multiply two elements of GF(2^m)
//	      static final int mul (int a, int b) {
//	         return (a != 0 && b != 0) ?
//	            alog[(log[a & 0xFF] + log[b & 0xFF]) % 255] :
//	            0;
//	      }
//	   
//	    // convenience method used in generating Transposition boxes
//	      static final int mul4 (int a, byte[] b) {
//	         if (a == 0) 
//	            return 0;
//	         a = log[a & 0xFF];
//	         int a0 = (b[0] != 0) ? alog[(a + log[b[0] & 0xFF]) % 255] & 0xFF : 0;
//	         int a1 = (b[1] != 0) ? alog[(a + log[b[1] & 0xFF]) % 255] & 0xFF : 0;
//	         int a2 = (b[2] != 0) ? alog[(a + log[b[2] & 0xFF]) % 255] & 0xFF : 0;
//	         int a3 = (b[3] != 0) ? alog[(a + log[b[3] & 0xFF]) % 255] & 0xFF : 0;
//	         return a0 << 24 | a1 << 16 | a2 << 8 | a3;
//	      }
//	   
//	   
//	   // Basic API methods
//	   //...........................................................................
//	   
//	    /**
//	     * Convenience method to expand a user-supplied key material into a
//	     * session key, assuming Rijndael's default block size (128-bit).
//	     *
//	     * @param key The 128/192/256-bit user-key to use.
//	     * @exception  InvalidKeyException  If the key is invalid.
//	     */
//	      public static Object makeKey (byte[] k) throws InvalidKeyException {
//	         return makeKey(k, BLOCK_SIZE);
//	      }
//	   
//	    /**
//	     * Convenience method to encrypt exactly one block of plaintext, assuming
//	     * Rijndael's default block size (128-bit).
//	     *
//	     * @param  in         The plaintext.
//	     * @param  inOffset   Index of in from which to start considering data.
//	     * @param  sessionKey The session key to use for encryption.
//	     * @return The ciphertext generated from a plaintext using the session key.
//	     */
//	      public static byte[] blockEncrypt (byte[] in, int inOffset, Object sessionKey) {
//	         if (DEBUG) trace(IN, "blockEncrypt("+in+", "+inOffset+", "+sessionKey+")");
//	         int[][] Ke = (int[][]) ((Object[]) sessionKey)[0]; // extract encryption round keys
//	         int ROUNDS = Ke.length - 1;
//	         int[] Ker = Ke[0];
//	      
//	        // plaintext to ints + key
//	         int t0   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Ker[0];
//	         int t1   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Ker[1];
//	         int t2   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Ker[2];
//	         int t3   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Ker[3];
//	      
//	         int a0, a1, a2, a3;
//	         for (int r = 1; r < ROUNDS; r++) {          // apply round transforms
//	            Ker = Ke[r];
//	            a0   = (T1[(t0 >>> 24) & 0xFF] ^
//	                   T2[(t1 >>> 16) & 0xFF] ^
//	                   T3[(t2 >>>  8) & 0xFF] ^
//	                   T4[ t3         & 0xFF]  ) ^ Ker[0];
//	            a1   = (T1[(t1 >>> 24) & 0xFF] ^
//	                   T2[(t2 >>> 16) & 0xFF] ^
//	                   T3[(t3 >>>  8) & 0xFF] ^
//	                   T4[ t0         & 0xFF]  ) ^ Ker[1];
//	            a2   = (T1[(t2 >>> 24) & 0xFF] ^
//	                   T2[(t3 >>> 16) & 0xFF] ^
//	                   T3[(t0 >>>  8) & 0xFF] ^
//	                   T4[ t1         & 0xFF]  ) ^ Ker[2];
//	            a3   = (T1[(t3 >>> 24) & 0xFF] ^
//	                   T2[(t0 >>> 16) & 0xFF] ^
//	                   T3[(t1 >>>  8) & 0xFF] ^
//	                   T4[ t2         & 0xFF]  ) ^ Ker[3];
//	            t0 = a0;
//	            t1 = a1;
//	            t2 = a2;
//	            t3 = a3;
//	            if (DEBUG && debuglevel > 6) System.out.println("CT"+r+"="+intToString(t0)+intToString(t1)+intToString(t2)+intToString(t3));
//	         }
//	      
//	        // last round is special
//	         byte[] result = new byte[BLOCK_SIZE]; // the resulting ciphertext
//	         Ker = Ke[ROUNDS];
//	         int tt = Ker[0];
//	         result[ 0] = (byte)(S[(t0 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[ 1] = (byte)(S[(t1 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[ 2] = (byte)(S[(t2 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[ 3] = (byte)(S[ t3         & 0xFF] ^  tt        );
//	         tt = Ker[1];
//	         result[ 4] = (byte)(S[(t1 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[ 5] = (byte)(S[(t2 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[ 6] = (byte)(S[(t3 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[ 7] = (byte)(S[ t0         & 0xFF] ^  tt        );
//	         tt = Ker[2];
//	         result[ 8] = (byte)(S[(t2 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[ 9] = (byte)(S[(t3 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[10] = (byte)(S[(t0 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[11] = (byte)(S[ t1         & 0xFF] ^  tt        );
//	         tt = Ker[3];
//	         result[12] = (byte)(S[(t3 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[13] = (byte)(S[(t0 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[14] = (byte)(S[(t1 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[15] = (byte)(S[ t2         & 0xFF] ^  tt        );
//	         if (DEBUG && debuglevel > 6) {
//	            System.out.println("CT="+toString(result));
//	            System.out.println();
//	         }
//	         if (DEBUG) trace(OUT, "blockEncrypt()");
//	         return result;
//	      }
//	   
//	    /**
//	     * Convenience method to decrypt exactly one block of plaintext, assuming
//	     * Rijndael's default block size (128-bit).
//	     *
//	     * @param  in         The ciphertext.
//	     * @param  inOffset   Index of in from which to start considering data.
//	     * @param  sessionKey The session key to use for decryption.
//	     * @return The plaintext generated from a ciphertext using the session key.
//	     */
//	      public static byte[] blockDecrypt (byte[] in, int inOffset, Object sessionKey) {
//	         if (DEBUG) trace(IN, "blockDecrypt("+in+", "+inOffset+", "+sessionKey+")");
//	         int[][] Kd = (int[][]) ((Object[]) sessionKey)[1]; // extract decryption round keys
//	         int ROUNDS = Kd.length - 1;
//	         int[] Kdr = Kd[0];
//	      
//	        // ciphertext to ints + key
//	         int t0   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Kdr[0];
//	         int t1   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Kdr[1];
//	         int t2   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Kdr[2];
//	         int t3   = ((in[inOffset++] & 0xFF) << 24 |
//	                       (in[inOffset++] & 0xFF) << 16 |
//	                       (in[inOffset++] & 0xFF) <<  8 |
//	                       (in[inOffset++] & 0xFF)        ) ^ Kdr[3];
//	      
//	         int a0, a1, a2, a3;
//	         for (int r = 1; r < ROUNDS; r++) {          // apply round transforms
//	            Kdr = Kd[r];
//	            a0   = (T5[(t0 >>> 24) & 0xFF] ^
//	                   T6[(t3 >>> 16) & 0xFF] ^
//	                   T7[(t2 >>>  8) & 0xFF] ^
//	                   T8[ t1         & 0xFF]  ) ^ Kdr[0];
//	            a1   = (T5[(t1 >>> 24) & 0xFF] ^
//	                   T6[(t0 >>> 16) & 0xFF] ^
//	                   T7[(t3 >>>  8) & 0xFF] ^
//	                   T8[ t2         & 0xFF]  ) ^ Kdr[1];
//	            a2   = (T5[(t2 >>> 24) & 0xFF] ^
//	                   T6[(t1 >>> 16) & 0xFF] ^
//	                   T7[(t0 >>>  8) & 0xFF] ^
//	                   T8[ t3         & 0xFF]  ) ^ Kdr[2];
//	            a3   = (T5[(t3 >>> 24) & 0xFF] ^
//	                   T6[(t2 >>> 16) & 0xFF] ^
//	                   T7[(t1 >>>  8) & 0xFF] ^
//	                   T8[ t0         & 0xFF]  ) ^ Kdr[3];
//	            t0 = a0;
//	            t1 = a1;
//	            t2 = a2;
//	            t3 = a3;
//	            if (DEBUG && debuglevel > 6) System.out.println("PT"+r+"="+intToString(t0)+intToString(t1)+intToString(t2)+intToString(t3));
//	         }
//	      
//	        // last round is special
//	         byte[] result = new byte[16]; // the resulting plaintext
//	         Kdr = Kd[ROUNDS];
//	         int tt = Kdr[0];
//	         result[ 0] = (byte)(Si[(t0 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[ 1] = (byte)(Si[(t3 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[ 2] = (byte)(Si[(t2 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[ 3] = (byte)(Si[ t1         & 0xFF] ^  tt        );
//	         tt = Kdr[1];
//	         result[ 4] = (byte)(Si[(t1 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[ 5] = (byte)(Si[(t0 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[ 6] = (byte)(Si[(t3 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[ 7] = (byte)(Si[ t2         & 0xFF] ^  tt        );
//	         tt = Kdr[2];
//	         result[ 8] = (byte)(Si[(t2 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[ 9] = (byte)(Si[(t1 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[10] = (byte)(Si[(t0 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[11] = (byte)(Si[ t3         & 0xFF] ^  tt        );
//	         tt = Kdr[3];
//	         result[12] = (byte)(Si[(t3 >>> 24) & 0xFF] ^ (tt >>> 24));
//	         result[13] = (byte)(Si[(t2 >>> 16) & 0xFF] ^ (tt >>> 16));
//	         result[14] = (byte)(Si[(t1 >>>  8) & 0xFF] ^ (tt >>>  8));
//	         result[15] = (byte)(Si[ t0         & 0xFF] ^  tt        );
//	         if (DEBUG && debuglevel > 6) {
//	            System.out.println("PT="+toString(result));
//	            System.out.println();
//	         }
//	         if (DEBUG) trace(OUT, "blockDecrypt()");
//	         return result;
//	      }
//	   
//	    /** A basic symmetric encryption/decryption test. */
//	      public static boolean self_test() { 
//	         return self_test(BLOCK_SIZE); }
//	   
//	   
//	   // Rijndael own methods
//	   //...........................................................................
//	   
//	    /** @return The default length in bytes of the Algorithm input block. */
//	      public static int blockSize() { 
//	         return BLOCK_SIZE; }
//	   
//	    /**
//	     * Expand a user-supplied key material into a session key.
//	     *
//	     * @param key        The 128/192/256-bit user-key to use.
//	     * @param blockSize  The block size in bytes of this Rijndael.
//	     * @exception  InvalidKeyException  If the key is invalid.
//	     */
//	      public static synchronized Object makeKey (byte[] k, int blockSize)
//	      throws InvalidKeyException {
//	         if (DEBUG) trace(IN, "makeKey("+k+", "+blockSize+")");
//	         if (k == null)
//	            throw new InvalidKeyException("Empty key");
//	         if (!(k.length == 16 || k.length == 24 || k.length == 32))
//	            throw new InvalidKeyException("Incorrect key length");
//	         int ROUNDS = getRounds(k.length, blockSize);
//	         int BC = blockSize / 4;
//	         int[][] Ke = new int[ROUNDS + 1][BC]; // encryption round keys
//	         int[][] Kd = new int[ROUNDS + 1][BC]; // decryption round keys
//	         int ROUND_KEY_COUNT = (ROUNDS + 1) * BC;
//	         int KC = k.length / 4;
//	         int[] tk = new int[KC];
//	         int i, j;
//	      
//	        // copy user material bytes into temporary ints
//	         for (i = 0, j = 0; i < KC; )
//	            tk[i++] = (k[j++] & 0xFF) << 24 |
//	                  (k[j++] & 0xFF) << 16 |
//	                  (k[j++] & 0xFF) <<  8 |
//	                  (k[j++] & 0xFF);
//	        // copy values into round key arrays
//	         int t = 0;
//	         for (j = 0; (j < KC) && (t < ROUND_KEY_COUNT); j++, t++) {
//	            Ke[t / BC][t % BC] = tk[j];
//	            Kd[ROUNDS - (t / BC)][t % BC] = tk[j];
//	         }
//	         int tt, rconpointer = 0;
//	         while (t < ROUND_KEY_COUNT) {
//	            // extrapolate using phi (the round key evolution function)
//	            tt = tk[KC - 1];
//	            tk[0] ^= (S[(tt >>> 16) & 0xFF] & 0xFF) << 24 ^
//	                  (S[(tt >>>  8) & 0xFF] & 0xFF) << 16 ^
//	                  (S[ tt         & 0xFF] & 0xFF) <<  8 ^
//	                  (S[(tt >>> 24) & 0xFF] & 0xFF)       ^
//	                  (rcon[rconpointer++]   & 0xFF) << 24;
//	            if (KC != 8)
//	               for (i = 1, j = 0; i < KC; ) tk[i++] ^= tk[j++];
//	            else {
//	               for (i = 1, j = 0; i < KC / 2; ) tk[i++] ^= tk[j++];
//	               tt = tk[KC / 2 - 1];
//	               tk[KC / 2] ^= (S[ tt         & 0xFF] & 0xFF)       ^
//	                     (S[(tt >>>  8) & 0xFF] & 0xFF) <<  8 ^
//	                     (S[(tt >>> 16) & 0xFF] & 0xFF) << 16 ^
//	                     (S[(tt >>> 24) & 0xFF] & 0xFF) << 24;
//	               for (j = KC / 2, i = j + 1; i < KC; ) tk[i++] ^= tk[j++];
//	            }
//	            // copy values into round key arrays
//	            for (j = 0; (j < KC) && (t < ROUND_KEY_COUNT); j++, t++) {
//	               Ke[t / BC][t % BC] = tk[j];
//	               Kd[ROUNDS - (t / BC)][t % BC] = tk[j];
//	            }
//	         }
//	         for (int r = 1; r < ROUNDS; r++)    // inverse MixColumn where needed
//	            for (j = 0; j < BC; j++) {
//	               tt = Kd[r][j];
//	               Kd[r][j] = U1[(tt >>> 24) & 0xFF] ^
//	                  U2[(tt >>> 16) & 0xFF] ^
//	                  U3[(tt >>>  8) & 0xFF] ^
//	                  U4[ tt         & 0xFF];
//	            }
//	        // assemble the encryption (Ke) and decryption (Kd) round keys into
//	        // one sessionKey object
//	         Object[] sessionKey = new Object[] {Ke, Kd};
//	         if (DEBUG) trace(OUT, "makeKey()");
//	         return sessionKey;
//	      }
//	   
//	    /**
//	     * Encrypt exactly one block of plaintext.
//	     *
//	     * @param  in         The plaintext.
//	     * @param  inOffset   Index of in from which to start considering data.
//	     * @param  sessionKey The session key to use for encryption.
//	     * @param  blockSize  The block size in bytes of this Rijndael.
//	     * @return The ciphertext generated from a plaintext using the session key.
//	     */
//	      public static byte[]
//	      blockEncrypt (byte[] in, int inOffset, Object sessionKey, int blockSize) {
//	         if (blockSize == BLOCK_SIZE)
//	            return blockEncrypt(in, inOffset, sessionKey);
//	         if (DEBUG) trace(IN, "blockEncrypt("+in+", "+inOffset+", "+sessionKey+", "+blockSize+")");
//	         Object[] sKey = (Object[]) sessionKey; // extract encryption round keys
//	         int[][] Ke = (int[][]) sKey[0];
//	      
//	         int BC = blockSize / 4;
//	         int ROUNDS = Ke.length - 1;
//	         int SC = BC == 4 ? 0 : (BC == 6 ? 1 : 2);
//	         int s1 = shifts[SC][1][0];
//	         int s2 = shifts[SC][2][0];
//	         int s3 = shifts[SC][3][0];
//	         int[] a = new int[BC];
//	         int[] t = new int[BC]; // temporary work array
//	         int i;
//	         byte[] result = new byte[blockSize]; // the resulting ciphertext
//	         int j = 0, tt;
//	      
//	         for (i = 0; i < BC; i++)                   // plaintext to ints + key
//	            t[i] = ((in[inOffset++] & 0xFF) << 24 |
//	                      (in[inOffset++] & 0xFF) << 16 |
//	                      (in[inOffset++] & 0xFF) <<  8 |
//	                      (in[inOffset++] & 0xFF)        ) ^ Ke[0][i];
//	         for (int r = 1; r < ROUNDS; r++) {          // apply round transforms
//	            for (i = 0; i < BC; i++)
//	               a[i] = (T1[(t[ i           ] >>> 24) & 0xFF] ^
//	                      T2[(t[(i + s1) % BC] >>> 16) & 0xFF] ^
//	                      T3[(t[(i + s2) % BC] >>>  8) & 0xFF] ^
//	                      T4[ t[(i + s3) % BC]         & 0xFF]  ) ^ Ke[r][i];
//	            System.arraycopy(a, 0, t, 0, BC);
//	            if (DEBUG && debuglevel > 6) System.out.println("CT"+r+"="+toString(t));
//	         }
//	         for (i = 0; i < BC; i++) {                   // last round is special
//	            tt = Ke[ROUNDS][i];
//	            result[j++] = (byte)(S[(t[ i           ] >>> 24) & 0xFF] ^ (tt >>> 24));
//	            result[j++] = (byte)(S[(t[(i + s1) % BC] >>> 16) & 0xFF] ^ (tt >>> 16));
//	            result[j++] = (byte)(S[(t[(i + s2) % BC] >>>  8) & 0xFF] ^ (tt >>>  8));
//	            result[j++] = (byte)(S[ t[(i + s3) % BC]         & 0xFF] ^ tt);
//	         }
//	         if (DEBUG && debuglevel > 6) {
//	            System.out.println("CT="+toString(result));
//	            System.out.println();
//	         }
//	         if (DEBUG) trace(OUT, "blockEncrypt()");
//	         return result;
//	      }
//	   
//	    /**
//	     * Decrypt exactly one block of ciphertext.
//	     *
//	     * @param  in         The ciphertext.
//	     * @param  inOffset   Index of in from which to start considering data.
//	     * @param  sessionKey The session key to use for decryption.
//	     * @param  blockSize  The block size in bytes of this Rijndael.
//	     * @return The plaintext generated from a ciphertext using the session key.
//	     */
//	      public static byte[]
//	      blockDecrypt (byte[] in, int inOffset, Object sessionKey, int blockSize) {
//	         if (blockSize == BLOCK_SIZE)
//	            return blockDecrypt(in, inOffset, sessionKey);
//	         if (DEBUG) trace(IN, "blockDecrypt("+in+", "+inOffset+", "+sessionKey+", "+blockSize+")");
//	         Object[] sKey = (Object[]) sessionKey; // extract decryption round keys
//	         int[][] Kd = (int[][]) sKey[1];
//	      
//	         int BC = blockSize / 4;
//	         int ROUNDS = Kd.length - 1;
//	         int SC = BC == 4 ? 0 : (BC == 6 ? 1 : 2);
//	         int s1 = shifts[SC][1][1];
//	         int s2 = shifts[SC][2][1];
//	         int s3 = shifts[SC][3][1];
//	         int[] a = new int[BC];
//	         int[] t = new int[BC]; // temporary work array
//	         int i;
//	         byte[] result = new byte[blockSize]; // the resulting plaintext
//	         int j = 0, tt;
//	      
//	         for (i = 0; i < BC; i++)                   // ciphertext to ints + key
//	            t[i] = ((in[inOffset++] & 0xFF) << 24 |
//	                      (in[inOffset++] & 0xFF) << 16 |
//	                      (in[inOffset++] & 0xFF) <<  8 |
//	                      (in[inOffset++] & 0xFF)        ) ^ Kd[0][i];
//	         for (int r = 1; r < ROUNDS; r++) {          // apply round transforms
//	            for (i = 0; i < BC; i++)
//	               a[i] = (T5[(t[ i           ] >>> 24) & 0xFF] ^
//	                      T6[(t[(i + s1) % BC] >>> 16) & 0xFF] ^
//	                      T7[(t[(i + s2) % BC] >>>  8) & 0xFF] ^
//	                      T8[ t[(i + s3) % BC]         & 0xFF]  ) ^ Kd[r][i];
//	            System.arraycopy(a, 0, t, 0, BC);
//	            if (DEBUG && debuglevel > 6) System.out.println("PT"+r+"="+toString(t));
//	         }
//	         for (i = 0; i < BC; i++) {                   // last round is special
//	            tt = Kd[ROUNDS][i];
//	            result[j++] = (byte)(Si[(t[ i           ] >>> 24) & 0xFF] ^ (tt >>> 24));
//	            result[j++] = (byte)(Si[(t[(i + s1) % BC] >>> 16) & 0xFF] ^ (tt >>> 16));
//	            result[j++] = (byte)(Si[(t[(i + s2) % BC] >>>  8) & 0xFF] ^ (tt >>>  8));
//	            result[j++] = (byte)(Si[ t[(i + s3) % BC]         & 0xFF] ^ tt);
//	         }
//	         if (DEBUG && debuglevel > 6) {
//	            System.out.println("PT="+toString(result));
//	            System.out.println();
//	         }
//	         if (DEBUG) trace(OUT, "blockDecrypt()");
//	         return result;
//	      }
//	   
//	    /** A basic symmetric encryption/decryption test for a given key size. */
//	      private static boolean self_test (int keysize) {
//	         if (DEBUG) trace(IN, "self_test("+keysize+")");
//	         boolean ok = false;
//	         try {
//	            byte[] kb = new byte[keysize];
//	            byte[] pt = new byte[BLOCK_SIZE];
//	            int i;
//	         
//	            for (i = 0; i < keysize; i++)
//	               kb[i] = (byte) i;
//	            for (i = 0; i < BLOCK_SIZE; i++)
//	               pt[i] = (byte) i;
//	         
//	            if (DEBUG && debuglevel > 6) {
//	               System.out.println("==========");
//	               System.out.println();
//	               System.out.println("KEYSIZE="+(8*keysize));
//	               System.out.println("KEY="+toString(kb));
//	               System.out.println();
//	            }
//	            Object key = makeKey(kb, BLOCK_SIZE);
//	         
//	            if (DEBUG && debuglevel > 6) {
//	               System.out.println("Intermediate Ciphertext Values (Encryption)");
//	               System.out.println();
//	               System.out.println("PT="+toString(pt));
//	            }
//	            byte[] ct =  blockEncrypt(pt, 0, key, BLOCK_SIZE);
//	         
//	            if (DEBUG && debuglevel > 6) {
//	               System.out.println("Intermediate Plaintext Values (Decryption)");
//	               System.out.println();
//	               System.out.println("CT="+toString(ct));
//	            }
//	            byte[] cpt = blockDecrypt(ct, 0, key, BLOCK_SIZE);
//	         
//	            ok = areEqual(pt, cpt);
//	            if (!ok)
//	               throw new RuntimeException("Symmetric operation failed");
//	         }
//	            catch (Exception x) {
//	               if (DEBUG && debuglevel > 0) {
//	                  debug("Exception encountered during self-test: " + x.getMessage());
//	                  x.printStackTrace();
//	               }
//	            }
//	         if (DEBUG && debuglevel > 0) debug("Self-test OK? " + ok);
//	         if (DEBUG) trace(OUT, "self_test()");
//	         return ok;
//	      }
//	   
//	    /**
//	     * Return The number of rounds for a given Rijndael's key and block sizes.
//	     *
//	     * @param keySize    The size of the user key material in bytes.
//	     * @param blockSize  The desired block size in bytes.
//	     * @return The number of rounds for a given Rijndael's key and
//	     *      block sizes.
//	     */
//	      public static int getRounds (int keySize, int blockSize) {
//	         switch (keySize) {
//	            case 16:
//	               return blockSize == 16 ? 10 : (blockSize == 24 ? 12 : 14);
//	            case 24:
//	               return blockSize != 32 ? 12 : 14;
//	            default: // 32 bytes = 256 bits
//	               return 14;
//	         }
//	      }
//	   
//	   
//	   // utility static methods (from cryptix.util.core ArrayUtil and Hex classes)
//	   //...........................................................................
//	   
//	    /**
//	     * Compares two byte arrays for equality.
//	     *
//	     * @return true if the arrays have identical contents
//	     */
//	      private static boolean areEqual (byte[] a, byte[] b) {
//	         int aLength = a.length;
//	         if (aLength != b.length)
//	            return false;
//	         for (int i = 0; i < aLength; i++)
//	            if (a[i] != b[i])
//	               return false;
//	         return true;
//	      }
//	   
//	    /**
//	     * Returns a string of 2 hexadecimal digits (most significant
//	     * digit first) corresponding to the lowest 8 bits of <i>n</i>.
//	     */
//	      private static String byteToString (int n) {
//	         char[] buf = {
//	            HEX_DIGITS[(n >>> 4) & 0x0F],
//	            HEX_DIGITS[ n        & 0x0F]
//	         };
//	         return new String(buf);
//	      }
//	   
//	    /**
//	     * Returns a string of 8 hexadecimal digits (most significant
//	     * digit first) corresponding to the integer <i>n</i>, which is
//	     * treated as unsigned.
//	     */
//	      private static String intToString (int n) {
//	         char[] buf = new char[8];
//	         for (int i = 7; i >= 0; i--) {
//	            buf[i] = HEX_DIGITS[n & 0x0F];
//	            n >>>= 4;
//	         }
//	         return new String(buf);
//	      }
//	   
//	    /**
//	     * Returns a string of hexadecimal digits from a byte array. Each
//	     * byte is converted to 2 hex symbols.
//	     */
//	      private static String toString (byte[] ba) {
//	         int length = ba.length;
//	         char[] buf = new char[length * 2];
//	         for (int i = 0, j = 0, k; i < length; ) {
//	            k = ba[i++];
//	            buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
//	            buf[j++] = HEX_DIGITS[ k        & 0x0F];
//	         }
//	         return new String(buf);
//	      }
//	   
//	    /**
//	     * Returns a string of hexadecimal digits from an integer array. Each
//	     * int is converted to 4 hex symbols.
//	     */
//	      private static String toString (int[] ia) {
//	         int length = ia.length;
//	         char[] buf = new char[length * 8];
//	         for (int i = 0, j = 0, k; i < length; i++) {
//	            k = ia[i];
//	            buf[j++] = HEX_DIGITS[(k >>> 28) & 0x0F];
//	            buf[j++] = HEX_DIGITS[(k >>> 24) & 0x0F];
//	            buf[j++] = HEX_DIGITS[(k >>> 20) & 0x0F];
//	            buf[j++] = HEX_DIGITS[(k >>> 16) & 0x0F];
//	            buf[j++] = HEX_DIGITS[(k >>> 12) & 0x0F];
//	            buf[j++] = HEX_DIGITS[(k >>>  8) & 0x0F];
//	            buf[j++] = HEX_DIGITS[(k >>>  4) & 0x0F];
//	            buf[j++] = HEX_DIGITS[ k         & 0x0F];
//	         }
//	         return new String(buf);
//	      }
//	   
//			public byte[] encrypt(byte[] clear)
//			{
//					return blockEncrypt(clear, 0, this.Key, this.BLOCK_SIZE);
//			}
//	   
//			public  String encrypt(String clear)
//			{
//				byte[] sValue = new byte[((clear.length() / (this.BLOCK_SIZE-1)) + (clear.length() % (this.BLOCK_SIZE-1) == 0 ? 0 : 1)) * this.BLOCK_SIZE];
//				int s=0;
//				while (clear.length() != 0)
//				{
//					//old dotnet byte[] text = clear.System.Text.Encoding.UTF8.GetBytes(clear.PadRight(this.BLOCK_SIZE, '0'));
//					byte[] text = clear.getBytes();
//					
//					if (clear.length() > (this.BLOCK_SIZE-1))
//						text[this.BLOCK_SIZE - 1] = (byte) 15;
//					else
//						text[this.BLOCK_SIZE - 1] = (byte) clear.length();
//					byte[] ct =  encrypt(text); 
//					System.arraycopy(ct,0,sValue,s*BLOCK_SIZE,BLOCK_SIZE);
//
//					//old dotnet Array.Copy(ct,0,sValue,s*BLOCK_SIZE,BLOCK_SIZE);
//					//old dotnet clear = (clear.length() < BLOCK_SIZE - 1 ? "" : clear.Remove(0,this.BLOCK_SIZE - 1));
//					clear = (clear.length() < BLOCK_SIZE - 1 ? "" : clear.substring(0, this.BLOCK_SIZE - 1) + clear.substring(this.BLOCK_SIZE)); 
//					s++;
//				}
//				return new String(sValue);
//					//ASCIIEncoding.ASCII.GetString(sValue);
//			}
//			
//			public byte[] decrypt(byte[] cipher)
//			{
//				return blockDecrypt(cipher, 0, this.Key, this.BLOCK_SIZE);
//			}
//
//
//			public  String decrypt(String cipher) throws Exception //, string key)
//			{
//				byte[] text = cipher.getBytes( "UTF-8");
//					//ASCIIEncoding.ASCII.GetBytes(cipher);
//
//				StringBuilder sValue = new StringBuilder();
//				for (int i=0; i < text.length/this.BLOCK_SIZE; i++)
//				{
//					byte[] ct = blockDecrypt(text, i*this.BLOCK_SIZE, this.Key, this.BLOCK_SIZE);
//					sValue.append(new String(ct));//System.Text.Encoding.UTF8.GetString(ct));
//					//sValue.Append(ASCIIEncoding.ASCII.GetString(ct));
//				}
//				return sValue.toString();
//			}
//
//
//	   // main(): use to generate the Intermediate Values KAT
//	   //...........................................................................
//	   
//	      public static void main (String[] args) {
//	         self_test(16);
//	         self_test(24);
//	         self_test(32);
//	      }
//	
}
