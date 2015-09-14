package edu.harvard.i2b2.crc.loader.util.security;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import java.sql.*;

import org.apache.commons.logging.LogFactory; 
import org.apache.commons.logging.Log;

import sun.misc.BASE64Decoder;
import edu.harvard.i2b2.common.exception.I2B2Exception;


/**
 * A class that recursively scans a directory and its sub_dirs,
 * The class implements Runnable
 * to allow to use this within a Thread in a larger program.
 */
public class HighEncryption {
	
	private static Log log = LogFactory.getLog(HighEncryption.class.getName()); 
	
	private Hashtable ht = new Hashtable();  
	
	
	private String msFile = null;
	
	private RijndaelAlgorithm cipher;						// Cipher for mrnrs
	
//	private Callback m_callBackHandle=null;
	private boolean m_wfcui=false;
	// medical record number encryption parameters
	private String m_sBWH_IdentifyingFirstCharacter = "B";
	private String m_sMGH_IdentifyingFirstCharacter = "M";
	private String m_sEMPI_IdentifyingFirstCharacter = "Z";
	private String m_sENCYPT_IdentifyingFirstCharacter = ")";
	
	private int m_iBWH_standard_length = 11;
	private int m_iMGH_standard_length = 11;
	private int m_iEMPI_standard_length = 12;
	private String m_sTheFillCharacter = "X";
	private String m_sEncryptionErrorValue = "";
	// medical record number decryption parameters
	private int m_iBWH_de_standard_length = 8;  
	private int m_iMGH_de_standard_length = 7;
	private int m_iEMPI_de_standard_length = 9;
	private String  m_sTheDeFillCharacter = "0";
	
	
	public HighEncryption(String sFileName) throws Exception {
		this(sFileName, null);
	}
	
	public HighEncryption(String inFileName, String keys, Connection sConn) throws Exception {
		
		String values = null;
		
		cipher = new RijndaelAlgorithm(keys, 128); //, "AES");
		Statement stmt = sConn.createStatement ();
		ResultSet rs = stmt.executeQuery ("SELECT description FROM key WHERE NAME='" + inFileName + "'");
		
		while (rs.next ()) {
			ht.put(inFileName, cipher.decrypt(rs.getString(1)));
			//ConvertBaseNToDecDecrypt(rs.getString(1));
		}
		rs.close();
		
	}
	
	public HighEncryption(String inFileName, Hashtable keys) throws Exception {
		String key = null;
		//	makearray64(); // create the array regardless of how key will be acquired AHA@20011016
		if ((keys!=null)&&(keys.size()>0))
			key = keys.get(inFileName).toString();    
		
		if ((key==null)||(key.length()==0)){
			msFile = inFileName;
			byte baBuffer[] = new byte[2056];
			try {
				FileInputStream oFileIn = new FileInputStream(inFileName);
				int iBytes = oFileIn.read(baBuffer,0,2056);
				String sString = new String(baBuffer);	
				//new String(baBuffer,0,0,iBytes);
				key = sString;
			}
			catch (FileNotFoundException fnfe) {
				log.fatal("HighEncryption initialization file-not-found error");
				throw new Exception("HighEncryption initialization error");
			}
			catch (Exception e) {
				log.fatal("HighEncryption initialization error");
				throw new Exception("HighEncryption initialization error");
			}
		}
		
		try {
			key = key.trim();
			cipher			= new RijndaelAlgorithm(key, 128); //, "AES");  //long version for dates
		}
		catch (Exception ex) {
			//Lib.TError("HighEncryption initialization error");
			ex.printStackTrace();
			throw new Exception("HighEncryption initialization error");
		}          
	}
	
	public boolean setDatabaseKey(Connection sConn, String masterKey, String sFilename, String newKey, String token, String username) throws Exception 
	{
		if (token == null)
			token = "";
		
		String eNewKey = cipher.encrypt(newKey); 
		
		if (!newKey.equals(cipher.decrypt(eNewKey)))
			throw new Exception ("Error verifying decrpyt is eqal to encrypt");
		
		PreparedStatement setKey = sConn.prepareStatement("INSERT INTO key (name, description, token, userinserted, dateinserted) VALUES "
				+ "(?, ?, ?, ?, ?)");
		setKey.setString(1, sFilename); 
		setKey.setString(2, eNewKey); 
		setKey.setString(3, token); 
		setKey.setString(4, username);
		setKey.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
		
		int count = setKey.executeUpdate();
		if (count == 1)
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Turns array of bytes into string
	 *
	 * @param buf	Array of bytes to convert to hex string
	 * @return	Generated hex string
	 */
	public static String asHex (byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;
		
		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");
			
			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}
		
		return strbuf.toString();
	}
	
