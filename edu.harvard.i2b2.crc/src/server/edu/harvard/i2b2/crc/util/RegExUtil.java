package edu.harvard.i2b2.crc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util class for regular expression 
 */
public class RegExUtil {

	/**
	 * This function returns value within the "[" and "]" 
	 * @param operator
	 * @return string
	 */
	public static String getOperatorOption(String operator) { 
		 Pattern pattern = Pattern.compile("\\[.+\\]");
		 Matcher matcher = pattern.matcher(operator);
		 if (matcher.find())  {
			 return operator.substring(matcher.start(), matcher.end());
		 } else { 
			 return null;
		 }
	}
}
