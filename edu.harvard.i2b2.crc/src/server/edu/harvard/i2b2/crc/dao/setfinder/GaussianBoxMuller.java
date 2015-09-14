package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class GaussianBoxMuller {

	public double getObsecationData(double originalValue, double totalValue,
			double rangeValue) {
		double obNum = 0.0;

		while ((obNum > rangeValue) || (obNum < (rangeValue * -1.0))) {
			obNum = Math.round(boxMuller(true, rangeValue, 0));
		}
		// if less than three, then return 0
		return (double) originalValue + obNum;

	}

	public double boxMuller(boolean iset, double stdDev, double mean) {
		double w = 0.0, x1 = 0.0, x2 = 0.0, y1 = 0.0, y2 = 0.0, obsData = 0.0;
		if (iset) {
			do {
				x1 = 2.0 * Math.random() - 1.0;
				x2 = 2.0 * Math.random() - 1.0;
				w = x1 * x1 + x2 * x2;
			} while (w >= 1.0);

			w = Math.sqrt((-2.0 * Math.log(w)) / w);

			y1 = x1 * w;
			y2 = x2 * w;
			obsData = y1;
		} else {
			iset = false;
			obsData = y2;
		}
		// System.out.println("obsData " + obsData);
		obsData = (obsData * stdDev) + mean;
		return obsData;
	}

	public long getNormalizedValueForCount(double originalValue,double sigma,double obfuscatedMinimumValue ) {
		double normalizedValue = 0.0;
		// read property file for the minimum cutoff
		double minimumCutoffValue = obfuscatedMinimumValue;
		// check if original value <3 then return zero
		if (originalValue <= minimumCutoffValue) {
			return (long) normalizedValue;
		}
		// read standard deviation from property file
		double stdDev = sigma;
		// read mean from property file
		double mean = 0;
		double boxMullerValue = boxMuller(true, stdDev , mean);
		
		/*
		while (normalizedValue <= 0.0) {
			normalizedValue = Math.round(originalValue + boxMullerValue);
		}
		*/
		
		
		normalizedValue = Math.round(originalValue + boxMullerValue);
		if (normalizedValue <= 0) { 
			return 0;
		} 
		return (long) normalizedValue;
	}

	public static double getCountSigma() throws I2B2Exception { 
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String countSigmaStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinderquery.obfuscation.count.sigma");
		return Double.parseDouble(countSigmaStr);
	}
	
	
	
	
	public static double getBreakdownCountSigma() throws I2B2Exception { 
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String breakdownCountSigmaStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinderquery.obfuscation.breakdowncount.sigma");
		return Double.parseDouble(breakdownCountSigmaStr);
	}
	
	public static double getObfuscatedMinimumVal() throws I2B2Exception { 
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String countSigmaStr = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinderquery.obfuscation.minimum.value");
		return Double.parseDouble(countSigmaStr);
	}
	
	public static void main(String[] args) {
		GaussianBoxMuller g = new GaussianBoxMuller();
		for (int i = 0; i < 400; i++) {
			double finalVal = 0.0d, boxMullerVal = -0.1;

			while (finalVal + boxMullerVal <= 0.0) {
				boxMullerVal = g.boxMuller(true, 1.3, 0);
			}
			finalVal = finalVal + boxMullerVal;
			// Random random = new Random(3);
			// double gaussianBlur = random.nextGaussian();
			// double finalObsData = g.getObsecationData(10, 3, 3);
			System.out.println(" *** value ***" + Math.round(finalVal));
			System.out.println(""+ g.getNormalizedValueForCount(4,150,3 ));
		}
		// System.out.println(" value " + String.valueOf(finalVal));
		// double finalObsData = g.getObsecationData(10, 3, 3);
		double testData = 0.42923558760362324 * 2.0;
		// System.out.println(" test data :" + testData);

		// System.out.println(" value " + String.valueOf(finalObsData));
	}
	
	

}
