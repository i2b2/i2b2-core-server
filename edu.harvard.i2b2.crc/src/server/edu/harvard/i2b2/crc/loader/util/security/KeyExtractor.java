package edu.harvard.i2b2.crc.loader.util.security;

import java.io.*;
import java.text.*;
import java.util.*;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import org.jdom.input.*;
import org.jdom.*;

//import edu.harvard.i2b2.util.Constant;


public class KeyExtractor {
	
	/**
	 * @param args
	 */
	
	private static String keyServer;
	private static String keyFile;
	private static String removableDeviceLocation = "A:";

	/**
	 * Will encrypt the master key on the floppy disk
	 * It uses the serial number on the logicaldisk as the key
	 * It thans uses the binhex to encrypt the master key
	 * 
	 */
	public static boolean setRemoveableDeviceKey(String masterKey, String clientHalfKey, String label, String comment) throws Exception
	{
		label = label.toUpperCase();
		if (clientHalfKey.length() != 8)
			throw new Exception("Key Length needs to be 8 characters");
		
		File findKey = new File(removableDeviceLocation + File.pathSeparator + keyFile);
		
		if (findKey.exists())
			throw new Exception("Master Key already exists on this device");
		
		//Check to see if half key already exists
		try 
		{
			getHalfKey(label);
			// Should of gotten a exception that it wasn't fonud. So it already exists
			throw new Exception(label + " already exists, select a different name");
		}
		catch (Exception e)
		{
			setHalfKey(label, clientHalfKey, comment);
			// Couldn't find it, so that is a good thing
		}
		//catch (Exception e)
		//{
		//	throw e;
		//}
		
		//ManagementObject disk = null;
		File output = null;
		BufferedWriter brWriter = null;
		try 
		{
			//TODO Convert to Java somehow
			/*
			 disk =
			 new ManagementObject("win32_logicaldisk.deviceid=\"A:\"");
			 disk.Get();
			 
			 string key = disk.GetPropertyValue("VolumeSerialNumber").ToString().Substring(0,8) + clientHalfKey;
			 
			 disk.SetPropertyValue("VolumeName", label);
			 disk.Put();
			 */
			
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128); // 192 and 256 bits may not be available
			
			// Generate the secret key specs.
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();
			
			
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			
			// Instantiate the cipher
			
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
			
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, skeySpec);
			
			byte[] encrypted =
				cipher.doFinal(masterKey.getBytes());
			String ciphertext =  asHex(encrypted);
			
			
			//RijndaelAlgorithm ra = new RijndaelAlgorithm(key,16,32);
			//String ciphertext =  ra.binhexEncrypt(masterKey); // RijndaelAlgorithm.Encrypt(plaintext,key); 
			output = new File(removableDeviceLocation + File.pathSeparator + keyFile);
			brWriter = new BufferedWriter(new PrintWriter(output));
			
			brWriter.write(ciphertext);
			brWriter.close();
			
			//Move current XML doc to backup
			java.util.Date currentdate = new java.util.Date();
			SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyy-hhmm", Locale.getDefault());
			
			// Move temp files
			output = new File(keyServer);
			output.renameTo(new File(keyServer + formatter.format(currentdate)));
			
