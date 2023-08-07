/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.datavo.loader;
import java.util.Calendar;


public class Pulmonary {
	private double height;
	private double wieght;
	private Bronchodilator firstSecVC;
	private Bronchodilator vitalCapacity;
	private Calendar reportDate; 
	
	public Bronchodilator getFirstSecVC() {
		return firstSecVC;
	}
	public void setFirstSecVC(Bronchodilator firstSecVC) {
		this.firstSecVC = firstSecVC;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public Bronchodilator getVitalCapacity() {
		return vitalCapacity;
	}
	public void setVitalCapacity(Bronchodilator vitalCapacity) {
		this.vitalCapacity = vitalCapacity;
	}
	public double getWieght() {
		return wieght;
	}
	public void setWieght(double wieght) {
		this.wieght = wieght;
	}
	public Calendar getReportDate() {
		return reportDate;
	}
	public void setReportDate(Calendar reportDate) {
		this.reportDate = reportDate;
	}
}
