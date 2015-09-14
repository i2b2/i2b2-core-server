package edu.harvard.i2b2.crc.util;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;



public class ItemKeyUtil {
	
	public static final String ITEM_KEY_PATIENT_SET = "patient_set_coll_id:";
	public static final String ITEM_KEY_PATIENT_ENCOUNTER_SET = "patient_set_enc_id:";
	public static final String ITEM_KEY_MASTERID = "masterid:";
	public static final String ITEM_KEY_PATIENT = "patient:";
	public static final String ITEM_KEY_ENCOUNTER = "encounter:";
	public static final String ITEM_KEY_CELLID = "cellurl:";
	
	public static String getItemTabel(String itemKey) throws I2B2Exception { 
	  String itemTableFromKey = null;
      if (itemKey == null) { 
      	throw new I2B2DAOException("Item key is null");
      }
      if (itemKey.indexOf("\\\\") != 0) {
      	throw new I2B2DAOException("Item key [" + itemKey + "] is not in the correct format");
      }
      
      int end = itemKey.indexOf("\\", 3);
      itemTableFromKey = itemKey.substring(2, end).trim();
      return itemTableFromKey;
	}
	
	 public static String getItemPath(String itemKey) throws I2B2DAOException {
    	 if (itemKey == null) { 
         	throw new I2B2DAOException("Item key path is null");
         }
         if (itemKey.indexOf("\\\\") != 0) {
         	throw new I2B2DAOException("Item key full path [" + itemKey + "] is not in the correct format");
         }
        int end = itemKey.indexOf("\\", 3);
        return itemKey.substring(end).trim();
    }
	 
	 public static boolean isConceptKey(String itemKey) { 
		 if (itemKey != null) {
			 if (itemKey.trim().toLowerCase().startsWith(ITEM_KEY_PATIENT_SET) || 
					 itemKey.trim().toLowerCase().startsWith(ITEM_KEY_PATIENT_ENCOUNTER_SET) ||
					 itemKey.trim().toLowerCase().startsWith(ITEM_KEY_ENCOUNTER) ||
					 itemKey.trim().toLowerCase().startsWith(ITEM_KEY_MASTERID) ||
					 itemKey.trim().toLowerCase().startsWith(ITEM_KEY_PATIENT)) {
				 return false;
			 } else { 
				 return true;
			 }
			 
		 } else { 
			 return false;
		 }
	 }
	
	
}