			output = new File(keyServer + ".tmp");
			output.renameTo(new File(keyServer));
		} 
		catch (Exception e)
		{
			//if (brWriter != null)
			//	brWriter.close();
			throw e;
			//
		}
		return true;
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
	 * Private function that will retrieve the the other half of the master key from the 
	 * network share xml document
	 * 
	 * @paraminput String label
	 * @return String key
	 * 
	 */
	private static String getHalfKey(String label) throws Exception
	{
		Document doc = new Document();
		SAXBuilder builder = new SAXBuilder();
		String results = null;
		try 
		{
			doc = builder.build(new File(keyServer));
			List i2b2 = doc.getRootElement().getChildren(label);
			
			for (Iterator i = i2b2.iterator(); i.hasNext();)  {
				Element e = (Element)i.next();
				results= e.getChild("key").getText();
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		return results;
	}
	
	/**
	 * Private function that will retrieve the the other half of the master key from the 
	 * network share xml document
	 * 
	 */
	private static void setHalfKey(String label, String key, String comment) throws Exception
	{
		label = label.toUpperCase();
		//TODO lots of work
		if (getHalfKey(label) != null)
			throw new Exception("Key (" + label + ") already exists in file.");
		
		Document doc = new Document();
		SAXBuilder builder = new SAXBuilder();
		String results = null;
		try 
		{
			Element e = new Element(label);
			e.addContent(new Element("key").setText(key));
			e.addContent(new Element("comment").setText(comment));
			
			doc = builder.build(new File(keyServer));
			
            org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter();
			List list = doc.getRootElement().getChildren(label);
			list.add(e);
			
			
		} catch (Exception e)
		{
			throw e;
		}
		/*
		 XmlDocument XMLDoc = new XmlDocument();
		 XmlTextReader myXmlURLreader = new XmlTextReader(keyServer);
		 XmlUrlResolver resolver = new XmlUrlResolver();
		 resolver.Credentials = System.Net.CredentialCache.DefaultCredentials;
		 myXmlURLreader.XmlResolver = resolver;
		 XMLDoc.Load(myXmlURLreader);
		 
		 
		 XmlNode newFloppy = XMLDoc.CreateNode(XmlNodeType.Element, label.ToUpper(), "");
		 XmlNode newKey = XMLDoc.CreateNode(XmlNodeType.Element, "key", "");
		 newKey.InnerText = key;
		 newFloppy.AppendChild(newKey);
		 
		 XmlNode newComment = XMLDoc.CreateNode(XmlNodeType.Element, "comment", "");
		 newComment.InnerText = comment;
		 newFloppy.AppendChild(newComment);
		 
		 XMLDoc.DocumentElement.AppendChild(newFloppy);
		 XMLDoc.Save(keyServer + ".tmp");
		 
		 myXmlURLreader.Close();
		 */
	}
	
	/**
	 * Private function that will retrieve the master key from the floppy drive
	 * Will use the serial number on the device as the key
	 * 
	 */
	public static String getFloppyKey() throws Exception
	{
		return getFloppyKey(null, null);
	}
	/**
	 * Private function that will retrieve the master key from the floppy drive
	 * Will use the serial number on the device as the key
	 * 
	 */
	public static String getFloppyKey(String label, String key) throws Exception
	{
		String deKey = null;
		File inputFile = new File (removableDeviceLocation + File.separator + keyFile);
		if (inputFile.exists()) {
			try {
				FileReader ist = new FileReader(inputFile);
				
				//InputStream ist = new FileInputStream(removableDeviceLocation + File.pathSeparator + keyFile); 
				BufferedReader istream = new BufferedReader(ist); //new InputStreamReader(ist));	
				String text[] = new String[2];
				text[0] = istream.readLine(); //just read the first line in the text file
				text[1] = istream.readLine(); //Read in the label for the the file
				
				if (text[1] != null)
				{
					if (text[1].length() < 9)
						throw new Exception ("Floppy Key label must be greater than 8 characters");
					
					key = text[1].substring(text[1].length()-8); //Last 8 characters
					label = text[1].substring(0,text[1].length()-8);
					//text[1] = label;
				}
				
				key += getHalfKey(label);
				RijndaelAlgorithm ra = new RijndaelAlgorithm(key,128); //, "AES");
				deKey = ra.decrypt(text[0]);
				
			} catch (Exception e)
			{
			}
			return deKey;
		}
		else 
		{
			return null;
		}
		
		/*
		 if (File.Exists("A:\\" + msKeyFile))
		 {
		 Stream s = File.OpenRead("A:\\" + msKeyFile);
		 StreamReader sr = new StreamReader(s);
		 String lineIn = sr.ReadLine(); 
		 if( lineIn != null )
		 {
		 ManagementObject disk =
		 new ManagementObject("win32_logicaldisk.deviceid=\"A:\"");
		 disk.Get();
		 //old
		  //key = disk.GetPropertyValue("VolumeSerialNumber").ToString();
		   key = disk.GetPropertyValue("VolumeSerialNumber").ToString().Substring(0,8);
		   string label = disk.GetPropertyValue("VolumeName").ToString();
		   
		   key += getHalfKey(label);
		   
		   RijndaelAlgorithm ra = new RijndaelAlgorithm(key,16,32);
		   key = ra.binhexDecrypt(lineIn);
		   }
		   sr.Close();
		   s.Close();
		   return key;
		   }
		   else
		   return key;
		   */
	}
	public static void main(String[] args) {
		KeyExtractor keye = new KeyExtractor();
		try {
		keye.setKeyServer("\\\\plato\\Load_Programs\\Ceas\\ceas.xml");
		keye.setRemovableDeviceLocation("A:");
		keye.setKeyFile("A1234.txt");
		String mykey = keye.getFloppyKey(); //"MIKENEW");
		int i=0;
		} catch (Exception e)
		{e.printStackTrace();}
		// TODO Auto-generated method stub
		
	}
	
	public static String getKeyServer() {
		return keyServer;
	}
	
	
	public static void setKeyServer(String keyServer) {
		KeyExtractor.keyServer = keyServer;
	}
	
	public static String getRemovableDeviceLocation() {
		return removableDeviceLocation;
	}
	
	
	public static void setRemovableDeviceLocation(String removableDeviceLocation) {
		KeyExtractor.removableDeviceLocation = removableDeviceLocation;
	}
	
	public static String getKeyFile() {
		return keyFile;
	}
	
	public static void setKeyFile(String keyFile) {
		KeyExtractor.keyFile = keyFile;
	}
	
}