	/**
	 * mrn_encrypt makes the encrypted medical record number.
	 * 
	 * All empi numbers are 9 digits long, in the form 100xxxxxx.  Therefore, no
	 * padding needs to be added to the front of the number.  The output empi 
	 * number is an octal of length 11, as set by the variable: empi_stardard_length.
	 *
	 * All bwh numbers are 8 digits long, but sometimes when they start with 
	 * zero's they are shorter (as though they were true numbers).  This routine
	 * does not care, it always removes leading zero's.  The output bwh LMRN is 
	 * an octal of length set by the variable m_iBWH_stardard_length.
	 *
	 * All mgh numbers are 7 digits long, but sometimes when they start with 
	 * zero's they are shorter (as though they were true numbers).  This routine
	 * does not care, it always removes leading zero's, however note that the 
	 * encrypt routine will add them back again as part of the standard 
	 * incryption method.  The output mgh LMRN is an octal of length set 
	 * by the variable m_iMGH_stardard_length.
	 *
	 * @param theInput String is the cleartext bwh LMRN number.
	 * @param standardLength Boolean if TRUE pads a return octal out with X's to
	 *   make the total length what m_iXXX_stardard_length is set to.
	 * @param theSite String is the name of the owner of the medical
	 *   record number.
	 * @returns the encrypted medical record number.
	 * @exception the program returns a String as defined by the
	 *   variable m_sEncryptionErrorValue.
	 */  
	public String mrn_encrypt(String theInput, boolean standardLength, String theSite) {     
		if ((theInput==null)||(theInput.length()==0)) return m_sEncryptionErrorValue;
		int standard_length;
		StringBuffer theOutput = new StringBuffer(16);
		if (theSite.equalsIgnoreCase("BWH")) {
			theOutput.append(m_sBWH_IdentifyingFirstCharacter);
			standard_length = m_iBWH_standard_length - 1;
		}
		else if (theSite.equalsIgnoreCase("MGH")) {
			theOutput.append(m_sMGH_IdentifyingFirstCharacter);
			standard_length = m_iMGH_standard_length - 1;
		}
		else if (theSite.equalsIgnoreCase("EMP") || theSite.equalsIgnoreCase("EMPI")) {
			theOutput.append(m_sEMPI_IdentifyingFirstCharacter);
			standard_length = m_iEMPI_standard_length - 1;
		}
		else {
			//Lib.TError("A valid site was not passed to mrn_encrypt function");
			return m_sEncryptionErrorValue;
		}    
		String theLong=null;
		try {
			//Strip off extra 0 in front
			theLong = ConvertDecToBaseNEncrypt(Integer.toString(Integer.parseInt(theInput)),32);
			//theLong = cipher.encrypt(theInput);
		}
		catch(Exception e) {
			//Lib.TError("Parsing error in mrn_encrypt, message was: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
		
		theOutput.append(theLong);
		return theOutput.toString();
		
		// RAJ:Following lines commented because exception is thrown
		// in length comparison
		// 
		/*
		 if (!standardLength) return (theOutput.append(theLong).toString());
		 try {
		 int addXes = standard_length - theLong.length();
		 if (addXes<0) {
		 //Lib.TError("A number to encrypt exceeded the maximum allowable length, the number was: '"+theLong+"'.");
		  return m_sEncryptionErrorValue;
		  }
		  else {
		  theOutput.append(theLong);
		  for (int i=0; i<addXes; i++) {
		  theOutput.append(m_sTheFillCharacter);
		  }
		  return theOutput.toString();
		  }
		  }
		  catch (Exception e) {
		  //Lib.TError("Unhandled error in mrn_encrypt: "+e.getMessage());
		   return m_sEncryptionErrorValue;
		   }
		   */
		
	}
	
	public String bwh_encnum_encrypt(String theInput)
	{
		if ((theInput == null) || (theInput.length() == 0))
			return m_sEncryptionErrorValue;
		try
		{
			// If AES just encrypt the whole input string, do not split of up and attach the 
			// remaining at the end.
			return m_sENCYPT_IdentifyingFirstCharacter + 
			m_sBWH_IdentifyingFirstCharacter +
			ConvertDecToBaseNEncrypt(theInput,32); 
		}
		catch (Exception e)
		{
			log.fatal("Unhandled error in bwh_encnum_encrypt: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
	}
	
	public String bwh_encnum_decrypt(String theInput)
	{
		if ((theInput == null) || (theInput.length() == 0))
			return m_sEncryptionErrorValue;
		try
		{
			return "TSI-BWH-" + ConvertBaseNToDecDecrypt(theInput.substring(2),32);
		}
		catch (Exception e)
		{
			log.fatal("Unhandled error in bwh_encnum_decrypt: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
	}
	
	
	
	public String mgh_encnum_encrypt(String theInput)
	{
		if ((theInput == null) || (theInput.length() == 0))
			return m_sEncryptionErrorValue;
		try
		{
			// If AES just encrypt the whole input string, do not split of up and attach the 
			// remaining at the end.
			return m_sENCYPT_IdentifyingFirstCharacter + 
			m_sMGH_IdentifyingFirstCharacter +
			ConvertDecToBaseNEncrypt(theInput,32); 
			
		}
		catch (Exception e)
		{
			log.fatal("Unhandled error in mgh_encnum_encrypt: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
	}
	
	public String mgh_encnum_decrypt(String theInput)
	{
		if ((theInput == null) || (theInput.length() == 0))
			return m_sEncryptionErrorValue;
		try
		{
			return "TSI-MGH-" + ConvertBaseNToDecDecrypt(theInput.substring(2),32);
		}
		catch (Exception e)
		{
			log.fatal("Unhandled error in mgh_encnum_decrypt: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
	}
	
	public String generic_encnum_encrypt(String theInput) throws I2B2Exception
	{
		return  generic_encrypt(theInput);
	}
	
	public String generic_encrypt(String theInput) throws I2B2Exception 
	{
		if ((theInput == null) || (theInput.length() == 0))
		{
			log.warn("Empty input to generic encryption: ");
			return m_sEncryptionErrorValue;
		}
		try
		{	
			String encryptedAccession = ConvertDecToBaseNEncrypt(theInput, 32); 
			return encryptedAccession;
		}
		catch (Exception e)
		{
			log.fatal("Unhandled error in generic_encrypt: "+e.getMessage());
				throw new I2B2Exception(e.getMessage(),e);
		}
			//
		//	return m_sEncryptionErrorValue;
		//}
	}
	
	public String generic_encnum_derypt(String theInput) throws I2B2Exception
	{
		return  generic_decrypt(theInput);
	}
	
	public String generic_decrypt(String theInput) throws I2B2Exception
	{
		if ((theInput == null) || (theInput.length() == 0))
		{
			log.warn("Empty input to generic decryption: ");
			return m_sEncryptionErrorValue;
		}
		String accession64 = theInput;
		try
		{
			accession64 = ConvertBaseNToDecDecrypt(accession64,32);
			return accession64;
		}
		catch (Exception e)
		{
			log.fatal("Unhandled error in generic_decrypt: "+e.getMessage());
			throw new I2B2Exception(e.getMessage(),e);
		//	return m_sEncryptionErrorValue;
		}
	}
	
	
	public String mrn_decrypt(String theInput, boolean standardLength) {     
		if ((theInput==null)||(theInput.length()==0)) return m_sEncryptionErrorValue;
		int standard_length;
		StringBuffer theOutput = new StringBuffer(16);
		if (theInput.startsWith(m_sMGH_IdentifyingFirstCharacter)) {
			standard_length = m_iMGH_de_standard_length;
		}
		else if (theInput.startsWith(m_sBWH_IdentifyingFirstCharacter)) {
			standard_length = m_iBWH_de_standard_length;
		}
		else if (theInput.startsWith(m_sEMPI_IdentifyingFirstCharacter)) {
			standard_length = m_iEMPI_de_standard_length;
		}
		else {
			//Lib.TError("A valid site was not passed to mrn_decrypt function");
			return m_sEncryptionErrorValue;
		}    
		String theLong=null;
		String sTempNumber = theInput.substring(1);
		sTempNumber = sTempNumber.replaceAll(m_sTheFillCharacter,"");   //Lib.StrFindAndReplace(m_sTheFillCharacter,"",sTempNumber);
		try {
			theLong = ConvertBaseNToDecDecrypt(sTempNumber, 32);
			theLong = leftPad(theLong, standard_length, m_sTheDeFillCharacter);
		}
		catch(Exception e) {
			log.fatal("Parsing error in mrn_decrypt, message was: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
		return theLong;
	}
	
	public static String leftPad (String stringToPad, int size, String padder)
	{
		if (padder.length() == 0)
		{
			return stringToPad;
		}
		StringBuffer strb = new StringBuffer(size);
		StringCharacterIterator sci  = new StringCharacterIterator(padder);
 
        while (strb.length() < (size - stringToPad.length()))
        {
			for (char ch = sci.first(); ch != CharacterIterator.DONE ; ch = sci.next())
			{
				if (strb.length() <  size - stringToPad.length())
				{
					strb.insert(  strb.length(),String.valueOf(ch));
				}
			}
		}
		return strb.append(stringToPad).toString();
	}
 

	public String rightPad(String s, int length, char pad) {
		StringBuffer buffer = new StringBuffer(s);
		int curLen=s.length();
		if (curLen < length)
		{
			for (int i=0; i<length; i++)
			{
				buffer.append(pad);
			}
		}
		return buffer.toString();
	}
	//* B220
	// Samples: Y229033779X3XXXXXXXX, Y96289732X10003XXXXX
	public String encnum_decrypt(String sEncNumber) {
		if ((sEncNumber==null)||(sEncNumber.length()==0)) {
			// log.fatal(("No IDX main number to decrypt");
			return m_sEncryptionErrorValue;
		}
		try {
			//int theLength = sEncNumber.length();
			int theGroupNumberStartsOn = sEncNumber.indexOf('X');
			// get the encounter number part, start after the 'Y'
			String sIdxEncNumber = sEncNumber.substring(1,theGroupNumberStartsOn);
			// decrypt the encounter number
			
			String sDecryptIdxEncNumber = cipher.decrypt(sIdxEncNumber);
			// get the group number part, start after the 'X'
			String sIdxGroupNumber = sEncNumber.substring(theGroupNumberStartsOn+1, sEncNumber.length());
			// take off the trailing 'X's
			char sTheFillCharacter = m_sTheFillCharacter.charAt(0);
			int iLastX = sIdxGroupNumber.length();
			for (int i=0; i<sIdxGroupNumber.length(); i++) {
				if (sIdxGroupNumber.charAt(i) == sTheFillCharacter) {
					iLastX = i;
					break;
				}
			}
			sIdxGroupNumber = sIdxGroupNumber.substring(0,iLastX);
			// make the encounter number in the form:
			// ... IDX-<group number>-<encounter number>
			String sFinalNumber = "IDX-MGH-" + sIdxGroupNumber + "-" + sDecryptIdxEncNumber;
			return sFinalNumber;
		}
		catch (Exception e) {
			log.fatal("Error in idxmgh_encnum_decrypt: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
	}
	//* 3231
	public String encnum_encrypt(String sExtraNumber, String sMainNumber) {
		int standard_length = 20;
		StringBuffer theOutput = new StringBuffer(standard_length);
		if ((sMainNumber==null)||(sMainNumber.length()==0)) {
			// log.fatal("No IDX main number to encrypt");
			return m_sEncryptionErrorValue;
		}
		if ((sExtraNumber==null)||(sExtraNumber.length()==0)) {
			log.error("No IDX extra number with main number " + sMainNumber);
			return m_sEncryptionErrorValue;
		}
		String sEncMainNumber = null; 
		try {
			sEncMainNumber = cipher.encrypt(sMainNumber);
		}
		catch(Exception e) {
			log.fatal("Parsing error in idxmgh_encnum_encrypt, message was: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
		String theLong = sEncMainNumber + "X" + sExtraNumber;
		try {
			int addXes = standard_length - 1 - theLong.length();
			if (addXes<0) {
				log.error("A number to encrypt exceeded the maximum allowable length, the number was: '"+theLong+"'.");
				return m_sEncryptionErrorValue;
			}
			else {
				theOutput.append("Z");
				theOutput.append(theLong);
				for (int i=0; i<addXes; i++) {
					theOutput.append(m_sTheFillCharacter);
				}
				return theOutput.toString();
			}
		}
		catch (Exception e) {
			log.fatal("Unhandled error in mrn_encrypt: "+e.getMessage());
			return m_sEncryptionErrorValue;
		}
	}
	
	/*
	 * ConvertDecToBaseNEncrypt makes the encrypted string in base N
	 * 
	 * The string is first encrypted into AES than converted to base N
	 *
	 * @param bVale String is the cleartext string of any length.
	 * @param byBase Int is the base to use, between 2 and 36
	 * @returns the ciphertext string in base N
	 */
	public String ConvertDecToBaseNEncrypt(String bValue, int byBase ) throws Exception
	{

		String sValue = ConvertDecToBaseNClear(cipher.bencrypt(bValue), byBase);
 		
		if (!bValue.equals(ConvertBaseNToDecDecrypt(sValue, byBase)))
			throw new Exception("Encryption check failed for Base N: " + bValue);
		
		//String sValue = cipher.bencrypt(bValue).toString();
		return sValue;
	}
	/*
	 * ConvertDecToBaseNClear makes the string in base N
	 * 
	 * The reason for calling Clear is because to does not call any encryption
	 * routines.  The input string is converted into multiple 8 byte arrays and 
	 * converted into int64 which is than converted into the selected base.
	 * The routine will add '!' for padding if the returned string is to small
	 * so that all the strings are the same length.
	 *
	 * @param bVale Byte[] is the byte array of any length.
	 * @param byBase Int is the base to use, between 2 and 36
	 * @returns the base N string
	 */
	private String ConvertDecToBaseNClear(byte[] bValue, int byBase ) throws Exception
	{
		double bValueLen = bValue.length;
		double divValue = bValueLen / 8;
		double celing = Math.ceil(divValue);
		int rounds = (int) celing;
		StringBuilder dValue = new StringBuilder(26);
		int count = 0;
		for (int i=0; i < rounds; i++)
		{
			int endLen = 8;
			if ((count+8) > bValue.length)
				endLen = bValue.length - count;
			byte[] b = new byte[8];
			
			System.arraycopy(bValue,count,b,0,endLen);
			
			
			
			// this is slow
			UInt64 myGuidInt = BitConverter(b);
			
			StringBuilder sValue = new StringBuilder();
			sValue.append(ConvertDecToBaseNClear(myGuidInt,32));
			// Deal with short strings
			if (sValue.length() < 13)
			{
				int addXes = 13 - sValue.length();
				for (int j = 0; j < addXes; j++)
				{
					sValue.append("!");
				}
			}
			dValue.append(sValue); 
			count=count+8;
		}
		return dValue.toString();
	}
	
	/*
	 * ConvertDecToBaseNClear makes the int in base N
	 * 
	 * The reason for calling Clear is because to does not call any encryption
	 * routines.  This is the actual routine that does the conversation into
	 * the base N.  The reason for using unsigned is because the conversation
	 * using AES would have negative inte values which would get lost when converting
	 * into the base representation.
	 *
	 * @param dValue uint64 is a valid unsigned integer
	 * @param byBase Int is the base to use, between 2 and 36
	 * @returns the base N string
	 */
	private String ConvertDecToBaseNClear( UInt64 dValue, long byBase ) throws Exception
	{
		String BaseNums = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		//String sResult = "";
		StringBuffer sResult = new StringBuffer();
		UInt64 dRemainder;
		
		//sResult = "";
		
		if ( (byBase >= 2) && (byBase <= 36) ) 
		{
			while (dValue.compareTo((Object) 0) > 0) // x > 0 
			{
				dRemainder = dValue.divideAndRemainder(byBase);
				//sResult = BaseNums.substring(dRemainder.intValue(), dRemainder.intValue()+1) + sResult;
				sResult.insert(0, BaseNums.substring(dRemainder.intValue(), dRemainder.intValue()+1));
			}
			return sResult.toString();
		} 
		else
		{
			throw new Exception("Base should be between 2 and 36.");
		}
	}
	
	/*
	 * ConvertBaseNToDecClear makes the string from base N
	 * 
	 * The reason for calling Clear is because to does not call any encryption
	 * routines.  This is the actual routine that does the conversation from
	 * the base N.  The reason for using unsigned is because the conversation
	 * using AES would have negative inte values which would get lost when converting
	 * into the base representation. The routine will remove any '!' used for padding
	 *
	 * @param dValue string is a valid base N string of length of a valid int64 base
	 * @param byBase Int is the base to use, between 2 and 36
	 * @returns the string
	 */
	private UInt64 ConvertBaseNToDecClear( int byBase, String origValue) throws Exception 
	{
		// Deal with short strings
		String dValue = origValue;
		while (dValue.endsWith("!"))
			dValue = dValue.substring(0,dValue.length()-1);
		
		String BaseNums = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		UInt64 lReturn = new UInt64(0);
		int n;
		if ( (byBase >= 2) && (byBase <= 36) ) 
		{
			n = 0;
			while (n != dValue.length() )
			{
				//lReturn = (long) ((BaseNums.indexOf(dValue.substring(((dValue.length() - n) - 1), dValue.length() - 1))) 
				//		* (Math.pow(byBase,n))) + lReturn;
				
				//Half working
				//lReturn.add(
				//		new  java.math.BigInteger(Long.toString((BaseNums.indexOf(
				//				dValue.substring(((dValue.length() - n) - 1), dValue.length() - n)))
				//		* (long) (Math.pow(byBase,n)))));
				
				
				UInt64 a =  new  UInt64(Long.toString((BaseNums.indexOf(
						dValue.substring(((dValue.length() - n) - 1), dValue.length() - n)))));
				a.multiply((long) (Math.pow(byBase,n)));
				
				lReturn.add(a.bigIntValue());
				
				
				n++;
			}  
			return lReturn;
		}
		else 
		{
			throw new Exception("Base should be between 2 and 36.");
		}
	}
	
	
	/*
	 * ConvertBaseNToDecDecrypt makes the string from base N into the original string
	 * 
	 * The input string is split into multiple chuncks.  Each chunck is split into 2
	 * sections.  Both are converted back into the original string and appeneded together
	 * The new string is decrypted and returned. 
	 *
	 * @param dValue string is a valid base N string of any length
	 * @param byBase Int is the base to use, between 2 and 36
	 * @returns the cleartext string in a string.
	 */
	public String ConvertBaseNToDecDecrypt(String dValue, int byBase ) throws Exception
	{
		int rounds = dValue.length() / 13;
		StringBuffer sValue = new StringBuffer();
		byte[] cipherB = new byte[16];
		for (int i=0; i < rounds; i=i+2)
		{
			UInt64 myInt = ConvertBaseNToDecClear(32,dValue.substring(i*13,(i+1)*13));
			byte[] b = BitConverter(myInt);
			
			System.arraycopy(b,0,cipherB,0,8);
			
			
			myInt = ConvertBaseNToDecClear(32,dValue.substring((i+1)*13,(i+2)*13));
			b = BitConverter(myInt);
			
			System.arraycopy(b,0,cipherB,8,8);
			//String a = new String( cipher.decrypt(cipherB));
			//a = a.substring(0, a.indexOf(0));		
			
			byte[] ciph = cipher.decrypt(cipherB);
			int len =  (int) ciph[15];
			if ((len < 0) || (len > 16))
				throw new Exception ("Invalid key");
			sValue.append(new String( ciph, 0, len )); //.trim();
		}
		return sValue.toString();
		
	}
	
	
//	Converts a double into an array of bytes with length 
	// eight.
	private  byte[] BitConverter(UInt64 a)
	{
		UInt64 value = new UInt64(a.bigIntValue());
		byte[] w = new byte[8];
		
		w[0] = (byte) value.byteValue();
		value.shiftRight(8);
		w[1] = (byte) (value.byteValue());
		
		value = new UInt64(a.bigIntValue());
		value.shiftRight(16);
		w[2] = (byte) (value.byteValue());
		
		value = new UInt64(a.bigIntValue());
		value.shiftRight(24);
		w[3] = (byte) (value.byteValue());
		
		value = new UInt64(a.bigIntValue());
		value.shiftRight(32);
		w[4] = (byte) (value.byteValue());
		
		value = new UInt64(a.bigIntValue());
		value.shiftRight(40);
		w[5] = (byte) (value.byteValue());
		
		value = new UInt64(a.bigIntValue());
		value.shiftRight(48);
		w[6] = (byte) (value.byteValue());
		
		value = new UInt64(a.bigIntValue());
		value.shiftRight(56);
		w[7] = (byte) (value.byteValue());
		return w;
	}
	
	
	private UInt64 BitConverter(byte[] a)
	{
		UInt64 accum = new UInt64("0");
		long fff = 0;
		for ( int shiftBy=0; shiftBy<64; shiftBy+=8 )
		{
			long  b = a[shiftBy/8] & 0xff;
			b = b << shiftBy;
			fff = fff | b;
		}
		if (fff < 0)
		{
			accum = new UInt64(Long.toString(Long.MAX_VALUE));
			accum.add(new java.math.BigInteger(Long.toString(fff+2)));
			accum.add(new java.math.BigInteger(Long.toString(Long.MAX_VALUE)));
		}
		else
		{
			accum = new UInt64(Long.toString(fff));
		}
		return accum;
	}
	
	public static void main(String[] args) {
	}
	
}
