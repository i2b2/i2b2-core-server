/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
