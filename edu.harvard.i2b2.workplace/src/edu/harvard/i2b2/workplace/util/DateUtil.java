/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 * 		Raj Kuttan
 * 
 */
package edu.harvard.i2b2.workplace.util;

import java.text.DateFormat;


public class DateUtil {

	public static String dateToString(){

		return DateFormat.getInstance().format(System.currentTimeMillis());
	}
}