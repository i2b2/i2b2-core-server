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
