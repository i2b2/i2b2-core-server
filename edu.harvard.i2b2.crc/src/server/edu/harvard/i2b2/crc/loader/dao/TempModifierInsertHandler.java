package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierType;

/**
 * Handle to insert temp obervation fact data. Wraps Springs batch inserter.
 * 
 * @author rk903
 * 
 */
public class TempModifierInsertHandler extends JdbcDaoSupport {
	private ModifierDAO.TempModifierInsert tempModifierInsert = null;

	public TempModifierInsertHandler(DataSource ds) {
		setDataSource(ds);
	}

	public TempModifierInsertHandler(
			ModifierDAO.TempModifierInsert tempModifierInsert) {
		this.tempModifierInsert = tempModifierInsert;
	}

	/**
	 * Submit individual inserts to batch.
	 * 
	 * @param observationFact
	 */
	public void insertModifier(ModifierType modifier) {
		tempModifierInsert.insert(modifier);
	}

	/**
	 * Manual submit of batched inserts.
	 */
	public void flush() {
		tempModifierInsert.flush();
		tempModifierInsert.reset();
	}

}
