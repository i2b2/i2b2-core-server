package edu.harvard.i2b2.crc.exec;

public class MediumMain {

	public static void main(String args[]) throws InterruptedException {
		for (long i = 0; i < 100000; i++) {
			Thread.sleep(500);
			System.out.println("Calculate Total executed ..." + i);
		}
	}
}
