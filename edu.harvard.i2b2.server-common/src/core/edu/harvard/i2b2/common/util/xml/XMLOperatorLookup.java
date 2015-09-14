package edu.harvard.i2b2.common.util.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author rkuttan
 *
 */
public class XMLOperatorLookup {
	
	static Map<String,String> operatorMap = new HashMap<String,String>();
	
	static { 
		operatorMap.put("GE", ">=");
		operatorMap.put("GT", ">");
		operatorMap.put("LT", "<");
		operatorMap.put("LE", "<=");
		operatorMap.put("EQ", "=");
		operatorMap.put("NE", "<>");
	}
	
	public static String getComparisonOperatorFromAcronum(String operatorAcronym) { 
		if (operatorAcronym == null) { 
			return null;
		}
		String comparisonOperator = operatorMap.get(operatorAcronym.toUpperCase());
		return comparisonOperator;
	}
}
