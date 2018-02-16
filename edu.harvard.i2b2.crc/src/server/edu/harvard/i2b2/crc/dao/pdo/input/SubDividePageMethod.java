package edu.harvard.i2b2.crc.dao.pdo.input;

public class SubDividePageMethod implements PageMethod {

	@Override
	public int calculateListSize(int maxInputList, long totalObservations,
			long pageSize) {
		int newMaxInputList = maxInputList / 2;
		return newMaxInputList;
	}

}
