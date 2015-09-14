package edu.harvard.i2b2.ontology.delegate;

import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public interface RequestRunnable {
	public void runnable(Map parameterMap) throws I2B2Exception ;
}
