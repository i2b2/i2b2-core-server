package edu.harvard.i2b2.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class XMLGregorianCalendarAdapterClass {
	private BigInteger year;
    private int month, day, timezone,  hour, minute, second;
    private BigDecimal fractionalSecond; 
    
    public BigInteger getYear() {
		return year;
	}
	public void setYear(BigInteger year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getTimezone() {
		return timezone;
	}
	public void setTimezone(int timezone) {
		this.timezone = timezone;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public int getSecond() {
		return second;
	}
	public void setSecond(int second) {
		this.second = second;
	}
	public BigDecimal getFractionalSecond() {
		return fractionalSecond;
	}
	public void setFractionalSecond(BigDecimal fractionalSecond) {
		this.fractionalSecond = fractionalSecond;
	}
       
}