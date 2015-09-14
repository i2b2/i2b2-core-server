package edu.harvard.i2b2.crc.dao.pdo.input;

public interface PageMethod {
	public int calculateListSize(int maxInputList, long totalObservations,
			long pageSize);
}
