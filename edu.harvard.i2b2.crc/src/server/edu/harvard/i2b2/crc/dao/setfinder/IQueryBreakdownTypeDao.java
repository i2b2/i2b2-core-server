package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;

public interface IQueryBreakdownTypeDao {

	public QtQueryBreakdownType getBreakdownTypeByName(String name);

}
