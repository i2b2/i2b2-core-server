package edu.harvard.i2b2.crc.loader.util;

import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2Exception;

/**
 * Util class for fetching key file and 
 * parsing key information from key file.
 * 
 * @author rk903
 *
 */
public class KeyReaderUtil {

	/**
	 * EMPI and ENCOUNTER key property name 
	 */
	public static final String  EMPIENCKEY = "empienckey";
	/**
	 * NOTES key propery name
	 */
	public static final String NOTESKEY = "noteskey";
	
	/**
	 * Spring loader property name. 
	 */
	public static final String KEY_FETCH_COMMAND = "KeyFetchCommand";
	
	private String keyFetchCmd = null;
	
	/**
	 * Constructor accepts command line value to fetch key file
	 * @param cmdToFetchKey
	 */
	public KeyReaderUtil(String cmdToFetchKey) { 
		this.keyFetchCmd = cmdToFetchKey;
	}
	
	/**
	 * Static function to fetch default command line value to fetch key file
	 * @return
	 */
	public static String getKeyFetchCommand() { 
		CRCLoaderUtil loaderUtil = CRCLoaderUtil.getInstance();
		Map<String,String> loaderPropertyMap = (Map<String,String>)loaderUtil.getSpringBeanFactory().getBean("loaderproperty");
		String keyFetchCommand = (String)loaderPropertyMap.get(KeyReaderUtil.KEY_FETCH_COMMAND);
		return keyFetchCommand;
	}
	
	
	
	/**
	 * Return parsed key from key file, obtained by command line value
	 * @return
	 * @throws NoKeyException
	 */
	public String[] getFetchedKeys() throws NoKeyException {
		ExecUtil execUtil = new ExecUtil();
		Process process;
		String fetchedEmpiEncKey = null; 
		String fetchedNotesKey = null;
		try {
			process = execUtil.exec(keyFetchCmd);
			String keyFileInfo = execUtil.getProcessOutput(process);
			String keyFileErrorInfo = execUtil.getProcessError(process);
			if (keyFileErrorInfo != null
					&& keyFileErrorInfo.trim().length() > 1) {
				throw new NoKeyException("Error reading default key file"
						+ keyFileErrorInfo);
			}

			if (keyFileInfo == null) {
				throw new NoKeyException("Key file is null");
			}

			fetchedEmpiEncKey = getKey(EMPIENCKEY, keyFileInfo);
			fetchedNotesKey = getKey(NOTESKEY, keyFileInfo);

		} catch (I2B2Exception e) {
			throw new NoKeyException("Failed while reading key file"
					+ e.getMessage());
		}
		return new String[] {fetchedEmpiEncKey,fetchedNotesKey};
	}
	
	/**
	 * Return full key file text obtained by command line value
	 * @return
	 * @throws I2B2Exception
	 */
	public String getFullKeyFileText()  throws I2B2Exception { 
		ExecUtil execUtil = new ExecUtil();
		Process process;
		process = execUtil.exec(keyFetchCmd);
		String keyFileInfo = execUtil.getProcessOutput(process);
		return keyFileInfo;
	}

	/**
	 * Retrive key value based on key property name
	 * @param attName key property name
	 * @param keyFileInfo Full key file text
	 * @return
	 * @throws NoKeyException
	 */
	public String getKey(String attName, String keyFileInfo)
			throws NoKeyException {
		int notesStartIndex = -1;
		if (keyFileInfo.indexOf(attName + "=") > -1) {
			notesStartIndex = keyFileInfo.indexOf(attName + "=")
					+ attName.trim().length()+2;
		} else {
			throw new NoKeyException(attName
					+ " property not present in key file");
		}
		String attKey = keyFileInfo.substring(notesStartIndex,
				notesStartIndex + 16);
		return attKey;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String keyFetchCommand = KeyReaderUtil.getKeyFetchCommand();
		System.out.println("keyFetchCommand " +keyFetchCommand);
		KeyReaderUtil readerUtil = new KeyReaderUtil(keyFetchCommand);
		String[] keys = readerUtil.getFetchedKeys(); 
		System.out.println("Empi Key " + keys[0]);
		System.out.println("Notes Key " + keys[1]);
	}

}
