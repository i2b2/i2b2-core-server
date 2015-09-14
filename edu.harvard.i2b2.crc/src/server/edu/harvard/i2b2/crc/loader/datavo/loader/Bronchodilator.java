package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.util.Calendar;

public class Bronchodilator {
	private double noObserved;
	private double noPredicted;
	private double noPercentPred;
	private double afterObserved;
	private double afterPercentChange;
	private double afterPercentPred;
	private Calendar bronchodilatorDate;
	
	public double getAfterObserved() {
		return afterObserved;
	}
	public void setAfterObserved(double afterObserved) {
		this.afterObserved = afterObserved;
	}
	public double getAfterPercentChange() {
		return afterPercentChange;
	}
	public void setAfterPercentChange(double afterPercentChange) {
		this.afterPercentChange = afterPercentChange;
	}
	public double getAfterPercentPred() {
		return afterPercentPred;
	}
	public void setAfterPercentPred(double afterPercentPred) {
		this.afterPercentPred = afterPercentPred;
	}
	public double getNoObserved() {
		return noObserved;
	}
	public void setNoObserved(double noObserved) {
		this.noObserved = noObserved;
	}
	public double getNoPercentPred() {
		return noPercentPred;
	}
	public void setNoPercentPred(double noPercentPred) {
		this.noPercentPred = noPercentPred;
	}
	public double getNoPredicted() {
		return noPredicted;
	}
	public void setNoPredicted(double noPredicted) {
		this.noPredicted = noPredicted;
	}
	public Calendar getBronchodilatorDate() {
		return bronchodilatorDate;
	}
	public void setBronchodilatorDate(Calendar bronchodilatorDate) {
		this.bronchodilatorDate = bronchodilatorDate;
	}
}
